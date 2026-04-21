package bll;

import dto.SearchResult;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

public interface DocumentService {
	
	List<String> getFormattedDocuments();
    void importFile(String filePath) throws IOException, NoSuchAlgorithmException, DuplicateFileException;
	void importFiles(List<String> filePaths);
	void createAndSaveDocument(String fileName, String content) throws IOException, NoSuchAlgorithmException;
	boolean deleteDocument(String fileName);
	void updateDocument(String fileName, String newContent) throws NoSuchAlgorithmException, IOException;
	List<String> getDocumentContentByTitle(String title);
	public String getContent(String documentId, String title);
	List<SearchResult> search(String word);
	String transliterateContent(String input, String title);
	public Map<String, Integer> getAllContent();
	 public String tagText(String text);
}
