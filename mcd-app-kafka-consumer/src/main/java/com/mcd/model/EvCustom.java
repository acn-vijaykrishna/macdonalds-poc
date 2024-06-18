package com.mcd.model;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlAttribute;
@XmlRootElement(name = "Ev_Custom")
public class EvCustom {
    // This class can have its own fields if needed
    private String info;

    @XmlAttribute(name = "Info")
    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
