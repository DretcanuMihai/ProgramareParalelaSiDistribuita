$mainClass = "Main"

$nrRuns = 10
$threads = 0, 1, 2, 4, 8, 16
$NValues = 10, 1000, 10, 10000
$MValues = 10, 1000, 10000, 10
$NKValues = 3, 5, 5, 5
$MKValues = 3, 5, 5, 5

if (!(Test-Path outJ.csv))
{
    New-Item outJ.csv -ItemType File
    Set-Content outJ.csv 'Tip Matrice,Nr threads,Timp executie'
}

for($fileIndex = 0; $fileIndex -lt 4; $fileIndex++){


    $filename = "date$( $fileIndex ).txt"
    Write-Host "Running for $( $filename )"
    Copy-Item $filename "date.txt" -Force
    for ($threadsIndex = 0; $threadsIndex -lt 6; $threadsIndex++){

        # Executare class Java
        $nrThreaduri = $threads[$threadsIndex]

        Write-Host "Running for $( $filename ) with $( $nrThreaduri ) threads"

        $suma = 0

        for ($runNumber = 0; $runNumber -lt $nrRuns; $runNumber++){
            Write-Host "Rulare" ($runNumber + 1)
            $a = java $mainClass $nrThreaduri
            Write-Host $a[$a.length - 1]
            $suma += $a[$a.length - 1]
            Write-Host ""
        }
        $media = $suma / $nrRuns
        Write-Host "Timp de executie mediu:" $media

        if ($nrThreaduri -eq 0)
        {
            $nrThreaduri = "secvential"
        }
        $NValue = $NValues[$fileIndex]
        $MValue = $MValues[$fileIndex]
        $NKValue = $NKValues[$fileIndex]
        $MKValue = $MKValues[$fileIndex]

        # Append
        Add-Content outJ.csv "N=$( $NValue );M=$( $MValue );n=$( $NKValue );m=$( $MKValue ),$( $nrThreaduri ),$( $media )"
    }
}