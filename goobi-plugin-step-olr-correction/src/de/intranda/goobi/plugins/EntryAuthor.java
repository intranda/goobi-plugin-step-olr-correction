package de.intranda.goobi.plugins;

import lombok.Getter;

public class EntryAuthor {
	@Getter
	private String fullName;
	
	public EntryAuthor(String fullName) {
		this.fullName = fullName;
	}
	
	public String getPicaName() {
		return replaceReverse(fullName, " ", "@").trim();
	}
	
	private String replaceLast(String string, String toReplace, String replacement) {
        int pos = string.lastIndexOf(toReplace);
        if (pos > -1) {
            return string.substring(0, pos) + replacement + string.substring(pos + toReplace.length(), string.length());
        } else {
            return string;
        }
    }
	
	private String replaceReverse(String string, String toReplace, String replacement) {
        int pos = string.lastIndexOf(toReplace);
        if (pos > -1) {
        		return string.substring(pos + toReplace.length(), string.length()) + ", " + string.substring(0, pos).trim();
        } else {
            return string;
        }
    }
}
