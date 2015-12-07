package rocks.inspectit.shared.cs.ci.business.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.business.expression.impl.NameExtractionExpression;
import rocks.inspectit.shared.cs.cmr.service.cache.CachedDataService;

/**
 * @author Alexander Wert
 *
 */
@SuppressWarnings("PMD")
public class BusinessTransactionDefinitionTest extends TestBase {
	@InjectMocks
	BusinessTransactionDefinition businessTxDefinition;

	/**
	 * Test
	 * {@link BusinessTransactionDefinition#determineBusinessTransactionName(InvocationSequenceData, rocks.inspectit.shared.all.cmr.service.ICachedDataService)}
	 * method.
	 */
	public static class DetermineBusinessTransactionName extends BusinessTransactionDefinitionTest {
		private static final String BUSINESS_TX_DEF_NAME = "bTxName";
		private static final String DYNAMIC_NAME = "dynamicName";

		@Mock
		CachedDataService cachedDataService;

		@Mock
		NameExtractionExpression extractionExpression;

		InvocationSequenceData isd1;

		InvocationSequenceData isd2;

		@BeforeMethod
		public void init() {
			businessTxDefinition.setBusinessTransactionDefinitionName(BUSINESS_TX_DEF_NAME);
			isd1 = new InvocationSequenceData();
			isd1.setId(1);
			isd2 = new InvocationSequenceData();
			isd2.setId(2);
			isd1.getNestedSequences().add(isd2);
		}

		@Test
		public void definitionName() {
			businessTxDefinition.setNameExtractionExpression(null);

			String businessTransactionName = businessTxDefinition.determineBusinessTransactionName(isd1, cachedDataService);

			assertThat(businessTransactionName, equalTo(BUSINESS_TX_DEF_NAME));
		}

		@Test
		public void dynamicNameOnFirstLevel() {
			when(extractionExpression.isSearchNodeInTrace()).thenReturn(false);
			when(extractionExpression.extractName(any(InvocationSequenceData.class), any(CachedDataService.class))).thenReturn(DYNAMIC_NAME);

			String businessTransactionName = businessTxDefinition.determineBusinessTransactionName(isd1, cachedDataService);

			assertThat(businessTransactionName, equalTo(DYNAMIC_NAME));
		}

		@Test
		public void dynamicNameOnSecondLevel() {
			when(extractionExpression.isSearchNodeInTrace()).thenReturn(true);
			when(extractionExpression.getMaxSearchDepth()).thenReturn(1);
			when(extractionExpression.extractName(isd1, cachedDataService)).thenReturn(null);
			when(extractionExpression.extractName(isd2, cachedDataService)).thenReturn(DYNAMIC_NAME);

			String businessTransactionName = businessTxDefinition.determineBusinessTransactionName(isd1, cachedDataService);

			assertThat(businessTransactionName, equalTo(DYNAMIC_NAME));
		}

		@Test
		public void dynamicNameNotMapped() {
			when(extractionExpression.isSearchNodeInTrace()).thenReturn(true);
			when(extractionExpression.getMaxSearchDepth()).thenReturn(1);
			when(extractionExpression.extractName(isd1, cachedDataService)).thenReturn(null);
			when(extractionExpression.extractName(isd2, cachedDataService)).thenReturn(null);

			String businessTransactionName = businessTxDefinition.determineBusinessTransactionName(isd1, cachedDataService);

			assertThat(businessTransactionName, equalTo(BUSINESS_TX_DEF_NAME + NameExtractionExpression.UNKNOWN_DYNAMIC_BUSINESS_TRANSACTION_POSTFIX));
		}

		@Test
		public void dynamicNameNotMappedWithUnlimitedSearchDepth() {
			when(extractionExpression.isSearchNodeInTrace()).thenReturn(true);
			when(extractionExpression.getMaxSearchDepth()).thenReturn(-1);
			when(extractionExpression.extractName(isd1, cachedDataService)).thenReturn(null);
			when(extractionExpression.extractName(isd2, cachedDataService)).thenReturn(null);

			String businessTransactionName = businessTxDefinition.determineBusinessTransactionName(isd1, cachedDataService);

			assertThat(businessTransactionName, equalTo(BUSINESS_TX_DEF_NAME + NameExtractionExpression.UNKNOWN_DYNAMIC_BUSINESS_TRANSACTION_POSTFIX));
		}
	}
}
