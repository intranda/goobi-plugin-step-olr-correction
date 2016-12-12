package de.intranda.goobi.plugins;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Entry {

    private String institutions;
    private String authors;
    private String title;
    private String pageLabel;
    
}
