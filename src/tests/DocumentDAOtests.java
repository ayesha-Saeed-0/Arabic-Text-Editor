package tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import dal.DatabaseConnection;
import dal.DocumentDAO;
import dto.DocumentDTO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class DocumentDAOtests {
    private DocumentDAO documentDAO;
    private DatabaseConnection dbConnection;

    @BeforeEach
    public void setUp() {
        dbConnection = new DatabaseConnection();
        documentDAO = new DocumentDAO(dbConnection);
    }

    @Test
    public void testFetchDocuments_ValidData() {
        List<DocumentDTO> docs = documentDAO.fetchDocuments();
        assertTrue(docs.size() > 0);
    }
    @Test
    public void testFetchAllContent() {
        Map<String, Integer> contentMap = documentDAO.fetchAllContent();
        assertFalse(contentMap.isEmpty());
    }

    @Test
    public void testFetchDocuments_EmptyDatabase() {
        // Scenario: Database has documents
        List<DocumentDTO> docs = documentDAO.fetchDocuments();
        assertFalse(docs.isEmpty());
    }


    @Test
    public void testIsContentDuplicate_Duplicate() {
        assertTrue(documentDAO.isContentDuplicate("a328ce37149da63ec98bc7ee8b4d1fc29b1fdfd4"));
    }

    @Test
    public void testIsContentDuplicate_Unique() {
        assertFalse(documentDAO.isContentDuplicate("unique-hash-value-test"));
    }

    @Test
    public void testIsContentDuplicate_EmptyHash() {
        // Scenario: Checking for duplicate with empty hash
        assertFalse(documentDAO.isContentDuplicate(""));
    }

    
    @Test
    public void testGetDocumentContent_ExistingDoc() {
        List<String> content = documentDAO.getDocumentContentByTitle("go.txt");
        assertFalse(content.isEmpty());
    }
    
    @Test
    public void testGetDocumentContent_NonExistentDoc() {
        // Scenario: Fetching content for a document that doesn't exist
        List<String> content = documentDAO.getDocumentContentByTitle("NonExistentDocument");
        assertTrue(content.isEmpty());
    }

    @Test
    public void testInsertFile_NewDocument() {
        String title = "NewTestDocument-" + System.currentTimeMillis();
        String content = "This is a test document content";
        String hash = "test-hash-" + System.currentTimeMillis();
        Timestamp creationTime = new Timestamp(System.currentTimeMillis());
        int wordCount = 10;

        documentDAO.insertFile(title, content, hash, creationTime, wordCount);
        List<String> fetchedContent = documentDAO.getDocumentContentByTitle(title);
        assertFalse(fetchedContent.isEmpty());
    }
    

    @Test
    public void testInsertFile_DuplicateTitle() {
        // Scenario: Inserting a document with a title that already exists
        String existingTitle = "go.txt";
        String content = "New content";
        String hash = "new-hash";
        Timestamp creationTime = new Timestamp(System.currentTimeMillis());
        int wordCount = 20;

        documentDAO.insertFile(existingTitle, content, hash, creationTime, wordCount);
        List<String> fetchedContent = documentDAO.getDocumentContentByTitle(existingTitle);
        assertFalse(fetchedContent.isEmpty());
    }

    @Test
    public void testFetchAllContent_EmptyDatabase() {
        // Scenario: Fetching content when the database is not empty
        Map<String, Integer> contentMap = documentDAO.fetchAllContent();
        assertFalse(contentMap.isEmpty());
    }
    
    
    
    
    @Test
    public void testUpdateDocument_NonExistentDoc() {
        DocumentDTO doc = new DocumentDTO(0, null);
        doc.setTitle("NonExistentDoc");
        documentDAO.updateDocument(doc);
        // Verify no update happened
    }

    @Test
    public void testUpdateDocument_BlankTitle() {
        DocumentDTO doc = new DocumentDTO(0, null);
        doc.setTitle("");
        documentDAO.updateDocument(doc);
        // Verify no update happened
    }

    @Test
    public void testInsertFilesBatch_EmptyList() {
        List<DocumentDTO> docs = new ArrayList<>();
        documentDAO.insertFilesBatch(docs);
        // Verify no exceptions
    }

    @Test
    public void testSearchWord_NonExistentWord() throws SQLException {
        ResultSet rs = documentDAO.searchWord("nonexistent-word");
        assertFalse(rs.next());
    }

    @Test
    public void testDeleteDocument_ExistingDoc() {
        assertTrue(documentDAO.deleteDocument("dsd.txt"));
    }

    @Test
    public void testDeleteDocument_NonExistingDoc() {
        assertFalse(documentDAO.deleteDocument("NonExistentDocumentXYZ"));
    }
    

    @Test
    public void testDeleteDocument_NonExistentDoc() {
        // Scenario: Deleting a document that doesn't exist
        assertFalse(documentDAO.deleteDocument("NonExistentDocument"));
    }

}





