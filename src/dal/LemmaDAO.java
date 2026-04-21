package dal;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LemmaDAO implements ILemma{
    protected static String API_URL = "http://oujda-nlp-team.net:8080/api/Apilmm/";

    // Fetch lemma for a given word from the API
    public String fetchLemma(String word) {
        StringBuilder response = new StringBuilder();
        try {
            String encodedWord = URLEncoder.encode(word, StandardCharsets.UTF_8.toString());
            String apiUrl = API_URL + encodedWord;
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }
        } catch (Exception e) {
            response.append("Error: ").append(e.getMessage());
        }
        return response.toString();
    }
   
    

}
