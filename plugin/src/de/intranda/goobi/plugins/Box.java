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
