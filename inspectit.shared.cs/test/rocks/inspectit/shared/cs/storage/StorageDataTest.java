package info.novatec.inspectit.storage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.novatec.inspectit.storage.label.AbstractStorageLabel;
import info.novatec.inspectit.storage.label.type.AbstractStorageLabelType;

import org.testng.annotations.Test;

/**
 * Test for {@link StorageData}.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("PMD")
public class StorageDataTest {

	/**
	 * Tests label insertions, querying and removals.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void labels() {
		AbstractStorageLabel<Object> storageLabel = mock(AbstractStorageLabel.class);
		AbstractStorageLabelType<Object> labelType = mock(AbstractStorageLabelType.class);
		when(storageLabel.getStorageLabelType()).thenReturn(labelType);
		when(labelType.isOnePerStorage()).thenReturn(false);

		StorageData storageData = new StorageData();
		// remove labels added in constructor
		storageData.getLabelList().clear();

		storageData.addLabel(storageLabel, false);
		storageData.addLabel(storageLabel, false);

		// no same label twice
		assertThat(storageData.getLabelList(), hasSize(1));
		assertThat(storageData.isLabelPresent(labelType), is(true));

		storageData.removeLabel(storageLabel);
		assertThat(storageData.getLabelList(), is(empty()));
		assertThat(storageData.isLabelPresent(labelType), is(false));

		when(labelType.isOnePerStorage()).thenReturn(true);
		AbstractStorageLabel<Object> storageLabel2 = mock(AbstractStorageLabel.class);
		when(storageLabel2.getStorageLabelType()).thenReturn(labelType);
		storageData.addLabel(storageLabel, false);
		storageData.addLabel(storageLabel2, false);

		// no overwrite
		assertThat(storageData.getLabelList(), hasSize(1));
		assertThat(storageData.getLabelList(), hasItem(storageLabel));

		// yes overwrite
		storageData.addLabel(storageLabel2, true);
		assertThat(storageData.getLabelList(), hasSize(1));
		assertThat(storageData.getLabelList(), hasItem(storageLabel2));

		// label by type
		assertThat(storageData.getLabels(labelType), hasSize(1));
		assertThat(storageData.getLabels(labelType), hasItem(storageLabel2));
	}

	/**
	 * Tests states.
	 */
	@Test
	public void states() {
		StorageData storageData = new StorageData();

		storageData.markOpened();
		assertThat(storageData.isStorageOpened(), is(true));
		assertThat(storageData.isStorageClosed(), is(false));
		assertThat(storageData.isStorageRecording(), is(false));

		storageData.markRecording();
		assertThat(storageData.isStorageOpened(), is(true));
		assertThat(storageData.isStorageClosed(), is(false));
		assertThat(storageData.isStorageRecording(), is(true));

		storageData.markClosed();
		assertThat(storageData.isStorageOpened(), is(false));
		assertThat(storageData.isStorageClosed(), is(true));
		assertThat(storageData.isStorageRecording(), is(false));
	}

}
