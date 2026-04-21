
package dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PageDAO implements IPage {

	public PageDAO() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void pagination(Connection conn, long documentId, String content) throws SQLException {
		if (content.length() > 0) {
			String pageSql = "INSERT INTO pages (document_id, page_number, page_content) VALUES (?, ?, ?)";
			String[] lines = content.split("\\r?\\n");
			int currentPageNumber = 1;
			StringBuilder pageContent = new StringBuilder();
			int currentLineCount = 0;
			int currentWordCount = 0;
			final int maxWordsPerPage = 500;
			final int maxLinesPerPage = 30;

			for (String line : lines) {
				String[] wordsInLine = line.split("\\s+");
				int wordsInLineCount = wordsInLine.length;

				if (currentWordCount + wordsInLineCount > maxWordsPerPage || currentLineCount + 1 > maxLinesPerPage) {

					try (PreparedStatement pageStmt = conn.prepareStatement(pageSql)) {
						pageStmt.setLong(1, documentId);
						pageStmt.setInt(2, currentPageNumber);
						pageStmt.setString(3, pageContent.toString().trim());
						pageStmt.executeUpdate();
					}
					currentPageNumber++;
					pageContent.setLength(0);
					currentWordCount = 0;
					currentLineCount = 0;
				}

				pageContent.append(line).append("\n");
				currentLineCount++;
				currentWordCount += wordsInLineCount;

			}

			// Insert any remaining content as the last page
			if (currentLineCount > 0 || currentWordCount > 0) {
				try (PreparedStatement pageStmt = conn.prepareStatement(pageSql)) {
					pageStmt.setLong(1, documentId);
					pageStmt.setInt(2, currentPageNumber);
					pageStmt.setString(3, pageContent.toString().trim());
					pageStmt.executeUpdate();
				}
			}
		}
	}
}
