import requests

artists = ["Conan Gray", "Laufey", "The Strokes", "Mac DeMarco"]
audio_db_key = "123"

for artist in artists:
    print(f"Testing {artist}:")
    
    # 1. MusicBrainz
    mb_url = f"https://musicbrainz.org/ws/2/artist/?query={requests.utils.quote(artist)}&fmt=json"
    headers = {'User-Agent': 'ArcMusicTest/1.0 ( test@example.com )'}
    try:
        r = requests.get(mb_url, headers=headers)
        r.raise_for_status()
        data = r.json()
        mbid = data.get("artists", [])[0].get("id") if data.get("artists") else None
        print(f"  MBID: {mbid}")
        
        if mbid:
            # 2. TheAudioDB
            tadb_url = f"https://theaudiodb.com/api/v1/json/{audio_db_key}/artist-mb.php?i={mbid}"
            r2 = requests.get(tadb_url)
            r2.raise_for_status()
            data2 = r2.json()
            artists_data = data2.get("artists")
            if artists_data and len(artists_data) > 0:
                thumb = artists_data[0].get("strArtistThumb")
                print(f"  Result: PASS - {thumb}")
            else:
                print(f"  Result: FAIL - No artist found on TheAudioDB for MBID {mbid}")
        else:
            print(f"  Result: FAIL - No MBID found for {artist}")
    except Exception as e:
        print(f"  Result: FAIL - Exception: {e}")
