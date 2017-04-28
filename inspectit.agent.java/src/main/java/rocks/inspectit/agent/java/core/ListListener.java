package rocks.inspectit.agent.java.core;

import java.util.EventListener;

/**
 * The {@link ListListener} interface allows a class to react on events that change a list.
 *
 * @param <E>
 *            The content class of the list.
 *
 * @author Patrice Bouillet
 */
public interface ListListener<E> extends EventListener {

	/**
	 * The content of a list has changed.
	 *
	 * @param elementCount
	 *            The amount of elements in the list.
	 */
	void contentChanged(int elementCount);

}
