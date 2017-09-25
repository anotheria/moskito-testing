package org.moskito.testing.rest;

import java.util.Map;

/**
 * Wrapper for results map.
 *
 * @author esmakula
 */
public class ResultsWrapper {

	private Map<String, String> stats;

	public Map<String, String> getStats() {
		return stats;
	}

	public void setStats(Map<String, String> stats) {
		this.stats = stats;
	}

	@Override
	public String toString() {
		return "ResultsWrapper{" +
				"stats='" + stats + '\'' +
				'}';
	}
}
