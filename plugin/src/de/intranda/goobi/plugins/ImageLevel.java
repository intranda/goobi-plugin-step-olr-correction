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

import java.awt.Dimension;

/**
 * @author florian
 *
 */
public class ImageLevel implements Comparable<ImageLevel> {

    private String url;
    private Dimension size;
    private int rotation;

    public ImageLevel(String url, Dimension size) {
        super();
        this.url = url;
        this.size = size;
        this.rotation = 0;
    }

    public ImageLevel(String url, int width, int height) {
        super();
        this.url = url;
        this.size = new Dimension(width, height);
        this.rotation = 0;
    }

    public ImageLevel(String url, Dimension size, int currentRotate) {
        super();
        this.url = url;
        this.size = size;
        this.rotation = currentRotate;
    }

    public String getUrl() {
        return url;
    }

    public Dimension getSize() {
        if (rotation % 180 == 0) {
            return size;
        } else {
            return new Dimension(size.height, size.width);
        }
    }

    public int getWidth() {
        return rotation % 180 == 90 ? size.height : size.width;
    }

    public int getHeight() {
        return rotation % 180 == 90 ? size.width : size.height;
    }

    @Override
    public String toString() {
        return "[\"" + url + "\"," + getWidth() + "," + getHeight() + "]";
    }

    @Override
    public int compareTo(ImageLevel other) {
        return Integer.compare(size.width * size.height, other.size.width * other.size.height);
    }

}
