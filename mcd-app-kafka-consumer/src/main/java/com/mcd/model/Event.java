package com.mcd.model;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Event")
public class Event {

    private String regId;
    private String type;
    private String time;
    private EvCustom evCustom;

    @XmlAttribute(name = "RegId")
    public String getRegId() {
        return regId;
    }

    public void setRegId(String regId) {
        this.regId = regId;
    }

    @XmlAttribute(name = "Type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @XmlAttribute(name = "Time")
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @XmlElement(name = "Ev_Custom")
    public EvCustom getEvCustom() {
        return evCustom;
    }

    public void setEvCustom(EvCustom evCustom) {
        this.evCustom = evCustom;
    }
}
