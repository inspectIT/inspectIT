package info.novatec.inspectit.indexing.aggregation.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import info.novatec.inspectit.communication.data.HttpTimerData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.testng.annotations.Test;

/**
 * Tests the buffer aggregation of <code>HttpTimerData</code> elements.
 * 
 * @author Stefan Siegl
 */
@SuppressWarnings("PMD")
public class HttpDataAggregatorTest {

	@Test
	public void aggregationWithInspectITHeaderTwoDifferent() {
		final HttpTimerData data = new HttpTimerData();
		Map<String, String> map1 = new HashMap<String, String>();
		MapUtils.putAll(map1, new String[][] { { "inspectit", "tag1" } });
		data.setHeaders(map1);
		data.setUri("URI");
		data.setRequestMethod("GET");

		final HttpTimerData data2 = new HttpTimerData();
		Map<String, String> map2 = new HashMap<String, String>();
		MapUtils.putAll(map2, new String[][] { { "inspectit", "tag2" } });
		data2.setHeaders(map2);
		data2.setUri("URI");
		data2.setRequestMethod("GET");

		final List<HttpTimerData> input = new ArrayList<HttpTimerData>();
		Collections.addAll(input, data, data2);

		AggregationPerformer<HttpTimerData> aggregationPerformer = new AggregationPerformer<HttpTimerData>(new HttpTimerDataAggregator(false, false));
		aggregationPerformer.processCollection(input);
		final List<HttpTimerData> output = aggregationPerformer.getResultList();
		assertThat(output, is(notNullValue()));
		assertThat(output.size(), is(equalTo(2)));
	}

	@Test
	public void aggregationWithInspectITHeader() {
		final HttpTimerData data = new HttpTimerData();
		Map<String, String> map = new HashMap<String, String>();
		MapUtils.putAll(map, new String[][] { { "inspectit", "tag1" } });
		data.setHeaders(map);

		data.setUri("URI");
		data.setRequestMethod("GET");
		data.setDuration(100d);
		data.setCpuDuration(10d);
		data.calculateCpuMin(10d);
		data.calculateCpuMax(10d);
		data.setCpuDuration(10d);

		final HttpTimerData data2 = new HttpTimerData();
		data2.setHeaders(new HashMap<String, String>() {
			private static final long serialVersionUID = 6328525502662081826L;

			{
				put("inspectit", "tag1");
			}
		});
		data2.setUri("URI");
		data2.setRequestMethod("GET");
		data2.setDuration(500d);
		data2.calculateCpuMin(20d);
		data2.calculateCpuMax(20d);
		data2.setCpuDuration(20d);

		final List<HttpTimerData> input = new ArrayList<HttpTimerData>();
		Collections.addAll(input, data, data2);

		AggregationPerformer<HttpTimerData> aggregationPerformer = new AggregationPerformer<HttpTimerData>(new HttpTimerDataAggregator(false, false));
		aggregationPerformer.processCollection(input);
		final List<HttpTimerData> output = aggregationPerformer.getResultList();

		assertThat(output, is(notNullValue()));
		assertThat(output.size(), is(equalTo(1)));
		HttpTimerData result = output.get(0);
		assertThat(result.getUri(), is(equalTo(HttpTimerData.UNDEFINED)));
		assertThat(result.hasInspectItTaggingHeader(), is(equalTo(true)));
		assertThat(result.getInspectItTaggingHeaderValue(), is(equalTo("tag1")));
		assertThat(result.getDuration(), is(equalTo(600d)));
		assertThat(result.getCpuDuration(), is(equalTo(30d)));
		assertThat(result.getAttributes(), is(nullValue()));
		assertThat(result.getParameters(), is(nullValue()));
	}

	@Test
	public void aggregationURI() {
		final HttpTimerData data = new HttpTimerData();
		data.setUri("URI");
		data.setRequestMethod("GET");
		data.setDuration(100d);
		data.setCpuDuration(10d);
		data.calculateCpuMin(10d);
		data.calculateCpuMax(10d);
		data.setCpuDuration(10d);

		final HttpTimerData data2 = new HttpTimerData();
		data2.setUri("URI");
		data2.setRequestMethod("POST");
		data2.setDuration(500d);
		data2.calculateCpuMin(20d);
		data2.calculateCpuMax(20d);
		data2.setCpuDuration(20d);

		final List<HttpTimerData> input = new ArrayList<HttpTimerData>();
		Collections.addAll(input, data, data2);

		AggregationPerformer<HttpTimerData> aggregationPerformer = new AggregationPerformer<HttpTimerData>(new HttpTimerDataAggregator(true, false));
		aggregationPerformer.processCollection(input);
		final List<HttpTimerData> output = aggregationPerformer.getResultList();

		assertThat(output, is(notNullValue()));
		assertThat(output.size(), is(equalTo(1)));
		HttpTimerData result = output.get(0);
		assertThat(result.getUri(), is(equalTo("URI")));
		assertThat(result.hasInspectItTaggingHeader(), is(equalTo(false)));
		assertThat(result.getDuration(), is(equalTo(600d)));
		assertThat(result.getCpuDuration(), is(equalTo(30d)));
	}

	@Test
	public void aggregationURIRequestMethods() {
		final HttpTimerData data = new HttpTimerData();
		data.setUri("URI");
		data.setRequestMethod("GET");
		data.setDuration(100d);
		data.setCpuDuration(10d);
		data.calculateCpuMin(10d);
		data.calculateCpuMax(10d);
		data.setCpuDuration(10d);

		final HttpTimerData data2 = new HttpTimerData();
		data2.setUri("URI");
		data2.setRequestMethod("POST");
		data2.setDuration(500d);
		data2.calculateCpuMin(20d);
		data2.calculateCpuMax(20d);
		data2.setCpuDuration(20d);

		final List<HttpTimerData> input = new ArrayList<HttpTimerData>();
		Collections.addAll(input, data, data2);
		AggregationPerformer<HttpTimerData> aggregationPerformer = new AggregationPerformer<HttpTimerData>(new HttpTimerDataAggregator(true, true));
		aggregationPerformer.processCollection(input);
		final List<HttpTimerData> output = aggregationPerformer.getResultList();

		assertThat(output, is(notNullValue()));
		assertThat(output.size(), is(equalTo(2)));
	}

}
