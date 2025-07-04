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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.lang.StringUtils;
import org.goobi.beans.GoobiProperty;
import org.goobi.beans.Step;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.enums.StepReturnValue;
import org.goobi.production.plugin.interfaces.IStepPlugin;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import de.intranda.goobi.plugins.toc2pica3.Pica3Entry;
import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.FilesystemHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;
import ugh.dl.RomanNumeral;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;

@Data
@PluginImplementation
@Log4j
public class OlrCorrectionPlugin implements IStepPlugin {

    private static final long serialVersionUID = -203327178125537657L;

    private static final String PLUGIN_NAME = "intranda_step_olr-correction";

    private Step step;
    private TocImageHelper tih = new TocImageHelper();
    private String imageFolderName = "";
    private Map<String, String> colors;
    private String returnPath;
    private HashMap<String, String> metadata = new HashMap<>();
    private String picaPreview;
    private boolean showOCR;
    private boolean inserting;
    private boolean bornDigital = false;
    private int movingEntryIdx;

    @Override
    public void initialize(Step step, String returnPath) {
        picaPreview = null;
        this.returnPath = returnPath;
        SubnodeConfiguration myconfig = ConfigPlugins.getProjectAndStepConfig(PLUGIN_NAME, step);

        tih.setImageFormat(myconfig.getString("imageFormat", "jpg"));
        List<String> imageSizes = Arrays.asList(myconfig.getStringArray("imagesize"));
        if (imageSizes == null || imageSizes.isEmpty()) {
            imageSizes = new ArrayList<>();
            imageSizes.add("600");
        }
        tih.setImageSizes(imageSizes);

        this.step = step;
        try {
            this.bornDigital = myconfig.getBoolean("bornDigital", false);

            setupBornDigital();

            if (myconfig.getBoolean("useOrigFolder", false)) {
                imageFolderName = step.getProzess().getImagesOrigDirectory(false);
            } else {
                imageFolderName = step.getProzess().getImagesTifDirectory(false);
            }
            tih.setImageFolderName(imageFolderName);
            Path xmlPath = Paths.get(step.getProzess().getOcrDirectory(), step.getProzess().getTitel() + "_tocxml");

            Path path = Paths.get(imageFolderName);
            if (Files.exists(path) && Files.exists(xmlPath)) {
                List<String> imageNameList = StorageProvider.getInstance().list(imageFolderName);
                int order = 1;

                for (String imagename : imageNameList) {
                    String text = FilesystemHelper.getOcrFileContent(this.step.getProzess(), imagename.substring(0, imagename.lastIndexOf('.')));
                    Image currentImage = new Image(imagename, order++, "", imagename, text);
                    tih.getAllImages().add(currentImage);
                    String xmlFile = xmlPath.toString() + File.separator + imagename.substring(0, imagename.lastIndexOf('.')) + ".xml";
                    List<Entry> entries = getEntries(xmlFile);
                    currentImage.setEntryList(entries);
                }
            }
            tih.setImageIndex(0);
            this.colors = new HashMap<>();
            List<HierarchicalConfiguration> colors = myconfig.configurationsAt("class");
            for (HierarchicalConfiguration color : colors) {
                this.colors.put(color.getString("type"), color.getString("color"));
            }

            // read the metadata file to get publication type, year and other
            // stuff out of it
            Fileformat gdzfile = step.getProzess().readMetadataFile();
            DocStruct dsParent = gdzfile.getDigitalDocument().getLogicalDocStruct();
            DocStruct ds = gdzfile.getDigitalDocument().getLogicalDocStruct();
            if (ds.getType().isAnchor()) {
                ds = ds.getAllChildren().get(0);
            }

            Prefs prefs = step.getProzess().getRegelsatz().getPreferences();
            metadata.put("type", ds.getType().getName());
            addMetadataField("title", ds, dsParent, prefs, "TitleDocMain");
            addMetadataField("year", ds, dsParent, prefs, "PublicationYear");
            addMetadataField("place", ds, dsParent, prefs, "PlaceOfPublication");
            addMetadataField("id", ds, dsParent, prefs, "CatalogIDDigital");
            addMetadataField("number", ds, dsParent, prefs, "CurrentNo");
            addMetadataField("language", ds, dsParent, prefs, "DocLanguage");

            addMetadataField("_urn", ds, dsParent, prefs, "_urn");
            addMetadataField("AccessLicense", ds, dsParent, prefs, "AccessLicense");
            addMetadataField("AccessLicenseB", ds, dsParent, prefs, "AccessLicenseB");
            addMetadataField("AccessLicenseC", ds, dsParent, prefs, "AccessLicenseC");
            addMetadataField("AccessLicenseG", ds, dsParent, prefs, "AccessLicenseG");
            addMetadataField("AccessLicenseU", ds, dsParent, prefs, "AccessLicenseU");
            addMetadataField("AccessStatus", ds, dsParent, prefs, "AccessStatus");
            addMetadataField("_selectionCode1", ds, dsParent, prefs, "_selectionCode1");
            addMetadataField("_selectionCode2", ds, dsParent, prefs, "_selectionCode2");

        } catch (SwapException | DAOException | IOException | ReadException | PreferencesException e) {
            log.error(e);
        }
    }

    private void setupBornDigital() {
        List<GoobiProperty> lstProps = this.step.getProzess().getProperties();
        for (GoobiProperty prop : lstProps) {
            if (prop.getPropertyName().contentEquals("bornDigital")) {
                this.bornDigital = Boolean.parseBoolean(prop.getPropertyValue());

                if (!bornDigital) {
                    log.debug(this.step.getProzess().getTitel() + " has bornDigital=" + prop.getPropertyValue());
                }
                break;
            }
        }
    }

    public void abortMove() {
        tih.getImage().getEntryList().get(this.movingEntryIdx).setMoving(false);
        this.inserting = false;
        this.movingEntryIdx = -1;
    }

    public void moveEntry(int toIdx) {
        if (toIdx > this.movingEntryIdx) {
            toIdx--;
        }
        List<Entry> currentEntries = tih.getImage().getEntryList();
        if (this.movingEntryIdx >= currentEntries.size()) {
            //this should not happen normally, but we abort moving in this
            tih.getImage().getEntryList().forEach(e -> e.setMoving(false));
            this.inserting = false;
            this.movingEntryIdx = -1;
            return;
        }
        Entry swapEntry = currentEntries.remove(this.movingEntryIdx);
        swapEntry.setMoving(false);
        tih.getImage().getEntryList().add(toIdx, swapEntry);
        this.abortMove();
    }

    private void addMetadataField(String label, DocStruct ds, DocStruct dsParent, Prefs prefs, String type) {
        MetadataType mtype = prefs.getMetadataTypeByName(type);
        if (mtype != null && ds.getAllMetadataByType(mtype).size() > 0) {
            addAllMetadata(label, mtype, ds);
        } else if (mtype != null && dsParent.getAllMetadataByType(mtype).size() > 0) {
            addAllMetadata(label, mtype, dsParent);
        }
    }

    private void addAllMetadata(String label, MetadataType mtype, DocStruct ds) {

        List<? extends Metadata> metaList = ds.getAllMetadataByType(mtype);

        if (metaList.size() < 2) {
            metadata.put(label, metaList.get(0).getValue());
        } else {
            StringBuilder strData = new StringBuilder().append(metaList.get(0).getValue());
            for (int i = 1; i < metaList.size(); i++) {
                strData.append(Pica3Entry.strSplitter).append(metaList.get(i).getValue());
            }
            metadata.put(label, strData.toString());
        }
    }

    private List<Entry> getEntries(String xmlFile) {
        List<Entry> returnList = new LinkedList<>();
        SAXBuilder builder = new SAXBuilder();
        try {
            Document doc = builder.build(xmlFile);
            Element toc = doc.getRootElement();
            List<Element> entryList = toc.getChildren();
            for (Element xmlEntry : entryList) {
                String authors = xmlEntry.getChildText("authors");
                String institutions = xmlEntry.getChildText("institutions");
                String title = xmlEntry.getChildText("title");
                String pageLabel = xmlEntry.getChildText("pageLabel");
                if (pageLabel == null) {
                    pageLabel = "";
                }
                List<Box> boxes = new ArrayList<>();
                Element coords = xmlEntry.getChild("coordinates");
                for (Element boxEl : coords.getChildren("box")) {
                    boxes.add(new Box(boxEl));
                }
                Entry entry = new Entry(institutions, authors, title, pageLabel, boxes, false);
                returnList.add(entry);
            }
        } catch (JDOMException | IOException e) {
            log.error(e);
        }

        return returnList;
    }

    @Override
    public boolean execute() {
        return false;
    }

    @Override
    public PluginGuiType getPluginGuiType() {
        return PluginGuiType.FULL;
    }

    @Override
    public String getPagePath() {
        return "/" + getTheme() + "/OlrTocCorrectionPlugin.xhtml";
    }

    @Override
    public PluginType getType() {
        return PluginType.Step;
    }

    @Override
    public String getTitle() {
        return PLUGIN_NAME;
    }

    public String getDescription() {
        return PLUGIN_NAME;
    }

    @Override
    public String cancel() {
        return "/" + getTheme() + returnPath;
    }

    @Override
    public String finish() {
        try {
            this.showOCR = false;
            Path xmlPath = null;
            File picaFile = new File(step.getProzess().getOcrDirectory(), step.getProzess().getTitel() + ".pica");
            Writer picaWriter = new FileWriter(picaFile);
            xmlPath = Paths.get(step.getProzess().getOcrDirectory(), step.getProzess().getTitel() + "_tocxml");

            XMLOutputter outp = new XMLOutputter();
            outp.setFormat(Format.getPrettyFormat());

            int iterator = 1;

            for (Image image : tih.getAllImages()) {
                String imageName = image.getImageName();
                String xmlName = imageName.substring(0, imageName.lastIndexOf('.')) + ".xml";
                String xmlFile = xmlPath.toString() + File.separator + xmlName;
                Document doc = new Document();
                Element root = new Element("toc");
                doc.setRootElement(root);
                for (Entry entry : image.getEntryList()) {
                    Element xmlEntry = new Element("entry");
                    root.addContent(xmlEntry);

                    if (StringUtils.isNotBlank(entry.getAuthors())) {
                        Element authors = new Element("authors");
                        authors.setText(entry.getAuthors());
                        xmlEntry.addContent(authors);
                    }

                    if (StringUtils.isNotBlank(entry.getInstitutions())) {
                        Element authors = new Element("institutions");
                        authors.setText(entry.getInstitutions());
                        xmlEntry.addContent(authors);
                    }
                    if (StringUtils.isNotBlank(entry.getPageLabel())) {
                        Element authors = new Element("pageLabel");
                        authors.setText(entry.getPageLabel());
                        xmlEntry.addContent(authors);
                    }
                    if (StringUtils.isNotBlank(entry.getTitle())) {
                        Element authors = new Element("title");
                        authors.setText(entry.getTitle());
                        xmlEntry.addContent(authors);
                    }

                    if (entry.getBoxes() != null) {
                        Element coords = new Element("coordinates");
                        for (Box box : entry.getBoxes()) {
                            Element ebox = new Element("box");
                            ebox.setAttribute("x", String.valueOf(box.getX()));
                            ebox.setAttribute("y", String.valueOf(box.getY()));
                            ebox.setAttribute("height", String.valueOf(box.getHeight()));
                            ebox.setAttribute("width", String.valueOf(box.getWidth()));
                            ebox.setAttribute("type", box.getType());
                            coords.addContent(ebox);
                        }
                        xmlEntry.addContent(coords);
                    }

                    // write the pica entry into pica file too
                    if (!bornDigital) {
                        setupBornDigital();
                    }

                    Pica3Entry pEntry = new Pica3Entry(entry, metadata, bornDigital);
                    pEntry.write(picaWriter, (tih.getAllImages().indexOf(image) + 1) + "-" + (image.getEntryList().indexOf(entry) + 1),
                            iterator);
                    //(tih.getAllImages().indexOf(image) + 1));
                    iterator++;
                }
                OutputStream os = new FileOutputStream(xmlFile);
                outp.output(doc, os);
                os.close();

            }

            // close the pica file
            picaWriter.flush();
            picaWriter.close();

        } catch (Exception e) {
            log.error("Error while writing the result files", e);
            Helper.setFehlerMeldung("Fehler beim Speichern der Daten");
            return "";
        }
        return "/" + getTheme() + returnPath;
    }

    @Override
    public HashMap<String, StepReturnValue> validate() {
        return null;
    }

    public void generateEndNumbers() {
        for (int i = 0; i < tih.getAllImages().size(); i++) {
            Image image = tih.getAllImages().get(i);
            List<Entry> entries = new ArrayList<>(image.getEntryList());
            //            Collections.sort(entries, Comparator.comparing(Entry::getPageLabel));
            for (int k = 0; k < entries.size(); k++) {
                Entry currEntry = entries.get(k);
                Optional<String> nextEntryPageLabel = Optional.empty();
                if (k < entries.size() - 1) {
                    //get next entry from this image
                    String label = entries.get(k + 1).getPageLabel();
                    if (label != null) {
                        int minusIndex = label.indexOf('-');
                        if (minusIndex > 0) {
                            label = label.substring(0, minusIndex);
                        }
                        nextEntryPageLabel = Optional.of(label);
                    }
                } else if (i < tih.getAllImages().size() - 1) {
                    //get next entry from next page
                    Image nextImage = tih.getAllImages().get(i + 1);
                    List<Entry> nextEntries = new ArrayList<>(nextImage.getEntryList());
                    //                    Collections.sort(nextEntries, Comparator.comparing(Entry::getPageLabel));
                    if (!nextEntries.isEmpty()) {
                        String label = nextEntries.get(0).getPageLabel();
                        if (label != null) {
                            int minusIndex = label.indexOf('-');
                            if (minusIndex > 0) {
                                label = label.substring(0, minusIndex);
                            }
                            nextEntryPageLabel = Optional.of(label);
                        }
                    }
                }
                String currentLabel = currEntry.getPageLabel();
                int minusIndex = currentLabel.indexOf('-');
                if (minusIndex > 0) {
                    currentLabel = currentLabel.substring(0, minusIndex);
                }
                if (nextEntryPageLabel.isPresent()) {
                    // check for roman numeral
                    try {
                        RomanNumeral roman = new RomanNumeral(nextEntryPageLabel.get());
                        roman = new RomanNumeral(roman.intValue() + 1);
                        currEntry.setPageLabel(currentLabel + "-" + roman.getNumber());
                    } catch (NumberFormatException e) {
                        // didn't work as roman numeral, try normal numbers
                        try {
                            int nextInt = Integer.parseInt(nextEntryPageLabel.get());
                            nextInt--;
                            currEntry.setPageLabel(currentLabel + "-" + nextInt);
                        } catch (NumberFormatException e1) {

                        }
                    }
                } else {
                    currEntry.setPageLabel(currentLabel + "-");
                }
            }
        }
    }

    public void showPicaPreview() {

        if (!bornDigital) {
            setupBornDigital();
        }
        int iterator = 1;
        StringWriter sw = new StringWriter();
        try {
            for (Image image : tih.getAllImages()) {
                for (Entry entry : image.getEntryList()) {

                    Pica3Entry pEntry = new Pica3Entry(entry, metadata, bornDigital);
                    pEntry.write(sw, (tih.getAllImages().indexOf(image) + 1) + "-" + (image.getEntryList().indexOf(entry) + 1), iterator);
                    //(tih.getAllImages().indexOf(image) + 1));

                    iterator++;
                }
            }
        } catch (IOException e) {
            Helper.setFehlerMeldung("Problem while generating the PICA preview", e);
        }
        picaPreview = sw.toString().replace("\n", "<br/>");
    }

    public void closePicaPreview() {
        picaPreview = null;
    }

    private String getTheme() {
        FacesContext context = FacesContextHelper.getCurrentFacesContext();
        String completePath = context.getExternalContext().getRequestServletPath();
        if (StringUtils.isNotBlank(completePath)) {
            String[] parts = completePath.split("/");
            return parts[1];
        }
        return "";
    }

}
