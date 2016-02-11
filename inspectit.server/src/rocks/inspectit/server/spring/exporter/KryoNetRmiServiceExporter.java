package rocks.inspectit.server.spring.exporter;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.shared.all.kryonet.Server;
import rocks.inspectit.shared.all.kryonet.rmi.ObjectSpace;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * Exporter that starts the {@link Server}, binds it to the given TCP port and register the object
 * to be used remotely.
 * 
 * @author Ivan Senic
 * 
 */
public class KryoNetRmiServiceExporter {

	/**
	 * Logger for the class.
	 */
	@Log
	Logger log;

	/**
	 * Service to export.
	 */
	private Object service;

	/**
	 * Service interface.
	 */
	private String serviceInterface;

	/**
	 * Id in the object space to export service within.
	 */
	private int serviceId;

	/**
	 * Server to register remote object to.
	 */
	@Autowired
	private ObjectSpace objectSpace;

	/**
	 * Prepares the server and register the service for remote usage.
	 */
	@PostConstruct
	protected void prepare() {
		if (null == service) {
			throw new BeanInitializationException("The service to export with the kryonet RMI must not be null.");
		}

		objectSpace.register(serviceId, service);
		log.info("|-Service " + serviceInterface + " exported and available via kryonet RMI with the ID " + serviceId);
	}

	/**
	 * Sets {@link #service}.
	 * 
	 * @param service
	 *            New value for {@link #service}
	 */
	public void setService(Object service) {
		this.service = service;
	}

	/**
	 * Sets {@link #serviceInterface}.
	 * 
	 * @param serviceInterface
	 *            New value for {@link #serviceInterface}
	 */
	public void setServiceInterface(String serviceInterface) {
		this.serviceInterface = serviceInterface;
	}

	/**
	 * Sets {@link #serviceId}.
	 * 
	 * @param serviceId
	 *            New value for {@link #serviceId}
	 */
	public void setServiceId(int serviceId) {
		this.serviceId = serviceId;
	}

}
