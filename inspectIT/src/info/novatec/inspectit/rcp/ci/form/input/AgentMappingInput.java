package info.novatec.inspectit.rcp.ci.form.input;

import info.novatec.inspectit.ci.AgentMappings;
import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

import java.util.Collection;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * Input for the agent mapping editor.
 * 
 * @author Ivan Senic
 * 
 */
public class AgentMappingInput implements IEditorInput {

	/**
	 * {@link CmrRepositoryDefinition}.
	 */
	private final CmrRepositoryDefinition cmrRepositoryDefinition;

	/**
	 * Mappings.
	 */
	private final AgentMappings agentMappings;

	/**
	 * Existing environments mappings can be linked to.
	 */
	private final Collection<Environment> environments;

	/**
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 * @param mappings
	 *            Mappings.
	 * @param environments
	 *            Existing environments mappings can be linked to.
	 */
	public AgentMappingInput(CmrRepositoryDefinition cmrRepositoryDefinition, AgentMappings mappings, Collection<Environment> environments) {
		Assert.isNotNull(cmrRepositoryDefinition);
		Assert.isNotNull(mappings);
		Assert.isNotNull(environments);

		this.cmrRepositoryDefinition = cmrRepositoryDefinition;
		this.agentMappings = mappings;
		this.environments = environments;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean exists() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ImageDescriptor getImageDescriptor() {
		return InspectIT.getDefault().getImageDescriptor(InspectITImages.IMG_AGENT);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return "Agent Mapping [" + cmrRepositoryDefinition.getName() + "]";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getToolTipText() {
		return "";
	}

	/**
	 * Gets {@link #cmrRepositoryDefinition}.
	 * 
	 * @return {@link #cmrRepositoryDefinition}
	 */
	public CmrRepositoryDefinition getCmrRepositoryDefinition() {
		return cmrRepositoryDefinition;
	}

	/**
	 * Gets {@link #agentMappings}.
	 * 
	 * @return {@link #agentMappings}
	 */
	public AgentMappings getAgentMappings() {
		return agentMappings;
	}

	/**
	 * Gets {@link #environments}.
	 * 
	 * @return {@link #environments}
	 */
	public Collection<Environment> getEnvironments() {
		return environments;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (CmrRepositoryDefinition.class.equals(adapter)) {
			return cmrRepositoryDefinition;
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		// use only CMR in the hash code equals to only open one editor per CMR
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cmrRepositoryDefinition == null) ? 0 : cmrRepositoryDefinition.hashCode());
		return result;
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
		AgentMappingInput other = (AgentMappingInput) obj;
		if (cmrRepositoryDefinition == null) {
			if (other.cmrRepositoryDefinition != null) {
				return false;
			}
		} else if (!cmrRepositoryDefinition.equals(other.cmrRepositoryDefinition)) {
			return false;
		}
		return true;
	}

}
