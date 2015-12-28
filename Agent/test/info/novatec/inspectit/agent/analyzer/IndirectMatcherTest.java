package info.novatec.inspectit.agent.analyzer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import info.novatec.inspectit.agent.TestBase;
import info.novatec.inspectit.agent.analyzer.impl.IndirectMatcher;
import info.novatec.inspectit.agent.config.impl.UnregisteredSensorConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

@SuppressWarnings("PMD")
public class IndirectMatcherTest extends TestBase {

	@Mock
	private IClassPoolAnalyzer classPoolAnalyzer;

	private UnregisteredSensorConfig unregisteredSensorConfig;

	private IMatcher matcher;

	@BeforeMethod
	public void initTestClass() {
		unregisteredSensorConfig = new UnregisteredSensorConfig(classPoolAnalyzer, mock(IInheritanceAnalyzer.class));
		unregisteredSensorConfig.setIgnoreSignature(false);
		unregisteredSensorConfig.setInterface(false);
		unregisteredSensorConfig.setSuperclass(false);
		unregisteredSensorConfig.setTargetPackageName("");
		unregisteredSensorConfig.setTargetClassName("info.novatec.test.*");
		unregisteredSensorConfig.setTargetMethodName("t*Method");
		List<String> parameterList = new ArrayList<String>();
		parameterList.add("*String");
		unregisteredSensorConfig.setParameterTypes(parameterList);
		unregisteredSensorConfig.setPropertyAccess(false);
		unregisteredSensorConfig.setSettings(Collections.<String, Object> emptyMap());

		matcher = new IndirectMatcher(classPoolAnalyzer, unregisteredSensorConfig);
	}

	@Test
	public void compareClassName() throws NotFoundException {
		boolean compareResult = matcher.compareClassName(this.getClass().getClassLoader(), "info.novatec.test.Test");
		assertThat(compareResult, is(true));

		verifyZeroInteractions(classPoolAnalyzer);
	}

	@Test
	public void failCompareClassName() throws NotFoundException {
		boolean compareResult = matcher.compareClassName(this.getClass().getClassLoader(), "info.novatec.fail.Test");
		assertThat(compareResult, is(false));

		verifyZeroInteractions(classPoolAnalyzer);
	}

	@Test
	public void emptyClassName() throws NotFoundException {
		boolean compareResult = matcher.compareClassName(this.getClass().getClassLoader(), "");
		assertThat(compareResult, is(false));

		verifyZeroInteractions(classPoolAnalyzer);
	}

	@Test
	public void regexClassName() throws NotFoundException {
		boolean compareResult = matcher.compareClassName(this.getClass().getClassLoader(), "*");
		assertThat(compareResult, is(false));

		verifyZeroInteractions(classPoolAnalyzer);
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

	@SuppressWarnings("unchecked")
	@Test
	public void getMatchingMethodsNoParameter() throws NotFoundException {
		// no parameters for this test
		unregisteredSensorConfig.setParameterTypes(Collections.EMPTY_LIST);

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
		assertThat(ctMethodList.get(0).getParameterTypes().length, is(equalTo(0)));

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
		assertThat(ctMethodList.get(0).getParameterTypes().length, is(equalTo(0)));
		assertThat(ctMethodList.get(1).getParameterTypes().length, is(equalTo(1)));
		assertThat(ctMethodList.get(1).getParameterTypes()[0].getName(), is(equalTo("java.lang.String")));

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
