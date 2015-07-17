package info.novatec.inspectit.agent.sensor.method.jdbc;

import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.hooking.IMethodHook;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This hook is intended to intercept the methods which are used to set some specific parameter
 * values on the prepared statements so that the final prepared statement with all inserted values
 * can be generated in the user interface.
 * 
 * @author Patrice Bouillet
 * 
 */
public class PreparedStatementParameterHook implements IMethodHook {

	/**
	 * The statement storage to add the statements to.
	 */
	private StatementStorage statementStorage;

	/**
	 * The ThreadLocal for a boolean value so only the last before and first after hook of an
	 * invocation is measured.
	 */
	private static ThreadLocal<Boolean> threadLast = new ThreadLocal<Boolean>();

	/**
	 * Map that holds the set methods for which we will not load the values via toString() method,
	 * but will have an assigned value given as the key in this map.
	 */
	private static final Map<String, String> METHOD_VALUE_MAP;

	static {
		METHOD_VALUE_MAP = new HashMap<String, String>();
		METHOD_VALUE_MAP.put("setNull", null);
		METHOD_VALUE_MAP.put("setAsciiStream", "[AsciiStream]");
		METHOD_VALUE_MAP.put("setBinaryStream", "[BinaryStream]");
		METHOD_VALUE_MAP.put("setBlob", "[Blob]");
		METHOD_VALUE_MAP.put("setCharacterStream", "[CharacterStream]");
		METHOD_VALUE_MAP.put("setClob", "[Clob]");
		METHOD_VALUE_MAP.put("setNCharacterStream", "[NCharacterStream]");
		METHOD_VALUE_MAP.put("setNClob", "[NClob]");
		METHOD_VALUE_MAP.put("setUnicodeStream", "[UnicodeStream]");
	}

	/**
	 * Default constructor which needs a reference to the statement storage.
	 * 
	 * @param statementStorage
	 *            The statement storage.
	 */
	public PreparedStatementParameterHook(StatementStorage statementStorage) {
		this.statementStorage = statementStorage;
	}

	/**
	 * {@inheritDoc}
	 */
	public void beforeBody(long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		threadLast.set(Boolean.TRUE);
	}

	/**
	 * {@inheritDoc}
	 */
	public void firstAfterBody(long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {
		// nothing to do
	}

	/**
	 * {@inheritDoc}
	 */
	public void secondAfterBody(ICoreService coreService, long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {
		if (threadLast.get().booleanValue()) {
			threadLast.set(Boolean.FALSE);

			List<String> parameterTypes = rsc.getParameterTypes();
			if (METHOD_VALUE_MAP.containsKey(rsc.getTargetMethodName()) && parameterTypes.size() >= 1 && "int".equals(parameterTypes.get(0))) {
				// subtract one as the index starts at 1, and not at 0
				int index = ((Integer) parameters[0]).intValue() - 1;
				Object value = METHOD_VALUE_MAP.get(rsc.getTargetMethodName());

				statementStorage.addParameter(object, index, value);
			} else if (parameterTypes.size() >= 2 && "int".equals(parameterTypes.get(0))) {
				// subtract one as the index starts at 1, and not at 0
				int index = ((Integer) parameters[0]).intValue() - 1;
				Object value = parameters[1];

				statementStorage.addParameter(object, index, value);
			} else if ("clearParameters".equals(rsc.getTargetMethodName())) {
				statementStorage.clearParameters(object);
			}
		}
	}

}
