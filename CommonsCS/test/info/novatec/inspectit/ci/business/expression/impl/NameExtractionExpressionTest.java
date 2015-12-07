package info.novatec.inspectit.ci.business.expression.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import info.novatec.inspectit.ci.business.valuesource.StringValueSource;
import info.novatec.inspectit.cmr.service.ICachedDataService;
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
public class NameExtractionExpressionTest extends TestBase {
	@InjectMocks
	NameExtractionExpression expression;

	@Mock
	StringValueSource stringValueSource;

	/**
	 * Tests
	 * {@link NameExtractionExpression#extractName(info.novatec.inspectit.communication.data.InvocationSequenceData, info.novatec.inspectit.cmr.service.ICachedDataService)}
	 * method.
	 *
	 *
	 */
	public static class ExtractName extends NameExtractionExpressionTest {
		@BeforeMethod
		public void initialize() {
			when(stringValueSource.getStringValues(any(InvocationSequenceData.class), any(ICachedDataService.class))).thenReturn(new String[] { "/a/sample/string", "/my/second/string/for/test" });
		}

		@Test
		public void matchFirstString() {
			expression.setRegularExpression("/a/([^/]*)/string");
			expression.setTargetNamePattern("Name:(1)");

			assertThat(expression.extractName(null, null), equalTo("Name:sample"));
		}

		@Test
		public void matchSecondString() {
			expression.setRegularExpression("/my/([^/]*)/string.*");
			expression.setTargetNamePattern("Name:(1)");

			assertThat(expression.extractName(null, null), equalTo("Name:second"));
		}

		@Test
		public void matchMultipleGroups() {
			expression.setRegularExpression("/my/([^/]*)/string/for/(.*)");
			expression.setTargetNamePattern("Name:(1)-(2)");

			assertThat(expression.extractName(null, null), equalTo("Name:second-test"));
		}

		@Test
		public void noMatch() {
			expression.setRegularExpression("/my/([^/]*)/xy/for/(.*)");
			expression.setTargetNamePattern("Name:(1)-(2)");

			assertThat(expression.extractName(null, null), is(nullValue()));
		}
	}
}
