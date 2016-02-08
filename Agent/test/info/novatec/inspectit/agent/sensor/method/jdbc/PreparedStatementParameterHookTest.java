package info.novatec.inspectit.agent.sensor.method.jdbc;

import info.novatec.inspectit.agent.AbstractLogSupport;
import info.novatec.inspectit.agent.config.impl.RegisteredSensorConfig;
import info.novatec.inspectit.agent.core.ICoreService;

import java.util.List;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Tests the {@link PreparedStatementParameterHook}.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("PMD")
public class PreparedStatementParameterHookTest extends AbstractLogSupport {

	/**
	 * Testing class.
	 */
	private PreparedStatementParameterHook preparedStatementParameterHook;

	@Mock
	private StatementStorage statementStorage;

	@Mock
	private RegisteredSensorConfig rsc;

	@Mock
	private ICoreService coreService;

	/**
	 * Initializes the test class.
	 */
	@BeforeMethod
	public void initTestClass() {
		preparedStatementParameterHook = new PreparedStatementParameterHook(statementStorage);
	}

	/**
	 * Test setting of the correct index and parameter value.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void setCorrectParameterAndIndex() {
		List<String> parameterTypes = Mockito.mock(List.class);
		Mockito.when(parameterTypes.size()).thenReturn(2);
		Mockito.when(parameterTypes.get(0)).thenReturn("int");

		Mockito.when(rsc.getParameterTypes()).thenReturn(parameterTypes);

		Object[] parameters = new Object[] { 1, "Value" };
		Object object = new Object();

		preparedStatementParameterHook.beforeBody(0, 0, object, parameters, rsc);
		preparedStatementParameterHook.firstAfterBody(0, 0, object, parameters, null, rsc);
		preparedStatementParameterHook.secondAfterBody(coreService, 0, 0, object, parameters, null, rsc);

		Mockito.verify(statementStorage, Mockito.times(1)).addParameter(object, 0, "Value");
		Mockito.verifyNoMoreInteractions(statementStorage);
		Mockito.verifyZeroInteractions(coreService);
	}

	/**
	 * Tests that no interaction is happening when first parameter is not 'int'.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void wrongFirstParameter() {
		List<String> parameterTypes = Mockito.mock(List.class);
		Mockito.when(parameterTypes.size()).thenReturn(2);
		Mockito.when(parameterTypes.get(0)).thenReturn("long");

		Mockito.when(rsc.getParameterTypes()).thenReturn(parameterTypes);

		Object[] parameters = new Object[] { 1L, "Value" };
		Object object = new Object();

		preparedStatementParameterHook.beforeBody(0, 0, object, parameters, rsc);
		preparedStatementParameterHook.firstAfterBody(0, 0, object, parameters, null, rsc);
		preparedStatementParameterHook.secondAfterBody(coreService, 0, 0, object, parameters, null, rsc);

		Mockito.verifyZeroInteractions(statementStorage);
		Mockito.verifyZeroInteractions(coreService);
	}

	/**
	 * Tests arbitrary method with wrong parameter count.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void wrongMethodInstrumentation() {
		List<String> parameterTypes = Mockito.mock(List.class);
		Mockito.when(parameterTypes.size()).thenReturn(1);
		Mockito.when(parameterTypes.get(0)).thenReturn("int");

		Mockito.when(rsc.getParameterTypes()).thenReturn(parameterTypes);
		Mockito.when(rsc.getTargetMethodName()).thenReturn("myMethod");

		Object[] parameters = new Object[] { 1 };
		Object object = new Object();

		preparedStatementParameterHook.beforeBody(0, 0, object, parameters, rsc);
		preparedStatementParameterHook.firstAfterBody(0, 0, object, parameters, null, rsc);
		preparedStatementParameterHook.secondAfterBody(coreService, 0, 0, object, parameters, null, rsc);

		Mockito.verifyZeroInteractions(statementStorage);
		Mockito.verifyZeroInteractions(coreService);
	}

	/**
	 * Test that setNull() methods of the prepare statement will set the <code>null</code> as the
	 * parameter value.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void setNull() {
		List<String> parameterTypes = Mockito.mock(List.class);
		Mockito.when(parameterTypes.size()).thenReturn(2);
		Mockito.when(parameterTypes.get(0)).thenReturn("int");

		Mockito.when(rsc.getParameterTypes()).thenReturn(parameterTypes);
		Mockito.when(rsc.getTargetMethodName()).thenReturn("setNull");

		Object[] parameters = new Object[] { 1, 2 };
		Object object = new Object();

		preparedStatementParameterHook.beforeBody(0, 0, object, parameters, rsc);
		preparedStatementParameterHook.firstAfterBody(0, 0, object, parameters, null, rsc);
		preparedStatementParameterHook.secondAfterBody(coreService, 0, 0, object, parameters, null, rsc);

		Mockito.verify(statementStorage, Mockito.times(1)).addParameter(object, 0, null);
		Mockito.verifyNoMoreInteractions(statementStorage);
		Mockito.verifyZeroInteractions(coreService);
	}

	/**
	 * Tests clear parameters method being invoked.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void clearParameters() {
		List<String> parameterTypes = Mockito.mock(List.class);
		Mockito.when(parameterTypes.size()).thenReturn(0);

		Mockito.when(rsc.getParameterTypes()).thenReturn(parameterTypes);
		Mockito.when(rsc.getTargetMethodName()).thenReturn("clearParameters");

		Object[] parameters = new Object[0];
		Object object = new Object();

		preparedStatementParameterHook.beforeBody(0, 0, object, parameters, rsc);
		preparedStatementParameterHook.firstAfterBody(0, 0, object, parameters, null, rsc);
		preparedStatementParameterHook.secondAfterBody(coreService, 0, 0, object, parameters, null, rsc);

		Mockito.verify(statementStorage, Mockito.times(1)).clearParameters(object);
		Mockito.verifyNoMoreInteractions(statementStorage);
		Mockito.verifyZeroInteractions(coreService);
	}

	/**
	 * Tests that the methods with big data structures will not set the complete structure, but a
	 * simple marker instead.
	 * 
	 * @param methodName
	 *            Method name that holds big structure.
	 */
	@SuppressWarnings("unchecked")
	@Test(dataProvider = "methodsWithBigDataStructures")
	public void methodWithBigData(String methodName) {
		List<String> parameterTypes = Mockito.mock(List.class);
		Mockito.when(parameterTypes.size()).thenReturn(2);
		Mockito.when(parameterTypes.get(0)).thenReturn("int");

		Mockito.when(rsc.getParameterTypes()).thenReturn(parameterTypes);
		Mockito.when(rsc.getTargetMethodName()).thenReturn(methodName);

		Object[] parameters = new Object[] { 1, "Value" };
		Object object = new Object();

		preparedStatementParameterHook.beforeBody(0, 0, object, parameters, rsc);
		preparedStatementParameterHook.firstAfterBody(0, 0, object, parameters, null, rsc);
		preparedStatementParameterHook.secondAfterBody(coreService, 0, 0, object, parameters, null, rsc);

		String expected = "[" + methodName.substring("set".length()) + "]";
		Mockito.verify(statementStorage, Mockito.times(1)).addParameter(object, 0, expected);
		Mockito.verifyNoMoreInteractions(statementStorage);
		Mockito.verifyZeroInteractions(coreService);
	}

	/**
	 * @return Method names.
	 */
	@DataProvider(name = "methodsWithBigDataStructures")
	public Object[][] methodsWithBigDataStructures() {
		return new Object[][] { { "setAsciiStream" }, { "setBinaryStream" }, { "setBlob" }, { "setCharacterStream" }, { "setClob" }, { "setNCharacterStream" }, { "setNClob" },
				{ "setUnicodeStream" } };
	}

}
