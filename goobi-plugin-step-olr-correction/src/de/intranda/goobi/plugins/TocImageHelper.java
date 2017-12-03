package de.intranda.goobi.plugins;

import java.awt.Dimension;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import de.sub.goobi.helper.FacesContextHelper;
import de.unigoettingen.sub.commons.contentlib.exceptions.ContentLibException;
import de.unigoettingen.sub.commons.contentlib.exceptions.ContentLibImageException;
import de.unigoettingen.sub.commons.contentlib.exceptions.ImageManagerException;
import de.unigoettingen.sub.commons.contentlib.exceptions.ImageManipulatorException;
import de.unigoettingen.sub.commons.contentlib.imagelib.ImageManager;
import de.unigoettingen.sub.commons.contentlib.imagelib.JpegInterpreter;
import de.unigoettingen.sub.commons.contentlib.imagelib.PngInterpreter;
import de.unigoettingen.sub.commons.contentlib.servlet.controller.GetImageDimensionAction;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

@Log4j
public class TocImageHelper {

	@Getter @Setter
	private List<String> imageSizes;
	@Getter @Setter
	private String imageFormat = "jpg";
	@Getter @Setter
	private String imageFolderName = "";
	
	public float scaleFile(String inFileName, String outFileName, int size) throws IOException, ContentLibException {
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
	
	public void createImage(Image currentImage) {

        if (currentImage.getSize() == null) {
            currentImage.setSize(getActualImageSize(currentImage));
        }

        String contextPath = getContextPath();
        for (String sizeString : imageSizes) {
            try {
                int size = Integer.parseInt(sizeString);
                String imageUrl = createImageUrl(currentImage, size, imageFormat, contextPath);
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
	
//	 @SuppressWarnings("unused")
//	    private String getImageUrl(Image image, String size) {
//	        FacesContext context = FacesContextHelper.getCurrentFacesContext();
//	        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
//	        String currentImageURL = session.getServletContext().getContextPath() + ConfigurationHelper.getTempImagesPath() + session.getId() + "_"
//	                + image.getImageName() + "_large_" + size + ".jpg";
//	        return currentImageURL;
//	    }
	
//	@SuppressWarnings("unused")
//    private String getImagePath(Image image) {
//        FacesContext context = FacesContextHelper.getCurrentFacesContext();
//        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
//        String path = ConfigurationHelper.getTempImagesPathAsCompleteDirectory() + session.getId() + "_" + image.getImageName() + "_large" + ".jpg";
//        return path;
//    }
	    
//	    @SuppressWarnings("unused")
//	    private Dimension scaleFile(String inFileName, String outFileName, List<String> sizes) throws IOException, ContentLibImageException {
//
//	        final ImageManager im = new ImageManager(new File(inFileName).toURI().toURL());
//	        Dimension originalImageSize = new Dimension(im.getMyInterpreter().getWidth(), im.getMyInterpreter().getHeight());
//	        String outputFilePath = FilenameUtils.getFullPath(outFileName);
//	        String outputFileBasename = FilenameUtils.getBaseName(outFileName);
//	        String outputFileSuffix = FilenameUtils.getExtension(outFileName);
//	        List<Future<File>> createdFiles = new ArrayList<>();
//	        for (String sizeString : sizes) {
//	            int size = Integer.parseInt(sizeString);
//	            final Dimension dim = new Dimension();
//	            dim.setSize(size, size);
//	            final String filename = outputFilePath + outputFileBasename + "_" + size + "." + outputFileSuffix;
//	            createdFiles.add(executor.submit(new Callable<File>() {
//
//	                @Override
//	                public File call() throws Exception {
//	                    return scaleToSize(im, dim, filename, false);
//	                }
//	            }));
//	        }
//	        while (!oneImageFinished(createdFiles)) {
//
//	        }
//	        log.debug("First image finished generation");
//	        return originalImageSize;
//
//	    }

//	    @SuppressWarnings("unused")
//	    private boolean allImagesFinished(List<Future<File>> createdFiles) {
//	        for (Future<File> future : createdFiles) {
//	            try {
//	                if (!future.isDone() || future.get() == null) {
//	                    return false;
//	                }
//	            } catch (InterruptedException | ExecutionException e) {
//	            }
//	        }
//	        return true;
//	    }
	    
//	    private boolean oneImageFinished(List<Future<File>> createdFiles) {
//	        for (Future<File> future : createdFiles) {
//	            try {
//	                if (future.isDone() && future.get() != null) {
//	                    return true;
//	                }
//	            } catch (InterruptedException | ExecutionException e) {
//	            }
//	        }
//	        return false;
//	    }

//	    private File scaleToSize(ImageManager im, Dimension dim, String filename, boolean overwrite) throws ImageManipulatorException,
//	            FileNotFoundException, ImageManagerException, IOException, ContentLibException {
//	        File outputFile = new File(filename);
//	        if (!overwrite && outputFile.isFile()) {
//	            return outputFile;
//	        }
//	        try (FileOutputStream outputFileStream = new FileOutputStream(outputFile)) {
//	            RenderedImage ri = im.scaleImageByPixel(dim, ImageManager.SCALE_TO_BOX, 0);
//	            try (JpegInterpreter pi = new JpegInterpreter(ri)) {
//	                pi.writeToStream(null, outputFileStream);
//	                outputFileStream.close();
//	                log.debug("Written file " + outputFile);
//	                return outputFile;
//	            }
//	        }
//	    }
}
