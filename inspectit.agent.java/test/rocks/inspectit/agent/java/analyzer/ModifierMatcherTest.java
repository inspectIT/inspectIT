package info.novatec.inspectit.agent.analyzer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import info.novatec.inspectit.agent.TestBase;
import info.novatec.inspectit.agent.analyzer.impl.ModifierMatcher;
import info.novatec.inspectit.agent.config.impl.UnregisteredSensorConfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;

@SuppressWarnings("PMD")
public class ModifierMatcherTest extends TestBase {

	@Mock
	private IClassPoolAnalyzer classPoolAnalyzer;

	private UnregisteredSensorConfig unregisteredSensorConfig;

	private IMatcher matcher;

	private IMatcher delegateMatcher;

	public ModifierMatcherTest() {
		super();
	}

	@SuppressWarnings("unused")
	private ModifierMatcherTest(int dummy) {
		super();
	}

	@BeforeMethod
	public void initTestClass() {
		unregisteredSensorConfig = new UnregisteredSensorConfig(classPoolAnalyzer, mock(IInheritanceAnalyzer.class));
		unregisteredSensorConfig.setIgnoreSignature(false);
		unregisteredSensorConfig.setInterface(false);
		unregisteredSensorConfig.setSuperclass(false);
		unregisteredSensorConfig.setTargetPackageName("");
		unregisteredSensorConfig.setTargetClassName("info.novatec.test.Test");
		unregisteredSensorConfig.setTargetMethodName("t*Method");
		unregisteredSensorConfig.setParameterTypes(Collections.<String> emptyList());
		unregisteredSensorConfig.setPropertyAccess(false);
		unregisteredSensorConfig.setSettings(Collections.<String, Object> emptyMap());

		delegateMatcher = mock(IMatcher.class);
		matcher = new ModifierMatcher(classPoolAnalyzer, unregisteredSensorConfig, delegateMatcher);
	}

	@SuppressWarnings("unused")
	private void testPrivateMethod() {
	}

	protected void testProtectedMethod() {
	}

	public void testPublicMethod() {
	}

	void testDefaultMethod() {
	}

	@Test
	public void testPrivate() throws NotFoundException {
		unregisteredSensorConfig.setModifiers(Modifier.PRIVATE);

		ClassLoader classLoader = this.getClass().getClassLoader();
		ClassPool classPool = ClassPool.getDefault();
		CtClass ctClass = classPool.get(this.getClass().getName());
		CtMethod[] ctMethods = ctClass.getDeclaredMethods();

		// stub the delegateMatcher method
		when(delegateMatcher.getMatchingMethods(classLoader, "info.novatec.test.Test")).thenReturn(Arrays.asList(ctMethods));

		// execute the test call
		List<CtMethod> ctMethodList = matcher.getMatchingMethods(classLoader, "info.novatec.test.Test");
		assertThat(ctMethodList, is(notNullValue()));
		assertThat(ctMethodList, is(not(empty())));
		for (CtMethod method : ctMethodList) {
			assertThat(Modifier.isPrivate(method.getModifiers()), is(true));
		}
	}

	@Test
	public void testProtected() throws NotFoundException {
		unregisteredSensorConfig.setModifiers(Modifier.PROTECTED);

		ClassLoader classLoader = this.getClass().getClassLoader();
		ClassPool classPool = ClassPool.getDefault();
		CtClass ctClass = classPool.get(this.getClass().getName());
		CtMethod[] ctMethods = ctClass.getDeclaredMethods();

		// stub the delegateMatcher method
		when(delegateMatcher.getMatchingMethods(classLoader, "info.novatec.test.Test")).thenReturn(Arrays.asList(ctMethods));

		// execute the test call
		List<CtMethod> ctMethodList = matcher.getMatchingMethods(classLoader, "info.novatec.test.Test");
		assertThat(ctMethodList, is(notNullValue()));
		assertThat(ctMethodList, is(not(empty())));
		for (CtMethod method : ctMethodList) {
			assertThat(Modifier.isProtected(method.getModifiers()), is(true));
		}

	}

	@Test
	public void testPublic() throws NotFoundException {
		unregisteredSensorConfig.setModifiers(Modifier.PUBLIC);

		ClassLoader classLoader = this.getClass().getClassLoader();
		ClassPool classPool = ClassPool.getDefault();
		CtClass ctClass = classPool.get(this.getClass().getName());
		CtMethod[] ctMethods = ctClass.getDeclaredMethods();

		// stub the delegateMatcher method
		when(delegateMatcher.getMatchingMethods(classLoader, "info.novatec.test.Test")).thenReturn(Arrays.asList(ctMethods));

		// execute the test call
		List<CtMethod> ctMethodList = matcher.getMatchingMethods(classLoader, "info.novatec.test.Test");
		assertThat(ctMethodList, is(notNullValue()));
		assertThat(ctMethodList, is(not(empty())));
		for (CtMethod method : ctMethodList) {
			assertThat(Modifier.isPublic(method.getModifiers()), is(true));
		}

	}

	@Test
	public void testDefault() throws NotFoundException {
		unregisteredSensorConfig.setModifiers(ModifierMatcher.DEFAULT);

		ClassLoader classLoader = this.getClass().getClassLoader();
		ClassPool classPool = ClassPool.getDefault();
		CtClass ctClass = classPool.get(this.getClass().getName());
		CtMethod[] ctMethods = ctClass.getDeclaredMethods();

		// stub the delegateMatcher method
		when(delegateMatcher.getMatchingMethods(classLoader, "info.novatec.test.Test")).thenReturn(Arrays.asList(ctMethods));

		// execute the test call
		List<CtMethod> ctMethodList = matcher.getMatchingMethods(classLoader, "info.novatec.test.Test");
		assertThat(ctMethodList, is(notNullValue()));
		assertThat(ctMethodList, is(not(empty())));
		for (CtMethod method : ctMethodList) {
			assertThat(Modifier.isPackage(method.getModifiers()), is(true));
		}

	}

	@Test
	public void testCombined() throws NotFoundException {
		unregisteredSensorConfig.setModifiers(Modifier.PRIVATE | Modifier.PROTECTED);

		ClassLoader classLoader = this.getClass().getClassLoader();
		ClassPool classPool = ClassPool.getDefault();
		CtClass ctClass = classPool.get(this.getClass().getName());
		CtMethod[] ctMethods = ctClass.getDeclaredMethods();

		// stub the delegateMatcher method
		when(delegateMatcher.getMatchingMethods(classLoader, "info.novatec.test.Test")).thenReturn(Arrays.asList(ctMethods));

		// execute the test call
		List<CtMethod> ctMethodList = matcher.getMatchingMethods(classLoader, "info.novatec.test.Test");
		assertThat(ctMethodList, is(notNullValue()));
		assertThat(ctMethodList, is(not(empty())));
		for (CtMethod method : ctMethodList) {
			assertThat(Modifier.isPrivate(method.getModifiers()) || Modifier.isProtected(method.getModifiers()), is(true));
		}

	}

	@Test
	public void testConstructor() throws NotFoundException {
		unregisteredSensorConfig.setModifiers(Modifier.PRIVATE);

		ClassLoader classLoader = this.getClass().getClassLoader();
		ClassPool classPool = ClassPool.getDefault();
		CtClass ctClass = classPool.get(this.getClass().getName());
		CtConstructor[] ctConstructors = ctClass.getDeclaredConstructors();

		// stub the delegateMatcher method
		when(delegateMatcher.getMatchingConstructors(classLoader, "info.novatec.test.Test")).thenReturn(Arrays.asList(ctConstructors));

		// execute the test call
		List<CtConstructor> ctConstructorList = matcher.getMatchingConstructors(classLoader, "info.novatec.test.Test");
		assertThat(ctConstructorList, is(notNullValue()));
		assertThat(ctConstructorList, is(not(empty())));
		for (CtConstructor constructor : ctConstructorList) {
			assertThat(Modifier.isPrivate(constructor.getModifiers()), is(true));
		}

	}
}
