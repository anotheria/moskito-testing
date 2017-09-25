package org.moskito.testing.rest;

/**
 * Response object from analyze.
 *
 * @author esmakula.
 */
public class Response {

	/**
	 * True if the call was successful.
	 */
	private boolean success;
	/**
	 * Optional message in case call was failed (exception message).
	 */
	private String message;

	/**
	 * Results wrapper object.
	 */
	private ResultsWrapper results;

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder("ReplyObject ");
		ret.append("Success: ").append(success);
		if (message != null) {
			ret.append(", Message: ").append(message);
		}
		ret.append(", Results: ").append(results);
		return ret.toString();
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public ResultsWrapper getResults() {
		return results;
	}

}