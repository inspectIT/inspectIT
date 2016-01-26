package rocks.inspectit.shared.all.instrumentation.classcache;

import java.lang.reflect.Modifier;

/**
 * We need a specialized <code>Modifiers</code> handler, as it can happen that one class might be
 * defined as public and private in the model. This is due to the situation, we need to handle the
 * package (default) as additional modifier and provide merge operations.
 *
 * @author Ivan Senic
 */
public final class Modifiers {

	/**
	 * Private constructor.
	 */
	private Modifiers() {
	}

	/**
	 * Defines package (default) modifier. Since our modifiers can be merged, we need to define and
	 * add this value to the each modifier.
	 */
	public static final int PACKAGE = 0x10000000;

	/**
	 * Returns the modifier with the correctly set bit for the package modifier if one should be
	 * defined.
	 *
	 * @param originalMod
	 *            Java based modifier.
	 * @return Modifier to be used in our types.
	 */
	public static int getModifiers(int originalMod) {
		if (isPublic(originalMod) || isPrivate(originalMod) || isProtected(originalMod)) {
			return originalMod;
		} else {
			return originalMod | PACKAGE;
		}
	}

	/**
	 * Merges two modifiers. This is a simple or operation on the given modifiers.
	 *
	 * @param m1
	 *            Modifier
	 * @param m2
	 *            Modifier
	 * @return Merge modifier.
	 */
	public static int mergeModifiers(int m1, int m2) {
		return m1 | m2;
	}

	/**
	 * Returns if modifier is public. Note that in our case modifier can be mix (public and private
	 * for example).
	 *
	 * @param mod
	 *            Modifier.
	 * @return If modifier is public. Note that in our case modifier can be mix (public and private
	 *         for example).
	 */
	public static boolean isPublic(int mod) {
		return Modifier.isPublic(mod);
	}

	/**
	 * Returns if modifier is private. Note that in our case modifier can be mix (public and private
	 * for example).
	 *
	 * @param mod
	 *            Modifier.
	 * @return If modifier is private. Note that in our case modifier can be mix (public and private
	 *         for example).
	 */
	public static boolean isPrivate(int mod) {
		return Modifier.isPrivate(mod);
	}

	/**
	 * Returns if modifier is protected. Note that in our case modifier can be mix (public and
	 * private for example).
	 *
	 * @param mod
	 *            Modifier.
	 * @return If modifier is protected. Note that in our case modifier can be mix (public and
	 *         private for example).
	 */
	public static boolean isProtected(int mod) {
		return Modifier.isProtected(mod);
	}

	/**
	 * Returns if modifier is package (default). Note that in our case modifier can be mix (public
	 * and private for example).
	 *
	 * @param mod
	 *            Modifier.
	 * @return If modifier is package (default). Note that in our case modifier can be mix (public
	 *         and private for example).
	 */
	public static boolean isPackage(int mod) {
		return (mod & PACKAGE) != 0;
	}

	/**
	 * Returns if modifier is volatile. Note that in our case modifier can be mix (public and
	 * private for example).
	 *
	 * @param mod
	 *            Modifier.
	 * @return If modifier is volatile. Note that in our case modifier can be mix (public and
	 *         private for example).
	 */
	public static boolean isVolatile(int mod) {
		return Modifier.isVolatile(mod);
	}


	/**
	 * Returns if modifier is static. Note that in our case modifier can be mix (public and private
	 * for example).
	 *
	 * @param mod
	 *            Modifier.
	 * @return If modifier is static. Note that in our case modifier can be mix (public and private
	 *         for example).
	 */
	public static boolean isStatic(int mod) {
		return Modifier.isStatic(mod);
	}

	/**
	 * Returns if modifier is abstract. Note that in our case modifier can be mix (public and
	 * private for example).
	 *
	 * @param mod
	 *            Modifier.
	 * @return If modifier is abstract. Note that in our case modifier can be mix (public and
	 *         private for example).
	 */
	public static boolean isAbstract(int mod) {
		return Modifier.isAbstract(mod);
	}

	/**
	 * Returns if modifier is final. Note that in our case modifier can be mix (public and private
	 * for example).
	 *
	 * @param mod
	 *            Modifier.
	 * @return If modifier is final. Note that in our case modifier can be mix (public and private
	 *         for example).
	 */
	public static boolean isFinal(int mod) {
		return Modifier.isFinal(mod);
	}

	/**
	 * Returns if modifier is synchronized. Note that in our case modifier can be mix (public and
	 * private for example).
	 *
	 * @param mod
	 *            Modifier.
	 * @return If modifier is synchronized. Note that in our case modifier can be mix (public and
	 *         private for example).
	 */
	public static boolean isSynchronized(int mod) {
		return Modifier.isSynchronized(mod);
	}

	/**
	 * Returns if modifier is transient. Note that in our case modifier can be mix (public and
	 * private for example).
	 *
	 * @param mod
	 *            Modifier.
	 * @return If modifier is transient. Note that in our case modifier can be mix (public and
	 *         private for example).
	 */
	public static boolean isTransient(int mod) {
		return Modifier.isTransient(mod);
	}

}
