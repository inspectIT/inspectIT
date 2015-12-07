package info.novatec.inspectit.ci.business.valuesource.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.mockito.Matchers.anyInt;

import info.novatec.inspectit.ci.business.valuesource.impl.MethodParameterValueSource;
import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.cmr.service.cache.CachedDataService;
import info.novatec.inspectit.communication.data.InvocationSequenceData;
import info.novatec.inspectit.communication.data.ParameterContentData;
import info.novatec.inspectit.testbase.TestBase;

import java.util.HashSet;
import java.util.Set;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.Test;

/**
 * @author Alexander Wert
 *
 */
public class MethodParameterValueSourceTest extends TestBase {
	@InjectMocks
	MethodParameterValueSource valueSource;

	@Mock
	CachedDataService cachedDataService;

	@Mock
	InvocationSequenceData invocationSeuence;

	MethodIdent methodIdent;

	@Mock
	ParameterContentData parameterContentData;

	/**
	 * Test
	 * {@link MethodParameterValueSource#getStringValues(InvocationSequenceData, info.novatec.inspectit.cmr.service.ICachedDataService)}
	 * .
	 */
	public static class GetStringVlaues extends MethodParameterValueSourceTest {
		private static final String PACKAGE = "package";
		private static final String CLASS = "class";
		private static final String METHOD = "method";
		private static final String PARAMETER = "parameter";
		private static final int INDEX = 2;

		@Test
		public void retrieveParameter() {
			methodIdent = new MethodIdent();
			methodIdent.setPackageName(PACKAGE);
			methodIdent.setClassName(CLASS);
			methodIdent.setMethodName(METHOD);

			Set<ParameterContentData> pContentDataSet = new HashSet<>();
			pContentDataSet.add(parameterContentData);

			Mockito.doReturn(PARAMETER).when(parameterContentData).getContent();
			Mockito.doReturn(INDEX).when(parameterContentData).getSignaturePosition();

			Mockito.doReturn(1L).when(invocationSeuence).getMethodIdent();
			Mockito.doReturn(pContentDataSet).when(invocationSeuence).getParameterContentData();
			Mockito.doReturn(methodIdent).when(cachedDataService).getMethodIdentForId(anyInt());

			String methodSignature = PACKAGE + "." + CLASS + "." + METHOD + "()";
			valueSource.setMethodSignature(methodSignature);
			valueSource.setParameterIndex(INDEX);

			String[] values = valueSource.getStringValues(invocationSeuence, cachedDataService);
			assertThat(values, hasItemInArray(PARAMETER));
		}

	}
}
