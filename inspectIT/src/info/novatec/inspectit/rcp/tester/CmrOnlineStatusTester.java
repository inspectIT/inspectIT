package info.novatec.inspectit.rcp.tester;

import info.novatec.inspectit.rcp.provider.ICmrRepositoryAndAgentProvider;
import info.novatec.inspectit.rcp.provider.ICmrRepositoryProvider;
import info.novatec.inspectit.rcp.provider.IInputDefinitionProvider;
import info.novatec.inspectit.rcp.provider.IStorageDataProvider;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition.OnlineStatus;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;
import info.novatec.inspectit.storage.recording.RecordingState;

import org.eclipse.core.expressions.PropertyTester;

/**
 * Tester for CMR Online Status.
 * 
 * @author Ivan Senic
 * 
 */
public class CmrOnlineStatusTester extends PropertyTester {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		CmrRepositoryDefinition cmrRepositoryDefinition = null;
		if (receiver instanceof ICmrRepositoryProvider) {
			cmrRepositoryDefinition = ((ICmrRepositoryProvider) receiver).getCmrRepositoryDefinition();
		} else if (receiver instanceof ICmrRepositoryAndAgentProvider) {
			cmrRepositoryDefinition = ((ICmrRepositoryAndAgentProvider) receiver).getCmrRepositoryDefinition();
		} else if (receiver instanceof IStorageDataProvider) {
			cmrRepositoryDefinition = ((IStorageDataProvider) receiver).getCmrRepositoryDefinition();
		} else if (receiver instanceof IInputDefinitionProvider) {
			RepositoryDefinition repository = ((IInputDefinitionProvider) receiver).getInputDefinition().getRepositoryDefinition();
			if (repository instanceof CmrRepositoryDefinition) {
				cmrRepositoryDefinition = (CmrRepositoryDefinition) repository;
			} else {
				return false;
			}
		} else {
			return false;
		}

		if ("onlineStatus".equals(property)) {
			if ("ONLINE".equals(expectedValue)) {
				return cmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.ONLINE;
			} else if ("OFFLINE".equals(expectedValue)) {
				return cmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.OFFLINE;
			} else if ("CHECKING".equals(expectedValue)) {
				return cmrRepositoryDefinition.getOnlineStatus() == OnlineStatus.CHECKING;
			}
		} else if ("recordingActive".equals(property)) {
			if (expectedValue instanceof Boolean) {
				if (cmrRepositoryDefinition.getOnlineStatus() != OnlineStatus.OFFLINE) {
					boolean recordingActive = cmrRepositoryDefinition.getStorageService().getRecordingState() != RecordingState.OFF;
					return ((Boolean) expectedValue).booleanValue() == recordingActive;
				} else {
					return false;
				}
			}
		}
		return false;
	}

}
