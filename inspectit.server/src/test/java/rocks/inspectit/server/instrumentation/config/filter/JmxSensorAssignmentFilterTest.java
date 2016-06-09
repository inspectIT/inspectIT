package rocks.inspectit.server.instrumentation.config.filter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import java.util.Collections;

import javax.management.ObjectName;

import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.instrumentation.config.impl.JmxAttributeDescriptor;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.assignment.impl.JmxBeanSensorAssignment;

/**
 * @author Ivan Senic
 */
public class JmxSensorAssignmentFilterTest extends TestBase {

	@InjectMocks
	JmxSensorAssignmentFilter filter;

	@Mock
	JmxBeanSensorAssignment assignment;

	@Mock
	JmxAttributeDescriptor descriptor;

	@Mock
	ObjectName objectName;

	public class Matches extends JmxSensorAssignmentFilterTest {

		@Test
		public void matchingAllAttributes() {
			when(descriptor.ismBeanAttributeIsReadable()).thenReturn(true);
			when(descriptor.getmBeanObjectName()).thenReturn("java.lang:type=Memory");
			when(descriptor.getAttributeName()).thenReturn("attribute");
			when(assignment.getObjectName()).thenReturn(objectName);
			when(assignment.getAttributes()).thenReturn(Collections.<String> emptySet());
			when(objectName.apply(Matchers.<ObjectName> any())).thenReturn(true);

			boolean matches = filter.matches(assignment, descriptor);

			assertThat(matches, is(true));
		}

		@Test
		public void matchingSpecificAttribute() {
			String attributeName = "attribute";
			when(descriptor.ismBeanAttributeIsReadable()).thenReturn(true);
			when(descriptor.getmBeanObjectName()).thenReturn("java.lang:type=Memory");
			when(descriptor.getAttributeName()).thenReturn(attributeName);
			when(assignment.getObjectName()).thenReturn(objectName);
			when(assignment.getAttributes()).thenReturn(Collections.singleton(attributeName));
			when(objectName.apply(Matchers.<ObjectName> any())).thenReturn(true);

			boolean matches = filter.matches(assignment, descriptor);

			assertThat(matches, is(true));
		}

		@Test
		public void notMatchingNotReadable() {
			when(descriptor.ismBeanAttributeIsReadable()).thenReturn(false);

			boolean matches = filter.matches(assignment, descriptor);

			assertThat(matches, is(false));
		}

		@Test
		public void notMatchingObjectName() {
			when(descriptor.ismBeanAttributeIsReadable()).thenReturn(true);
			when(descriptor.getmBeanObjectName()).thenReturn("java.lang:type=Memory");
			when(assignment.getObjectName()).thenReturn(objectName);
			when(objectName.apply(Matchers.<ObjectName> any())).thenReturn(false);

			boolean matches = filter.matches(assignment, descriptor);

			assertThat(matches, is(false));
		}

		@Test
		public void notMatchingAssignmentObjectNameNull() {
			when(descriptor.ismBeanAttributeIsReadable()).thenReturn(true);
			when(descriptor.getmBeanObjectName()).thenReturn("java.lang:type=Memory");
			when(assignment.getObjectName()).thenReturn(null);

			boolean matches = filter.matches(assignment, descriptor);

			assertThat(matches, is(false));
		}


		@Test
		public void notMatchingWrongObjectName() {
			when(descriptor.ismBeanAttributeIsReadable()).thenReturn(true);
			when(descriptor.getmBeanObjectName()).thenReturn("something not valid");

			boolean matches = filter.matches(assignment, descriptor);

			assertThat(matches, is(false));
		}

		@Test
		public void notMatchingAttribute() {
			when(descriptor.ismBeanAttributeIsReadable()).thenReturn(true);
			when(descriptor.getmBeanObjectName()).thenReturn("java.lang:type=Memory");
			when(descriptor.getAttributeName()).thenReturn("attribute");
			when(assignment.getAttributes()).thenReturn(Collections.singleton("not matching"));
			when(assignment.getObjectName()).thenReturn(objectName);
			when(objectName.apply(Matchers.<ObjectName> any())).thenReturn(true);

			boolean matches = filter.matches(assignment, descriptor);

			assertThat(matches, is(false));
		}
	}
}
