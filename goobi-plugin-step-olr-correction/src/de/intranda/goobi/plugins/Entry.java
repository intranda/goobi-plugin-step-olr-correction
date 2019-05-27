package de.intranda.goobi.plugins;

import java.util.ArrayList;
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

    public String getAsJSON() {
        return gson.toJson(this);
    }

    public List<EntryAuthor> getAuthorList() {
        List<EntryAuthor> myAuthors = new ArrayList<EntryAuthor>();

        if (authors != null) {
            String[] authorArray = authors.split(",| and ");
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
        String id = pageLabel;
        if (boxes != null && boxes.size() > 0) {
            id += "-" + boxes.get(0).getX() + "-" + boxes.get(0).getY();
        }
        return id;
    }
}
