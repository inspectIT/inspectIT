package info.novatec.inspectit.rcp.editor.inputdefinition.extra;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.InvocationAwareData;

import java.util.List;

import com.google.common.base.Objects;

/**
 * Extended input definition data to support the navigation and stepping.
 * 
 * @author Ivan Senic
 * 
 */
public class NavigationSteppingInputDefinitionExtra implements IInputDefinitionExtra {

	/**
	 * List of objects that define data from which navigate to was executed.
	 */
	private List<InvocationAwareData> invocationAwareDataList;

	/**
	 * List of the initial stepping template list.
	 */
	private List<DefaultData> steppingTemplateList;

	/**
	 * Gets {@link #invocationAwareDataList}.
	 * 
	 * @return {@link #invocationAwareDataList}
	 */
	public List<InvocationAwareData> getInvocationAwareDataList() {
		return invocationAwareDataList;
	}

	/**
	 * Sets {@link #invocationAwareDataList}.
	 * 
	 * @param invocationAwareDataList
	 *            New value for {@link #invocationAwareDataList}
	 */
	public void setInvocationAwareDataList(List<InvocationAwareData> invocationAwareDataList) {
		this.invocationAwareDataList = invocationAwareDataList;
	}

	/**
	 * Gets {@link #steppingTemplateList}.
	 * 
	 * @return {@link #steppingTemplateList}
	 */
	public List<DefaultData> getSteppingTemplateList() {
		return steppingTemplateList;
	}

	/**
	 * Sets {@link #steppingTemplateList}.
	 * 
	 * @param steppingTemplateList
	 *            New value for {@link #steppingTemplateList}
	 */
	public void setSteppingTemplateList(List<DefaultData> steppingTemplateList) {
		this.steppingTemplateList = steppingTemplateList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(invocationAwareDataList, steppingTemplateList);
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
		NavigationSteppingInputDefinitionExtra that = (NavigationSteppingInputDefinitionExtra) object;
		return Objects.equal(this.invocationAwareDataList, that.invocationAwareDataList) && Objects.equal(this.steppingTemplateList, that.steppingTemplateList);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("invocationAwareDataList", invocationAwareDataList).add("steppingTemplateList", steppingTemplateList).toString();
	}

}
