package rocks.inspectit.agent.java.sensor.method;

import java.lang.reflect.Field;
import java.util.concurrent.ThreadFactory;

import org.openjdk.jmh.infra.ThreadParams;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import rocks.inspectit.agent.java.core.impl.CoreService;
import rocks.inspectit.agent.java.core.impl.DefaultDataFactory;
import rocks.inspectit.agent.java.core.impl.DefaultDataWrapper;
import rocks.inspectit.agent.java.core.impl.PlatformManager;

/**
 * Base class for hook perf tests, holds some repeating setup and cleanup actions.
 *
 * @author Ivan Senic
 *
 */
public abstract class AbstractHookPerfTest {

	// constants
	protected static final long PLATFORM_ID = 42L;
	protected static final long SENSOR_ID = 1L;
	protected static final Object[] PARAMS = new Object[] { "param" };
	protected static final Object TARGET = "target";
	protected static final Object RETURN_VALUE = "return";

	// needed components
	protected CoreService coreService;
	protected Disruptor<DefaultDataWrapper> disruptor;
	protected PlatformManager platformManager;

	// thread id saved in the method
	protected long methodId;

	@SuppressWarnings("unchecked")
	protected void init(ThreadParams threadParams) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		// method id
		methodId = threadParams.getThreadIndex();

		// init hooks
		platformManager = new PlatformManager();
		Field platformIdField = platformManager.getClass().getDeclaredField("platformId");
		platformIdField.setAccessible(true);
		platformIdField.setLong(platformManager, PLATFORM_ID);
		platformIdField.setAccessible(false);

		// init CoreService
		coreService = new CoreService();

		// setup disruptor
		ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("inspectit-disruptor-thread-%d").setDaemon(true).build();
		disruptor = new Disruptor<DefaultDataWrapper>(new DefaultDataFactory(), 1024 * 1024, threadFactory, ProducerType.MULTI, new BlockingWaitStrategy());
		disruptor.handleEventsWith(new EventHandler<DefaultDataWrapper>() {
			@Override
			public void onEvent(DefaultDataWrapper event, long sequence, boolean endOfBatch) throws Exception {
			}
		});
		disruptor.start();
		RingBuffer<DefaultDataWrapper> ringBuffer = disruptor.getRingBuffer();
		Field ringBufferField = coreService.getClass().getDeclaredField("ringBuffer");
		ringBufferField.setAccessible(true);
		ringBufferField.set(coreService, ringBuffer);
		ringBufferField.setAccessible(false);
	}

	protected void cleanUp() throws Exception {
		disruptor.shutdown();

		disruptor = null; // NOPMD
		coreService = null; // NOPMD
	}
}
