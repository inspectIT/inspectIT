package rocks.inspectit.server.processor.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.dao.impl.BufferInvocationDataDaoImpl;
import rocks.inspectit.server.service.BusinessContextManagementService;
import rocks.inspectit.server.service.ConfigurationInterfaceService;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
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
	public static class AssignBusinessContext extends BusinessContextRecognitionProcessorTest {
		final static int APPLICATION_ID = 123;
		final static int BUSINESS_TX_ID_1 = 456;
		final static int BUSINESS_TX_TD_2 = 789;

		@Mock
		CachedDataService cachedDataService;

		@Mock
		BusinessContextManagementService businessContextManagementService;

		@Mock
		BufferInvocationDataDaoImpl invocationDataDao;

		@Mock
		ConfigurationInterfaceService ciService;

		InvocationSequenceData root;
		InvocationSequenceData level_1_1;
		InvocationSequenceData level_1_2;
		InvocationSequenceData level_2_1;

		ApplicationDefinition applicationDefinition;
		BusinessTransactionDefinition businessTxDefinition_1;
		BusinessTransactionDefinition businessTxDefinition_2;
		StringValueSource stringValueSource;

		ApplicationData application;
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

			applicationDefinition = new ApplicationDefinition(APPLICATION_ID, "SomeApplication", null);
			application = new ApplicationData(12345, applicationDefinition.getId(), applicationDefinition.getApplicationName());

			businessTxDefinition_1 = new BusinessTransactionDefinition(BUSINESS_TX_ID_1, "businessTxDefinition_1", null);
			businessTx_1 = new BusinessTransactionData(5678, businessTxDefinition_1.getId(), application, businessTxDefinition_1.getBusinessTransactionDefinitionName());
			businessTxDefinition_2 = new BusinessTransactionDefinition(BUSINESS_TX_TD_2, "businessTxDefinition_2", null);
			businessTx_2 = new BusinessTransactionData(91011, businessTxDefinition_2.getId(), application, businessTxDefinition_2.getBusinessTransactionDefinitionName());
			businessTx_unknown = new BusinessTransactionData(0, 0, application, "Unknown Transaction");
			applicationDefinition.addBusinessTransactionDefinition(businessTxDefinition_1);
			applicationDefinition.addBusinessTransactionDefinition(businessTxDefinition_2);

			when(ciService.getApplicationDefinitions()).thenReturn(Collections.singletonList(applicationDefinition));

			stringValueSource = mock(StringValueSource.class);
			when(stringValueSource.getStringValues(root, cachedDataService)).thenReturn(new String[] { "node/root/" });
			when(stringValueSource.getStringValues(level_1_1, cachedDataService)).thenReturn(new String[] { "node/level_1_1/" });
			when(stringValueSource.getStringValues(level_1_2, cachedDataService)).thenReturn(new String[] { "node/level_1_2/" });
			when(stringValueSource.getStringValues(level_2_1, cachedDataService)).thenReturn(new String[] { "node/level_2_1/", "node/level_2_1/multiple" });
			when(businessContextManagementService.registerApplication(applicationDefinition)).thenReturn(application);
			when(businessContextManagementService.registerBusinessTransaction(application, businessTxDefinition_1, businessTxDefinition_1.getBusinessTransactionDefinitionName()))
					.thenReturn(businessTx_1);
			when(businessContextManagementService.registerBusinessTransaction(application, businessTxDefinition_2, businessTxDefinition_2.getBusinessTransactionDefinitionName()))
					.thenReturn(businessTx_2);
			when(businessContextManagementService.registerBusinessTransaction(application, applicationDefinition.getBusinessTransactionDefinition(0),
					applicationDefinition.getBusinessTransactionDefinition(0).getBusinessTransactionDefinitionName())).thenReturn(businessTx_unknown);
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
			businessTxDefinition_1.setMatchingRuleExpression(stringMatchingExpression_2);
			businessTxDefinition_2.setMatchingRuleExpression(stringMatchingExpression);

			processor.assignBusinessContext(root);

			assertThat(root.getApplicationId(), equalTo(application.getId()));
			assertThat(root.getBusinessTransactionId(), not(equalTo(0)));
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
			businessTxDefinition_1.setMatchingRuleExpression(stringMatchingExpression_2);
			businessTxDefinition_2.setMatchingRuleExpression(stringMatchingExpression);

			processor.assignBusinessContext(root);

			assertThat(root.getApplicationId(), equalTo(application.getId()));
			assertThat(root.getBusinessTransactionId(), equalTo(businessTx_2.getId()));
		}

		@Test
		public void noMatch() {
			StringMatchingExpression stringMatchingExpression = new StringMatchingExpression(PatternMatchingType.CONTAINS, "root");
			stringMatchingExpression.setStringValueSource(stringValueSource);
			stringMatchingExpression.setSearchNodeInTrace(false);

			StringMatchingExpression stringMatchingExpression_2 = new StringMatchingExpression(PatternMatchingType.CONTAINS, "nothing");
			stringMatchingExpression_2.setStringValueSource(stringValueSource);
			stringMatchingExpression_2.setSearchNodeInTrace(false);

			applicationDefinition.setMatchingRuleExpression(stringMatchingExpression);
			businessTxDefinition_1.setMatchingRuleExpression(stringMatchingExpression_2);
			businessTxDefinition_2.setMatchingRuleExpression(stringMatchingExpression_2);

			processor.assignBusinessContext(root);

			assertThat(root.getApplicationId(), not(equalTo(0)));
			assertThat(root.getBusinessTransactionId(), equalTo(0));
		}
	}
}
