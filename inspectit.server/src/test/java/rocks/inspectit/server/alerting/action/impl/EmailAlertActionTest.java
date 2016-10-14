package rocks.inspectit.server.alerting.action.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.alerting.Alert;
import rocks.inspectit.server.alerting.state.AlertingState;
import rocks.inspectit.server.mail.IEMailSender;
import rocks.inspectit.server.template.TemplateManager;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.AlertingDefinition;
import rocks.inspectit.shared.cs.ci.AlertingDefinition.ThresholdType;

/**
 *
 * Test the {@link EmailAlertActionTest}.
 *
 * @author Marius Oehler
 *
 */
public class EmailAlertActionTest extends TestBase {

	@InjectMocks
	EmailAlertAction emailAction;

	@Mock
	Logger log;

	@Mock
	IEMailSender emailSender;

	@Mock
	TemplateManager templateManager;

	AlertingState alertingState;

	@BeforeMethod
	public void init() throws BusinessException {
		AlertingDefinition alertingDefinition = new AlertingDefinition();
		alertingDefinition.setName("myAlert");
		alertingDefinition.setField("field");
		alertingDefinition.setMeasurement("measurement");
		alertingDefinition.setThreshold(1D);
		alertingDefinition.setThresholdType(ThresholdType.LOWER_THRESHOLD);
		alertingDefinition.setTimerange(1);
		alertingDefinition.addNotificationEmailAddress("test@example.com");
		alertingDefinition.putTag("tagKey", "tagVal");
		alertingDefinition.setThreshold(10);
		alertingDefinition.setThresholdType(ThresholdType.UPPER_THRESHOLD);
		alertingDefinition.setTimerange(0);

		Alert alert = new Alert(alertingDefinition, 0);

		alertingState = new AlertingState(alertingDefinition);
		alertingState.setAlert(alert);
	}

	/**
	 * Test the
	 * {@link EmailAlertAction#onStarting(rocks.inspectit.server.alerting.state.AlertingState, double)}
	 * method.
	 */
	public static class OnStart extends EmailAlertActionTest {

		@Test
		@SuppressWarnings("unchecked")
		public void testOnStart() {
			ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

			emailAction.onStarting(alertingState, 10);

			verify(emailSender, times(1)).sendEMail(any(String.class), captor.capture(), any(String.class), any(List.class));

			System.out.println(captor.getValue());
		}
	}
}
