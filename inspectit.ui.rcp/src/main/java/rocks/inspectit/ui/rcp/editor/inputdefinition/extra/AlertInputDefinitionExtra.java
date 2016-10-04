package rocks.inspectit.ui.rcp.editor.inputdefinition.extra;

import com.google.common.base.Objects;

import rocks.inspectit.shared.cs.communication.data.cmr.Alert;

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
	private Alert alert;

	/**
	 * Gets {@link #alert}.
	 * 
	 * @return {@link #alert}
	 */
	public Alert getAlert() {
		return alert;
	}

	/**
	 * Sets {@link #alert}.
	 * 
	 * @param alert
	 *            New value for {@link #alert}
	 */
	public void setAlert(Alert alert) {
		this.alert = alert;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(super.hashCode(), alert);
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
		return Objects.equal(this.getAlert(), other.getAlert());
	}
}
