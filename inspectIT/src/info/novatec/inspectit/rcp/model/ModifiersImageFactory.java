package info.novatec.inspectit.rcp.model;

import static info.novatec.inspectit.rcp.model.Modifier.isPackage;
import static info.novatec.inspectit.rcp.model.Modifier.isPrivate;
import static info.novatec.inspectit.rcp.model.Modifier.isProtected;
import static info.novatec.inspectit.rcp.model.Modifier.isPublic;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;

import org.eclipse.swt.graphics.Image;

/**
 * Creates the appropriate method visibility image for the int value.
 * 
 * @author Patrice Bouillet
 * 
 */
public final class ModifiersImageFactory {

	/**
	 * The image key for the private visibility method.
	 */
	private static final String METHOD_PRIV_IMAGE = InspectITImages.IMG_METHOD_PRIVATE;

	/**
	 * The image key for the default visibility method.
	 */
	private static final String METHOD_DEFAULT_IMAGE = InspectITImages.IMG_METHOD_DEFAULT;

	/**
	 * The image key for the protected visibility method.
	 */
	private static final String METHOD_PROT_IMAGE = InspectITImages.IMG_METHOD_PROTECTED;

	/**
	 * The image key for the public visibility method.
	 */
	private static final String METHOD_PUB_IMAGE = InspectITImages.IMG_METHOD_PUBLIC;

	/**
	 * Hide constructor to disallow instantiation.
	 */
	private ModifiersImageFactory() {
	}

	/**
	 * Returns the image for the given modifiers. This image should not be disposed.
	 * 
	 * @param modifiers
	 *            The modifiers.
	 * @return The image.
	 */
	public static Image getImage(int modifiers) {
		InspectIT inspectIT = InspectIT.getDefault();
		if (isPrivate(modifiers)) {
			return inspectIT.getImage(METHOD_PRIV_IMAGE);
		} else if (isPackage(modifiers)) {
			return inspectIT.getImage(METHOD_DEFAULT_IMAGE);
		} else if (isProtected(modifiers)) {
			return inspectIT.getImage(METHOD_PROT_IMAGE);
		} else if (isPublic(modifiers)) {
			return inspectIT.getImage(METHOD_PUB_IMAGE);
		}

		return null;
	}

}
