package dal;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import dto.DocumentDTO;

public interface IDal {
	List<DocumentDTO> fetchDocuments();

	boolean deleteDocument(String title);

	boolean isContentDuplicate(String contentHash);

	void updateDocument(DocumentDTO document);

	List<String> getDocumentContentByTitle(String title);

	void insertFilesBatch(List<DocumentDTO> documents);

	ResultSet searchWord(String searchWord) throws SQLException;

	public Map<String, Integer> fetchAllContent();
	public int getDocumentIdByName(String documentName);

}
