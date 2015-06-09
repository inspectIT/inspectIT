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
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.esotericsoftware.kryonet.rmi.RemoteObject;

/**
 * Tests the complete kryonet server-client communication.
 * 
 * @author Ivan Senic
 * 
 */
@ContextConfiguration(locations = { "classpath:spring/spring-context-global.xml", "classpath:spring/spring-context-database.xml", "classpath:spring/spring-context-beans.xml",
		"classpath:spring/spring-context-processors.xml", "classpath:spring/spring-context-storage-test.xml" })
@SuppressWarnings("PMD")
public class KryoNetIntegrationTest extends AbstractTransactionalTestNGLogSupport {

	private Logger log = LoggerFactory.getLogger(KryoNetIntegrationTest.class);

	@Autowired
	private SerializationManagerProvider serializationManagerProvider;

	@Autowired
	private StreamProvider streamProvider;

	private Server server;

	private Client client;

	@Mock
	private Listener listener;

	@Mock
	private Service service;

	@BeforeClass
	public void init() throws IOException {
		MockitoAnnotations.initMocks(this);

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
		server.addListener(listener);
		
		doAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				return invocation.getArguments()[0];
			}
		}).when(service).returnSame(Mockito.any());
		final ObjectSpace objectSpace = new ObjectSpace();
		objectSpace.register(1, service);
		server.addListener(new Listener() {
			@Override
			public void connected(Connection connection) {
				objectSpace.addConnection(connection);
			}
		});

		client = new Client(serialization, streamProvider);
		client.start();
		client.connect(5000, "localhost", port);

		verify(listener).connected(Mockito.<Connection> anyObject());
	}

	@AfterClass
	public void closeConnections() {
		client.close();
		server.close();
		verify(listener).disconnected(Mockito.<Connection> anyObject());
	}

	@Test
	public void simpleSend() throws IOException {
		String toSend = "toSend";
		client.sendTCP(toSend);
		client.update(100);

		verify(listener).received(Mockito.<Connection> anyObject(), eq(toSend));
	}

	@Test(invocationCount = 10)
	public void multiThreadedSend() throws InterruptedException, BrokenBarrierException, IOException {
		Random random = new Random();
		int numThreads = 1 + random.nextInt(5); // min 1 thread
		int numObjects = 1 + random.nextInt(1000); // max 1k objects

		List<Object> sendingObjectsList = new ArrayList<>();
		for (int i = 0; i < numObjects; i++) {
			byte[] toSend = new byte[random.nextInt(8096)];
			random.nextBytes(toSend);
			sendingObjectsList.add(toSend);
		}

		Queue<Object> queue = new ArrayBlockingQueue<>(numObjects);
		queue.addAll(sendingObjectsList);
		
		log.info("Starting multi threaded kryonet sending test.. Threads = " + numThreads + ", sending objects = " + numObjects);

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

		client.update(100);
		for (Object toSend : sendingObjectsList) {
			verify(listener).received(Mockito.<Connection> anyObject(), eq(toSend));
		}
	}

	@Test
	public void simpleRMI() {
		Service clientService = getServiceForClient();
		
		String toSend = "toSend";
		assertThat(clientService.returnSame(toSend), is(equalTo(toSend)));

		verify(service).returnSame(toSend);
		verifyNoMoreInteractions(service);
	}

	@Test(invocationCount = 10)
	public void multiThreadedRMI() throws InterruptedException, BrokenBarrierException {
		Service clientService = getServiceForClient();

		Random random = new Random();
		int numThreads = 1 + random.nextInt(5); // min 1 thread
		int numObjects = 1 + random.nextInt(1000); // max 1k objects

		List<Object> sendingObjectsList = new ArrayList<>();
		for (int i = 0; i < numObjects; i++) {
			byte[] toSend = new byte[random.nextInt(8096)];
			random.nextBytes(toSend);
			sendingObjectsList.add(toSend);
		}

		Queue<Object> queue = new ArrayBlockingQueue<>(numObjects);
		queue.addAll(sendingObjectsList);

		log.info("Starting multi threaded kryonet RMI test.. Threads = " + numThreads + ", sending objects = " + numObjects);

		CyclicBarrier cyclicBarrier = new CyclicBarrier(numThreads + 1);
		List<Thread> threads = new ArrayList<>();
		for (int i = 0; i < numThreads; i++) {
			RMIInvoker invoker = new RMIInvoker(cyclicBarrier, queue, clientService);
			threads.add(invoker);
			invoker.start();
		}

		cyclicBarrier.await();

		for (Thread thread : threads) {
			thread.join(10000);
		}

		for (Object toSend : sendingObjectsList) {
			verify(service).returnSame(toSend);
		}
	}

	private Service getServiceForClient() {
		Service service = ObjectSpace.getRemoteObject(client, 1, Service.class);
		((RemoteObject) service).setNonBlocking(false);
		((RemoteObject) service).setTransmitReturnValue(true);
		((RemoteObject) service).setResponseTimeout(6000000);
		return service;
	}

	private class Sender extends Thread {

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

	private class RMIInvoker extends Thread {

		private final CyclicBarrier cyclicBarrier;

		private final Queue<Object> queue;

		private final Service service;

		public RMIInvoker(CyclicBarrier cyclicBarrier, Queue<Object> queue, Service service) {
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
				assertThat(service.returnSame(toSend), is(equalTo(toSend)));
				toSend = queue.poll();
			}
		}

	}

	public interface Service {

		<E> E returnSame(E o);
	}
}
