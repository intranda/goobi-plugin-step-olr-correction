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
        this.fullName = fullName.replaceAll("(\\.)(\\w)", "$1 $2");
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
        String trimmed = string.trim();
        int pos = trimmed.lastIndexOf(toReplace);
        if (pos > -1) {
            return trimmed.substring(pos + toReplace.length(), trimmed.length()) + ", " + trimmed.substring(0, pos).trim();
        } else {
            return trimmed;
        }
    }
}
