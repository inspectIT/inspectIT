package info.novatec.inspectit.agent.sensor.method.averagetimer;

import info.novatec.inspectit.agent.config.IPropertyAccessor;
import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.core.ICoreService;
import info.novatec.inspectit.agent.core.IIdManager;
import info.novatec.inspectit.agent.core.IdNotAvailableException;
import info.novatec.inspectit.agent.core.impl.CoreService;
import info.novatec.inspectit.agent.hooking.IConstructorHook;
import info.novatec.inspectit.agent.hooking.IMethodHook;
import info.novatec.inspectit.communication.data.ParameterContentData;
import info.novatec.inspectit.communication.data.TimerData;
import info.novatec.inspectit.util.StringConstraint;
import info.novatec.inspectit.util.ThreadLocalStack;
import info.novatec.inspectit.util.Timer;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The hook implementation for the average timer sensor. It uses the {@link ThreadLocalStack} class
 * to save the time when the method was called. After the complete original method was executed, it
 * computes the how long the method took to finish. Afterwards, the measurement is added to the
 * {@link CoreService}.
 * 
 * @author Patrice Bouillet
 * 
 */
public class AverageTimerHook implements IMethodHook, IConstructorHook {

	/**
	 * The logger of this class. Initialized manually.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(AverageTimerHook.class);

	/**
	 * The stack containing the start time values.
	 */
	private ThreadLocalStack<Double> timeStack = new ThreadLocalStack<Double>();

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
	 * The StringConstraint to ensure a maximum length of strings.
	 */
	private StringConstraint strConstraint;

	/**
	 * The only constructor which needs the {@link Timer}.
	 * 
	 * @param timer
	 *            The timer.
	 * @param idManager
	 *            The ID manager.
	 * @param propertyAccessor
	 *            The property accessor.
	 * @param param
	 *            Additional parameters.
	 */
	public AverageTimerHook(Timer timer, IIdManager idManager, IPropertyAccessor propertyAccessor, Map<String, Object> param) {
		this.timer = timer;
		this.idManager = idManager;
		this.propertyAccessor = propertyAccessor;
		this.strConstraint = new StringConstraint(param);
	}

	/**
	 * {@inheritDoc}
	 */
	public void beforeBody(long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		timeStack.push(new Double(timer.getCurrentTime()));
	}

	/**
	 * {@inheritDoc}
	 */
	public void firstAfterBody(long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {
		timeStack.push(new Double(timer.getCurrentTime()));
	}

	/**
	 * {@inheritDoc}
	 */
	public void secondAfterBody(ICoreService coreService, long methodId, long sensorTypeId, Object object, Object[] parameters, Object result, RegisteredSensorConfig rsc) {
		double endTime = timeStack.pop().doubleValue();
		double startTime = timeStack.pop().doubleValue();
		double duration = endTime - startTime;

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

		TimerData timerData = (TimerData) coreService.getMethodSensorData(sensorTypeId, methodId, prefix);

		if (null == timerData) {
			try {
				long platformId = idManager.getPlatformId();
				long registeredSensorTypeId = idManager.getRegisteredSensorTypeId(sensorTypeId);
				long registeredMethodId = idManager.getRegisteredMethodId(methodId);

				Timestamp timestamp = new Timestamp(System.currentTimeMillis() - Math.round(duration));

				timerData = new TimerData(timestamp, platformId, registeredSensorTypeId, registeredMethodId, parameterContentData);
				timerData.increaseCount();
				timerData.addDuration(duration);
				timerData.calculateMin(duration);
				timerData.calculateMax(duration);

				coreService.addMethodSensorData(sensorTypeId, methodId, prefix, timerData);
			} catch (IdNotAvailableException e) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Could not save the average timer data because of an unavailable id. " + e.getMessage());
				}
			}
		} else {
			timerData.increaseCount();
			timerData.addDuration(duration);

			timerData.calculateMin(duration);
			timerData.calculateMax(duration);

		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void beforeConstructor(long methodId, long sensorTypeId, Object[] parameters, RegisteredSensorConfig rsc) {
		timeStack.push(new Double(timer.getCurrentTime()));
	}

	/**
	 * {@inheritDoc}
	 */
	public void afterConstructor(ICoreService coreService, long methodId, long sensorTypeId, Object object, Object[] parameters, RegisteredSensorConfig rsc) {
		timeStack.push(new Double(timer.getCurrentTime()));
		secondAfterBody(coreService, methodId, sensorTypeId, object, parameters, null, rsc);
	}

}
