package org.moskito.testing.rest;

/**
 *  Producer stats parameter object.
 *
 * @author esmakula
 */
public class ProducerStatsPO {

	private String producer;

	private String method;

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

	@Override
	public String toString() {
		return "ProducerStatsPO{" +
				"producer='" + producer + '\'' +
				", method='" + method + '\'' +
				'}';
	}
}
