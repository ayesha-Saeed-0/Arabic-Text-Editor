
package bll;

import java.util.List;
import java.util.Map;

public interface StatService {
	public double calculatePMI(String word1, String word2, String documentName);
	public double calculateTFIDF(String word, String documentName);
	public double calculateIDF(String word);
	 public double calculateTF(String word, String documentName);
	 public String calculatePKL(String word1,String word2, String documentName);
	 public Map<String, List<String>> fetchAndNormalizeData();
	 public int getDocumentIdByName(String documentName);
	 public List<String> fetchAndNormalizeDataForDocument(String documentName);

}

