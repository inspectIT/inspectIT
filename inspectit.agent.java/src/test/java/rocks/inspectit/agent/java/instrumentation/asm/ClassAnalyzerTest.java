package rocks.inspectit.agent.java.instrumentation.asm;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import info.novatec.inspectit.org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Retention;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.instrumentation.asm.ClassAnalyzer;
import rocks.inspectit.shared.all.instrumentation.classcache.AnnotationType;
import rocks.inspectit.shared.all.instrumentation.classcache.ClassType;
import rocks.inspectit.shared.all.instrumentation.classcache.ImmutableType;
import rocks.inspectit.shared.all.instrumentation.classcache.InterfaceType;
import rocks.inspectit.shared.all.instrumentation.classcache.MethodType;
import rocks.inspectit.shared.all.instrumentation.classcache.Modifiers;

/**
 * Test for the {@link ClassAnalyzer}.
 *
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class ClassAnalyzerTest {

	/**
	 * Hash to be used for testing purposes.
	 */
	protected static final String HASH = "MyHash";

	/**
	 * Class being tested.
	 */
	protected ClassAnalyzer classAnalyzer;

	/**
	 * Init.
	 */
	@BeforeMethod
	public void init() {
		classAnalyzer = new ClassAnalyzer(HASH);
	}

	public class Analyze extends ClassAnalyzerTest {

		/**
		 * Testing the correct parsing of {@link TestAnnotation}.
		 */
		@Test
		public void theAnnotation() throws IOException {
			ClassReader classReader = new ClassReader(TestAnnotation.class.getName());
			classReader.accept(classAnalyzer, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

			ImmutableType immutableType = classAnalyzer.getType();
			assertThat(immutableType, is(instanceOf(AnnotationType.class)));

			AnnotationType type = (AnnotationType) immutableType;

			// name
			assertThat(type.getFQN(), is(equalTo(TestAnnotation.class.getName())));
			assertThat(type.isInitialized(), is(true));

			// modifiers
			assertThat(Modifiers.isPublic(type.getModifiers()), is(true));

			// only one hash when analyzed
			assertThat(type.getHashes(), hasSize(1));
			assertThat(type.getHashes(), hasItem(HASH));

			// @Retention annotation
			assertThat(type.getAnnotations(), hasSize(1));
			assertThat(type.getAnnotations().iterator().next().getFQN(), is(equalTo(Retention.class.getName())));

			// other
			assertThat(type.getAnnotatedTypes(), is(not(nullValue())));
			assertThat(type.getAnnotatedTypes(), is(empty()));
			assertThat(type.getRealizingClasses(), is(not(nullValue())));
			assertThat(type.getRealizingClasses(), is(empty()));
		}

		/**
		 * Testing the correct parsing of {@link TestInterface}.
		 */
		@Test
		public void theInterface() throws IOException {
			ClassReader classReader = new ClassReader(TestInterface.class.getName());
			classReader.accept(classAnalyzer, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

			ImmutableType immutableType = classAnalyzer.getType();
			assertThat(immutableType, is(instanceOf(InterfaceType.class)));

			InterfaceType type = (InterfaceType) immutableType;

			assertThat(type.getFQN(), is(equalTo(TestInterface.class.getName())));
			assertThat(type.isInitialized(), is(true));

			// modifiers
			assertThat(Modifiers.isPublic(type.getModifiers()), is(false));
			assertThat(Modifiers.isPrivate(type.getModifiers()), is(false));
			assertThat(Modifiers.isProtected(type.getModifiers()), is(false));

			// only one hash when analyzed
			assertThat(type.getHashes(), hasSize(1));
			assertThat(type.getHashes(), hasItem(HASH));

			// @TestAnnotation annotation
			assertThat(type.getAnnotations(), hasSize(1));
			AnnotationType annotation = type.getAnnotations().iterator().next();
			assertThat(annotation.getFQN(), is(equalTo(TestAnnotation.class.getName())));
			assertThat(annotation.isInitialized(), is(false));

			// super interface(s)
			assertThat(type.getSuperInterfaces(), hasSize(1));
			InterfaceType superInterface = type.getSuperInterfaces().iterator().next();
			assertThat(superInterface.getFQN(), is(equalTo(Serializable.class.getName())));
			assertThat(superInterface.isInitialized(), is(false));

			// other
			assertThat(type.getRealizingClasses(), is(not(nullValue())));
			assertThat(type.getRealizingClasses(), is(empty()));
			assertThat(type.getSubInterfaces(), is(not(nullValue())));
			assertThat(type.getSubInterfaces(), is(empty()));

			// method
			assertThat(type.getMethods(), hasSize(1));
			MethodType method = type.getMethods().iterator().next();

			assertThat(method.getName(), is(equalTo("method1")));
			assertThat(Modifiers.isPublic(method.getModifiers()), is(true));
			assertThat(method.getMethodCharacter(), is(MethodType.Character.METHOD));
			assertThat(method.getReturnType(), is(String.class.getName()));
			assertThat(method.getAnnotations(), hasSize(1));
			assertThat(method.getAnnotations().iterator().next().getFQN(), is(equalTo(TestAnnotation.class.getName())));
			assertThat(method.getParameters(), hasSize(4));
			assertThat(method.getParameters().get(0), is("int"));
			assertThat(method.getParameters().get(1), is("long[]"));
			assertThat(method.getParameters().get(2), is(String.class.getName()));
			assertThat(method.getParameters().get(3), is(Object.class.getName() + "[][][]"));
		}

		/**
		 * Testing the correct parsing of {@link AbstractTestClass}.
		 */
		@Test
		public void theAbstractClass() throws IOException {
			ClassReader classReader = new ClassReader(AbstractTestClass.class.getName());
			classReader.accept(classAnalyzer, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

			ImmutableType immutableType = classAnalyzer.getType();
			assertThat(immutableType, is(instanceOf(ClassType.class)));

			ClassType type = (ClassType) immutableType;

			assertThat(type.getFQN(), is(equalTo(AbstractTestClass.class.getName())));
			assertThat(type.isInitialized(), is(true));

			// modifiers
			assertThat(Modifiers.isAbstract(type.getModifiers()), is(true));

			// only one hash when analyzed
			assertThat(type.getHashes(), hasSize(1));
			assertThat(type.getHashes(), hasItem(HASH));

			// super class is Object
			assertThat(type.getSuperClasses(), hasSize(1));
			ClassType superClass = type.getSuperClasses().iterator().next();
			assertThat(superClass.getFQN(), is(equalTo(Object.class.getName())));
			assertThat(superClass.isInitialized(), is(false));

			// other
			assertThat(type.getRealizedInterfaces(), is(not(nullValue())));
			assertThat(type.getRealizedInterfaces(), is(empty()));
			assertThat(type.getSubClasses(), is(not(nullValue())));
			assertThat(type.getSubClasses(), is(empty()));

			// note that there is method + no-arg constructor
			assertThat(type.getMethods(), hasSize(2));

			MethodType method = null;
			MethodType constructor = null;

			for (MethodType methodType : type.getMethods()) {
				if (methodType.getMethodCharacter().equals(MethodType.Character.CONSTRUCTOR)) {
					constructor = methodType;
				} else if (methodType.getMethodCharacter().equals(MethodType.Character.METHOD)) {
					method = methodType;
				}
			}

			// check constructor
			assertThat(constructor.getName(), is(equalTo("<init>")));
			assertThat(constructor.getMethodCharacter(), is(MethodType.Character.CONSTRUCTOR));
			assertThat(constructor.getReturnType(), is("void"));
			assertThat(constructor.getAnnotations(), is(empty()));
			assertThat(constructor.getParameters(), is(empty()));

			// check method
			assertThat(method.getName(), is(equalTo("method0")));
			assertThat(Modifiers.isProtected(method.getModifiers()), is(true));
			assertThat(Modifiers.isAbstract(method.getModifiers()), is(true));
			assertThat(method.getMethodCharacter(), is(MethodType.Character.METHOD));
			assertThat(method.getReturnType(), is("void"));
			assertThat(method.getAnnotations(), is(empty()));
			assertThat(method.getParameters(), is(empty()));
		}

		/**
		 * Testing the correct parsing of {@link TestClass}.
		 */
		@Test
		public void theClass() throws IOException {
			ClassReader classReader = new ClassReader(TestClass.class.getName());
			classReader.accept(classAnalyzer, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

			ImmutableType immutableType = classAnalyzer.getType();
			assertThat(immutableType, is(instanceOf(ClassType.class)));

			ClassType type = (ClassType) immutableType;

			assertThat(type.getFQN(), is(equalTo(TestClass.class.getName())));
			assertThat(type.isInitialized(), is(true));

			// modifiers
			assertThat(Modifiers.isAbstract(type.getModifiers()), is(false));
			assertThat(Modifiers.isFinal(type.getModifiers()), is(true));
			assertThat(Modifiers.isPublic(type.getModifiers()), is(true));

			// only one hash when analyzed
			assertThat(type.getHashes(), hasSize(1));
			assertThat(type.getHashes(), hasItem(HASH));

			// super class is AbstractTestClass
			assertThat(type.getSuperClasses(), hasSize(1));
			ClassType superClass = type.getSuperClasses().iterator().next();
			assertThat(superClass.getFQN(), is(equalTo(AbstractTestClass.class.getName())));
			assertThat(superClass.isInitialized(), is(false));

			// has one interface
			assertThat(type.getRealizedInterfaces(), hasSize(2));
			assertThat(type.getRealizedInterfaces(), hasItem(new InterfaceType(TestInterface.class.getName())));
			assertThat(type.getRealizedInterfaces(), hasItem(new InterfaceType(TestAnnotation.class.getName())));

			// other
			assertThat(type.getSubClasses(), is(not(nullValue())));
			assertThat(type.getSubClasses(), is(empty()));

			// note that there are methods + no-arg constructor
			assertThat(type.getMethods(), hasSize(6));

			MethodType method0 = null;
			MethodType method1 = null;
			MethodType methodWithException = null;
			MethodType constructor = null;
			// ignoring the methods from annotation

			for (MethodType methodType : type.getMethods()) {
				if (methodType.getMethodCharacter().equals(MethodType.Character.CONSTRUCTOR)) {
					constructor = methodType;
				} else if (methodType.getMethodCharacter().equals(MethodType.Character.METHOD) && "method0".equals(methodType.getName())) {
					method0 = methodType;
				} else if (methodType.getMethodCharacter().equals(MethodType.Character.METHOD) && "method1".equals(methodType.getName())) {
					method1 = methodType;
				} else if (methodType.getMethodCharacter().equals(MethodType.Character.METHOD) && "methodWithException".equals(methodType.getName())) {
					methodWithException = methodType;
				}
			}

			// check constructor
			assertThat(constructor.getName(), is(equalTo("<init>")));
			assertThat(constructor.getMethodCharacter(), is(MethodType.Character.CONSTRUCTOR));
			assertThat(Modifiers.isPublic(constructor.getModifiers()), is(true));
			assertThat(constructor.getReturnType(), is("void"));
			assertThat(constructor.getAnnotations(), is(empty()));
			assertThat(constructor.getParameters(), is(empty()));

			// check method0
			assertThat(method0.getName(), is(equalTo("method0")));
			assertThat(Modifiers.isProtected(method0.getModifiers()), is(true));
			assertThat(method0.getMethodCharacter(), is(MethodType.Character.METHOD));
			assertThat(method0.getReturnType(), is("void"));
			assertThat(method0.getParameters(), is(empty()));
			assertThat(method0.getAnnotations(), hasSize(1));
			AnnotationType annotation = method0.getAnnotations().iterator().next();
			assertThat(annotation.getFQN(), is(equalTo(TestAnnotation.class.getName())));
			assertThat(annotation.isInitialized(), is(false));

			// check method1
			assertThat(method1.getName(), is(equalTo("method1")));
			assertThat(Modifiers.isPublic(method1.getModifiers()), is(true));
			assertThat(method1.getMethodCharacter(), is(MethodType.Character.METHOD));
			assertThat(method1.getReturnType(), is(String.class.getName()));
			assertThat(method1.getAnnotations(), hasSize(0));
			assertThat(method1.getParameters(), hasSize(4));
			assertThat(method1.getParameters().get(0), is("int"));
			assertThat(method1.getParameters().get(1), is("long[]"));
			assertThat(method1.getParameters().get(2), is(String.class.getName()));
			assertThat(method1.getParameters().get(3), is(Object.class.getName() + "[][][]"));

			// check method with exception
			assertThat(methodWithException.getName(), is(equalTo("methodWithException")));
			assertThat(methodWithException.getMethodCharacter(), is(MethodType.Character.METHOD));
			assertThat(methodWithException.getReturnType(), is("void"));
			assertThat(methodWithException.getParameters(), is(empty()));
			assertThat(methodWithException.getExceptions(), hasSize(1));
			ClassType exception = methodWithException.getExceptions().iterator().next();
			assertThat(exception.getFQN(), is(equalTo(Exception.class.getName())));
			assertThat(exception.isInitialized(), is(false));
		}
	}

}
