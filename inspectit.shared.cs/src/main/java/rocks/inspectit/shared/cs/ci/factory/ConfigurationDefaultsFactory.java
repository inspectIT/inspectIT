package rocks.inspectit.shared.cs.ci.factory;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlTransient;

import rocks.inspectit.shared.all.instrumentation.config.impl.JmxAttributeDescriptor;
import rocks.inspectit.shared.all.instrumentation.config.impl.RetransformationStrategy;
import rocks.inspectit.shared.cs.ci.eum.EndUserMonitoringConfig;
import rocks.inspectit.shared.cs.ci.sensor.exception.IExceptionSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.exception.impl.ExceptionSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.jmx.JmxSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.IMethodSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.ConnectionSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.ExecutorClientSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.HttpSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.InvocationSequenceSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.Log4jLoggingSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.PreparedStatementParameterSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.PreparedStatementSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteApacheHttpClientV40SensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteAsyncApacheHttpClientSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteJavaHttpServerSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteJettyHttpClientV61ClientSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteJmsClientSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteJmsListenerServerSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteManualServerSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteSpringRestTemplateClientSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteUrlConnectionClientSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.StatementSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.TimerSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.IPlatformSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.ClassLoadingSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.CompilationSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.CpuSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.MemorySensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.RuntimeSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.SystemSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.ThreadSensorConfig;
import rocks.inspectit.shared.cs.ci.strategy.IStrategyConfig;
import rocks.inspectit.shared.cs.ci.strategy.impl.DisruptorStrategyConfig;

/**
 * Defaults factory defines what is default for our configuration.
 *
 * @author Ivan Senic
 *
 */
@XmlTransient
public final class ConfigurationDefaultsFactory {

	/**
	 * Private factory.
	 */
	private ConfigurationDefaultsFactory() {
	}

	/**
	 * Returns all available {@link IPlatformSensorConfig}s.
	 *
	 * @return Returns all available {@link IPlatformSensorConfig}s.
	 */
	public static List<IPlatformSensorConfig> getAvailablePlatformSensorConfigs() {
		List<IPlatformSensorConfig> platformSensorConfigs = new ArrayList<>();
		platformSensorConfigs.add(new ClassLoadingSensorConfig());
		platformSensorConfigs.add(new CompilationSensorConfig());
		platformSensorConfigs.add(new CpuSensorConfig());
		platformSensorConfigs.add(new MemorySensorConfig());
		platformSensorConfigs.add(new RuntimeSensorConfig());
		platformSensorConfigs.add(new SystemSensorConfig());
		platformSensorConfigs.add(new ThreadSensorConfig());
		return platformSensorConfigs;
	}

	/**
	 * Returns all available {@link IMethodSensorConfig}s.
	 *
	 * @return Returns all available {@link IMethodSensorConfig}s.
	 */
	public static List<IMethodSensorConfig> getAvailableMethodSensorConfigs() {
		List<IMethodSensorConfig> methodSensorConfigs = new ArrayList<>();
		methodSensorConfigs.add(new ConnectionSensorConfig());
		methodSensorConfigs.add(new HttpSensorConfig());
		methodSensorConfigs.add(new InvocationSequenceSensorConfig());
		methodSensorConfigs.add(new PreparedStatementParameterSensorConfig());
		methodSensorConfigs.add(new PreparedStatementSensorConfig());
		methodSensorConfigs.add(new StatementSensorConfig());
		methodSensorConfigs.add(new TimerSensorConfig());
		methodSensorConfigs.add(new Log4jLoggingSensorConfig());
		methodSensorConfigs.add(new RemoteApacheHttpClientV40SensorConfig());
		methodSensorConfigs.add(new RemoteJettyHttpClientV61ClientSensorConfig());
		methodSensorConfigs.add(new RemoteUrlConnectionClientSensorConfig());
		methodSensorConfigs.add(new RemoteSpringRestTemplateClientSensorConfig());
		methodSensorConfigs.add(new RemoteJavaHttpServerSensorConfig());
		methodSensorConfigs.add(new RemoteJmsClientSensorConfig());
		methodSensorConfigs.add(new RemoteJmsListenerServerSensorConfig());
		methodSensorConfigs.add(new RemoteManualServerSensorConfig());
		methodSensorConfigs.add(new ExecutorClientSensorConfig());
		methodSensorConfigs.add(new RemoteAsyncApacheHttpClientSensorConfig());
		return methodSensorConfigs;
	}

	/**
	 * Returns default {@link IExceptionSensorConfig}. That's {@link ExceptionSensorConfig}.
	 *
	 * @return Returns default {@link IExceptionSensorConfig}.
	 */
	public static IExceptionSensorConfig getDefaultExceptionSensorConfig() {
		return new ExceptionSensorConfig();
	}

	/**
	 * Returns default End User Monitoring configuration.
	 *
	 * @return Returns default End User Monitoring configuration.
	 */
	public static EndUserMonitoringConfig getDefaultEndUserMonitoringConfig() {
		return new EndUserMonitoringConfig();
	}

	/**
	 * Returns default {@link JmxAttributeDescriptor}.
	 *
	 * @return Returns default {@link JmxAttributeDescriptor}.
	 */
	public static JmxSensorConfig getDefaultJmxSensorConfig() {
		return new JmxSensorConfig();
	}

	/**
	 * Returns default {@link RetransformationStrategy}.
	 *
	 * @return The default {@link RetransformationStrategy}
	 */
	public static RetransformationStrategy getDefaultRetransformationStrategy() {
		return RetransformationStrategy.DISABLE_ON_IBM_JVM;
	}

	/**
	 * Returns default disruptor strategy config.
	 *
	 * @return Returns default disruptor strategy config.
	 */
	public static IStrategyConfig getDefaultDisruptorStrategy() {
		return new DisruptorStrategyConfig();
	}

}
