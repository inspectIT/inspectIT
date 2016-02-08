package info.novatec.inspectit.agent.analyzer.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import info.novatec.inspectit.agent.TestBase;
import info.novatec.inspectit.agent.analyzer.IClassPoolAnalyzer;
import info.novatec.inspectit.agent.analyzer.classes.AbstractSubTest;
import info.novatec.inspectit.agent.analyzer.classes.AbstractTest;
import info.novatec.inspectit.agent.analyzer.classes.ISubTest;
import info.novatec.inspectit.agent.analyzer.classes.ITest;
import info.novatec.inspectit.agent.analyzer.classes.ITestTwo;
import info.novatec.inspectit.agent.analyzer.classes.MyTestError;
import info.novatec.inspectit.agent.analyzer.classes.MyTestException;
import info.novatec.inspectit.agent.analyzer.classes.TestClass;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mockito.Mock;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

@SuppressWarnings("PMD")
public class InheritanceAnalyzerTest extends TestBase {

	@Mock
	private IClassPoolAnalyzer classPoolAnalyzer;

	private InheritanceAnalyzer inheritanceAnalyzer;

	@BeforeMethod
	public void initTestClass() {
		inheritanceAnalyzer = new InheritanceAnalyzer(classPoolAnalyzer);
		inheritanceAnalyzer.log = LoggerFactory.getLogger(InheritanceAnalyzer.class);
	}

	@Test
	public void getSuperclassIterator() throws NotFoundException {
		// set up everything
		ClassLoader classLoader = TestClass.class.getClassLoader();
		String className = TestClass.class.getName();
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(ClassPool.getDefault());

		// main test
		Iterator<CtClass> iterator = inheritanceAnalyzer.getSuperclassIterator(classLoader, className);
		assertThat(iterator, is(notNullValue()));
		assertThat(iterator.hasNext(), is(true));
		CtClass superclass = iterator.next();
		assertThat(superclass.getName(), is(equalTo(AbstractSubTest.class.getName())));

		assertThat(iterator.hasNext(), is(true));
		superclass = iterator.next();
		assertThat(superclass.getName(), is(equalTo(AbstractTest.class.getName())));

		assertThat(iterator.hasNext(), is(true));
		superclass = iterator.next();
		assertThat(superclass.getName(), is(equalTo(Object.class.getName())));

		assertThat(iterator.hasNext(), is(false));

		verify(classPoolAnalyzer, times(1)).getClassPool(classLoader);
		verifyNoMoreInteractions(classPoolAnalyzer);
	}

	@Test
	public void getInterfaceIterator() throws NotFoundException {
		// set up everything
		ClassLoader classLoader = TestClass.class.getClassLoader();
		String className = TestClass.class.getName();
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(ClassPool.getDefault());

		List<CtClass> interfaceList = new ArrayList<CtClass>(3);
		interfaceList.add(ClassPool.getDefault().get(ITestTwo.class.getName()));
		interfaceList.add(ClassPool.getDefault().get(ISubTest.class.getName()));
		interfaceList.add(ClassPool.getDefault().get(ITest.class.getName()));

		// main test
		Iterator<CtClass> iterator = inheritanceAnalyzer.getInterfaceIterator(classLoader, className);
		assertThat(iterator, is(notNullValue()));

		assertThat(iterator.hasNext(), is(true));
		CtClass interfaceCtClass = iterator.next();
		assertThat(interfaceList, hasItem(interfaceCtClass));
		interfaceList.remove(interfaceCtClass);

		assertThat(iterator.hasNext(), is(true));
		interfaceCtClass = iterator.next();
		assertThat(interfaceList, hasItem(interfaceCtClass));
		interfaceList.remove(interfaceCtClass);

		assertThat(iterator.hasNext(), is(true));
		interfaceCtClass = iterator.next();
		assertThat(interfaceList, hasItem(interfaceCtClass));
		interfaceList.remove(interfaceCtClass);

		assertThat(iterator.hasNext(), is(false));

		verify(classPoolAnalyzer, times(1)).getClassPool(classLoader);
		verifyNoMoreInteractions(classPoolAnalyzer);
	}

	@Test(expectedExceptions = { NotFoundException.class })
	public void superclassNotFound() throws NotFoundException {
		// set up everything
		ClassLoader classLoader = this.getClass().getClassLoader();
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(ClassPool.getDefault());

		inheritanceAnalyzer.getSuperclassIterator(classLoader, "xxx");
	}

	@Test(expectedExceptions = { NotFoundException.class })
	public void interfaceNotFound() throws NotFoundException {
		// set up everything
		ClassLoader classLoader = this.getClass().getClassLoader();
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(ClassPool.getDefault());

		inheritanceAnalyzer.getInterfaceIterator(classLoader, "xxx");
	}

	@Test(expectedExceptions = { NotFoundException.class })
	public void superclassNullClassName() throws NotFoundException {
		// set up everything
		ClassLoader classLoader = this.getClass().getClassLoader();
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(ClassPool.getDefault());

		inheritanceAnalyzer.getSuperclassIterator(classLoader, null);
	}

	@Test(expectedExceptions = { NotFoundException.class })
	public void interfaceNullClassName() throws NotFoundException {
		// set up everything
		ClassLoader classLoader = this.getClass().getClassLoader();
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(ClassPool.getDefault());

		inheritanceAnalyzer.getInterfaceIterator(classLoader, null);
	}

	@Test
	public void superclassEmptyResult() throws NotFoundException {
		// set up everything
		ClassLoader classLoader = Object.class.getClassLoader();
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(ClassPool.getDefault());

		Iterator<CtClass> iterator = inheritanceAnalyzer.getSuperclassIterator(classLoader, Object.class.getName());
		assertThat(iterator, is(notNullValue()));
		assertThat(iterator.hasNext(), is(false));

		verify(classPoolAnalyzer, times(1)).getClassPool(classLoader);
		verifyNoMoreInteractions(classPoolAnalyzer);
	}

	@Test
	public void interfaceEmptyResult() throws NotFoundException {
		// set up everything
		ClassLoader classLoader = Object.class.getClassLoader();
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(ClassPool.getDefault());

		Iterator<CtClass> iterator = inheritanceAnalyzer.getInterfaceIterator(classLoader, Object.class.getName());
		assertThat(iterator, is(notNullValue()));
		assertThat(iterator.hasNext(), is(false));

		verify(classPoolAnalyzer, times(1)).getClassPool(classLoader);
		verifyNoMoreInteractions(classPoolAnalyzer);
	}

	@Test
	public void subclassOfThrowable() {
		// set up everything
		ClassLoader classLoader = MyTestException.class.getClassLoader();
		String className = MyTestException.class.getName();
		ClassPool classPool = ClassPool.getDefault();
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(classPool);

		// main test
		boolean subclassOfThrowable = inheritanceAnalyzer.subclassOf(className, "java.lang.Throwable", classPool);
		assertThat(subclassOfThrowable, is(true));

		boolean subclassOfException = inheritanceAnalyzer.subclassOf(className, "java.lang.Exception", classPool);
		assertThat(subclassOfException, is(true));
	}

	@Test
	public void subclassOfError() {
		// set up everything
		ClassLoader classLoader = MyTestError.class.getClassLoader();
		String className = MyTestError.class.getName();
		ClassPool classPool = ClassPool.getDefault();
		when(classPoolAnalyzer.getClassPool(classLoader)).thenReturn(classPool);

		boolean subclassOfError = inheritanceAnalyzer.subclassOf(className, "java.lang.Error", classPool);
		assertThat(subclassOfError, is(true));
	}

}
