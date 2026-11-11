# End-to-end demo through the API gateway (PowerShell 5+).  Run:  .\scripts\demo-flow.ps1
$ErrorActionPreference = "Stop"
$gw = "http://localhost:8080/api"

function Login($u) {
    $r = Invoke-RestMethod -Method Post -Uri "$gw/auth/login" -ContentType "application/json" `
         -Body (@{ username = $u; password = "Password@123" } | ConvertTo-Json)
    return @{ Authorization = "Bearer $($r.token)" }
}
function Call($method, $path, $headers, $body = $null) {
    if ($body) { return Invoke-RestMethod -Method $method -Uri "$gw$path" -Headers $headers -ContentType "application/json" -Body ($body | ConvertTo-Json -Depth 5) }
    return Invoke-RestMethod -Method $method -Uri "$gw$path" -Headers $headers
}
function Step($t) { Write-Host "`n=== $t ===" -ForegroundColor Cyan }
function Wait-Events { Start-Sleep -Seconds 4 }

$doc  = Login "dr.smith"
$lab  = Login "lab.tech"
$rec  = Login "reception"
$doc2 = Login "dr.jones"

Step "1. Doctor starts the Diabetes Review pathway (episode-scoped tracking begins now)"
$paths = Call Get "/care/pathways" $doc
$ep = Call Post "/care/episodes" $doc @{ patientId = 1; pathwayId = ($paths | Where-Object { $_.pathway.code -eq "DIABETES_REVIEW" }).pathway.id }
"Episode id: $($ep.episode.id)"

Step "2. Doctor orders a lab test for patient 1"
$order = Call Post "/clinical/lab-orders" $doc @{ patientId = 1; testName = "HbA1c"; priority = "URGENT" }
$order | Format-List id, testName, status

Step "3. Lab technician uploads the report (event LAB_REPORT_UPLOADED -> Kafka)"
$report = Call Post "/clinical/lab-reports" $lab @{ labOrderId = $order.id; resultSummary = "HbA1c 9.1% - above target"; abnormal = $true }
Wait-Events

Step "4. Care gap detected: lab report not reviewed"
Call Get "/care/gaps?patientId=1&status=OPEN" $doc | Format-Table id, gapType, severity, description

Step "5. Receptionist books a follow-up, then it is marked MISSED"
$appt = Call Post "/appointments" $rec @{ patientId = 1; doctorName = "Dr. Alice Smith"; department = "Endocrinology"; scheduledAt = (Get-Date).AddDays(7).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ"); reason = "Review HbA1c"; followUp = $true }
Call Post "/appointments/$($appt.id)/missed" $rec @{ notes = "No show" } | Out-Null
Wait-Events
"Open gaps now (expect LAB_REPORT_UNREVIEWED + MISSED_FOLLOWUP + COMBINED):"
Call Get "/care/gaps?patientId=1&status=OPEN" $doc | Format-Table id, gapType, severity

Step "6. Dr. Smith hands the patient to Dr. Jones - open gaps are attached automatically"
$h = Call Post "/handoffs" $doc @{ patientId = 1; toDoctor = "dr.jones"; toDepartment = "Endocrinology"; reason = "Shift change"; clinicalSummary = "Poorly controlled diabetes, follow up urgently." }
"Handoff $($h.handoff.id) created with $($h.items.Count) pending items"

Step "7. Continuity verification must FAIL now"
Call Get "/care/episodes/$($ep.episode.id)/verify" $doc | ConvertTo-Json -Depth 5

Step "8. Resolve everything: review report, re-book, acknowledge handoff, complete follow-up"
Call Post "/clinical/lab-reports/$($report.id)/review" $doc @{ comment = "Adjust medication" } | Out-Null
$appt2 = Call Post "/appointments" $rec @{ patientId = 1; doctorName = "Dr. Bob Jones"; department = "Endocrinology"; scheduledAt = (Get-Date).AddDays(3).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ"); reason = "Rebooked review"; followUp = $true }
Call Post "/handoffs/$($h.handoff.id)/acknowledge" $doc2 @{ comment = "Received, will follow up" } | Out-Null
Call Post "/appointments/$($appt2.id)/complete" $doc2 @{ notes = "Therapy adjusted" } | Out-Null
Call Post "/clinical/notes" $doc2 @{ patientId = 1; noteType = "PROGRESS"; content = "Patient counselled." } | Out-Null
Wait-Events

Step "9. Verification should now PASS, then close the episode"
$v = Call Get "/care/episodes/$($ep.episode.id)/verify" $doc
"verified = $($v.verified)"
Call Post "/care/episodes/$($ep.episode.id)/close" $doc @{ notes = "All care activities completed" } | Out-Null
"Episode closed."

Step "10. Patient journey timeline"
Call Get "/care/timeline/1" $doc | Format-Table occurredAt, eventType, summary -AutoSize
