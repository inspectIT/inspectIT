package info.novatec.inspectit.instrumentation.classcache;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import info.novatec.inspectit.instrumentation.classcache.AnnotationType;
import info.novatec.inspectit.instrumentation.classcache.ClassType;
import info.novatec.inspectit.instrumentation.classcache.InterfaceType;
import info.novatec.inspectit.instrumentation.classcache.MethodType;
import info.novatec.inspectit.instrumentation.classcache.TypeWithAnnotations;
import info.novatec.inspectit.instrumentation.classcache.TypeWithMethods;
import info.novatec.inspectit.instrumentation.config.impl.MethodInstrumentationConfig;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.testng.annotations.Test;

@SuppressWarnings("PMD")
public class ClassTypeTest {

	private ClassType test;

	String fqn1 = "fqn1";
	String fqn2 = "fqn2";

	@Test
	public void addHashToNotInitialized() {
		String fqn1 = "fqn1";
		String hash = "hash";
		test = new ClassType(fqn1);

		test.addHash(hash);

		assertThat(test.getHashes(), contains(hash));
		assertThat(test.getHashes().size(), is(1));
		assertThat(test.getHash(), is(equalTo(hash)));
	}

	@Test
	public void addHashToInitialized() {
		String fqn1 = "fqn1";
		String storedHash = "shash";
		String hash = "hash";
		test = new ClassType(fqn1, storedHash, 0);

		test.addHash(hash);

		assertThat(test.getHashes().size(), is(2));
		assertThat(test.getHashes(), hasItem(storedHash));
		assertThat(test.getHashes(), hasItem(hash));
		assertThat(test.getHash(), is(anyOf(is(hash), is(storedHash))));
	}

	@Test
	public void setModifier() {
		String fqn1 = "fqn1";
		String storedHash = "shash";
		int m = 0;
		test = new ClassType(fqn1, storedHash, m);

		assertThat(test.getModifiers(), is(equalTo(m)));
		int m2 = 2;

		test.setModifiers(m2);
		assertThat(test.getModifiers(), is(equalTo(m2)));
	}

	@Test
	public void addSuperClass() {
		String fqn1 = "fqn1";
		String fqn2 = "fqn2";
		test = new ClassType(fqn1);

		ClassType superclass = new ClassType(fqn2);

		test.addSuperClass(superclass);
		assertThat(test.getSuperClasses(), hasItem(superclass));
		assertThat(superclass.getSubClasses(), hasItem(test));
	}

	@Test
	public void addSubClass() {
		String fqn1 = "fqn1";
		String fqn2 = "fqn2";
		test = new ClassType(fqn1);

		ClassType subclass = new ClassType(fqn2);

		test.addSubclass(subclass);
		assertThat(test.getSubClasses(), hasItem(subclass));
		assertThat(subclass.getSuperClasses(), hasItem(test));
	}

	@Test
	public void addRealizingInterface() {
		String fqn1 = "fqn1";
		String fqn2 = "fqn2";
		test = new ClassType(fqn1);

		InterfaceType i = new InterfaceType(fqn2);

		test.addInterface(i);
		assertThat(test.getRealizedInterfaces(), hasItem(i));
		assertThat(i.getRealizingClasses(), hasItem(test));
	}

	@Test
	public void addMethod() {
		String fqn1 = "fqn1";
		test = new ClassType(fqn1);

		MethodType m = new MethodType();

		test.addMethod(m);

		assertThat(test.getMethods(), hasItem(m));
		assertThat(m.getClassOrInterfaceType(), is(equalTo((TypeWithMethods) test)));
	}

	@Test
	public void addAnnotation() {
		String fqn1 = "fqn1";
		String fqn2 = "fqn2";
		test = new ClassType(fqn1);

		AnnotationType a = new AnnotationType(fqn2);

		test.addAnnotation(a);

		assertThat(test.getAnnotations(), hasItem(a));
		assertThat(a.getAnnotatedTypes(), hasItem((TypeWithAnnotations) test));
	}

	@Test
	public void addMethodThrowsException() {
		test = new ClassType(fqn1);

		MethodType m = new MethodType();

		test.addMethodThrowingException(m);

		assertThat(test.getMethodsThrowingThisException(), hasItem(m));
		assertThat(m.getExceptions(), hasItem(test));
	}

	@Test
	public void noInstrumentationPoints() {
		test = new ClassType(fqn1);

		assertThat(test.hasInstrumentationPoints(), is(false));
		assertThat(test.getInstrumentationPoints(), is(empty()));

		MethodType m = new MethodType();
		test.addMethod(m);

		assertThat(test.hasInstrumentationPoints(), is(false));
		assertThat(test.getInstrumentationPoints(), is(empty()));
	}

	@Test
	public void addInstrumentationPoints() {
		test = new ClassType(fqn1);

		MethodInstrumentationConfig config = new MethodInstrumentationConfig();
		MethodType m = new MethodType();
		m.setMethodInstrumentationConfig(config);
		test.addMethod(m);

		assertThat(test.hasInstrumentationPoints(), is(true));
		assertThat(test.getInstrumentationPoints(), hasSize(1));
		assertThat(test.getInstrumentationPoints(), hasItem(config));
	}

	@Test
	public void isException() {
		test = new ClassType(fqn1);
		assertThat(test.isException(), is(false));

		ClassType superClass = new ClassType("some.Class");
		test.addSuperClass(superClass);
		assertThat(test.isException(), is(false));

		superClass = new ClassType("java.lang.Throwable");
		test.addSuperClass(superClass);
		assertThat(test.isException(), is(true));
	}

	@Test
	public void equalsContract() {
		// note that we add state to the type so super.equals(InterfaceType) should return false
		EqualsVerifier.forClass(ClassType.class).usingGetClass().withRedefinedSuperclass().verify();
	}
}
