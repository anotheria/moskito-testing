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

	public MoskitoJunitRunner(Class<?> klass) throws InitializationError {
		super(klass);
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			doc = docBuilder.newDocument();
			rootElement = doc.createElement("result");
			doc.appendChild(rootElement);
		} catch (ParserConfigurationException e) {
			logger.error("Failed to initialize MoskitoJunitRunner", e);
		}
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
		Element testElement = doc.createElement("test");
		rootElement.appendChild(testElement);
		Element name = doc.createElement("name");
		testElement.appendChild(name);
		name.appendChild(doc.createTextNode(getName()));
		currentElement = doc.createElement("methods");
		testElement.appendChild(currentElement);
		super.run(notifier);
		try {
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File("testresult_" + getName().replace(".", "_") + ".xml"));
			transformer.transform(source, result);
		} catch (TransformerException e) {
			logger.error("Failed to save data", e);
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
		createXML(method, serviceProducers);
	}

	private void createXML(String method, List<IStatsProducer> producers) {

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
			}

		}
	}
}
