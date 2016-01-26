package rocks.inspectit.agent.java.io;

import java.io.File;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.agent.java.SpringAgent;
import rocks.inspectit.agent.java.config.IConfigurationStorage;

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
	 * <p>
	 * Expected path: <i>[PATH_TO_AGENT]/cache/agentName/sendingClasses.cache</i>
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
		agentJar = SpringAgent.getInspectitJarFile();
	}

}
