package pl;


public class WordAnalysis {
    private String lemma;
    private String stem;
    private double tfidf;
    private String postTag;

    public WordAnalysis(String lemma, String stem,String postTag,double tfidf) {
        this.lemma = lemma;
        this.stem = stem;
        this.tfidf = tfidf;
        this.postTag=postTag;
    }

    public String getLemma() {
        return lemma;
    }

    public String getStem() {
        return stem;
    }

    public double getTfidf() {
        return tfidf;
    }

	public String getPostTag() {
		return postTag;
	}


}
