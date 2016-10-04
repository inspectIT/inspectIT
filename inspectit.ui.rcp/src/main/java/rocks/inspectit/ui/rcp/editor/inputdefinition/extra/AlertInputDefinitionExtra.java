package rocks.inspectit.ui.rcp.editor.inputdefinition.extra;

import com.google.common.base.Objects;

/**
 * {@link IInputDefinitionExtra} that holds the alert ID.
 *
 * @author Alexander Wert
 *
 */
public class AlertInputDefinitionExtra implements IInputDefinitionExtra {

	/**
	 * The identifier of the alert.
	 */
	private String alertId;

	/**
	 * Gets {@link #alertId}.
	 *
	 * @return {@link #alertId}
	 */
	public String getAlertId() {
		return alertId;
	}

	/**
	 * Sets {@link #alertId}.
	 *
	 * @param alertId
	 *            New value for {@link #alertId}
	 */
	public void setAlertId(String alertId) {
		this.alertId = alertId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(super.hashCode(), alertId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		AlertInputDefinitionExtra other = (AlertInputDefinitionExtra) obj;
		return Objects.equal(this.alertId, other.alertId);
	}

}
