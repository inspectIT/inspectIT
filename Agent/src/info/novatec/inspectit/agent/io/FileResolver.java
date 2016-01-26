package info.novatec.inspectit.agent.io;

import info.novatec.inspectit.agent.SpringAgent;
import info.novatec.inspectit.agent.config.IConfigurationStorage;

import java.io.File;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * File resolver knows where all files that agent saves data to should be.
 *
 * @author Ivan Senic
 *
 */
@Component
public class FileResolver implements InitializingBean {

	/**
	 * Location of the agent jar.
	 */
	private File agentJar;

	/**
	 * {@link IConfigurationStorage}.
	 */
	@Autowired
	private IConfigurationStorage configurationStorage;

	/**
	 * Returns file where cache hash cache for this agent should be.
	 *
	 * @return Returns file where cache hash cache for this agent should be.
	 */
	public File getClassHashCacheFile() {
		return new File(agentJar.getParent() + File.separator + "cache" + File.separator + configurationStorage.getAgentName() + File.separator + "sendingClasses.cache");
	}

	/**
	 * {@inheritDoc}
	 */
	public void afterPropertiesSet() throws Exception {
		agentJar = new File(SpringAgent.getInspectitJarLocation()).getAbsoluteFile();
	}

}
