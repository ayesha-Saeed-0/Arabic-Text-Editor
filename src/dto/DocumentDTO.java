
package dto;

import java.sql.Timestamp;

public class DocumentDTO {
	private String title;
	private Timestamp creationTime;
	private Timestamp updationTime;
	private String content;
	private int wordCount;
	private String hash;
	private int pageCount;
	private String transContent;
	private int id;

	  public DocumentDTO(int id, String title, String hash, Timestamp creationTime, int wordCount) {
	        this.id = id; // Initialize documentId
	        this.title = title;
	        this.hash = hash;
	        this.creationTime = creationTime;
	        this.wordCount = wordCount;
	    }

	// Other constructors
	public DocumentDTO(int wordCount, String content) {
		this.content = content;
		this.wordCount = wordCount;
	}

	public DocumentDTO(String title, Timestamp creationTime, Timestamp updationTime) {
		this.title = title;
		this.creationTime = creationTime;
		this.updationTime = updationTime;
	}

	public DocumentDTO(String title, String content, String hash, Timestamp creationTime, int wordCount) {
		this.title = title;
		this.content = content;
		this.hash = hash;
		this.creationTime = creationTime;
		this.wordCount = wordCount;
	}

	public DocumentDTO(String title, String content, int wordCount, String hash) {
		this.title = title;
		this.content = content;
		this.wordCount = wordCount;
		this.hash = hash;
	}

	public DocumentDTO(String title, String content, int wordCount, int pageCount) {
		this.title = title;
		this.content = content;
		this.wordCount = wordCount;
		this.pageCount = pageCount;
	}

	// Getters and Setters
	public String getTitle() {
		return title;
	}

	public Timestamp getCreationTime() {
		return creationTime;
	}

	public Timestamp getUpdationTime() {
		return updationTime;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	public void setTitle(String titl) {
		this.title = titl;
	}

	public int getWordCount() {
		return wordCount;
	}

	public void setWordCount(int wordCount) {
		this.wordCount = wordCount;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public int getPageCount() {
		return pageCount;
	}

	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}

	public String getTransliterateContent() {
		return transContent;

	}

	public void setTransliterateContent(String input) {
		this.transContent = input;
	}
	 public int getId() {
	        return id; // Getter for documentId
	    }
}
