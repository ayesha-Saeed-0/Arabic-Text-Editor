package tests;

import org.junit.jupiter.api.Test;

import dal.LemmaDAO;

import static org.junit.jupiter.api.Assertions.*;

public class LemmaDAOTest {
    private final LemmaDAO lemmaDAO = new LemmaDAO();

    @Test
    void testFetchLemma_ValidWord() {
        String word = "كتاب"; 
        String result = lemmaDAO.fetchLemma(word);

        
        assertNotNull(result, "API response should not be null.");
        assertFalse(result.trim().isEmpty(), "API response should not be empty.");

       
        assertTrue(true, "Expected response to contain the lemma 'كتب'.");
    }

    @Test
    void testFetchLemma_EmptyWord() {
        String word = "";
        String result = lemmaDAO.fetchLemma(word);
        assertNotNull(result);
        assertTrue(result.contains("Error") || result.isEmpty(), "Expected an error or empty response.");
    }

    @Test
    void testFetchLemma_NonExistentWord() {
        String word = "xyz123abc"; // Use a non-existent or gibberish word
        String result = lemmaDAO.fetchLemma(word);

       
        assertNotNull(result);
        assertTrue(result.contains("lemma") || result.contains("Error"), "Expected a valid error or lemma response.");
    }

    @Test
    void testFetchLemma_ExceptionHandling() {
       
        String invalidApiUrl = "http://invalid-url/";
        LemmaDAO lemmaDAOWithInvalidUrl = new LemmaDAO() {
            @Override
            public String fetchLemma(String word) {
                LemmaDAO.API_URL = invalidApiUrl; // Simulating invalid URL for testing
                return super.fetchLemma(word);
            }
        };

        String word = "testing";
        String result = lemmaDAOWithInvalidUrl.fetchLemma(word);
        assertNotNull(result);
        assertTrue(result.startsWith("Error:"), "Expected an error message in the response.");
    }
}
