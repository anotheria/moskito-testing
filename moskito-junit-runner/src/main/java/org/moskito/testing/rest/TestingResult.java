package org.moskito.testing.rest;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "result")
public class TestingResult {

    private TestingSnapshot snapshot;

    @JacksonXmlProperty(localName = "test")
    public TestingSnapshot getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(TestingSnapshot snapshot) {
        this.snapshot = snapshot;
    }

}
