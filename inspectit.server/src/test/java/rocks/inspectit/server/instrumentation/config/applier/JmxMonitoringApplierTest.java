package rocks.inspectit.server.instrumentation.config.applier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.instrumentation.config.filter.AssignmentFilterProvider;
import rocks.inspectit.server.instrumentation.config.filter.JmxSensorAssignmentFilter;
import rocks.inspectit.shared.all.instrumentation.config.impl.AgentConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.JmxAttributeDescriptor;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.assignment.impl.JmxBeanSensorAssignment;
import rocks.inspectit.shared.cs.cmr.service.IRegistrationService;

/**
 * @author Ivan Senic
 *
 */
public class JmxMonitoringApplierTest extends TestBase {

	@InjectMocks
	JmxMonitoringApplier applier;

	@Mock
	JmxAttributeDescriptor descriptor;

	@Mock
	JmxBeanSensorAssignment assignment;

	@Mock
	Environment environment;

	@Mock
	IRegistrationService registrationService;

	@Mock
	AgentConfig agentConfig;

	@Mock
	AssignmentFilterProvider filterProvider;

	@Mock
	JmxSensorAssignmentFilter filter;

	public class AddMonitoringPoint extends JmxMonitoringApplierTest {

		@BeforeMethod
		public void setup() {
			when(filterProvider.getJmxSensorAssignmentFilter()).thenReturn(filter);
		}

		@Test
		public void add() {
			long platformId = 7L;
			String attributeName = "attributeName";
			String objectName = "objectname";
			long attributeId = 13L;
			String attributeType = "type";
			String attributeDecs = "desc";
			boolean isIs = false;
			boolean readable = true;
			boolean writable = false;
			when(filter.matches(assignment, descriptor)).thenReturn(true);
			when(agentConfig.getPlatformId()).thenReturn(platformId);
			when(descriptor.getAttributeName()).thenReturn(attributeName);
			when(descriptor.getmBeanAttributeDescription()).thenReturn(attributeDecs);
			when(descriptor.getmBeanObjectName()).thenReturn(objectName);
			when(descriptor.getmBeanAttributeId()).thenReturn(attributeId);
			when(descriptor.getmBeanAttributeType()).thenReturn(attributeType);
			when(descriptor.ismBeanAttributeIsIs()).thenReturn(isIs);
			when(descriptor.ismBeanAttributeIsReadable()).thenReturn(readable);
			when(descriptor.ismBeanAttributeIsWritable()).thenReturn(writable);

			boolean added = applier.addMonitoringPoint(agentConfig, descriptor);

			assertThat(added, is(true));
			verify(registrationService).registerJmxSensorDefinitionDataIdent(platformId, objectName, attributeName, attributeDecs, attributeType, isIs, readable, writable);
		}

		@Test
		public void filterDoesNotMatch() {
			when(filter.matches(assignment, descriptor)).thenReturn(false);

			boolean added = applier.addMonitoringPoint(agentConfig, descriptor);

			assertThat(added, is(false));
			verifyZeroInteractions(registrationService);
		}
	}

}
