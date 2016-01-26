package info.novatec.inspectit.instrumentation.classcache;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import info.novatec.inspectit.instrumentation.classcache.AbstractInterfaceType;
import info.novatec.inspectit.instrumentation.classcache.AnnotationType;
import info.novatec.inspectit.instrumentation.classcache.ClassType;
import info.novatec.inspectit.instrumentation.classcache.InterfaceType;
import info.novatec.inspectit.instrumentation.classcache.MethodType;
import info.novatec.inspectit.instrumentation.classcache.TypeWithAnnotations;
import info.novatec.inspectit.instrumentation.classcache.TypeWithMethods;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.testng.annotations.Test;

@SuppressWarnings("PMD")
public class InterfaceTypeTest {
	private InterfaceType test;

	@Test
	public void addHashToNotInitialized() {
		String fqn1 = "fqn1";
		String hash = "hash";
		test = new InterfaceType(fqn1);

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
		test = new InterfaceType(fqn1, storedHash, 0);

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
		test = new InterfaceType(fqn1, storedHash, m);

		assertThat(test.getModifiers(), is(equalTo(m)));
		int m2 = 2;

		test.setModifiers(m2);
		assertThat(test.getModifiers(), is(equalTo(m2)));
	}

	@Test
	public void addSuperInterface() {
		String fqn1 = "fqn1";
		String fqn2 = "fqn2";
		test = new InterfaceType(fqn1);

		InterfaceType i = new InterfaceType(fqn2);

		test.addSuperInterface(i);
		assertThat(test.getSuperInterfaces(), contains(i));
		assertThat(i.getSubInterfaces(), contains(test));
	}

	@Test
	public void addSubInterface() {
		String fqn1 = "fqn1";
		String fqn2 = "fqn2";
		test = new InterfaceType(fqn1);

		InterfaceType i = new InterfaceType(fqn2);

		test.addSubInterface(i);
		assertThat(test.getSubInterfaces(), contains(i));
		assertThat(i.getSuperInterfaces(), contains(test));
	}

	@Test
	public void addClassRealizingInterface() {
		String fqn1 = "fqn1";
		String fqn2 = "fqn2";
		test = new InterfaceType(fqn1);

		ClassType i = new ClassType(fqn2);

		test.addRealizingClass(i);
		assertThat(test.getRealizingClasses(), contains(i));
		assertThat(i.getRealizedInterfaces(), contains((AbstractInterfaceType) test));
	}


	@Test
	public void addMethod() {
		String fqn1 = "fqn1";
		test = new InterfaceType(fqn1);

		MethodType m = new MethodType();

		test.addMethod(m);

		assertThat(test.getMethods(), contains(m));
		assertThat(m.getClassOrInterfaceType(), is(equalTo((TypeWithMethods) test)));
	}

	@Test
	public void addAnnotation() {
		String fqn1 = "fqn1";
		String fqn2 = "fqn2";
		test = new InterfaceType(fqn1);

		AnnotationType a = new AnnotationType(fqn2);

		test.addAnnotation(a);

		assertThat(test.getAnnotations(), contains(a));
		assertThat(a.getAnnotatedTypes(), contains((TypeWithAnnotations) test));
	}

	@Test
	public void equalsContract() {
		// note that we add state to the type so super.equals(InterfaceType) should return false
		EqualsVerifier.forClass(InterfaceType.class).usingGetClass().withRedefinedSuperclass().verify();
	}
}
