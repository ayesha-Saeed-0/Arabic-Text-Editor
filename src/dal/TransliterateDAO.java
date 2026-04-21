package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TransliterateDAO implements ITransliterate {

    public TransliterateDAO( DatabaseConnection connection) {
    }

    @Override
    public String getContent(String documentId, String title) {
        String content = null;
        String query = "SELECT page_content FROM pages WHERE document_id = (SELECT ID FROM document WHERE title = ?) AND page_id = ?";

        try (Connection connection = DatabaseConnection.getConnection(); // Use the connection here
             PreparedStatement stmt = connection.prepareStatement(query)) {

            // Set the parameters for the query
            stmt.setString(1, title); // Set the title for the first '?'
            stmt.setString(2, documentId); // Set the pageId for the second '?'

            // Execute the query and process the result set
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    content = rs.getString("page_content");
                }
            }
        } catch (SQLException e) {
            // Log error for better debugging
            System.err.println("Error retrieving content for document ID " + documentId + " and title " + title);
            e.printStackTrace();
        }

        return content;
    }

   
    @Override
    public void updateTransliteratedContent(String transliteratedContent, String title) {
        String query = "UPDATE pages " +
                       "SET transliterateContent = ? " +
                       "WHERE page_id = (SELECT p.page_id " +
                       "                FROM pages p " +
                       "                INNER JOIN document d ON p.document_id = d.ID " +
                       "                WHERE d.title = ? " +
                       "                LIMIT 1)";

        try (Connection connection = DatabaseConnection.getConnection(); // Obtain the connection
             PreparedStatement stmt = connection.prepareStatement(query)) {

            // Set the parameters for the prepared statement
            stmt.setString(1, transliteratedContent);
            stmt.setString(2, title);

            // Execute the update
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Transliterated content successfully updated for document: " + title);
            } else {
                System.err.println("No matching document or page found for the given title.");
            }

        } catch (SQLException e) {
            // Use a logging framework for better practices
            System.err.println("Error updating transliterated content for title: " + title);
            e.printStackTrace();
        }
    }


}