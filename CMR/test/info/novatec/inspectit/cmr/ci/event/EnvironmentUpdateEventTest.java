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

import info.novatec.inspectit.ci.Environment;
import info.novatec.inspectit.ci.Profile;
import info.novatec.inspectit.ci.assignment.AbstractClassSensorAssignment;
import info.novatec.inspectit.ci.assignment.impl.ExceptionSensorAssignment;
import info.novatec.inspectit.ci.assignment.impl.FunctionalMethodSensorAssignment;
import info.novatec.inspectit.ci.assignment.impl.MethodSensorAssignment;
import info.novatec.inspectit.ci.factory.FunctionalMethodSensorAssignmentFactory;
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
public class EnvironmentUpdateEventTest extends TestBase {

	protected static final String ID = "";

	protected EnvironmentUpdateEvent event;

	@Mock
	protected Environment old;

	@Mock
	protected Environment updated;

	@Mock
	protected FunctionalMethodSensorAssignmentFactory functionalAssignmentFactory;

	@Mock
	protected Profile profile;

	@BeforeMethod
	public void init() {
		when(old.getId()).thenReturn(ID);
		when(updated.getId()).thenReturn(ID);
	}

	public static class Constructor extends EnvironmentUpdateEventTest {

		@Test(expectedExceptions = IllegalArgumentException.class)
		public void idsNotMatching() {
			when(old.getId()).thenReturn("smth");

			event = new EnvironmentUpdateEvent(this, old, updated, null, null);
		}
	}

	public static class RemovedAssignments extends EnvironmentUpdateEventTest {

		@Test
		public void profileRemoved() {
			MethodSensorAssignment methodAssignment = mock(MethodSensorAssignment.class);
			ExceptionSensorAssignment exceptionAssignment = mock(ExceptionSensorAssignment.class);

			when(profile.getMethodSensorAssignments()).thenReturn(Collections.singletonList(methodAssignment));
			when(profile.getExceptionSensorAssignments()).thenReturn(Collections.singletonList(exceptionAssignment));
			when(profile.isActive()).thenReturn(true);

			event = new EnvironmentUpdateEvent(this, old, updated, null, Collections.singletonList(profile));

			Collection<AbstractClassSensorAssignment<?>> removed = event.getRemovedSensorAssignments(functionalAssignmentFactory);

			assertThat(removed, hasSize(2));
			assertThat(removed, hasItem(methodAssignment));
			assertThat(removed, hasItem(exceptionAssignment));
		}

		@Test
		public void profileRemovedNotActive() {
			MethodSensorAssignment methodAssignment = mock(MethodSensorAssignment.class);
			ExceptionSensorAssignment exceptionAssignment = mock(ExceptionSensorAssignment.class);

			when(profile.getMethodSensorAssignments()).thenReturn(Collections.singletonList(methodAssignment));
			when(profile.getExceptionSensorAssignments()).thenReturn(Collections.singletonList(exceptionAssignment));
			when(profile.isActive()).thenReturn(false);

			event = new EnvironmentUpdateEvent(this, old, updated, null, Collections.singletonList(profile));

			Collection<AbstractClassSensorAssignment<?>> removed = event.getRemovedSensorAssignments(functionalAssignmentFactory);

			assertThat(removed, is(empty()));
		}

		@Test
		public void functionalRemoved() {
			FunctionalMethodSensorAssignment functionalAssignment = mock(FunctionalMethodSensorAssignment.class);

			when(functionalAssignmentFactory.getFunctionalAssignments(old)).thenReturn(Collections.singletonList(functionalAssignment));
			when(functionalAssignmentFactory.getFunctionalAssignments(updated)).thenReturn(Collections.<FunctionalMethodSensorAssignment> emptyList());

			event = new EnvironmentUpdateEvent(this, old, updated, null, null);

			Collection<AbstractClassSensorAssignment<?>> removed = event.getRemovedSensorAssignments(functionalAssignmentFactory);

			assertThat(removed, hasSize(1));
			assertThat(removed, hasItem(functionalAssignment));
		}

		@Test
		public void functionalNoChange() {
			FunctionalMethodSensorAssignment functionalAssignment = mock(FunctionalMethodSensorAssignment.class);

			when(functionalAssignmentFactory.getFunctionalAssignments(old)).thenReturn(Collections.singletonList(functionalAssignment));
			when(functionalAssignmentFactory.getFunctionalAssignments(updated)).thenReturn(Collections.singletonList(functionalAssignment));

			event = new EnvironmentUpdateEvent(this, old, updated, null, null);

			Collection<AbstractClassSensorAssignment<?>> removed = event.getRemovedSensorAssignments(functionalAssignmentFactory);

			assertThat(removed, is(empty()));
		}
	}

	public static class AddedAssignments extends EnvironmentUpdateEventTest {

		@Test
		public void profileAdded() {
			MethodSensorAssignment methodAssignment = mock(MethodSensorAssignment.class);
			ExceptionSensorAssignment exceptionAssignment = mock(ExceptionSensorAssignment.class);

			when(profile.getMethodSensorAssignments()).thenReturn(Collections.singletonList(methodAssignment));
			when(profile.getExceptionSensorAssignments()).thenReturn(Collections.singletonList(exceptionAssignment));
			when(profile.isActive()).thenReturn(true);

			event = new EnvironmentUpdateEvent(this, old, updated, Collections.singletonList(profile), null);

			Collection<AbstractClassSensorAssignment<?>> added = event.getAddedSensorAssignments(functionalAssignmentFactory);

			assertThat(added, hasSize(2));
			assertThat(added, hasItem(methodAssignment));
			assertThat(added, hasItem(exceptionAssignment));
		}

		@Test
		public void profileRemovedNotActive() {
			MethodSensorAssignment methodAssignment = mock(MethodSensorAssignment.class);
			ExceptionSensorAssignment exceptionAssignment = mock(ExceptionSensorAssignment.class);

			when(profile.getMethodSensorAssignments()).thenReturn(Collections.singletonList(methodAssignment));
			when(profile.getExceptionSensorAssignments()).thenReturn(Collections.singletonList(exceptionAssignment));
			when(profile.isActive()).thenReturn(false);

			event = new EnvironmentUpdateEvent(this, old, updated, Collections.singletonList(profile), null);

			Collection<AbstractClassSensorAssignment<?>> added = event.getAddedSensorAssignments(functionalAssignmentFactory);

			assertThat(added, is(empty()));
		}

		@Test
		public void functionalAdded() {
			FunctionalMethodSensorAssignment functionalAssignment = mock(FunctionalMethodSensorAssignment.class);

			when(functionalAssignmentFactory.getFunctionalAssignments(old)).thenReturn(Collections.<FunctionalMethodSensorAssignment> emptyList());
			when(functionalAssignmentFactory.getFunctionalAssignments(updated)).thenReturn(Collections.singletonList(functionalAssignment));

			event = new EnvironmentUpdateEvent(this, old, updated, null, null);

			Collection<AbstractClassSensorAssignment<?>> added = event.getAddedSensorAssignments(functionalAssignmentFactory);

			assertThat(added, hasSize(1));
			assertThat(added, hasItem(functionalAssignment));
		}

		@Test
		public void functionalNoChange() {
			FunctionalMethodSensorAssignment functionalAssignment = mock(FunctionalMethodSensorAssignment.class);

			when(functionalAssignmentFactory.getFunctionalAssignments(old)).thenReturn(Collections.singletonList(functionalAssignment));
			when(functionalAssignmentFactory.getFunctionalAssignments(updated)).thenReturn(Collections.singletonList(functionalAssignment));

			event = new EnvironmentUpdateEvent(this, old, updated, null, null);

			Collection<AbstractClassSensorAssignment<?>> added = event.getAddedSensorAssignments(functionalAssignmentFactory);

			assertThat(added, is(empty()));
		}
	}

}
