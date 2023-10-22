if (!(Test-Path outJ.csv))
{
    New-Item outJ.csv -ItemType File
    Set-Content outJ.csv 'Numar polinoame,Exponent Maxim,Numar maxim de monoame,Capacitate coada,Producatori,Consumatori,Timp executie'
}

$nrRuns = 10
$nrProducersValues = 0, 2, 3, 2, 3, 2, 3
$nrConsumersValues = 0, 2, 1, 4, 3, 6, 5
$queueCapacityValues = 20, 30
$nrPolynomialsValues = 10, 5
$maxGradeValues = 1000, 10000
$maxMonomNrValues = 50, 100

for($caseIndex = 0; $caseIndex -lt 2; $caseIndex++){
    Write-Host "Running for case $( $caseIndex )"

    $queueCapacity = $queueCapacityValues[$caseIndex]
    $nrPolynomials = $nrPolynomialsValues[$caseIndex]
    $maxGrade = $maxGradeValues[$caseIndex]
    $maxMonomNr = $maxMonomNrValues[$caseIndex]

    for($polynomialIndex = 0; $polynomialIndex -lt $nrPolynomials; $polynomialIndex++){
        Copy-Item "$( $caseIndex )_$( $polynomialIndex ).txt" "$( $polynomialIndex ).txt" -Force
    }

    for ($threadsIndex = 0; $threadsIndex -lt 7; $threadsIndex++){

        $nrProducers = $nrProducersValues[$threadsIndex]
        $nrConsumers = $nrConsumersValues[$threadsIndex]

        Write-Host "Running for case $( $caseIndex ) with $( $nrProducers ) producers and $( $nrConsumers ) consumers"

        $suma = 0
        for ($runNumber = 0; $runNumber -lt $nrRuns; $runNumber++){
            Write-Host "Rulare" ($runNumber + 1)
            $a = java "Main" $nrProducers $nrConsumers $queueCapacity $nrPolynomials
            Write-Host $a[$a.length - 1]
            $suma += $a[$a.length - 1]
            Write-Host ""
            if ($nrProducers -ne 0)
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

        if ($nrProducers -eq 0)
        {
            $nrProducers = "secvential"
            $nrConsumers = "secvential"
            Copy-Item "out.txt" "out_correct.txt" -Force
        }

        # Append
        Add-Content outJ.csv "$( $nrPolynomials ),$( $maxGrade ),$( $maxMonomNr ),$( $queueCapacity ),$( $nrProducers ),$( $nrConsumers ),$( $media )"
    }
}