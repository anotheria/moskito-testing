package org.moskito.testing.junit.runner;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
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
import org.moskito.testing.rest.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Moskito Junit Runner based on {@link BlockJUnit4ClassRunner}.
 * Resets snapshot interval before/after each test(method).
 * Results saved to xml file for each unit test.
 *
 * @author esmakula
 */
public class MoskitoJunitRunner extends BlockJUnit4ClassRunner {

	private Logger logger = LoggerFactory.getLogger(MoskitoJunitRunner.class);

	/**
	 * Current test snapshot object
	 */
	private TestingSnapshot testingSnapshot = new TestingSnapshot();

	/**
	 * Current test snapshot object with additional
	 * statistic from journey
	 */
	private TestingSnapshot testingSnapshotExtended = new TestingSnapshot();

	/**
	 * Mapper for converting testing snapshots to xml documents
	 */
	private static final XmlMapper xmlMapper;

	/**
	 * Stores timestamp of test suite run start
	 */
	private static final long testRunStartTimestamp;

	static {
		testRunStartTimestamp = System.currentTimeMillis();
		xmlMapper = new XmlMapper();
		xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
	}

	public MoskitoJunitRunner(Class<?> klass) throws InitializationError {
		super(klass);
	}

	@Override
	protected void runChild(FrameworkMethod method, RunNotifier notifier) {

		resetInterval();
		super.runChild(method, notifier);
		resetInterval();

		makeTestMethodSnapshot(method.getName(), testingSnapshot, false);
		makeTestMethodSnapshot(method.getName(), testingSnapshotExtended, true);

	}

	@Override
	public void run(RunNotifier notifier) {

		testingSnapshot.setName(getName());
		testingSnapshot.setTestRunStartTimestamp(testRunStartTimestamp);
		testingSnapshotExtended.setName(getName());
		testingSnapshot.setTestRunStartTimestamp(testRunStartTimestamp);

		super.run(notifier);

		writeSnapshotToFileAsXml(testingSnapshot, false);
		writeSnapshotToFileAsXml(testingSnapshotExtended, true);

		try {
			RESTConnector.getInstance().sendTestingSnapshot(testingSnapshot);
		} catch (UniformInterfaceException | ClientHandlerException e){
			logger.error("Failed to send test snapshot to moskito analyze.", e);
		}

	}

	/**
	 * Writes test run data to xml file.
	 * File will be saved to module build sources directory.
	 * Overwrites file with this test name if it exists.
	 *
	 * @param snapshot test snapshot object. Source of data for xml document
	 * @param extended is current snapshot extended. Extended snapshot xml files has special prefix
	 */
	private void writeSnapshotToFileAsXml(TestingSnapshot snapshot, boolean extended) {

		TestingResult result = new TestingResult();
		result.setSnapshot(snapshot);

		try {
			xmlMapper.writeValue(
					new File(
							"testresult_" + (extended ? "ext_" : "") +
									snapshot.getName().replace('.', '_') + ".xml"
					),
					result
			);
		} catch (IOException e) {
			logger.error("Failed to save testing snapshots.", e);
		}

	}

	/**
	 * Updates moskito metrics
	 */
	private void resetInterval() {
		IntervalRegistry registry = IntervalRegistry.getInstance();
		Interval interval = registry.getInterval("snapshot");
		((IUpdateable) interval).update();
	}

	/**
	 * Saves moskito statistics that been accumulated
	 * due test method run to test snapshot
	 *
	 * @param methodName name of current test method
	 * @param snapshot test snapshot object
	 * @param extended is current snapshot is extended
	 */
	private void makeTestMethodSnapshot(String methodName, TestingSnapshot snapshot, boolean extended) {

		Collection<IStatsProducer> producers = ProducerRegistryFactory.getProducerRegistryInstance().getProducers();
		TestingMethodSnapshot methodSnapshot = new TestingMethodSnapshot();
		methodSnapshot.setName(methodName);
		List<TestingProducerSnapshot> currentProducersSnapshot = new ArrayList<>();
		for (IStatsProducer producer : producers) {
			if (producer.getStats().size() == 0 || !ServiceStats.class.equals(producer.getStats().get(0).getClass())) {
				continue;
			}

			TestingProducerSnapshot producerSnapshot = new TestingProducerSnapshot();
			producerSnapshot.setProducerId(producer.getProducerId());
			List<TestingStat> currentProducerStats = new LinkedList<>();
			
			for(ServiceStats stat : ((List<ServiceStats>) producer.getStats())){

				TestingStat statSnapshot = new TestingStat();
				statSnapshot.setName(stat.getName());
				Map<String, String> statValues = new HashMap<>();

				for(String statValueName : stat.getAvailableValueNames()){
					statValues.put(statValueName,
							stat.getValueByNameAsString(statValueName, "snapshot", TimeUnit.MILLISECONDS)
					);
				}

				if(extended && !stat.getName().equals("cumulated")){

					try {

						Map<String, String> results = RESTConnector.getInstance().getJourneyCallsData(producer.getProducerId(), stat.getName());

						for (Map.Entry<String, String> entry : results.entrySet()) {
							if (!entry.getKey().equals("producer") && !entry.getKey().equals("method"))
								statValues.put(entry.getKey(), entry.getValue());
						}

					} catch (UniformInterfaceException | ClientHandlerException e){
						logger.error("Failed to get journey calls data from moskito analyze.", e);
					}

				}

				statSnapshot.setValues(statValues);
				currentProducerStats.add(statSnapshot);

			}

			producerSnapshot.setStats(currentProducerStats);
			currentProducersSnapshot.add(producerSnapshot);

		}

		methodSnapshot.setProducers(currentProducersSnapshot);
		snapshot.addMethod(methodSnapshot);

	}

}