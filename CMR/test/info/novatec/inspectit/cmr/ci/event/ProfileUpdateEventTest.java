/**
 *
 */
package info.novatec.inspectit.cmr.ci.event;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import info.novatec.inspectit.ci.Profile;
import info.novatec.inspectit.ci.assignment.AbstractClassSensorAssignment;
import info.novatec.inspectit.ci.assignment.impl.ExceptionSensorAssignment;
import info.novatec.inspectit.ci.assignment.impl.MethodSensorAssignment;
import info.novatec.inspectit.testbase.TestBase;

import java.util.Collection;
import java.util.Collections;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Ivan Senic
 *
 */
public class ProfileUpdateEventTest extends TestBase {

	protected static final String ID = "";

	protected ProfileUpdateEvent event;

	@Mock
	protected Profile old;

	@Mock
	protected Profile updated;

	@BeforeMethod
	public void setup() {
		when(old.getId()).thenReturn(ID);
		when(updated.getId()).thenReturn(ID);

		event = new ProfileUpdateEvent(this, old, updated);
	}

	public static class Constructor extends ProfileUpdateEventTest {

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void idsNotMatching() {
			when(old.getId()).thenReturn("smth");

			event = new ProfileUpdateEvent(this, old, updated);
		}
	}

	public static class ProfileDeactivated extends ProfileUpdateEventTest {

		@Test
		public void deactivated() {
			when(old.isActive()).thenReturn(true);
			when(updated.isActive()).thenReturn(false);

			boolean deactivated = event.isProfileDeactivated();

			assertThat(deactivated, is(true));
		}

		@Test
		public void wasNotActive() {
			when(old.isActive()).thenReturn(false);
			when(updated.isActive()).thenReturn(false);

			boolean deactivated = event.isProfileDeactivated();

			assertThat(deactivated, is(false));
		}

		@Test
		public void stillActive() {
			when(old.isActive()).thenReturn(true);
			when(updated.isActive()).thenReturn(true);

			boolean deactivated = event.isProfileDeactivated();

			assertThat(deactivated, is(false));
		}
	}

	public static class ProfileActivated extends ProfileUpdateEventTest {

		@Test
		public void activated() {
			when(old.isActive()).thenReturn(false);
			when(updated.isActive()).thenReturn(true);

			boolean activated = event.isProfileActivated();

			assertThat(activated, is(true));
		}

		@Test
		public void wasActive() {
			when(old.isActive()).thenReturn(true);
			when(updated.isActive()).thenReturn(true);

			boolean activated = event.isProfileActivated();

			assertThat(activated, is(false));
		}

		@Test
		public void stillNotActive() {
			when(old.isActive()).thenReturn(false);
			when(updated.isActive()).thenReturn(false);

			boolean activated = event.isProfileActivated();

			assertThat(activated, is(false));
		}
	}

	public static class RemovedAssignments extends ProfileUpdateEventTest {

		@Test
		public void removedAssignments() {
			MethodSensorAssignment methodAssignment = mock(MethodSensorAssignment.class);
			ExceptionSensorAssignment exceptionAssignment = mock(ExceptionSensorAssignment.class);

			when(old.getMethodSensorAssignments()).thenReturn(Collections.singletonList(methodAssignment));
			when(old.getExceptionSensorAssignments()).thenReturn(Collections.singletonList(exceptionAssignment));
			when(updated.getMethodSensorAssignments()).thenReturn(Collections.<MethodSensorAssignment> emptyList());
			when(updated.getExceptionSensorAssignments()).thenReturn(Collections.<ExceptionSensorAssignment> emptyList());
			when(old.isActive()).thenReturn(true);
			when(updated.isActive()).thenReturn(true);

			Collection<AbstractClassSensorAssignment<?>> removed = event.getRemovedSensorAssignments();

			assertThat(removed, hasSize(2));
			assertThat(removed, hasItem(methodAssignment));
			assertThat(removed, hasItem(exceptionAssignment));
		}

		@Test
		public void noChange() {
			MethodSensorAssignment methodAssignment = mock(MethodSensorAssignment.class);
			ExceptionSensorAssignment exceptionAssignment = mock(ExceptionSensorAssignment.class);

			when(old.getMethodSensorAssignments()).thenReturn(Collections.singletonList(methodAssignment));
			when(old.getExceptionSensorAssignments()).thenReturn(Collections.singletonList(exceptionAssignment));
			when(updated.getMethodSensorAssignments()).thenReturn(Collections.singletonList(methodAssignment));
			when(updated.getExceptionSensorAssignments()).thenReturn(Collections.singletonList(exceptionAssignment));
			when(old.isActive()).thenReturn(true);
			when(updated.isActive()).thenReturn(true);

			Collection<AbstractClassSensorAssignment<?>> removed = event.getRemovedSensorAssignments();

			assertThat(removed, is(empty()));
		}

		@Test
		public void deactivated() {
			MethodSensorAssignment methodAssignment = mock(MethodSensorAssignment.class);
			ExceptionSensorAssignment exceptionAssignment = mock(ExceptionSensorAssignment.class);

			when(old.getMethodSensorAssignments()).thenReturn(Collections.singletonList(methodAssignment));
			when(old.getExceptionSensorAssignments()).thenReturn(Collections.singletonList(exceptionAssignment));
			when(updated.getMethodSensorAssignments()).thenReturn(Collections.singletonList(methodAssignment));
			when(updated.getExceptionSensorAssignments()).thenReturn(Collections.singletonList(exceptionAssignment));
			when(old.isActive()).thenReturn(true);
			when(updated.isActive()).thenReturn(false);

			Collection<AbstractClassSensorAssignment<?>> removed = event.getRemovedSensorAssignments();

			assertThat(removed, hasSize(2));
			assertThat(removed, hasItem(methodAssignment));
			assertThat(removed, hasItem(exceptionAssignment));
		}

		@Test
		public void activated() {
			MethodSensorAssignment methodAssignment = mock(MethodSensorAssignment.class);
			ExceptionSensorAssignment exceptionAssignment = mock(ExceptionSensorAssignment.class);

			when(old.getMethodSensorAssignments()).thenReturn(Collections.singletonList(methodAssignment));
			when(old.getExceptionSensorAssignments()).thenReturn(Collections.singletonList(exceptionAssignment));
			when(updated.getMethodSensorAssignments()).thenReturn(Collections.singletonList(methodAssignment));
			when(updated.getExceptionSensorAssignments()).thenReturn(Collections.singletonList(exceptionAssignment));
			when(old.isActive()).thenReturn(false);
			when(updated.isActive()).thenReturn(true);

			Collection<AbstractClassSensorAssignment<?>> removed = event.getRemovedSensorAssignments();

			assertThat(removed, is(empty()));
		}
	}

	public static class AddedAssignments extends ProfileUpdateEventTest {

		@Test
		public void addedAssignments() {
			MethodSensorAssignment methodAssignment = mock(MethodSensorAssignment.class);
			ExceptionSensorAssignment exceptionAssignment = mock(ExceptionSensorAssignment.class);

			when(old.getMethodSensorAssignments()).thenReturn(Collections.<MethodSensorAssignment> emptyList());
			when(old.getExceptionSensorAssignments()).thenReturn(Collections.<ExceptionSensorAssignment> emptyList());
			when(updated.getMethodSensorAssignments()).thenReturn(Collections.singletonList(methodAssignment));
			when(updated.getExceptionSensorAssignments()).thenReturn(Collections.singletonList(exceptionAssignment));
			when(old.isActive()).thenReturn(true);
			when(updated.isActive()).thenReturn(true);

			Collection<AbstractClassSensorAssignment<?>> added = event.getAddedSensorAssignments();

			assertThat(added, hasSize(2));
			assertThat(added, hasItem(methodAssignment));
			assertThat(added, hasItem(exceptionAssignment));
		}

		@Test
		public void noChange() {
			MethodSensorAssignment methodAssignment = mock(MethodSensorAssignment.class);
			ExceptionSensorAssignment exceptionAssignment = mock(ExceptionSensorAssignment.class);

			when(old.getMethodSensorAssignments()).thenReturn(Collections.singletonList(methodAssignment));
			when(old.getExceptionSensorAssignments()).thenReturn(Collections.singletonList(exceptionAssignment));
			when(updated.getMethodSensorAssignments()).thenReturn(Collections.singletonList(methodAssignment));
			when(updated.getExceptionSensorAssignments()).thenReturn(Collections.singletonList(exceptionAssignment));
			when(old.isActive()).thenReturn(true);
			when(updated.isActive()).thenReturn(true);

			Collection<AbstractClassSensorAssignment<?>> added = event.getAddedSensorAssignments();

			assertThat(added, is(empty()));
		}

		@Test
		public void deactivated() {
			MethodSensorAssignment methodAssignment = mock(MethodSensorAssignment.class);
			ExceptionSensorAssignment exceptionAssignment = mock(ExceptionSensorAssignment.class);

			when(old.getMethodSensorAssignments()).thenReturn(Collections.singletonList(methodAssignment));
			when(old.getExceptionSensorAssignments()).thenReturn(Collections.singletonList(exceptionAssignment));
			when(updated.getMethodSensorAssignments()).thenReturn(Collections.singletonList(methodAssignment));
			when(updated.getExceptionSensorAssignments()).thenReturn(Collections.singletonList(exceptionAssignment));
			when(old.isActive()).thenReturn(true);
			when(updated.isActive()).thenReturn(false);

			Collection<AbstractClassSensorAssignment<?>> added = event.getAddedSensorAssignments();

			assertThat(added, is(empty()));
		}

		@Test
		public void activated() {
			MethodSensorAssignment methodAssignment = mock(MethodSensorAssignment.class);
			ExceptionSensorAssignment exceptionAssignment = mock(ExceptionSensorAssignment.class);

			when(old.getMethodSensorAssignments()).thenReturn(Collections.singletonList(methodAssignment));
			when(old.getExceptionSensorAssignments()).thenReturn(Collections.singletonList(exceptionAssignment));
			when(updated.getMethodSensorAssignments()).thenReturn(Collections.singletonList(methodAssignment));
			when(updated.getExceptionSensorAssignments()).thenReturn(Collections.singletonList(exceptionAssignment));
			when(old.isActive()).thenReturn(false);
			when(updated.isActive()).thenReturn(true);

			Collection<AbstractClassSensorAssignment<?>> added = event.getAddedSensorAssignments();

			assertThat(added, hasSize(2));
			assertThat(added, hasItem(methodAssignment));
			assertThat(added, hasItem(exceptionAssignment));
		}

	}

}
