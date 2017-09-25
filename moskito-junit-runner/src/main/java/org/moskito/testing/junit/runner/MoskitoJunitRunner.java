package org.moskito.testing.junit.runner;

import net.anotheria.moskito.core.predefined.ServiceStats;
import net.anotheria.moskito.core.producers.IStatsProducer;
import net.anotheria.moskito.core.registry.ProducerRegistryFactory;
import net.anotheria.moskito.core.stats.Interval;
import net.anotheria.moskito.core.stats.TimeUnit;
import net.anotheria.moskito.core.stats.impl.IntervalRegistry;
import net.anotheria.moskito.core.timing.IUpdateable;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.moskito.testing.rest.RESTConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Moskito Junit Runner based on {@link BlockJUnit4ClassRunner}.
 * Resets snapshot interval before/after each test(method).
 * Results saved to xml file for each unit test.
 *
 * @author esmakula
 */
public class MoskitoJunitRunner extends BlockJUnit4ClassRunner {

	private Logger logger = LoggerFactory.getLogger(MoskitoJunitRunner.class);

	private Document doc;
	private Element rootElement;
	private Element currentElement;
	private Document docExtended;
	private Element rootElementExtended;
	private Element currentElementExtended;

	public MoskitoJunitRunner(Class<?> klass) throws InitializationError {
		super(klass);
		createXmlRootElements();
	}


	@Override
	protected void runChild(FrameworkMethod method, RunNotifier notifier) {
		resetInterval();
		super.runChild(method, notifier);
		resetInterval();
		createChildResults(method.getName());
	}

	@Override
	public void run(RunNotifier notifier) {
		appendXmlTestMethodsElement(false);
		appendXmlTestMethodsElement(true);
		super.run(notifier);
		writeXml();
	}

	private void appendXmlTestMethodsElement(boolean extended) {
		Document doc = getDoc(extended);
		Element rootElement = getRootElement(extended);
		Element testElement = doc.createElement("test");
		rootElement.appendChild(testElement);
		Element name = doc.createElement("name");
		testElement.appendChild(name);
		name.appendChild(doc.createTextNode(getName()));
		if (extended) {
			currentElementExtended = doc.createElement("methods");
			testElement.appendChild(currentElementExtended);
			return;
		}
		currentElement = doc.createElement("methods");
		testElement.appendChild(currentElement);

	}

	private void writeXml() {
		try {
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			String name = getName().replace(".", "_");
			transformXml(transformer, doc, "testresult_" + name);
			transformXml(transformer, docExtended, "testresult_ext_" + name);
		} catch (TransformerException e) {
			logger.error("Failed to save data", e);
		}
	}

	private void transformXml(Transformer transformer, Document doc, String fileNamePrefix) throws TransformerException {
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(fileNamePrefix + ".xml"));
			transformer.transform(source, result);
	}

	private void createXmlRootElements() {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			doc = docBuilder.newDocument();
			rootElement = doc.createElement("result");
			doc.appendChild(rootElement);

			docExtended = docBuilder.newDocument();
			rootElementExtended = docExtended.createElement("result");
			docExtended.appendChild(rootElementExtended);

		} catch (ParserConfigurationException e) {
			logger.error("Failed to initialize MoskitoJunitRunner", e);
		}
	}

	private void resetInterval() {
		IntervalRegistry registry = IntervalRegistry.getInstance();
		Interval interval = registry.getInterval("snapshot");
		((IUpdateable) interval).update();
	}

	private void createChildResults(String method) {
		Collection<IStatsProducer> producers = ProducerRegistryFactory.getProducerRegistryInstance().getProducers();
		List<IStatsProducer> serviceProducers = new ArrayList<>();
		for (IStatsProducer producer : producers) {
			if (producer.getStats().size() == 0 || !ServiceStats.class.equals(producer.getStats().get(0).getClass())) {
				continue;
			}
			serviceProducers.add(producer);

		}
		createXML(method, serviceProducers, false);
		createXML(method, serviceProducers, true);
	}

	private void createXML(String method, List<IStatsProducer> producers, boolean extended) {
		Document doc = getDoc(extended);
		Element currentElement = getCurrentElement(extended);
		Element methodElement = doc.createElement("method");
		currentElement.appendChild(methodElement);
		Element methodNameElement = doc.createElement("name");
		methodElement.appendChild(methodNameElement);
		methodNameElement.appendChild(doc.createTextNode(method));
		Element producersElement = doc.createElement("producers");
		methodElement.appendChild(producersElement);
		for (IStatsProducer producer : producers) {
			@SuppressWarnings("unchecked")
			List<ServiceStats> stats = producer.getStats();

			Element producerElement = doc.createElement("producer");
			producersElement.appendChild(producerElement);
			Element producerNameElement = doc.createElement("name");
			producerNameElement.appendChild(doc.createTextNode(producer.getProducerId()));
			producerElement.appendChild(producerNameElement);
			Element statsElement = doc.createElement("stats");
			producerElement.appendChild(statsElement);
			for (ServiceStats stat : stats) {
				Element statElement = doc.createElement("stat");
				statsElement.appendChild(statElement);
				Element statName = doc.createElement("name");
				statElement.appendChild(statName);
				statName.appendChild(doc.createTextNode(stat.getName()));
				Element values = doc.createElement("values");
				statElement.appendChild(values);
				for (String valueName : stat.getAvailableValueNames()) {
					Element valueElement = doc.createElement("value");
					values.appendChild(valueElement);
					String statValue = stat.getValueByNameAsString(valueName, "snapshot", TimeUnit.MILLISECONDS);
					Element valueNameElement = doc.createElement("name");
					valueNameElement.appendChild(doc.createTextNode(valueName));
					valueElement.appendChild(valueNameElement);
					Element statValueElement = doc.createElement("statValue");
					statValueElement.appendChild(doc.createTextNode(statValue));
					valueElement.appendChild(statValueElement);
				}

				if (extended && stat.getName() != "cumulated") {
					Map<String, String> results = RESTConnector.getInstance().getJourneyCallsData(producer.getProducerId(), stat.getName());
					for (Map.Entry<String, String> entry : results.entrySet()) {
						if (entry.getKey().equals("producer") || entry.getKey().equals("method"))
							continue;

						Element valueElement = doc.createElement("value");
						values.appendChild(valueElement);
						String statValue = entry.getValue();
						Element valueNameElement = doc.createElement("name");
						valueNameElement.appendChild(doc.createTextNode(entry.getKey()));
						valueElement.appendChild(valueNameElement);
						Element statValueElement = doc.createElement("statValue");
						statValueElement.appendChild(doc.createTextNode(statValue));
						valueElement.appendChild(statValueElement);
					}

				}
			}

		}
	}

	public Document getDoc(boolean extended) {
		if (extended)
			return docExtended;
		return doc;
	}

	public Element getRootElement(boolean extended) {
		if (extended)
			return rootElementExtended;
		return rootElement;
	}

	public Element getCurrentElement(boolean extended) {
		if (extended)
			return currentElementExtended;
		return currentElement;
	}
}
