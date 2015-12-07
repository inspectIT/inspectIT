/**
 *
 */
package info.novatec.inspectit.cmr.processor.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import info.novatec.inspectit.ci.business.impl.AndExpression;
import info.novatec.inspectit.ci.business.impl.ApplicationDefinition;
import info.novatec.inspectit.ci.business.impl.BooleanExpression;
import info.novatec.inspectit.ci.business.impl.BusinessTransactionDefinition;
import info.novatec.inspectit.ci.business.impl.NameExtractionExpression;
import info.novatec.inspectit.ci.business.impl.NotExpression;
import info.novatec.inspectit.ci.business.impl.OrExpression;
import info.novatec.inspectit.ci.business.impl.PatternMatchingType;
import info.novatec.inspectit.ci.business.impl.StringMatchingExpression;
import info.novatec.inspectit.ci.business.impl.StringValueSource;
import info.novatec.inspectit.cmr.dao.impl.BufferInvocationDataDaoImpl;
import info.novatec.inspectit.cmr.service.BusinessContextManagementService;
import info.novatec.inspectit.cmr.service.cache.CachedDataService;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.exception.BusinessException;
import info.novatec.inspectit.testbase.TestBase;

import java.util.Collections;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Alexander Wert
 *
 */
public class ExpressionEvaluationTest extends TestBase {
	final int applicationId = 123;
	final int businessTxId_1 = 456;
	final int businessTxId_2 = 789;

	@InjectMocks
	ExpressionEvaluation expressionEvaluation;

	@Mock
	CachedDataService cachedDataService;

	@Mock
	BusinessContextManagementService businessContextManagementService;

	@Mock
	BufferInvocationDataDaoImpl invocationDataDao;

	InvocationSequenceData root;
	InvocationSequenceData level_1_1;
	InvocationSequenceData level_1_2;
	InvocationSequenceData level_2_1;

	ApplicationDefinition applicationDefinition;
	BusinessTransactionDefinition businessTxDefinition_1;
	BusinessTransactionDefinition businessTxDefinition_2;

	@BeforeMethod
	public void init() throws BusinessException {
		MockitoAnnotations.initMocks(this);

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

		applicationDefinition = new ApplicationDefinition(applicationId, "SomeApplication", null);
		businessTxDefinition_1 = new BusinessTransactionDefinition(businessTxId_1, "businessTxDefinition_1", null);
		businessTxDefinition_2 = new BusinessTransactionDefinition(businessTxId_2, "businessTxDefinition_2", null);
		applicationDefinition.addBusinessTransactionDefinition(businessTxDefinition_1);
		applicationDefinition.addBusinessTransactionDefinition(businessTxDefinition_2);

		when(businessContextManagementService.getApplicationDefinitions()).thenReturn(Collections.singletonList(applicationDefinition));
	}

	/**
	 * Tests {@link ExpressionEvaluation#assignBusinessContext(InvocationSequenceData)} method.
	 *
	 */
	public static class AssignBusinessContext extends ExpressionEvaluationTest {
		StringValueSource stringValueSource;

		@BeforeMethod
		public void initialize() {
			stringValueSource = mock(StringValueSource.class);
			when(stringValueSource.getStringValues(root, cachedDataService)).thenReturn(new String[] { "node/root/" });

			when(stringValueSource.getStringValues(level_1_1, cachedDataService)).thenReturn(new String[] { "node/level_1_1/" });

			when(stringValueSource.getStringValues(level_1_2, cachedDataService)).thenReturn(new String[] { "node/level_1_2/" });

			when(stringValueSource.getStringValues(level_2_1, cachedDataService)).thenReturn(new String[] { "node/level_2_1/", "node/level_2_1/multiple" });
		}

		@Test
		public void simpleStringMatching() {
			StringMatchingExpression stringMatchingExpression = new StringMatchingExpression(PatternMatchingType.CONTAINS, "root");
			stringMatchingExpression.setStringValueSource(stringValueSource);
			stringMatchingExpression.setSearchNodeInTrace(false);

			StringMatchingExpression stringMatchingExpression_2 = new StringMatchingExpression(PatternMatchingType.CONTAINS, "nothing");
			stringMatchingExpression_2.setStringValueSource(stringValueSource);
			stringMatchingExpression_2.setSearchNodeInTrace(false);

			applicationDefinition.setMatchingRuleExpression(stringMatchingExpression);
			businessTxDefinition_1.setMatchingRuleExpression(stringMatchingExpression_2);
			businessTxDefinition_2.setMatchingRuleExpression(stringMatchingExpression);

			expressionEvaluation.assignBusinessContext(root);

			assertThat(root.getApplicationId(), equalTo(applicationDefinition.createApplicationId()));
			assertThat(root.getBusinessTransactionId(), not(equalTo(businessTxDefinition_1.createBusinessTransactionId(businessTxDefinition_1.getBusinessTransactionDefinitionName()))));
			assertThat(root.getBusinessTransactionId(), equalTo(businessTxDefinition_2.createBusinessTransactionId(businessTxDefinition_2.getBusinessTransactionDefinitionName())));
		}

		@Test
		public void simpleStringMatchingWithEquals() {
			StringMatchingExpression stringMatchingExpression = new StringMatchingExpression(PatternMatchingType.EQUALS, "node/root/");
			stringMatchingExpression.setStringValueSource(stringValueSource);
			stringMatchingExpression.setSearchNodeInTrace(false);

			StringMatchingExpression stringMatchingExpression_2 = new StringMatchingExpression(PatternMatchingType.EQUALS, "nothing");
			stringMatchingExpression_2.setStringValueSource(stringValueSource);
			stringMatchingExpression_2.setSearchNodeInTrace(false);

			applicationDefinition.setMatchingRuleExpression(stringMatchingExpression);
			businessTxDefinition_1.setMatchingRuleExpression(stringMatchingExpression_2);
			businessTxDefinition_2.setMatchingRuleExpression(stringMatchingExpression);

			expressionEvaluation.assignBusinessContext(root);

			assertThat(root.getApplicationId(), equalTo(applicationDefinition.createApplicationId()));
			assertThat(root.getBusinessTransactionId(), not(equalTo(businessTxDefinition_1.createBusinessTransactionId(businessTxDefinition_1.getBusinessTransactionDefinitionName()))));
			assertThat(root.getBusinessTransactionId(), equalTo(businessTxDefinition_2.createBusinessTransactionId(businessTxDefinition_2.getBusinessTransactionDefinitionName())));
		}

		@Test
		public void simpleStringMatchingWithStartsWith() {
			StringMatchingExpression stringMatchingExpression = new StringMatchingExpression(PatternMatchingType.STARTS_WITH, "node/r");
			stringMatchingExpression.setStringValueSource(stringValueSource);
			stringMatchingExpression.setSearchNodeInTrace(false);

			StringMatchingExpression stringMatchingExpression_2 = new StringMatchingExpression(PatternMatchingType.STARTS_WITH, "nothing");
			stringMatchingExpression_2.setStringValueSource(stringValueSource);
			stringMatchingExpression_2.setSearchNodeInTrace(false);

			applicationDefinition.setMatchingRuleExpression(stringMatchingExpression);
			businessTxDefinition_1.setMatchingRuleExpression(stringMatchingExpression_2);
			businessTxDefinition_2.setMatchingRuleExpression(stringMatchingExpression);

			expressionEvaluation.assignBusinessContext(root);

			assertThat(root.getApplicationId(), equalTo(applicationDefinition.createApplicationId()));
			assertThat(root.getBusinessTransactionId(), not(equalTo(businessTxDefinition_1.createBusinessTransactionId(businessTxDefinition_1.getBusinessTransactionDefinitionName()))));
			assertThat(root.getBusinessTransactionId(), equalTo(businessTxDefinition_2.createBusinessTransactionId(businessTxDefinition_2.getBusinessTransactionDefinitionName())));
		}

		@Test
		public void simpleStringMatchingWithEndsWith() {
			StringMatchingExpression stringMatchingExpression = new StringMatchingExpression(PatternMatchingType.ENDS_WITH, "oot/");
			stringMatchingExpression.setStringValueSource(stringValueSource);
			stringMatchingExpression.setSearchNodeInTrace(false);

			StringMatchingExpression stringMatchingExpression_2 = new StringMatchingExpression(PatternMatchingType.ENDS_WITH, "nothing");
			stringMatchingExpression_2.setStringValueSource(stringValueSource);
			stringMatchingExpression_2.setSearchNodeInTrace(false);

			applicationDefinition.setMatchingRuleExpression(stringMatchingExpression);
			businessTxDefinition_1.setMatchingRuleExpression(stringMatchingExpression_2);
			businessTxDefinition_2.setMatchingRuleExpression(stringMatchingExpression);

			expressionEvaluation.assignBusinessContext(root);

			assertThat(root.getApplicationId(), equalTo(applicationDefinition.createApplicationId()));
			assertThat(root.getBusinessTransactionId(), not(equalTo(businessTxDefinition_1.createBusinessTransactionId(businessTxDefinition_1.getBusinessTransactionDefinitionName()))));
			assertThat(root.getBusinessTransactionId(), equalTo(businessTxDefinition_2.createBusinessTransactionId(businessTxDefinition_2.getBusinessTransactionDefinitionName())));
		}

		@Test
		public void simpleStringMatchingWithRegEx() {
			StringMatchingExpression stringMatchingExpression = new StringMatchingExpression(PatternMatchingType.REGEX, ".*oo.*");
			stringMatchingExpression.setStringValueSource(stringValueSource);
			stringMatchingExpression.setSearchNodeInTrace(false);

			StringMatchingExpression stringMatchingExpression_2 = new StringMatchingExpression(PatternMatchingType.REGEX, "nothing");
			stringMatchingExpression_2.setStringValueSource(stringValueSource);
			stringMatchingExpression_2.setSearchNodeInTrace(false);

			applicationDefinition.setMatchingRuleExpression(stringMatchingExpression);
			businessTxDefinition_1.setMatchingRuleExpression(stringMatchingExpression_2);
			businessTxDefinition_2.setMatchingRuleExpression(stringMatchingExpression);

			expressionEvaluation.assignBusinessContext(root);

			assertThat(root.getApplicationId(), equalTo(applicationDefinition.createApplicationId()));
			assertThat(root.getBusinessTransactionId(), not(equalTo(businessTxDefinition_1.createBusinessTransactionId(businessTxDefinition_1.getBusinessTransactionDefinitionName()))));
			assertThat(root.getBusinessTransactionId(), equalTo(businessTxDefinition_2.createBusinessTransactionId(businessTxDefinition_2.getBusinessTransactionDefinitionName())));
		}

		@Test
		public void searchInDepthMatching() {
			StringMatchingExpression stringMatchingExpression = new StringMatchingExpression(PatternMatchingType.CONTAINS, "root");
			stringMatchingExpression.setStringValueSource(stringValueSource);
			stringMatchingExpression.setSearchNodeInTrace(false);

			StringMatchingExpression stringMatchingExpression_2 = new StringMatchingExpression(PatternMatchingType.CONTAINS, "level_1_2");
			stringMatchingExpression_2.setStringValueSource(stringValueSource);
			stringMatchingExpression_2.setSearchNodeInTrace(true);
			stringMatchingExpression_2.setMaxSearchDepth(1);

			applicationDefinition.setMatchingRuleExpression(stringMatchingExpression);
			businessTxDefinition_1.setMatchingRuleExpression(stringMatchingExpression_2);
			businessTxDefinition_2.setMatchingRuleExpression(stringMatchingExpression);

			expressionEvaluation.assignBusinessContext(root);

			assertThat(root.getApplicationId(), equalTo(applicationDefinition.createApplicationId()));
			assertThat(root.getBusinessTransactionId(), equalTo(businessTxDefinition_1.createBusinessTransactionId(businessTxDefinition_1.getBusinessTransactionDefinitionName())));
			assertThat(root.getBusinessTransactionId(), not(equalTo(businessTxDefinition_2.createBusinessTransactionId(businessTxDefinition_2.getBusinessTransactionDefinitionName()))));
		}

		@Test
		public void searchInDepthLimitedMatching() {
			StringMatchingExpression stringMatchingExpression = new StringMatchingExpression(PatternMatchingType.CONTAINS, "nothing");
			stringMatchingExpression.setStringValueSource(stringValueSource);
			stringMatchingExpression.setSearchNodeInTrace(false);

			StringMatchingExpression stringMatchingExpression_2 = new StringMatchingExpression(PatternMatchingType.CONTAINS, "level_2_1");
			stringMatchingExpression_2.setStringValueSource(stringValueSource);
			stringMatchingExpression_2.setSearchNodeInTrace(true);
			stringMatchingExpression_2.setMaxSearchDepth(1);

			applicationDefinition.setMatchingRuleExpression(stringMatchingExpression_2);
			businessTxDefinition_1.setMatchingRuleExpression(stringMatchingExpression_2);
			businessTxDefinition_2.setMatchingRuleExpression(stringMatchingExpression_2);

			expressionEvaluation.assignBusinessContext(root);

			assertThat(root.getApplicationId(), equalTo(0));

		}

		@Test
		public void multiValueMatching() {
			StringMatchingExpression stringMatchingExpression = new StringMatchingExpression(PatternMatchingType.CONTAINS, "root");
			stringMatchingExpression.setStringValueSource(stringValueSource);
			stringMatchingExpression.setSearchNodeInTrace(false);

			StringMatchingExpression stringMatchingExpression_2 = new StringMatchingExpression(PatternMatchingType.CONTAINS, "multiple");
			stringMatchingExpression_2.setStringValueSource(stringValueSource);
			stringMatchingExpression_2.setSearchNodeInTrace(true);
			stringMatchingExpression_2.setMaxSearchDepth(-1);

			applicationDefinition.setMatchingRuleExpression(stringMatchingExpression);
			businessTxDefinition_1.setMatchingRuleExpression(stringMatchingExpression_2);
			businessTxDefinition_2.setMatchingRuleExpression(stringMatchingExpression);

			expressionEvaluation.assignBusinessContext(root);

			assertThat(root.getApplicationId(), equalTo(applicationDefinition.createApplicationId()));
			assertThat(root.getBusinessTransactionId(), equalTo(businessTxDefinition_1.createBusinessTransactionId(businessTxDefinition_1.getBusinessTransactionDefinitionName())));
			assertThat(root.getBusinessTransactionId(), not(equalTo(businessTxDefinition_2.createBusinessTransactionId(businessTxDefinition_2.getBusinessTransactionDefinitionName()))));
		}

		@Test
		public void andMatch() {
			StringMatchingExpression stringMatchingExpression = new StringMatchingExpression(PatternMatchingType.CONTAINS, "node");
			stringMatchingExpression.setStringValueSource(stringValueSource);
			stringMatchingExpression.setSearchNodeInTrace(false);

			StringMatchingExpression stringMatchingExpression_2 = new StringMatchingExpression(PatternMatchingType.CONTAINS, "root");
			stringMatchingExpression_2.setStringValueSource(stringValueSource);
			stringMatchingExpression_2.setSearchNodeInTrace(false);

			AndExpression andExpression = new AndExpression(stringMatchingExpression, stringMatchingExpression_2);

			applicationDefinition.setMatchingRuleExpression(andExpression);
			businessTxDefinition_1.setMatchingRuleExpression(andExpression);

			expressionEvaluation.assignBusinessContext(root);

			assertThat(root.getApplicationId(), equalTo(applicationDefinition.createApplicationId()));
			assertThat(root.getBusinessTransactionId(), equalTo(businessTxDefinition_1.createBusinessTransactionId(businessTxDefinition_1.getBusinessTransactionDefinitionName())));
		}

		@Test
		public void andMatchNegative() {
			StringMatchingExpression stringMatchingExpression = new StringMatchingExpression(PatternMatchingType.CONTAINS, "node");
			stringMatchingExpression.setStringValueSource(stringValueSource);
			stringMatchingExpression.setSearchNodeInTrace(false);

			StringMatchingExpression stringMatchingExpression_2 = new StringMatchingExpression(PatternMatchingType.CONTAINS, "nothing");
			stringMatchingExpression_2.setStringValueSource(stringValueSource);
			stringMatchingExpression_2.setSearchNodeInTrace(false);

			AndExpression andExpression = new AndExpression(stringMatchingExpression, stringMatchingExpression_2);

			applicationDefinition.setMatchingRuleExpression(andExpression);
			businessTxDefinition_1.setMatchingRuleExpression(andExpression);

			expressionEvaluation.assignBusinessContext(root);

			assertThat(root.getApplicationId(), equalTo(0));
		}

		@Test
		public void orMatch() {
			StringMatchingExpression stringMatchingExpression = new StringMatchingExpression(PatternMatchingType.CONTAINS, "node");
			stringMatchingExpression.setStringValueSource(stringValueSource);
			stringMatchingExpression.setSearchNodeInTrace(false);

			StringMatchingExpression stringMatchingExpression_2 = new StringMatchingExpression(PatternMatchingType.CONTAINS, "nothing");
			stringMatchingExpression_2.setStringValueSource(stringValueSource);
			stringMatchingExpression_2.setSearchNodeInTrace(false);

			OrExpression orExpression = new OrExpression(stringMatchingExpression, stringMatchingExpression_2);

			applicationDefinition.setMatchingRuleExpression(orExpression);
			businessTxDefinition_1.setMatchingRuleExpression(orExpression);

			expressionEvaluation.assignBusinessContext(root);

			assertThat(root.getApplicationId(), equalTo(applicationDefinition.createApplicationId()));
			assertThat(root.getBusinessTransactionId(), equalTo(businessTxDefinition_1.createBusinessTransactionId(businessTxDefinition_1.getBusinessTransactionDefinitionName())));
		}

		@Test
		public void orMatchNegative() {
			StringMatchingExpression stringMatchingExpression = new StringMatchingExpression(PatternMatchingType.CONTAINS, "invalid");
			stringMatchingExpression.setStringValueSource(stringValueSource);
			stringMatchingExpression.setSearchNodeInTrace(false);

			StringMatchingExpression stringMatchingExpression_2 = new StringMatchingExpression(PatternMatchingType.CONTAINS, "nothing");
			stringMatchingExpression_2.setStringValueSource(stringValueSource);
			stringMatchingExpression_2.setSearchNodeInTrace(false);

			OrExpression orExpression = new OrExpression(stringMatchingExpression, stringMatchingExpression_2);

			applicationDefinition.setMatchingRuleExpression(orExpression);
			businessTxDefinition_1.setMatchingRuleExpression(orExpression);

			expressionEvaluation.assignBusinessContext(root);

			assertThat(root.getApplicationId(), equalTo(0));
		}

		@Test
		public void notMatch() {
			StringMatchingExpression stringMatchingExpression = new StringMatchingExpression(PatternMatchingType.CONTAINS, "nothing");
			stringMatchingExpression.setStringValueSource(stringValueSource);
			stringMatchingExpression.setSearchNodeInTrace(false);

			NotExpression notExpression = new NotExpression(stringMatchingExpression);

			applicationDefinition.setMatchingRuleExpression(notExpression);
			businessTxDefinition_1.setMatchingRuleExpression(notExpression);

			expressionEvaluation.assignBusinessContext(root);

			assertThat(root.getApplicationId(), equalTo(applicationDefinition.createApplicationId()));
			assertThat(root.getBusinessTransactionId(), equalTo(businessTxDefinition_1.createBusinessTransactionId(businessTxDefinition_1.getBusinessTransactionDefinitionName())));
		}

		@Test
		public void booleanMatch() {
			applicationDefinition.setMatchingRuleExpression(new BooleanExpression(true));
			businessTxDefinition_1.setMatchingRuleExpression(new BooleanExpression(true));

			expressionEvaluation.assignBusinessContext(root);

			assertThat(root.getApplicationId(), equalTo(applicationDefinition.createApplicationId()));
			assertThat(root.getBusinessTransactionId(), equalTo(businessTxDefinition_1.createBusinessTransactionId(businessTxDefinition_1.getBusinessTransactionDefinitionName())));
		}

		@Test
		public void booleanMatchNegative() {
			applicationDefinition.setMatchingRuleExpression(new BooleanExpression(false));
			businessTxDefinition_1.setMatchingRuleExpression(new BooleanExpression(false));

			expressionEvaluation.assignBusinessContext(root);

			assertThat(root.getApplicationId(), equalTo(0));
		}

		@Test
		public void simpleDynamicNameExtraction() {
			NameExtractionExpression nameExtractionExpr = new NameExtractionExpression();
			nameExtractionExpr.setRegularExpression("node/([^/]*)/");
			nameExtractionExpr.setTargetNamePattern("Name:(1)");
			nameExtractionExpr.setStringValueSource(stringValueSource);

			StringMatchingExpression stringMatchingExpression = new StringMatchingExpression(PatternMatchingType.CONTAINS, "root");
			stringMatchingExpression.setStringValueSource(stringValueSource);
			stringMatchingExpression.setSearchNodeInTrace(false);

			StringMatchingExpression stringMatchingExpression_2 = new StringMatchingExpression(PatternMatchingType.CONTAINS, "nothing");
			stringMatchingExpression_2.setStringValueSource(stringValueSource);
			stringMatchingExpression_2.setSearchNodeInTrace(false);

			applicationDefinition.setMatchingRuleExpression(stringMatchingExpression);
			businessTxDefinition_1.setMatchingRuleExpression(stringMatchingExpression_2);
			businessTxDefinition_2.setMatchingRuleExpression(stringMatchingExpression);
			businessTxDefinition_2.setNameExtractionExpression(nameExtractionExpr);

			expressionEvaluation.assignBusinessContext(root);

			assertThat(root.getApplicationId(), equalTo(applicationDefinition.createApplicationId()));
			assertThat(root.getBusinessTransactionId(), not(equalTo(businessTxDefinition_2.createBusinessTransactionId(businessTxDefinition_2.getBusinessTransactionDefinitionName()))));
			assertThat(root.getBusinessTransactionId(), equalTo(businessTxDefinition_2.createBusinessTransactionId("Name:root")));
		}

		@Test
		public void searchInDepthDynamicNameExtraction() {
			NameExtractionExpression nameExtractionExpr = new NameExtractionExpression();
			nameExtractionExpr.setRegularExpression("node/level_2_1/(m.*)");
			nameExtractionExpr.setTargetNamePattern("Name:(1)");
			nameExtractionExpr.setStringValueSource(stringValueSource);
			nameExtractionExpr.setSearchNodeInTrace(true);
			nameExtractionExpr.setMaxSearchDepth(-1);

			StringMatchingExpression stringMatchingExpression = new StringMatchingExpression(PatternMatchingType.CONTAINS, "root");
			stringMatchingExpression.setStringValueSource(stringValueSource);
			stringMatchingExpression.setSearchNodeInTrace(false);

			applicationDefinition.setMatchingRuleExpression(stringMatchingExpression);
			businessTxDefinition_1.setMatchingRuleExpression(stringMatchingExpression);
			businessTxDefinition_1.setNameExtractionExpression(nameExtractionExpr);

			expressionEvaluation.assignBusinessContext(root);

			assertThat(root.getApplicationId(), equalTo(applicationDefinition.createApplicationId()));
			assertThat(root.getBusinessTransactionId(), equalTo(businessTxDefinition_1.createBusinessTransactionId("Name:multiple")));
		}
	}

}
