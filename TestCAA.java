import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.URLEncoder;

public class TestCAA {
    public static void main(String[] args) throws Exception {
        String query = URLEncoder.encode("artist:\"Lisa\" AND recording:\"MONEY\"", "UTF-8");
        URL url = new URL("https://musicbrainz.org/ws/2/recording/?query=" + query + "&fmt=json");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "ArcMusic/1.0 ( test@test.com )");
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
    }
}
