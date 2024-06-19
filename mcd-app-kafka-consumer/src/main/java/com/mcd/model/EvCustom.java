package com.mcd.model;

import jakarta.xml.bind.annotation.XmlElement;

public class EvCustom {

    private Info info;

    @XmlElement(name = "Info")
    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }
}
