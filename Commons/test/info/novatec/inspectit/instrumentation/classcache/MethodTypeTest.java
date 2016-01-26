package info.novatec.inspectit.instrumentation.classcache;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import info.novatec.inspectit.instrumentation.classcache.AnnotationType;
import info.novatec.inspectit.instrumentation.classcache.ClassType;
import info.novatec.inspectit.instrumentation.classcache.InterfaceType;
import info.novatec.inspectit.instrumentation.classcache.MethodType;
import info.novatec.inspectit.instrumentation.classcache.TypeWithAnnotations;
import info.novatec.inspectit.instrumentation.classcache.TypeWithMethods;
import info.novatec.inspectit.instrumentation.config.impl.MethodInstrumentationConfig;

import java.lang.reflect.Modifier;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.testng.annotations.Test;

@SuppressWarnings("PMD")
public class MethodTypeTest {
	private MethodType test;

	@Test
	public void setClassOrInterface() {
		test = new MethodType();

		ClassType c = new ClassType(null);

		test.setClassOrInterfaceType(c);

		assertThat(test.getClassOrInterfaceType(), is(equalTo((TypeWithMethods) c)));
		assertThat(c.getMethods(), hasItem((test)));

		InterfaceType i = new InterfaceType(null);

		test.setClassOrInterfaceType(i);

		assertThat(test.getClassOrInterfaceType(), is(equalTo((TypeWithMethods) i)));
		assertThat(i.getMethods(), hasItem((test)));
	}

	@Test
	public void addException() {
		test = new MethodType();

		ClassType i = new ClassType(null);

		test.addException(i);

		assertThat(test.getExceptions(), hasItem(i));
		assertThat(i.getMethodsThrowingThisException(), hasItem((test)));
	}

	@Test
	public void addAnnotation() {
		String fqn2 = "fqn2";
		test = new MethodType();

		AnnotationType a = new AnnotationType(fqn2);

		test.addAnnotation(a);

		assertThat(test.getAnnotations(), contains(a));
		assertThat(a.getAnnotatedTypes(), contains((TypeWithAnnotations) test));
	}

	@Test
	public void factoryMethod() {
		String modifiersMergeMethod = "mergeModifiers";
		int modifiersMergeMethodGiven = Modifier.PUBLIC | Modifier.VOLATILE;
		MethodType m = MethodType.build(modifiersMergeMethod, modifiersMergeMethodGiven, null, null, null, null);

		MethodType m2 = new MethodType();
		m2.setName(modifiersMergeMethod);
		m2.setModifiers(modifiersMergeMethodGiven);

		assertThat(m2, equalTo(m));
	}

	@Test
	public void equalsContract() {
		MethodInstrumentationConfig c1 = new MethodInstrumentationConfig();
		c1.setTargetClassFqn("c1");
		MethodInstrumentationConfig c2 = new MethodInstrumentationConfig();
		c2.setTargetClassFqn("c2");

		EqualsVerifier.forClass(MethodType.class).usingGetClass().withPrefabValues(MethodInstrumentationConfig.class, c1, c2).suppress(Warning.NONFINAL_FIELDS).verify();
	}

	@Test
	public void differentModifiersIsEqual() {
		assertThat(MethodType.build("a", Modifier.PUBLIC, null, null, null, null), is(equalTo(MethodType.build("a", Modifier.PRIVATE, null, null, null, null))));
	}

	@Test
	public void differentReturnTypeIsNotEqual() {
		assertThat(MethodType.build("a", 0, "void", null, null, null), is(not(equalTo(MethodType.build("a", 0, null, null, null, null)))));
	}

}
