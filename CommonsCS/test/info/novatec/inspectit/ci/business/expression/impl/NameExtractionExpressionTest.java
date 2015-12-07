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

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Alexander Wert
 *
 */
public class NameExtractionExpressionTest extends TestBase {
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
			NameExtractionExpression expression = new NameExtractionExpression();
			expression.setRegularExpression("/a/([^/]*)/string");
			expression.setTargetNamePattern("Name:(1)");

			String name = expression.extractName(null, null);

			assertThat(name, equalTo("Name:sample"));
		}

		@Test
		public void matchSecondString() {
			NameExtractionExpression expression = new NameExtractionExpression();
			expression.setRegularExpression("/my/([^/]*)/string.*");
			expression.setTargetNamePattern("Name:(1)");

			String name = expression.extractName(null, null);

			assertThat(name, equalTo("Name:second"));
		}

		@Test
		public void matchMultipleGroups() {
			NameExtractionExpression expression = new NameExtractionExpression();
			expression.setRegularExpression("/my/([^/]*)/string/for/(.*)");
			expression.setTargetNamePattern("Name:(1)-(2)");

			String name = expression.extractName(null, null);

			assertThat(name, equalTo("Name:second-test"));
		}

		@Test
		public void noMatch() {
			NameExtractionExpression expression = new NameExtractionExpression();
			expression.setRegularExpression("/my/([^/]*)/xy/for/(.*)");
			expression.setTargetNamePattern("Name:(1)-(2)");

			String name = expression.extractName(null, null);

			assertThat(name, is(nullValue()));
		}
	}
}
