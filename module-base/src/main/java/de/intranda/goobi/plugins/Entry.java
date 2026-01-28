/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 *
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi-workflow
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package de.intranda.goobi.plugins;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.Gson;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Entry {
    private static Gson gson = new Gson();

    private String institutions;
    private String authors;
    private String title;
    private String pageLabel = "";

    private List<Box> boxes;

    private boolean moving = false;

    public String getAsJSON() {
        return gson.toJson(this);
    }

    public List<EntryAuthor> getAuthorList() {
        return extractAuthors(authors);
    }

    // Note: "u." (German abbreviation for "und") is case-sensitive to avoid matching author initials like "U. Pal"
    private static final String AUTHOR_SEPARATOR_PATTERN = "\\s+(?:(?i:and|und|et|y|e|en|with|mit|avec|&)|u\\.)\\s+";

    private List<EntryAuthor> extractAuthors(String authorString) {
        if (authorString == null || authorString.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<EntryAuthor> result = new LinkedList<>();

        // First, split by word separators (and, und, et, etc.)
        if (containsWordSeparator(authorString)) {
            String[] parts = authorString.split(AUTHOR_SEPARATOR_PATTERN);
            for (String part : parts) {
                result.addAll(extractAuthors(part.trim()));
            }
            return result;
        }

        String[] authorArray = splitByPattern(authorString);

        for (String author : authorArray) {
            String trimmed = author.trim();
            if (!trimmed.isEmpty()) {
                result.add(new EntryAuthor(trimmed));
            }
        }

        return result;
    }

    private boolean containsWordSeparator(String authorString) {
        return authorString.matches(".*" + AUTHOR_SEPARATOR_PATTERN + ".*");
    }

    /**
     * Determines the appropriate splitting pattern and splits the author string accordingly.
     */
    private String[] splitByPattern(String authorString) {
        if (isSemicolonSeparated(authorString)) {
            return authorString.split("\\s*;\\s*");
        }
        if (isCommaSeparatedWithInitials(authorString)) {
            return authorString.split("\\s*,\\s*(?=[A-ZÁČĎÉĚÍŇÓŘŠŤÚŮÝŽ]\\.)");
        }
        if (isSingleAuthorLastnameFirstname(authorString)) {
            return new String[] { authorString };
        }
        if (isCommaSeparated(authorString)) {
            return authorString.split("\\s*,\\s*");
        }
        // Fallback: Single author
        return new String[] { authorString };
    }

    /**
     * Pattern 1: Semicolon-separated authors (e.g., "Folger, M.;Alkatiri, F.;Nguyen, T.A.")
     */
    private boolean isSemicolonSeparated(String authorString) {
        return authorString.contains(";");
    }

    /**
     * Pattern 2: Comma-separated with initials - only if ALL parts after comma start with initial
     * (e.g., "M. Folger, F. Alkatiri, T.A. Nguyen")
     */
    private boolean isCommaSeparatedWithInitials(String authorString) {
        return authorString.matches(".*,\\s+[A-ZÁČĎÉĚÍŇÓŘŠŤÚŮÝŽ]\\.(\\s|$).*")
                && allPartsStartWithInitial(authorString);
    }

    /**
     * Pattern 3: Single author in "Lastname, Firstname" format (exactly one comma,
     * and the part after the comma looks like a first name, not a full name)
     */
    private boolean isSingleAuthorLastnameFirstname(String authorString) {
        return authorString.contains(",")
                && authorString.indexOf(",") == authorString.lastIndexOf(",")
                && looksLikeLastnameFirstname(authorString);
    }

    /**
     * Pattern 4: Simple comma-separated authors (e.g., "Folger, Alkatiri, Nguyen" or "Maik Folger, Tom Nguyen")
     */
    private boolean isCommaSeparated(String authorString) {
        return authorString.contains(",");
    }

    public String getEntryId() {
        StringBuilder id = new StringBuilder().append(pageLabel);
        if (boxes != null && boxes.size() > 0) {
            id.append("-").append(boxes.get(0).getX()).append("-").append(boxes.get(0).getY());
        }
        return id.toString();
    }

    /**
     * Checks if all parts after commas start with an initial (e.g., "M." or "T.A.").
     * This is used to determine if Pattern 2 (comma-separated initials) should be applied.
     */
    private boolean allPartsStartWithInitial(String authorString) {
        String[] parts = authorString.split("\\s*,\\s*");
        for (int i = 1; i < parts.length; i++) {
            if (!parts[i].matches("^[A-ZÁČĎÉĚÍŇÓŘŠŤÚŮÝŽ]\\..*")) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if a string with exactly one comma looks like "Lastname, Firstname" format.
     * Returns false if the part after the comma looks like a full name (e.g., "Firstname Lastname"),
     * which would indicate comma-separated authors.
     * Returns true if the part after the comma looks like a first name only.
     */
    private boolean looksLikeLastnameFirstname(String authorString) {
        int commaIndex = authorString.indexOf(",");
        String afterComma = authorString.substring(commaIndex + 1).trim();

        // If the part after the comma contains multiple words where at least two start
        // with uppercase letters (like "Tom Albert Nguyen"),
        // it's likely another full author name, not a first name
        String[] words = afterComma.split("\\s+");
        if (words.length >= 2) {
            int uppercaseWordCount = 0;
            for (String word : words) {
                if (!word.isEmpty() && Character.isUpperCase(word.charAt(0)) && !word.matches("^[A-ZÁČĎÉĚÍŇÓŘŠŤÚŮÝŽ]\\.$")) {
                    uppercaseWordCount++;
                }
            }
            // If there are 2+ uppercase words that aren't initials, this looks like a full name
            if (uppercaseWordCount >= 2) {
                return false;
            }
        }
        return true;
    }
}
