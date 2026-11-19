"""Run real MySQL reports and curl GET requests against the local coursework app."""
from pathlib import Path
from datetime import date,timedelta
import json,subprocess,urllib.request

root=Path(__file__).resolve().parents[1]
out=root/'evidence';out.mkdir(exist_ok=True)
mysql=root/'.tools/mysql-8.4.13-winx64/bin/mysql.exe'
options=[str(mysql),f'--defaults-extra-file={root / "runtime/mysql-client.cnf"}','--table','smart_clinic']
# Only install reporting procedures when absent; do not drop any existing data.
check=subprocess.check_output(options[:2]+['--batch','--skip-column-names','-e',"SELECT COUNT(*) FROM information_schema.routines WHERE routine_schema='smart_clinic' AND routine_type='PROCEDURE';"],text=True).strip()
if check=='0':subprocess.run(options,input=(root/'sql/reporting.sql').read_text(),text=True,check=True)
day=date.today()+timedelta(days=1)
queries={
19:'SHOW TABLES;',
20:'SELECT id, name, email, phone FROM patient ORDER BY id LIMIT 5;',
21:f"CALL GetDailyAppointmentReportByDoctor('{day}');",
22:f'CALL GetDoctorWithMostPatientsByMonth({day.year}, {day.month});',
23:f'CALL GetDoctorWithMostPatientsByYear({day.year});'
}
for q,sql in queries.items():
    result=subprocess.check_output(options+['-e',sql],text=True)
    (out/f'{q}-sql.txt').write_text('mysql> '+sql+'\n'+result,encoding='utf8')
base='http://127.0.0.1:18770'
req=urllib.request.Request(base+'/api/auth/login',data=json.dumps({'role':'PATIENT','email':'patient1@example.test','password':'DemoPatient!2026'}).encode(),headers={'Content-Type':'application/json'})
with urllib.request.urlopen(req) as response:token=json.load(response)['token']
for q,path in {24:'/api/doctors',25:'/api/appointments',26:'/api/doctors?specialty=Cardiology&time=09%3A00'}.items():
    command=['curl.exe','--silent','--show-error','--fail-with-body',base+path,'-H','Authorization: Bearer '+token]
    result=subprocess.check_output(command,text=True)
    data=json.loads(result)
    intro=''
    if q==25:intro='Authenticated as patient1@example.test using POST /api/auth/login with role PATIENT and the documented synthetic demo password. The returned token is represented by $TOKEN below.\n'
    (out/f'{q}-curl.txt').write_text(intro+'curl "'+base+path+'" -H "Authorization: Bearer $TOKEN"\n\n'+json.dumps(data,indent=2,ensure_ascii=False)+'\n',encoding='utf8')
(out/'tests.txt').write_text((root/'target/surefire-reports/com.smartclinic.ClinicIntegrationTest.txt').read_text(),encoding='utf8')
print('Collected real SQL outputs for Q19–23 and curl outputs for Q24–26.')
