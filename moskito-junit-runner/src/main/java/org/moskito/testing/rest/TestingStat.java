package org.moskito.testing.rest;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.Map;

public class TestingStat {

    private String name;

    private Map<String, String> values;

    @JacksonXmlElementWrapper(localName = "values")
    @JacksonXmlProperty(localName = "value")
    public Map<String, String> getValues() {
        return values;
    }

    public void setValues(Map<String, String> values) {
        this.values = values;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}