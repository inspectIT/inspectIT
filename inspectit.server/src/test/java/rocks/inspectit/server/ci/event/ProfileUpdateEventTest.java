package rocks.inspectit.server.ci.event;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;

import org.mockito.Mock;
import org.testng.annotations.Test;

import rocks.inspectit.server.ci.event.ProfileUpdateEvent;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.Profile;
import rocks.inspectit.shared.cs.ci.assignment.AbstractClassSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.ExceptionSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.MethodSensorAssignment;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class ProfileUpdateEventTest extends TestBase {

	static final String ID = "";

	@Mock
	Profile old;

	@Mock
	Profile updated;

	@Mock
	MethodSensorAssignment methodAssignment;

	@Mock
	ExceptionSensorAssignment exceptionAssignment;

	public static class Constructor extends ProfileUpdateEventTest {

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void idsNotMatching() {
			when(updated.getId()).thenReturn(ID);
			when(old.getId()).thenReturn("smth");

			new ProfileUpdateEvent(this, old, updated);
		}
	}

	public static class IsProfileDeactivated extends ProfileUpdateEventTest {

		@Test
		public void deactivated() {
			when(old.getId()).thenReturn(ID);
			when(updated.getId()).thenReturn(ID);
			when(old.isActive()).thenReturn(true);
			when(updated.isActive()).thenReturn(false);
			ProfileUpdateEvent event = new ProfileUpdateEvent(this, old, updated);

			boolean deactivated = event.isProfileDeactivated();

			assertThat(deactivated, is(true));
		}

		@Test
		public void wasNotActive() {
			when(old.getId()).thenReturn(ID);
			when(updated.getId()).thenReturn(ID);
			when(old.isActive()).thenReturn(false);
			when(updated.isActive()).thenReturn(false);
			ProfileUpdateEvent event = new ProfileUpdateEvent(this, old, updated);

			boolean deactivated = event.isProfileDeactivated();

			assertThat(deactivated, is(false));
		}

		@Test
		public void stillActive() {
			when(old.getId()).thenReturn(ID);
			when(updated.getId()).thenReturn(ID);
			when(old.isActive()).thenReturn(true);
			when(updated.isActive()).thenReturn(true);
			ProfileUpdateEvent event = new ProfileUpdateEvent(this, old, updated);

			boolean deactivated = event.isProfileDeactivated();

			assertThat(deactivated, is(false));
		}
	}

	public static class IsProfileActivated extends ProfileUpdateEventTest {

		@Test
		public void activated() {
			when(old.getId()).thenReturn(ID);
			when(updated.getId()).thenReturn(ID);
			when(old.isActive()).thenReturn(false);
			when(updated.isActive()).thenReturn(true);
			ProfileUpdateEvent event = new ProfileUpdateEvent(this, old, updated);

			boolean activated = event.isProfileActivated();

			assertThat(activated, is(true));
		}

		@Test
		public void wasActive() {
			when(old.getId()).thenReturn(ID);
			when(updated.getId()).thenReturn(ID);
			when(old.isActive()).thenReturn(true);
			when(updated.isActive()).thenReturn(true);
			ProfileUpdateEvent event = new ProfileUpdateEvent(this, old, updated);

			boolean activated = event.isProfileActivated();

			assertThat(activated, is(false));
		}

		@Test
		public void stillNotActive() {
			when(old.getId()).thenReturn(ID);
			when(updated.getId()).thenReturn(ID);
			when(old.isActive()).thenReturn(false);
			when(updated.isActive()).thenReturn(false);
			ProfileUpdateEvent event = new ProfileUpdateEvent(this, old, updated);

			boolean activated = event.isProfileActivated();

			assertThat(activated, is(false));
		}
	}

	public static class GetRemovedSensorAssignments extends ProfileUpdateEventTest {

		@Test
		public void removedAssignments() {
			when(old.getId()).thenReturn(ID);
			when(updated.getId()).thenReturn(ID);
			when(old.getMethodSensorAssignments()).thenReturn(Collections.singletonList(methodAssignment));
			when(old.getExceptionSensorAssignments()).thenReturn(Collections.singletonList(exceptionAssignment));
			when(updated.getMethodSensorAssignments()).thenReturn(Collections.<MethodSensorAssignment> emptyList());
			when(updated.getExceptionSensorAssignments()).thenReturn(Collections.<ExceptionSensorAssignment> emptyList());
			when(old.isActive()).thenReturn(true);
			when(updated.isActive()).thenReturn(true);
			ProfileUpdateEvent event = new ProfileUpdateEvent(this, old, updated);

			Collection<AbstractClassSensorAssignment<?>> removed = event.getRemovedSensorAssignments();

			assertThat(removed, hasSize(2));
			assertThat(removed, hasItem(methodAssignment));
			assertThat(removed, hasItem(exceptionAssignment));
		}

		@Test
		public void noChange() {
			when(old.getId()).thenReturn(ID);
			when(updated.getId()).thenReturn(ID);
			when(old.getMethodSensorAssignments()).thenReturn(Collections.singletonList(methodAssignment));
			when(old.getExceptionSensorAssignments()).thenReturn(Collections.singletonList(exceptionAssignment));
			when(updated.getMethodSensorAssignments()).thenReturn(Collections.singletonList(methodAssignment));
			when(updated.getExceptionSensorAssignments()).thenReturn(Collections.singletonList(exceptionAssignment));
			when(old.isActive()).thenReturn(true);
			when(updated.isActive()).thenReturn(true);
			ProfileUpdateEvent event = new ProfileUpdateEvent(this, old, updated);

			Collection<AbstractClassSensorAssignment<?>> removed = event.getRemovedSensorAssignments();

			assertThat(removed, is(empty()));
		}

		@Test
		public void deactivated() {
			when(old.getId()).thenReturn(ID);
			when(updated.getId()).thenReturn(ID);
			when(old.getMethodSensorAssignments()).thenReturn(Collections.singletonList(methodAssignment));
			when(old.getExceptionSensorAssignments()).thenReturn(Collections.singletonList(exceptionAssignment));
			when(updated.getMethodSensorAssignments()).thenReturn(Collections.singletonList(methodAssignment));
			when(updated.getExceptionSensorAssignments()).thenReturn(Collections.singletonList(exceptionAssignment));
			when(old.isActive()).thenReturn(true);
			when(updated.isActive()).thenReturn(false);
			ProfileUpdateEvent event = new ProfileUpdateEvent(this, old, updated);

			Collection<AbstractClassSensorAssignment<?>> removed = event.getRemovedSensorAssignments();

			assertThat(removed, hasSize(2));
			assertThat(removed, hasItem(methodAssignment));
			assertThat(removed, hasItem(exceptionAssignment));
		}

		@Test
		public void activated() {
			when(old.getId()).thenReturn(ID);
			when(updated.getId()).thenReturn(ID);
			when(old.getMethodSensorAssignments()).thenReturn(Collections.singletonList(methodAssignment));
			when(old.getExceptionSensorAssignments()).thenReturn(Collections.singletonList(exceptionAssignment));
			when(updated.getMethodSensorAssignments()).thenReturn(Collections.singletonList(methodAssignment));
			when(updated.getExceptionSensorAssignments()).thenReturn(Collections.singletonList(exceptionAssignment));
			when(old.isActive()).thenReturn(false);
			when(updated.isActive()).thenReturn(true);
			ProfileUpdateEvent event = new ProfileUpdateEvent(this, old, updated);

			Collection<AbstractClassSensorAssignment<?>> removed = event.getRemovedSensorAssignments();

			assertThat(removed, is(empty()));
		}
	}

	public static class GetAddedSensorAssignments extends ProfileUpdateEventTest {

		@Test
		public void addedAssignments() {
			when(old.getId()).thenReturn(ID);
			when(updated.getId()).thenReturn(ID);
			when(old.getMethodSensorAssignments()).thenReturn(Collections.<MethodSensorAssignment> emptyList());
			when(old.getExceptionSensorAssignments()).thenReturn(Collections.<ExceptionSensorAssignment> emptyList());
			when(updated.getMethodSensorAssignments()).thenReturn(Collections.singletonList(methodAssignment));
			when(updated.getExceptionSensorAssignments()).thenReturn(Collections.singletonList(exceptionAssignment));
			when(old.isActive()).thenReturn(true);
			when(updated.isActive()).thenReturn(true);
			ProfileUpdateEvent event = new ProfileUpdateEvent(this, old, updated);

			Collection<AbstractClassSensorAssignment<?>> added = event.getAddedSensorAssignments();

			assertThat(added, hasSize(2));
			assertThat(added, hasItem(methodAssignment));
			assertThat(added, hasItem(exceptionAssignment));
		}

		@Test
		public void noChange() {
			when(old.getId()).thenReturn(ID);
			when(updated.getId()).thenReturn(ID);
			when(old.getMethodSensorAssignments()).thenReturn(Collections.singletonList(methodAssignment));
			when(old.getExceptionSensorAssignments()).thenReturn(Collections.singletonList(exceptionAssignment));
			when(updated.getMethodSensorAssignments()).thenReturn(Collections.singletonList(methodAssignment));
			when(updated.getExceptionSensorAssignments()).thenReturn(Collections.singletonList(exceptionAssignment));
			when(old.isActive()).thenReturn(true);
			when(updated.isActive()).thenReturn(true);
			ProfileUpdateEvent event = new ProfileUpdateEvent(this, old, updated);

			Collection<AbstractClassSensorAssignment<?>> added = event.getAddedSensorAssignments();

			assertThat(added, is(empty()));
		}

		@Test
		public void deactivated() {
			when(old.getId()).thenReturn(ID);
			when(updated.getId()).thenReturn(ID);
			when(old.getMethodSensorAssignments()).thenReturn(Collections.singletonList(methodAssignment));
			when(old.getExceptionSensorAssignments()).thenReturn(Collections.singletonList(exceptionAssignment));
			when(updated.getMethodSensorAssignments()).thenReturn(Collections.singletonList(methodAssignment));
			when(updated.getExceptionSensorAssignments()).thenReturn(Collections.singletonList(exceptionAssignment));
			when(old.isActive()).thenReturn(true);
			when(updated.isActive()).thenReturn(false);
			ProfileUpdateEvent event = new ProfileUpdateEvent(this, old, updated);

			Collection<AbstractClassSensorAssignment<?>> added = event.getAddedSensorAssignments();

			assertThat(added, is(empty()));
		}

		@Test
		public void activated() {
			when(old.getId()).thenReturn(ID);
			when(updated.getId()).thenReturn(ID);
			when(old.getMethodSensorAssignments()).thenReturn(Collections.singletonList(methodAssignment));
			when(old.getExceptionSensorAssignments()).thenReturn(Collections.singletonList(exceptionAssignment));
			when(updated.getMethodSensorAssignments()).thenReturn(Collections.singletonList(methodAssignment));
			when(updated.getExceptionSensorAssignments()).thenReturn(Collections.singletonList(exceptionAssignment));
			when(old.isActive()).thenReturn(false);
			when(updated.isActive()).thenReturn(true);
			ProfileUpdateEvent event = new ProfileUpdateEvent(this, old, updated);

			Collection<AbstractClassSensorAssignment<?>> added = event.getAddedSensorAssignments();

			assertThat(added, hasSize(2));
			assertThat(added, hasItem(methodAssignment));
			assertThat(added, hasItem(exceptionAssignment));
		}

	}

}
