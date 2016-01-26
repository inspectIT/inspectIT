package rocks.inspectit.server.ci.event;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;

import org.mockito.Mock;
import org.testng.annotations.Test;

import rocks.inspectit.server.ci.event.EnvironmentUpdateEvent;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.Profile;
import rocks.inspectit.shared.cs.ci.assignment.AbstractClassSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.ExceptionSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.MethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.SpecialMethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.factory.SpecialMethodSensorAssignmentFactory;

/**
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class EnvironmentUpdateEventTest extends TestBase {

	protected static final String ID = "";

	@Mock
	Environment old;

	@Mock
	Environment updated;

	@Mock
	SpecialMethodSensorAssignmentFactory functionalAssignmentFactory;

	@Mock
	Profile profile;

	public static class Constructor extends EnvironmentUpdateEventTest {

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void idsNotMatching() {
			when(updated.getId()).thenReturn(ID);
			when(old.getId()).thenReturn("smth");

			new EnvironmentUpdateEvent(this, old, updated, null, null);
		}
	}

	public static class GetRemovedSensorAssignments extends EnvironmentUpdateEventTest {

		@Test
		public void profileRemoved() {
			when(old.getId()).thenReturn(ID);
			when(updated.getId()).thenReturn(ID);
			MethodSensorAssignment methodAssignment = mock(MethodSensorAssignment.class);
			ExceptionSensorAssignment exceptionAssignment = mock(ExceptionSensorAssignment.class);

			when(profile.getMethodSensorAssignments()).thenReturn(Collections.singletonList(methodAssignment));
			when(profile.getExceptionSensorAssignments()).thenReturn(Collections.singletonList(exceptionAssignment));
			when(profile.isActive()).thenReturn(true);

			EnvironmentUpdateEvent event = new EnvironmentUpdateEvent(this, old, updated, null, Collections.singletonList(profile));

			Collection<AbstractClassSensorAssignment<?>> removed = event.getRemovedSensorAssignments(functionalAssignmentFactory);

			assertThat(removed, hasSize(2));
			assertThat(removed, hasItem(methodAssignment));
			assertThat(removed, hasItem(exceptionAssignment));
		}

		@Test
		public void profileRemovedNotActive() {
			when(old.getId()).thenReturn(ID);
			when(updated.getId()).thenReturn(ID);
			MethodSensorAssignment methodAssignment = mock(MethodSensorAssignment.class);
			ExceptionSensorAssignment exceptionAssignment = mock(ExceptionSensorAssignment.class);

			when(profile.getMethodSensorAssignments()).thenReturn(Collections.singletonList(methodAssignment));
			when(profile.getExceptionSensorAssignments()).thenReturn(Collections.singletonList(exceptionAssignment));
			when(profile.isActive()).thenReturn(false);

			EnvironmentUpdateEvent event = new EnvironmentUpdateEvent(this, old, updated, null, Collections.singletonList(profile));

			Collection<AbstractClassSensorAssignment<?>> removed = event.getRemovedSensorAssignments(functionalAssignmentFactory);

			assertThat(removed, is(empty()));
		}

		@Test
		public void functionalRemoved() {
			when(old.getId()).thenReturn(ID);
			when(updated.getId()).thenReturn(ID);
			SpecialMethodSensorAssignment functionalAssignment = mock(SpecialMethodSensorAssignment.class);

			when(functionalAssignmentFactory.getSpecialAssignments(old)).thenReturn(Collections.singletonList(functionalAssignment));
			when(functionalAssignmentFactory.getSpecialAssignments(updated)).thenReturn(Collections.<SpecialMethodSensorAssignment> emptyList());

			EnvironmentUpdateEvent event = new EnvironmentUpdateEvent(this, old, updated, null, null);

			Collection<AbstractClassSensorAssignment<?>> removed = event.getRemovedSensorAssignments(functionalAssignmentFactory);

			assertThat(removed, hasSize(1));
			assertThat(removed, hasItem(functionalAssignment));
		}

		@Test
		public void functionalNoChange() {
			when(old.getId()).thenReturn(ID);
			when(updated.getId()).thenReturn(ID);
			SpecialMethodSensorAssignment functionalAssignment = mock(SpecialMethodSensorAssignment.class);

			when(functionalAssignmentFactory.getSpecialAssignments(old)).thenReturn(Collections.singletonList(functionalAssignment));
			when(functionalAssignmentFactory.getSpecialAssignments(updated)).thenReturn(Collections.singletonList(functionalAssignment));

			EnvironmentUpdateEvent event = new EnvironmentUpdateEvent(this, old, updated, null, null);

			Collection<AbstractClassSensorAssignment<?>> removed = event.getRemovedSensorAssignments(functionalAssignmentFactory);

			assertThat(removed, is(empty()));
		}
	}

	public static class GetAddedSensorAssignments extends EnvironmentUpdateEventTest {

		@Test
		public void profileAdded() {
			when(old.getId()).thenReturn(ID);
			when(updated.getId()).thenReturn(ID);
			MethodSensorAssignment methodAssignment = mock(MethodSensorAssignment.class);
			ExceptionSensorAssignment exceptionAssignment = mock(ExceptionSensorAssignment.class);

			when(profile.getMethodSensorAssignments()).thenReturn(Collections.singletonList(methodAssignment));
			when(profile.getExceptionSensorAssignments()).thenReturn(Collections.singletonList(exceptionAssignment));
			when(profile.isActive()).thenReturn(true);

			EnvironmentUpdateEvent event = new EnvironmentUpdateEvent(this, old, updated, Collections.singletonList(profile), null);

			Collection<AbstractClassSensorAssignment<?>> added = event.getAddedSensorAssignments(functionalAssignmentFactory);

			assertThat(added, hasSize(2));
			assertThat(added, hasItem(methodAssignment));
			assertThat(added, hasItem(exceptionAssignment));
		}

		@Test
		public void profileRemovedNotActive() {
			when(old.getId()).thenReturn(ID);
			when(updated.getId()).thenReturn(ID);
			MethodSensorAssignment methodAssignment = mock(MethodSensorAssignment.class);
			ExceptionSensorAssignment exceptionAssignment = mock(ExceptionSensorAssignment.class);

			when(profile.getMethodSensorAssignments()).thenReturn(Collections.singletonList(methodAssignment));
			when(profile.getExceptionSensorAssignments()).thenReturn(Collections.singletonList(exceptionAssignment));
			when(profile.isActive()).thenReturn(false);

			EnvironmentUpdateEvent event = new EnvironmentUpdateEvent(this, old, updated, Collections.singletonList(profile), null);

			Collection<AbstractClassSensorAssignment<?>> added = event.getAddedSensorAssignments(functionalAssignmentFactory);

			assertThat(added, is(empty()));
		}

		@Test
		public void functionalAdded() {
			when(old.getId()).thenReturn(ID);
			when(updated.getId()).thenReturn(ID);
			SpecialMethodSensorAssignment functionalAssignment = mock(SpecialMethodSensorAssignment.class);

			when(functionalAssignmentFactory.getSpecialAssignments(old)).thenReturn(Collections.<SpecialMethodSensorAssignment> emptyList());
			when(functionalAssignmentFactory.getSpecialAssignments(updated)).thenReturn(Collections.singletonList(functionalAssignment));

			EnvironmentUpdateEvent event = new EnvironmentUpdateEvent(this, old, updated, null, null);

			Collection<AbstractClassSensorAssignment<?>> added = event.getAddedSensorAssignments(functionalAssignmentFactory);

			assertThat(added, hasSize(1));
			assertThat(added, hasItem(functionalAssignment));
		}

		@Test
		public void functionalNoChange() {
			when(old.getId()).thenReturn(ID);
			when(updated.getId()).thenReturn(ID);
			SpecialMethodSensorAssignment functionalAssignment = mock(SpecialMethodSensorAssignment.class);

			when(functionalAssignmentFactory.getSpecialAssignments(old)).thenReturn(Collections.singletonList(functionalAssignment));
			when(functionalAssignmentFactory.getSpecialAssignments(updated)).thenReturn(Collections.singletonList(functionalAssignment));

			EnvironmentUpdateEvent event = new EnvironmentUpdateEvent(this, old, updated, null, null);

			Collection<AbstractClassSensorAssignment<?>> added = event.getAddedSensorAssignments(functionalAssignmentFactory);

			assertThat(added, is(empty()));
		}
	}

}
