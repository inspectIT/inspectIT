package info.novatec.inspectit.ci.business.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import info.novatec.inspectit.ci.business.expression.impl.NameExtractionExpression;
import info.novatec.inspectit.cmr.service.cache.CachedDataService;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.testbase.TestBase;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Alexander Wert
 *
 */
public class BusinessTransactionDefinitionTest extends TestBase {
	@InjectMocks
	BusinessTransactionDefinition businessTxDefinition;

	/**
	 * Test
	 * {@link BusinessTransactionDefinition#determineBusinessTransactionName(info.novatec.inspectit.communication.data.InvocationSequenceData, info.novatec.inspectit.cmr.service.ICachedDataService)}
	 * method.
	 */
	public static class DetermineBusinessTransactionName extends BusinessTransactionDefinitionTest {
		private static final String BUSINESS_TX_DEF_NAME = "bTxName";
		private static final String DYNAMIC_NAME = "dynamicName";

		@Mock
		CachedDataService cachedDataService;

		@Mock
		NameExtractionExpression extractionExpression;

		InvocationSequenceData isd_1;

		InvocationSequenceData isd_2;

		@BeforeMethod
		public void init() {
			businessTxDefinition.setBusinessTransactionDefinitionName(BUSINESS_TX_DEF_NAME);
			isd_1 = new InvocationSequenceData();
			isd_2 = new InvocationSequenceData();
			isd_1.getNestedSequences().add(isd_2);
		}

		@Test
		public void definitionName() {
			businessTxDefinition.setNameExtractionExpression(null);

			assertThat(businessTxDefinition.determineBusinessTransactionName(isd_1, cachedDataService), equalTo(BUSINESS_TX_DEF_NAME));
		}

		@Test
		public void dynamicNameOnFirstLevel() {
			when(extractionExpression.getSearchNodeInTrace()).thenReturn(false);
			when(extractionExpression.extractName(any(InvocationSequenceData.class), any(CachedDataService.class))).thenReturn(DYNAMIC_NAME);

			assertThat(businessTxDefinition.determineBusinessTransactionName(isd_1, cachedDataService), equalTo(DYNAMIC_NAME));
		}

		@Test
		public void dynamicNameOnSecondLevel() {
			when(extractionExpression.getSearchNodeInTrace()).thenReturn(true);
			when(extractionExpression.getMaxSearchDepth()).thenReturn(1);
			when(extractionExpression.extractName(isd_1, cachedDataService)).thenReturn(null);
			when(extractionExpression.extractName(isd_2, cachedDataService)).thenReturn(DYNAMIC_NAME);

			assertThat(businessTxDefinition.determineBusinessTransactionName(isd_1, cachedDataService), equalTo(DYNAMIC_NAME));
		}

		@Test
		public void dynamicNameNotMapped() {
			when(extractionExpression.getSearchNodeInTrace()).thenReturn(true);
			when(extractionExpression.getMaxSearchDepth()).thenReturn(1);
			when(extractionExpression.extractName(isd_1, cachedDataService)).thenReturn(null);
			when(extractionExpression.extractName(isd_2, cachedDataService)).thenReturn(null);

			assertThat(businessTxDefinition.determineBusinessTransactionName(isd_1, cachedDataService),
					equalTo(BUSINESS_TX_DEF_NAME + NameExtractionExpression.UNKNOWN_DYNAMIC_BUSINESS_TRANSACTION_POSTFIX));
		}
	}
}
