if (!(Test-Path outJ.csv))
{
    New-Item outJ.csv -ItemType File
    Set-Content outJ.csv 'Numar polinoame,Exponent Maxim,Numar maxim de monoame,Nr threads,Timp executie'
}

$nrRuns = 10
$threads = 0, 4, 6, 8
$nrPolynomialsValues = 10, 5
$maxGradeValues = 1000, 10000
$maxMonomNrValues = 50, 100

for($caseIndex = 0; $caseIndex -lt 2; $caseIndex++){
    Write-Host "Running for case $( $caseIndex )"

    $nrPolynomials = $nrPolynomialsValues[$caseIndex]
    $maxGrade = $maxGradeValues[$caseIndex]
    $maxMonomNr = $maxMonomNrValues[$caseIndex]

    for($polynomialIndex = 0; $polynomialIndex -lt $nrPolynomials; $polynomialIndex++){
        Copy-Item "$( $caseIndex )_$( $polynomialIndex ).txt" "$( $polynomialIndex ).txt" -Force
    }

    for ($threadsIndex = 0; $threadsIndex -lt 4; $threadsIndex++){

        $nrThreaduri = $threads[$threadsIndex]

        Write-Host "Running for case $( $caseIndex ) with $( $nrThreaduri ) threads"

        $suma = 0
        for ($runNumber = 0; $runNumber -lt $nrRuns; $runNumber++){
            Write-Host "Rulare" ($runNumber + 1)
            $a = java "Main" $nrThreaduri $nrPolynomials
            Write-Host $a[$a.length - 1]
            $suma += $a[$a.length - 1]
            Write-Host ""
            if ($nrThreaduri -ne 0)
            {
                if ((Get-FileHash "out.txt").Hash -ne (Get-FileHash "out_correct.txt").Hash)
                {
                    Write-Host "Testing failed"
                    Exit
                }
            }
        }
        $media = $suma / $nrRuns

        Write-Host "Timp de executie mediu:" $media

        if ($nrThreaduri -eq 0)
        {
            $nrThreaduri = "secvential"
            Copy-Item "out.txt" "out_correct.txt" -Force
        }

        # Append
        Add-Content outJ.csv "$( $nrPolynomials ),$( $maxGrade ),$( $maxMonomNr ),$( $nrThreaduri ),$( $media )"
    }
}