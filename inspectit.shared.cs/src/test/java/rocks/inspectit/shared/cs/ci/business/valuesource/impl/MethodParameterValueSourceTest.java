package rocks.inspectit.shared.cs.ci.business.valuesource.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.anyInt;

import java.util.HashSet;
import java.util.Set;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.cmr.model.MethodIdent;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.ParameterContentData;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.cmr.service.cache.CachedDataService;

/**
 * @author Alexander Wert
 *
 */
@SuppressWarnings("PMD")
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
	 * {@link MethodParameterValueSource#getStringValues(InvocationSequenceData, rocks.inspectit.shared.all.cmr.service.ICachedDataService)}
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

		@Test
		public void methodIdentNull() {
			Mockito.doReturn(PARAMETER).when(parameterContentData).getContent();
			Mockito.doReturn(INDEX).when(parameterContentData).getSignaturePosition();

			Mockito.doReturn(1L).when(invocationSeuence).getMethodIdent();
			Mockito.doReturn(null).when(cachedDataService).getMethodIdentForId(anyInt());

			String methodSignature = PACKAGE + "." + CLASS + "." + METHOD + "()";
			valueSource.setMethodSignature(methodSignature);
			valueSource.setParameterIndex(INDEX);

			String[] values = valueSource.getStringValues(invocationSeuence, cachedDataService);
			assertThat(values, is(notNullValue()));
			assertThat(values.length, is(equalTo(0)));
		}

		@Test
		public void wrongMethodIdent() {
			methodIdent = new MethodIdent();
			methodIdent.setPackageName("someWrongPackage");
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
			assertThat(values, is(notNullValue()));
			assertThat(values.length, is(equalTo(0)));
		}

		@Test
		public void noCapturedParameters() {
			methodIdent = new MethodIdent();
			methodIdent.setPackageName(PACKAGE);
			methodIdent.setClassName(CLASS);
			methodIdent.setMethodName(METHOD);

			Set<ParameterContentData> pContentDataSet = new HashSet<>();

			Mockito.doReturn(PARAMETER).when(parameterContentData).getContent();
			Mockito.doReturn(INDEX).when(parameterContentData).getSignaturePosition();

			Mockito.doReturn(1L).when(invocationSeuence).getMethodIdent();
			Mockito.doReturn(pContentDataSet).when(invocationSeuence).getParameterContentData();
			Mockito.doReturn(methodIdent).when(cachedDataService).getMethodIdentForId(anyInt());

			String methodSignature = PACKAGE + "." + CLASS + "." + METHOD + "()";
			valueSource.setMethodSignature(methodSignature);
			valueSource.setParameterIndex(INDEX);

			String[] values = valueSource.getStringValues(invocationSeuence, cachedDataService);
			assertThat(values, is(notNullValue()));
			assertThat(values.length, is(equalTo(0)));
		}

		@Test
		public void wrongCapturedParameters() {
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
			valueSource.setParameterIndex(INDEX + 1);

			String[] values = valueSource.getStringValues(invocationSeuence, cachedDataService);
			assertThat(values, is(notNullValue()));
			assertThat(values.length, is(equalTo(0)));
		}
	}
}
