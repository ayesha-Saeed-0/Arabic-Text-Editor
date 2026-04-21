package dal;

import java.sql.Connection;
import java.sql.SQLException;

public interface IPage {

	void pagination(Connection conn, long documentId, String content) throws SQLException;

}
