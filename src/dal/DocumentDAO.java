package dal;

import dto.DocumentDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentDAO implements IDal {
	private DatabaseConnection dbConnection;

	public DocumentDAO() {
	}

	public DocumentDAO(DatabaseConnection dbConnection) {
		this.setDbConnection(dbConnection);
	}

	@Override
	public List<DocumentDTO> fetchDocuments() {
		List<DocumentDTO> documents = new ArrayList<>();
		String query = "SELECT title, creation_time, upadation_time FROM Document";

		try (Connection conn = DatabaseConnection.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(query)) {

			while (rs.next()) {
				DocumentDTO document = new DocumentDTO(rs.getString("title"), rs.getTimestamp("creation_time"),
						rs.getTimestamp("upadation_time"));
				documents.add(document);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return documents;
	}

	@Override
	public boolean deleteDocument(String title) {
		String query = "DELETE FROM Document WHERE title = ?";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setString(1, title);
			int rowsAffected = stmt.executeUpdate();
			return rowsAffected > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean isContentDuplicate(String contentHash) {
		String query = "SELECT COUNT(*) FROM document WHERE Hash = ?";

		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setString(1, contentHash);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				return rs.getInt(1) > 0;
			}
		} catch (SQLException e) {
			System.err.println("Error checking content duplicate: " + e.getMessage());
		}
		return false;
	}

	@Override
	public void updateDocument(DocumentDTO document) {
		 String getIdQuery = "SELECT ID FROM document WHERE Title = ?";
		    String updateQuery = "UPDATE document SET word_count = ?, Hash = ?, upadation_time = CURRENT_TIMESTAMP WHERE Title = ?";
		    
		    
		    try (Connection conn = DatabaseConnection.getConnection();
		         PreparedStatement getIdStmt = conn.prepareStatement(getIdQuery);
		         PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {

		       
		        getIdStmt.setString(1, document.getTitle());
		        ResultSet rs = getIdStmt.executeQuery();
		        
		        if (rs.next()) {
		            long documentId = rs.getLong("ID");

		            
		            updateStmt.setInt(1, document.getWordCount());
		            updateStmt.setString(2, document.getHash());
		            updateStmt.setString(3, document.getTitle());

		            int rowsUpdated = updateStmt.executeUpdate();
		            if (rowsUpdated > 0) {
		                System.out.println("Document updated successfully.");
		                String deleteSql = "DELETE FROM pages WHERE document_id = ?";
		           	 // Clear existing pages for the document
		           	    try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
		           	        deleteStmt.setLong(1, documentId);
		           	        deleteStmt.executeUpdate();
		           	    }


		           	 PageDAO page = new PageDAO();
		                page.pagination(conn, documentId, document.getContent());
		            } else {
		                System.out.println("No document found with the given title.");
		            }
		        } else {
		            System.out.println("Document not found.");
		        }

		    } catch (SQLException e) {
		        System.err.println("Error updating document: " + e.getMessage());
		    }
	}

	@Override
	public List<String> getDocumentContentByTitle(String title) {
		String getDocumentIdQuery = "SELECT ID FROM document WHERE title = ?";
		String getPageContentQuery = "SELECT page_content FROM pages WHERE document_id = ?";
		List<String> pagesContent = new ArrayList<>();

		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement docStmt = conn.prepareStatement(getDocumentIdQuery);
				PreparedStatement pageStmt = conn.prepareStatement(getPageContentQuery)) {

			// Get the document ID for the given title
			docStmt.setString(1, title);
			ResultSet docRs = docStmt.executeQuery();

			if (docRs.next()) {
				int documentId = docRs.getInt("ID");

				// Fetch pages based on the document ID
				pageStmt.setInt(1, documentId);
				ResultSet pageRs = pageStmt.executeQuery();

				// Collect each page's content
				while (pageRs.next()) {
					pagesContent.add(pageRs.getString("page_content"));
				}
			}
		} catch (SQLException e) {
			System.err.println("Error fetching document content by pages: " + e.getMessage());
		}

		return pagesContent;
	}

	public void insertFile(String title, String content, String hash, Timestamp creationTime, int wordCount) {
		String sql = "INSERT INTO document (title, hash, creation_time, word_count) VALUES (?, ?, ?, ?)";

		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			stmt.setString(1, title);
			stmt.setString(2, hash);
			stmt.setTimestamp(3, creationTime);
			stmt.setInt(4, wordCount);
			stmt.executeUpdate();

			try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					long documentId = generatedKeys.getLong(1);
					System.out.println("File inserted: " + title);
					PageDAO page = new PageDAO();
					page.pagination(conn, documentId, content);
				}
			}
		} catch (SQLException e) {
			System.err.println("Error inserting data for file '" + title + "': " + e.getMessage());
		}
	}

	@Override
	public void insertFilesBatch(List<DocumentDTO> documents) {
		try (Connection conn = DatabaseConnection.getConnection()) {
			conn.setAutoCommit(false); // Start transaction

			for (DocumentDTO doc : documents) {
				insertFile(doc.getTitle(), doc.getContent(), doc.getHash(), doc.getCreationTime(), doc.getWordCount());
			}

			conn.commit();
			System.out.println("Batch insert completed.");

		} catch (SQLException e) {
			System.err.println("Error inserting batch data: " + e.getMessage());
		}
	}

	public DatabaseConnection getDbConnection() {
		return dbConnection;
	}

	public void setDbConnection(DatabaseConnection dbConnection) {
		this.dbConnection = dbConnection;
	}

	public ResultSet searchWord(String searchWord) throws SQLException {
		Connection connection = DatabaseConnection.getConnection(); // Get a connection
		String query = """
				    SELECT d.title, p.page_number, p.page_content
				    FROM document d
				    JOIN pages p ON d.ID = p.document_id
				    WHERE p.page_content LIKE CONCAT('%', ?, '%')
				""";

		PreparedStatement stmt = connection.prepareStatement(query);
		stmt.setString(1, searchWord);

		return stmt.executeQuery(); // Return the ResultSet
	}

	@Override
	public Map<String, Integer> fetchAllContent() {
		Map<String, Integer> contentMap = new HashMap<>();
        String query = "SELECT page_id, document_id, page_number, SUBSTRING(page_content, 1, 50) AS preview FROM pages";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                int pageId = resultSet.getInt("page_id");
                int documentId = resultSet.getInt("document_id");
                int pageNumber = resultSet.getInt("page_number");
                String preview = resultSet.getString("preview");

                String description = "Doc ID: " + documentId + ", Page No: " + pageNumber + ", Preview: " + preview;
                contentMap.put(description, pageId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return contentMap;
	}
	// Method to get the document ID by name
    public int getDocumentIdByName(String documentName) {
    	 
        String sql = "SELECT id FROM document WHERE title = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, documentName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id"); // Returning the document ID
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if no document is found or if there's an error
    }
	
}
