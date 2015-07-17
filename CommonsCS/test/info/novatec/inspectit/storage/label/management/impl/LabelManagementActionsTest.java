package info.novatec.inspectit.storage.label.management.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import info.novatec.inspectit.cmr.service.IStorageService;
import info.novatec.inspectit.storage.StorageException;
import info.novatec.inspectit.storage.label.AbstractStorageLabel;
import info.novatec.inspectit.storage.label.type.AbstractStorageLabelType;

import java.util.Collections;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test the label management actions.
 * 
 * @author Ivan Senic
 * 
 */
@SuppressWarnings("PMD")
public class LabelManagementActionsTest {

	/**
	 * Add action to test.
	 */
	private AddLabelManagementAction addLabelManagementAction;

	/**
	 * Remove action to test.
	 */
	private RemoveLabelManagementAction removeLabelManagementAction;

	@Mock
	private IStorageService storageService;

	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Add label type.
	 */
	@Test
	public void addLabelTypeAction() {
		AbstractStorageLabelType<?> labelType = mock(AbstractStorageLabelType.class);
		addLabelManagementAction = new AddLabelManagementAction(labelType);
		addLabelManagementAction.execute(storageService);

		verify(storageService, times(1)).saveLabelType(labelType);
		verifyNoMoreInteractions(storageService);
	}

	/**
	 * Add label.
	 */
	@Test
	public void addLabelAction() {
		AbstractStorageLabel<?> label = mock(AbstractStorageLabel.class);
		addLabelManagementAction = new AddLabelManagementAction(Collections.<AbstractStorageLabel<?>> singletonList(label));
		addLabelManagementAction.execute(storageService);

		verify(storageService, times(1)).saveLabelToCmr(label);
		verifyNoMoreInteractions(storageService);
	}

	/**
	 * No exceptions and no interactions with storage service with empty action.
	 */
	@Test
	public void emptyAddAction() {
		addLabelManagementAction = new AddLabelManagementAction();
		verifyNoMoreInteractions(storageService);
	}

	/**
	 * Remove label type.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void removeLabelTypeAction() throws StorageException {
		AbstractStorageLabelType<Object> labelType = mock(AbstractStorageLabelType.class);
		when(storageService.getLabelSuggestions(labelType)).thenReturn(Collections.<AbstractStorageLabel<Object>> emptyList());
		removeLabelManagementAction = new RemoveLabelManagementAction(labelType, false);
		removeLabelManagementAction.execute(storageService);

		verify(storageService, times(1)).getLabelSuggestions(labelType);
		verify(storageService, times(1)).removeLabelType(labelType);
		verify(storageService, times(1)).removeLabelsFromCmr(Collections.<AbstractStorageLabel<?>> emptyList(), false);
		verifyNoMoreInteractions(storageService);

		removeLabelManagementAction.setRemoveFromStorageAlso(true);
		removeLabelManagementAction.execute(storageService);

		verify(storageService, times(1)).removeLabelsFromCmr(Collections.<AbstractStorageLabel<?>> emptyList(), true);
	}

	/**
	 * Remove label.
	 */
	@Test
	public void removeLabelAction() throws StorageException {
		AbstractStorageLabel<?> label = mock(AbstractStorageLabel.class);
		removeLabelManagementAction = new RemoveLabelManagementAction(Collections.<AbstractStorageLabel<?>> singletonList(label), false);
		removeLabelManagementAction.execute(storageService);

		verify(storageService, times(1)).removeLabelsFromCmr(Collections.<AbstractStorageLabel<?>> singletonList(label), false);

		removeLabelManagementAction.setRemoveFromStorageAlso(true);
		removeLabelManagementAction.execute(storageService);

		verify(storageService, times(1)).removeLabelsFromCmr(Collections.<AbstractStorageLabel<?>> singletonList(label), true);
		verifyNoMoreInteractions(storageService);
	}

	/**
	 * No exceptions and no interactions with storage service with empty action.
	 */
	@Test
	public void emptyRemoveAction() {
		removeLabelManagementAction = new RemoveLabelManagementAction();
		verifyNoMoreInteractions(storageService);
	}
}
