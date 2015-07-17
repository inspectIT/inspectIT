package info.novatec.inspectit.rcp.model;

import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.communication.data.cmr.AgentStatusData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.util.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

/**
 * Factory for creating the input data representing the agents in the tree folder structure.
 * 
 * @author Ivan Senic
 * 
 */
public final class AgentFolderFactory {

	/**
	 * Folder split regex.
	 */
	private static final String FOLDER_SPLIT_REGEX = "/";

	/**
	 * Private constructor.
	 */
	private AgentFolderFactory() {
	}

	/**
	 * Returns the list of components representing the input tree structure of agents divided if
	 * needed to folders.
	 * 
	 * @param platformIdentMap
	 *            {@link Map} of {@link PlatformIdent}s and their statuses.
	 * @param cmrRepositoryDefinition
	 *            Repository agents belong to.
	 * @return List of components.
	 */
	public static List<Component> getAgentFolderTree(Map<PlatformIdent, AgentStatusData> platformIdentMap, CmrRepositoryDefinition cmrRepositoryDefinition) {
		Composite dummy = new Composite();
		for (Entry<PlatformIdent, AgentStatusData> entry : platformIdentMap.entrySet()) {
			PlatformIdent platformIdent = entry.getKey();
			AgentStatusData agentStatusData = entry.getValue();

			addToFolder(dummy, 0, platformIdent, agentStatusData, cmrRepositoryDefinition);
		}
		return dummy.getChildren();
	}

	/**
	 * Adds the platform to the folder on the given level. This method is recursive. If agent name
	 * is not fitting the level, it will be added to the given composite. If it fitting for the
	 * level, proper search will be done for sub-composite to insert the platform.
	 * 
	 * @param folder
	 *            Top composite.
	 * @param level
	 *            Wanted level.
	 * @param platformIdent
	 *            {@link PlatformIdent}.
	 * @param agentStatusData
	 *            {@link AgentStatusData}.
	 * @param cmrRepositoryDefinition
	 *            Repository agent is belonging to.
	 */
	private static void addToFolder(Composite folder, int level, PlatformIdent platformIdent, AgentStatusData agentStatusData, CmrRepositoryDefinition cmrRepositoryDefinition) {
		if (!accessibleForLevel(platformIdent.getAgentName(), level)) {
			// if name is not matching the level just add th leaf
			AgentLeaf agentLeaf = new AgentLeaf(platformIdent, agentStatusData, cmrRepositoryDefinition, level != 0);
			folder.addChild(agentLeaf);
		} else {
			// search for proper folder
			boolean folderExisting = false;
			String agentLevelName = getFolderNameFromAgent(platformIdent.getAgentName(), level);
			for (Component child : folder.getChildren()) {
				if (child instanceof Composite && ObjectUtils.equals(child.getName(), agentLevelName)) {
					addToFolder((Composite) child, level + 1, platformIdent, agentStatusData, cmrRepositoryDefinition);
					folderExisting = true;
				}
			}

			// if not found create new one
			if (!folderExisting) {
				Composite newFolder = createFolder(agentLevelName);
				addToFolder(newFolder, level + 1, platformIdent, agentStatusData, cmrRepositoryDefinition);
				folder.addChild(newFolder);
			}
		}
	}

	/**
	 * Creates new folder with given name.
	 * 
	 * @param levelName
	 *            Name.
	 * @return {@link Composite}.
	 */
	private static Composite createFolder(String levelName) {
		Composite composite = new Composite();
		composite.setName(levelName);
		composite.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_FOLDER));
		return composite;
	}

	/**
	 * Does agent name applies that agent should be put in the folder.
	 * 
	 * @param agentName
	 *            Name of the agent
	 * @return True if name of the agent has correctly specified folder structure at least for the 0
	 *         level (root folder).
	 */
	public static boolean accessibleForFolder(String agentName) {
		return accessibleForLevel(agentName, 0);
	}

	/**
	 * Returns what should be the name of the agent if the agent is located in a folder.
	 * 
	 * @param agentName
	 *            Name of the agent
	 * @return Display name. Result is the string between last found {@value #FOLDER_SPLIT_REGEX}
	 *         and end of name.
	 */
	public static String getAgentDisplayNameInFolder(String agentName) {
		String[] splitted = getSplittedAgentName(agentName);
		return splitted[splitted.length - 1];
	}

	/**
	 * Checks if the name of the agent is accessible for the given level of folder.
	 * 
	 * @param agentName
	 *            Name of the agent
	 * @param level
	 *            Level
	 * @return True if name of the agent has correctly specified folder structure for given level.
	 */
	private static boolean accessibleForLevel(String agentName, int level) {
		String[] splitted = getSplittedAgentName(agentName);
		if (splitted.length > level + 1) {
			for (String string : splitted) {
				if (StringUtils.isEmpty(string)) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns folder name by extracting it from the agent name.
	 * 
	 * @param agentName
	 *            Name of the agent
	 * @param level
	 *            Level
	 * @return Name or <code>null</code> if the agent name is not accessible for the given level.
	 */
	private static String getFolderNameFromAgent(String agentName, int level) {
		String[] splitted = getSplittedAgentName(agentName);
		if (splitted.length > level) {
			return splitted[level];
		} else {
			return null;
		}
	}

	/**
	 * @param agentName
	 *            Agent name.
	 * @return Splits the agent name to several strings that represent the folder names.
	 */
	private static String[] getSplittedAgentName(String agentName) {
		if (null != agentName) {
			return agentName.split(FOLDER_SPLIT_REGEX);
		} else {
			return new String[0];
		}
	}
}
