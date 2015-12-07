/**
 *
 */
package info.novatec.inspectit.ci.business.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.mockito.Matchers.anyInt;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.cmr.service.cache.CachedDataService;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.testbase.TestBase;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
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

		@BeforeMethod
		public void init() {
			MockitoAnnotations.initMocks(this);
			methodIdent = new MethodIdent();
			methodIdent.setPackageName(PACKAGE);
			methodIdent.setClassName(CLASS);
			methodIdent.setMethodName(METHOD);
			Mockito.doReturn(1L).when(invocationSeuence).getMethodIdent();
			Mockito.doReturn(methodIdent).when(cachedDataService).getMethodIdentForId(anyInt());
		}

		@Test
		public void retrieveURI() {
			String[] values = valueSource.getStringValues(invocationSeuence, cachedDataService);
			assertThat(values, hasItemInArray(PACKAGE + "." + CLASS + "." + METHOD + "()"));
		}
	}
}
