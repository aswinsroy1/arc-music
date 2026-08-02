$files = Get-ChildItem -Path "app\src" -Recurse -Include *.kt,*.xml

foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw -Encoding UTF8
    $newContent = $content -replace "com\.example", "com.aeswox.arcmusic"
    $newContent = $newContent -replace "Theme\.MyApplication", "Theme.ArcMusic"
    
    if ($file.Name -eq "strings.xml") {
        $newContent = $newContent -replace ">Music<", ">Arc Music<"
    }
    
    if ($newContent -cne $content) {
        Set-Content -Path $file.FullName -Value $newContent -NoNewline -Encoding UTF8
        Write-Host "Updated $($file.FullName)"
    }
}

function Move-PackageDir {
    param($basePath)
    $source = Join-Path $basePath "com\example"
    $dest = Join-Path $basePath "com\aeswox\arcmusic"
    
    if (Test-Path $source) {
        New-Item -ItemType Directory -Force -Path $dest | Out-Null
        Get-ChildItem -Path $source | Move-Item -Destination $dest -Force
        Remove-Item -Path $source -Recurse -Force
        Write-Host "Moved $source to $dest"
    }
}

Move-PackageDir "app\src\main\java"
Move-PackageDir "app\src\test\java"
Move-PackageDir "app\src\androidTest\java"

Write-Host "Done"
