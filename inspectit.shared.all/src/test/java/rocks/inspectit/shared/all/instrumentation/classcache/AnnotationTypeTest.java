package rocks.inspectit.shared.all.instrumentation.classcache;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import org.testng.annotations.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import rocks.inspectit.shared.all.instrumentation.classcache.AnnotationType;
import rocks.inspectit.shared.all.instrumentation.classcache.TypeWithAnnotations;

@SuppressWarnings("PMD")
public class AnnotationTypeTest {

	AnnotationType test;

	@Test
	public void equalsContract() {
		// note that we add state to the type so super.equals(InterfaceType) should return false
		EqualsVerifier.forClass(AnnotationType.class).usingGetClass().withRedefinedSuperclass().verify();
	}

	public class AddHash extends AnnotationTypeTest {
		@Test
		public void addHashToNotInitialized() {
			String fqn1 = "fqn1";
			String hash = "hash";
			test = new AnnotationType(fqn1);

			test.addHash(hash);

			assertThat(test.getHashes(), contains(hash));
			assertThat(test.getHashes().size(), is(1));
		}

		@Test
		public void addHashToInitialized() {
			String fqn1 = "fqn1";
			String storedHash = "shash";
			String hash = "hash";
			test = new AnnotationType(fqn1, storedHash, 0);

			test.addHash(hash);

			assertThat(test.getHashes().size(), is(2));
			assertThat(test.getHashes(), hasItem(storedHash));
			assertThat(test.getHashes(), hasItem(hash));
		}
	}

	public class SetModifiers extends AnnotationTypeTest {

		@Test
		public void setModifier() {
			String fqn1 = "fqn1";
			String storedHash = "shash";
			int m = 0;
			test = new AnnotationType(fqn1, storedHash, m);
			int m2 = 2;

			test.setModifiers(m2);

			assertThat(test.getModifiers(), is(equalTo(m2)));
		}

	}

	public class AddAnnotation extends AnnotationTypeTest {

		@Test
		public void addAnnotation() {
			String fqn1 = "fqn1";
			String fqn2 = "fqn2";
			test = new AnnotationType(fqn1);

			AnnotationType a = new AnnotationType(fqn2);

			test.addAnnotation(a);

			assertThat(test.getAnnotations(), contains(a));
			assertThat(a.getAnnotatedTypes(), contains((TypeWithAnnotations) test));
		}
	}

	public class AddAnnotatedType extends AnnotationTypeTest {

		@Test
		public void addAnnotatedType() {
			String fqn1 = "fqn1";
			String fqn2 = "fqn2";
			test = new AnnotationType(fqn1);

			AnnotationType a = new AnnotationType(fqn2);

			test.addAnnotatedType(a);

			assertThat(test.getAnnotatedTypes(), contains((TypeWithAnnotations) a));
			assertThat(a.getAnnotations(), contains(test));
		}
	}

}
