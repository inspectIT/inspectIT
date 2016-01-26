package rocks.inspectit.shared.cs.cmr.service;

import java.util.List;
import java.util.Map;

import rocks.inspectit.shared.all.exception.BusinessException;

/**
 * The registration service is used and called by all inspectIT Agents. First, they have to call the
 * {@link #registerPlatformIdent(List, String)} method which returns a unique id for their JVM.
 * Afterwards, all sensor types (no matter if method- or platform-) are registered. Then they are
 * going to register all methods which are instrumented by that Agent. The last step is to map the
 * method sensor type to the instrumented method by calling the
 * {@link #addSensorTypeToMethod(long, long)} method.
 * <p>
 * All of this information will be persisted in the database. The returned values are basically
 * representing the index of the data in the db.
 *
 * @author Patrice Bouillet
 *
 */
public interface IRegistrationService {

	/**
	 * A unique platform identifier is generated out of the network interfaces from the target
	 * server and by specifying a self-defined Agent name.
	 *
	 * Note: the version String of the agent is not used to match existing platform identifications,
	 * that is even if the version String changes the platform identification will still be the
	 * same.
	 *
	 * @param definedIPs
	 *            The list of all network interfaces.
	 * @param agentName
	 *            The self-defined name of the inspectIT Agent.
	 * @param version
	 *            The version the agent is currently running with.
	 * @return Returns the unique platform identifier.
	 * @throws BusinessException
	 *             If database contains more than one corresponding platform ident already.
	 */
	long registerPlatformIdent(List<String> definedIPs, String agentName, String version) throws BusinessException;

	/**
	 * Unregisters a platform by passing the agent ID.
	 * 
	 * @param platformIdent
	 *            The unique identifier of the platform.
	 *
	 * @throws BusinessException
	 *             If the agent defined IPs is not registered.
	 */
	void unregisterPlatformIdent(long platformIdent) throws BusinessException;

	/**
	 * Every instrumented method has to be registered from every Agent. This method returns a unique
	 * value for this method so that measurements acquired from these methods can be linked in the
	 * database.
	 *
	 * @param platformIdent
	 *            The unique identifier of the platform.
	 * @param packageName
	 *            The name of the package.
	 * @param className
	 *            The name of the class.
	 * @param methodName
	 *            The name of the method.
	 * @param parameterTypes
	 *            The parameter types of the method.
	 * @param returnType
	 *            The return type of the method.
	 * @param modifiers
	 *            The modifiers.
	 * @return Returns the unique method identifier.
	 */
	long registerMethodIdent(long platformIdent, String packageName, String className, String methodName, List<String> parameterTypes, String returnType, int modifiers);

	/**
	 * Every sensor type which is called by an instrumented method to gather data has to be
	 * registered by calling this method.
	 *
	 * @param platformIdent
	 *            The unique identifier of the platform.
	 * @param fullyQualifiedClassName
	 *            The fully qualified class name of the sensor type.
	 * @param parameters
	 *            Map of parameters that sensor was assigned.
	 * @return Returns the unique method sensor type identifier.
	 */
	long registerMethodSensorTypeIdent(long platformIdent, String fullyQualifiedClassName, Map<String, Object> parameters);

	/**
	 * This method is used to map a registered method sensor type to a registered method.
	 *
	 * @param methodSensorTypeIdent
	 *            The unique identifier of the sensor type.
	 * @param methodIdent
	 *            The unique identifier of the method.
	 */
	void addSensorTypeToMethod(long methodSensorTypeIdent, long methodIdent);

	/**
	 * Every sensor type which gathers information about the target platform/system has to be
	 * registered by calling this method.
	 *
	 * @param platformIdent
	 *            The unique identifier of the platform.
	 * @param fullyQualifiedClassName
	 *            The fully qualified class name of the sensor type.
	 * @return Returns the unique platform sensor type identifier.
	 */
	long registerPlatformSensorTypeIdent(long platformIdent, String fullyQualifiedClassName);

	/**
	 * Every jmx sensor type has to be registered by calling this method.
	 *
	 * @param platformIdent
	 *            The unique identifier of the platform.
	 * @param sensorName
	 *            User given Name of the Sensor.
	 * @return Returns the unique JMX sensor sensor type identifier.
	 */
	long registerJmxSensorTypeIdent(long platformIdent, String sensorName);

	/**
	 *
	 * @param platformIdent
	 *            The unique identifier of the platform.
	 * @param mBeanObjectName
	 *            The Name of the monitored MBean.
	 * @param mBeanAttributeName
	 *            The Name of the specific attribute of the MBean which is monitored.
	 * @param mBeanAttributeDescription
	 *            The Description of the Attribute according to the information provided in the
	 *            MBeanAttributeInfo.
	 * @param mBeanAttributeType
	 *            The Type of the Attribute according to the information provided in the
	 *            MBeanAttributeInfo.
	 * @param isIs
	 *            True if a is-getter is available in the Attribute according to the information
	 *            provided in the MBeanAttributeInfo.
	 * @param isReadable
	 *            True if the Attribute is readable according to the information provided in the
	 *            MBeanAttributeInfo.
	 * @param isWritable
	 *            True if the Attribute is writable according to the information provided in the
	 *            MBeanAttributeInfo.
	 * @return Returns the unique JMX definition data identifier.
	 */
	long registerJmxSensorDefinitionDataIdent(long platformIdent, String mBeanObjectName, String mBeanAttributeName, String mBeanAttributeDescription, String mBeanAttributeType, boolean isIs,// NOCHK
			boolean isReadable, boolean isWritable);
}
