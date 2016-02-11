package info.novatec.inspectit.agent.analyzer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import info.novatec.inspectit.agent.TestBase;
import info.novatec.inspectit.agent.analyzer.impl.InterfaceMatcher;
import info.novatec.inspectit.agent.config.impl.UnregisteredSensorConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

@SuppressWarnings("PMD")
public class InterfaceMatcherTest extends TestBase {

	@Mock
	private IClassPoolAnalyzer classPoolAnalyzer;

	@Mock
	private IInheritanceAnalyzer inheritanceAnalyzer;

	private UnregisteredSensorConfig unregisteredSensorConfig;

	private IMatcher matcher;

	@BeforeMethod
	public void initTestClass() {
		unregisteredSensorConfig = new UnregisteredSensorConfig(classPoolAnalyzer, mock(IInheritanceAnalyzer.class));
		unregisteredSensorConfig.setIgnoreSignature(false);
		unregisteredSensorConfig.setInterface(true);
		unregisteredSensorConfig.setSuperclass(false);
		unregisteredSensorConfig.setTargetPackageName("");
		unregisteredSensorConfig.setTargetClassName("info.novatec.test.ITest");
		unregisteredSensorConfig.setTargetMethodName("testMethod");
		List<String> parameterList = new ArrayList<String>();
		parameterList.add("java.lang.String");
		unregisteredSensorConfig.setParameterTypes(parameterList);
		unregisteredSensorConfig.setPropertyAccess(false);
		unregisteredSensorConfig.setSettings(Collections.<String, Object> emptyMap());

		matcher = new InterfaceMatcher(inheritanceAnalyzer, classPoolAnalyzer, unregisteredSensorConfig);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void compareClassName() throws NotFoundException {
		ClassLoader classLoader = this.getClass().getClassLoader();
		Iterator<CtClass> iterator = mock(Iterator.class);
		when(iterator.hasNext()).thenReturn(true).thenReturn(false);
		CtClass ctClass = mock(CtClass.class);
		when(ctClass.getName()).thenReturn("info.novatec.test.ITest");
		when(iterator.next()).thenReturn(ctClass);

		String className = "info.novatec.test.Test";
		when(inheritanceAnalyzer.getInterfaceIterator(classLoader, className)).thenReturn(iterator);
		boolean compareResult = matcher.compareClassName(this.getClass().getClassLoader(), className);
		assertThat(compareResult, is(true));

		verifyZeroInteractions(classPoolAnalyzer);
		verify(inheritanceAnalyzer, times(1)).getInterfaceIterator(classLoader, className);
		verifyNoMoreInteractions(inheritanceAnalyzer);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void compareClassNameVirtual() throws NotFoundException {
		unregisteredSensorConfig.setVirtual(true);
		unregisteredSensorConfig.setTargetClassName("info.novatec.test.*");
		// needed as the virtual setting is checked in the constructor of the
		// interface matcher
		matcher = new InterfaceMatcher(inheritanceAnalyzer, classPoolAnalyzer, unregisteredSensorConfig);

		ClassLoader classLoader = this.getClass().getClassLoader();
		Iterator<CtClass> iterator = mock(Iterator.class);
		when(iterator.hasNext()).thenReturn(true).thenReturn(false);
		CtClass ctClass = mock(CtClass.class);
		when(ctClass.getName()).thenReturn("info.novatec.test.ITest");
		when(iterator.next()).thenReturn(ctClass);

		String className = "info.novatec.test.Test";
		when(inheritanceAnalyzer.getInterfaceIterator(classLoader, className)).thenReturn(iterator);
		boolean compareResult = matcher.compareClassName(this.getClass().getClassLoader(), className);
		assertThat(compareResult, is(true));

		verifyZeroInteractions(classPoolAnalyzer);
		verify(inheritanceAnalyzer, times(1)).getInterfaceIterator(classLoader, className);
		verifyNoMoreInteractions(inheritanceAnalyzer);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void failCompareClassName() throws NotFoundException {
		ClassLoader classLoader = this.getClass().getClassLoader();
		Iterator<CtClass> iterator = mock(Iterator.class);
		when(iterator.hasNext()).thenReturn(false);

		when(inheritanceAnalyzer.getInterfaceIterator(eq(classLoader), anyString())).thenReturn(iterator);
		boolean compareResult = matcher.compareClassName(classLoader, "info.novatec.test.Fail");
		assertThat(compareResult, is(false));
		compareResult = matcher.compareClassName(classLoader, "info.novatec.Fail");
		assertThat(compareResult, is(false));
		compareResult = matcher.compareClassName(classLoader, "info.novatec.*");
		assertThat(compareResult, is(false));
		compareResult = matcher.compareClassName(classLoader, "");
		assertThat(compareResult, is(false));

		verifyZeroInteractions(classPoolAnalyzer);
		verify(inheritanceAnalyzer, times(4)).getInterfaceIterator(eq(classLoader), anyString());
		verifyNoMoreInteractions(inheritanceAnalyzer);
	}

	public void testMethod() {
	}

	public void testMethod(String msg) {
	}

	@Test
	public void getMatchingMethods() throws NotFoundException {
		ClassLoader classLoader = this.getClass().getClassLoader();
		CtMethod[] ctMethods = new CtMethod[2];
		ClassPool classPool = ClassPool.getDefault();
		CtClass ctClass = classPool.get(this.getClass().getName());
		ctMethods[0] = ctClass.getDeclaredMethod("testMethod", null);
		ctMethods[1] = ctClass.getDeclaredMethod("testMethod", new CtClass[] { classPool.get("java.lang.String") });

		// stub the getMethodsForClassName method
		when(classPoolAnalyzer.getMethodsForClassName(classLoader, "info.novatec.test.Test")).thenReturn(ctMethods);

		// execute the test call
		List<CtMethod> ctMethodList = matcher.getMatchingMethods(classLoader, "info.novatec.test.Test");
		matcher.checkParameters(ctMethodList);
		assertThat(ctMethodList, is(notNullValue()));
		assertThat(ctMethodList, hasSize(1));
		assertThat(ctMethodList.get(0).getParameterTypes().length, is(equalTo(1)));
		assertThat(ctMethodList.get(0).getParameterTypes()[0].getName(), is(equalTo("java.lang.String")));

		verify(classPoolAnalyzer, times(1)).getMethodsForClassName(classLoader, "info.novatec.test.Test");
		verifyNoMoreInteractions(classPoolAnalyzer);
	}

	@Test
	public void getMatchingMethodsIgnoreSignature() throws NotFoundException {
		// ignore the signature, now the result should contain two methods
		unregisteredSensorConfig.setIgnoreSignature(true);

		ClassLoader classLoader = this.getClass().getClassLoader();
		CtMethod[] ctMethods = new CtMethod[2];
		ClassPool classPool = ClassPool.getDefault();
		CtClass ctClass = classPool.get(this.getClass().getName());
		ctMethods[0] = ctClass.getDeclaredMethod("testMethod", null);
		ctMethods[1] = ctClass.getDeclaredMethod("testMethod", new CtClass[] { classPool.get("java.lang.String") });

		// stub the getMethodsForClassName method
		when(classPoolAnalyzer.getMethodsForClassName(classLoader, "info.novatec.test.Test")).thenReturn(ctMethods);

		// execute the test call
		List<CtMethod> ctMethodList = matcher.getMatchingMethods(classLoader, "info.novatec.test.Test");
		assertThat(ctMethodList, is(notNullValue()));
		assertThat(ctMethodList, hasSize(2));

		verify(classPoolAnalyzer, times(1)).getMethodsForClassName(classLoader, "info.novatec.test.Test");
		verifyNoMoreInteractions(classPoolAnalyzer);
	}

	@Test
	public void getMatchingMethodsNoMethods() throws NotFoundException {
		ClassLoader classLoader = this.getClass().getClassLoader();
		CtMethod[] ctMethods = new CtMethod[0];

		// stub the getMethodsForClassName method
		when(classPoolAnalyzer.getMethodsForClassName(classLoader, "info.novatec.test.Test")).thenReturn(ctMethods);

		// execute the test call
		List<CtMethod> ctMethodList = matcher.getMatchingMethods(classLoader, "info.novatec.test.Test");
		assertThat(ctMethodList, is(notNullValue()));
		assertThat(ctMethodList, is(empty()));

		verify(classPoolAnalyzer, times(1)).getMethodsForClassName(classLoader, "info.novatec.test.Test");
		verifyNoMoreInteractions(classPoolAnalyzer);
	}

}
