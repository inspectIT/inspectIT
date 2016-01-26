package info.novatec.inspectit.agent.instrumentation.asm;

import info.novatec.inspectit.instrumentation.classcache.Modifiers;

import java.lang.reflect.Modifier;

import org.objectweb.asm.Opcodes;

/**
 * Our small utility class for ASM.
 * 
 * @author Ivan Senic
 * 
 */
public final class AsmUtil {

	/**
	 * Private constructor.
	 */
	private AsmUtil() {
	}

	/**
	 * Returns FQN of the class giving the ASM internal name.
	 * 
	 * @param asmInternalName
	 *            ASM internal name.
	 * @return FQN of the class giving the ASM internal name.
	 */
	public static String getFqn(String asmInternalName) {
		return asmInternalName.replace('/', '.');
	}

	/**
	 * Returns FQN of the class giving the ASM internal name. Option intern allows to execute the
	 * {@link String#intern()} method prior to returning the FQN string.
	 * 
	 * @param asmInternalName
	 *            ASM internal name.
	 * @param internFQN
	 *            If should execute the {@link String#intern()} method prior to returning the FQN
	 *            string.
	 * @return FQN of the class giving the ASM internal name.
	 */
	public static String getFqn(String asmInternalName, boolean internFQN) {
		if (internFQN) {
			return getFqn(asmInternalName).intern();
		} else {
			return getFqn(asmInternalName);
		}
	}

	/**
	 * Returns {@link Modifiers} based modifiers for given access code. Returned modifier can be
	 * used directly in our types.
	 * 
	 * @param access
	 *            Asm access code
	 * @return Returns {@link Modifiers} based modifiers.
	 */
	public static int getModifiers(int access) {
		int modifier = 0;
		
		modifier |= ((access & Opcodes.ACC_PUBLIC) != 0) ? Modifier.PUBLIC : 0;
		modifier |= ((access & Opcodes.ACC_PRIVATE) != 0) ? Modifier.PRIVATE : 0;
		modifier |= ((access & Opcodes.ACC_PROTECTED) != 0) ? Modifier.PROTECTED : 0;
		modifier |= ((access & Opcodes.ACC_STATIC) != 0) ? Modifier.STATIC : 0;
		modifier |= ((access & Opcodes.ACC_FINAL) != 0) ? Modifier.FINAL : 0;
		modifier |= ((access & Opcodes.ACC_SYNCHRONIZED) != 0) ? Modifier.SYNCHRONIZED : 0;
		modifier |= ((access & Opcodes.ACC_VOLATILE) != 0) ? Modifier.VOLATILE : 0;
		modifier |= ((access & Opcodes.ACC_TRANSIENT) != 0) ? Modifier.TRANSIENT : 0;
		modifier |= ((access & Opcodes.ACC_NATIVE) != 0) ? Modifier.NATIVE : 0;
		modifier |= ((access & Opcodes.ACC_ABSTRACT) != 0) ? Modifier.ABSTRACT : 0;

		return Modifiers.getModifiers(modifier);
	}
}