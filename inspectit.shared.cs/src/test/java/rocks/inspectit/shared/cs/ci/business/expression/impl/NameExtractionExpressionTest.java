package rocks.inspectit.shared.cs.ci.business.expression.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.cmr.service.ICachedDataService;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.business.valuesource.StringValueSource;

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
	 * {@link NameExtractionExpression#extractName(InvocationSequenceData, ICachedDataService)}
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

			String name = expression.extractName(null, null);

			assertThat(name, equalTo("Name:sample"));
		}

		@Test
		public void matchSecondString() {
			expression.setRegularExpression("/my/([^/]*)/string.*");
			expression.setTargetNamePattern("Name:(1)");

			String name = expression.extractName(null, null);

			assertThat(name, equalTo("Name:second"));
		}

		@Test
		public void matchMultipleGroups() {
			expression.setRegularExpression("/my/([^/]*)/string/for/(.*)");
			expression.setTargetNamePattern("Name:(1)-(2)");

			String name = expression.extractName(null, null);

			assertThat(name, equalTo("Name:second-test"));
		}

		@Test
		public void noMatch() {
			expression.setRegularExpression("/my/([^/]*)/xy/for/(.*)");
			expression.setTargetNamePattern("Name:(1)-(2)");

			String name = expression.extractName(null, null);

			assertThat(name, is(nullValue()));
		}

		@Test
		public void invalidPattern() {
			expression.setRegularExpression("(my");
			expression.setTargetNamePattern("Name:(1)-(2)");

			String name = expression.extractName(null, null);

			assertThat(name, is(nullValue()));
			verifyNoMoreInteractions(stringValueSource);
		}
	}
}
