package org.moskito.testing.rest;

/**
 * Producer stats object.
 *
 * @author esmakula
 */
public class ProducerStats {

	private String producer;

	private String method;

	private String totalRequests;

	private String totalErrors;

	private String averageDuration;

	private String averageErrors;

	public String getProducer() {
		return producer;
	}

	public void setProducer(String producer) {
		this.producer = producer;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getTotalRequests() {
		return totalRequests;
	}

	public void setTotalRequests(String totalRequests) {
		this.totalRequests = totalRequests;
	}

	public String getTotalErrors() {
		return totalErrors;
	}

	public void setTotalErrors(String totalErrors) {
		this.totalErrors = totalErrors;
	}

	public String getAverageDuration() {
		return averageDuration;
	}

	public void setAverageDuration(String averageDuration) {
		this.averageDuration = averageDuration;
	}

	public String getAverageErrors() {
		return averageErrors;
	}

	public void setAverageErrors(String averageErrors) {
		this.averageErrors = averageErrors;
	}

	@Override
	public String toString() {
		return "ProducerStats{" +
				"producer='" + producer + '\'' +
				", method='" + method + '\'' +
				", totalRequests='" + totalRequests + '\'' +
				", totalErrors='" + totalErrors + '\'' +
				", averageDuration='" + averageDuration + '\'' +
				", averageErrors='" + averageErrors + '\'' +
				'}';
	}
}
