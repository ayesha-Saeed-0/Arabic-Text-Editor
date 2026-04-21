package dal;

public interface ITransliterate {
	void updateTransliteratedContent(String transliteratedContent, String documentId);

	String getContent(String documentId, String title);
}
