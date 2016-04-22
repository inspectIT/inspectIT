package rocks.inspectit.server.cache.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.cliffc.high_scale_lib.NonBlockingHashMapLong;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.javamex.classmexer.MemoryUtil;
import com.javamex.classmexer.MemoryUtil.VisibilityFilter;

import rocks.inspectit.server.cache.AbstractObjectSizes;
import rocks.inspectit.server.cache.impl.ObjectSizesFactory;
import rocks.inspectit.server.test.AbstractTestNGLogSupport;
import rocks.inspectit.shared.all.cmr.cache.IObjectSizes;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.MethodSensorData;
import rocks.inspectit.shared.all.communication.Sizeable;
import rocks.inspectit.shared.all.communication.data.AggregatedExceptionSensorData;
import rocks.inspectit.shared.all.communication.data.AggregatedHttpTimerData;
import rocks.inspectit.shared.all.communication.data.AggregatedSqlStatementData;
import rocks.inspectit.shared.all.communication.data.AggregatedTimerData;
import rocks.inspectit.shared.all.communication.data.ClassLoadingInformationData;
import rocks.inspectit.shared.all.communication.data.CompilationInformationData;
import rocks.inspectit.shared.all.communication.data.ExceptionSensorData;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.communication.data.InvocationAwareData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.MemoryInformationData;
import rocks.inspectit.shared.all.communication.data.RuntimeInformationData;
import rocks.inspectit.shared.all.communication.data.SqlStatementData;
import rocks.inspectit.shared.all.communication.data.SystemInformationData;
import rocks.inspectit.shared.all.communication.data.ThreadInformationData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.all.util.UnderlyingSystemInfo;
import rocks.inspectit.shared.all.util.UnderlyingSystemInfo.JvmProvider;

/**
 * This test class provides test for the method of {@link IObjectSizes} that calculate the size of
 * the core Java classes.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("PMD")
public class MemoryCalculationTest extends AbstractTestNGLogSupport {

	/**
	 * The logger of this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(MemoryCalculationTest.class);

	/**
	 * Our classes to be tested.
	 */
	public static final Object[][] TESTING_CLASSES = new Object[][] { { TestDefaultData.class }, { TestMethodSensorData.class }, { TestInvocationAwareData.class }, { TimerData.class },
			{ SqlStatementData.class }, { ExceptionSensorData.class }, { InvocationSequenceData.class }, { ClassLoadingInformationData.class }, { CompilationInformationData.class },
			{ MemoryInformationData.class }, { RuntimeInformationData.class }, { SystemInformationData.class }, { ThreadInformationData.class }, { HttpTimerData.class },
			{ AggregatedExceptionSensorData.class }, { AggregatedHttpTimerData.class }, { AggregatedSqlStatementData.class }, { AggregatedTimerData.class } };

	/**
	 * Amount that we add to each hash map because of the entry, key and value set safety.
	 */
	private static long HASH_MAP_SAFTY_DELTA;

	/**
	 * Field for knowing how much big is the hash map table.
	 */
	private static Field oracleHashMapTable;

	/**
	 * Field for knowing how much big is the hash map table.
	 */
	private static Field ibmHashMapTable;

	/**
	 * {@link IObjectSizes}.
	 */
	private IObjectSizes objectSizes;

	/**
	 * Gets the proper instance of the {@link IObjectSizes} because it s dependent on the system
	 * test is run on.
	 * 
	 * @throws Exception
	 *             If Exception occurs.
	 */
	@BeforeClass
	public void initCorrectObjectSizesInstance() throws Exception {
		objectSizes = new ObjectSizesFactory().getObject();

		if (UnderlyingSystemInfo.JVM_PROVIDER == JvmProvider.SUN || UnderlyingSystemInfo.JVM_PROVIDER == JvmProvider.ORACLE) {
			oracleHashMapTable = HashMap.class.getDeclaredField("table"); // NOPMD
			if (null != oracleHashMapTable) {
				oracleHashMapTable.setAccessible(true);
			}
		} else if (UnderlyingSystemInfo.JVM_PROVIDER == JvmProvider.IBM) {
			ibmHashMapTable = HashMap.class.getDeclaredField("table"); // NOPMD
			if (null != ibmHashMapTable) {
				ibmHashMapTable.setAccessible(true);
			}
		}

		HASH_MAP_SAFTY_DELTA = objectSizes.getSizeOfHashMapKeyEntrySet();
	}

	/**
	 * Tests size of a simple {@link Object}.
	 */
	@Test
	public void object() {
		Object o = new Object();
		long ourSize = objectSizes.getSizeOfObjectObject();
		long theirSize = MemoryUtil.deepMemoryUsageOf(o, VisibilityFilter.ALL);
		assertThat(ourSize, is(equalTo(theirSize)));
	}

	/**
	 * Tests size of a {@link Long}.
	 */
	@Test
	public void longObject() {
		Long l = Long.valueOf(1);
		long ourSize = objectSizes.getSizeOfLongObject();
		long theirSize = MemoryUtil.deepMemoryUsageOf(l, VisibilityFilter.ALL);
		assertThat(ourSize, is(equalTo(theirSize)));
	}

	/**
	 * Tests size of a {@link Integer}.
	 */
	@Test
	public void integerObject() {
		Integer i = Integer.valueOf(1);
		long ourSize = objectSizes.getSizeOfIntegerObject();
		long theirSize = MemoryUtil.deepMemoryUsageOf(i, VisibilityFilter.ALL);
		assertThat(ourSize, is(equalTo(theirSize)));
	}

	/**
	 * Tests size of a {@link Short}.
	 */
	@Test
	public void shortObject() {
		Short s = Short.valueOf((short) 1); // NOPMD
		long ourSize = objectSizes.getSizeOfShortObject();
		long theirSize = MemoryUtil.deepMemoryUsageOf(s, VisibilityFilter.ALL);
		assertThat(ourSize, is(equalTo(theirSize)));
	}

	/**
	 * Tests size of a {@link Character}.
	 */
	@Test
	public void characterObject() {
		Character s = Character.valueOf('c');
		long ourSize = objectSizes.getSizeOfCharacterObject();
		long theirSize = MemoryUtil.deepMemoryUsageOf(s, VisibilityFilter.ALL);
		assertThat(ourSize, is(equalTo(theirSize)));
	}

	/**
	 * Tests size of a {@link Boolean}.
	 */
	@Test
	public void booleanObject() {
		Boolean b = Boolean.TRUE;
		long ourSize = objectSizes.getSizeOfBooleanObject();
		long theirSize = MemoryUtil.deepMemoryUsageOf(b, VisibilityFilter.ALL);
		assertThat(ourSize, is(equalTo(theirSize)));
	}

	/**
	 * Tests empty{@link String}.
	 */
	@Test(invocationCount = 50)
	public void empryString() {
		String s = "";
		long ourSize = objectSizes.getSizeOf(s);
		long theirSize = MemoryUtil.deepMemoryUsageOf(s, VisibilityFilter.ALL);
		assertThat(ourSize, is(equalTo(theirSize)));
	}

	/**
	 * Tests size of a random {@link String} with length from 1 - 100 characters.
	 */
	@Test(invocationCount = 50)
	public void string() {
		int size = (int) (Math.random() * 100);
		String s = RandomStringUtils.random(size);
		long ourSize = objectSizes.getSizeOf(s);
		long theirSize = MemoryUtil.deepMemoryUsageOf(s, VisibilityFilter.ALL);
		assertThat(ourSize, is(equalTo(theirSize)));
	}

	/**
	 * Tests size of a {@link Timestamp}.
	 */
	@Test
	public void timestamp() {
		Timestamp t = new Timestamp(new Date().getTime());
		long ourSize = objectSizes.getSizeOf(t);
		long theirSize = MemoryUtil.deepMemoryUsageOf(t, VisibilityFilter.ALL);
		assertThat(ourSize, is(equalTo(theirSize)));
	}

	/**
	 * Test the array of size 0 and 1.
	 */
	@Test
	public void emptyArray() {
		Object[] array = new Object[0];
		long ourSize = objectSizes.getSizeOfArray(array.length);
		long theirSize = MemoryUtil.deepMemoryUsageOf(array, VisibilityFilter.ALL);
		assertThat(ourSize, is(equalTo(theirSize)));

		array = new Object[1];
		ourSize = objectSizes.getSizeOfArray(array.length);
		theirSize = MemoryUtil.deepMemoryUsageOf(array, VisibilityFilter.ALL);
		assertThat(ourSize, is(equalTo(theirSize)));
	}

	/**
	 * General test for arrays of different sizes.
	 */
	@Test(invocationCount = 50)
	public void array() {
		Object[] array = new Object[(int) (Math.random() * 100)];
		long ourSize = objectSizes.getSizeOfArray(array.length);
		long theirSize = MemoryUtil.deepMemoryUsageOf(array, VisibilityFilter.ALL);
		assertThat("Empty array", ourSize, is(equalTo(theirSize)));

		for (int i = 0; i < array.length; i++) {
			array[i] = new Object();
		}

		if (checkObjectGraphIdentityHashCodeCollision(array, new HashSet<Integer>(), new ArrayList<Integer>())) {
			LOGGER.info("Object graph identity collision check passed for test: array()");
			ourSize = objectSizes.getSizeOfArray(array.length) + array.length * objectSizes.getSizeOfObjectObject();
			theirSize = MemoryUtil.deepMemoryUsageOf(array, VisibilityFilter.ALL);
			assertThat("Full array of " + array.length + " elements", ourSize, is(equalTo(theirSize)));
		}
	}

	/**
	 * boolean array.
	 */
	@Test(invocationCount = 10)
	public void booleanArray() {
		boolean[] array = new boolean[(int) (Math.random() * 100)];
		long ourSize = objectSizes.getSizeOfPrimitiveArray(array.length, AbstractObjectSizes.BOOLEAN_SIZE);
		long theirSize = MemoryUtil.deepMemoryUsageOf(array, VisibilityFilter.ALL);
		assertThat("boolean array", ourSize, is(equalTo(theirSize)));
	}

	/**
	 * char array.
	 */
	@Test(invocationCount = 10)
	public void charArray() {
		char[] array = new char[(int) (Math.random() * 100)];
		long ourSize = objectSizes.getSizeOfPrimitiveArray(array.length, AbstractObjectSizes.CHAR_SIZE);
		long theirSize = MemoryUtil.deepMemoryUsageOf(array, VisibilityFilter.ALL);
		assertThat("char array", ourSize, is(equalTo(theirSize)));
	}

	/**
	 * int array.
	 */
	@Test(invocationCount = 10)
	public void intArray() {
		int[] array = new int[(int) (Math.random() * 100)];
		long ourSize = objectSizes.getSizeOfPrimitiveArray(array.length, AbstractObjectSizes.INT_SIZE);
		long theirSize = MemoryUtil.deepMemoryUsageOf(array, VisibilityFilter.ALL);
		assertThat("int array", ourSize, is(equalTo(theirSize)));
	}

	/**
	 * float array.
	 */
	@Test(invocationCount = 10)
	public void floatArray() {
		float[] array = new float[(int) (Math.random() * 100)];
		long ourSize = objectSizes.getSizeOfPrimitiveArray(array.length, AbstractObjectSizes.FLOAT_SIZE);
		long theirSize = MemoryUtil.deepMemoryUsageOf(array, VisibilityFilter.ALL);
		assertThat("float array", ourSize, is(equalTo(theirSize)));
	}

	/**
	 * long array.
	 */
	@Test(invocationCount = 10)
	public void longArray() {
		long[] array = new long[(int) (Math.random() * 100)];
		long ourSize = objectSizes.getSizeOfPrimitiveArray(array.length, AbstractObjectSizes.LONG_SIZE);
		long theirSize = MemoryUtil.deepMemoryUsageOf(array, VisibilityFilter.ALL);
		assertThat("long array", ourSize, is(equalTo(theirSize)));
	}

	/**
	 * double array.
	 */
	@Test(invocationCount = 10)
	public void doubleArray() {
		double[] array = new double[(int) (Math.random() * 100)];
		long ourSize = objectSizes.getSizeOfPrimitiveArray(array.length, AbstractObjectSizes.DOUBLE_SIZE);
		long theirSize = MemoryUtil.deepMemoryUsageOf(array, VisibilityFilter.ALL);
		assertThat("double array", ourSize, is(equalTo(theirSize)));
	}

	/**
	 * short array.
	 */
	@Test(invocationCount = 10)
	public void shortArray() {
		short[] array = new short[(int) (Math.random() * 100)];
		long ourSize = objectSizes.getSizeOfPrimitiveArray(array.length, AbstractObjectSizes.SHORT_SIZE);
		long theirSize = MemoryUtil.deepMemoryUsageOf(array, VisibilityFilter.ALL);
		assertThat("short array", ourSize, is(equalTo(theirSize)));
	}

	/**
	 * Tests size of empty and populated {@link ArrayList} object.
	 */
	@Test(invocationCount = 50)
	public void arrayList() {
		ArrayList<Object> arrayList = new ArrayList<Object>();
		long ourSize = objectSizes.getSizeOf(arrayList);
		long theirSize = MemoryUtil.deepMemoryUsageOf(arrayList, VisibilityFilter.ALL);
		assertThat("Empty list", ourSize, is(equalTo(theirSize)));

		int size = (int) (Math.random() * 100);
		for (int i = 0; i < size; i++) {
			arrayList.add(new Object());
		}

		if (checkObjectGraphIdentityHashCodeCollision(arrayList, new HashSet<Integer>(), new ArrayList<Integer>())) {
			LOGGER.info("Object graph identity collision check passed for test: arrayList()");
			ourSize = objectSizes.getSizeOf(arrayList) + size * objectSizes.getSizeOfObjectObject();
			theirSize = MemoryUtil.deepMemoryUsageOf(arrayList, VisibilityFilter.ALL);
			assertThat("Random list size", ourSize, is(equalTo(theirSize)));
		}
	}

	/**
	 * Tests size of empty and populated {@link ArrayList} object with initial size of 0.
	 */
	@Test(invocationCount = 50)
	public void arrayListInitializeZero() {
		ArrayList<Object> arrayList = new ArrayList<Object>(0);
		long ourSize = objectSizes.getSizeOf(arrayList, 0);
		long theirSize = MemoryUtil.deepMemoryUsageOf(arrayList, VisibilityFilter.ALL);
		assertThat("Empty list", ourSize, is(equalTo(theirSize)));

		int size = (int) (Math.random() * 100);
		for (int i = 0; i < size; i++) {
			arrayList.add(new Object());
		}

		if (checkObjectGraphIdentityHashCodeCollision(arrayList, new HashSet<Integer>(), new ArrayList<Integer>())) {
			LOGGER.info("Object graph identity collision check passed for test: arrayListInitializeZero()");
			ourSize = objectSizes.getSizeOf(arrayList, 0) + size * objectSizes.getSizeOfObjectObject();
			theirSize = MemoryUtil.deepMemoryUsageOf(arrayList, VisibilityFilter.ALL);
			assertThat("Random list size", ourSize, is(equalTo(theirSize)));
		}
	}

	/**
	 * Tests size of empty and populated {@link HashMap} object.
	 * 
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	@Test(invocationCount = 50)
	public void hashMap() throws IllegalArgumentException, IllegalAccessException {
		HashMap<Object, Object> hashMap = new HashMap<Object, Object>();
		long ourSize = objectSizes.getSizeOfHashMap(0);
		long theirSize = MemoryUtil.deepMemoryUsageOf(hashMap, VisibilityFilter.ALL);
		assertThat("Empty map", ourSize, is(equalTo(theirSize + HASH_MAP_SAFTY_DELTA)));

		int size = (int) (Math.random() * 100);
		for (int i = 0; i < size; i++) {
			hashMap.put(new Object(), new Object());
		}

		if (checkObjectGraphIdentityHashCodeCollision(hashMap, new HashSet<Integer>(), new ArrayList<Integer>())) {
			LOGGER.info("Object graph identity collision check passed for test: hashMap()");
			ourSize = objectSizes.getSizeOfHashMap(hashMap.size()) + 2 * size * objectSizes.getSizeOfObjectObject();
			theirSize = MemoryUtil.deepMemoryUsageOf(hashMap, VisibilityFilter.ALL);

			boolean tableCorrect = false;
			Object[] table = null;

			if (null != oracleHashMapTable) {
				table = (Object[]) oracleHashMapTable.get(hashMap);
			} else if (null != ibmHashMapTable) {
				table = (Object[]) ibmHashMapTable.get(hashMap);
			}
			if (null != table) {
				int tableSize = table.length;
				int ourTableSize = ((AbstractObjectSizes) objectSizes).getHashMapCapacityFromSize(size, 16);
				tableCorrect = tableSize == ourTableSize;
			}

			if (tableCorrect) {
				assertThat("Random map size of " + size, ourSize, is(equalTo(theirSize + HASH_MAP_SAFTY_DELTA)));
			} else {
				assertThat("Random map size of " + size, ourSize, is(greaterThanOrEqualTo(theirSize + HASH_MAP_SAFTY_DELTA)));
			}
		}
	}

	/**
	 * Tests size of empty and populated {@link HashSet} object.
	 */
	@Test
	public void hashSet() {
		HashSet<Object> hashSet = new HashSet<Object>();
		long ourSize = objectSizes.getSizeOfHashSet(hashSet.size());
		long theirSize = MemoryUtil.deepMemoryUsageOf(hashSet, VisibilityFilter.ALL);
		assertThat("Empty set", ourSize, is(equalTo(theirSize + HASH_MAP_SAFTY_DELTA)));
		// no hashSet can pass checkObjectGraphIdentityHashCodeCollision check, so we don't checks
		// with elements
		// since it is anyway holding just the HashMap, testing HashMap will be enough
	}

	/**
	 * Tests size of empty and populated {@link ConcurrentHashMap} object. Note that populated
	 * {@link ConcurrentHashMap} is being tested with only 1 segment. It is impossible to provide
	 * the correct size with more segments, because it is unknown what will be the distributon of
	 * elements between segments.
	 */
	@Test(invocationCount = 50)
	public void concurrentHashMap() {
		// we can only precisely calculate random amount of elements with one segment
		int concurrencyLevel = 1;
		ConcurrentHashMap<Object, Object> concurrentHashMap = new ConcurrentHashMap<Object, Object>(16, 0.75f, concurrencyLevel);
		long theirSize = MemoryUtil.deepMemoryUsageOf(concurrentHashMap, VisibilityFilter.ALL);
		long ourSize = objectSizes.getSizeOfConcurrentHashMap(0, concurrencyLevel);
		assertThat("Empty map", ourSize, is(equalTo(theirSize)));

		concurrentHashMap = new ConcurrentHashMap<Object, Object>(16, 0.75f, 1);
		int size = (int) (Math.random() * 100);
		for (int i = 0; i < size; i++) {
			concurrentHashMap.put(new Object(), new Object());
		}

		if (checkObjectGraphIdentityHashCodeCollision(concurrentHashMap, new HashSet<Integer>(), new ArrayList<Integer>())) {
			LOGGER.info("Object graph identity collision check passed for test: concurrentHashMap()");
			theirSize = MemoryUtil.deepMemoryUsageOf(concurrentHashMap, VisibilityFilter.ALL);
			ourSize = objectSizes.getSizeOfConcurrentHashMap(size, 1) + size * 2 * objectSizes.getSizeOfObjectObject();
			assertThat("Random map size of " + size, ourSize, is(equalTo(theirSize)));
		}
	}

	/**
	 * Tests size of empty and populated {@link NonBlockingHashMapLong} object.
	 */
	@Test(invocationCount = 50)
	public void nonBlockingHashMapLong() {
		// we can only precisely calculate random amount of elements with one segment
		NonBlockingHashMapLong<Object> nonBlockingHashMapLong = new NonBlockingHashMapLong<>();
		long theirSize = MemoryUtil.deepMemoryUsageOf(nonBlockingHashMapLong, VisibilityFilter.ALL);
		long ourSize = objectSizes.getSizeOfNonBlockingHashMapLong(0);
		assertThat("Empty map", ourSize, is(equalTo(theirSize)));

		nonBlockingHashMapLong = new NonBlockingHashMapLong<>();
		int size = (int) (Math.random() * 100);
		for (long l = 1; l <= size; l++) { // zero key not supported
			nonBlockingHashMapLong.put(l, Long.valueOf(l));
		}

		theirSize = MemoryUtil.deepMemoryUsageOf(nonBlockingHashMapLong, VisibilityFilter.ALL);
		ourSize = objectSizes.getSizeOfNonBlockingHashMapLong(size) + size * objectSizes.getSizeOfLongObject();
		assertThat("Random map size of " + size, ourSize, is(equalTo(theirSize)));
	}

	/**
	 * Tests the arbitrary class created for test purposes.
	 */
	@Test
	public void arbitraryClass() {
		TestClass testObject = new TestClass();
		long ourSize = objectSizes.getSizeOf(testObject);
		long theirSize = MemoryUtil.deepMemoryUsageOf(testObject, VisibilityFilter.ALL);
		assertThat("Test class", ourSize, is(equalTo(theirSize)));
	}

	/**
	 * Tests the special boolean test class to assuer boolean sizes in JVM are in fact compiled as
	 * int.
	 */
	@Test
	public void booleanTestClass() {
		BooleanTestClass testObject = new BooleanTestClass();
		long ourSize = objectSizes.getSizeOf(testObject);
		long theirSize = MemoryUtil.deepMemoryUsageOf(testObject, VisibilityFilter.ALL);
		assertThat("Boolean test class", ourSize, is(equalTo(theirSize)));
	}

	/**
	 * Tests the inspectIT classes instances created by reflection.
	 * <p>
	 * <b>Important:</b> The {@link SqlStatementData} class has a problem with calculation when Sun
	 * JVM is used. The 8 bytes are calculated more than needed. Thus, we need to have the "closeTo"
	 * assertion until this problem is fixed. This problem is now part of the
	 * <a href="https://jira.novatec-gmbh.de/browse/INSPECTIT-705">INSPECTIT-705</a> ticket.
	 * 
	 * @param defaultDataClass
	 *            Class to test.
	 * @throws InstantiationException
	 *             If {@link InstantiationException} occurs.
	 * @throws IllegalAccessException
	 *             If {@link IllegalAccessException} occurs.
	 */
	@Test(dataProvider = "classProvider")
	public void inspectitClasses(Class<? extends DefaultData> defaultDataClass) throws InstantiationException, IllegalAccessException {
		DefaultData object = defaultDataClass.newInstance();
		long ourSize = objectSizes.getSizeOf(object);
		long theirSize = MemoryUtil.deepMemoryUsageOf(object, VisibilityFilter.ALL);
		assertThat("Size of " + defaultDataClass.getName(), ourSize, is(theirSize));
		LOGGER.info("Tested class " + defaultDataClass.getName() + " -> our size: " + ourSize + ", their size: " + theirSize);
	}

	/**
	 * Checks for the possible object graph identity hash code collision. Objects that don't pass
	 * this check can not be given to the classmexer for size calculations, because correct size
	 * will not be calculated due to the collision that will occur in the classmexer internally.
	 * <p>
	 * Most of this method is copied by the classmexer.
	 * 
	 * @param obj
	 *            Object to check. Note that this method is recursive.
	 * @param counted
	 *            HashSet that will serve as the collision tester
	 * @param countedList
	 *            List that will hold all the original identity values.
	 * 
	 * @return True if there is no collisions in the hash set (map) and object can be tested via
	 *         classmexer. False otherwise.
	 * @throws SecurityException
	 */
	private static boolean checkObjectGraphIdentityHashCodeCollision(Object obj, Set<Integer> counted, List<Integer> countedList) throws SecurityException {
		Stack<Object> stack = new Stack<Object>();
		stack.push(obj);
		while (!(stack.isEmpty())) {
			Object object = stack.pop();
			Integer identityHashCode = Integer.valueOf(System.identityHashCode(object));
			boolean added = counted.add(identityHashCode);
			if (!added) {
				if (!countedList.contains(identityHashCode)) {
					return false;
				}
			} else {
				Class<?> clazz = object.getClass();
				Class<?> compType = clazz.getComponentType();
				if ((compType != null) && (!(compType.isPrimitive()))) {
					Object[] array = (Object[]) object;
					for (int i = 0; i < array.length; i++) {
						Object element = array[i];
						if (element != null) {
							stack.push(element);
						}
					}
				}
				while (clazz != null) {
					for (Field field : clazz.getDeclaredFields()) {
						int mod = field.getModifiers();
						if ((mod & 0x8) == 0) {
							Class<?> fieldClass = field.getType();
							if (!(fieldClass.isPrimitive())) {
								if (!(field.isAccessible())) {
									field.setAccessible(true);
								}
								try {
									Object subObject = field.get(object);
									if (subObject != null) {
										stack.push(subObject);
									}
								} catch (IllegalAccessException illAcc) {
									throw new InternalError("Couldn't read " + field);
								}
							}
						}
					}
					clazz = clazz.getSuperclass();
				}
			}
		}
		return true;
	}

	/**
	 * Provides classes to be tested.
	 * 
	 * @return Provides classes to be tested.
	 */
	@DataProvider(name = "classProvider")
	public Object[][] classprovider() {
		return TESTING_CLASSES; // NOPMD
	}

	@SuppressWarnings("unused")
	private static class TestClass implements Sizeable {

		private boolean booleanField;

		private int intField;

		private long longField;

		private String str = RandomStringUtils.random((int) (Math.random() * 100));

		@Override
		public long getObjectSize(IObjectSizes objectSizes) {
			long size = objectSizes.getSizeOfObjectHeader();
			size += objectSizes.getPrimitiveTypesSize(1, 1, 1, 0, 1, 0);
			size += objectSizes.getSizeOf(str);
			return objectSizes.alignTo8Bytes(size);
		}

		/**
		 * {@inheritDoc}
		 */
		public long getObjectSize(IObjectSizes objectSizes, boolean doAlign) {
			return getObjectSize(objectSizes);
		}
	}

	@SuppressWarnings("unused")
	private static class BooleanTestClass implements Sizeable {

		private boolean b1, b2, b3, b4, b5, b6, b7, b8, b9;

		@Override
		public long getObjectSize(IObjectSizes objectSizes) {
			long size = objectSizes.getSizeOfObjectHeader();
			size += objectSizes.getPrimitiveTypesSize(0, 9, 0, 0, 0, 0);
			return objectSizes.alignTo8Bytes(size);
		}

		/**
		 * {@inheritDoc}
		 */
		public long getObjectSize(IObjectSizes objectSizes, boolean doAlign) {
			return getObjectSize(objectSizes);
		}
	}

	@SuppressWarnings("serial")
	public static class TestDefaultData extends DefaultData {
	};

	@SuppressWarnings("serial")
	public static class TestMethodSensorData extends MethodSensorData {
	};

	@SuppressWarnings("serial")
	public static class TestInvocationAwareData extends InvocationAwareData {

		@Override
		public double getInvocationAffiliationPercentage() {
			return 0;
		}
	};
}
