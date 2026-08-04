import urllib.request
import json
import ssl

ctx = ssl.create_default_context()
ctx.check_hostname = False
ctx.verify_mode = ssl.CERT_NONE

req = urllib.request.Request("https://api.deezer.com/search/album?q=LALISA%20LISA")
req.add_header('User-Agent', 'Mozilla/5.0')
with urllib.request.urlopen(req, context=ctx) as response:
    data = json.loads(response.read().decode())
    print(json.dumps(data['data'][:2], indent=2))
