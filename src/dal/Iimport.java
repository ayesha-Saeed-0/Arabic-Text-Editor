package dal;

import java.sql.Timestamp;
import java.util.List;

import dto.DocumentDTO;

public interface Iimport {

	void insertFilesBatch(List<DocumentDTO> documents);

	void insertFile(String title, String content, String hash, Timestamp creationTime, int wordCount);

}
