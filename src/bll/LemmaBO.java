package bll;

import dal.LemmaDAO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LemmaBO implements LemmaService {

	private LemmaDAO lemmaDAO;
	private static final List<String> PREFIXES = Arrays.asList("ال", "ب", "ك", "ف", "و");
	private static final List<String> SUFFIXES = Arrays.asList("ة", "ه", "ي", "ون", "ان", "ات", "ين");

	public LemmaBO() {
		this.lemmaDAO = new LemmaDAO();
	}

	public String normalizeText(String text) {
		return text.replaceAll("[إأآا]", "ا").replaceAll("ى", "ي").replaceAll("ؤ", "و").replaceAll("ئ", "ي")
				.replaceAll("ء", "").replaceAll("َ|ً|ُ|ٌ|ِ|ٍ|ْ|ّ", ""); // Remove diacritics
	}

	
	public List<String> tokenizeText(String text) {
		String[] tokens = text.split("\\s+");
		List<String> words = new ArrayList<>();
		for (String token : tokens) {
			words.add(token.trim());
		}
		return words;
	}

	public String removePrefixes(String word) {
		for (String prefix : PREFIXES) {
			if (word.startsWith(prefix)) {
				return word.substring(prefix.length());
			}
		}
		return word;
	}

	
	public String removeSuffixes(String word) {
		for (String suffix : SUFFIXES) {
			if (word.endsWith(suffix)) {
				return word.substring(0, word.length() - suffix.length());
			}
		}
		return word;
	}

	public String findStem(String word) {
		word = normalizeText(word);
		word = removePrefixes(word);
		word = removeSuffixes(word);
		return word;
	}

	
	public String getLemma(String word) {
		String lemma = lemmaDAO.fetchLemma(word);
		if (lemma != null && !lemma.isEmpty()) {
			lemma = lemma.replaceAll("[{}]", ""); 
			String[] lemmaParts = lemma.split(":");
			return lemmaParts.length > 1 ? lemmaParts[1].trim() : lemmaParts[0].trim();
		}
		return lemma;
	}

	

}
