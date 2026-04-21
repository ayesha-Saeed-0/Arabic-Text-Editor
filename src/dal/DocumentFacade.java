
package dal;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import dto.DocumentDTO;

public class DocumentFacade implements IDALFacade {

    private final PageDAO pageDAO;
    private final TransliterateDAO transliterateDAO;
    private final DocumentDAO documentDAO;
    
    private final LemmaDAO lemma;


    public DocumentFacade(DatabaseConnection dbManager) throws SQLException {
        // Pass the Connection object from dbManager to the DAOs
        this.pageDAO = new PageDAO();
        this.transliterateDAO = new TransliterateDAO(dbManager); 
        this.documentDAO = new DocumentDAO();
		

		this.lemma = new LemmaDAO();

    }

    @Override
    public void pagination(Connection conn, long documentId, String content) throws SQLException {
        pageDAO.pagination(conn, documentId, content);
    }

   
    @Override
    public void insertFilesBatch(List<DocumentDTO> documents) {
        documentDAO.insertFilesBatch(documents);
    }

    @Override
    public void insertFile(String title, String content, String hash, Timestamp creationTime, int wordCount) {
        documentDAO.insertFile(title, content, hash, creationTime, wordCount);
    }

    @Override
    public List<DocumentDTO> fetchDocuments() {
        return documentDAO.fetchDocuments();
    }

    @Override
    public boolean deleteDocument(String title) {
        return documentDAO.deleteDocument(title);
    }

    @Override
    public boolean isContentDuplicate(String contentHash) {
        return documentDAO.isContentDuplicate(contentHash);
    }

    @Override
    public void updateDocument(DocumentDTO document) {
        documentDAO.updateDocument(document);
    }

    @Override
    public List<String> getDocumentContentByTitle(String title) {
        return documentDAO.getDocumentContentByTitle(title);
    }

    @Override
    public ResultSet searchWord(String searchWord) throws SQLException {
        return documentDAO.searchWord(searchWord);
    }


    // Implement updateTransliteratedContent method to update content using the TransliterateDAO
    public void updateTransliteratedContent(String transliteratedContent, String documentId) {
        transliterateDAO.updateTransliteratedContent(transliteratedContent, documentId);
    }

	@Override
	public String getContent(String documentId, String title) {
		// TODO Auto-generated method stub
		return transliterateDAO.getContent(documentId,title);
	}

	@Override
	public Map<String, Integer> fetchAllContent() {
		return documentDAO.fetchAllContent();
	}

	
	@Override
	public String fetchLemma(String word) {
		// TODO Auto-generated method stub
		return lemma.fetchLemma(word);
	}

	
	@Override
	public int getDocumentIdByName(String documentName) {
		// TODO Auto-generated method stub
		return documentDAO.getDocumentIdByName(documentName);
	}

	

}
