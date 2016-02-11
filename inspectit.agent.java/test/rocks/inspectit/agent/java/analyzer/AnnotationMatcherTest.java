package info.novatec.inspectit.agent.analyzer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import info.novatec.inspectit.agent.TestBase;
import info.novatec.inspectit.agent.analyzer.impl.AnnotationMatcher;
import info.novatec.inspectit.agent.config.impl.UnregisteredSensorConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.NotFoundException;

@SuppressWarnings("PMD")
public class AnnotationMatcherTest extends TestBase {

	@Mock
	private IClassPoolAnalyzer classPoolAnalyzer;

	@Mock
	private IInheritanceAnalyzer inheritanceAnalyzer;

	private UnregisteredSensorConfig unregisteredSensorConfig;

	private IMatcher matcher;

	private IMatcher delegateMatcher;

	public AnnotationMatcherTest() {
		super();
	}

	@TestAnnotation
	public AnnotationMatcherTest(int dummy) {
		super();
	}

	@BeforeMethod
	public void initTestClass() {
		unregisteredSensorConfig = new UnregisteredSensorConfig(classPoolAnalyzer, inheritanceAnalyzer);
		unregisteredSensorConfig.setIgnoreSignature(false);
		unregisteredSensorConfig.setInterface(false);
		unregisteredSensorConfig.setSuperclass(false);
		unregisteredSensorConfig.setTargetPackageName("");
		unregisteredSensorConfig.setTargetClassName(this.getClass().getName());
		unregisteredSensorConfig.setTargetMethodName("*");
		unregisteredSensorConfig.setParameterTypes(Collections.<String> emptyList());
		unregisteredSensorConfig.setPropertyAccess(false);
		unregisteredSensorConfig.setSettings(Collections.<String, Object> emptyMap());

		delegateMatcher = mock(IMatcher.class);
		matcher = new AnnotationMatcher(inheritanceAnalyzer, classPoolAnalyzer, unregisteredSensorConfig, delegateMatcher);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testAnnotatedMethods() throws NotFoundException {
		unregisteredSensorConfig.setAnnotationClassName(TestAnnotation.class.getName());

		ClassLoader classLoader = this.getClass().getClassLoader();
		ClassPool classPool = ClassPool.getDefault();
		CtClass ctClass = classPool.get(this.getClass().getName());
		CtMethod[] ctMethods = ctClass.getDeclaredMethods();

		when(delegateMatcher.getMatchingMethods(classLoader, this.getClass().getName())).thenReturn(Arrays.asList(ctMethods));
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(classPool);

		Iterator<CtClass> iterator = mock(Iterator.class);
		when(iterator.hasNext()).thenReturn(false);
		when(inheritanceAnalyzer.getSuperclassIterator(classLoader, this.getClass().getName())).thenReturn(iterator);
		when(inheritanceAnalyzer.getInterfaceIterator(classLoader, this.getClass().getName())).thenReturn(iterator);

		// execute the test call
		List<CtMethod> ctMethodList = matcher.getMatchingMethods(classLoader, this.getClass().getName());
		assertThat(ctMethodList, is(notNullValue()));
		assertThat(ctMethodList, is(not(empty())));
		for (CtMethod method : ctMethodList) {
			assertThat(method.hasAnnotation(TestAnnotation.class), is(true));
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testAnnotatedConstructors() throws NotFoundException {
		unregisteredSensorConfig.setAnnotationClassName(TestAnnotation.class.getName());

		ClassLoader classLoader = this.getClass().getClassLoader();
		ClassPool classPool = ClassPool.getDefault();
		CtClass ctClass = classPool.get(this.getClass().getName());
		CtConstructor[] ctConstructors = ctClass.getDeclaredConstructors();

		when(delegateMatcher.getMatchingConstructors(classLoader, this.getClass().getName())).thenReturn(Arrays.asList(ctConstructors));
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(classPool);

		Iterator<CtClass> iterator = mock(Iterator.class);
		when(iterator.hasNext()).thenReturn(false);
		when(inheritanceAnalyzer.getSuperclassIterator(classLoader, this.getClass().getName())).thenReturn(iterator);
		when(inheritanceAnalyzer.getInterfaceIterator(classLoader, this.getClass().getName())).thenReturn(iterator);

		// execute the test call
		List<CtConstructor> ctConstructorList = matcher.getMatchingConstructors(classLoader, this.getClass().getName());
		assertThat(ctConstructorList, is(notNullValue()));
		assertThat(ctConstructorList, is(not(empty())));
		for (CtConstructor constructor : ctConstructorList) {
			assertThat(constructor.hasAnnotation(TestAnnotation.class), is(true));
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testAnnotatedClass() throws NotFoundException {
		unregisteredSensorConfig.setAnnotationClassName(TestAnnotation.class.getName());

		ClassLoader classLoader = this.getClass().getClassLoader();
		ClassPool classPool = ClassPool.getDefault();
		CtClass ctClass = classPool.get(TestClass.class.getName());

		// test the methods of annotated class (all should be loaded)
		CtMethod[] ctMethods = ctClass.getDeclaredMethods();
		when(delegateMatcher.getMatchingMethods(classLoader, TestClass.class.getName())).thenReturn(Arrays.asList(ctMethods));
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(classPool);

		Iterator<CtClass> iterator = mock(Iterator.class);
		when(iterator.hasNext()).thenReturn(false);
		when(inheritanceAnalyzer.getSuperclassIterator(classLoader, this.getClass().getName())).thenReturn(iterator);
		when(inheritanceAnalyzer.getInterfaceIterator(classLoader, this.getClass().getName())).thenReturn(iterator);

		// execute the test call
		List<CtMethod> ctMethodList = matcher.getMatchingMethods(classLoader, TestClass.class.getName());
		assertThat(ctMethodList, is(notNullValue()));
		assertThat(ctMethodList, is(not(empty())));
		assertThat(ctMethodList, hasSize(ctMethods.length));

		// test the constructors of annotated class (all should be loaded)
		CtConstructor[] ctConstructors = ctClass.getDeclaredConstructors();
		when(delegateMatcher.getMatchingConstructors(classLoader, TestClass.class.getName())).thenReturn(Arrays.asList(ctConstructors));
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(classPool);

		// execute the test call
		List<CtConstructor> ctConstructorList = matcher.getMatchingConstructors(classLoader, TestClass.class.getName());
		assertThat(ctConstructorList, is(notNullValue()));
		assertThat(ctConstructorList, is(not(empty())));
		assertThat(ctConstructorList, hasSize(ctConstructors.length));
	}

	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testSuperclassAnnotation() throws NotFoundException {
		unregisteredSensorConfig.setAnnotationClassName(TestAnnotation.class.getName());

		ClassLoader classLoader = this.getClass().getClassLoader();
		ClassPool classPool = ClassPool.getDefault();
		CtClass ctClass = classPool.get(ExtendedTestClass.class.getName());

		// test the methods of annotated class (all should be loaded)
		CtMethod[] ctMethods = ctClass.getDeclaredMethods();
		when(delegateMatcher.getMatchingMethods(classLoader, ExtendedTestClass.class.getName())).thenReturn(Arrays.asList(ctMethods));
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(classPool);

		Iterator iterator = mock(Iterator.class);
		when(iterator.hasNext()).thenReturn(true).thenReturn(false);
		when(iterator.next()).thenReturn(classPool.get(ExtendedTestClass.class.getSuperclass().getName()));
		when(inheritanceAnalyzer.getSuperclassIterator(classLoader, ExtendedTestClass.class.getName())).thenReturn(iterator);
		when(inheritanceAnalyzer.getInterfaceIterator(classLoader, ExtendedTestClass.class.getName())).thenReturn(new ArrayList().iterator());

		// execute the test call
		List<CtMethod> ctMethodList = matcher.getMatchingMethods(classLoader, ExtendedTestClass.class.getName());
		assertThat(ctMethodList, is(notNullValue()));
		assertThat(ctMethodList, is(not(empty())));
		assertThat(ctMethodList, hasSize(ctMethods.length));

		// test the constructors of annotated class (all should be loaded)
		CtConstructor[] ctConstructors = ctClass.getDeclaredConstructors();
		when(delegateMatcher.getMatchingConstructors(classLoader, ExtendedTestClass.class.getName())).thenReturn(Arrays.asList(ctConstructors));
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(classPool);

		iterator = mock(Iterator.class);
		when(iterator.hasNext()).thenReturn(true).thenReturn(false);
		when(iterator.next()).thenReturn(classPool.get(ExtendedTestClass.class.getSuperclass().getName()));
		when(inheritanceAnalyzer.getSuperclassIterator(classLoader, ExtendedTestClass.class.getName())).thenReturn(iterator);
		when(inheritanceAnalyzer.getInterfaceIterator(classLoader, ExtendedTestClass.class.getName())).thenReturn(new ArrayList().iterator());

		// execute the test call
		List<CtConstructor> ctConstructorList = matcher.getMatchingConstructors(classLoader, ExtendedTestClass.class.getName());
		assertThat(ctConstructorList, is(notNullValue()));
		assertThat(ctConstructorList, is(not(empty())));
		assertThat(ctConstructorList, hasSize(ctConstructors.length));
	}

	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testInterfaceAnnotation() throws NotFoundException {
		unregisteredSensorConfig.setAnnotationClassName(TestAnnotation.class.getName());

		ClassLoader classLoader = this.getClass().getClassLoader();
		ClassPool classPool = ClassPool.getDefault();
		CtClass ctClass = classPool.get(InterfaceImplTest.class.getName());

		// test the methods of annotated class (all should be loaded)
		CtMethod[] ctMethods = ctClass.getDeclaredMethods();
		when(delegateMatcher.getMatchingMethods(classLoader, InterfaceImplTest.class.getName())).thenReturn(Arrays.asList(ctMethods));
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(classPool);

		Iterator iterator = mock(Iterator.class);
		when(iterator.hasNext()).thenReturn(true).thenReturn(false);
		when(iterator.next()).thenReturn(classPool.get(TestInterface.class.getName()));
		when(inheritanceAnalyzer.getSuperclassIterator(classLoader, InterfaceImplTest.class.getName())).thenReturn(new ArrayList().iterator());
		when(inheritanceAnalyzer.getInterfaceIterator(classLoader, InterfaceImplTest.class.getName())).thenReturn(iterator);

		// execute the test call
		List<CtMethod> ctMethodList = matcher.getMatchingMethods(classLoader, InterfaceImplTest.class.getName());
		assertThat(ctMethodList, is(notNullValue()));
		assertThat(ctMethodList, is(not(empty())));
		assertThat(ctMethodList, hasSize(ctMethods.length));

		// test the constructors of annotated class (all should be loaded)
		CtConstructor[] ctConstructors = ctClass.getDeclaredConstructors();
		when(delegateMatcher.getMatchingConstructors(classLoader, InterfaceImplTest.class.getName())).thenReturn(Arrays.asList(ctConstructors));
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(classPool);

		iterator = mock(Iterator.class);
		when(iterator.hasNext()).thenReturn(true).thenReturn(false);
		when(iterator.next()).thenReturn(classPool.get(TestInterface.class.getName()));
		when(inheritanceAnalyzer.getSuperclassIterator(classLoader, InterfaceImplTest.class.getName())).thenReturn(new ArrayList().iterator());
		when(inheritanceAnalyzer.getInterfaceIterator(classLoader, InterfaceImplTest.class.getName())).thenReturn(iterator);

		// execute the test call
		List<CtConstructor> ctConstructorList = matcher.getMatchingConstructors(classLoader, InterfaceImplTest.class.getName());
		assertThat(ctConstructorList, is(notNullValue()));
		assertThat(ctConstructorList, is(not(empty())));
		assertThat(ctConstructorList, hasSize(ctConstructors.length));
	}

	public static @interface TestAnnotation {

	}

	@TestAnnotation
	public void testMethod() {

	}

	@TestAnnotation
	public static class TestClass {

		public void dummyMethod() {
		}

	}

	@TestAnnotation
	public interface TestInterface {

	}

	public static class ExtendedTestClass extends TestClass {

		public void dummyMethod() {
		}
	}

	public static class InterfaceImplTest implements TestInterface {

		public void dummyMethod() {
		}
	}
}
