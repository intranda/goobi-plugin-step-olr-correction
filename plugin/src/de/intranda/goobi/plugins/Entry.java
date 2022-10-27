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

        if (authors != null) {
            String[] authorArray = authors.split(",");
            if (authorArray.length == 1 || Arrays.stream(authorArray).anyMatch(author -> author.contains(" and "))) {
                authorArray = authors.split(" and ");
            }
            if (authorArray.length == 1 || Arrays.stream(authorArray).anyMatch(author -> author.contains(";"))) {
                authorArray = authors.split(";");
            }
            if (authorArray != null && authorArray.length > 0) {
                for (String author : authorArray) {
                    EntryAuthor ea = new EntryAuthor(author);
                    myAuthors.add(ea);
                }
            }
        }
        return myAuthors;
    }

    public String getEntryId() {
        StringBuilder id = new StringBuilder().append(pageLabel);
        if (boxes != null && boxes.size() > 0) {
            id.append("-").append(boxes.get(0).getX()).append("-").append(boxes.get(0).getY());
        }
        return id.toString();
    }
}
