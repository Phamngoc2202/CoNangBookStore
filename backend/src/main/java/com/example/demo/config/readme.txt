$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()

try {
    $bytes = New-Object byte[] 32
    $rng.GetBytes($bytes)
    $secret = [Convert]::ToBase64String($bytes)
    Write-Output $secret
}
finally {
    $rng.Dispose()
}
Code để sinh ra key trong powershell