package info.novatec.inspectit.ci.business.valuesource.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.mockito.Matchers.anyInt;

import info.novatec.inspectit.ci.business.valuesource.impl.MethodSignatureValueSource;
import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.cmr.service.cache.CachedDataService;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.testbase.TestBase;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.Test;

/**
 * @author Alexander Wert
 *
 */
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
	 * {@link MethodSignatureValueSource#getStringValues(InvocationSequenceData, info.novatec.inspectit.cmr.service.ICachedDataService)}
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
	}
}
