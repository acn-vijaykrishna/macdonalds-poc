package com.mcd.model;

import jakarta.xml.bind.annotation.XmlAttribute;



public class Info {

    private String code;
    private String data;

    @XmlAttribute(name = "code")
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @XmlAttribute(name = "data")
    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
