
import java.net.HttpURLConnection
import java.net.URL
import java.io.InputStreamReader
import java.io.BufferedReader

fun getTopMBArtists(query: String) {
    val url = URL("https://musicbrainz.org/ws/2/artist/?query=$query&fmt=json")
    val conn = url.openConnection() as HttpURLConnection
    conn.setRequestProperty("User-Agent", "ArcMusic/1.0 ( test@test.com )")
    val reader = BufferedReader(InputStreamReader(conn.inputStream))
    val response = reader.readText()
    println(response.take(1000))
}
getTopMBArtists("Lisa")
