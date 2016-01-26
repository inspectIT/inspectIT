package rocks.inspectit.agent.java.sensor.method.averagetimer;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rocks.inspectit.agent.java.config.IPropertyAccessor;
import rocks.inspectit.agent.java.config.impl.RegisteredSensorConfig;
import rocks.inspectit.agent.java.core.ICoreService;
import rocks.inspectit.agent.java.core.IPlatformManager;
import rocks.inspectit.agent.java.core.IdNotAvailableException;
import rocks.inspectit.agent.java.core.impl.CoreService;
import rocks.inspectit.agent.java.hooking.IConstructorHook;
import rocks.inspectit.agent.java.hooking.IMethodHook;
import rocks.inspectit.agent.java.util.StringConstraint;
import rocks.inspectit.agent.java.util.ThreadLocalStack;
import rocks.inspectit.agent.java.util.Timer;
import rocks.inspectit.shared.all.communication.data.ParameterContentData;
import rocks.inspectit.shared.all.communication.data.TimerData;

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
	private final ThreadLocalStack<Double> timeStack = new ThreadLocalStack<Double>();

	/**
	 * The timer used for accurate measuring.
	 */
	private final Timer timer;

	/**
	 * The Platform manager.
	 */
	private final IPlatformManager platformManager;

	/**
	 * The property accessor.
	 */
	private final IPropertyAccessor propertyAccessor;

	/**
	 * The StringConstraint to ensure a maximum length of strings.
	 */
	private final StringConstraint strConstraint;

	/**
	 * The only constructor which needs the {@link Timer}.
	 *
	 * @param timer
	 *            The timer.
	 * @param platformManager
	 *            The Platform manager.
	 * @param propertyAccessor
	 *            The property accessor.
	 * @param param
	 *            Additional parameters.
	 */
	public AverageTimerHook(Timer timer, IPlatformManager platformManager, IPropertyAccessor propertyAccessor, Map<String, Object> param) {
		this.timer = timer;
		this.platformManager = platformManager;
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
				long platformId = platformManager.getPlatformId();

				Timestamp timestamp = new Timestamp(System.currentTimeMillis() - Math.round(duration));

				timerData = new TimerData(timestamp, platformId, sensorTypeId, methodId, parameterContentData);
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
