package rocks.inspectit.ui.rcp.editor.inputdefinition.extra;

import java.util.List;

import com.google.common.base.Objects;

import rocks.inspectit.shared.all.communication.data.RemoteCallData;

/**
 * Extended input definition data to support the navigation and stepping.
 *
 * @author Thomas Kluge
 *
 */
public class RemoteInvocationInputDefinitionExtra implements IInputDefinitionExtra {

	/**
	 * List of objects that define data from which navigate to was executed.
	 */
	private List<RemoteCallData> remoteCallDataList;

	/**
	 * Gets {@link #remoteCallDataList}.
	 *
	 * @return {@link #remoteCallDataList}
	 */
	public List<RemoteCallData> getRemoteCallDataList() {
		return remoteCallDataList;
	}

	/**
	 * Sets {@link #remoteCallDataList}.
	 *
	 * @param remoteCallDataList
	 *            New value for {@link #remoteCallDataList}
	 */
	public void setRemoteCallDataList(List<RemoteCallData> remoteCallDataList) {
		this.remoteCallDataList = remoteCallDataList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(remoteCallDataList);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null) {
			return false;
		}
		if (getClass() != object.getClass()) {
			return false;
		}
		RemoteInvocationInputDefinitionExtra that = (RemoteInvocationInputDefinitionExtra) object;
		return Objects.equal(this.remoteCallDataList, that.remoteCallDataList);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("remoteCallDataList", remoteCallDataList).toString();
	}

}
