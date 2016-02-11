package info.novatec.inspectit.agent.sensor.method.timer;

import info.novatec.inspectit.agent.config.IPropertyAccessor;
import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.agent.hooking.IConstructorHook;
import info.novatec.inspectit.agent.hooking.IMethodHook;
import info.novatec.inspectit.agent.sensor.method.averagetimer.AverageTimerHook;
import info.novatec.inspectit.communication.data.ParameterContentData;
import info.novatec.inspectit.util.StringConstraint;
import info.novatec.inspectit.util.ThreadLocalStack;
import info.novatec.inspectit.util.Timer;

import java.lang.management.ThreadMXBean;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The hook implementation for the timer sensor. It uses the {@link ThreadLocalStack} class to save
 * the time when the method was called.
 * <p>
 * The difference to the {@link AverageTimerHook} is that it's using {@link ITimerStorage} objects
 * to save the values. The {@link ITimerStorage} is responsible for the actual data saving, so
 * different strategies can be chosen from (set through the configuration file).
 * 
 * @author Patrice Bouillet
 * 
 */
public class TimerHook implements IMethodHook, IConstructorHook {

	/**
	 * The logger of this class. Initialized manually.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(TimerHook.class);

	/**
	 * The stack containing the start time values.
	 */
	private final ThreadLocalStack<Double> timeStack = new ThreadLocalStack<Double>();

	/**
	 * The timer used for accurate measuring.
	 */
	private final Timer timer;

	/**
	 * The ID manager.
	 */
	private final IIdManager idManager;

	/**
	 * The property accessor.
	 */
	private final IPropertyAccessor propertyAccessor;

	/**
	 * The timer storage factory which returns a new {@link ITimerStorage} object every time we
	 * request one. The returned storage depends on the settings in the configuration file.
	 */
	private final TimerStorageFactory timerStorageFactory = TimerStorageFactory.getFactory();

	/**
	 * The StringConstraint to ensure a maximum length of strings.
	 */
	private StringConstraint strConstraint;

	/**
	 * The thread MX bean.
	 */
	private ThreadMXBean threadMXBean;

	/**
	 * Defines if the thread CPU time is supported.
	 */
	private boolean supported = false;

	/**
	 * Defines if the thread CPU time is enabled.
	 */
	private boolean enabled = false;

	/**
	 * The stack containing the start time values.
	 */
	private final ThreadLocalStack<Long> threadCpuTimeStack = new ThreadLocalStack<Long>();

	/**
	 * The only constructor which needs the used {@link ICoreService} implementation and the used
	 * {@link Timer}.
	 * 
	 * @param timer
	 *            The timer.
	 * @param idManager
	 *            The ID manager.
	 * @param propertyAccessor
	 *            The property accessor.
	 * @param param
	 *            Additional parameters passed to the {@link TimerStorageFactory} for proper
	 *            initialization.
	 * @param threadMXBean
	 *            The bean used to access the cpu time.
	 */
	public TimerHook(Timer timer, IIdManager idManager, IPropertyAccessor propertyAccessor, Map<String, Object> param, ThreadMXBean threadMXBean) {
		this.timer = timer;
		this.idManager = idManager;
		this.propertyAccessor = propertyAccessor;
		this.threadMXBean = threadMXBean;

		try {
			// if it is even supported by this JVM
			supported = threadMXBean.isThreadCpuTimeSupported();
			if (supported) {
				// check if its enabled
				enabled = threadMXBean.isThreadCpuTimeEnabled();
				if (!enabled) {
					// try to enable it
					threadMXBean.setThreadCpuTimeEnabled(true);
					// check again now if it is enabled now
					enabled = threadMXBean.isThreadCpuTimeEnabled();
				}
			}
		} catch (RuntimeException e) {
			// catching the runtime exceptions which could be thrown by the
			// above statements.
			LOG.warn("Exception in the TimerHook.", e);
		}

		timerStorageFactory.setParameters(param);
		this.strConstraint = new StringConstraint(param);
	}

	/**
	 * {@inheritDoc}
	 */
	public void beforeBody(long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		timeStack.push(new Double(timer.getCurrentTime()));
		if (enabled) {
			threadCpuTimeStack.push(Long.valueOf(threadMXBean.getCurrentThreadCpuTime()));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void firstAfterBody(long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {
		timeStack.push(new Double(timer.getCurrentTime()));
		if (enabled) {
			threadCpuTimeStack.push(Long.valueOf(threadMXBean.getCurrentThreadCpuTime()));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void secondAfterBody(ICoreService coreService, long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {
		double endTime = timeStack.pop().doubleValue();
		double startTime = timeStack.pop().doubleValue();
		double duration = endTime - startTime;

		// default setting to a negative number
		double cpuDuration = -1.0d;
		if (enabled) {
			long cpuEndTime = threadCpuTimeStack.pop().longValue();
			long cpuStartTime = threadCpuTimeStack.pop().longValue();
			cpuDuration = (cpuEndTime - cpuStartTime) / 1000000.0d;
		}

		List<ParameterContentData> parameterContentData = null;
		String prefix = null;
		// check if some properties need to be accessed and saved
		if (rsc.isPropertyAccess()) {
			parameterContentData = propertyAccessor.getParameterContentData(rsc.getPropertyAccessorList(), object, parameters, result);
			prefix = parameterContentData.toString();

			// crop the content strings of all ParameterContentData but leave the prefix as it is
			for (ParameterContentData contentData : parameterContentData) {
				contentData.setContent(strConstraint.crop(contentData.getContent()));
			}
		}

		ITimerStorage storage = (ITimerStorage) coreService.getObjectStorage(sensorTypeId, methodId, prefix);

		if (null == storage) {
			try {
				long platformId = idManager.getPlatformId();
				long registeredSensorTypeId = idManager.getRegisteredSensorTypeId(sensorTypeId);
				long registeredMethodId = idManager.getRegisteredMethodId(methodId);

				Timestamp timestamp = new Timestamp(System.currentTimeMillis() - Math.round(duration));

				boolean charting = "true".equals(rsc.getSettings().get("charting"));

				storage = timerStorageFactory.newStorage(timestamp, platformId, registeredSensorTypeId, registeredMethodId, parameterContentData, charting);
				storage.addData(duration, cpuDuration);

				coreService.addObjectStorage(sensorTypeId, methodId, prefix, storage);
			} catch (IdNotAvailableException e) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Could not save the timer data because of an unavailable id. " + e.getMessage());
				}
			}
		} else {
			storage.addData(duration, cpuDuration);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void beforeConstructor(long methodId, long sensorTypeId, Object[] parameters, RegisteredSensorConfig rsc) {
		timeStack.push(new Double(timer.getCurrentTime()));
		if (enabled) {
			threadCpuTimeStack.push(Long.valueOf(threadMXBean.getCurrentThreadCpuTime()));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void afterConstructor(ICoreService coreService, long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		timeStack.push(new Double(timer.getCurrentTime()));
		if (enabled) {
			threadCpuTimeStack.push(Long.valueOf(threadMXBean.getCurrentThreadCpuTime()));
		}
		// just call the second after body method directly
		secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, null, rsc);
	}

}
