package main;

import bll.DocumentBO;
import bll.DocumentFacadeService;
import dal.DatabaseConnection;
import dal.DocumentFacade;
import pl.TextEditorApp;

import javax.swing.SwingUtilities;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
        DatabaseConnection dbManager = new DatabaseConnection();
        DocumentFacade documentFacade = new DocumentFacade(dbManager);
        DocumentBO documentBO = new DocumentBO(documentFacade, dbManager);
        DocumentFacadeService documentService = new DocumentFacadeService(documentBO, documentFacade);  
        SwingUtilities.invokeLater(() -> new TextEditorApp(documentService));
    }
}
