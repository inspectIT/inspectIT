package rocks.inspectit.agent.java.sensor.method.special;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.agent.java.hooking.IHook;
import rocks.inspectit.agent.java.hooking.ISpecialHook;
import rocks.inspectit.agent.java.proxy.IRuntimeLinker;
import rocks.inspectit.agent.java.sensor.method.AbstractMethodSensor;
import rocks.inspectit.agent.java.sensor.method.IMethodSensor;

/**
 * @author Isabel Vico Peinado
 *
 */
public class CloseableHttpAsyncClientSensor extends AbstractMethodSensor implements IMethodSensor {

	/**
	 * Hook to use.
	 */
	ISpecialHook hook;

	/**
	 * {@link IRuntimeLinked} used to build the hook for the closeable.
	 */
	@Autowired
	private IRuntimeLinker runtimeLinker;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IHook getHook() {
		return hook;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initHook(Map<String, Object> parameters) {
		hook = new CloseableHttpAsyncClientHook(runtimeLinker);
	}

}
