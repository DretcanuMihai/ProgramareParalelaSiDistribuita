if (!(Test-Path outC.csv))
{
    New-Item outC.csv -ItemType File
    #Scrie date in csv
    Set-Content outC.csv 'Tip Matrice,Tip alocare,Nr threads,Timp executie'
}

$nrRuns = 10
$threads = 0, 1, 2, 4, 8, 16
$NValues = 10, 1000, 10, 10000
$MValues = 10, 1000, 10000, 10
$NKValues = 3, 5, 5, 5
$MKValues = 3, 5, 5, 5

for($fileIndex = 0; $fileIndex -lt 4; $fileIndex++){
    $filename = "date$( $fileIndex ).txt"
    Write-Host "Running for $( $filename )"
    Copy-Item $filename "date.txt" -Force

    $allocation = "static"
    for ($threadsIndex = 0; $threadsIndex -lt 6; $threadsIndex++){

        $nrThreaduri = $threads[$threadsIndex]
        $suma = 0
        $executable = "CPP_Static$( $fileIndex ).exe"
        Write-Host "Running for $( $filename ) with $( $nrThreaduri ) threads with $( $allocation ) allocation"
        for ($runNumber = 0; $runNumber -lt $nrRuns; $runNumber++){
            Write-Host "Rulare" ($runNumber + 1)
            $a = (cmd /c .\$executable $nrThreaduri 2`>`&1)
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
        Add-Content outC.csv "N=$( $NValue );M=$( $MValue );n=$( $NKValue );m=$( $MKValue ),$( $allocation ),$( $nrThreaduri ),$( $media )"

    }

    $allocation = "dinamic"
    for ($threadsIndex = 0; $threadsIndex -lt 6; $threadsIndex++){

        $nrThreaduri = $threads[$threadsIndex]
        $suma = 0
        $executable = "CPP_Dynamic.exe"
        Write-Host "Running for $( $filename ) with $( $nrThreaduri ) threads with $( $allocation ) allocation"
        for ($runNumber = 0; $runNumber -lt $nrRuns; $runNumber++){
            Write-Host "Rulare" ($runNumber + 1)
            $a = (cmd /c .\$executable $nrThreaduri 2`>`&1)
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
        Add-Content outC.csv "N=$( $NValue );M=$( $MValue );n=$( $NKValue );m=$( $MKValue ),$( $allocation ),$( $nrThreaduri ),$( $media )"
    }
}