package de.intranda.goobi.plugins;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.faces.context.FacesContext;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.StringUtils;
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

import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.NIOFileUtils;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@Data
@PluginImplementation
@Log4j
public class OlrCorrectionPlugin implements IStepPlugin {

	private static final String PLUGIN_NAME = "intranda_step_olr-correction";

    private Step step;
    private TocImageHelper tih = new TocImageHelper();
    private String imageFolderName = "";
    private Map<String, String> colors;
    private ExecutorService executor;
    private String returnPath;

    @Override
    public void initialize(Step step, String returnPath) {
        this.returnPath = returnPath;
        String projectName = step.getProzess().getProjekt().getTitel();
        HierarchicalConfiguration myconfig = null;

        // get the correct configuration for the right project
        List<HierarchicalConfiguration> configs = ConfigPlugins.getPluginConfig(this).configurationsAt("config");
        for (HierarchicalConfiguration hc : configs) {
            List<HierarchicalConfiguration> projects = hc.configurationsAt("project");
            for (HierarchicalConfiguration project : projects) {
                if (myconfig == null || project.getString("").equals("*") || project.getString("").equals(projectName)) {
                    myconfig = hc;
                }
            }
        }
        
        tih.setImageFormat(myconfig.getString("imageFormat", "jpg"));
        List<String> imageSizes = myconfig.getList("imagesize");
        if (imageSizes == null || imageSizes.isEmpty()) {
            imageSizes = new ArrayList<>();
            imageSizes.add("600");
        }
        tih.setImageSizes(imageSizes);
        
        executor = Executors.newFixedThreadPool(imageSizes.size());
        this.step = step;
        try {
            if (myconfig.getBoolean("useOrigFolder", false)) {
                imageFolderName = step.getProzess().getImagesTifDirectory(false);
            } else {
                imageFolderName = step.getProzess().getImagesOrigDirectory(false);
            }
            tih.setImageFolderName(imageFolderName);
            Path xmlPath = Paths.get(step.getProzess().getOcrDirectory(), step.getProzess().getTitel() + "_tocxml");

            Path path = Paths.get(imageFolderName);
            if (Files.exists(path) && Files.exists(xmlPath)) {
                List<String> imageNameList = NIOFileUtils.list(imageFolderName);
                int order = 1;

                for (String imagename : imageNameList) {
                    Image currentImage = new Image(imagename, order++, "", imagename);
                    tih.getAllImages().add(currentImage);
                    String xmlFile = xmlPath.toString() + File.separator + imagename.replace("png", "xml");
                    List<Entry> entries = getEntries(xmlFile);
                    currentImage.setEntryList(entries);
                }
            }
            this.colors = new HashMap<>();
            List<HierarchicalConfiguration> colors = myconfig.configurationsAt("class");
            for (HierarchicalConfiguration color : colors) {
                this.colors.put(color.getString("type"), color.getString("color"));
            }
        } catch (SwapException | DAOException | IOException | InterruptedException e) {
            log.error(e);
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
                List<Box> boxes = new ArrayList<>();
                Element coords = xmlEntry.getChild("coordinates");
                for (Element boxEl : coords.getChildren("box")) {
                    boxes.add(new Box(boxEl));
                }
                Entry entry = new Entry(institutions, authors, title, pageLabel, boxes);
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

        Path xmlPath = null;
        try {
            xmlPath = Paths.get(step.getProzess().getOcrDirectory(), step.getProzess().getTitel() + "_tocxml");
        } catch (SwapException | DAOException | IOException | InterruptedException e1) {
            log.error(e1);
        }
        XMLOutputter outp = new XMLOutputter();
        outp.setFormat(Format.getPrettyFormat());

        for (Image image : tih.getAllImages()) {
            String xmlFile = xmlPath.toString() + File.separator + image.getImageName().replace("png", "xml");
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
                
                if (entry.getBoxes() != null){
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
                
            }
            OutputStream os = null;
            try {
                os = new FileOutputStream(xmlFile);
                outp.output(doc, os);
                os.close();
            } catch (IOException e) {
                log.error(e);
            }
        }

        return "/" + getTheme() + returnPath;
    }

    @Override
    public HashMap<String, StepReturnValue> validate() {
        return null;
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
