package rocks.inspectit.shared.cs.cmr.property.update;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.shared.cs.cmr.property.update.AbstractPropertyUpdate;
import rocks.inspectit.shared.cs.cmr.property.update.configuration.ConfigurationUpdate;

@SuppressWarnings("PMD")
public class ConfigurationUpdateTest {

	private ConfigurationUpdate configurationUpdate;

	private ConfigurationUpdate toMerge;

	@Mock
	private AbstractPropertyUpdate<Object> propertyUpdate1;

	@Mock
	private AbstractPropertyUpdate<Object> propertyUpdate2;

	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
		configurationUpdate = new ConfigurationUpdate();
		configurationUpdate.addPropertyUpdate(propertyUpdate1);

		toMerge = new ConfigurationUpdate();
		toMerge.addPropertyUpdate(propertyUpdate2);
	}

	@Test
	public void merge() {
		when(propertyUpdate1.getPropertyLogicalName()).thenReturn("property1");
		when(propertyUpdate2.getPropertyLogicalName()).thenReturn("property2");

		configurationUpdate.merge(toMerge, false);

		assertThat(configurationUpdate.getPropertyUpdates(), hasSize(2));
	}

	@Test
	public void mergeOverwrite() {
		when(propertyUpdate1.getPropertyLogicalName()).thenReturn("property1");
		when(propertyUpdate2.getPropertyLogicalName()).thenReturn("property1");

		configurationUpdate.merge(toMerge, true);

		assertThat(configurationUpdate.getPropertyUpdates(), hasSize(1));
		assertThat(configurationUpdate.getPropertyUpdates(), hasItem(propertyUpdate2));
		assertThat(configurationUpdate.getPropertyUpdates(), not(hasItem(propertyUpdate1)));
	}

	@Test
	public void mergeNoOverwrite() {
		when(propertyUpdate1.getPropertyLogicalName()).thenReturn("property1");
		when(propertyUpdate2.getPropertyLogicalName()).thenReturn("property1");

		configurationUpdate.merge(toMerge, false);

		assertThat(configurationUpdate.getPropertyUpdates(), hasSize(1));
		assertThat(configurationUpdate.getPropertyUpdates(), hasItem(propertyUpdate1));
		assertThat(configurationUpdate.getPropertyUpdates(), not(hasItem(propertyUpdate2)));
	}
}
