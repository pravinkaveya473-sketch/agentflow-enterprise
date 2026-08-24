param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$Password = "SmokeTest123!"
)

$ErrorActionPreference = "Stop"

function Assert-True([bool]$Condition, [string]$Message) {
    if (-not $Condition) {
        throw "Smoke test failed: $Message"
    }
}

$Suffix = [Guid]::NewGuid().ToString("N").Substring(0, 8)
$Username = "smoke_$Suffix"

Write-Host "[1/9] Health"
$Health = Invoke-RestMethod -Method Get -Uri "$BaseUrl/actuator/health"
Assert-True ($Health.status -eq "UP") "application health is not UP"

Write-Host "[2/9] Register $Username"
$Register = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/auth/register" `
    -ContentType "application/json" `
    -Body (@{ username = $Username; password = $Password } | ConvertTo-Json)
Assert-True $Register.success "registration failed"
$Token = $Register.data.accessToken
$Headers = @{ Authorization = "Bearer $Token" }

Write-Host "[3/9] Create project"
$Project = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/projects" `
    -Headers ($Headers + @{ "X-Idempotency-Key" = "project-$Suffix" }) `
    -ContentType "application/json" `
    -Body (@{ name = "Smoke Project $Suffix"; description = "AgentFlow automated smoke test" } | ConvertTo-Json)
Assert-True ($Project.data.status -eq "ACTIVE") "project should be ACTIVE"
$ProjectId = $Project.data.id

Write-Host "[4/9] Create task"
$Deadline = (Get-Date).AddDays(2).ToString("yyyy-MM-ddTHH:mm:ss")
$Task = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/tasks" `
    -Headers ($Headers + @{ "X-Idempotency-Key" = "task-$Suffix" }) `
    -ContentType "application/json" `
    -Body (@{
        projectId = $ProjectId
        name = "Verify AgentFlow task lifecycle"
        description = "Created by smoke-test.ps1"
        assigneeUsername = $Username
        deadline = $Deadline
        priority = "HIGH"
    } | ConvertTo-Json)
Assert-True ($Task.data.status -eq "CREATED") "task should be CREATED"
$TaskId = $Task.data.id

Write-Host "[5/9] Start task"
$Started = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/tasks/$TaskId/start" -Headers $Headers
Assert-True ($Started.data.status -eq "IN_PROGRESS") "task should be IN_PROGRESS"

Write-Host "[6/9] Block and resume task"
$Blocked = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/tasks/$TaskId/block" `
    -Headers $Headers -ContentType "application/json" `
    -Body (@{ reason = "Smoke-test dependency" } | ConvertTo-Json)
Assert-True ($Blocked.data.status -eq "BLOCKED") "task should be BLOCKED"
$Resumed = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/tasks/$TaskId/resume" -Headers $Headers
Assert-True ($Resumed.data.status -eq "IN_PROGRESS") "task should resume to IN_PROGRESS"

Write-Host "[7/9] Complete task"
$Completed = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/tasks/$TaskId/complete" -Headers $Headers
Assert-True ($Completed.data.status -eq "COMPLETED") "task should be COMPLETED"

Write-Host "[8/9] Verify core audit trail"
$Activities = Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/v1/tasks/$TaskId/activities" -Headers $Headers
$Types = @($Activities.data | ForEach-Object { $_.type })
foreach ($Expected in @("CREATED", "STARTED", "BLOCKED", "RESUMED", "COMPLETED")) {
    Assert-True ($Types -contains $Expected) "missing activity $Expected"
}

Write-Host "[9/9] Verify Kafka event read model"
$Events = @()
for ($Attempt = 1; $Attempt -le 20; $Attempt++) {
    $Events = @(Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/v1/events" -Headers $Headers |
        Where-Object { $_.aggregateId -eq $TaskId })
    $EventTypes = @($Events | ForEach-Object { $_.eventType })
    if ($EventTypes -contains "TASK_COMPLETED") { break }
    Start-Sleep -Milliseconds 750
}
foreach ($Expected in @("TASK_CREATED", "TASK_STARTED", "TASK_BLOCKED", "TASK_RESUMED", "TASK_COMPLETED")) {
    Assert-True ($EventTypes -contains $Expected) "Kafka read model is missing event $Expected"
}

Write-Host "Smoke test passed. projectId=$ProjectId taskId=$TaskId user=$Username kafkaEvents=$($EventTypes.Count)"
