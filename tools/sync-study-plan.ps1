# Copies the root learning-plan markdown into the app assets so the study-plan tab shows the latest version.
$root = Join-Path $PSScriptRoot '..'
# Matched by pattern to keep this script ASCII-only (Windows PowerShell 5 reads BOM-less files as ANSI).
$source = Get-ChildItem -Path $root -Filter 'LangChain_LangGraph_LangSmith_4*.md' | Select-Object -First 1 -ExpandProperty FullName
$target = Join-Path $root 'app\src\main\assets\docs\study_plan.md'

if (-not $source) { Write-Error "Learning plan markdown not found in $root"; exit 1 }
New-Item -ItemType Directory -Force (Split-Path $target) | Out-Null
Copy-Item $source $target -Force
Write-Host "Synced study plan -> $target"
