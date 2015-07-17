package info.novatec.inspectit.agent.buffer.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import info.novatec.inspectit.communication.MethodSensorData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("PMD")
public class SizeBufferStrategyTest {

	private SizeBufferStrategy bufferStrategy;

	@BeforeMethod
	public void initTestClass() {
		bufferStrategy = new SizeBufferStrategy();
		bufferStrategy.log = LoggerFactory.getLogger(SizeBufferStrategy.class);
	}

	@Test
	public void addAndRetrieve() {
		bufferStrategy.addMeasurements(Collections.<MethodSensorData> emptyList());

		assertThat(bufferStrategy.hasNext(), is(true));
		List<MethodSensorData> list = bufferStrategy.next();
		assertThat(list, is(notNullValue()));
		assertThat(list, is(equalTo(Collections.<MethodSensorData> emptyList())));

		assertThat(bufferStrategy.hasNext(), is(false));
	}

	@Test
	public void emptyBuffer() {
		assertThat(bufferStrategy.hasNext(), is(false));
	}

	@Test(expectedExceptions = { NoSuchElementException.class })
	public void noSuchElementException() {
		bufferStrategy.next();
	}

	@Test(expectedExceptions = { IllegalArgumentException.class })
	public void addNullMeasurement() {
		bufferStrategy.addMeasurements(null);
	}

	@Test(expectedExceptions = { NoSuchElementException.class })
	public void exceptionAfterDoubleRetrieve() {
		bufferStrategy.addMeasurements(Collections.<MethodSensorData> emptyList());
		bufferStrategy.next();
		bufferStrategy.next();
	}

	@Test
	public void callInit() {
		Map<String, String> settings = new HashMap<String, String>();
		settings.put("size", "3");
		bufferStrategy.init(Collections.<String, String> emptyMap());
	}

	@Test
	public void addElementFullStack() {
		Map<String, String> settings = new HashMap<String, String>();
		settings.put("size", "3");
		bufferStrategy.init(settings);

		List<MethodSensorData> listOne = new ArrayList<MethodSensorData>(0);
		List<MethodSensorData> listTwo = new ArrayList<MethodSensorData>(0);
		List<MethodSensorData> listThree = new ArrayList<MethodSensorData>(0);
		List<MethodSensorData> listFour = new ArrayList<MethodSensorData>(0);
		List<MethodSensorData> listFive = new ArrayList<MethodSensorData>(0);

		bufferStrategy.addMeasurements(listOne);
		bufferStrategy.addMeasurements(listTwo);
		bufferStrategy.addMeasurements(listThree);
		bufferStrategy.addMeasurements(listFour);
		bufferStrategy.addMeasurements(listFive);

		assertThat(bufferStrategy.next(), is(equalTo(listFive)));
		assertThat(bufferStrategy.next(), is(equalTo(listFour)));
		assertThat(bufferStrategy.next(), is(equalTo(listThree)));

		assertThat(bufferStrategy.hasNext(), is(false));
	}

}
