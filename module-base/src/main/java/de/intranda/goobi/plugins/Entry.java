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

import java.util.ArrayList;
import java.util.Arrays;
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
        List<EntryAuthor> myAuthors = new ArrayList<>();

        if (authors == null || authors.trim().isEmpty()) {
            return myAuthors;
        }

        String[] authorArray = splitAuthors(authors);

        for (String author : authorArray) {
            String trimmed = author.trim();
            if (!trimmed.isEmpty()) {
                myAuthors.add(new EntryAuthor(trimmed));
            }
        }

        return myAuthors;
    }

    private String[] splitAuthors(String authorString) {
        // Pattern 1: Semicolon-separated (e.g., "Folger, M.;Alkatiri, F.;Nguyen, T.A.")
        if (authorString.contains(";")) {
            return authorString.split("\\s*;\\s*");
        }

        // Pattern 2: " and " separated (e.g., "Maik Folger and Frank Alkatiri and Tom Albert Nguyen")
        if (authorString.contains(" and ")) {
            return authorString.split("\\s+and\\s+");
        }

        // Pattern 3: Comma-separated initials (e.g., "M. Folger, F. Alkatiri, T.A. Nguyen")
        if (authorString.matches(".*,\\s+[A-ZÁČĎÉĚÍŇÓŘŠŤÚŮÝŽ]\\.(\\s|$).*")) {
            return authorString.split("\\s*,\\s*(?=[A-ZÁČĎÉĚÍŇÓŘŠŤÚŮÝŽ]\\.)");
        }

        // Pattern 4: Simple comma-separated (e.g., "Folger, Alkatiri, Nguyen")
        if (authorString.contains(",")) {
            return authorString.split("\\s*,\\s*");
        }

        // Fallback: Single Author
        return new String[] { authorString };
    }

    public String getEntryId() {
        StringBuilder id = new StringBuilder().append(pageLabel);
        if (boxes != null && boxes.size() > 0) {
            id.append("-").append(boxes.get(0).getX()).append("-").append(boxes.get(0).getY());
        }
        return id.toString();
    }
}
