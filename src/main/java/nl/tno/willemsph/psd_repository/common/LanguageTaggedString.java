package nl.tno.willemsph.psd_repository.common;

public class LanguageTaggedString {

	public LanguageTaggedString() {
	}

	public LanguageTaggedString(String language, String content) {
		this.language = language;
		this.content = content;
	}

	//
	// Content
	//
	private String content;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	//
	// Language
	//
	private String language;

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

}
