if (!(Test-Path outC.csv))
{
    New-Item outC.csv -ItemType File
    #Scrie date in csv
    Set-Content outC.csv 'Tip Numere,Implementare,Nr procese,Timp executie'
}

$nrRuns = 10
$processesCounts = 2, 4, 8, 16
$N1Sizes = 18, 1000, 100
$N2Sizes = 18, 1000, 100000
$implementations = 'MPI_V0', 'MPI_V1', 'MPI_V2', 'MPI_V3_1', 'MPI_V3_2'

for($caseIndex = 0; $caseIndex -lt 3; $caseIndex++){
    Write-Host "Running for case $( $caseIndex )"

    $filename1 = "$( $caseIndex )_Numar1.txt"
    $filename2 = "$( $caseIndex )_Numar2.txt"
    Copy-Item $filename1 "Numar1.txt" -Force
    Copy-Item $filename2 "Numar2.txt" -Force

    $N1Size = $N1Sizes[$caseIndex]
    $N2Size = $N2Sizes[$caseIndex]

    for ($implementationIndex = 0; $implementationIndex -lt 5; $implementationIndex++){
        $implementation = $implementations[$implementationIndex]
        Write-Host "Running for case $( $caseIndex ) with implementation $( $implementation )"
        if ($implementationIndex -eq 0)
        {
            $executable = "$( $implementation ).exe"
            $suma = 0
            for ($runNumber = 0; $runNumber -lt $nrRuns; $runNumber++){
                Write-Host "Run" ($runNumber + 1)
                $a = (cmd /c .\$executable 2`>`&1)
                Write-Host $a[$a.length - 1]
                $suma += $a[$a.length - 1]
                Write-Host ""
            }
            Copy-Item "Numar3.txt" "Numar3_Correct.txt" -Force
            $media = $suma / $nrRuns
            Write-Host "Timp de executie mediu:" $media
            Add-Content outC.csv "N1_Size=$( $N1Size );N2_Size=$( $N2Size ),$( $implementation ),secvential,$( $media )"
        }
        else
        {
            $executable = "./$( $implementation ).exe"
            for ($processesIndex = 0; $processesIndex -lt 4; $processesIndex++){
                $processesCount = $processesCounts[$processesIndex]
                Write-Host "Running for case $( $caseIndex ) with implementation $( $implementation ) and with $( $processesCount ) processes"
                $suma = 0
                for ($runNumber = 0; $runNumber -lt $nrRuns; $runNumber++){
                    Write-Host "Rulare" ($runNumber + 1)
                    $a = (cmd /c mpiexec -n $processesCount $executable 2`>`&1)
                    Write-Host $a[$a.length - 1]
                    $suma += $a[$a.length - 1]
                    Write-Host ""
                    if((Get-FileHash "Numar3.txt").Hash -ne (Get-FileHash "Numar3_Correct.txt").Hash){
                        Write-Host "Testing failed"
                        Exit
                    }
                }

                $media = $suma / $nrRuns
                Write-Host "Timp de executie mediu:" $media
                Add-Content outC.csv "N1_Size=$( $N1Size );N2Size=$( $N2Size ),$( $implementation ),$( $processesCount ),$( $media )"
            }
        }
    }
}