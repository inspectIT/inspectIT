package rocks.inspectit.server.processor.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.EntityManager;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.ci.event.BusinessContextDefinitionUpdateEvent;
import rocks.inspectit.server.dao.impl.BufferInvocationDataDaoImpl;
import rocks.inspectit.server.service.BusinessContextManagementService;
import rocks.inspectit.server.service.ConfigurationInterfaceService;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.all.communication.data.cmr.ApplicationData;
import rocks.inspectit.shared.all.communication.data.cmr.BusinessTransactionData;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.business.expression.impl.StringMatchingExpression;
import rocks.inspectit.shared.cs.ci.business.impl.ApplicationDefinition;
import rocks.inspectit.shared.cs.ci.business.impl.BusinessTransactionDefinition;
import rocks.inspectit.shared.cs.ci.business.valuesource.PatternMatchingType;
import rocks.inspectit.shared.cs.ci.business.valuesource.StringValueSource;
import rocks.inspectit.shared.cs.cmr.service.cache.CachedDataService;

/**
 * @author Alexander Wert
 *
 */
@SuppressWarnings("PMD")
public class BusinessContextRecognitionProcessorTest extends TestBase {
	@InjectMocks
	BusinessContextRecognitionProcessor processor;

	/**
	 * Tests
	 * {@link BusinessContextRecognitionProcessor#assignBusinessContext(InvocationSequenceData)}
	 * method.
	 *
	 */
	public static class Process extends BusinessContextRecognitionProcessorTest {
		private static AtomicInteger idGenerator = new AtomicInteger(100);


		@Mock
		CachedDataService cachedDataService;

		@Mock
		BusinessContextManagementService businessContextManagementService;

		@Mock
		BufferInvocationDataDaoImpl invocationDataDao;

		@Mock
		ConfigurationInterfaceService ciService;

		@Mock
		EntityManager entityManager;

		InvocationSequenceData root;
		InvocationSequenceData level_1_1;
		InvocationSequenceData level_1_2;
		InvocationSequenceData level_2_1;

		ApplicationDefinition applicationDefinition;
		ApplicationDefinition applicationDefinition_empty;
		BusinessTransactionDefinition businessTxDefinition_1;
		BusinessTransactionDefinition businessTxDefinition_2;
		StringValueSource stringValueSource;

		ApplicationData application;
		ApplicationData application_unknown;
		ApplicationData application_empty;
		BusinessTransactionData businessTx_1;
		BusinessTransactionData businessTx_2;
		BusinessTransactionData businessTx_unknown;

		@BeforeMethod
		public void init() throws BusinessException {
			root = new InvocationSequenceData();
			root.setId(1);
			level_1_1 = new InvocationSequenceData();
			level_1_1.setId(2);
			level_1_2 = new InvocationSequenceData();
			level_1_2.setId(3);
			level_2_1 = new InvocationSequenceData();
			level_2_1.setId(4);
			root.getNestedSequences().add(level_1_1);
			root.getNestedSequences().add(level_1_2);
			level_1_2.getNestedSequences().add(level_2_1);

			when(cachedDataService.getApplicationForId(anyInt())).thenReturn(null);
			when(cachedDataService.getBusinessTransactionForId(anyInt(), anyInt())).thenReturn(null);

			applicationDefinition = new ApplicationDefinition(idGenerator.getAndIncrement(), "SomeApplication", null);
			application = new ApplicationData(idGenerator.getAndIncrement(), applicationDefinition.getId(), applicationDefinition.getApplicationName());

			applicationDefinition_empty = new ApplicationDefinition(idGenerator.getAndIncrement(), "EmptyApplication", null);
			application_empty = new ApplicationData(idGenerator.getAndIncrement(), applicationDefinition_empty.getId(), applicationDefinition_empty.getApplicationName());

			application_unknown = new ApplicationData(idGenerator.getAndIncrement(), ApplicationDefinition.DEFAULT_ID, "Unknown Application");

			businessTxDefinition_1 = new BusinessTransactionDefinition(idGenerator.getAndIncrement(), "businessTxDefinition_1", null);
			businessTx_1 = new BusinessTransactionData(idGenerator.getAndIncrement(), businessTxDefinition_1.getId(), application, businessTxDefinition_1.getBusinessTransactionDefinitionName());
			businessTxDefinition_2 = new BusinessTransactionDefinition(idGenerator.getAndIncrement(), "businessTxDefinition_2", null);
			businessTx_2 = new BusinessTransactionData(idGenerator.getAndIncrement(), businessTxDefinition_2.getId(), application, businessTxDefinition_2.getBusinessTransactionDefinitionName());
			businessTx_unknown = new BusinessTransactionData(idGenerator.getAndIncrement(), BusinessTransactionDefinition.DEFAULT_ID, application, "Unknown Transaction");
			applicationDefinition.addBusinessTransactionDefinition(businessTxDefinition_1);
			applicationDefinition.addBusinessTransactionDefinition(businessTxDefinition_2);

			List<ApplicationDefinition> applicationDefinitions = new ArrayList<>();
			applicationDefinitions.add(applicationDefinition);
			applicationDefinitions.add(applicationDefinition_empty);
			when(ciService.getApplicationDefinitions()).thenReturn(applicationDefinitions);

			stringValueSource = mock(StringValueSource.class);
			when(stringValueSource.getStringValues(root, cachedDataService)).thenReturn(new String[] { "node/root/" });
			when(stringValueSource.getStringValues(level_1_1, cachedDataService)).thenReturn(new String[] { "node/level_1_1/" });
			when(stringValueSource.getStringValues(level_1_2, cachedDataService)).thenReturn(new String[] { "node/level_1_2/" });
			when(stringValueSource.getStringValues(level_2_1, cachedDataService)).thenReturn(new String[] { "node/level_2_1/", "node/level_2_1/multiple" });
			when(businessContextManagementService.registerApplication(applicationDefinition)).thenReturn(application);
			when(businessContextManagementService.registerApplication(ApplicationDefinition.DEFAULT_APPLICATION_DEFINITION)).thenReturn(application_unknown);
			when(businessContextManagementService.registerBusinessTransaction(application, businessTxDefinition_1, businessTxDefinition_1.getBusinessTransactionDefinitionName()))
			.thenReturn(businessTx_1);
			when(businessContextManagementService.registerBusinessTransaction(application, businessTxDefinition_2, businessTxDefinition_2.getBusinessTransactionDefinitionName()))
			.thenReturn(businessTx_2);
			when(businessContextManagementService.registerBusinessTransaction(application, applicationDefinition.getBusinessTransactionDefinition(0),
					applicationDefinition.getBusinessTransactionDefinition(0).getBusinessTransactionDefinitionName())).thenReturn(businessTx_unknown);
			when(businessContextManagementService.registerBusinessTransaction(application_unknown, ApplicationDefinition.DEFAULT_APPLICATION_DEFINITION.getBusinessTransactionDefinition(0),
					ApplicationDefinition.DEFAULT_APPLICATION_DEFINITION.getBusinessTransactionDefinition(0).getBusinessTransactionDefinitionName())).thenReturn(businessTx_unknown);
			when(businessContextManagementService.registerApplication(applicationDefinition_empty)).thenReturn(application_empty);
			when(businessContextManagementService.registerBusinessTransaction(application_empty, applicationDefinition_empty.getBusinessTransactionDefinition(0),
					applicationDefinition_empty.getBusinessTransactionDefinition(0).getBusinessTransactionDefinitionName())).thenReturn(businessTx_unknown);
		}

		@Test
		public void match() {
			StringMatchingExpression stringMatchingExpression = new StringMatchingExpression(PatternMatchingType.CONTAINS, "root");
			stringMatchingExpression.setStringValueSource(stringValueSource);
			stringMatchingExpression.setSearchNodeInTrace(false);

			StringMatchingExpression stringMatchingExpression_2 = new StringMatchingExpression(PatternMatchingType.CONTAINS, "nothing");
			stringMatchingExpression_2.setStringValueSource(stringValueSource);
			stringMatchingExpression_2.setSearchNodeInTrace(false);

			applicationDefinition.setMatchingRuleExpression(stringMatchingExpression);
			applicationDefinition_empty.setMatchingRuleExpression(stringMatchingExpression_2);
			businessTxDefinition_1.setMatchingRuleExpression(stringMatchingExpression_2);
			businessTxDefinition_2.setMatchingRuleExpression(stringMatchingExpression);

			processor.process(root, entityManager);

			assertThat(root.getApplicationId(), equalTo(application.getId()));
			assertThat(root.getBusinessTransactionId(), equalTo(businessTx_2.getId()));
		}

		@Test
		public void matchWithSearchInTrace() {
			StringMatchingExpression stringMatchingExpression = new StringMatchingExpression(PatternMatchingType.CONTAINS, "multiple");
			stringMatchingExpression.setStringValueSource(stringValueSource);
			stringMatchingExpression.setSearchNodeInTrace(true);
			stringMatchingExpression.setMaxSearchDepth(2);

			StringMatchingExpression stringMatchingExpression_2 = new StringMatchingExpression(PatternMatchingType.CONTAINS, "nothing");
			stringMatchingExpression_2.setStringValueSource(stringValueSource);
			stringMatchingExpression_2.setSearchNodeInTrace(false);

			applicationDefinition.setMatchingRuleExpression(stringMatchingExpression);
			applicationDefinition_empty.setMatchingRuleExpression(stringMatchingExpression_2);
			businessTxDefinition_1.setMatchingRuleExpression(stringMatchingExpression_2);
			businessTxDefinition_2.setMatchingRuleExpression(stringMatchingExpression);

			processor.process(root, entityManager);

			assertThat(root.getApplicationId(), equalTo(application.getId()));
			assertThat(root.getBusinessTransactionId(), equalTo(businessTx_2.getId()));
		}

		@Test
		public void matchDefaultBT() {
			StringMatchingExpression stringMatchingExpression = new StringMatchingExpression(PatternMatchingType.CONTAINS, "root");
			stringMatchingExpression.setStringValueSource(stringValueSource);
			stringMatchingExpression.setSearchNodeInTrace(false);

			StringMatchingExpression stringMatchingExpression_2 = new StringMatchingExpression(PatternMatchingType.CONTAINS, "nothing");
			stringMatchingExpression_2.setStringValueSource(stringValueSource);
			stringMatchingExpression_2.setSearchNodeInTrace(false);

			applicationDefinition.setMatchingRuleExpression(stringMatchingExpression);
			applicationDefinition_empty.setMatchingRuleExpression(stringMatchingExpression_2);
			businessTxDefinition_1.setMatchingRuleExpression(stringMatchingExpression_2);
			businessTxDefinition_2.setMatchingRuleExpression(stringMatchingExpression_2);

			processor.process(root, entityManager);

			assertThat(root.getApplicationId(), equalTo(application.getId()));
			assertThat(root.getBusinessTransactionId(), equalTo(businessTx_unknown.getId()));
		}

		@Test
		public void matchDefaultAppAndBT() {
			StringMatchingExpression stringMatchingExpression = new StringMatchingExpression(PatternMatchingType.CONTAINS, "nothing");
			stringMatchingExpression.setStringValueSource(stringValueSource);
			stringMatchingExpression.setSearchNodeInTrace(false);

			StringMatchingExpression stringMatchingExpression_2 = new StringMatchingExpression(PatternMatchingType.CONTAINS, "nothing");
			stringMatchingExpression_2.setStringValueSource(stringValueSource);
			stringMatchingExpression_2.setSearchNodeInTrace(false);

			applicationDefinition.setMatchingRuleExpression(stringMatchingExpression);
			applicationDefinition_empty.setMatchingRuleExpression(stringMatchingExpression_2);
			businessTxDefinition_1.setMatchingRuleExpression(stringMatchingExpression_2);
			businessTxDefinition_2.setMatchingRuleExpression(stringMatchingExpression_2);

			processor.process(root, entityManager);

			assertThat(root.getApplicationId(), equalTo(application_unknown.getId()));
			assertThat(root.getBusinessTransactionId(), equalTo(businessTx_unknown.getId()));
		}

		@Test
		public void matchEmptyApplication() {
			StringMatchingExpression stringMatchingExpression = new StringMatchingExpression(PatternMatchingType.CONTAINS, "root");
			stringMatchingExpression.setStringValueSource(stringValueSource);
			stringMatchingExpression.setSearchNodeInTrace(false);

			StringMatchingExpression stringMatchingExpression_2 = new StringMatchingExpression(PatternMatchingType.CONTAINS, "nothing");
			stringMatchingExpression_2.setStringValueSource(stringValueSource);
			stringMatchingExpression_2.setSearchNodeInTrace(false);

			applicationDefinition.setMatchingRuleExpression(stringMatchingExpression_2);
			applicationDefinition_empty.setMatchingRuleExpression(stringMatchingExpression);
			processor.process(root, entityManager);

			assertThat(root.getApplicationId(), equalTo(application_empty.getId()));
			assertThat(root.getBusinessTransactionId(), equalTo(businessTx_unknown.getId()));
		}

		@Test
		public void invalidInputData() {
			TimerData invalidInput = new TimerData();
			processor.process(invalidInput, entityManager);

			verifyNoMoreInteractions(ciService);
		}

		/**
		 * Clean test folder after each test.
		 */
		@AfterMethod
		public void cleanUp() throws IOException {
			root.setApplicationId(-1);
			root.setBusinessTransactionId(-1);
		}
	}

	/**
	 * Tests
	 * {@link BusinessContextRecognitionProcessor#onApplicationEvent(rocks.inspectit.server.ci.event.BusinessContextDefinitionUpdateEvent)}
	 * method.
	 *
	 */
	public static class OnApplicationEvent extends BusinessContextRecognitionProcessorTest {
		private static AtomicInteger idGenerator = new AtomicInteger(100);

		@Mock
		CachedDataService cachedDataService;

		@Mock
		BusinessContextManagementService businessContextManagementService;

		@Mock
		BufferInvocationDataDaoImpl invocationDataDao;

		@Mock
		ConfigurationInterfaceService ciService;

		@Mock
		EntityManager entityManager;

		@Mock
		BusinessContextDefinitionUpdateEvent event;

		@Mock
		ScheduledExecutorService executorService;

		InvocationSequenceData root;
		InvocationSequenceData level_1_1;
		InvocationSequenceData level_1_2;
		InvocationSequenceData level_2_1;

		ApplicationDefinition applicationDefinition;
		BusinessTransactionDefinition businessTxDefinition_1;
		StringValueSource stringValueSource;

		ApplicationData application;
		BusinessTransactionData businessTx_1;

		@BeforeMethod
		public void init() throws BusinessException {
			root = new InvocationSequenceData();
			root.setId(1);
			level_1_1 = new InvocationSequenceData();
			level_1_1.setId(2);
			level_1_2 = new InvocationSequenceData();
			level_1_2.setId(3);
			level_2_1 = new InvocationSequenceData();
			level_2_1.setId(4);
			root.getNestedSequences().add(level_1_1);
			root.getNestedSequences().add(level_1_2);
			level_1_2.getNestedSequences().add(level_2_1);
			root.setApplicationId(-1);
			root.setBusinessTransactionId(-1);

			when(cachedDataService.getApplicationForId(anyInt())).thenReturn(null);
			when(cachedDataService.getBusinessTransactionForId(anyInt(), anyInt())).thenReturn(null);

			applicationDefinition = new ApplicationDefinition(idGenerator.getAndIncrement(), "SomeApplication", null);
			application = new ApplicationData(idGenerator.getAndIncrement(), applicationDefinition.getId(), applicationDefinition.getApplicationName());

			businessTxDefinition_1 = new BusinessTransactionDefinition(idGenerator.getAndIncrement(), "businessTxDefinition_1", null);
			businessTx_1 = new BusinessTransactionData(idGenerator.getAndIncrement(), businessTxDefinition_1.getId(), application, businessTxDefinition_1.getBusinessTransactionDefinitionName());
			applicationDefinition.addBusinessTransactionDefinition(businessTxDefinition_1);

			when(ciService.getApplicationDefinitions()).thenReturn(Collections.singletonList(applicationDefinition));

			stringValueSource = mock(StringValueSource.class);
			when(stringValueSource.getStringValues(root, cachedDataService)).thenReturn(new String[] { "node/root/" });
			when(stringValueSource.getStringValues(level_1_1, cachedDataService)).thenReturn(new String[] { "node/level_1_1/" });
			when(stringValueSource.getStringValues(level_1_2, cachedDataService)).thenReturn(new String[] { "node/level_1_2/" });
			when(stringValueSource.getStringValues(level_2_1, cachedDataService)).thenReturn(new String[] { "node/level_2_1/", "node/level_2_1/multiple" });
			when(businessContextManagementService.registerApplication(applicationDefinition)).thenReturn(application);
			when(businessContextManagementService.registerBusinessTransaction(application, businessTxDefinition_1, businessTxDefinition_1.getBusinessTransactionDefinitionName()))
			.thenReturn(businessTx_1);
			when(invocationDataDao.getInvocationSequenceDetail(0, 0, -1, null, null, null)).thenReturn(Collections.singletonList(root));
			doAnswer(new Answer<Object>() {
				@Override
				public Object answer(InvocationOnMock invocation) throws Exception {
					Object[] arguments = invocation.getArguments();
					Runnable runnable = (Runnable) arguments[0];
					runnable.run();
					return null;
				}
			}).when(executorService).execute(any(Runnable.class));
		}

		@Test
		public void businessContextChanged() throws InterruptedException {
			StringMatchingExpression stringMatchingExpression = new StringMatchingExpression(PatternMatchingType.CONTAINS, "root");
			stringMatchingExpression.setStringValueSource(stringValueSource);
			stringMatchingExpression.setSearchNodeInTrace(false);

			StringMatchingExpression stringMatchingExpression_2 = new StringMatchingExpression(PatternMatchingType.CONTAINS, "node");
			stringMatchingExpression_2.setStringValueSource(stringValueSource);
			stringMatchingExpression_2.setSearchNodeInTrace(false);

			applicationDefinition.setMatchingRuleExpression(stringMatchingExpression);
			businessTxDefinition_1.setMatchingRuleExpression(stringMatchingExpression_2);

			processor.onApplicationEvent(event);

			assertThat(root.getApplicationId(), equalTo(application.getId()));
			assertThat(root.getBusinessTransactionId(), equalTo(businessTx_1.getId()));
		}

		/**
		 * Clean test folder after each test.
		 */
		@AfterMethod
		public void cleanUp() throws IOException {
			root.setApplicationId(-1);
			root.setBusinessTransactionId(-1);
		}
	}
}
