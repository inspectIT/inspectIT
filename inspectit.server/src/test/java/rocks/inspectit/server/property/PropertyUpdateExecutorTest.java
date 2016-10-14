package rocks.inspectit.server.property;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.reflect.AbstractInvocationHandler;

import rocks.inspectit.shared.all.cmr.property.spring.PropertyUpdate;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.cmr.property.configuration.SingleProperty;

@SuppressWarnings("PMD")
public class PropertyUpdateExecutorTest extends TestBase {

	private static final String PROPERTY1 = "property1";

	private static final String PROPERTY2 = "property2";

	@InjectMocks
	private PropertyUpdateExecutor propertyUpdateExecutor;

	@Mock
	private ConfigurableListableBeanFactory beanFactory;

	@Mock
	private TypeConverter typeConverter;

	@Mock
	private TargetSource targetSource;

	@BeforeMethod
	public void init() {
		Mockito.when(beanFactory.getTypeConverter()).thenReturn(typeConverter);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void fieldOnePropertyNotMatchingType() {
		SingleProperty<Integer> property = Mockito.mock(SingleProperty.class);
		Mockito.when(property.getLogicalName()).thenReturn(PROPERTY1);
		Mockito.when(property.getValue()).thenReturn(Integer.valueOf(10));
		Mockito.when(typeConverter.convertIfNecessary(Integer.valueOf(10), int.class)).thenReturn(10);

		FieldTestClass fieldTestClass = new FieldTestClass();
		Object returnedObject = propertyUpdateExecutor.postProcessAfterInitialization(fieldTestClass, "testClass");
		propertyUpdateExecutor.executePropertyUpdates(Collections.<SingleProperty<?>> singletonList(property));

		assertThat(returnedObject, is((Object) fieldTestClass));
		assertThat(fieldTestClass.update1, is(10));
		assertThat(fieldTestClass.update2, is(nullValue()));
		assertThat(fieldTestClass.noUpdate, is(nullValue()));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void proxyFieldOnePropertyNotMatchingType() throws Exception {
		SingleProperty<Integer> property = Mockito.mock(SingleProperty.class);
		Mockito.when(property.getLogicalName()).thenReturn(PROPERTY1);
		Mockito.when(property.getValue()).thenReturn(Integer.valueOf(10));
		Mockito.when(typeConverter.convertIfNecessary(Integer.valueOf(10), int.class)).thenReturn(10);

		FieldTestClass fieldTestClass = new FieldTestClass();
		Mockito.when(targetSource.getTarget()).thenReturn(fieldTestClass);
		InvocationHandler handler = new TestInvocationHanler();
		Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { SpringProxy.class, Advised.class }, handler);
		Object returnedObject = propertyUpdateExecutor.postProcessAfterInitialization(proxy, "testClass");
		propertyUpdateExecutor.executePropertyUpdates(Collections.<SingleProperty<?>> singletonList(property));

		assertThat(returnedObject, is(proxy));
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
		Object returnedObject = propertyUpdateExecutor.postProcessAfterInitialization(fieldTestClass, "testClass");
		propertyUpdateExecutor.executePropertyUpdates(Collections.<SingleProperty<?>> singletonList(property));

		Mockito.verifyZeroInteractions(typeConverter);
		assertThat(returnedObject, is((Object) fieldTestClass));
		assertThat(fieldTestClass.update1, is(0));
		assertThat(fieldTestClass.update2, is(10L));
		assertThat(fieldTestClass.noUpdate, is(nullValue()));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void proxyFieldOnePropertyMatchingType() throws Exception {
		SingleProperty<Long> property = Mockito.mock(SingleProperty.class);
		Mockito.when(property.getLogicalName()).thenReturn(PROPERTY2);
		Mockito.when(property.getValue()).thenReturn(Long.valueOf(10));

		FieldTestClass fieldTestClass = new FieldTestClass();
		Mockito.when(targetSource.getTarget()).thenReturn(fieldTestClass);
		InvocationHandler handler = new TestInvocationHanler();
		Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { SpringProxy.class, Advised.class }, handler);
		Object returnedObject = propertyUpdateExecutor.postProcessAfterInitialization(proxy, "testClass");
		propertyUpdateExecutor.executePropertyUpdates(Collections.<SingleProperty<?>> singletonList(property));

		Mockito.verifyZeroInteractions(typeConverter);
		assertThat(returnedObject, is(proxy));
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
		Object returnedObject = propertyUpdateExecutor.postProcessAfterInitialization(fieldTestClass, "testClass");
		propertyUpdateExecutor.executePropertyUpdates(list);

		assertThat(returnedObject, is((Object) fieldTestClass));
		assertThat(fieldTestClass.update1, is(10));
		assertThat(fieldTestClass.update2, is(10L));
		assertThat(fieldTestClass.noUpdate, is(nullValue()));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void proxyFieldTwoProperties() throws Exception {
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
		Mockito.when(targetSource.getTarget()).thenReturn(fieldTestClass);
		InvocationHandler handler = new TestInvocationHanler();
		Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { SpringProxy.class, Advised.class }, handler);
		Object returnedObject = propertyUpdateExecutor.postProcessAfterInitialization(proxy, "testClass");
		propertyUpdateExecutor.executePropertyUpdates(list);

		assertThat(returnedObject, is(proxy));
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
		Object returnedObject1 = propertyUpdateExecutor.postProcessAfterInitialization(fieldTestClass1, "testClass");
		Object returnedObject2 = propertyUpdateExecutor.postProcessAfterInitialization(fieldTestClass2, "testClass");
		propertyUpdateExecutor.executePropertyUpdates(Collections.<SingleProperty<?>> singletonList(property));

		Mockito.verifyZeroInteractions(typeConverter);
		assertThat(returnedObject1, is((Object) fieldTestClass1));
		assertThat(returnedObject2, is((Object) fieldTestClass2));
		assertThat(fieldTestClass1.update1, is(0));
		assertThat(fieldTestClass1.update2, is(10L));
		assertThat(fieldTestClass1.noUpdate, is(nullValue()));
		assertThat(fieldTestClass2.update1, is(0));
		assertThat(fieldTestClass2.update2, is(10L));
		assertThat(fieldTestClass2.noUpdate, is(nullValue()));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void proxyFieldTwoInstances() throws Exception {
		SingleProperty<Long> property = Mockito.mock(SingleProperty.class);
		Mockito.when(property.getLogicalName()).thenReturn(PROPERTY2);
		Mockito.when(property.getValue()).thenReturn(Long.valueOf(10));

		FieldTestClass fieldTestClass1 = new FieldTestClass();
		InvocationHandler handler1 = new TestInvocationHanler();
		Object proxy1 = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { SpringProxy.class, Advised.class }, handler1);
		FieldTestClass fieldTestClass2 = new FieldTestClass();
		InvocationHandler handler2 = new TestInvocationHanler();
		Object proxy2 = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { SpringProxy.class, Advised.class }, handler2);
		Mockito.when(targetSource.getTarget()).thenReturn(fieldTestClass1).thenReturn(fieldTestClass2);
		Object returnedObject1 = propertyUpdateExecutor.postProcessAfterInitialization(proxy1, "testClass");
		Object returnedObject2 = propertyUpdateExecutor.postProcessAfterInitialization(proxy2, "testClass");
		propertyUpdateExecutor.executePropertyUpdates(Collections.<SingleProperty<?>> singletonList(property));

		Mockito.verifyZeroInteractions(typeConverter);
		assertThat(returnedObject1, is(proxy1));
		assertThat(returnedObject2, is(proxy2));
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
		Object returnedObject = propertyUpdateExecutor.postProcessAfterInitialization(testClassInstance, "testClass");
		propertyUpdateExecutor.executePropertyUpdates(Collections.<SingleProperty<?>> singletonList(property));

		assertThat(returnedObject, is((Object) testClassInstance));
		assertThat(testClassInstance.a, is(2));
	}

	@Test
	public void proxyMethodOneProperty() throws Exception {
		SingleProperty<?> property = Mockito.mock(SingleProperty.class);
		Mockito.when(property.getLogicalName()).thenReturn(PROPERTY1);

		MethodTestClass testClassInstance = new MethodTestClass();
		Mockito.when(targetSource.getTarget()).thenReturn(testClassInstance);
		InvocationHandler handler = new TestInvocationHanler();
		Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { SpringProxy.class, Advised.class }, handler);
		Object returnedObject = propertyUpdateExecutor.postProcessAfterInitialization(proxy, "testClass");
		propertyUpdateExecutor.executePropertyUpdates(Collections.<SingleProperty<?>> singletonList(property));

		assertThat(returnedObject, is(proxy));
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
		Object returnedObject = propertyUpdateExecutor.postProcessAfterInitialization(testClassInstance, "testClass");
		propertyUpdateExecutor.executePropertyUpdates(list);

		assertThat(returnedObject, is((Object) testClassInstance));
		assertThat(testClassInstance.a, is(3));
	}

	@Test
	public void proxyMethodTwoProperties() throws Exception {
		SingleProperty<?> property1 = Mockito.mock(SingleProperty.class);
		Mockito.when(property1.getLogicalName()).thenReturn(PROPERTY1);
		SingleProperty<?> property2 = Mockito.mock(SingleProperty.class);
		Mockito.when(property2.getLogicalName()).thenReturn(PROPERTY2);
		List<SingleProperty<?>> list = new ArrayList<>();
		list.add(property1);
		list.add(property2);

		MethodTestClass testClassInstance = new MethodTestClass();
		Mockito.when(targetSource.getTarget()).thenReturn(testClassInstance);
		InvocationHandler handler = new TestInvocationHanler();
		Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { SpringProxy.class, Advised.class }, handler);
		Object returnedObject = propertyUpdateExecutor.postProcessAfterInitialization(proxy, "testClass");
		propertyUpdateExecutor.executePropertyUpdates(list);

		assertThat(returnedObject, is(proxy));
		assertThat(testClassInstance.a, is(3));
	}

	@Test
	public void methodNoProperties() {
		SingleProperty<?> property = Mockito.mock(SingleProperty.class);
		Mockito.when(property.getLogicalName()).thenReturn("someOtherProperty");

		MethodTestClass testClassInstance = new MethodTestClass();
		Object returnedObject = propertyUpdateExecutor.postProcessAfterInitialization(testClassInstance, "testClass");
		propertyUpdateExecutor.executePropertyUpdates(Collections.<SingleProperty<?>> singletonList(property));

		assertThat(returnedObject, is((Object) testClassInstance));
		assertThat(testClassInstance.a, is(0));
	}

	@Test
	public void proxyMethodNoProperties() throws Exception {
		SingleProperty<?> property = Mockito.mock(SingleProperty.class);
		Mockito.when(property.getLogicalName()).thenReturn("someOtherProperty");

		MethodTestClass testClassInstance = new MethodTestClass();
		Mockito.when(targetSource.getTarget()).thenReturn(testClassInstance);
		InvocationHandler handler = new TestInvocationHanler();
		Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { SpringProxy.class, Advised.class }, handler);
		Object returnedObject = propertyUpdateExecutor.postProcessAfterInitialization(proxy, "testClass");
		propertyUpdateExecutor.executePropertyUpdates(Collections.<SingleProperty<?>> singletonList(property));

		assertThat(returnedObject, is(proxy));
		assertThat(testClassInstance.a, is(0));
	}

	@Test
	public void methodTwoTestInstances() throws Exception {
		SingleProperty<?> property1 = Mockito.mock(SingleProperty.class);
		Mockito.when(property1.getLogicalName()).thenReturn(PROPERTY1);
		SingleProperty<?> property2 = Mockito.mock(SingleProperty.class);
		Mockito.when(property2.getLogicalName()).thenReturn(PROPERTY2);
		List<SingleProperty<?>> list = new ArrayList<>();
		list.add(property1);
		list.add(property2);

		MethodTestClass testClassInstance1 = new MethodTestClass();
		InvocationHandler handler1 = new TestInvocationHanler();
		Object proxy1 = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { SpringProxy.class, Advised.class }, handler1);
		MethodTestClass testClassInstance2 = new MethodTestClass();
		InvocationHandler handler2 = new TestInvocationHanler();
		Object proxy2 = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { SpringProxy.class, Advised.class }, handler2);
		Mockito.when(targetSource.getTarget()).thenReturn(testClassInstance1).thenReturn(testClassInstance2);
		Object returnedObject1 = propertyUpdateExecutor.postProcessAfterInitialization(proxy1, "testClass");
		Object returnedObject2 = propertyUpdateExecutor.postProcessAfterInitialization(proxy2, "testClass2");
		propertyUpdateExecutor.executePropertyUpdates(list);

		assertThat(returnedObject1, is(proxy1));
		assertThat(returnedObject2, is(proxy2));
		assertThat(testClassInstance1.a, is(3));
		assertThat(testClassInstance2.a, is(3));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void proxyFieldException() throws Exception {
		SingleProperty<Long> property = Mockito.mock(SingleProperty.class);
		Mockito.when(property.getLogicalName()).thenReturn(PROPERTY2);
		Mockito.when(property.getValue()).thenReturn(Long.valueOf(10));

		FieldTestClass fieldTestClass = new FieldTestClass();
		Mockito.when(targetSource.getTarget()).thenThrow(RuntimeException.class);
		InvocationHandler handler = new TestInvocationHanler();
		Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { SpringProxy.class, Advised.class }, handler);
		Object returnedObject = propertyUpdateExecutor.postProcessAfterInitialization(proxy, "testClass");
		propertyUpdateExecutor.executePropertyUpdates(Collections.<SingleProperty<?>> singletonList(property));

		Mockito.verifyZeroInteractions(typeConverter);
		assertThat(returnedObject, is(proxy));
		assertThat(fieldTestClass.update1, is(0));
		assertThat(fieldTestClass.update2, is(nullValue()));
		assertThat(fieldTestClass.noUpdate, is(nullValue()));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void proxyFieldTargetNull() throws Exception {
		SingleProperty<Long> property = Mockito.mock(SingleProperty.class);
		Mockito.when(property.getLogicalName()).thenReturn(PROPERTY2);
		Mockito.when(property.getValue()).thenReturn(Long.valueOf(10));

		FieldTestClass fieldTestClass = new FieldTestClass();
		Mockito.when(targetSource.getTarget()).thenReturn(null);
		InvocationHandler handler = new TestInvocationHanler();
		Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { SpringProxy.class, Advised.class }, handler);
		Object returnedObject = propertyUpdateExecutor.postProcessAfterInitialization(proxy, "testClass");
		propertyUpdateExecutor.executePropertyUpdates(Collections.<SingleProperty<?>> singletonList(property));

		Mockito.verifyZeroInteractions(typeConverter);
		assertThat(returnedObject, is(proxy));
		assertThat(fieldTestClass.update1, is(0));
		assertThat(fieldTestClass.update2, is(nullValue()));
		assertThat(fieldTestClass.noUpdate, is(nullValue()));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void proxyMethodException() throws Exception {
		SingleProperty<?> property = Mockito.mock(SingleProperty.class);
		Mockito.when(property.getLogicalName()).thenReturn(PROPERTY1);

		MethodTestClass testClassInstance = new MethodTestClass();
		Mockito.when(targetSource.getTarget()).thenThrow(RuntimeException.class);
		InvocationHandler handler = new TestInvocationHanler();
		Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { SpringProxy.class, Advised.class }, handler);
		Object returnedObject = propertyUpdateExecutor.postProcessAfterInitialization(proxy, "testClass");
		propertyUpdateExecutor.executePropertyUpdates(Collections.<SingleProperty<?>> singletonList(property));

		assertThat(returnedObject, is(proxy));
		assertThat(testClassInstance.a, is(0));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void proxyMethodTargetNull() throws Exception {
		SingleProperty<?> property = Mockito.mock(SingleProperty.class);
		Mockito.when(property.getLogicalName()).thenReturn(PROPERTY1);

		MethodTestClass testClassInstance = new MethodTestClass();
		Mockito.when(targetSource.getTarget()).thenReturn(null);
		InvocationHandler handler = new TestInvocationHanler();
		Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { SpringProxy.class, Advised.class }, handler);
		Object returnedObject = propertyUpdateExecutor.postProcessAfterInitialization(proxy, "testClass");
		propertyUpdateExecutor.executePropertyUpdates(Collections.<SingleProperty<?>> singletonList(property));

		assertThat(returnedObject, is(proxy));
		assertThat(testClassInstance.a, is(0));
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

	private final class TestInvocationHanler extends AbstractInvocationHandler {

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
			if ("getTargetSource".equals(method.getName())) {
				return targetSource;
			}
			return null;
		}

	}
}
