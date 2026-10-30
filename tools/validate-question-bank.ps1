param([int]$Week = 0)

# Validates the whole question bank (main + extra files per week) with the same rules as QuestionBankValidator.
$dir = Join-Path $PSScriptRoot '..\app\src\main\assets\questions'
function Load($name) { Get-Content (Join-Path $dir $name) -Raw -Encoding UTF8 | ConvertFrom-Json }

$types = 'SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'CODE_READING', 'CODE_COMPLETION', 'DEBUGGING'
$diffs = 'BEGINNER', 'INTERMEDIATE', 'ADVANCED'
$errs = New-Object System.Collections.Generic.List[string]
$allTopics = @{}
$allQuestions = @{}
$manifest = Load 'manifest.json'

foreach ($w in $manifest.weeks) {
    $files = @($w.file) + @($w.extraFiles | Where-Object { $_ })
    $topics = @()
    $questions = @()
    foreach ($f in $files) {
        if (-not (Test-Path (Join-Path $dir $f))) { $errs.Add("missing file $f"); continue }
        try { $d = Load $f } catch { $errs.Add("$f : invalid JSON - $($_.Exception.Message)"); continue }
        if ($d.week -ne $w.week) { $errs.Add("$f : declares week $($d.week), expected $($w.week)") }
        $topics += @($d.topics | Where-Object { $_ })
        $questions += @($d.questions | Where-Object { $_ })
    }

    foreach ($t in $topics) {
        if ($allTopics.ContainsKey($t.id)) { $errs.Add("duplicate topic id $($t.id)") } else { $allTopics[$t.id] = 1 }
        if (-not $t.notes -or @($t.notes).Count -lt 3) { $errs.Add("topic $($t.id) : notes missing or shorter than 3 lines") }
    }
    $weekTopicIds = @($topics | ForEach-Object { $_.id })

    $diffCounts = @{}
    $typeCounts = @{}
    foreach ($q in $questions) {
        $id = $q.id
        if ($allQuestions.ContainsKey($id)) { $errs.Add("duplicate question id $id") } else { $allQuestions[$id] = 1 }
        if ($q.topicId -notin $weekTopicIds) { $errs.Add("$id : topicId '$($q.topicId)' is not a topic of week $($w.week)") }
        if ($q.type -notin $types) { $errs.Add("$id : unknown type '$($q.type)'") } else { $typeCounts[$q.type] = 1 + [int]$typeCounts[$q.type] }
        if ($q.difficulty -notin $diffs) { $errs.Add("$id : unknown difficulty '$($q.difficulty)'") } else { $diffCounts[$q.difficulty] = 1 + [int]$diffCounts[$q.difficulty] }
        if (-not $q.title) { $errs.Add("$id : blank title") }
        if (-not $q.explanation) { $errs.Add("$id : blank explanation") }

        $optionIds = @($q.options | ForEach-Object { $_.id })
        if ($optionIds.Count -lt 2) { $errs.Add("$id : needs at least 2 options") }
        if (($optionIds | Select-Object -Unique).Count -ne $optionIds.Count) { $errs.Add("$id : duplicate option ids") }
        $optionTexts = @($q.options | ForEach-Object { $_.text })
        if (($optionTexts | Select-Object -Unique).Count -ne $optionTexts.Count) { $errs.Add("$id : duplicate option texts") }

        $correct = @($q.correctAnswer | Where-Object { $_ })
        if ($correct.Count -eq 0) { $errs.Add("$id : correctAnswer is empty") }
        foreach ($c in $correct) { if ($c -notin $optionIds) { $errs.Add("$id : correct answer '$c' is not an option") } }
        if ($q.type -eq 'SINGLE_CHOICE' -and $correct.Count -ne 1) { $errs.Add("$id : SINGLE_CHOICE needs exactly one correct answer") }
        if ($q.type -eq 'MULTIPLE_CHOICE' -and $correct.Count -lt 2) { $errs.Add("$id : MULTIPLE_CHOICE needs at least two correct answers") }

        $isCode = $q.type -in 'CODE_READING', 'CODE_COMPLETION', 'DEBUGGING'
        if ($isCode -and -not $q.codeSnippet) { $errs.Add("$id : $($q.type) needs a codeSnippet") }
        if (-not $isCode -and $q.codeSnippet) { $errs.Add("$id : non-code question must not have a codeSnippet") }
    }

    if ($Week -eq 0 -or $Week -eq $w.week) {
        $d = ($diffs | ForEach-Object { "$($_.Substring(0,3))=$([int]$diffCounts[$_])" }) -join ' '
        $t = ($types | ForEach-Object { "$($_.Substring(0,4))=$([int]$typeCounts[$_])" }) -join ' '
        "week {0,2}: topics={1,2} questions={2,3}  {3}  {4}" -f $w.week, $topics.Count, $questions.Count, $d, $t
    }
}

"total topics=$($allTopics.Count) questions=$($allQuestions.Count) errors=$($errs.Count)"
$errs | ForEach-Object { "ERROR: $_" }
if ($errs.Count -gt 0) { exit 1 }
