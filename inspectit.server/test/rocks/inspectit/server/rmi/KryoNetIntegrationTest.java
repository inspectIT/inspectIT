package info.novatec.inspectit.cmr.rmi;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import info.novatec.inspectit.cmr.test.AbstractTransactionalTestNGLogSupport;
import info.novatec.inspectit.kryonet.Client;
import info.novatec.inspectit.kryonet.Connection;
import info.novatec.inspectit.kryonet.ExtendedSerializationImpl;
import info.novatec.inspectit.kryonet.IExtendedSerialization;
import info.novatec.inspectit.kryonet.Listener;
import info.novatec.inspectit.kryonet.Server;
import info.novatec.inspectit.kryonet.rmi.ObjectSpace;
import info.novatec.inspectit.storage.nio.stream.StreamProvider;
import info.novatec.inspectit.storage.serializer.IKryoProvider;
import info.novatec.inspectit.storage.serializer.provider.SerializationManagerProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.esotericsoftware.kryonet.rmi.RemoteObject;

/**
 * Tests the complete kryonet server-client communication.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("PMD")
@ContextConfiguration(locations = { "classpath:spring/spring-context-global.xml", "classpath:spring/spring-context-database.xml", "classpath:spring/spring-context-beans.xml",
		"classpath:spring/spring-context-processors.xml", "classpath:spring/spring-context-storage-test.xml" })
public class KryoNetIntegrationTest extends AbstractTransactionalTestNGLogSupport {

	@Autowired
	protected SerializationManagerProvider serializationManagerProvider;

	@Autowired
	protected StreamProvider streamProvider;

	protected Server server;

	protected Client client;

	protected ObjectSpace objectSpace;

	@Mock
	protected Listener listener;

	@Mock
	protected Service service;

	@BeforeClass
	public void init() throws Exception {
		int port = 8765;
		IExtendedSerialization serialization = new ExtendedSerializationImpl(serializationManagerProvider) {
			@Override
			protected IKryoProvider createKryoProvider() {
				// hook in to register the test service
				IKryoProvider kryoProvider = super.createKryoProvider();
				kryoProvider.getKryo().register(Service.class);
				return kryoProvider;
			}
		};

		server = new Server(serialization, streamProvider);
		server.start();
		server.bind(port);

		objectSpace = new ObjectSpace();
		server.addListener(new Listener() {
			@Override
			public void connected(Connection connection) {
				objectSpace.addConnection(connection);
			}
		});

		client = new Client(serialization, streamProvider);
		client.start();
		client.connect(5000, "localhost", port);
	}

	@BeforeMethod
	public void initMocks() {
		if (null != listener) {
			server.removeListener(listener);
		}

		MockitoAnnotations.initMocks(this);

		server.addListener(listener);

		doAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				return invocation.getArguments()[0];
			}
		}).when(service).returnSame(Mockito.any());
		objectSpace.register(1, service);
	}

	@AfterClass
	public void closeConnections() throws IOException {
		client.stop();
		server.stop();
	}

	public class RemothMethodInvocation extends KryoNetIntegrationTest {

		@Test
		public void simple() {
			Service clientService = getServiceForClient();

			String toSend = "toSend";
			assertThat(clientService.returnSame(toSend), is(equalTo(toSend)));

			verify(service).returnSame(toSend);
			verifyNoMoreInteractions(service);
		}

		@Test(invocationCount = 10)
		public void multiThreaded() throws InterruptedException, BrokenBarrierException {
			Service clientService = getServiceForClient();

			int numThreads = 3;
			int numObjects = 1024;

			Set<Object> sendingObjectsSet = getObjectToSend(numObjects);
			Queue<Object> queue = new ArrayBlockingQueue<>(sendingObjectsSet.size());
			queue.addAll(sendingObjectsSet);

			CyclicBarrier cyclicBarrier = new CyclicBarrier(numThreads + 1);
			List<Thread> threads = new ArrayList<>();
			for (int i = 0; i < numThreads; i++) {
				Invoker invoker = new Invoker(cyclicBarrier, queue, clientService);
				threads.add(invoker);
				invoker.start();
			}

			cyclicBarrier.await();

			for (Thread thread : threads) {
				thread.join(10000);
			}

			for (Object toSend : sendingObjectsSet) {
				verify(service).returnSame(toSend);
			}
		}
	}

	public class Send extends KryoNetIntegrationTest {

		@Test
		public void simple() throws IOException, InterruptedException {
			String toSend = "toSend";
			client.sendTCP(toSend);

			// sleep as the receiving is done in another thread
			Thread.sleep(100);

			verify(listener).received(Mockito.<Connection> anyObject(), eq(toSend));
		}

		@Test(invocationCount = 10)
		public void multiThreaded() throws InterruptedException, BrokenBarrierException, IOException {
			int numThreads = 3;
			int numObjects = 1024;

			Set<Object> sendingObjectsSet = getObjectToSend(numObjects);
			Queue<Object> queue = new ArrayBlockingQueue<>(numObjects);
			queue.addAll(sendingObjectsSet);

			CyclicBarrier cyclicBarrier = new CyclicBarrier(numThreads + 1);
			List<Thread> threads = new ArrayList<>();
			for (int i = 0; i < numThreads; i++) {
				Sender sender = new Sender(cyclicBarrier, queue);
				threads.add(sender);
				sender.start();
			}

			cyclicBarrier.await();

			for (Thread thread : threads) {
				thread.join(10000);
			}

			// sleep as the receiving is done in another thread
			Thread.sleep(100);

			for (Object toSend : sendingObjectsSet) {
				verify(listener).received(Mockito.<Connection> anyObject(), eq(toSend));
			}
		}
	}

	protected Set<Object> getObjectToSend(int numObjects) {
		Random random = new Random();
		// use set to avoid possibility of same to send
		Set<Object> sendingObjectsSet = new HashSet<>();
		for (int i = 0; i < numObjects; i++) {
			List<Integer> sendList = new ArrayList<>();
			for (int j = 0, count = 1024; j < count; j++) {
				sendList.add(Integer.valueOf(random.nextInt()));
			}
			sendingObjectsSet.add(sendList);
		}
		return sendingObjectsSet;
	}

	protected Service getServiceForClient() {
		Service service = ObjectSpace.getRemoteObject(client, 1, Service.class);
		((RemoteObject) service).setNonBlocking(false);
		((RemoteObject) service).setTransmitReturnValue(true);
		((RemoteObject) service).setResponseTimeout(6000000);
		return service;
	}

	protected class Sender extends Thread {

		private final CyclicBarrier cyclicBarrier;
		private final Queue<Object> queue;

		public Sender(CyclicBarrier cyclicBarrier, Queue<Object> queue) {
			this.cyclicBarrier = cyclicBarrier;
			this.queue = queue;
		}

		@Override
		public void run() {
			try {
				cyclicBarrier.await();
			} catch (Exception e) {
				// ignore
			}

			Object toSend = queue.poll();
			while (null != toSend) {
				client.sendTCP(toSend);
				toSend = queue.poll();
			}
		}

	}

	/**
	 * Invoker for the {@link RemothMethodInvocation#multiThreaded()} method.
	 * 
	 * @author Ivan Senic
	 *
	 */
	protected class Invoker extends Thread {

		private final CyclicBarrier cyclicBarrier;
		private final Queue<Object> queue;
		private final Service service;

		public Invoker(CyclicBarrier cyclicBarrier, Queue<Object> queue, Service service) {
			this.cyclicBarrier = cyclicBarrier;
			this.queue = queue;
			this.service = service;
		}

		@Override
		public void run() {
			try {
				cyclicBarrier.await();
			} catch (Exception e) {
				// ignore
			}

			Object toSend = queue.poll();
			while (null != toSend) {
				assertThat(toSend.toString(), service.returnSame(toSend), is(equalTo(toSend)));
				toSend = queue.poll();
			}
		}

	}

	/**
	 * Test service.
	 */
	public interface Service {
		<E> E returnSame(E o);
	}
}
