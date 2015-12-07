package rocks.inspectit.shared.cs.cmr.service.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.shared.all.cmr.model.JmxDefinitionDataIdent;
import rocks.inspectit.shared.all.cmr.model.MethodIdent;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.cmr.model.SensorTypeIdent;
import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.data.cmr.AgentStatusData;
import rocks.inspectit.shared.all.communication.data.cmr.ApplicationData;
import rocks.inspectit.shared.all.communication.data.cmr.BusinessTransactionData;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.util.Pair;
import rocks.inspectit.shared.cs.cmr.service.IBusinessContextManagementService;
import rocks.inspectit.shared.cs.cmr.service.IGlobalDataAccessService;

/**
 * The default implementation of the cached ident objects. Provides a protected-visible method to
 * analyze and put a list of platform ident objects into the cache.
 * <p>
 * The implementing classes should realize the {@link #shouldRefreshIdents()} method to properly
 * instruct clearing of cache when needed.
 *
 * @author Patrice Bouillet
 * @author Ivan Senic
 *
 */
@Component
public class CachedDataService implements InitializingBean, ICachedDataService {

	/**
	 * Logger for the class. Needed to be directly assigned, because this class is used on the UI
	 * with no Spring to enhance it.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(CachedDataService.class);

	/**
	 * Delegated service.
	 */
	@Autowired
	private IGlobalDataAccessService globalDataAccessService;

	/**
	 * Delegate business context service.
	 */
	@Autowired
	private IBusinessContextManagementService businessContextService;

	/**
	 * This map is needed to store the mapping between the ID's and the {@link PlatformIdent}
	 * objects. Some views / editors need this information because they can only access the ID.
	 */
	private final Map<Long, PlatformIdent> platformMap = new ConcurrentHashMap<Long, PlatformIdent>();

	/**
	 * This map is needed to store the mapping between the ID's and the {@link SensorTypeIdent}
	 * objects. Some views / editors need this information because they can only access the ID.
	 */
	private final Map<Long, SensorTypeIdent> sensorTypeMap = new ConcurrentHashMap<Long, SensorTypeIdent>();

	/**
	 * This map is needed to store the mapping between the ID's and the {@link MethodIdent} objects.
	 * Some views / editors need this information because they can only access the ID.
	 */
	private final Map<Long, MethodIdent> methodMap = new ConcurrentHashMap<Long, MethodIdent>();

	/**
	 * This map is needed to store the mapping between the ID's and the
	 * {@link JmxDefinitionDataIdent} objects. Some views / editors need this information because
	 * they can only access the ID.
	 */
	private final Map<Long, JmxDefinitionDataIdent> jmxDefinitionDataMap = new ConcurrentHashMap<Long, JmxDefinitionDataIdent>();

	/**
	 * This map is needed to store the mapping between the ID's and the {@link ApplicationData}
	 * objects. Some views / editors need this information because they can only access the ID.
	 */
	private final Map<Integer, ApplicationData> applicationMap = new ConcurrentHashMap<Integer, ApplicationData>();

	/**
	 * This map is needed to store the mapping between the ID's and the
	 * {@link BusinessTransactionData} objects. Some views / editors need this information because
	 * they can only access the ID.
	 */
	private final Map<Pair<Integer, Integer>, BusinessTransactionData> businessTransactionsMap = new ConcurrentHashMap<Pair<Integer, Integer>, BusinessTransactionData>();

	/**
	 * No-args constructor.
	 */
	public CachedDataService() {
	}

	/**
	 * @param globalDataAccessService
	 *            Delegated service.
	 * @param businessContextService
	 *            Delegated {@link IBusinessContextManagementService}
	 */
	public CachedDataService(IGlobalDataAccessService globalDataAccessService, IBusinessContextManagementService businessContextService) {
		this.globalDataAccessService = globalDataAccessService;
		this.businessContextService = businessContextService;
	}

	/**
	 * This is a hook method for all subclasses that will be called after the idents have been
	 * refreshed due to the fact that {@link #shouldRefreshIdents()} reports ident should be
	 * refreshed.
	 */
	protected void postRefreshIdents() {
	}

	/**
	 * Triggers the refresh of the idents. After refresh {@link #postRefreshIdents()} will be
	 * executed.
	 */
	public void triggerRefreshIdents() {
		refreshIdents();
		postRefreshIdents();
	}

	/**
	 * Updates the data in the cache for the one agent. This method should be called with care,
	 * since it removes and inserts all the sensor data.
	 *
	 * @param platformIdent
	 *            Agento to refresh.
	 */
	public void refreshData(PlatformIdent platformIdent) {
		platformMap.remove(platformIdent.getId());
		platformMap.put(platformIdent.getId(), platformIdent);

		for (MethodIdent methodIdent : platformIdent.getMethodIdents()) {
			methodMap.remove(methodIdent.getId());
			methodMap.put(methodIdent.getId(), methodIdent);
		}

		for (SensorTypeIdent sensorTypeIdent : platformIdent.getSensorTypeIdents()) {
			sensorTypeMap.remove(sensorTypeIdent.getId());
			sensorTypeMap.put(sensorTypeIdent.getId(), sensorTypeIdent);
		}

		for (JmxDefinitionDataIdent jmxDefinitionDataIdent : platformIdent.getJmxDefinitionDataIdents()) {
			jmxDefinitionDataMap.remove(jmxDefinitionDataIdent.getId());
			jmxDefinitionDataMap.put(jmxDefinitionDataIdent.getId(), jmxDefinitionDataIdent);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PlatformIdent getPlatformIdentForId(long platformId) {
		Long id = Long.valueOf(platformId);
		// load only if the id is not 0
		if (0 != id.longValue() && !platformMap.containsKey(id)) {
			refreshIdents();
		}

		return platformMap.get(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SensorTypeIdent getSensorTypeIdentForId(long sensorTypeId) {
		Long id = Long.valueOf(sensorTypeId);
		// load only if the id is not 0
		if (0 != id.longValue() && !sensorTypeMap.containsKey(id)) {
			refreshIdents();
		}

		return sensorTypeMap.get(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MethodIdent getMethodIdentForId(long methodId) {
		Long id = Long.valueOf(methodId);
		// load only if the id is not 0
		if (0 != id.longValue() && !methodMap.containsKey(id)) {
			refreshIdents();
		}

		return methodMap.get(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JmxDefinitionDataIdent getJmxDefinitionDataIdentForId(long jmxDefinitionDataId) {
		Long id = Long.valueOf(jmxDefinitionDataId);
		// load only if the id is not 0
		if (0 != id.longValue() && !jmxDefinitionDataMap.containsKey(id)) {
			refreshIdents();
		}

		return jmxDefinitionDataMap.get(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ApplicationData getApplicationForId(int id) {
		if (!applicationMap.containsKey(id)) {
			refreshBusinessContext();
		}
		return applicationMap.get(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BusinessTransactionData getBusinessTransactionForId(int appId, int businessTxId) {
		Pair<Integer, Integer> keyPair = new Pair<Integer, Integer>(appId, businessTxId);
		if (!businessTransactionsMap.containsKey(keyPair)) {
			refreshBusinessContext();
		}
		BusinessTransactionData businessTxData = businessTransactionsMap.get(keyPair);
		return businessTxData;
	}

	/**
	 * Internal refresh of the idents. Currently everything is loaded again.
	 */
	protected void refreshIdents() {
		Map<PlatformIdent, AgentStatusData> agentMap = globalDataAccessService.getAgentsOverview();
		platformMap.clear();
		methodMap.clear();
		sensorTypeMap.clear();
		jmxDefinitionDataMap.clear();

		for (PlatformIdent overview : agentMap.keySet()) {
			PlatformIdent platformIdent;
			try {
				platformIdent = globalDataAccessService.getCompleteAgent(overview.getId());
			} catch (BusinessException e) {
				LOG.warn("Exception occurred trying to refresh sensor information for the agent " + overview.getAgentName() + ".", e);
				continue;
			}
			platformMap.put(platformIdent.getId(), platformIdent);

			for (MethodIdent methodIdent : platformIdent.getMethodIdents()) {
				methodMap.put(methodIdent.getId(), methodIdent);
			}

			for (SensorTypeIdent sensorTypeIdent : platformIdent.getSensorTypeIdents()) {
				sensorTypeMap.put(sensorTypeIdent.getId(), sensorTypeIdent);
			}

			for (JmxDefinitionDataIdent jmxDefinitionDataIdent : platformIdent.getJmxDefinitionDataIdents()) {
				jmxDefinitionDataMap.put(jmxDefinitionDataIdent.getId(), jmxDefinitionDataIdent);
			}
		}
	}

	/**
	 * Reloads the business context data.
	 */
	private void refreshBusinessContext() {
		applicationMap.clear();
		businessTransactionsMap.clear();
		for (BusinessTransactionData businessTx : businessContextService.getBusinessTransactions()) {
			businessTransactionsMap.put(new Pair<Integer, Integer>(businessTx.getApplication().getId(), businessTx.getId()), businessTx);
		}
		for (ApplicationData application : businessContextService.getApplications()) {
			applicationMap.put(application.getId(), application);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		refreshIdents();
		refreshBusinessContext();
	}
}
