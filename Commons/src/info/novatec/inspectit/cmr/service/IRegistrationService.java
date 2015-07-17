package info.novatec.inspectit.cmr.service;

import info.novatec.inspectit.cmr.service.exception.ServiceException;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

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
@ServiceInterface(exporter = ServiceExporterType.RMI, serviceId = 1)
public interface IRegistrationService extends Remote {

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
	 * @throws RemoteException
	 *             If a remote exception occurs somewhere or if the registration was not successful
	 *             because the registration failed.
	 * @throws ServiceException
	 *             If database contains more than one corresponding platform ident already.
	 */
	long registerPlatformIdent(List<String> definedIPs, String agentName, String version) throws RemoteException, ServiceException;

	/**
	 * Unregisters a platform by passing the network interfaces from the target server.
	 * 
	 * @param definedIPs
	 *            The list of all network interfaces.
	 * @param agentName
	 *            Name of the Agent.
	 * @throws RemoteException
	 *             If a remote exception occurs somewhere or if the registration was not successful
	 *             because the registration failed.
	 * @throws ServiceException
	 *             If the agent defined IPs is not registered.
	 */
	void unregisterPlatformIdent(List<String> definedIPs, String agentName) throws RemoteException, ServiceException;

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
	 * @throws RemoteException
	 *             If a remote exception occurs somewhere.
	 */
	long registerMethodIdent(long platformIdent, String packageName, String className, String methodName, List<String> parameterTypes, String returnType, int modifiers) throws RemoteException;

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
	 * @throws RemoteException
	 *             If a remote exception occurs somewhere.
	 */
	long registerMethodSensorTypeIdent(long platformIdent, String fullyQualifiedClassName, Map<String, Object> parameters) throws RemoteException;

	/**
	 * This method is used to map a registered method sensor type to a registered method.
	 * 
	 * @param methodSensorTypeIdent
	 *            The unique identifier of the sensor type.
	 * @param methodIdent
	 *            The unique identifier of the method.
	 * @throws RemoteException
	 *             If a remote exception occurs somewhere.
	 */
	void addSensorTypeToMethod(long methodSensorTypeIdent, long methodIdent) throws RemoteException;

	/**
	 * Every sensor type which gathers information about the target platform/system has to be
	 * registered by calling this method.
	 * 
	 * @param platformIdent
	 *            The unique identifier of the platform.
	 * @param fullyQualifiedClassName
	 *            The fully qualified class name of the sensor type.
	 * @return Returns the unique platform sensor type identifier.
	 * @throws RemoteException
	 *             If a remote exception occurs somewhere.
	 */
	long registerPlatformSensorTypeIdent(long platformIdent, String fullyQualifiedClassName) throws RemoteException;

}
