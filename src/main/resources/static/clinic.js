let role=new URLSearchParams(location.search).get('role')||'ADMIN';
if(!['ADMIN','PATIENT','DOCTOR'].includes(role))role='ADMIN';
let session=null;
const $=id=>document.getElementById(id);
function notice(text,error=false){$('notice').textContent=text;$('notice').className=error?'error':'';}
async function api(path,options={}){
 const response=await fetch('/api'+path,{...options,headers:{'Content-Type':'application/json',...(session?{Authorization:'Bearer '+session.token}:{}),...options.headers}});
 const body=await response.json();if(!response.ok)throw Error(body.error||'Request failed');return body;
}
function portal(next){role=next;session=null;$('login-panel').hidden=false;$('workspace').hidden=true;$('logout').hidden=true;$('login-form').reset();notice('');
 document.querySelectorAll('[data-role]').forEach(b=>b.setAttribute('aria-pressed',String(b.dataset.role===role)));
 $('portal-title').textContent=role[0]+role.slice(1).toLowerCase()+' portal';
 $('portal-description').textContent={ADMIN:'Sign in to manage doctors and clinic availability.',PATIENT:'Sign in to find a doctor and manage your appointments.',DOCTOR:'Sign in to review appointments and record prescriptions.'}[role];
 history.replaceState(null,'','?role='+role);
}
document.querySelectorAll('[data-role]').forEach(b=>b.onclick=()=>portal(b.dataset.role));$('logout').onclick=()=>portal(role);
function element(tag,text){const e=document.createElement(tag);e.textContent=text;return e;}
async function doctors(query=''){
 const data=await api('/doctors'+query);$('doctors').replaceChildren();$('doctor-results').hidden=false;
 if(!data.doctors.length){$('doctors').append(element('p','No matching doctors. Try another name or specialty.'));return;}
 for(const d of data.doctors){const row=element('article','');row.className='doctor-row';const details=element('div','');details.append(element('h3',d.name),element('p',d.specialty+' · '+d.email));row.append(details);
   if(role==='PATIENT'){
    const date=$('search-date').value;const availability=await api('/doctors/'+d.id+'/availability/'+date);const slots=element('div','');slots.className='slots';
    for(const time of availability.availableTimes){const b=element('button','Book '+time);b.onclick=async()=>{b.disabled=true;try{await api('/appointments',{method:'POST',body:JSON.stringify({doctorId:d.id,appointmentTime:date+'T'+time+':00',reason:'Demo consultation'})});notice('Appointment booked with '+d.name+' at '+time);await doctors(query);await appointments();}catch(e){notice(e.message,true);}finally{b.disabled=false;}};slots.append(b);}
    if(!availability.availableTimes.length)slots.append(element('p','No available slots on this date.'));details.append(slots);
   }else row.append(element('p','Available: '+d.availableTimes.join(', ')));
   $('doctors').append(row);
 }
}
async function appointments(){const data=await api('/appointments');$('appointments').replaceChildren();
 if(!data.appointments.length){$('appointments').append(element('p','No appointments booked.'));return;}
 const wrap=element('div','');wrap.className='table-wrap';const table=element('table','');const head=element('tr','');for(const h of ['ID','Patient','Doctor','Date and time','Status'])head.append(element('th',h));const thead=element('thead','');thead.append(head);table.append(thead);const tbody=element('tbody','');
 for(const a of data.appointments){const tr=element('tr','');for(const value of [a.id,a.patient.name,a.doctor.name,a.appointmentTime.replace('T',' '),a.status])tr.append(element('td',value));tbody.append(tr);}table.append(tbody);wrap.append(table);$('appointments').append(wrap);
}
$('login-form').onsubmit=async e=>{e.preventDefault();const b=e.submitter;b.disabled=true;notice('Signing in…');try{session=await api('/auth/login',{method:'POST',body:JSON.stringify({role,email:$('email').value,password:$('password').value})});$('password').value='';$('login-panel').hidden=true;$('workspace').hidden=false;$('logout').hidden=false;$('workspace-title').textContent=$('portal-title').textContent;$('welcome').textContent='Welcome, '+session.name;$('role-badge').textContent=role;for(const id of ['admin-work','patient-work','appointment-work','prescription-work','doctor-results'])$(id).hidden=true;$('admin-work').hidden=role!=='ADMIN';$('patient-work').hidden=role!=='PATIENT';$('appointment-work').hidden=role==='ADMIN';$('prescription-work').hidden=role!=='DOCTOR';notice('Signed in successfully.');if(role==='ADMIN')await doctors();else await appointments();}catch(error){notice(error.message,true);}finally{b.disabled=false;}};
$('doctor-form').onsubmit=async e=>{e.preventDefault();const b=e.submitter;b.disabled=true;try{const d=Object.fromEntries(new FormData(e.target));d.availableTimes=d.availableTimes.split(',').map(s=>s.trim());await api('/doctors',{method:'POST',body:JSON.stringify(d)});notice('Doctor added successfully: '+d.name);e.target.reset();await doctors();}catch(error){notice(error.message,true);}finally{b.disabled=false;}};
$('search-form').onsubmit=async e=>{e.preventDefault();const b=e.submitter;b.disabled=true;try{const p=new URLSearchParams();for(const [k,v]of new FormData(e.target))if(v)p.set(k,v);await doctors('?'+p);notice('Search complete.');}catch(error){notice(error.message,true);}finally{b.disabled=false;}};
$('refresh-appointments').onclick=()=>appointments().catch(e=>notice(e.message,true));
$('prescription-form').onsubmit=async e=>{e.preventDefault();const b=e.submitter;b.disabled=true;try{const d=Object.fromEntries(new FormData(e.target));d.appointmentId=Number(d.appointmentId);await api('/prescriptions',{method:'POST',body:JSON.stringify(d)});notice('Prescription saved successfully.');e.target.reset();}catch(error){notice(error.message,true);}finally{b.disabled=false;}};
const tomorrow=new Date();tomorrow.setDate(tomorrow.getDate()+1);$('search-date').value=tomorrow.getFullYear()+'-'+String(tomorrow.getMonth()+1).padStart(2,'0')+'-'+String(tomorrow.getDate()).padStart(2,'0');portal(role);
