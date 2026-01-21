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

import lombok.Getter;

public class EntryAuthor {
    @Getter
    private String fullName;

    public EntryAuthor(String fullName) {
        //		this.fullName = fullName;
        this.fullName = fullName.replaceAll("(\\.)(\\w)", "$1 $2").trim();
    }

    public String getPicaName() {
        String name = fullName.trim();

        // Condition 1: Format is "LastName, FirstName", keep it
        if (name.contains(",")) {
            return name;
        }

        // Condition 2: Only one word - do not change
        String[] parts = name.split("\\s+");
        if (parts.length == 1) {
            return name;
        }

        // Condition 3: Last word is an initial (e.g. "Nguyen T.A." or "Nguyen T. A.")
        if (isInitial(parts[parts.length - 1])) {
            String lastName = parts[0];
            StringBuilder firstName = new StringBuilder();
            for (int i = 1; i < parts.length; i++) {
                if (i > 1) {
                    firstName.append(" ");
                }
                firstName.append(parts[i]);
            }
            return lastName + ", " + firstName;
        }

        // Condition 4: Standard - last word is last name (e.g. "M. Folger" or "Maik Folger")
        String lastName = parts[parts.length - 1];
        StringBuilder firstName = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
            if (i > 0) {
                firstName.append(" ");
            }
            firstName.append(parts[i]);
        }

        return lastName + ", " + firstName;
    }

    /**
     * Checks if the given string is an initial (e.g. "T.A.").
     * @param str
     * @return
     */
    private boolean isInitial(String str) {
        return str.matches("([A-Za-z]\\.)+");
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
        String trimmed = string.trim();
        int pos = trimmed.lastIndexOf(toReplace);
        if (pos > -1) {
            return trimmed.substring(pos + toReplace.length(), trimmed.length()) + ", " + trimmed.substring(0, pos).trim();
        } else {
            return trimmed;
        }
    }
}
