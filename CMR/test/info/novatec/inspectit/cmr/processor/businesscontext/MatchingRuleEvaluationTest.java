package info.novatec.inspectit.cmr.processor.businesscontext;

import info.novatec.inspectit.ci.business.HostValueSource;
import info.novatec.inspectit.ci.business.HttpParameterValueSource;
import info.novatec.inspectit.ci.business.HttpUriValueSource;
import info.novatec.inspectit.ci.business.MatchingRule;
import info.novatec.inspectit.ci.business.MethodSignatureValueSource;
import info.novatec.inspectit.ci.business.PatternMatchingType;
import info.novatec.inspectit.ci.business.StringMatchingExpression;
import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.processor.businesscontext.AndEvalStrategy;
import info.novatec.inspectit.cmr.processor.businesscontext.ExpressionEvaluation;
import info.novatec.inspectit.cmr.processor.businesscontext.NotEvalStrategy;
import info.novatec.inspectit.cmr.processor.businesscontext.OrEvalStrategy;
import info.novatec.inspectit.cmr.processor.businesscontext.StringMatchingEvalStrategy;
import info.novatec.inspectit.cmr.service.cache.CachedDataService;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MatchingRuleEvaluationTest {
	private static final String APPLICATION = "myApplication";
	private static final String B_TX_1 = "myBusinessTx1";
	private static final String B_TX_2 = "anotherBusinessTx";
	private static final String PARAM_NAME = "aHttpParameter";
	private static final String URI = APPLICATION + "/" + B_TX_1;
	private static final String IP = "10.10.10.10";

	private static InvocationSequenceData iSeqRoot;

	private ExpressionEvaluation evaluation;

	private NotEvalStrategy notStrategy = new NotEvalStrategy(null);

	private AndEvalStrategy andStrategy = new AndEvalStrategy(null);

	private OrEvalStrategy orStrategy = new OrEvalStrategy(null);

	@InjectMocks
	private StringMatchingEvalStrategy stringStrategy = new StringMatchingEvalStrategy(null);

	@Spy
	private CachedDataService cachedDataService;

	@BeforeClass
	public static void initializeInvocationSequence() {
		// root node
		iSeqRoot = new InvocationSequenceData();
		iSeqRoot.setMethodIdent(1);
		iSeqRoot.setPlatformIdent(1);
		iSeqRoot.setChildCount(3);

		// root HTTP timer data
		HttpTimerData httpData = new HttpTimerData();
		httpData.setUri(URI);
		Map<String, String[]> parameters = new HashMap<String, String[]>();
		String[] strArray = new String[1];
		strArray[0] = B_TX_2;
		parameters.put(PARAM_NAME, strArray);
		httpData.setParameters(parameters);
		httpData.setPlatformIdent(1);
		iSeqRoot.setTimerData(httpData);

		// first level children
		InvocationSequenceData child_level_1_a = new InvocationSequenceData();
		child_level_1_a.setParentSequence(iSeqRoot);
		child_level_1_a.setMethodIdent(2);
		child_level_1_a.setPlatformIdent(1);
		InvocationSequenceData child_level_1_b = new InvocationSequenceData();
		child_level_1_b.setParentSequence(iSeqRoot);
		child_level_1_b.setMethodIdent(3);
		child_level_1_b.setPlatformIdent(1);
		List<InvocationSequenceData> children_level_1 = new ArrayList<InvocationSequenceData>();
		children_level_1.add(child_level_1_a);
		children_level_1.add(child_level_1_b);
		iSeqRoot.setNestedSequences(children_level_1);

		// second level child
		InvocationSequenceData child_level_2 = new InvocationSequenceData();
		child_level_2.setParentSequence(iSeqRoot);
		child_level_2.setMethodIdent(4);
		child_level_2.setPlatformIdent(1);
		List<InvocationSequenceData> children_level_2 = new ArrayList<InvocationSequenceData>();
		children_level_2.add(child_level_2);
		child_level_1_b.setNestedSequences(children_level_2);

	}

	@BeforeMethod
	public void initEvaluation() {

	}

	@BeforeClass
	public void initMocks() {
		MockitoAnnotations.initMocks(this);

		// prepare mock for data access service
		for (int i = 1; i <= 4; i++) {
			MethodIdent mIdent = new MethodIdent();
			mIdent.setId(1L);
			mIdent.setClassName("Class" + i);
			mIdent.setMethodName("method" + i);
			mIdent.setPackageName("package" + i);
			Mockito.doReturn(mIdent).when(cachedDataService).getMethodIdentForId(i);
		}

		PlatformIdent pIdent = new PlatformIdent();
		List<String> ips = new ArrayList<String>();
		ips.add(IP);
		pIdent.setDefinedIPs(ips);
		Mockito.doReturn(pIdent).when(cachedDataService).getPlatformIdentForId(1);

		evaluation = new ExpressionEvaluation(notStrategy, andStrategy, orStrategy, stringStrategy);
		notStrategy.expressionEvaluation = evaluation;
		andStrategy.expressionEvaluation = evaluation;
		orStrategy.expressionEvaluation = evaluation;
		stringStrategy.expressionEvaluation = evaluation;
	}

	@Test
	private void testHTTPUriMatching() {
		// check positive contains
		StringMatchingExpression strMatchingExpression_positive = new StringMatchingExpression(PatternMatchingType.CONTAINS, APPLICATION);
		strMatchingExpression_positive.setSearchNodeInTrace(false);
		strMatchingExpression_positive.setStringValueSource(new HttpUriValueSource());

		Assert.assertTrue(evaluation.evaluate(new MatchingRule(strMatchingExpression_positive), iSeqRoot));

		// check positive starts with
		strMatchingExpression_positive = new StringMatchingExpression(PatternMatchingType.STARTS_WITH, URI.substring(0, 5));
		strMatchingExpression_positive.setSearchNodeInTrace(false);
		strMatchingExpression_positive.setStringValueSource(new HttpUriValueSource());

		Assert.assertTrue(evaluation.evaluate(new MatchingRule(strMatchingExpression_positive), iSeqRoot));

		// check positive ends with
		strMatchingExpression_positive = new StringMatchingExpression(PatternMatchingType.ENDS_WITH, URI.substring(7));
		strMatchingExpression_positive.setSearchNodeInTrace(false);
		strMatchingExpression_positive.setStringValueSource(new HttpUriValueSource());

		Assert.assertTrue(evaluation.evaluate(new MatchingRule(strMatchingExpression_positive), iSeqRoot));

		// check positive equals
		strMatchingExpression_positive = new StringMatchingExpression(PatternMatchingType.EQUALS, URI);
		strMatchingExpression_positive.setSearchNodeInTrace(false);
		strMatchingExpression_positive.setStringValueSource(new HttpUriValueSource());

		Assert.assertTrue(evaluation.evaluate(new MatchingRule(strMatchingExpression_positive), iSeqRoot));

		// check negative contains
		StringMatchingExpression strMatchingExpression_negative = new StringMatchingExpression(PatternMatchingType.CONTAINS, "someString");
		strMatchingExpression_negative.setSearchNodeInTrace(false);
		strMatchingExpression_negative.setStringValueSource(new HttpUriValueSource());

		Assert.assertFalse(evaluation.evaluate(new MatchingRule(strMatchingExpression_negative), iSeqRoot));
	}

	@Test
	private void testHTTPParameterMatching() {
		// check correct parameter and correct value
		StringMatchingExpression strMatchingExpression_positive = new StringMatchingExpression(PatternMatchingType.EQUALS, B_TX_2);
		strMatchingExpression_positive.setSearchNodeInTrace(false);
		strMatchingExpression_positive.setStringValueSource(new HttpParameterValueSource(PARAM_NAME));

		Assert.assertTrue(evaluation.evaluate(new MatchingRule(strMatchingExpression_positive), iSeqRoot));

		// check correct parameter but incorrect value
		StringMatchingExpression strMatchingExpression_negative = new StringMatchingExpression(PatternMatchingType.EQUALS, "someString");
		strMatchingExpression_negative.setSearchNodeInTrace(false);
		strMatchingExpression_negative.setStringValueSource(new HttpParameterValueSource(PARAM_NAME));

		Assert.assertFalse(evaluation.evaluate(new MatchingRule(strMatchingExpression_negative), iSeqRoot));

		// check incorrect parameter but correct value
		strMatchingExpression_negative = new StringMatchingExpression(PatternMatchingType.EQUALS, B_TX_2);
		strMatchingExpression_negative.setSearchNodeInTrace(false);
		strMatchingExpression_negative.setStringValueSource(new HttpParameterValueSource("someString"));

		Assert.assertFalse(evaluation.evaluate(new MatchingRule(strMatchingExpression_negative), iSeqRoot));
	}

	@Test
	private void testMethodSignatureMatching() {
		// check positive
		StringMatchingExpression strMatchingExpression_positive = new StringMatchingExpression(PatternMatchingType.CONTAINS, ".Class1.");
		strMatchingExpression_positive.setSearchNodeInTrace(false);
		strMatchingExpression_positive.setStringValueSource(new MethodSignatureValueSource());

		Assert.assertTrue(evaluation.evaluate(new MatchingRule(strMatchingExpression_positive), iSeqRoot));

		// check negative
		StringMatchingExpression strMatchingExpression_negative = new StringMatchingExpression(PatternMatchingType.CONTAINS, ".Class2.");
		strMatchingExpression_negative.setSearchNodeInTrace(false);
		strMatchingExpression_negative.setStringValueSource(new MethodSignatureValueSource());

		Assert.assertFalse(evaluation.evaluate(new MatchingRule(strMatchingExpression_negative), iSeqRoot));
	}

	@Test
	private void testHostMatching() {
		// check positive
		StringMatchingExpression strMatchingExpression_positive = new StringMatchingExpression(PatternMatchingType.CONTAINS, ".10.10.");
		strMatchingExpression_positive.setSearchNodeInTrace(false);
		strMatchingExpression_positive.setStringValueSource(new HostValueSource());

		Assert.assertTrue(evaluation.evaluate(new MatchingRule(strMatchingExpression_positive), iSeqRoot));

		// check negative
		StringMatchingExpression strMatchingExpression_negative = new StringMatchingExpression(PatternMatchingType.CONTAINS, "11");
		strMatchingExpression_negative.setSearchNodeInTrace(false);
		strMatchingExpression_negative.setStringValueSource(new HostValueSource());

		Assert.assertFalse(evaluation.evaluate(new MatchingRule(strMatchingExpression_negative), iSeqRoot));
	}

	@Test
	private void testNestedMethodSignatureMatching() {
		// check on seconds level without search depth limitations
		StringMatchingExpression strMatchingExpression_positive = new StringMatchingExpression(PatternMatchingType.CONTAINS, ".Class2.");
		strMatchingExpression_positive.setSearchNodeInTrace(true);
		strMatchingExpression_positive.setStringValueSource(new MethodSignatureValueSource());

		Assert.assertTrue(evaluation.evaluate(new MatchingRule(strMatchingExpression_positive), iSeqRoot));

		// check non existent value without search depth limitations
		StringMatchingExpression strMatchingExpression_negative = new StringMatchingExpression(PatternMatchingType.CONTAINS, ".Class5.");
		strMatchingExpression_positive.setSearchNodeInTrace(true);
		strMatchingExpression_negative.setStringValueSource(new MethodSignatureValueSource());

		Assert.assertFalse(evaluation.evaluate(new MatchingRule(strMatchingExpression_negative), iSeqRoot));

		// check on third level without search depth limitations
		strMatchingExpression_positive = new StringMatchingExpression(PatternMatchingType.CONTAINS, ".Class4.");
		strMatchingExpression_positive.setSearchNodeInTrace(true);
		strMatchingExpression_positive.setStringValueSource(new MethodSignatureValueSource());

		Assert.assertTrue(evaluation.evaluate(new MatchingRule(strMatchingExpression_positive), iSeqRoot));

		// check on second level with search depth limitations to up to level 2
		strMatchingExpression_positive = new StringMatchingExpression(PatternMatchingType.CONTAINS, ".Class2.");
		strMatchingExpression_positive.setSearchNodeInTrace(true);
		strMatchingExpression_positive.setMaxSearchDepth(2);
		strMatchingExpression_positive.setStringValueSource(new MethodSignatureValueSource());

		Assert.assertTrue(evaluation.evaluate(new MatchingRule(strMatchingExpression_positive), iSeqRoot));

		// check on third level with search depth limitations to up to level 2
		strMatchingExpression_negative = new StringMatchingExpression(PatternMatchingType.CONTAINS, ".Class4.");
		strMatchingExpression_positive.setSearchNodeInTrace(true);
		strMatchingExpression_positive.setMaxSearchDepth(2);
		strMatchingExpression_negative.setStringValueSource(new MethodSignatureValueSource());

		Assert.assertFalse(evaluation.evaluate(new MatchingRule(strMatchingExpression_negative), iSeqRoot));

	}

}
