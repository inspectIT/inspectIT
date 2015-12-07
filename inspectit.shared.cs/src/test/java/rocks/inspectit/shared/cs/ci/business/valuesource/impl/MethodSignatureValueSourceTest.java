package rocks.inspectit.shared.cs.ci.business.valuesource.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.anyInt;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.cmr.model.MethodIdent;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.cmr.service.cache.CachedDataService;

/**
 * @author Alexander Wert
 *
 */
@SuppressWarnings("PMD")
public class MethodSignatureValueSourceTest extends TestBase {
	@InjectMocks
	MethodSignatureValueSource valueSource;

	@Mock
	CachedDataService cachedDataService;

	@Mock
	InvocationSequenceData invocationSeuence;

	MethodIdent methodIdent;

	/**
	 * Test
	 * {@link MethodSignatureValueSource#getStringValues(InvocationSequenceData, rocks.inspectit.shared.all.cmr.service.ICachedDataService)}
	 * .
	 */
	public static class GetStringVlaues extends MethodSignatureValueSourceTest {
		private static final String PACKAGE = "package";
		private static final String CLASS = "class";
		private static final String METHOD = "method";

		@Test
		public void retrieveURI() {
			methodIdent = new MethodIdent();
			methodIdent.setPackageName(PACKAGE);
			methodIdent.setClassName(CLASS);
			methodIdent.setMethodName(METHOD);
			Mockito.doReturn(1L).when(invocationSeuence).getMethodIdent();
			Mockito.doReturn(methodIdent).when(cachedDataService).getMethodIdentForId(anyInt());

			String[] values = valueSource.getStringValues(invocationSeuence, cachedDataService);
			assertThat(values, hasItemInArray(PACKAGE + "." + CLASS + "." + METHOD + "()"));
		}

		@Test
		public void methodIdentNull() {
			Mockito.doReturn(1L).when(invocationSeuence).getMethodIdent();
			Mockito.doReturn(null).when(cachedDataService).getMethodIdentForId(anyInt());

			String[] values = valueSource.getStringValues(invocationSeuence, cachedDataService);
			assertThat(values, is(notNullValue()));
			assertThat(values.length, is(equalTo(0)));
		}
	}
}
