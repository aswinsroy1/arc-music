# pull_and_query_db.ps1
# Pulls the Room DB from device and queries it for the acceptance check

$ADB  = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
$PKG  = "com.aeswox.arcmusic"
$DEST = "$env:TEMP\arc_music_db.db"

Write-Host "Pulling database from device..." -ForegroundColor Yellow

# Use adb exec-out piped directly to a FileStream to preserve binary data
$proc = New-Object System.Diagnostics.Process
$proc.StartInfo = New-Object System.Diagnostics.ProcessStartInfo
$proc.StartInfo.FileName = $ADB
$proc.StartInfo.Arguments = "exec-out `"run-as $PKG cat /data/data/$PKG/databases/music_database`""
$proc.StartInfo.RedirectStandardOutput = $true
$proc.StartInfo.UseShellExecute = $false
$proc.StartInfo.CreateNoWindow = $true
$proc.Start() | Out-Null

$stream = $proc.StandardOutput.BaseStream
$fs = [System.IO.File]::OpenWrite($DEST)
$buf = New-Object byte[] 65536
$total = 0
while (($read = $stream.Read($buf, 0, $buf.Length)) -gt 0) {
    $fs.Write($buf, 0, $read)
    $total += $read
}
$fs.Close()
$proc.WaitForExit()

Write-Host "Pulled $total bytes -> $DEST" -ForegroundColor Green

# Now query using the System.Data.SQLite-free approach:
# Read raw SQLite pages and parse play_history + tracks tables
# We use the sqlite3.exe if present, otherwise report raw byte check
$sqlite3 = Get-Command sqlite3 -ErrorAction SilentlyContinue
if (-not $sqlite3) {
    # Try common locations
    $candidates = @(
        "C:\Program Files\SQLite\sqlite3.exe",
        "C:\sqlite\sqlite3.exe",
        "$env:LOCALAPPDATA\sqlite3.exe",
        "sqlite3.exe"
    )
    foreach ($c in $candidates) {
        if (Test-Path $c) { $sqlite3 = $c; break }
    }
}

if ($sqlite3) {
    Write-Host "`n=== play_history (newest first) ===" -ForegroundColor Cyan
    & $sqlite3 $DEST "SELECT id, trackId, timestamp, completed, skipReason FROM play_history ORDER BY timestamp DESC;"

    Write-Host "`n=== tracks: playCount + lastPlayedAt (recently played) ===" -ForegroundColor Cyan
    & $sqlite3 $DEST "SELECT id, title, playCount, lastPlayedAt FROM tracks WHERE lastPlayedAt IS NOT NULL ORDER BY lastPlayedAt DESC LIMIT 10;"
} else {
    Write-Host "`nsqlite3.exe not found. Downloading portable sqlite3..." -ForegroundColor Yellow
    $zipUrl  = "https://www.sqlite.org/2024/sqlite-tools-win-x64-3450100.zip"
    $zipDest = "$env:TEMP\sqlite_tools.zip"
    $sqlDir  = "$env:TEMP\sqlite_tools"
    Invoke-WebRequest -Uri $zipUrl -OutFile $zipDest -UseBasicParsing
    Expand-Archive -Path $zipDest -DestinationPath $sqlDir -Force
    $sqlite3 = Get-ChildItem $sqlDir -Filter "sqlite3.exe" -Recurse | Select-Object -First 1 -ExpandProperty FullName

    Write-Host "`n=== play_history (newest first) ===" -ForegroundColor Cyan
    & $sqlite3 $DEST "SELECT id, trackId, timestamp, completed, skipReason FROM play_history ORDER BY timestamp DESC;"

    Write-Host "`n=== tracks: playCount + lastPlayedAt (recently played) ===" -ForegroundColor Cyan
    & $sqlite3 $DEST "SELECT id, title, playCount, lastPlayedAt FROM tracks WHERE lastPlayedAt IS NOT NULL ORDER BY lastPlayedAt DESC LIMIT 10;"
}

Write-Host "`nDone." -ForegroundColor Green
