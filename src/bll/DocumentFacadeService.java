package bll;

import dto.SearchResult;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import dal.DocumentFacade;


public class DocumentFacadeService implements DocumentService, StatService, LemmaService,IExport {
	private final DocumentBO documentBO;
	private final StatisticalAnalysisBO stat;
	private final LemmaBO lemma;
	private final ExportBO export;

	public DocumentFacadeService(DocumentBO documentBO, DocumentFacade documentFacade)  {
		this.documentBO = documentBO;
		
		this.lemma= new LemmaBO();
		this.export=new ExportBO();
		this.stat = new StatisticalAnalysisBO(documentFacade, lemma);
	}
	@Override
	public List<String> getFormattedDocuments() {
		return documentBO.getFormattedDocuments();
	}

	@Override
	public void importFile(String filePath) throws IOException, NoSuchAlgorithmException, DuplicateFileException {
		documentBO.importFile(filePath);
	}

	@Override
	public void importFiles(List<String> filePaths) {
		documentBO.importFiles(filePaths);
	}

	@Override
	public void createAndSaveDocument(String fileName, String content) throws IOException, NoSuchAlgorithmException {
		documentBO.createAndSaveDocument(fileName, content);
	}

	@Override
	public boolean deleteDocument(String fileName) {
		return documentBO.deleteDocument(fileName);
	}

	@Override
	public void updateDocument(String fileName, String newContent) throws NoSuchAlgorithmException, IOException {
		documentBO.updateDocument(fileName, newContent);
	}

	@Override
	public List<String> getDocumentContentByTitle(String title) {
		return documentBO.getDocumentContentByTitle(title);
	}

	

	@Override
	public List<SearchResult> search(String word) {
		return documentBO.search(word);
	}

	

	public String getContent(String documentId,String title) {
		// TODO Auto-generated method stub
		return documentBO.getContent(documentId, title);
	}


	public void transliterateAndSaveContent(String docId, String title) {
		documentBO.transliterateAndSaveContent(docId, title);
		
	}

	@Override
	public String transliterateContent(String input, String title) {
		// TODO Auto-generated method stub
		return documentBO.transliterateContent(input,title);
	}

	@Override
	public Map<String, Integer> getAllContent() {
		return documentBO.getAllContent();
	}


	@Override
	public String tagText(String text) {
		// TODO Auto-generated method stub
		return documentBO.tagText(text);
	}

	@Override
	public double calculatePMI(String word1, String word2, String documentName) {
		// TODO Auto-generated method stub
		return stat.calculatePMI(word1, word2, documentName);
	}

	@Override
	public double calculateTFIDF(String word, String documentName) {
		// TODO Auto-generated method stub
		return stat.calculateTFIDF(word, documentName);
	}

	@Override
	public double calculateIDF(String word) {
		// TODO Auto-generated method stub
		return stat.calculateIDF(word);
	}

	@Override
	public double calculateTF(String word, String documentName) {
		// TODO Auto-generated method stub
		return stat.calculateTF(word, documentName);
	}

	@Override
	public String calculatePKL(String word1,String word2, String documentName) {
		// TODO Auto-generated method stub
		return stat.calculatePKL(word1, word2, documentName);
	}


	@Override
	public String getLemma(String word) {
		// TODO Auto-generated method stub
		return lemma.getLemma(word);
	}

	@Override
	public String findStem(String word) {
		// TODO Auto-generated method stub
		return lemma.findStem(word);
	}

	@Override
	public String removeSuffixes(String word) {
		// TODO Auto-generated method stub
		return lemma.removeSuffixes(word);
	}

	@Override
	public String removePrefixes(String word) {
		// TODO Auto-generated method stub
		return lemma.removePrefixes(word);
	}

	@Override
	public List<String> tokenizeText(String text) {
		// TODO Auto-generated method stub
		return lemma.tokenizeText(text);
	}

	@Override
	public String normalizeText(String text) {
		// TODO Auto-generated method stub
		return lemma.normalizeText(text);
	}
	
	
	@Override
	public String convertToMarkdown(String content) {
		// TODO Auto-generated method stub
		return export.convertToMarkdown(content);
	}
	@Override
	public Map<String, List<String>> fetchAndNormalizeData() {
		// TODO Auto-generated method stub
		return stat.fetchAndNormalizeData();
	}
	@Override
	public int getDocumentIdByName(String documentName) {
		// TODO Auto-generated method stub
		return stat.getDocumentIdByName(documentName);
	}
	@Override
	public List<String> fetchAndNormalizeDataForDocument(String documentName) {
		// TODO Auto-generated method stub
		return stat.fetchAndNormalizeDataForDocument(documentName);
	}

}
