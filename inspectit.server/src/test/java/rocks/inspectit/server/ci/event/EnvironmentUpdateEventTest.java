package rocks.inspectit.server.ci.event;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.Environment;
import rocks.inspectit.shared.cs.ci.Profile;
import rocks.inspectit.shared.cs.ci.assignment.AbstractClassSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.SpecialMethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.factory.SpecialMethodSensorAssignmentFactory;
import rocks.inspectit.shared.cs.ci.profile.data.SensorAssignmentProfileData;

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

	@Mock
	SensorAssignmentProfileData profileData;

	@BeforeMethod
	public void setupProfileData() {
		doReturn(profileData).when(profile).getProfileData();
		when(profileData.isOfType(SensorAssignmentProfileData.class)).thenReturn(true);
	}

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
			AbstractClassSensorAssignment<?> assignment = mock(AbstractClassSensorAssignment.class);

			doReturn(Collections.singletonList(assignment)).when(profileData).getData(SensorAssignmentProfileData.class);
			when(profile.isActive()).thenReturn(true);

			EnvironmentUpdateEvent event = new EnvironmentUpdateEvent(this, old, updated, null, Collections.singletonList(profile));

			Collection<AbstractClassSensorAssignment<?>> removed = event.getRemovedSensorAssignments(functionalAssignmentFactory);

			assertThat(removed, hasSize(1));
			assertThat(removed, hasItem(assignment));
		}

		@Test
		public void profileRemovedNotActive() {
			when(old.getId()).thenReturn(ID);
			when(updated.getId()).thenReturn(ID);
			AbstractClassSensorAssignment<?> assignment = mock(AbstractClassSensorAssignment.class);

			doReturn(Collections.singletonList(assignment)).when(profileData).getData(SensorAssignmentProfileData.class);
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

		@Test
		public void wrongProfileData() {
			when(old.getId()).thenReturn(ID);
			when(updated.getId()).thenReturn(ID);

			when(profileData.isOfType(SensorAssignmentProfileData.class)).thenReturn(false);
			when(profile.isActive()).thenReturn(true);

			EnvironmentUpdateEvent event = new EnvironmentUpdateEvent(this, old, updated, null, Collections.singletonList(profile));

			Collection<AbstractClassSensorAssignment<?>> removed = event.getRemovedSensorAssignments(functionalAssignmentFactory);

			assertThat(removed, is(empty()));
		}

	}

	public static class GetAddedSensorAssignments extends EnvironmentUpdateEventTest {

		@Test
		public void profileAdded() {
			when(old.getId()).thenReturn(ID);
			when(updated.getId()).thenReturn(ID);
			AbstractClassSensorAssignment<?> assignment = mock(AbstractClassSensorAssignment.class);

			doReturn(Collections.singletonList(assignment)).when(profileData).getData(SensorAssignmentProfileData.class);
			when(profile.isActive()).thenReturn(true);

			EnvironmentUpdateEvent event = new EnvironmentUpdateEvent(this, old, updated, Collections.singletonList(profile), null);

			Collection<AbstractClassSensorAssignment<?>> added = event.getAddedSensorAssignments(functionalAssignmentFactory);

			assertThat(added, hasSize(1));
			assertThat(added, hasItem(assignment));
		}

		@Test
		public void profileRemovedNotActive() {
			when(old.getId()).thenReturn(ID);
			when(updated.getId()).thenReturn(ID);
			AbstractClassSensorAssignment<?> assignment = mock(AbstractClassSensorAssignment.class);

			doReturn(Collections.singletonList(assignment)).when(profileData).getData(SensorAssignmentProfileData.class);
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

		@Test
		public void wrongProfileData() {
			when(old.getId()).thenReturn(ID);
			when(updated.getId()).thenReturn(ID);

			when(profileData.isOfType(SensorAssignmentProfileData.class)).thenReturn(false);
			when(profile.isActive()).thenReturn(true);

			EnvironmentUpdateEvent event = new EnvironmentUpdateEvent(this, old, updated, null, Collections.singletonList(profile));

			Collection<AbstractClassSensorAssignment<?>> added = event.getAddedSensorAssignments(functionalAssignmentFactory);

			assertThat(added, is(empty()));
		}
	}

}
