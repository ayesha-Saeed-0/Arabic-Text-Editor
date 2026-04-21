package tests;

import org.junit.jupiter.api.*;
import dal.TransliterateDAO;
import dal.DatabaseConnection;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class TransliterateDAOTest {

    private static final String URL = "jdbc:mysql://localhost:3306/dummyDatabase";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static Connection connection;
    private TransliterateDAO transliterateDAO;

    @BeforeAll
    public static void setUpDatabase() throws Exception {
        connection = DriverManager.getConnection(URL, USER, PASSWORD);

        try (Statement statement = connection.createStatement()) {
            // Create tables for testing
            statement.execute("CREATE TABLE IF NOT EXISTS document (ID INT PRIMARY KEY, title VARCHAR(255))");
            statement.execute("CREATE TABLE IF NOT EXISTS pages (page_id INT PRIMARY KEY, document_id INT, page_content TEXT, FOREIGN KEY (document_id) REFERENCES document(document_id))");

            // Insert some test data
            statement.execute("INSERT IGNORE INTO document (ID, title) VALUES (59, 'Ablution_Hadess')");
            statement.execute("INSERT IGNORE INTO pages (page_id, document_id, page_content) VALUES (1, 59, 'Sample page content')");
        }
    }

    @AfterAll
    public static void tearDownDatabase() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @BeforeEach
    public void setUp() {
        DatabaseConnection dbConnection = new DatabaseConnection();
           
        transliterateDAO = new TransliterateDAO(dbConnection);
    }

    @Test
    public void testGetContent_ValidDocumentIdAndTitle() {
        String validTitle = "Ablution_Hadess";
        String documentId = "59";
        String expectedContent = null;

        String content = transliterateDAO.getContent(documentId, validTitle);

        assertEquals(expectedContent, content);
    }

    @Test
    public void testGetContent_InvalidDocumentId() {
        String invalidDocumentId = "999";
        String validTitle = "Ablution_Hadess";

        String content = transliterateDAO.getContent(invalidDocumentId, validTitle);

        assertNull(content);
    }

    @Test
    public void testGetContent_EmptyTitle() {
        String validDocumentId = "59";
        String emptyTitle = "";

        String content = transliterateDAO.getContent(validDocumentId, emptyTitle);

        assertNull(content);
    }

    @Test
    public void testGetContent_NoMatchingRecords() {
        String validDocumentId = "59";
        String nonExistentTitle = "Non-existent";

        String content = transliterateDAO.getContent(validDocumentId, nonExistentTitle);

        assertNull(content);
    }

    @Test
    public void testGetContent_NullTitle() {
        String validDocumentId = "59";
        String nullTitle = null;

        String content = transliterateDAO.getContent(validDocumentId, nullTitle);

        assertNull(content);
    }
}
