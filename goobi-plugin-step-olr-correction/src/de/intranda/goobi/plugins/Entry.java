package de.intranda.goobi.plugins;

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
    private String pageLabel;

    private List<Box> boxes;

    public String getAsJSON() {
        return gson.toJson(this);
    }
}
