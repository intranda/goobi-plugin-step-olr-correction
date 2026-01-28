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

        if (authorString.matches(".*" + AUTHOR_SEPARATOR_PATTERN + ".*")) {
            String[] parts = authorString.split(AUTHOR_SEPARATOR_PATTERN);
            for (String part : parts) {
                result.addAll(extractAuthors(part.trim()));
            }
            return result;
        }

        String[] authorArray;

        // Pattern 1: Semicolon-separated (e.g., "Folger, M.;Alkatiri, F.;Nguyen, T.A.")
        if (authorString.contains(";")) {
            authorArray = authorString.split("\\s*;\\s*");
        }
        // Pattern 2: Comma-separated initials (e.g., "M. Folger, F. Alkatiri, T.A. Nguyen")
        else if (authorString.matches(".*,\\s+[A-ZÁČĎÉĚÍŇÓŘŠŤÚŮÝŽ]\\.(\\s|$).*")) {
            authorArray = authorString.split("\\s*,\\s*(?=[A-ZÁČĎÉĚÍŇÓŘŠŤÚŮÝŽ]\\.)");
        }
        // Pattern 3: Single author in "Lastname, Firstname" format (only one comma)
        else if (authorString.contains(",") && authorString.indexOf(",") == authorString.lastIndexOf(",")) {
            authorArray = new String[] { authorString };
        }
        // Pattern 4: Simple comma-separated (e.g., "Folger, Alkatiri, Nguyen")
        else if (authorString.contains(",")) {
            authorArray = authorString.split("\\s*,\\s*");
        }
        // Fallback: Single Author
        else {
            authorArray = new String[] { authorString };
        }

        for (String author : authorArray) {
            String trimmed = author.trim();
            if (!trimmed.isEmpty()) {
                result.add(new EntryAuthor(trimmed));
            }
        }

        return result;
    }

    public String getEntryId() {
        StringBuilder id = new StringBuilder().append(pageLabel);
        if (boxes != null && boxes.size() > 0) {
            id.append("-").append(boxes.get(0).getX()).append("-").append(boxes.get(0).getY());
        }
        return id.toString();
    }
}
