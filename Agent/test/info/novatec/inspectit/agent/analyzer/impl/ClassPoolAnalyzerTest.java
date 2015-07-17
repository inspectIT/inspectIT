package info.novatec.inspectit.agent.analyzer.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import info.novatec.inspectit.agent.AbstractLogSupport;
import info.novatec.inspectit.agent.analyzer.classes.TestClass;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

import javassist.ClassPool;
import javassist.CtMethod;

import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("PMD")
public class ClassPoolAnalyzerTest extends AbstractLogSupport {

	private ClassPoolAnalyzer classPoolAnalyzer;

	@BeforeMethod
	public void init() {
		classPoolAnalyzer = new ClassPoolAnalyzer();
		classPoolAnalyzer.log = LoggerFactory.getLogger(ClassPoolAnalyzer.class);
	}

	@Test
	public void getMethodsForClassName() {
		CtMethod[] ctMethods = classPoolAnalyzer.getMethodsForClassName(TestClass.class.getClassLoader(), TestClass.class.getName());
		assertThat(ctMethods, is(notNullValue()));
		assertThat(ctMethods.length, is(equalTo(TestClass.class.getDeclaredMethods().length)));
	}

	@Test
	public void getMethodsForClassNameNullClassLoader() {
		CtMethod[] ctMethods = classPoolAnalyzer.getMethodsForClassName(null, TestClass.class.getName());
		assertThat(ctMethods, is(notNullValue()));
		assertThat(ctMethods.length, is(equalTo(TestClass.class.getDeclaredMethods().length)));
	}

	@Test
	public void getMethodsForClassNameNullClassName() {
		CtMethod[] ctMethods = classPoolAnalyzer.getMethodsForClassName(TestClass.class.getClassLoader(), null);
		assertThat(ctMethods, is(notNullValue()));
		assertThat(ctMethods.length, is(equalTo(0)));
	}

	@Test
	public void getMethodsForClassNameEmptyClassName() {
		CtMethod[] ctMethods = classPoolAnalyzer.getMethodsForClassName(TestClass.class.getClassLoader(), "");
		assertThat(ctMethods, is(notNullValue()));
		assertThat(ctMethods.length, is(equalTo(0)));
	}

	@Test
	public void equalClassPool() {
		ClassPool classPool = classPoolAnalyzer.addClassLoader(TestClass.class.getClassLoader());
		assertThat(classPool, is(notNullValue()));
		ClassPool otherClassPool = classPoolAnalyzer.getClassPool(TestClass.class.getClassLoader());
		assertThat(otherClassPool, is(notNullValue()));
		assertThat(classPool, is(equalTo(otherClassPool)));
	}

	@Test
	public void extClassLoaderParent() {
		ClassPool classPool = classPoolAnalyzer.getClassPool(TestClass.class.getClassLoader());
		assertThat(classPool, is(notNullValue()));
		ClassPool appClassPool = classPoolAnalyzer.getClassPool(TestClass.class.getClassLoader().getParent());
		assertThat(appClassPool, is(notNullValue()));
		assertThat(appClassPool.getClassLoader().toString(), containsString("AppClassLoader"));
	}

	private class TestClassLoader extends ClassLoader {
		public TestClassLoader(ClassLoader parent) {
			super(parent);
		}
	}

	@Test
	public void classLoaderHierarchy() throws Exception {
		TestClassLoader testClassLoader = new TestClassLoader(TestClass.class.getClassLoader());
		TestClassLoader subTestClassLoader = new TestClassLoader(testClassLoader);
		TestClassLoader subSubTestClassLoader = new TestClassLoader(subTestClassLoader);

		classPoolAnalyzer.addClassLoader(subSubTestClassLoader);

		ClassPool classPool = classPoolAnalyzer.getClassPool(subSubTestClassLoader);
		assertThat(getClassLoader(classPool), is(equalTo((ClassLoader) subSubTestClassLoader)));
		ClassPool parentClassPool = getParentClassPool(classPool);
		assertThat(getClassLoader(parentClassPool), is(equalTo((ClassLoader) subTestClassLoader)));
		ClassPool parentParentClassPool = getParentClassPool(parentClassPool);
		assertThat(getClassLoader(parentParentClassPool), is(equalTo((ClassLoader) testClassLoader)));
	}

	private ClassPool getParentClassPool(ClassPool classPool) throws Exception {
		// only possible through reflection :(
		Field field = ClassPool.class.getDeclaredField("parent");
		field.setAccessible(true);
		return (ClassPool) field.get(classPool);
	}

	@SuppressWarnings("unchecked")
	private ClassLoader getClassLoader(ClassPool classPool) throws Exception {
		// more reflection, yay!
		Field field = ClassPool.class.getDeclaredField("source");
		field.setAccessible(true);
		Object classPoolTail = field.get(classPool);
		field = classPoolTail.getClass().getDeclaredField("pathList");
		field.setAccessible(true);
		Object classPathList = field.get(classPoolTail);
		field = classPathList.getClass().getDeclaredField("path");
		field.setAccessible(true);
		Object classPath = field.get(classPathList);
		field = classPath.getClass().getDeclaredField("clref");
		field.setAccessible(true);
		WeakReference<ClassLoader> weakReference = (WeakReference<ClassLoader>) field.get(classPath);
		return weakReference.get();
	}

}
