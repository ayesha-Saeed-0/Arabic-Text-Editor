package bll;

import java.util.List;

public interface LemmaService {
	public String getLemma(String word);
	public String findStem(String word);
	public String removeSuffixes(String word);
	public String removePrefixes(String word);
	public List<String> tokenizeText(String text);
	 public String normalizeText(String text);
	
	 
}
