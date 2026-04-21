package bll;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bll.DuplicateFileException;
import dal.DatabaseConnection;
import dal.DocumentDAO;
import dal.DocumentFacade;
import dal.TransliterateDAO;
import dto.DocumentDTO;
import dto.SearchResult;


public class DocumentBO {
    private final DocumentFacade documentFacade;
    private TransliterateDAO transliterationDAO;

    public DocumentBO(DocumentFacade documentFacade,  DatabaseConnection dbManager) {
        this.documentFacade = documentFacade;
        this.transliterationDAO = new TransliterateDAO(dbManager);
		
        
    }

    public List<String> getFormattedDocuments() {
        List<String> result = new ArrayList<>();
        List<DocumentDTO> documents = documentFacade.fetchDocuments();

        for (DocumentDTO document : documents) {
            String formattedDocument = "Title: " + document.getTitle() + ", Creation Time: "
                    + document.getCreationTime() + ", Updation Time: " + document.getUpdationTime();
            result.add(formattedDocument);
        }
        return result;
    }

    public void importFile(String filePath) throws IOException, NoSuchAlgorithmException, DuplicateFileException {
        System.out.println("Starting import for file: " + filePath);
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("File not found: " + filePath);
        }

        StringBuilder contentBuilder = new StringBuilder();
        int wordCount = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line).append("\n");
                wordCount += countWords(line);
            }
        }

        String content = contentBuilder.toString();
        String hash = calculateHash(content);
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());

        System.out.println("Calculated hash: " + hash);
        System.out.println("Word count: " + wordCount);

        if (documentFacade.isContentDuplicate(hash)) {
            throw new DuplicateFileException("Duplicate file detected: " + file.getName());
        }

        documentFacade.insertFile(file.getName(), content, hash, currentTime, wordCount);
        System.out.println("File import completed for: " + filePath);
    }

    public void importFiles(List<String> filePaths) {
        System.out.println("Starting bulk import of " + filePaths.size() + " files.");
        List<DocumentDTO> documents = new ArrayList<>();
        List<String> duplicateFiles = new ArrayList<>();

        for (String filePath : filePaths) {
            System.out.println("Processing file: " + filePath);
            File file = new File(filePath);
            if (!file.exists()) {
                System.err.println("File not found: " + filePath);
                continue;
            }

            StringBuilder contentBuilder = new StringBuilder();
            int wordCount = 0;

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    contentBuilder.append(line).append("\n");
                    wordCount += countWords(line);
                }
            } catch (IOException e) {
                System.err.println("Error reading file '" + filePath + "': " + e.getMessage());
                continue;
            }

            String content = contentBuilder.toString();
            String hash;
            try {
                hash = calculateHash(content);
            } catch (NoSuchAlgorithmException e) {
                System.err.println("Error calculating hash for file '" + filePath + "': " + e.getMessage());
                continue;
            }

            Timestamp currentTime = new Timestamp(System.currentTimeMillis());

            if (documentFacade.isContentDuplicate(hash)) {
                duplicateFiles.add(file.getName());
                continue;
            }

            DocumentDTO doc = new DocumentDTO(file.getName(), content, hash, currentTime, wordCount);
            documents.add(doc);
        }

        if (!documents.isEmpty()) {
            documentFacade.insertFilesBatch(documents);
            System.out.println("Bulk import completed. Imported " + documents.size() + " files.");
        }

        if (!duplicateFiles.isEmpty()) {
            System.err.println("Duplicate files detected: " + String.join(", ", duplicateFiles));
        }
    }

    private int countWords(String line) {
        if (line == null || line.isEmpty()) {
            return 0;
        }
        return line.trim().split("\\s+").length;
    }

    private String calculateHash(String content) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] bytes = md.digest(content.getBytes());
        StringBuilder hashStringBuilder = new StringBuilder();
        for (byte b : bytes) {
            hashStringBuilder.append(String.format("%02x", b));
        }
        return hashStringBuilder.toString();
    }

    public void createAndSaveDocument(String fileName, String content) throws IOException, NoSuchAlgorithmException {
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        String contentHash = calculateHash(content);

        if (documentFacade.isContentDuplicate(contentHash)) {
            throw new IOException("Duplicate content found. Cannot create a document with the same content.");
        }

        int totalWordCount = content.trim().split("\\s+").length;

        documentFacade.insertFile(fileName, content, contentHash, currentTime, totalWordCount);
    }

    public boolean deleteDocument(String fileName) {
        return documentFacade.deleteDocument(fileName);
    }

    public void updateDocument(String fileName, String newContent) throws NoSuchAlgorithmException, IOException {
        List<String> existingContent = documentFacade.getDocumentContentByTitle(fileName);

        if (existingContent == null) {
            throw new IOException("File not found: " + fileName);
        }

        String contentHash = calculateHash(newContent);
        DocumentDTO document = new DocumentDTO(fileName, newContent, newContent.trim().split("\\s+").length,
                contentHash);

        documentFacade.updateDocument(document);
    }

    public List<String> getDocumentContentByTitle(String title) {
        return documentFacade.getDocumentContentByTitle(title);
    }

   

    public List<SearchResult> search(String word) {
        List<SearchResult> results = new ArrayList<>();
        DocumentDAO documentDAO = new DocumentDAO(); // Create an instance of DocumentDAO

        try {
            ResultSet rs = documentDAO.searchWord(word); // Use the instance to call searchWord
            while (rs.next()) {
                String title = rs.getString("title");
                int pageNumber = rs.getInt("page_number");
                String pageContent = rs.getString("page_content");

                // Debugging output to see full content
                System.out.println("Title: " + title + ", Page Number: " + pageNumber + ", Content: " + pageContent);

                // Find words before and after the searched word
                int index = pageContent.indexOf(word);
                String wordBefore = "";
                String wordAfter = "";

                if (index > 0) {
                    // Get substring before the searched word
                    String beforeContent = pageContent.substring(0, index).trim();
                    String[] beforeWords = beforeContent.split("\\s+");
                    if (beforeWords.length > 0) {
                        wordBefore = beforeWords[beforeWords.length - 1];
                    }
                }

                if (index + word.length() < pageContent.length()) {
                    // Get substring after the searched word
                    String afterContent = pageContent.substring(index + word.length()).trim();
                    String[] afterWords = afterContent.split("\\s+");
                    if (afterWords.length > 0) {
                        wordAfter = afterWords[0];
                    }
                }

                results.add(new SearchResult(title, pageNumber, wordBefore, wordAfter));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

   


    private static final Map<Character, String> transliterationTable = new HashMap<>();

    static {
        transliterationTable.put('ا', "a");
        transliterationTable.put('ب', "b");
        transliterationTable.put('ت', "t");
        transliterationTable.put('ث', "th");
        transliterationTable.put('ج', "j");
        transliterationTable.put('ح', "H");
        transliterationTable.put('خ', "kh");
        transliterationTable.put('د', "d");
        transliterationTable.put('ذ', "dh");
        transliterationTable.put('ر', "r");
        transliterationTable.put('ز', "z");
        transliterationTable.put('س', "s");
        transliterationTable.put('ش', "sh");
        transliterationTable.put('ص', "S");
        transliterationTable.put('ض', "D");
        transliterationTable.put('ط', "T");
        transliterationTable.put('ظ', "DH");
        transliterationTable.put('ع', "ʿ");
        transliterationTable.put('غ', "gh");
        transliterationTable.put('ف', "f");
        transliterationTable.put('ق', "q");
        transliterationTable.put('ك', "k");
        transliterationTable.put('ل', "l");
        transliterationTable.put('م', "m");
        transliterationTable.put('ن', "n");
        transliterationTable.put('ه', "h");
        transliterationTable.put('و', "w");
        transliterationTable.put('ي', "y");
        transliterationTable.put('ء', "'");
        transliterationTable.put('َ', "a");
        transliterationTable.put('ُ', "u");
        transliterationTable.put('ِ', "i");
    }


    public String transliterateContent(String input,String title) {

    	System.out.println(title);

        StringBuilder output = new StringBuilder();

        for (char ch : input.toCharArray()) {

            output.append(transliterationTable.getOrDefault(ch, String.valueOf(ch)));

        }



        String transliteratedContent = output.toString();



        transliterationDAO.updateTransliteratedContent(transliteratedContent, title);

        System.out.println(transliteratedContent);



        return transliteratedContent;

    }
    //  the provided content and save it to the database
    public void transliterateAndSaveContent(String documentId, String title) {
        String content = transliterationDAO.getContent(documentId, title);
        if (content != null && !content.isEmpty()) {
            StringBuilder transliteratedContent = new StringBuilder();

            // each character in content
            for (char ch : content.toCharArray()) {
                transliteratedContent.append(transliterationTable.getOrDefault(ch, String.valueOf(ch)));
            }

            // Update content in the database
            transliterationDAO.updateTransliteratedContent(transliteratedContent.toString(), documentId);
        }
    }

	public String getContent(String documentId, String title) {
		// TODO Auto-generated method stub
		return transliterationDAO.getContent(documentId, title);
	}
	
	 public Map<String, Integer> getAllContent() {
	        return documentFacade.fetchAllContent();
	    }

	    public String tagText(String text) {
	        try {
	            String baseUrl = "http://oujda-nlp-team.net:8082/api/pos";
	            System.out.println("Text to be tagged: " + text);

	            // Split the input text into words separated by spaces
	            String[] words = text.split("\\s+");  // Split by one or more spaces
	            StringBuilder finalResponse = new StringBuilder();

	            for (String word : words) {
	                // Trim spaces from word (in case there are leading/trailing spaces)
	                word = word.trim();
	                if (word.isEmpty()) continue;  // Skip empty words

	                // Encode the word for the URL
	                String encodedText = URLEncoder.encode(word, StandardCharsets.UTF_8.toString());
	                String apiUrl = baseUrl + "?textinput=" + encodedText;
	                System.out.println("Request URL: " + apiUrl);  // Log the full URL to verify

	                // Create the HTTP connection
	                URL apiURL = new URL(apiUrl);
	                HttpURLConnection connection = (HttpURLConnection) apiURL.openConnection();
	                connection.setRequestMethod("POST");  // Use GET method (not POST)
	                connection.setRequestProperty("Content-Type", "application/json");  // Set Content-Type header
	                connection.setRequestProperty("Accept", "*/*");  // Accept any response type

	                // Read the response
	                int responseCode = connection.getResponseCode();
	                System.out.println("Response Code: " + responseCode);  // Log the response code

	                StringBuilder response = new StringBuilder();
	                if (responseCode == HttpURLConnection.HTTP_OK) {  // Success
	                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
	                        String line;
	                        while ((line = reader.readLine()) != null) {
	                            response.append(line);
	                        }
	                        System.out.println("Response: " + response.toString());
	                    }
	                } else {  // Error
	                     try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
	                        String line;
	                        while ((line = errorReader.readLine()) != null) {
	                            response.append(line);
	                        }
	                    }
	                    System.out.println("Error Response: " + response.toString());
	                }

	                // Append the response to the final response string
	                finalResponse.append(response.toString()).append("\n");

	                // Disconnect after processing the word
	                connection.disconnect();
	            }


	            System.out.println(finalResponse.toString());
	            return finalResponse.toString();
	        } catch (IOException e) {
	            e.printStackTrace();
	            return "Error processing text: " + e.getMessage();
	        }
	    }



}
