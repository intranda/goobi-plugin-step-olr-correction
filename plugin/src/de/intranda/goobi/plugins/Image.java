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

import java.awt.Dimension;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import lombok.Data;

@Data
public class Image {
    private Gson gson = null;

    private String imageName;
    private int order;
    //    private String thumbnailUrl;
    //    private String largeThumbnailUrl;
    private List<ImageLevel> imageLevels = new ArrayList<>();
    private String tooltip;
    private Dimension size = null;
    private String tempName;
    private boolean selected = false;
    private Entry currentEntry;
    private float scale;
    private String ocrText;
    private String imageUrl;

    private List<Entry> entryList = new LinkedList<>();

    public Image(String imageName, int order, String thumbnailUrl, String tooltip, String ocrText) {
        this.ocrText = ocrText;
        this.imageName = imageName;
        this.order = order;
        //        this.thumbnailUrl = thumbnailUrl;
        this.tooltip = tooltip;
    }

    public String getEntriesAsJSON() {
        if (this.gson == null) {
            initGson();
        }
        return gson.toJson(this.entryList);
    }

    private void initGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(String.class, new EscapeStringSerializer());
        this.gson = builder.create();
    }

    public String getImageNameShort() {
        if (imageName.length() > 25) {
            return "..." + imageName.substring(imageName.length() - 25, imageName.length());
        } else {
            return imageName;
        }
    }

    public void addImageLevel(String imageUrl, int size) {
        double scale = size / (double) (Math.max(getSize().height, getSize().width));
        Dimension dim = new Dimension((int) (getSize().width * scale), (int) (getSize().height * scale));
        ImageLevel layer = new ImageLevel(imageUrl, dim);
        imageLevels.add(layer);
    }

    public void addEntry(Entry entry) {
        entryList.add(entry);
    }

    public void createEntry() {
        entryList.add(new Entry("", "", "", "", new ArrayList<Box>(), false));
    }

    public void removeEntry() {
        entryList.remove(currentEntry);
    }

    public void removeAllEntriesOfThisPage() {
        entryList.clear();
    }

    private static class EscapeStringSerializer implements JsonSerializer<String> {
        @Override
        public JsonElement serialize(String s, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(escapeJS(s));
        }

        public static String escapeJS(String string) {
            String escapes[][] = new String[][] { { "\\", "\\\\" }, { "\"", "\\\"" }, { "\n", "\\n" }, { "\r", "\\r" }, { "\b", "\\b" }, { "\f",
                    "\\f" }, { "\t", "\\t" } };
            for (String[] esc : escapes) {
                string = string.replace(esc[0], esc[1]);
            }
            return string;
        }
    }
}
