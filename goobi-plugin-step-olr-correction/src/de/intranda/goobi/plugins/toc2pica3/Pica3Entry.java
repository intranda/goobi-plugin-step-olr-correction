package de.intranda.goobi.plugins.toc2pica3;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import de.intranda.goobi.plugins.EntryAuthor;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Pica3Entry {
    private String type;
    private String year;
    private String id;
    private String volume;
    private String heft;

    private List<EntryAuthor> authors;
    private String title;
    private String pageLabel;

    public void write(Writer w) throws IOException {
        if (this.title == null) {
            return;
        }
        w.write("0500 ");
        w.write(this.type);
        w.write('\n');
        w.write("1100 ");
        w.write(this.year);
        w.write('\n');
        w.write("2185 ");
        w.write(this.id);
        w.write('\n');
        if (this.authors.size() > 0) {
            int authorField = 3011;
            for (EntryAuthor author : this.authors) {
                w.write(Integer.toString(authorField));
                w.write(' ');
                w.write(author.getPicaName());
                w.write('\n');
                authorField++;
            }
        }
        w.write("4000 ");
        w.write(this.title);
        w.write('\n');
        w.write("4070 ");
        w.write("/v");
        w.write(volume);
        w.write("/j");
        w.write(year);
        w.write("/a");
        w.write(this.heft);
        if (this.pageLabel != null) {
            w.write("/p");
            w.write(this.pageLabel);
        }
        w.write('\n');
        w.write('\n');
    }


}
