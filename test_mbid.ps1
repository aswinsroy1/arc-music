$artists = @("Conan Gray", "Laufey", "The Strokes", "Mac DeMarco")
$audio_db_key = "123"

[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

foreach ($artist in $artists) {
    Write-Host "Testing $artist :"
    
    $encoded = [uri]::EscapeDataString($artist)
    $mb_url = "https://musicbrainz.org/ws/2/artist/?query=$encoded&fmt=json"
    $headers = @{ "User-Agent" = "ArcMusicTest/1.0 ( test@example.com )" }
    
    try {
        $mb_response = Invoke-RestMethod -Uri $mb_url -Headers $headers -ErrorAction Stop
        $mbid = $mb_response.artists[0].id
        Write-Host "  MBID: $mbid"
        
        if ($mbid) {
            $tadb_url = "https://theaudiodb.com/api/v1/json/$audio_db_key/artist-mb.php?i=$mbid"
            $tadb_response = Invoke-RestMethod -Uri $tadb_url -ErrorAction Stop
            $artists_data = $tadb_response.artists
            if ($artists_data -and $artists_data.Count -gt 0) {
                $thumb = $artists_data[0].strArtistThumb
                Write-Host "  Result: PASS - $thumb"
            } else {
                Write-Host "  Result: FAIL - No artist found on TheAudioDB for MBID $mbid"
            }
        }
    } catch {
        Write-Host "  Result: FAIL - Exception: $_"
    }
}
