package info.novatec.inspectit.instrumentation.classcache;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import info.novatec.inspectit.instrumentation.config.impl.MethodInstrumentationConfig;

import java.lang.reflect.Modifier;

import org.testng.annotations.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

@SuppressWarnings("PMD")
public class MethodTypeTest {

	MethodType test;

	@Test
	public void equalsContract() {
		MethodInstrumentationConfig c1 = new MethodInstrumentationConfig();
		c1.setTargetClassFqn("c1");
		MethodInstrumentationConfig c2 = new MethodInstrumentationConfig();
		c2.setTargetClassFqn("c2");

		EqualsVerifier.forClass(MethodType.class).usingGetClass().withPrefabValues(MethodInstrumentationConfig.class, c1, c2).suppress(Warning.NONFINAL_FIELDS).verify();
	}

	public class setClassOrInterfaceType extends MethodTypeTest {

		@Test
		public void setClassOrInterface() {
			test = new MethodType();
			ClassType c = new ClassType(null);

			test.setClassOrInterfaceType(c);

			assertThat(test.getClassOrInterfaceType(), is(equalTo((TypeWithMethods) c)));
			assertThat(c.getMethods(), hasItem((test)));
		}
	}

	public class AddException extends MethodTypeTest {

		@Test
		public void addException() {
			test = new MethodType();
			ClassType i = new ClassType(null);

			test.addException(i);

			assertThat(test.getExceptions(), hasItem(i));
			assertThat(i.getMethodsThrowingThisException(), hasItem((test)));
		}
	}

	public class AddAnnotation extends MethodTypeTest {

		@Test
		public void addAnnotation() {
			String fqn2 = "fqn2";
			test = new MethodType();
			AnnotationType a = new AnnotationType(fqn2);

			test.addAnnotation(a);

			assertThat(test.getAnnotations(), contains(a));
			assertThat(a.getAnnotatedTypes(), contains((TypeWithAnnotations) test));
		}
	}

	public class Build extends MethodTypeTest {

		@Test
		public void factoryMethod() {
			String modifiersMergeMethod = "mergeModifiers";
			int modifiersMergeMethodGiven = Modifier.PUBLIC | Modifier.VOLATILE;
			MethodType m2 = new MethodType();
			m2.setName(modifiersMergeMethod);
			m2.setModifiers(modifiersMergeMethodGiven);

			MethodType m = MethodType.build(modifiersMergeMethod, modifiersMergeMethodGiven, null, null, null, null);

			assertThat(m2, equalTo(m));
		}
	}

	public class EqualsTo extends MethodTypeTest {

		@Test
		public void differentModifiersIsEqual() {
			MethodType m1 = MethodType.build("a", Modifier.PUBLIC, null, null, null, null);
			MethodType m2 = MethodType.build("a", Modifier.PRIVATE, null, null, null, null);

			assertThat(m1, is(equalTo(m2)));
		}

		@Test
		public void differentReturnTypeIsNotEqual() {
			MethodType m1 = MethodType.build("a", 0, "void", null, null, null);
			MethodType m2 = MethodType.build("a", 0, null, null, null, null);

			assertThat(m1, is(not(equalTo(m2))));
		}
	}

}
