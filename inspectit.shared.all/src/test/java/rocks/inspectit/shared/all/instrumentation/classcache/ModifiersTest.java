package rocks.inspectit.shared.all.instrumentation.classcache;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.lang.reflect.Modifier;

import org.testng.annotations.Test;

import rocks.inspectit.shared.all.instrumentation.classcache.Modifiers;

public class ModifiersTest {

	public class IsPublic extends ModifiersTest {

		@Test
		public void single() {
			int m = Modifiers.getModifiers(Modifier.PUBLIC);

			boolean result = Modifiers.isPublic(m);

			assertThat(result, is(true));
		}

		@Test
		public void multiple() {
			int m = Modifiers.getModifiers(Modifier.PUBLIC | Modifier.STATIC);

			boolean result = Modifiers.isPublic(m);

			assertThat(result, is(true));
		}

		@Test
		public void other() {
			int m = Modifiers.getModifiers(Modifier.STATIC);

			boolean result = Modifiers.isPublic(m);

			assertThat(result, is(false));
		}
	}

	public class IsPrivate extends ModifiersTest {

		@Test
		public void single() {
			int m = Modifiers.getModifiers(Modifier.PRIVATE);

			boolean result = Modifiers.isPrivate(m);

			assertThat(result, is(true));
		}

		@Test
		public void multiple() {
			int m = Modifiers.getModifiers(Modifier.PRIVATE | Modifier.STATIC);

			boolean result = Modifiers.isPrivate(m);

			assertThat(result, is(true));
		}

		@Test
		public void other() {
			int m = Modifiers.getModifiers(Modifier.STATIC);

			boolean result = Modifiers.isPrivate(m);

			assertThat(result, is(false));
		}
	}

	public class IsProtected extends ModifiersTest {

		@Test
		public void single() {
			int m = Modifiers.getModifiers(Modifier.PROTECTED);

			boolean result = Modifiers.isProtected(m);

			assertThat(result, is(true));
		}

		@Test
		public void multiple() {
			int m = Modifiers.getModifiers(Modifier.PROTECTED | Modifier.STATIC);

			boolean result = Modifiers.isProtected(m);

			assertThat(result, is(true));
		}

		@Test
		public void other() {
			int m = Modifiers.getModifiers(Modifier.STATIC);

			boolean result = Modifiers.isProtected(m);

			assertThat(result, is(false));
		}
	}

	public class IsPackage extends ModifiersTest {

		@Test
		public void single() {
			int m = Modifiers.getModifiers(Modifiers.PACKAGE);

			boolean result = Modifiers.isPackage(m);

			assertThat(result, is(true));
		}

		@Test
		public void multiple() {
			int m = Modifiers.getModifiers(Modifiers.PACKAGE | Modifier.PUBLIC);

			boolean result = Modifiers.isPackage(m);

			assertThat(result, is(true));
		}

		@Test
		public void other() {
			int m = Modifiers.getModifiers(Modifier.PUBLIC);

			boolean result = Modifiers.isPackage(m);

			assertThat(result, is(false));
		}
	}

	public class IsFinal extends ModifiersTest {

		@Test
		public void single() {
			int m = Modifiers.getModifiers(Modifier.FINAL);

			boolean result = Modifiers.isFinal(m);

			assertThat(result, is(true));
		}

		@Test
		public void multiple() {
			int m = Modifiers.getModifiers(Modifier.FINAL | Modifier.STATIC);

			boolean result = Modifiers.isFinal(m);

			assertThat(result, is(true));
		}

		@Test
		public void other() {
			int m = Modifiers.getModifiers(Modifier.STATIC);

			boolean result = Modifiers.isFinal(m);

			assertThat(result, is(false));
		}
	}

	public class IsSynchronized extends ModifiersTest {

		@Test
		public void single() {
			int m = Modifiers.getModifiers(Modifier.SYNCHRONIZED);

			boolean result = Modifiers.isSynchronized(m);

			assertThat(result, is(true));
		}

		@Test
		public void multiple() {
			int m = Modifiers.getModifiers(Modifier.SYNCHRONIZED | Modifier.STATIC);

			boolean result = Modifiers.isSynchronized(m);

			assertThat(result, is(true));
		}

		@Test
		public void other() {
			int m = Modifiers.getModifiers(Modifier.STATIC);

			boolean result = Modifiers.isSynchronized(m);

			assertThat(result, is(false));
		}
	}

	public class IsTransient extends ModifiersTest {

		@Test
		public void single() {
			int m = Modifiers.getModifiers(Modifier.TRANSIENT);

			boolean result = Modifiers.isTransient(m);

			assertThat(result, is(true));
		}

		@Test
		public void multiple() {
			int m = Modifiers.getModifiers(Modifier.TRANSIENT | Modifier.STATIC);

			boolean result = Modifiers.isTransient(m);

			assertThat(result, is(true));
		}

		@Test
		public void other() {
			int m = Modifiers.getModifiers(Modifier.STATIC);

			boolean result = Modifiers.isTransient(m);

			assertThat(result, is(false));
		}
	}

	public class IsVolatile extends ModifiersTest {

		@Test
		public void single() {
			int m = Modifiers.getModifiers(Modifier.VOLATILE);

			boolean result = Modifiers.isVolatile(m);

			assertThat(result, is(true));
		}

		@Test
		public void multiple() {
			int m = Modifiers.getModifiers(Modifier.VOLATILE | Modifier.STATIC);

			boolean result = Modifiers.isVolatile(m);

			assertThat(result, is(true));
		}

		@Test
		public void other() {
			int m = Modifiers.getModifiers(Modifier.STATIC);

			boolean result = Modifiers.isVolatile(m);

			assertThat(result, is(false));
		}
	}

	public class IsStatic extends ModifiersTest {

		@Test
		public void single() {
			int m = Modifiers.getModifiers(Modifier.STATIC);

			boolean result = Modifiers.isStatic(m);

			assertThat(result, is(true));
		}

		@Test
		public void multiple() {
			int m = Modifiers.getModifiers(Modifier.VOLATILE | Modifier.STATIC);

			boolean result = Modifiers.isStatic(m);

			assertThat(result, is(true));
		}

		@Test
		public void other() {
			int m = Modifiers.getModifiers(Modifier.VOLATILE);

			boolean result = Modifiers.isStatic(m);

			assertThat(result, is(false));
		}
	}

	public class MergeModifiers extends ModifiersTest {

		@Test
		public void merge() {
			int publicMod = Modifiers.getModifiers(Modifier.PUBLIC);
			int privateMod = Modifiers.getModifiers(Modifier.PRIVATE);

			int merged = Modifiers.mergeModifiers(publicMod, privateMod);

			assertThat(Modifiers.isPrivate(merged), is(true));
			assertThat(Modifiers.isPublic(merged), is(true));
		}

		@Test
		public void mergeTwice() {
			int publicMod = Modifiers.getModifiers(Modifier.PUBLIC);
			int privateMod = Modifiers.getModifiers(Modifier.PRIVATE);
			int protectedMod = Modifiers.getModifiers(Modifier.PROTECTED);

			int merged = Modifiers.mergeModifiers(publicMod, privateMod);
			merged = Modifiers.mergeModifiers(merged, protectedMod);

			assertThat(Modifiers.isPrivate(merged), is(true));
			assertThat(Modifiers.isPublic(merged), is(true));
			assertThat(Modifiers.isProtected(merged), is(true));
		}
	}
}
