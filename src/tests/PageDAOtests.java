package tests;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dal.PageDAO;

class PageDAOtests {
    private static Connection connection;
    private PageDAO pageDAO;

    @BeforeAll
    static void setupDatabase() throws SQLException {
       
        String url = "jdbc:mysql://localhost:3306/dummy";
        String username = "root";
        String password = "";
        connection = DriverManager.getConnection(url, username, password);

        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS pages (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "document_id BIGINT, " +
                    "page_number INT, " +
                    "page_content TEXT)");
        }
    }

    @BeforeEach
    void setup() {
        pageDAO = new PageDAO();
    }

    @AfterEach
    void cleanUp() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM pages");
        }
    }

    @AfterAll
    static void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    void testEmptyContent() throws SQLException {
        pageDAO.pagination(connection, 1, "");
        try (Statement stmt = connection.createStatement()) {
            var rs = stmt.executeQuery("SELECT COUNT(id) FROM pages");
            rs.next();
            assertEquals(0, rs.getInt(1), "No pages should be created for empty content.");
        }
    }

    @Test
    void testSinglePageContent() throws SQLException {
        String content = "This is a test line.\n".repeat(30);
        pageDAO.pagination(connection, 1, content);

        try (Statement stmt = connection.createStatement()) {
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM pages");
            rs.next();
            assertEquals(1, rs.getInt(1), "A single page should be created.");

            rs = stmt.executeQuery("SELECT page_content FROM pages WHERE page_number = 1");
            rs.next();
            assertEquals(content.trim(), rs.getString(1), "Page content should match the input.");
        }
    }
//    
    @Test
    void testMultiPageContent() throws SQLException {
        String content = ("This is a test line.\n".repeat(30) + "Extra words ".repeat(100)).repeat(2); // Exceeds limits
        pageDAO.pagination(connection, 1, content);

        try (Statement stmt = connection.createStatement()) {
            var rs = stmt.executeQuery("SELECT COUNT(id) FROM pages");
            rs.next();
            assertEquals(3, rs.getInt(1), "Two pages should be created.");

            rs = stmt.executeQuery("SELECT page_number, page_content FROM pages ORDER BY page_number");
            while (rs.next()) {
                int pageNumber = rs.getInt("page_number");
                String pageContent = rs.getString("page_content");
                assertNotNull(pageContent, "Page content should not be null.");
                assertTrue(pageContent.length() > 0, "Page content should have valid text.");
            }
        }
    }
    
   
}
