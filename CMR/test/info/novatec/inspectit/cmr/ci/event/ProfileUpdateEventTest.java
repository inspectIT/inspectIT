package info.novatec.inspectit.cmr.ci.event;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
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

		@BeforeMethod
		public void setup() {
			when(old.getId()).thenReturn(ID);
			when(updated.getId()).thenReturn(ID);
		}

		@Test
		public void deactivated() {
			when(old.isActive()).thenReturn(true);
			when(updated.isActive()).thenReturn(false);
			ProfileUpdateEvent event = new ProfileUpdateEvent(this, old, updated);

			boolean deactivated = event.isProfileDeactivated();

			assertThat(deactivated, is(true));
		}

		@Test
		public void wasNotActive() {
			when(old.isActive()).thenReturn(false);
			when(updated.isActive()).thenReturn(false);
			ProfileUpdateEvent event = new ProfileUpdateEvent(this, old, updated);

			boolean deactivated = event.isProfileDeactivated();

			assertThat(deactivated, is(false));
		}

		@Test
		public void stillActive() {
			when(old.isActive()).thenReturn(true);
			when(updated.isActive()).thenReturn(true);
			ProfileUpdateEvent event = new ProfileUpdateEvent(this, old, updated);

			boolean deactivated = event.isProfileDeactivated();

			assertThat(deactivated, is(false));
		}
	}

	public static class IsProfileActivated extends ProfileUpdateEventTest {

		@BeforeMethod
		public void setup() {
			when(old.getId()).thenReturn(ID);
			when(updated.getId()).thenReturn(ID);
		}

		@Test
		public void activated() {
			when(old.isActive()).thenReturn(false);
			when(updated.isActive()).thenReturn(true);
			ProfileUpdateEvent event = new ProfileUpdateEvent(this, old, updated);

			boolean activated = event.isProfileActivated();

			assertThat(activated, is(true));
		}

		@Test
		public void wasActive() {
			when(old.isActive()).thenReturn(true);
			when(updated.isActive()).thenReturn(true);
			ProfileUpdateEvent event = new ProfileUpdateEvent(this, old, updated);

			boolean activated = event.isProfileActivated();

			assertThat(activated, is(false));
		}

		@Test
		public void stillNotActive() {
			when(old.isActive()).thenReturn(false);
			when(updated.isActive()).thenReturn(false);
			ProfileUpdateEvent event = new ProfileUpdateEvent(this, old, updated);

			boolean activated = event.isProfileActivated();

			assertThat(activated, is(false));
		}
	}

	public static class GetRemovedSensorAssignments extends ProfileUpdateEventTest {

		@BeforeMethod
		public void setup() {
			when(old.getId()).thenReturn(ID);
			when(updated.getId()).thenReturn(ID);
		}

		@Test
		public void removedAssignments() {
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
