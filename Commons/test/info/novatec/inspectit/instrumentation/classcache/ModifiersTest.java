package info.novatec.inspectit.instrumentation.classcache;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import info.novatec.inspectit.instrumentation.classcache.Modifiers;

import java.lang.reflect.Modifier;

import org.testng.annotations.Test;

public class ModifiersTest {

	@Test
	public void create() {
		int m = Modifiers.getModifiers(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);

		assertThat(Modifiers.isPublic(m), is(true));
		assertThat(Modifiers.isStatic(m), is(true));
		assertThat(Modifiers.isFinal(m), is(true));

		assertThat(Modifiers.isPrivate(m), is(false));
		assertThat(Modifiers.isPackage(m), is(false));
		assertThat(Modifiers.isProtected(m), is(false));
		assertThat(Modifiers.isSynchronized(m), is(false));
		assertThat(Modifiers.isTransient(m), is(false));
		assertThat(Modifiers.isVolatile(m), is(false));
	}

	@Test
	public void packageTrue() {
		int m = Modifiers.getModifiers(Modifier.STATIC);
		assertThat(Modifiers.isPackage(m), is(true));
	}

	@Test
	public void packageFalse() {
		assertThat(Modifiers.isPackage(Modifiers.getModifiers(Modifier.PUBLIC)), is(false));
		assertThat(Modifiers.isPackage(Modifiers.getModifiers(Modifier.PRIVATE)), is(false));
		assertThat(Modifiers.isPackage(Modifiers.getModifiers(Modifier.PROTECTED)), is(false));
	}

	@Test
	public void merge() {
		int m = Modifiers.mergeModifiers(Modifiers.getModifiers(Modifier.PUBLIC), Modifiers.getModifiers(Modifier.PRIVATE));
		assertThat(Modifiers.isPrivate(m), is(true));
		assertThat(Modifiers.isPublic(m), is(true));
	}

}
