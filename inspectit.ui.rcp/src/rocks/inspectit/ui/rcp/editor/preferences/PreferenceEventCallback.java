package info.novatec.inspectit.rcp.editor.preferences;

import java.util.Map;

import org.eclipse.core.runtime.Assert;

/**
 * The callback is used by the {@link IPreferencePanel} implementations to fire the events.
 * 
 * @author Patrice Bouillet
 * 
 */
public interface PreferenceEventCallback {

	/**
	 * This interface holds all the relevant data for the event.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	class PreferenceEvent {

		/**
		 * The ID of this event.
		 */
		private final PreferenceId preferenceId;

		/**
		 * The hash map containing all the objects.
		 */
		private Map<IPreferenceGroup, Object> preferenceMap;

		/**
		 * Constructor which needs an {@link PreferenceId}. Throws {@link NullPointerException} if
		 * the <code>preferenceId</code> is <code>null</code>.
		 * 
		 * @param preferenceId
		 *            The preference ID.
		 */
		public PreferenceEvent(PreferenceId preferenceId) {
			Assert.isNotNull(preferenceId);

			this.preferenceId = preferenceId;
		}

		/**
		 * @return the preferenceId
		 */
		public PreferenceId getPreferenceId() {
			return preferenceId;
		}

		/**
		 * @param preferenceMap
		 *            the preferenceMap to set
		 */
		public void setPreferenceMap(Map<IPreferenceGroup, Object> preferenceMap) {
			this.preferenceMap = preferenceMap;
		}

		/**
		 * @return the preferenceMap
		 */
		public Map<IPreferenceGroup, Object> getPreferenceMap() {
			return preferenceMap;
		}

	}

	/**
	 * This method is called whenever the preferences are changed.
	 * 
	 * @param preferenceEvent
	 *            The event object containing the changed objects.
	 */
	void eventFired(PreferenceEvent preferenceEvent);

}
