package rocks.inspectit.shared.all.communication;

import java.sql.Timestamp;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import rocks.inspectit.shared.all.cmr.model.MethodIdent;
import rocks.inspectit.shared.all.cmr.model.MethodSensorTypeIdent;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.cmr.model.PlatformSensorTypeIdent;
import rocks.inspectit.shared.all.cmr.model.SensorTypeIdent;
import rocks.inspectit.shared.all.communication.data.ClassLoadingInformationData;
import rocks.inspectit.shared.all.communication.data.CompilationInformationData;
import rocks.inspectit.shared.all.communication.data.ExceptionSensorData;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.MemoryInformationData;
import rocks.inspectit.shared.all.communication.data.RuntimeInformationData;
import rocks.inspectit.shared.all.communication.data.SqlStatementData;
import rocks.inspectit.shared.all.communication.data.SystemInformationData;
import rocks.inspectit.shared.all.communication.data.ThreadInformationData;
import rocks.inspectit.shared.all.communication.data.TimerData;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test all domain model classes to verify whether the contract for the equals and hashCode methods
 * in a class is met. The contracts are described in the Javadoc comments for the java.lang.Object
 * class.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("PMD")
public class EqualsVerifierTest {

	/**
	 * Classes to be tested.
	 */
	public static final Object[][] TESTING_CLASSES = new Object[][] { { TimerData.class }, { SqlStatementData.class }, { ExceptionSensorData.class }, { InvocationSequenceData.class },
			{ ClassLoadingInformationData.class }, { CompilationInformationData.class }, { MemoryInformationData.class }, { RuntimeInformationData.class }, { SystemInformationData.class },
			{ ThreadInformationData.class }, { HttpTimerData.class }, { MethodIdent.class }, { MethodSensorTypeIdent.class }, { PlatformIdent.class }, { PlatformSensorTypeIdent.class },
			{ SensorTypeIdent.class } };

	/**
	 * Verify equals contract test.
	 * 
	 * @param clazz
	 *            Class to test.
	 */
	@Test(dataProvider = "classProvider")
	public void equalsContract(Class<?> clazz) {
		EqualsVerifier.forClass(clazz).usingGetClass().withPrefabValues(Timestamp.class, new Timestamp(1), new Timestamp(2))
				.withPrefabValues(ExceptionSensorData.class, new ExceptionSensorData(new Timestamp(1), 1, 1, 1), new ExceptionSensorData(new Timestamp(2), 2, 2, 2))
				.withPrefabValues(InvocationSequenceData.class, new InvocationSequenceData(new Timestamp(1), 1, 1, 1), new InvocationSequenceData(new Timestamp(2), 2, 2, 2)).withRedefinedSuperclass()
				.suppress(Warning.NONFINAL_FIELDS).suppress(Warning.TRANSIENT_FIELDS).verify();
	}

	/**
	 * Provides classes to be tested.
	 * 
	 * @return Provides classes to be tested.
	 */
	@DataProvider(name = "classProvider")
	protected Object[][] classprovider() {
		return TESTING_CLASSES;
	}

}
