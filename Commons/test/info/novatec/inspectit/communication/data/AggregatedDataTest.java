package info.novatec.inspectit.communication.data;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.IAggregatedData;
import info.novatec.inspectit.communication.IIdsAwareAggregatedData;

import java.util.ArrayList;
import java.util.List;

import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@SuppressWarnings("PMD")
public class AggregatedDataTest {

	/**
	 * Tests the ID aggregation with in {@link IAggregatedData}.
	 * 
	 * @param aggregated
	 *            {@link IAggregatedData}
	 * @param data
	 *            Data to be aggregated.
	 */
	@SuppressWarnings("unchecked")
	@Test(dataProvider = "normalDataProvider")
	public <E extends DefaultData> void idAggregation(IIdsAwareAggregatedData<E> aggregated, E data) {
		aggregated.aggregate(data);
		if (data instanceof IIdsAwareAggregatedData) {
			for (long id : ((IIdsAwareAggregatedData<E>) data).getAggregatedIds()) {
				assertThat(aggregated.getAggregatedIds(), hasItem(id));
			}
		} else {
			assertThat(aggregated.getAggregatedIds(), hasItem(data.getId()));
		}
	}

	@DataProvider(name = "normalDataProvider")
	public Object[][] getDataForAggregationOfNormalData() {
		Object[][] data = new Object[8][2];

		// first normal aggregation
		TimerData timerData = new TimerData();
		timerData.setId(1);
		data[0][0] = new AggregatedTimerData();
		data[0][1] = timerData;

		SqlStatementData sqlStatementData = new SqlStatementData();
		sqlStatementData.setId(1);
		data[1][0] = new AggregatedSqlStatementData();
		data[1][1] = sqlStatementData;

		HttpTimerData httpTimerData = new HttpTimerData();
		httpTimerData.setId(1);
		data[2][0] = new AggregatedHttpTimerData();
		data[2][1] = httpTimerData;

		ExceptionSensorData exceptionSensorData = new ExceptionSensorData();
		exceptionSensorData.setId(1);
		data[3][0] = new AggregatedExceptionSensorData();
		data[3][1] = exceptionSensorData;

		// then aggregation of already aggregated
		List<Long> aggregateId = new ArrayList<Long>();
		aggregateId.add(1L);
		aggregateId.add(3L);

		AggregatedTimerData aggregatedTimerData = Mockito.mock(AggregatedTimerData.class);
		Mockito.when(aggregatedTimerData.getAggregatedIds()).thenReturn(aggregateId);
		data[4][0] = new AggregatedTimerData();
		data[4][1] = aggregatedTimerData;

		AggregatedSqlStatementData aggregatedSqlStatementData = Mockito.mock(AggregatedSqlStatementData.class);
		Mockito.when(aggregatedSqlStatementData.getAggregatedIds()).thenReturn(aggregateId);
		data[5][0] = new AggregatedSqlStatementData();
		data[5][1] = aggregatedSqlStatementData;

		AggregatedHttpTimerData aggregatedHttpTimerData = Mockito.mock(AggregatedHttpTimerData.class);
		Mockito.when(aggregatedHttpTimerData.getAggregatedIds()).thenReturn(aggregateId);
		data[6][0] = new AggregatedHttpTimerData();
		data[6][1] = aggregatedHttpTimerData;

		AggregatedExceptionSensorData aggregatedExceptionSensorData = Mockito.mock(AggregatedExceptionSensorData.class);
		Mockito.when(aggregatedExceptionSensorData.getAggregatedIds()).thenReturn(aggregateId);
		data[7][0] = new AggregatedExceptionSensorData();
		data[7][1] = aggregatedExceptionSensorData;

		return data;
	}
}
