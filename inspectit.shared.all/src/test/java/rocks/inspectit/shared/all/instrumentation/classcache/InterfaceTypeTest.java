package rocks.inspectit.shared.all.instrumentation.classcache;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import org.testng.annotations.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import rocks.inspectit.shared.all.instrumentation.classcache.AbstractInterfaceType;
import rocks.inspectit.shared.all.instrumentation.classcache.AnnotationType;
import rocks.inspectit.shared.all.instrumentation.classcache.ClassType;
import rocks.inspectit.shared.all.instrumentation.classcache.InterfaceType;
import rocks.inspectit.shared.all.instrumentation.classcache.MethodType;
import rocks.inspectit.shared.all.instrumentation.classcache.TypeWithAnnotations;
import rocks.inspectit.shared.all.instrumentation.classcache.TypeWithMethods;

@SuppressWarnings("PMD")
public class InterfaceTypeTest {

	InterfaceType test;

	@Test
	public void equalsContract() {
		// note that we add state to the type so super.equals(InterfaceType) should return false
		EqualsVerifier.forClass(InterfaceType.class).usingGetClass().withRedefinedSuperclass().verify();
	}

	public class AddHash extends InterfaceTypeTest {
		@Test
		public void addHashToNotInitialized() {
			String fqn1 = "fqn1";
			String hash = "hash";
			test = new InterfaceType(fqn1);

			test.addHash(hash);

			assertThat(test.getHashes(), contains(hash));
			assertThat(test.getHashes().size(), is(1));
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
		}
	}

	public class SetModifiers extends InterfaceTypeTest {

		@Test
		public void setModifiesr() {
			String fqn1 = "fqn1";
			String storedHash = "shash";
			int m = 0;
			test = new InterfaceType(fqn1, storedHash, m);
			int m2 = 2;

			test.setModifiers(m2);

			assertThat(test.getModifiers(), is(equalTo(m2)));
		}
	}

	public class AddSuperInterface extends InterfaceTypeTest {

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
	}

	public class AddSubInterface extends InterfaceTypeTest {

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
	}

	public class AddRealizingClass extends InterfaceTypeTest {

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
	}

	public class AddMethod extends InterfaceTypeTest {

		@Test
		public void addMethod() {
			String fqn1 = "fqn1";
			test = new InterfaceType(fqn1);
			MethodType m = new MethodType();

			test.addMethod(m);

			assertThat(test.getMethods(), contains(m));
			assertThat(m.getClassOrInterfaceType(), is(equalTo((TypeWithMethods) test)));
		}
	}

	public class AddAnnotation extends InterfaceTypeTest {

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
	}
}
