package rocks.inspectit.server.ci.event;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.Profile;
import rocks.inspectit.shared.cs.ci.assignment.AbstractClassSensorAssignment;
import rocks.inspectit.shared.cs.ci.profile.data.SensorAssignmentProfileData;

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
	SensorAssignmentProfileData oldProfileData;

	@Mock
	SensorAssignmentProfileData updatedProfileData;

	@Mock
	AbstractClassSensorAssignment<?> assignment;

	@BeforeMethod
	public void setupProfileData() {
		doReturn(oldProfileData).when(old).getProfileData();
		doReturn(updatedProfileData).when(updated).getProfileData();
		when(oldProfileData.isOfType(SensorAssignmentProfileData.class)).thenReturn(true);
		when(updatedProfileData.isOfType(SensorAssignmentProfileData.class)).thenReturn(true);
	}

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
			doReturn(Collections.singletonList(assignment)).when(oldProfileData).getData(SensorAssignmentProfileData.class);
			doReturn(Collections.<AbstractClassSensorAssignment<?>> emptyList()).when(updatedProfileData).getData(SensorAssignmentProfileData.class);
			when(old.isActive()).thenReturn(true);
			when(updated.isActive()).thenReturn(true);
			ProfileUpdateEvent event = new ProfileUpdateEvent(this, old, updated);

			Collection<AbstractClassSensorAssignment<?>> removed = event.getRemovedSensorAssignments();

			assertThat(removed, hasSize(1));
			assertThat(removed, hasItem(assignment));
		}

		@Test
		public void noChange() {
			when(old.getId()).thenReturn(ID);
			when(updated.getId()).thenReturn(ID);
			doReturn(Collections.singletonList(assignment)).when(oldProfileData).getData(SensorAssignmentProfileData.class);
			doReturn(Collections.singletonList(assignment)).when(updatedProfileData).getData(SensorAssignmentProfileData.class);
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
			doReturn(Collections.singletonList(assignment)).when(oldProfileData).getData(SensorAssignmentProfileData.class);
			doReturn(Collections.singletonList(assignment)).when(updatedProfileData).getData(SensorAssignmentProfileData.class);
			when(old.isActive()).thenReturn(true);
			when(updated.isActive()).thenReturn(false);
			ProfileUpdateEvent event = new ProfileUpdateEvent(this, old, updated);

			Collection<AbstractClassSensorAssignment<?>> removed = event.getRemovedSensorAssignments();

			assertThat(removed, hasSize(1));
			assertThat(removed, hasItem(assignment));
		}

		@Test
		public void activated() {
			when(old.getId()).thenReturn(ID);
			when(updated.getId()).thenReturn(ID);
			doReturn(Collections.singletonList(assignment)).when(oldProfileData).getData(SensorAssignmentProfileData.class);
			doReturn(Collections.singletonList(assignment)).when(updatedProfileData).getData(SensorAssignmentProfileData.class);
			when(old.isActive()).thenReturn(false);
			when(updated.isActive()).thenReturn(true);
			ProfileUpdateEvent event = new ProfileUpdateEvent(this, old, updated);

			Collection<AbstractClassSensorAssignment<?>> removed = event.getRemovedSensorAssignments();

			assertThat(removed, is(empty()));
		}

		@Test
		public void wrongProfileData() {
			when(old.getId()).thenReturn(ID);
			when(updated.getId()).thenReturn(ID);
			when(oldProfileData.isOfType(SensorAssignmentProfileData.class)).thenReturn(false);
			when(updatedProfileData.isOfType(SensorAssignmentProfileData.class)).thenReturn(false);
			when(old.isActive()).thenReturn(true);
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
			doReturn(Collections.<AbstractClassSensorAssignment<?>> emptyList()).when(oldProfileData).getData(SensorAssignmentProfileData.class);
			doReturn(Collections.singletonList(assignment)).when(updatedProfileData).getData(SensorAssignmentProfileData.class);
			when(old.isActive()).thenReturn(true);
			when(updated.isActive()).thenReturn(true);
			ProfileUpdateEvent event = new ProfileUpdateEvent(this, old, updated);

			Collection<AbstractClassSensorAssignment<?>> added = event.getAddedSensorAssignments();

			assertThat(added, hasSize(1));
			assertThat(added, hasItem(assignment));
		}

		@Test
		public void noChange() {
			when(old.getId()).thenReturn(ID);
			when(updated.getId()).thenReturn(ID);
			doReturn(Collections.singletonList(assignment)).when(oldProfileData).getData(SensorAssignmentProfileData.class);
			doReturn(Collections.singletonList(assignment)).when(updatedProfileData).getData(SensorAssignmentProfileData.class);
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
			doReturn(Collections.singletonList(assignment)).when(oldProfileData).getData(SensorAssignmentProfileData.class);
			doReturn(Collections.singletonList(assignment)).when(updatedProfileData).getData(SensorAssignmentProfileData.class);
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
			doReturn(Collections.singletonList(assignment)).when(oldProfileData).getData(SensorAssignmentProfileData.class);
			doReturn(Collections.singletonList(assignment)).when(updatedProfileData).getData(SensorAssignmentProfileData.class);
			when(old.isActive()).thenReturn(false);
			when(updated.isActive()).thenReturn(true);
			ProfileUpdateEvent event = new ProfileUpdateEvent(this, old, updated);

			Collection<AbstractClassSensorAssignment<?>> added = event.getAddedSensorAssignments();

			assertThat(added, hasSize(1));
			assertThat(added, hasItem(assignment));
		}

		@Test
		public void wrongProfileData() {
			when(old.getId()).thenReturn(ID);
			when(updated.getId()).thenReturn(ID);
			when(oldProfileData.isOfType(SensorAssignmentProfileData.class)).thenReturn(false);
			when(updatedProfileData.isOfType(SensorAssignmentProfileData.class)).thenReturn(false);
			when(old.isActive()).thenReturn(true);
			when(updated.isActive()).thenReturn(true);
			ProfileUpdateEvent event = new ProfileUpdateEvent(this, old, updated);

			Collection<AbstractClassSensorAssignment<?>> added = event.getAddedSensorAssignments();

			assertThat(added, is(empty()));
		}
	}

}
