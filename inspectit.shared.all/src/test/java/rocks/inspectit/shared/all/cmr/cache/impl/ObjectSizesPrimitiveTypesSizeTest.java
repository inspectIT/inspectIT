package rocks.inspectit.shared.all.cmr.cache.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.cmr.cache.IObjectSizes;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.ExceptionEvent;
import rocks.inspectit.shared.all.communication.MethodSensorData;
import rocks.inspectit.shared.all.communication.Sizeable;
import rocks.inspectit.shared.all.communication.data.AggregatedExceptionSensorData;
import rocks.inspectit.shared.all.communication.data.AggregatedHttpTimerData;
import rocks.inspectit.shared.all.communication.data.AggregatedSqlStatementData;
import rocks.inspectit.shared.all.communication.data.AggregatedTimerData;
import rocks.inspectit.shared.all.communication.data.ClassLoadingInformationData;
import rocks.inspectit.shared.all.communication.data.CompilationInformationData;
import rocks.inspectit.shared.all.communication.data.ExceptionSensorData;
import rocks.inspectit.shared.all.communication.data.HttpInfo;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.communication.data.InvocationAwareData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.MemoryInformationData;
import rocks.inspectit.shared.all.communication.data.ParameterContentData;
import rocks.inspectit.shared.all.communication.data.ParameterContentType;
import rocks.inspectit.shared.all.communication.data.RuntimeInformationData;
import rocks.inspectit.shared.all.communication.data.SqlStatementData;
import rocks.inspectit.shared.all.communication.data.SystemInformationData;
import rocks.inspectit.shared.all.communication.data.ThreadInformationData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.all.communication.data.VmArgumentData;

/**
 * This tests checks all the {@link DefaultData} classes for the proper use of the
 * {@link IObjectSizes#getPrimitiveTypesSize(int, int, int, int, int, int)} method in the
 * {@link DefaultData#getObjectSize(IObjectSizes)} call.
 *
 * @author Ivan Senic
 *
 */
public class ObjectSizesPrimitiveTypesSizeTest {

	/**
	 * Classes to be tested.
	 */
	public static final Object[][] TESTING_CLASSES = new Object[][] { { TestDefaultData.class }, { TestMethodSensorData.class }, { TestInvocationAwareData.class }, { TimerData.class },
		{ SqlStatementData.class }, { ExceptionSensorData.class }, { InvocationSequenceData.class }, { ClassLoadingInformationData.class }, { CompilationInformationData.class },
		{ MemoryInformationData.class }, { RuntimeInformationData.class }, { SystemInformationData.class }, { ThreadInformationData.class }, { HttpTimerData.class },
		{ AggregatedExceptionSensorData.class }, { AggregatedHttpTimerData.class }, { AggregatedSqlStatementData.class }, { AggregatedTimerData.class }, { ParameterContentData.class },
		{ HttpInfo.class }, { VmArgumentData.class } };

		/**
		 * Enums that implement sizable.
		 */
	@SuppressWarnings("rawtypes")
		public static final Enum[][] ENUM_CLASSES = new Enum[][] { { ExceptionEvent.CREATED }, { ParameterContentType.FIELD } };

		/**
		 * Mocked {@link IObjectSizes}.
		 */
		private IObjectSizes objectSizes;

		/**
		 * Tests the class that extends the {@link DefaultData} class via reflection. Note that tested
		 * class can not be abstract.
		 *
		 * @param sizableClass
		 *            Class to test.
		 * @throws InstantiationException
		 *             InstantiationException
		 * @throws IllegalAccessException
		 *             IllegalAccessException
		 */
		@Test(dataProvider = "classProvider")
		public void sizeableClass(Class<? extends Sizeable> sizableClass) throws InstantiationException, IllegalAccessException {
			testClassForProperUseOfObjectSizesInternal(sizableClass, sizableClass.newInstance());
		}

		@SuppressWarnings("unchecked")
		@Test(dataProvider = "enumProvider")
		public <E extends Enum<?> & Sizeable> void sizeableEnum(E enumValue) {
			testClassForProperUseOfObjectSizesInternal((Class<? extends Sizeable>) enumValue.getClass(), enumValue);
		}

		private void testClassForProperUseOfObjectSizesInternal(Class<? extends Sizeable> sizableClass, Sizeable sizable) {
			assertThat("Class is not abstract.", !Modifier.isAbstract(sizableClass.getModifiers()));
			objectSizes = mock(IObjectSizes.class);
			Map<PrimitiveCount, Integer> primitiveCountMap = new HashMap<PrimitiveCount, Integer>();

			// In order to correctly verify we need to check all attributes of the whole
			// class hierarchy. In addition we need to ensure that each class hierarchy
			// called the object size factory with the correct number of attributes. Due to
			// the verification method in Mockito it is necessary to check whether the same
			// call to the object size factory was in fact performed twice (if they have
			// the same amount of attributes) as we then need to verify that the method was
			// called times(x).

			Class<?> clazz = sizableClass;
			while (!clazz.equals(Object.class)) {
				Field[] fields = clazz.getDeclaredFields();
				PrimitiveCount primitiveCount = new PrimitiveCount();
				for (Field field : fields) {
					if (Modifier.isStatic(field.getModifiers())) {
						// Static attributes are stored with the class.
						continue;
					}
					if (field.getType().isPrimitive() && !field.getType().isArray()) {
						if (field.getType().equals(Boolean.TYPE)) {
							primitiveCount.booleanCount++;
						} else if (field.getType().equals(Integer.TYPE)) {
							primitiveCount.intCount++;
						} else if (field.getType().equals(Float.TYPE)) {
							primitiveCount.floatCount++;
						} else if (field.getType().equals(Long.TYPE)) {
							primitiveCount.longCount++;
						} else if (field.getType().equals(Double.TYPE)) {
							primitiveCount.doubleCount++;
						}
					} else if (!field.getType().isArray()) {
						primitiveCount.referenceCount++;
					}
				}
				Integer cachedPrimitiveCount = primitiveCountMap.get(primitiveCount);
				if (null == cachedPrimitiveCount) {
					primitiveCountMap.put(primitiveCount, Integer.valueOf(1));
				} else {
					primitiveCountMap.put(primitiveCount, Integer.valueOf(cachedPrimitiveCount.intValue() + 1));
				}
				clazz = clazz.getSuperclass();
			}

			sizable.getObjectSize(objectSizes);

			int primitiveCountSize = 0;
			for (Map.Entry<PrimitiveCount, Integer> entry : primitiveCountMap.entrySet()) {
				PrimitiveCount primitiveCount = entry.getKey();
				if (primitiveCount.shouldBeCounted()) {
					primitiveCountSize += entry.getValue().intValue();
					verify(objectSizes, times(entry.getValue().intValue())).getPrimitiveTypesSize(primitiveCount.referenceCount, primitiveCount.booleanCount, primitiveCount.intCount,
							primitiveCount.floatCount, primitiveCount.longCount, primitiveCount.doubleCount);
				}
			}
			verify(objectSizes, times(primitiveCountSize)).getPrimitiveTypesSize(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyInt());
		}

		/**
		 * Provides classes to be tested.
		 *
		 * @return Provides classes to be tested.
		 */
		@DataProvider(name = "classProvider")
		public Object[][] classProvider() {
			return TESTING_CLASSES;
		}

		@DataProvider(name = "enumProvider")
		public Object[][] enumProvider() {
			return ENUM_CLASSES;
		}

		/**
		 * Simple class for counting purposes.
		 *
		 * @author Ivan Senic
		 *
		 */
		private class PrimitiveCount {
			int referenceCount = 0;
			int booleanCount = 0;
			int intCount = 0;
			int floatCount = 0;
			int longCount = 0;
			int doubleCount = 0;

			public boolean shouldBeCounted() {
				return !((referenceCount == 0) && (booleanCount == 0) && (intCount == 0) && (floatCount == 0) && (longCount == 0) && (doubleCount == 0));
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = (prime * result) + getOuterType().hashCode();
				result = (prime * result) + booleanCount;
				result = (prime * result) + doubleCount;
				result = (prime * result) + floatCount;
				result = (prime * result) + intCount;
				result = (prime * result) + longCount;
				result = (prime * result) + referenceCount;
				return result;
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj) {
					return true;
				}
				if (obj == null) {
					return false;
				}
				if (getClass() != obj.getClass()) {
					return false;
				}
				PrimitiveCount other = (PrimitiveCount) obj;
				if (!getOuterType().equals(other.getOuterType())) {
					return false;
				}
				if (booleanCount != other.booleanCount) {
					return false;
				}
				if (doubleCount != other.doubleCount) {
					return false;
				}
				if (floatCount != other.floatCount) {
					return false;
				}
				if (intCount != other.intCount) {
					return false;
				}
				if (longCount != other.longCount) {
					return false;
				}
				if (referenceCount != other.referenceCount) {
					return false;
				}
				return true;
			}

			private ObjectSizesPrimitiveTypesSizeTest getOuterType() {
				return ObjectSizesPrimitiveTypesSizeTest.this;
			}

		}

		public static class TestDefaultData extends DefaultData {

			private static final long serialVersionUID = -8907800333606213369L;
		};

		public static class TestMethodSensorData extends MethodSensorData {

			private static final long serialVersionUID = 3859181039818602878L;
		};

		public static class TestInvocationAwareData extends InvocationAwareData {

			private static final long serialVersionUID = 3283986124498709204L;

			@Override
			public double getInvocationAffiliationPercentage() {
				return 0;
			}
		};
}
