package info.novatec.inspectit.indexing.indexer.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.MethodSensorData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.SqlStatementData;
import info.novatec.inspectit.indexing.IIndexQuery;
import info.novatec.inspectit.indexing.impl.IndexQuery;
import info.novatec.inspectit.indexing.storage.impl.StorageIndexQuery;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.testng.annotations.Test;

/**
 * Tests all available branch indexer classes.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("PMD")
public class BranchIndexersTest {

	/**
	 * Testing that the {@link InvocationChildrenIndexer} will return different keys based on if the
	 * invocation has children or not.
	 */
	@Test
	public void invocationChildIndexer() {
		// different keys for invocation with and without children
		InvocationSequenceData data1 = mock(InvocationSequenceData.class);
		when(data1.getNestedSequences()).thenReturn(Collections.<InvocationSequenceData> emptyList());

		InvocationSequenceData data2 = mock(InvocationSequenceData.class);
		when(data2.getNestedSequences()).thenReturn(Collections.singletonList(data1));

		InvocationChildrenIndexer<DefaultData> indexer = new InvocationChildrenIndexer<DefaultData>();

		Object key1 = indexer.getKey(data1);
		Object key2 = indexer.getKey(data2);

		assertThat(key1, is(not(key2)));

		StorageIndexQuery query = mock(StorageIndexQuery.class);
		when(query.isOnlyInvocationsWithoutChildren()).thenReturn(true).thenReturn(false);

		key1 = indexer.getKeys(query);
		key2 = indexer.getKeys(query);

		assertThat(key1, is(not(key2)));
	}

	/**
	 * Testing that the {@link MethodIdentIndexer} returns different keys based on the method ident.
	 */
	@Test
	public void methodIdentIndexer() {
		// different keys if method idents differ
		MethodSensorData data = mock(MethodSensorData.class);
		when(data.getMethodIdent()).thenReturn(10L).thenReturn(20L);

		MethodIdentIndexer<DefaultData> indexer = new MethodIdentIndexer<DefaultData>();

		Object key1 = indexer.getKey(data);
		Object key2 = indexer.getKey(data);

		assertThat(key1, is(not(key2)));

		IIndexQuery query = mock(IIndexQuery.class);

		when(query.getMethodIdent()).thenReturn(0L);
		assertThat(indexer.getKeys(query), is(emptyArray()));

		when(query.getMethodIdent()).thenReturn(10L);
		key1 = indexer.getKeys(query);
		when(query.getMethodIdent()).thenReturn(20L);
		key2 = indexer.getKeys(query);

		assertThat(key1, is(not(key2)));
	}

	/**
	 * Testing that the {@link PlatformIdentIndexer} returns different keys based on the platform
	 * ident.
	 */
	@Test
	public void platformIdentIndexer() {
		// different keys if platform idents differ
		DefaultData data = mock(DefaultData.class);

		PlatformIdentIndexer<DefaultData> indexer = new PlatformIdentIndexer<DefaultData>();

		when(data.getPlatformIdent()).thenReturn(10L);
		Object key1 = indexer.getKey(data);
		when(data.getPlatformIdent()).thenReturn(20L);
		Object key2 = indexer.getKey(data);

		assertThat(key1, is(not(key2)));

		IIndexQuery query = mock(IIndexQuery.class);

		when(query.getPlatformIdent()).thenReturn(0L);
		assertThat(indexer.getKeys(query), is(emptyArray()));

		when(query.getPlatformIdent()).thenReturn(10L);
		key1 = indexer.getKeys(query);
		when(query.getPlatformIdent()).thenReturn(20L);
		key2 = indexer.getKeys(query);

		assertThat(key1, is(not(key2)));
	}

	/**
	 * Testing that the {@link SensorTypeIdentIndexer} returns different keys based on the sensor
	 * type ident.
	 */
	@Test
	public void sensorTypeIdentIndexer() {
		// different keys if sensor type idents differ
		DefaultData data = mock(DefaultData.class);

		SensorTypeIdentIndexer<DefaultData> indexer = new SensorTypeIdentIndexer<DefaultData>();

		when(data.getSensorTypeIdent()).thenReturn(10L);
		Object key1 = indexer.getKey(data);
		when(data.getSensorTypeIdent()).thenReturn(20L);
		Object key2 = indexer.getKey(data);

		assertThat(key1, is(not(key2)));

		IIndexQuery query = mock(IIndexQuery.class);

		when(query.getSensorTypeIdent()).thenReturn(0L);
		assertThat(indexer.getKeys(query), is(emptyArray()));

		when(query.getSensorTypeIdent()).thenReturn(10L);
		key1 = indexer.getKeys(query);
		when(query.getSensorTypeIdent()).thenReturn(20L);
		key2 = indexer.getKeys(query);

		assertThat(key1, is(not(key2)));
	}

	/**
	 * Tests the {@link ObjectTypeIndexer} for different keys when different objects are indexed.
	 */
	@Test
	public void objectTypeIndexer() {
		DefaultData data1 = mock(InvocationSequenceData.class);
		DefaultData data2 = mock(SqlStatementData.class);

		ObjectTypeIndexer<DefaultData> indexer = new ObjectTypeIndexer<DefaultData>();

		Object key1 = indexer.getKey(data1);
		Object key2 = indexer.getKey(data2);

		assertThat(key1, is(not(key2)));

		IIndexQuery query = mock(IIndexQuery.class);
		when(query.getObjectClasses()).thenReturn(null);
		assertThat(indexer.getKeys(query), is(emptyArray()));

		List<Class<?>> classes = new ArrayList<Class<?>>();
		CollectionUtils.addAll(classes, new Object[] { InvocationSequenceData.class, SqlStatementData.class });
		when(query.getObjectClasses()).thenReturn(classes);

		Object[] keys = indexer.getKeys(query);
		assertThat(keys, is(arrayWithSize(2)));
		assertThat(keys[0], is(not(keys[1])));
	}

	/**
	 * Tests the functionality of the {@link SqlStringIndexer}.
	 */
	@Test
	public void sqlStringIndexer() {
		SqlStatementData data = mock(SqlStatementData.class);

		SqlStringIndexer<DefaultData> indexer = new SqlStringIndexer<DefaultData>();

		when(data.getSql()).thenReturn(RandomStringUtils.random(100));
		Object key1 = indexer.getKey(data);
		when(data.getSql()).thenReturn(RandomStringUtils.random(100));
		Object key2 = indexer.getKey(data);

		assertThat(key1, is(not(key2)));

		// assert that if max keys is set it will work
		indexer = new SqlStringIndexer<DefaultData>(1);

		when(data.getSql()).thenReturn(RandomStringUtils.random(100));
		key1 = indexer.getKey(data);
		when(data.getSql()).thenReturn(RandomStringUtils.random(100));
		key2 = indexer.getKey(data);

		assertThat(key1, is(key2));

		StorageIndexQuery query = mock(StorageIndexQuery.class);
		assertThat(indexer.getKeys(query), is(emptyArray()));

		when(query.getSql()).thenReturn(RandomStringUtils.random(100));
		Object[] keys = indexer.getKeys(query);
		assertThat(keys, is(arrayWithSize(1)));
		assertThat(keys[0], is(notNullValue()));
	}

	/**
	 * Tests the {@link TimestampIndexer}.
	 */
	@Test
	public void timestampIndexer() {
		DefaultData data = mock(DefaultData.class);

		TimestampIndexer<DefaultData> indexer = new TimestampIndexer<DefaultData>();

		when(data.getTimeStamp()).thenReturn(new Timestamp(System.currentTimeMillis()));
		Object key1 = indexer.getKey(data);
		when(data.getTimeStamp()).thenReturn(new Timestamp(System.currentTimeMillis() + 20 * 60 * 1000)); // 20
		Object key2 = indexer.getKey(data);

		assertThat(key1, is(not(key2)));

		IIndexQuery query = mock(IndexQuery.class);
		assertThat(indexer.getKeys(query), is(emptyArray()));
		when(query.isIntervalSet()).thenReturn(true);

		// not index period should also return empty array
		when(query.getFromDate()).thenReturn(new Timestamp(System.currentTimeMillis() - 30 * 60 * 1000));
		when(query.getToDate()).thenReturn(new Timestamp(System.currentTimeMillis() - 20 * 60 * 1000));
		assertThat(indexer.getKeys(query), is(emptyArray()));

		// when only one indexed time stamp is in interval return only one key
		when(query.getFromDate()).thenReturn(new Timestamp(System.currentTimeMillis() - 20 * 60 * 1000));
		when(query.getToDate()).thenReturn(new Timestamp(System.currentTimeMillis() + 3 * 60 * 1000));
		assertThat(indexer.getKeys(query), is(arrayWithSize(1)));

		// when both indexed time stamps are in interval return two keys
		when(query.getFromDate()).thenReturn(new Timestamp(System.currentTimeMillis() - 10 * 60 * 1000));
		when(query.getToDate()).thenReturn(new Timestamp(System.currentTimeMillis() + 30 * 60 * 1000));
		assertThat(indexer.getKeys(query), is(arrayWithSize(2)));
	}
}
