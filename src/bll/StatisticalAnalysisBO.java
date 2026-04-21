package bll;

import java.util.*;

import dal.DocumentFacade;
import dto.DocumentDTO;

public class StatisticalAnalysisBO implements StatService {
	private final DocumentFacade dao;
	private final LemmaBO lemmaBO;

	public StatisticalAnalysisBO(DocumentFacade facade, LemmaBO lemmaBO) {
		this.dao = facade;
		this.lemmaBO = lemmaBO;
	}

	@Override

	public Map<String, List<String>> fetchAndNormalizeData() {
		Map<String, List<String>> normalizedData = new HashMap<>();

		List<DocumentDTO> documents = dao.fetchDocuments();

		for (DocumentDTO document : documents) {
			String title = document.getTitle();

			List<String> content = dao.getDocumentContentByTitle(title);

			StringBuilder combinedContent = new StringBuilder();
			for (String page : content) {
				combinedContent.append(page).append(" ");
			}

			List<String> normalizedWords = normalizeContent(combinedContent.toString());

			normalizedData.put(title, normalizedWords);
		}
		return normalizedData;
	}

	private List<String> normalizeContent(String content) {
		String[] words = content.split("\\s+");
		List<String> normalizedWords = new ArrayList<>();

		for (String word : words) {
			String normalizedWord = lemmaBO.findStem(word);
			normalizedWords.add(normalizedWord);
		}
		return normalizedWords;
	}

	@Override
	public double calculateIDF(String word) {
		Map<String, List<String>> normalizedData = fetchAndNormalizeData();
		int totalDocuments = normalizedData.size();
		int documentFrequency = 0;

		for (List<String> words : normalizedData.values()) {
			if (words.contains(word)) {
				documentFrequency++;
			}
		}

		if (documentFrequency > 0) {
			return Math.log((double) totalDocuments / documentFrequency);
		} else {
			return 0.0;
		}
	}

	@Override
	public double calculateTF(String word, String documentName) {
		Map<String, List<String>> normalizedData = fetchAndNormalizeData();

		List<String> documentWords = normalizedData.get(documentName);
		if (documentWords == null) {
			return 0.0;
		}

		long wordCount = documentWords.stream().filter(w -> w.equals(word)).count();

		return (double) wordCount / documentWords.size();
	}

	@Override
	public double calculateTFIDF(String word, String documentName) {

		word = lemmaBO.findStem(word);

		double tf = calculateTF(word, documentName);
		double idf = calculateIDF(word);

		return tf * idf;
	}
@Override
	public List<String> fetchAndNormalizeDataForDocument(String documentName) {
	    List<String> normalizedWords = new ArrayList<>();
	    
	    List<String> content = dao.getDocumentContentByTitle(documentName);
	    
	    if (content == null || content.isEmpty()) {
	        return normalizedWords; 
	    }

	    // Combine all pages content into a single string
	    StringBuilder combinedContent = new StringBuilder();
	    for (String page : content) {
	        combinedContent.append(page).append(" ");
	    }

	    
	    normalizedWords = normalizeContent(combinedContent.toString());

	    return normalizedWords;
	}

	
	@Override
	public double calculatePMI(String word1, String word2, String documentName) {
	    final String normalizedWord1 = lemmaBO.findStem(word1);  
	    final String normalizedWord2 = lemmaBO.findStem(word2);

	    
	    List<String> documentWords = fetchAndNormalizeDataForDocument(documentName);
	    
	    if (documentWords == null || documentWords.isEmpty()) {
	        return 0.0;
	    }

	   
	    long countWord1 = documentWords.stream().filter(w -> w.equals(normalizedWord1)).count();
	    long countWord2 = documentWords.stream().filter(w -> w.equals(normalizedWord2)).count();
	    long countBoth = documentWords.stream().filter(w -> w.equals(normalizedWord1) || w.equals(normalizedWord2)).count();

	    double probWord1 = (double) countWord1 / documentWords.size();
	    double probWord2 = (double) countWord2 / documentWords.size();
	    double probBoth = (double) countBoth / documentWords.size();

	    if (probWord1 > 0 && probWord2 > 0) {
	        return Math.log(probBoth / (probWord1 * probWord2));
	    }
	    return 0.0;
	}

	@Override
	public String calculatePKL(String word1, String word2, String documentName) {
	    final String normalizedWord1 = lemmaBO.findStem(word1); 
	    final String normalizedWord2 = lemmaBO.findStem(word2);

	    
	    List<String> documentWords = fetchAndNormalizeDataForDocument(documentName);
	    
	    if (documentWords == null || documentWords.isEmpty()) {
	        return "PKL cannot be calculated: Document is empty or missing.";
	    }

	    long countWord1 = documentWords.stream().filter(w -> w.equals(normalizedWord1)).count();
	    long countWord2 = documentWords.stream().filter(w -> w.equals(normalizedWord2)).count();
	    
	    double probWord1 = (double) countWord1 / documentWords.size();
	    double probWord2 = (double) countWord2 / documentWords.size();
	   
	    double pkl = probWord1 * Math.log(probWord1 /  probWord2);
	   
	    return "PKL: " + pkl;
	}

	@Override
	public int getDocumentIdByName(String documentName) {
		
		return dao.getDocumentIdByName(documentName);
	}

}
