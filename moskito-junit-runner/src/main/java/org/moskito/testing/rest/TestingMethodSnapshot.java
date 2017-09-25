package org.moskito.testing.rest;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Represents test methods data
 */
public class TestingMethodSnapshot {

    /**
     * Name of method
     */
    private String name;
    /**
     * List of producers used in method
     */
    private List<TestingProducerSnapshot> producers;

    @JacksonXmlElementWrapper(localName = "producers")
    @JacksonXmlProperty(localName = "producer")
    public List<TestingProducerSnapshot> getProducers() {
        return producers;
    }

    public void setProducers(List<TestingProducerSnapshot> producers) {
        this.producers = producers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString(){
        return "TestingMethodSnapshot [" +
                    "name= " + name + ", " +
                    "producers= [" + StringUtils.join(producers.toArray(), ", ") + "]" +
                "]";
    }

}