package dto;

public class SearchResult {
	public String title;
	public int pageNumber;
	public String wordBefore;
	public String wordAfter;

	public SearchResult(String title, int pageNumber, String wordBefore, String wordAfter) {
		this.title = title;
		this.pageNumber = pageNumber;
		this.wordBefore = wordBefore;
		this.wordAfter = wordAfter;
	}

	public String getTitle() {
		return title;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public String getWordBefore() {
		return wordBefore;
	}

	public String getWordAfter() {
		return wordAfter;
	}

	// Optionally add setters if you need to update data after creation
}
