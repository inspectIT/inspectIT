package info.novatec.inspectit.agent.sensor.method.jdbc;

import info.novatec.inspectit.agent.hooking.IHook;
import info.novatec.inspectit.agent.sensor.method.AbstractMethodSensor;
import info.novatec.inspectit.agent.sensor.method.IMethodSensor;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * This sensor reads the meta information from a connection.
 * 
 * @author Stefan Siegl
 */
public class ConnectionMetaDataSensor extends AbstractMethodSensor implements IMethodSensor {

	/**
	 * Meta information about the connection.
	 */
	@Autowired
	private ConnectionMetaDataStorage connectionStorage;

	/**
	 * The used prepared statement hook.
	 */
	private ConnectionMetaDataHook connectionHook = null;

	/**
	 * {@inheritDoc}
	 */
	public IHook getHook() {
		return connectionHook;
	}

	/**
	 * {@inheritDoc}
	 */
	public void init(Map<String, Object> parameter) {
		connectionHook = new ConnectionMetaDataHook(connectionStorage);
	}

}
