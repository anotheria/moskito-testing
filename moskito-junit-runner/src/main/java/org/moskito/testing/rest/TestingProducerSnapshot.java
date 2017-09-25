package org.moskito.testing.rest;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents producer statistics from test run snapshot
 */
public class TestingProducerSnapshot {

    /**
     * Id of producer
     */
    private String producerId;
    /**
     * List of producer stats
     */
    private List<TestingStat> stats = new ArrayList<>();

    public String getProducerId() {
        return producerId;
    }

    public void setProducerId(String producerId) {
        this.producerId = producerId;
    }

    public List<TestingStat> getStats() {
        return stats;
    }

    public void setStats(List<TestingStat> stats) {
        this.stats = stats;
    }

    public String toString(){
        return "TestingMethodSnapshot [" +
                "producerId= " + producerId + ", " +
                "producers= [" + StringUtils.join(stats.toArray(), ", ") + "]" +
                "]";
    }

}