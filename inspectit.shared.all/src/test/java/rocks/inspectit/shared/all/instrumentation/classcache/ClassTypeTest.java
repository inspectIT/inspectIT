package rocks.inspectit.shared.all.instrumentation.classcache;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.Collection;

import org.testng.annotations.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import rocks.inspectit.shared.all.instrumentation.classcache.AnnotationType;
import rocks.inspectit.shared.all.instrumentation.classcache.ClassType;
import rocks.inspectit.shared.all.instrumentation.classcache.InterfaceType;
import rocks.inspectit.shared.all.instrumentation.classcache.MethodType;
import rocks.inspectit.shared.all.instrumentation.classcache.TypeWithAnnotations;
import rocks.inspectit.shared.all.instrumentation.classcache.TypeWithMethods;
import rocks.inspectit.shared.all.instrumentation.config.impl.MethodInstrumentationConfig;

@SuppressWarnings("PMD")
public class ClassTypeTest {

	ClassType test;

	@Test
	public void equalsContract() {
		// note that we add state to the type so super.equals(InterfaceType) should return false
		EqualsVerifier.forClass(ClassType.class).usingGetClass().withRedefinedSuperclass().verify();
	}

	public class AddHash extends ClassTypeTest {

		@Test
		public void addHashToNotInitialized() {
			String fqn1 = "fqn1";
			String hash = "hash";
			test = new ClassType(fqn1);

			test.addHash(hash);

			assertThat(test.getHashes(), contains(hash));
			assertThat(test.getHashes().size(), is(1));
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
		}
	}

	public class SetModifiers extends ClassTypeTest {

		@Test
		public void setModifier() {
			String fqn1 = "fqn1";
			String storedHash = "shash";
			int m = 0;
			test = new ClassType(fqn1, storedHash, m);
			int m2 = 2;

			test.setModifiers(m2);

			assertThat(test.getModifiers(), is(equalTo(m2)));
		}
	}

	public class AddSuperClass extends ClassTypeTest {

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
	}

	public class AddSubClass extends ClassTypeTest {

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
	}

	public class AddInterface extends ClassTypeTest {

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
	}

	public class AddMethod extends ClassTypeTest {

		@Test
		public void addMethod() {
			String fqn1 = "fqn1";
			test = new ClassType(fqn1);
			MethodType m = new MethodType();

			test.addMethod(m);

			assertThat(test.getMethods(), hasItem(m));
			assertThat(m.getClassOrInterfaceType(), is(equalTo((TypeWithMethods) test)));
		}
	}

	public class AddAnnotation extends ClassTypeTest {

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
	}

	public class AddMethodThrowingException extends ClassTypeTest {

		@Test
		public void addMethodThrowsException() {
			String fqn1 = "fqn1";
			test = new ClassType(fqn1);
			MethodType m = new MethodType();

			test.addMethodThrowingException(m);

			assertThat(test.getMethodsThrowingThisException(), hasItem(m));
			assertThat(m.getExceptions(), hasItem(test));
		}
	}

	public class HasInstrumentationPoints extends ClassTypeTest {

		@Test
		public void noInstrumentationPointsNoMethods() {
			String fqn1 = "fqn1";
			test = new ClassType(fqn1);

			boolean hasInstrumentationPoints = test.hasInstrumentationPoints();

			assertThat(hasInstrumentationPoints, is(false));
		}

		@Test
		public void noInstrumentationMethodDoesNotHave() {
			String fqn1 = "fqn1";
			test = new ClassType(fqn1);
			MethodType m = new MethodType();
			test.addMethod(m);

			boolean hasInstrumentationPoints = test.hasInstrumentationPoints();

			assertThat(hasInstrumentationPoints, is(false));
		}

		@Test
		public void hasInstrumentationPoints() {
			String fqn1 = "fqn1";
			test = new ClassType(fqn1);
			MethodInstrumentationConfig config = new MethodInstrumentationConfig();
			MethodType m = new MethodType();
			m.setMethodInstrumentationConfig(config);
			test.addMethod(m);

			boolean hasInstrumentationPoints = test.hasInstrumentationPoints();

			assertThat(hasInstrumentationPoints, is(true));
		}
	}

	public class GetInstrumentationPoints extends ClassTypeTest {

		@Test
		public void noInstrumentationPointsNoMethods() {
			String fqn1 = "fqn1";
			test = new ClassType(fqn1);

			Collection<MethodInstrumentationConfig> instrumentationPoints = test.getInstrumentationPoints();

			assertThat(instrumentationPoints, is(empty()));
		}

		@Test
		public void noInstrumentationMethodDoesNotHave() {
			String fqn1 = "fqn1";
			test = new ClassType(fqn1);
			MethodType m = new MethodType();
			test.addMethod(m);

			Collection<MethodInstrumentationConfig> instrumentationPoints = test.getInstrumentationPoints();

			assertThat(instrumentationPoints, is(empty()));
		}

		@Test
		public void hasInstrumentationPoints() {
			String fqn1 = "fqn1";
			test = new ClassType(fqn1);
			MethodInstrumentationConfig config = new MethodInstrumentationConfig();
			MethodType m = new MethodType();
			m.setMethodInstrumentationConfig(config);
			test.addMethod(m);

			Collection<MethodInstrumentationConfig> instrumentationPoints = test.getInstrumentationPoints();

			assertThat(instrumentationPoints, hasSize(1));
			assertThat(instrumentationPoints, hasItem(config));
		}
	}

	public class IsException extends ClassTypeTest {

		@Test
		public void doesNotHaveSuperClass() {
			String fqn1 = "fqn1";
			test = new ClassType(fqn1);

			boolean exception = test.isException();

			assertThat(exception, is(false));
		}

		@Test
		public void superClassIsNotException() {
			String fqn1 = "fqn1";
			test = new ClassType(fqn1);
			ClassType superClass = new ClassType("some.Class");
			test.addSuperClass(superClass);

			boolean exception = test.isException();

			assertThat(exception, is(false));
		}

		@Test
		public void superClassIsException() {
			String fqn1 = "fqn1";
			test = new ClassType(fqn1);
			ClassType superClass = new ClassType("java.lang.Throwable");
			test.addSuperClass(superClass);

			boolean exception = test.isException();

			assertThat(exception, is(true));
		}
	}

}
