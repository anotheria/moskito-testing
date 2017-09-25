package org.moskito.testing.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.json.JSONConfiguration;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.configureme.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Map;

/**
 * REST connector implementation.
 *
 * @author esmakula
 */
public class RESTConnector {

	/**
	 * Logger instance.
	 */
	private final static Logger log = LoggerFactory.getLogger(RESTConnector.class);

	/**
	 * Http prefix.
	 */
	private final static String HTTP_PREFIX = "http://";

	/**
	 * Connector config instance.
	 */
	private static RESTConnectorConfig connectorConfig;

	private static final Gson gson = new GsonBuilder().create();

	/**
	 * Cached client instance.
	 */
	private Client client;

	private static RESTConnector INSTANCE;

	/**
	 * Default constructor.
	 */
	private RESTConnector() {
		setConfigurationName("analyze-connector");
	}

	public static RESTConnector getInstance() {
		if (INSTANCE == null) {
			synchronized (RESTConnector.class) {
				if (INSTANCE == null) {
					INSTANCE = new RESTConnector();
				}
			}
		}
		return INSTANCE;
	}

	private void setConfigurationName(String configurationName) {
		connectorConfig = new RESTConnectorConfig();
		ConfigurationManager.INSTANCE.configureAs(connectorConfig, configurationName);
		log.debug("Config: " + connectorConfig);
		client = getClient();
	}

	public Map<String, String> getJourneyCallsData(String producer, String method) {
		ProducerStatsPO journeyCallsPO = new ProducerStatsPO();
		journeyCallsPO.setProducer(producer);
		journeyCallsPO.setMethod(method);
		WebResource resource = client.resource(getBaseURI(connectorConfig.getJourney()));
		Response response = resource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).post(Response.class, gson.toJson(journeyCallsPO));

		return response.getResults().getStats();
	}

	private static Client getClient() {
		Client client = Client.create(getClientConfig());
		if (connectorConfig.isBasicAuthEnabled()) {
			/* adding HTTP basic auth header to request */
			client.addFilter(new HTTPBasicAuthFilter(connectorConfig.getLogin(), connectorConfig.getPassword()));
		}
		return client;
	}

	private static ClientConfig getClientConfig() {
		ClientConfig clientConfig = new DefaultClientConfig();
		clientConfig.getClasses().add(JacksonJaxbJsonProvider.class);
		clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
		return clientConfig;
	}

	private static URI getBaseURI(String context) {
		return UriBuilder.fromUri(HTTP_PREFIX + connectorConfig.getHost() + connectorConfig.getResourcePath() + context).port(connectorConfig.getPort()).build();
	}

	protected RESTConnectorConfig getConnectorConfig() {
		return connectorConfig;
	}

	public static void main(String[] args) {
		Map<String, String> response = INSTANCE.getJourneyCallsData("OrderController", "order");
		System.out.println("Output from Server .... ");
		System.out.println(response + "\n");
	}

}
