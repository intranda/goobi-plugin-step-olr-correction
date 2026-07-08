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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.goobi.beans.Process;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import de.sub.goobi.helper.FacesContextHelper;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TocImageHelperTest {

    @Mock
    private FacesContext facesContext;
    @Mock
    private ExternalContext externalContext;
    @Mock
    private HttpServletRequest servletRequest;
    @Mock
    private Process process;

    private TocImageHelper tih;

    @BeforeEach
    public void setUp() {
        FacesContextHelper.setFacesContext(facesContext);
        Mockito.when(facesContext.getExternalContext()).thenReturn(externalContext);
        Mockito.when(externalContext.getRequest()).thenReturn(servletRequest);
        Mockito.when(servletRequest.getScheme()).thenReturn("https");
        Mockito.when(servletRequest.getServerName()).thenReturn("localhost");
        Mockito.when(servletRequest.getServerPort()).thenReturn(443);
        Mockito.when(servletRequest.getContextPath()).thenReturn("/goobi");

        Mockito.when(process.getId()).thenReturn(1);

        tih = new TocImageHelper();
        tih.setProcess(process);
        tih.setImageFolderName("/opt/digiverso/goobi/metadata/1/images/testprocess_media");
    }

    @AfterEach
    public void tearDown() {
        FacesContextHelper.reset();
    }

    @Test
    public void testImageEndpoint() {
        Image image = new Image("00000001.tif", 1, "", "00000001.tif", "");

        tih.setImage(image);

        assertEquals("https://localhost:443/goobi/api/process/image/1/testprocess_media/00000001.tif/info.json", image.getImageUrl());
    }

    @Test
    public void testSetImageRescaleCoordinates() {
        Image image = new Image("00000001.tif", 1, "", "00000001.tif", "");

        tih.setImage(image);

        assertEquals(1f, image.getScale(), 0f);
    }

    @Test
    public void testSetImageStoresCurrentImage() {
        Image image = new Image("00000001.tif", 1, "", "00000001.tif", "");

        tih.setImage(image);

        assertEquals(image, tih.getImage());
    }
}
