import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.URLEncoder;

public class TestDeezer {
    public static void main(String[] args) throws Exception {
        String query = URLEncoder.encode("LALISA LISA", "UTF-8");
        URL url = new URL("https://api.deezer.com/search/album?q=" + query);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println("Album search: " + line.substring(0, Math.min(line.length(), 200)));
        }
        
        URL url2 = new URL("https://api.deezer.com/search/track?q=" + query);
        HttpURLConnection conn2 = (HttpURLConnection) url2.openConnection();
        BufferedReader reader2 = new BufferedReader(new InputStreamReader(conn2.getInputStream()));
        String line2;
        while ((line2 = reader2.readLine()) != null) {
            System.out.println("Track search: " + line2.substring(0, Math.min(line2.length(), 200)));
        }
    }
}
