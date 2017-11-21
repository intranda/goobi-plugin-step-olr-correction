package de.intranda.goobi.plugins;

import java.awt.Dimension;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.io.FilenameUtils;
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
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.NIOFileUtils;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.unigoettingen.sub.commons.contentlib.exceptions.ContentLibException;
import de.unigoettingen.sub.commons.contentlib.exceptions.ContentLibImageException;
import de.unigoettingen.sub.commons.contentlib.exceptions.ImageManagerException;
import de.unigoettingen.sub.commons.contentlib.exceptions.ImageManipulatorException;
import de.unigoettingen.sub.commons.contentlib.imagelib.ImageManager;
import de.unigoettingen.sub.commons.contentlib.imagelib.JpegInterpreter;
import de.unigoettingen.sub.commons.contentlib.imagelib.PngInterpreter;
import de.unigoettingen.sub.commons.contentlib.servlet.controller.GetImageDimensionAction;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@Data
@PluginImplementation
@Log4j
public class OlrCorrectionPlugin implements IStepPlugin {

    private Step step;

    private static final String PLUGIN_NAME = "intranda_step_olr-correction";

    private int NUMBER_OF_IMAGES_PER_PAGE = 10;
    //    private int THUMBNAIL_SIZE_IN_PIXEL = 175;
    //    private String THUMBNAIL_FORMAT = "png";
    private String MAINIMAGE_FORMAT = "jpg";
    //    private boolean allowDeletion = false;
    //    private boolean allowRotation = false;
    //    private boolean allowRenaming = false;
    //    private boolean allowSelection = false;
    //    private boolean allowDownload = false;
    //
    //    private String rotationCommandLeft = "";
    //    private String rotationCommandRight = "";
    //    private String deletionCommand = "";
    //    boolean askForConfirmation = true;

    private int pageNo = 0;

    private int imageIndex = 0;

    private String imageFolderName = "";

    private List<Image> allImages = new ArrayList<Image>();
    private Map<String, String> colors;

    private Image image = null;
    private List<String> imageSizes;

    private ExecutorService executor;

    private String returnPath;

    public void setImage(Image image) {
        this.image = image;
        FacesContext context = FacesContextHelper.getCurrentFacesContext();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
        String scaledImageOut = ConfigurationHelper.getTempImagesPathAsCompleteDirectory() + session.getId() + "_" + image.getImageName() + "_large_"
                + ".png";
        String baseUrl = getServletPathWithHostAsUrlFromJsfContext();
        String currentImageUrl = baseUrl + ConfigurationHelper.getTempImagesPath() + session.getId() + "_" + image.getImageName() + "_large_"
                + ".png";
        try {
            if (scaledImageOut != null) {
                float scale = scaleFile(imageFolderName + "/" + image.getImageName(), scaledImageOut, 1200);
                image.setScale(scale);
                image.setImageUrl(currentImageUrl);
            }
        } catch (ContentLibException | IOException e) {
            log.error(e);
        }
    }

    private float scaleFile(String inFileName, String outFileName, int size) throws IOException, ContentLibException {
        ImageManager im = null;
        PngInterpreter pi = null;
        FileOutputStream outputFileStream = null;
        try {
            im = new ImageManager(new File(inFileName).toURI().toURL());
            Dimension dim = new Dimension();
            dim.setSize(size, size);
            float originalHeight = im.getMyInterpreter().getHeight();
            RenderedImage ri = im.scaleImageByPixel(dim, ImageManager.SCALE_TO_BOX, 0);
            pi = new PngInterpreter(ri);
            outputFileStream = new FileOutputStream(outFileName);
            pi.writeToStream(null, outputFileStream);
            return originalHeight / size;
        } finally {
            if (im != null) {
                im.close();
            }
            if (pi != null) {
                pi.close();
            }
            if (outputFileStream != null) {
                outputFileStream.close();
            }
        }
    }

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

        //        allowDeletion = myconfig.getBoolean("allowDeletion", false);
        //        allowRotation = myconfig.getBoolean("allowRotation", false);
        //        allowRenaming = myconfig.getBoolean("allowRenaming", false);
        //        allowSelection = myconfig.getBoolean("allowSelection", false);
        //        allowDownload = myconfig.getBoolean("allowDownload", false);
        //
        //        deletionCommand = myconfig.getString("deletionCommand", "-");
        //        rotationCommandLeft = myconfig.getString("rotationCommands.left", "-");
        //        rotationCommandRight = myconfig.getString("rotationCommands.right", "-");
        //
        //        NUMBER_OF_IMAGES_PER_PAGE = myconfig.getInt("numberOfImagesPerPage", 50);
        //        THUMBNAIL_SIZE_IN_PIXEL = myconfig.getInt("thumbnailsize", 200);
        //        THUMBNAIL_FORMAT = myconfig.getString("thumbnailFormat", "png");
        MAINIMAGE_FORMAT = myconfig.getString("mainImageFormat", "jpg");
        imageSizes = myconfig.getList("imagesize");
        if (imageSizes == null || imageSizes.isEmpty()) {
            imageSizes = new ArrayList<>();
            imageSizes.add("600");
        }
        executor = Executors.newFixedThreadPool(imageSizes.size());
        this.step = step;
        try {
            if (myconfig.getBoolean("useOrigFolder", false)) {
                imageFolderName = step.getProzess().getImagesTifDirectory(false);
            } else {
                imageFolderName = step.getProzess().getImagesOrigDirectory(false);
            }
            Path xmlPath = Paths.get(step.getProzess().getOcrDirectory(), step.getProzess().getTitel() + "_tocxml");

            Path path = Paths.get(imageFolderName);
            if (Files.exists(path) && Files.exists(xmlPath)) {
                List<String> imageNameList = NIOFileUtils.list(imageFolderName);
                int order = 1;

                for (String imagename : imageNameList) {
                    Image currentImage = new Image(imagename, order++, "", imagename);
                    allImages.add(currentImage);
                    String xmlFile = xmlPath.toString() + File.separator + imagename.replace("png", "xml");
                    List<Entry> entries = getEntries(xmlFile);
                    currentImage.setEntryList(entries);

                }
                setImageIndex(0);
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

    public List<Image> getPaginatorList() {
        List<Image> subList = new ArrayList<Image>();
        if (allImages.size() > (pageNo * NUMBER_OF_IMAGES_PER_PAGE) + NUMBER_OF_IMAGES_PER_PAGE) {
            subList = allImages.subList(pageNo * NUMBER_OF_IMAGES_PER_PAGE, (pageNo * NUMBER_OF_IMAGES_PER_PAGE) + NUMBER_OF_IMAGES_PER_PAGE);
        } else {
            subList = allImages.subList(pageNo * NUMBER_OF_IMAGES_PER_PAGE, allImages.size());
        }
        return subList;
    }

    private void createImage(Image currentImage) {

        if (currentImage.getSize() == null) {
            currentImage.setSize(getActualImageSize(currentImage));
        }

        String contextPath = getContextPath();
        for (String sizeString : imageSizes) {
            try {
                int size = Integer.parseInt(sizeString);
                String imageUrl = createImageUrl(currentImage, size, MAINIMAGE_FORMAT, contextPath);
                currentImage.addImageLevel(imageUrl, size);
            } catch (NullPointerException | NumberFormatException e) {
                log.error("Cannot build image with size " + sizeString);
            }
        }
        Collections.sort(currentImage.getImageLevels());
    }

    private String getContextPath() {
        FacesContext context = FacesContextHelper.getCurrentFacesContext();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
        String baseUrl = session.getServletContext().getContextPath();
        return baseUrl;
    }

    private Dimension getActualImageSize(Image image) {
        Dimension dim;
        try {
            String imagePath = imageFolderName + image.getImageName();
            String dimString = new GetImageDimensionAction().getDimensions(imagePath);
            int width = Integer.parseInt(dimString.replaceAll("::.*", ""));
            int height = Integer.parseInt(dimString.replaceAll(".*::", ""));
            dim = new Dimension(width, height);
        } catch (NullPointerException | NumberFormatException | ContentLibImageException | URISyntaxException | IOException e) {
            log.error("Could not retrieve actual image size", e);
            dim = new Dimension(0, 0);
        }
        return dim;
    }

    private String createImageUrl(Image currentImage, Integer size, String format, String baseUrl) {
        StringBuilder url = new StringBuilder(baseUrl);
        url.append("/cs").append("?action=").append("image").append("&format=").append(format).append("&sourcepath=").append("file://"
                + imageFolderName + currentImage.getImageName()).append("&width=").append(size).append("&height=").append(size);
        return url.toString();
    }

    @SuppressWarnings("unused")
    private Dimension scaleFile(String inFileName, String outFileName, List<String> sizes) throws IOException, ContentLibImageException {

        final ImageManager im = new ImageManager(new File(inFileName).toURI().toURL());
        Dimension originalImageSize = new Dimension(im.getMyInterpreter().getWidth(), im.getMyInterpreter().getHeight());
        String outputFilePath = FilenameUtils.getFullPath(outFileName);
        String outputFileBasename = FilenameUtils.getBaseName(outFileName);
        String outputFileSuffix = FilenameUtils.getExtension(outFileName);
        List<Future<File>> createdFiles = new ArrayList<>();
        for (String sizeString : sizes) {
            int size = Integer.parseInt(sizeString);
            final Dimension dim = new Dimension();
            dim.setSize(size, size);
            final String filename = outputFilePath + outputFileBasename + "_" + size + "." + outputFileSuffix;
            createdFiles.add(executor.submit(new Callable<File>() {

                @Override
                public File call() throws Exception {
                    return scaleToSize(im, dim, filename, false);
                }
            }));
        }
        while (!oneImageFinished(createdFiles)) {

        }
        log.debug("First image finished generation");
        return originalImageSize;

    }

    @SuppressWarnings("unused")
    private boolean allImagesFinished(List<Future<File>> createdFiles) {
        for (Future<File> future : createdFiles) {
            try {
                if (!future.isDone() || future.get() == null) {
                    return false;
                }
            } catch (InterruptedException | ExecutionException e) {
            }
        }
        return true;
    }

    private boolean oneImageFinished(List<Future<File>> createdFiles) {
        for (Future<File> future : createdFiles) {
            try {
                if (future.isDone() && future.get() != null) {
                    return true;
                }
            } catch (InterruptedException | ExecutionException e) {
            }
        }
        return false;
    }

    private File scaleToSize(ImageManager im, Dimension dim, String filename, boolean overwrite) throws ImageManipulatorException,
            FileNotFoundException, ImageManagerException, IOException, ContentLibException {
        File outputFile = new File(filename);
        if (!overwrite && outputFile.isFile()) {
            return outputFile;
        }
        try (FileOutputStream outputFileStream = new FileOutputStream(outputFile)) {
            RenderedImage ri = im.scaleImageByPixel(dim, ImageManager.SCALE_TO_BOX, 0);
            try (JpegInterpreter pi = new JpegInterpreter(ri)) {
                pi.writeToStream(null, outputFileStream);
                outputFileStream.close();
                log.debug("Written file " + outputFile);
                return outputFile;
            }
        }
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

        for (Image image : allImages) {
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

    public void setImageIndex(int imageIndex) {
        this.imageIndex = imageIndex;
        if (this.imageIndex < 0) {
            this.imageIndex = 0;
        }
        if (this.imageIndex >= getSizeOfImageList()) {
            this.imageIndex = getSizeOfImageList() - 1;
        }
        if (this.imageIndex >= 0) {
            setImage(allImages.get(this.imageIndex));
        }
    }

    public String getBild() {
        if (image == null) {
            return null;
        } else {
            FacesContext context = FacesContextHelper.getCurrentFacesContext();
            String baseUrl = getServletPathWithHostAsUrlFromJsfContext();
            HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
            String currentImageURL = baseUrl + ConfigurationHelper.getTempImagesPath() + session.getId() + "_" + image.getImageName() + "_large_"
                    + ".jpg";
            return currentImageURL;
        }
    }

    public static String getServletPathWithHostAsUrlFromJsfContext() {
        if (FacesContext.getCurrentInstance() != null) {
            HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            if (request != null) {
                return getServletPathWithHostAsUrlFromRequest(request);
            }
        }

        return "";
    }

    public static String getServletPathWithHostAsUrlFromRequest(HttpServletRequest request) {
        String scheme = request.getScheme(); // http
        String serverName = request.getServerName(); // hostname.com
        int serverPort = request.getServerPort(); // 80
        String contextPath = request.getContextPath(); // /mywebapp
        if (serverPort != 80) {
            return scheme + "://" + serverName + ":" + serverPort + contextPath;
        }
        return scheme + "://" + serverName + contextPath;
    }

    public int getImageWidth() {
        if (image == null) {
            log.error("Must set image before querying image size");
            return 0;
        } else if (image.getSize() == null) {
            createImage(image);
        }
        return image.getSize().width;
    }

    public int getImageHeight() {
        if (image == null) {
            log.error("Must set image before querying image size");
            return 0;
        } else if (image.getSize() == null) {
            createImage(image);
        }
        return image.getSize().height;
    }

    @SuppressWarnings("unused")
    private String getImageUrl(Image image, String size) {
        FacesContext context = FacesContextHelper.getCurrentFacesContext();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
        String currentImageURL = session.getServletContext().getContextPath() + ConfigurationHelper.getTempImagesPath() + session.getId() + "_"
                + image.getImageName() + "_large_" + size + ".jpg";
        return currentImageURL;
    }

    @SuppressWarnings("unused")
    private String getImagePath(Image image) {
        FacesContext context = FacesContextHelper.getCurrentFacesContext();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
        String path = ConfigurationHelper.getTempImagesPathAsCompleteDirectory() + session.getId() + "_" + image.getImageName() + "_large" + ".jpg";
        return path;
    }

    public String cmdMoveFirst() {
        if (this.pageNo != 0) {
            this.pageNo = 0;
            getPaginatorList();
        }
        return "";
    }

    public String cmdMovePrevious() {
        if (!isFirstPage()) {
            this.pageNo--;
            getPaginatorList();
        }
        return "";
    }

    public String cmdMoveNext() {
        if (!isLastPage()) {
            this.pageNo++;
            getPaginatorList();
        }
        return "";
    }

    public String cmdMoveLast() {
        if (this.pageNo != getLastPageNumber()) {
            this.pageNo = getLastPageNumber();
            getPaginatorList();
        }
        return "";
    }

    public void setTxtMoveTo(String neueSeite) {
        try {
            int pageNumber = Integer.parseInt(neueSeite);
            if ((this.pageNo != pageNumber - 1) && pageNumber > 0 && pageNumber <= getLastPageNumber() + 1) {
                this.pageNo = pageNumber - 1;
                getPaginatorList();
            }
        } catch (NumberFormatException e) {
        }
    }

    public String getTxtMoveTo() {
        return this.pageNo + 1 + "";
    }

    public void setImageMoveTo(String page) {
        try {
            int pageNumber = Integer.parseInt(page);
            if ((this.imageIndex != pageNumber - 1) && pageNumber > 0 && pageNumber <= getSizeOfImageList() + 1) {
                setImageIndex(pageNumber - 1);
            }
        } catch (NumberFormatException e) {
        }
    }

    public String getImageMoveTo() {
        return this.imageIndex + 1 + "";
    }

    public int getLastPageNumber() {
        int ret = new Double(Math.floor(this.allImages.size() / NUMBER_OF_IMAGES_PER_PAGE)).intValue();
        if (this.allImages.size() % NUMBER_OF_IMAGES_PER_PAGE == 0) {
            ret--;
        }
        return ret;
    }

    public boolean isFirstPage() {
        return this.pageNo == 0;
    }

    public boolean isLastPage() {
        return this.pageNo >= getLastPageNumber();
    }

    public boolean hasNextPage() {
        return this.allImages.size() > NUMBER_OF_IMAGES_PER_PAGE;
    }

    public boolean hasPreviousPage() {
        return this.pageNo > 0;
    }

    public Long getPageNumberCurrent() {
        return Long.valueOf(this.pageNo + 1);
    }

    public Long getPageNumberLast() {
        return Long.valueOf(getLastPageNumber() + 1);
    }

    public int getSizeOfImageList() {
        return allImages.size();
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
