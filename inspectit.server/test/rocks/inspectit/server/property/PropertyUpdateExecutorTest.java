package info.novatec.inspectit.cmr.property;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import info.novatec.inspectit.cmr.property.configuration.SingleProperty;
import info.novatec.inspectit.cmr.property.spring.PropertyUpdate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("PMD")
public class PropertyUpdateExecutorTest {

	private static final String PROPERTY1 = "property1";

	private static final String PROPERTY2 = "property2";

	private PropertyUpdateExecutor propertyUpdateExecutor;

	@Mock
	private ConfigurableListableBeanFactory beanFactory;

	@Mock
	private TypeConverter typeConverter;

	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
		Mockito.when(beanFactory.getTypeConverter()).thenReturn(typeConverter);
		propertyUpdateExecutor = new PropertyUpdateExecutor();
		propertyUpdateExecutor.setBeanFactory(beanFactory);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void fieldOnePropertyNotMatchingType() {
		SingleProperty<Integer> property = Mockito.mock(SingleProperty.class);
		Mockito.when(property.getLogicalName()).thenReturn(PROPERTY1);
		Mockito.when(property.getValue()).thenReturn(Integer.valueOf(10));
		Mockito.when(typeConverter.convertIfNecessary(Integer.valueOf(10), int.class)).thenReturn(10);

		FieldTestClass fieldTestClass = new FieldTestClass();
		propertyUpdateExecutor.postProcessAfterInitialization(fieldTestClass, "testClass");
		propertyUpdateExecutor.executePropertyUpdates(Collections.<SingleProperty<?>> singletonList(property));

		assertThat(fieldTestClass.update1, is(10));
		assertThat(fieldTestClass.update2, is(nullValue()));
		assertThat(fieldTestClass.noUpdate, is(nullValue()));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void fieldOnePropertyMatchingType() {
		SingleProperty<Long> property = Mockito.mock(SingleProperty.class);
		Mockito.when(property.getLogicalName()).thenReturn(PROPERTY2);
		Mockito.when(property.getValue()).thenReturn(Long.valueOf(10));

		FieldTestClass fieldTestClass = new FieldTestClass();
		propertyUpdateExecutor.postProcessAfterInitialization(fieldTestClass, "testClass");
		propertyUpdateExecutor.executePropertyUpdates(Collections.<SingleProperty<?>> singletonList(property));

		Mockito.verifyZeroInteractions(typeConverter);
		assertThat(fieldTestClass.update1, is(0));
		assertThat(fieldTestClass.update2, is(10L));
		assertThat(fieldTestClass.noUpdate, is(nullValue()));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void fieldTwoProperties() {
		SingleProperty<Integer> property1 = Mockito.mock(SingleProperty.class);
		Mockito.when(property1.getLogicalName()).thenReturn(PROPERTY1);
		Mockito.when(property1.getValue()).thenReturn(Integer.valueOf(10));
		Mockito.when(typeConverter.convertIfNecessary(Integer.valueOf(10), int.class)).thenReturn(10);
		SingleProperty<Long> property2 = Mockito.mock(SingleProperty.class);
		Mockito.when(property2.getLogicalName()).thenReturn(PROPERTY2);
		Mockito.when(property2.getValue()).thenReturn(Long.valueOf(10));
		List<SingleProperty<?>> list = new ArrayList<>();
		list.add(property1);
		list.add(property2);

		FieldTestClass fieldTestClass = new FieldTestClass();
		propertyUpdateExecutor.postProcessAfterInitialization(fieldTestClass, "testClass");
		propertyUpdateExecutor.executePropertyUpdates(list);

		assertThat(fieldTestClass.update1, is(10));
		assertThat(fieldTestClass.update2, is(10L));
		assertThat(fieldTestClass.noUpdate, is(nullValue()));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void fieldTwoInstances() {
		SingleProperty<Long> property = Mockito.mock(SingleProperty.class);
		Mockito.when(property.getLogicalName()).thenReturn(PROPERTY2);
		Mockito.when(property.getValue()).thenReturn(Long.valueOf(10));

		FieldTestClass fieldTestClass1 = new FieldTestClass();
		FieldTestClass fieldTestClass2 = new FieldTestClass();
		propertyUpdateExecutor.postProcessAfterInitialization(fieldTestClass1, "testClass");
		propertyUpdateExecutor.postProcessAfterInitialization(fieldTestClass2, "testClass");
		propertyUpdateExecutor.executePropertyUpdates(Collections.<SingleProperty<?>> singletonList(property));

		Mockito.verifyZeroInteractions(typeConverter);
		assertThat(fieldTestClass1.update1, is(0));
		assertThat(fieldTestClass1.update2, is(10L));
		assertThat(fieldTestClass1.noUpdate, is(nullValue()));
		assertThat(fieldTestClass2.update1, is(0));
		assertThat(fieldTestClass2.update2, is(10L));
		assertThat(fieldTestClass2.noUpdate, is(nullValue()));
	}

	@Test
	public void methodOneProperty() {
		SingleProperty<?> property = Mockito.mock(SingleProperty.class);
		Mockito.when(property.getLogicalName()).thenReturn(PROPERTY1);

		MethodTestClass testClassInstance = new MethodTestClass();
		propertyUpdateExecutor.postProcessAfterInitialization(testClassInstance, "testClass");
		propertyUpdateExecutor.executePropertyUpdates(Collections.<SingleProperty<?>> singletonList(property));

		assertThat(testClassInstance.a, is(2));
	}

	@Test
	public void methodTwoProperties() {
		SingleProperty<?> property1 = Mockito.mock(SingleProperty.class);
		Mockito.when(property1.getLogicalName()).thenReturn(PROPERTY1);
		SingleProperty<?> property2 = Mockito.mock(SingleProperty.class);
		Mockito.when(property2.getLogicalName()).thenReturn(PROPERTY2);
		List<SingleProperty<?>> list = new ArrayList<>();
		list.add(property1);
		list.add(property2);

		MethodTestClass testClassInstance = new MethodTestClass();
		propertyUpdateExecutor.postProcessAfterInitialization(testClassInstance, "testClass");
		propertyUpdateExecutor.executePropertyUpdates(list);

		assertThat(testClassInstance.a, is(3));
	}

	@Test
	public void methodNoProperties() {
		SingleProperty<?> property = Mockito.mock(SingleProperty.class);
		Mockito.when(property.getLogicalName()).thenReturn("someOtherProperty");

		MethodTestClass testClassInstance = new MethodTestClass();
		propertyUpdateExecutor.postProcessAfterInitialization(testClassInstance, "testClass");
		propertyUpdateExecutor.executePropertyUpdates(Collections.<SingleProperty<?>> singletonList(property));

		assertThat(testClassInstance.a, is(0));
	}

	@Test
	public void methodTwoTestInstances() {
		SingleProperty<?> property1 = Mockito.mock(SingleProperty.class);
		Mockito.when(property1.getLogicalName()).thenReturn(PROPERTY1);
		SingleProperty<?> property2 = Mockito.mock(SingleProperty.class);
		Mockito.when(property2.getLogicalName()).thenReturn(PROPERTY2);
		List<SingleProperty<?>> list = new ArrayList<>();
		list.add(property1);
		list.add(property2);

		MethodTestClass testClassInstance = new MethodTestClass();
		MethodTestClass testClassInstance2 = new MethodTestClass();
		propertyUpdateExecutor.postProcessAfterInitialization(testClassInstance, "testClass");
		propertyUpdateExecutor.postProcessAfterInitialization(testClassInstance2, "testClass2");
		propertyUpdateExecutor.executePropertyUpdates(list);

		assertThat(testClassInstance.a, is(3));
		assertThat(testClassInstance2.a, is(3));
	}

	public static final class MethodTestClass {

		public int a;

		@PropertyUpdate(properties = { PROPERTY1 })
		public void property1() {
			a++;
		}

		@PropertyUpdate(properties = { PROPERTY2 })
		public void property2() {
			a++;
		}

		@PropertyUpdate(properties = { PROPERTY1, PROPERTY2 })
		public void property1And2() {
			a++;
		}

		@PropertyUpdate
		public void noPropertiesDefined() {
			a++;
		}
	}

	public static final class FieldTestClass {

		@Value("${" + PROPERTY1 + "}")
		public int update1;

		@Value("${" + PROPERTY2 + "}")
		public Long update2;

		public String noUpdate;

	}
}
