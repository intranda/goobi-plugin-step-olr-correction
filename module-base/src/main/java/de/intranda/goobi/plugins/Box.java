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

import org.jdom2.Element;

import lombok.Data;

@Data
public class Box {
    private double x;
    private double y;
    private double width;
    private double height;
    private String type;

    public Box(Element xmlBox) {
        this.x = Double.parseDouble(xmlBox.getAttributeValue("x"));
        this.y = Double.parseDouble(xmlBox.getAttributeValue("y"));
        this.width = Double.parseDouble(xmlBox.getAttributeValue("width"));
        this.height = Double.parseDouble(xmlBox.getAttributeValue("height"));
        this.type = xmlBox.getAttributeValue("type");
    }
}
