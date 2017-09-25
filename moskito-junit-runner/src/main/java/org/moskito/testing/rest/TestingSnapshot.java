package org.moskito.testing.rest;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.commons.lang.StringUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents test run snapshot.
 */
public class TestingSnapshot {

    /**
     * Test class name
     */
    private String name;
    /**
     * Timestamp, when this snapshot arrived
     */
    private long testRunStartTimestamp;
    /**
     * Test methods
     */
    private List<TestingMethodSnapshot> methods = new LinkedList<>();

    @JacksonXmlElementWrapper(localName = "methods")
    @JacksonXmlProperty(localName = "method")
    public List<TestingMethodSnapshot> getMethods() {
        return methods;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTestRunStartTimestamp() {
        return testRunStartTimestamp;
    }

    public void setTestRunStartTimestamp(long testRunStartTimestamp) {
        this.testRunStartTimestamp = testRunStartTimestamp;
    }

    public void addMethod(TestingMethodSnapshot method){
        methods.add(method);
    }

    public String toString(){
        return "TestingSnapshot [" +
                    "testName=" + name + ", " +
                    "testRunStartTimestamp=" + testRunStartTimestamp + ", " +
                    "methods=[" + StringUtils.join(methods.toArray(), ", ") + "]" +
                "]";
    }

}