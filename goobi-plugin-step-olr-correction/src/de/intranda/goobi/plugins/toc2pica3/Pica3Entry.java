package de.intranda.goobi.plugins.toc2pica3;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPSClient;

import de.intranda.goobi.plugins.Entry;
import de.intranda.goobi.plugins.EntryAuthor;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Pica3Entry {
	private Entry entry;
	private HashMap<String, String> metadata;

	public void write(Writer w) throws IOException {
		if (entry.getTitle() == null || 
				entry.getAuthors() == null ||
				entry.getAuthors().length() == 0) {
			return;
		}
		w.write("0500 ");
		w.write("Asx");
		w.write('\n');

		w.write("0501 ");
		w.write("Text$btxt");
		w.write('\n');

		w.write("0502 ");
		w.write("ohne Hilfsmittel zu benutzen$bn");
		w.write('\n');

		w.write("0503 ");
		w.write("Band$bnc");
		w.write('\n');

		if (metadata.containsKey("year")) {
			w.write("1100 ");
			w.write(metadata.get("year"));
			w.write('\n');
		}
		
		if (metadata.containsKey("language")) {
			w.write("1500 ");
			w.write(metadata.get("language"));
			w.write('\n');
		}

		w.write("1505 ");
		w.write("$erda");
		w.write('\n');

		// laut GBV mit rein, laut TIB raus
		// w.write("2199 ");
		// w.write(metadata.get("id"));
		// w.write('\n');

		// laut GBV mit rein, laut TIB raus
		// w.write("2240 ");
		// w.write(metadata.get("id"));
		// w.write('\n');

		if (entry.getAuthorList().size() > 0) {
			int authorField = 3000;
			for (EntryAuthor author : entry.getAuthorList()) {
				w.write(Integer.toString(authorField));
				w.write(' ');
				w.write(author.getPicaName());
				w.write("$BVerfasserIn$4aut");
				w.write('\n');
				authorField = 3010;
			}
		}

		// laut GBV mit rein, laut TIB raus
		// w.write("3290 ");
		// w.write("");
		// w.write('\n');

		w.write("4000 ");
		w.write(entry.getTitle());
		w.write("$h");
		w.write(entry.getAuthors());
		w.write('\n');

		if (entry.getPageLabel() != null) {
			w.write("4070 ");
			w.write("/p" + entry.getPageLabel());
			w.write('\n');
		}

		w.write("4241 ");
		//w.write("Enthalten in!" + metadata.get("id") + "!" + metadata.get("title"));
		w.write("Enthalten in!" + metadata.get("id") + "!");
		w.write('\n');

		w.write("7001 ");
		w.write("xa");
		w.write('\n');

		w.write("8600 ");
		w.write("aufkon");
		w.write('\n');

		w.write('\n');
	}

	
}
