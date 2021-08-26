package de.intranda.goobi.plugins.toc2pica3;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;

import de.intranda.goobi.plugins.Entry;
import de.intranda.goobi.plugins.EntryAuthor;
import lombok.AllArgsConstructor;
import lombok.Data;

//@Data
public class Pica3Entry {

    //used for separating metadata inthe hashmap with multiple values
    public static String strSplitter = "~SPLIT~";

    private Entry entry;
    private HashMap<String, String> metadata;
    private Boolean bornDigital;

    private Writer w;

    public Pica3Entry(Entry entry, HashMap<String, String> metadata, boolean bornDigital) {
        this.entry = entry;
        this.metadata = metadata;
        this.bornDigital = bornDigital;

    }

    public void write(Writer w, String entryCounter, int iIndexNumber) throws IOException {
        if (entry.getTitle() == null || entry.getAuthors() == null || entry.getAuthors().length() == 0) {
            return;
        }

        this.w = w;

        if (bornDigital) {
            w.write("0500 ");
            w.write("Osx");
            w.write('\n');

            w.write("0501 ");
            w.write("Text$btxt");
            w.write('\n');

            w.write("0502 ");
            w.write("Computermedien$bc");
            w.write('\n');

            w.write("0503 ");
            w.write("Online-Ressource$bcr");
            w.write('\n');
        } else {
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
        }

        // only if the year exists write it there
        writeMetadata("year", 1100);
        //        if (metadata.containsKey("year")) {
        //            w.write("1100 ");
        //            w.write(metadata.get("year").trim());
        //            w.write('\n');
        //        }

        writeMetadata("language", 1500);

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

        w.write("2199 ");

        if (bornDigital) {
            w.write("ConTIBo_");
        } else {
            w.write("ConTIB_");
        }

        w.write(metadata.get("id"));
        w.write("_");
        w.write(entryCounter);
        w.write('\n');

        if (entry.getAuthorList().size() > 0) {
            int authorField = 3000;
            for (EntryAuthor author : entry.getAuthorList()) {
                w.write(Integer.toString(authorField));
                w.write(' ');
                w.write(author.getPicaName().trim());
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
        w.write(entry.getTitle().trim());
        w.write("$h");
        w.write(entry.getAuthors().trim());
        w.write('\n');

        if (entry.getPageLabel() != null) {
            w.write("4070 ");
            // add a year if it is there
            if (metadata.containsKey("year")) {
                w.write("$j" + metadata.get("year").trim());
            }
            // now add the pages
            String strPage = entry.getPageLabel().trim();

            if (bornDigital) {
                w.write("$i" + iIndexNumber);
            }

            if (!strPage.isEmpty()) {
                w.write("$p" + strPage);
            }

            w.write('\n');
        }

        w.write("4241 ");
        //w.write("Enthalten in!" + metadata.get("id") + "!" + metadata.get("title"));
        w.write("Enthalten in!" + metadata.get("id") + "!");
        w.write('\n');

        //instructions from TIB:
        Boolean boWriteUrn = true;
        if (metadata.containsKey("_selectionCode1") && metadata.containsKey("_selectionCode2")) {
            String strCode1 = metadata.get("_selectionCode1").replace(" ", "").toLowerCase();
            String strCode2 = metadata.get("_selectionCode2").replace(" ", "").toLowerCase();

            if (strCode1.contentEquals("gbv") && strCode2.contentEquals("hybr")) {
                boWriteUrn = false;
            }
        }

        writeAccessLicense();

        if (boWriteUrn) {
            writeMetadata("_urn", 7133); //orig: 4950, requested output: 7133
        }

        if (bornDigital) {
            w.write("8600 ");
            w.write("ConTIBo");
            w.write('\n');
        } else {
            w.write("8600 ");
            w.write("ConTIB");
            w.write('\n');
        }

        w.write("E001 ");
        w.write("xa");
        w.write('\n');

        //        if (metadata.containsKey("_urn")) {
        //            w.write("4950 ");
        //            w.write(metadata.get("_urn"));
        //            w.write('\n');
        //        }
        //        if (metadata.containsKey("AccessLicense")) {
        //            w.write("4980 ");
        //            w.write(metadata.get("AccessLicense"));
        //            w.write('\n');
        //        }
        //        if (metadata.containsKey("AccessStatus")) {
        //            w.write("4985 ");
        //            w.write(metadata.get("AccessStatus"));
        //            w.write('\n');
        //        }

        w.write('\n');
    }

    private void writeAccessLicense() throws IOException {
        String strAccess = "";

        if (metadata.containsKey("AccessLicenseB")) {
            strAccess += "[" + metadata.get("AccessLicenseB").trim() + "]";
        }
        if (metadata.containsKey("AccessLicense")) {
            strAccess += metadata.get("AccessLicense").trim();
        }
        if (metadata.containsKey("AccessLicenseC")) {
            strAccess += "$c" + metadata.get("AccessLicenseC").trim();
        }
        if (metadata.containsKey("AccessLicenseG")) {
            strAccess += "$g" + metadata.get("AccessLicenseG").trim();
        }
        if (metadata.containsKey("AccessLicenseU")) {
            strAccess += "$u" + metadata.get("AccessLicenseU").trim();
        }

        w.write(4980 + " ");
        w.write(strAccess);
        w.write('\n');

    }

    private void writeMetadata(String key, int picaId) throws IOException {
        if (metadata.containsKey(key)) {

            String strData = metadata.get(key).trim();
            if (!strData.contains(strSplitter)) {
                w.write(picaId + " ");
                w.write(metadata.get(key).trim());
                w.write('\n');
            } else {
                String[] strDatas = strData.split(strSplitter);
                for (int i = 0; i < strDatas.length; i++) {
                    w.write(picaId + " ");
                    w.write(strDatas[i].trim());
                    w.write('\n');
                }
            }
        }
    }

}
