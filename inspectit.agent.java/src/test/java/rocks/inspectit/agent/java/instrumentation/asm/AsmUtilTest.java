package rocks.inspectit.agent.java.instrumentation.asm;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import info.novatec.inspectit.org.objectweb.asm.Opcodes;

import java.lang.reflect.Modifier;

import org.testng.annotations.Test;

/**
 * @author Ivan Senic
 *
 */
public class AsmUtilTest {

	public class GetFqn extends AsmUtilTest {

		@Test
		public void fqn() {
			String asmInternal = "com/test/Example";

			String fqn = AsmUtil.getFqn(asmInternal);

			assertThat(fqn, is("com.test.Example"));
		}

		@Test
		public void defaultPackage() {
			String asmInternal = "Example";

			String fqn = AsmUtil.getFqn(asmInternal);

			assertThat(fqn, is("Example"));
		}
	}

	public class GetAsmInternalName extends AsmUtilTest {

		@Test
		public void asmInternalName() {
			String fqn = "com.test.Example";

			String asmInternal = AsmUtil.getAsmInternalName(fqn);

			assertThat(asmInternal, is("com/test/Example"));
		}

		@Test
		public void defaultPackage() {
			String fqn = "Example";

			String asmInternal = AsmUtil.getAsmInternalName(fqn);

			assertThat(asmInternal, is("Example"));
		}
	}

	public class GetModifiers extends AsmUtilTest {

		@Test
		public void publicMod() {
			int mod = AsmUtil.getModifiers(Opcodes.ACC_PUBLIC);

			assertThat(Modifier.isPublic(mod), is(true));
		}

		@Test
		public void privateMod() {
			int mod = AsmUtil.getModifiers(Opcodes.ACC_PRIVATE);

			assertThat(Modifier.isPrivate(mod), is(true));
		}

		@Test
		public void protectedMod() {
			int mod = AsmUtil.getModifiers(Opcodes.ACC_PROTECTED);

			assertThat(Modifier.isProtected(mod), is(true));
		}

		@Test
		public void staticMod() {
			int mod = AsmUtil.getModifiers(Opcodes.ACC_STATIC);

			assertThat(Modifier.isStatic(mod), is(true));
		}

		@Test
		public void finalMod() {
			int mod = AsmUtil.getModifiers(Opcodes.ACC_FINAL);

			assertThat(Modifier.isFinal(mod), is(true));
		}

		@Test
		public void synchronizedMod() {
			int mod = AsmUtil.getModifiers(Opcodes.ACC_SYNCHRONIZED);

			assertThat(Modifier.isSynchronized(mod), is(true));
		}

		@Test
		public void volatileMod() {
			int mod = AsmUtil.getModifiers(Opcodes.ACC_VOLATILE);

			assertThat(Modifier.isVolatile(mod), is(true));
		}

		@Test
		public void transientMod() {
			int mod = AsmUtil.getModifiers(Opcodes.ACC_TRANSIENT);

			assertThat(Modifier.isTransient(mod), is(true));
		}

		@Test
		public void nativeMod() {
			int mod = AsmUtil.getModifiers(Opcodes.ACC_NATIVE);

			assertThat(Modifier.isNative(mod), is(true));
		}

		@Test
		public void abstractMod() {
			int mod = AsmUtil.getModifiers(Opcodes.ACC_ABSTRACT);

			assertThat(Modifier.isAbstract(mod), is(true));
		}

		@Test
		public void mixed() {
			int mod = AsmUtil.getModifiers(Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT | Opcodes.ACC_FINAL);

			assertThat(Modifier.isPublic(mod), is(true));
			assertThat(Modifier.isAbstract(mod), is(true));
			assertThat(Modifier.isFinal(mod), is(true));
		}
	}

}
