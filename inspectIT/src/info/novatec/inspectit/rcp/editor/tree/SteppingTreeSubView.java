package info.novatec.inspectit.rcp.editor.tree;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.editor.preferences.IPreferenceGroup;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.editor.tree.input.SteppingTreeInputController;
import info.novatec.inspectit.rcp.util.ElementOccurrenceCount;

import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * View that enables locating the element in the tree via {@link SteppingControl}.
 * 
 * @author Ivan Senic
 * 
 */
public class SteppingTreeSubView extends TreeSubView {

	/**
	 * Main composite for this view. It holds the {@link org.eclipse.jface.viewers.TreeViewer} and
	 * additionally {@link SteppingControl} if necessary.
	 */
	private Composite subComposite;

	/**
	 * Stepping control.
	 */
	private SteppingControl steppingControl;

	/**
	 * Input controller for this view.
	 */
	private SteppingTreeInputController steppingTreeInputController;

	/**
	 * Default constructor.
	 * 
	 * @param treeInputController
	 *            Stepping tree input controller.
	 * @see TreeSubView#TreeSubView(info.novatec.inspectit.rcp.editor.tree.input.TreeInputController)
	 */
	public SteppingTreeSubView(SteppingTreeInputController treeInputController) {
		super(treeInputController);

		this.steppingTreeInputController = treeInputController;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void createPartControl(Composite parent, FormToolkit toolkit) {
		subComposite = toolkit.createComposite(parent);
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		subComposite.setLayout(layout);

		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		subComposite.setLayoutData(gd);

		super.createPartControl(subComposite, toolkit);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		getTreeViewer().getTree().setLayoutData(gd);

		if (steppingControl == null) {
			steppingControl = new SteppingControl(subComposite, toolkit, steppingTreeInputController.getSteppingObjectList());
		}

		if (steppingTreeInputController.initSteppingControlVisible() && null != steppingTreeInputController.getSteppingObjectList() && !steppingTreeInputController.getSteppingObjectList().isEmpty()) {
			steppingControl.showControl();
		}

		// the focus has to be passed to the subComposite, because it can not register it
		getTreeViewer().getTree().addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				subComposite.notifyListeners(SWT.FocusIn, null);
			}
		});

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Control getControl() {
		return subComposite;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDataInput(List<? extends DefaultData> data) {
		super.setDataInput(data);
		steppingControl.inputChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void preferenceEventFired(PreferenceEvent preferenceEvent) {
		super.preferenceEventFired(preferenceEvent);
		switch (preferenceEvent.getPreferenceId()) {
		case STEPPABLE_CONTROL:
			Map<IPreferenceGroup, Object> preferenceMap = preferenceEvent.getPreferenceMap();
			Object isChecked = preferenceMap.get(PreferenceId.SteppableControl.BUTTON_STEPPABLE_CONTROL_ID);
			if (isChecked instanceof Boolean) {
				Boolean makeControlVisible = (Boolean) isChecked;
				if (makeControlVisible) {
					steppingControl.showControl();
				} else {
					steppingControl.hideControl();
				}
			}
			break;
		case CLEAR_BUFFER:
		case FILTERDATATYPE:
		case INVOCFILTEREXCLUSIVETIME:
		case INVOCFILTERTOTALTIME:
			steppingControl.inputChanged();
			break;
		default:
			break;
		}
	}

	/**
	 * Adds new element to the stepping control. This method will also register the new object with
	 * the {@link SteppingTreeInputController}.
	 * 
	 * @param element
	 *            Object to be added.
	 */
	public void addObjectToSteppingControl(Object element) {
		steppingTreeInputController.addObjectToSteppingObjectList(element);
		if (steppingControl.isControlShown()) {
			steppingControl.inputChanged();
			steppingControl.selectObject(element);
		} else {
			steppingControl.showControl();
		}
	}

	/**
	 * Alters the state of the stepping control button on preference panel.
	 * 
	 * @param checked
	 *            Should button be checked or not.
	 */
	private void setSwitchSteppingControlButtonChecked(boolean checked) {
		this.getRootEditor().getPreferencePanel().setSteppingControlChecked(checked);
	}

	/**
	 * Tries to expand the tree viewer to the wanted occurrence of wanted element. If the wanted
	 * occurrence is not reachable, nothing is done. Otherwise the tree is expanded and element
	 * selected.
	 * 
	 * @param template
	 *            Element to reach.
	 * @param occurrence
	 *            Wanted occurrence in the tree.
	 */
	private void expandToObject(Object template, int occurrence) {
		Object realElement = steppingTreeInputController.getElement(template, occurrence, getTreeViewer().getFilters());
		if (null != realElement) {
			((DeferredTreeViewer) getTreeViewer()).expandToObjectAndSelect(realElement, 0);
		}
	}

	/**
	 * Counts total occurrences found for given element. This method is just delegating the call to
	 * the {@link SteppingTreeInputController}. Result depends on the filters that are currently
	 * active for the tree.
	 * 
	 * @param element
	 *            Element to count occurrences.
	 * @return Total number of elements found.
	 */
	private ElementOccurrenceCount countOccurrences(Object element) {
		return steppingTreeInputController.countOccurrences(element, getTreeViewer().getFilters());
	}

	/**
	 * Is input set for this sub view.
	 * 
	 * @return True is input is not null or if it is not empty. Otherwise false.
	 */
	@SuppressWarnings("unchecked")
	private boolean isInputSet() {
		List<Object> input = (List<Object>) getTreeViewer().getInput();
		if (input == null || input.isEmpty()) {
			return false;
		}
		return true;
	}

	/**
	 * Stepping control class.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	private class SteppingControl {

		/**
		 * Composite where stepping control will be created.
		 */
		private Composite parent;

		/**
		 * Toolkit.
		 */
		private FormToolkit toolkit;

		/**
		 * List of objects that are able to be located in the tree.
		 */
		private List<Object> steppableObjects;

		/**
		 * List of objects that are able currently in the combo.
		 */
		private List<Object> objectsInCombo;

		/**
		 * Main composite of stepping control.
		 */
		private Composite mainComposite;

		/**
		 * Combo for object selection.
		 */
		private Combo objectSelection;

		/**
		 * Next button.
		 */
		private Button next;

		/**
		 * Previous button.
		 */
		private Button previous;

		/**
		 * Clear all button.
		 */
		private Button clearAllButton;

		/**
		 * Information label.
		 */
		private Label info;

		/**
		 * Flag for defining is the control show or not.
		 */
		private boolean controlShown = false;

		/**
		 * The currently selected object that is to be found in the tree.
		 */
		private Object selectedObject;

		/**
		 * Current displayed occurrence of selected object.
		 */
		private int occurrence;

		/**
		 * Visible occurrence of the selected object that could be reached.
		 */
		private int visibleOccurrences;

		/**
		 * Filtered occurrence of the selected object that could not be reached.
		 */
		private int filteredOccurrences;

		/**
		 * Default constructor.
		 * 
		 * @param parent
		 *            Composite where stepping control will be created.
		 * @param toolkit
		 *            Toolkit.
		 * @param objectList
		 *            List of objects that are able to be located in the tree.
		 */
		public SteppingControl(Composite parent, FormToolkit toolkit, List<Object> objectList) {
			super();
			this.parent = parent;
			this.toolkit = toolkit;
			this.steppableObjects = objectList;
		}

		/**
		 * Creates stepping control.
		 * 
		 * @param parent
		 *            Composite where stepping control will be created.
		 * @param toolkit
		 *            Toolkit.
		 */
		private void createPartControl(Composite parent, FormToolkit toolkit) {
			mainComposite = toolkit.createComposite(parent);
			GridLayout layout = new GridLayout(7, false);
			mainComposite.setLayout(layout);
			mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

			toolkit.createLabel(mainComposite, "Object to locate:");

			objectSelection = new Combo(mainComposite, SWT.SIMPLE | SWT.DROP_DOWN | SWT.READ_ONLY);
			GridData gd = new GridData();
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalAlignment = GridData.FILL;
			gd.minimumWidth = 200;
			objectSelection.setLayoutData(gd);

			previous = toolkit.createButton(mainComposite, "Previous", SWT.PUSH | SWT.NO_BACKGROUND);
			previous.setEnabled(false);
			previous.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_PREVIOUS));

			next = toolkit.createButton(mainComposite, "Next", SWT.PUSH | SWT.NO_BACKGROUND);
			next.setEnabled(false);
			next.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_NEXT));

			info = toolkit.createLabel(mainComposite, "No invocation loaded");

			// added additional composite to the right, so that minimizing and maximizing the window
			// can look better
			Composite helpComposite = toolkit.createComposite(mainComposite);
			gd = new GridData();
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalAlignment = GridData.FILL;
			gd.minimumWidth = 0;
			gd.heightHint = 0;
			gd.widthHint = 0;
			helpComposite.setLayoutData(gd);

			clearAllButton = toolkit.createButton(mainComposite, "", SWT.PUSH | SWT.NO_BACKGROUND);
			clearAllButton.setEnabled(false);
			clearAllButton.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_TRASH));
			clearAllButton.setToolTipText("Empty steppable objects list");

			objectSelection.addListener(SWT.Modify, new Listener() {
				@Override
				public void handleEvent(Event event) {
					int selectionIndex = objectSelection.getSelectionIndex();
					if (selectionIndex != -1) {
						Object selObject = objectsInCombo.get(selectionIndex);
						selectedObject = selObject;
						if (isInputSet()) {
							occurrence = 0;
							ElementOccurrenceCount elementOccurrenceCount = countOccurrences(selectedObject);
							visibleOccurrences = elementOccurrenceCount.getVisibleOccurrences();
							filteredOccurrences = elementOccurrenceCount.getFilteredOccurrences();
							if (visibleOccurrences > 0) {
								expandToObject(selectedObject, ++occurrence);
							}
							if (visibleOccurrences <= occurrence) {
								next.setEnabled(false);
							} else {
								next.setEnabled(true);
							}
							if (occurrence <= 1) {
								previous.setEnabled(false);
							} else {
								previous.setEnabled(true);
							}
						}
					} else {
						next.setEnabled(false);
						previous.setEnabled(false);
					}
					updateInfoBox();
				}
			});

			next.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					expandToObject(selectedObject, ++occurrence);
					if (visibleOccurrences <= occurrence) {
						next.setEnabled(false);
					}
					if (occurrence <= 1) {
						previous.setEnabled(false);
					} else {
						previous.setEnabled(true);
					}
					updateInfoBox();
				}
			});

			previous.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					expandToObject(selectedObject, --occurrence);
					next.setEnabled(true);
					if (occurrence <= 1) {
						previous.setEnabled(false);
					}
					updateInfoBox();
				}
			});

			clearAllButton.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					clearAll();
				}
			});

			controlShown = true;
		}

		/**
		 * Clears all objects from the list.
		 */
		private void clearAll() {
			steppableObjects.clear();
			objectSelection.removeAll();
			objectsInCombo.clear();
			selectedObject = null; // NOPMD
			occurrence = 0;
			inputChanged();
		}

		/**
		 * 
		 * 
		 * @param object
		 *            One of the objects that are to be located in the tree.
		 * @return Returns the string to be inserted into the combo box for supplied object.
		 */
		private String getTextualString(Object object) {
			String representation = steppingTreeInputController.getElementTextualRepresentation(object);
			// Assure that string is not too long
			if (representation.length() > 120) {
				representation = representation.substring(0, 118) + "..";
			}
			ElementOccurrenceCount elementOccurrenceCount = countOccurrences(object);
			return representation + " (" + elementOccurrenceCount.getVisibleOccurrences() + " visible, " + elementOccurrenceCount.getFilteredOccurrences() + " filtered)";
		}

		/**
		 * Selects the given object in the stepping control, if the object is currently in the
		 * combo-box.
		 * 
		 * @param element
		 *            Element to select.
		 */
		public void selectObject(Object element) {
			if (controlShown) {
				int index = objectsInCombo.indexOf(element);
				if (index != -1) {
					objectSelection.select(index);
				}
			}
		}

		/**
		 * Hides stepping control.
		 */
		public void hideControl() {
			if (controlShown) {
				mainComposite.dispose();
				subComposite.layout();
				controlShown = false;
				setSwitchSteppingControlButtonChecked(false);
			}
		}

		/**
		 * Shows stepping control.
		 */
		public void showControl() {
			if (!controlShown) {
				createPartControl(parent, toolkit);
				subComposite.layout();
				controlShown = true;
				inputChanged();
				setSwitchSteppingControlButtonChecked(true);
			}
		}

		/**
		 * Resets stepping control.
		 */
		public void inputChanged() {
			if (controlShown) {
				if (isInputSet()) {
					objectsInCombo = steppableObjects;
					objectSelection.removeAll();
					if (!objectsInCombo.isEmpty()) {
						clearAllButton.setEnabled(true);
						for (Object object : objectsInCombo) {
							objectSelection.add(getTextualString(object));
						}
						objectSelection.pack(true);
						if (null != selectedObject && objectsInCombo.contains(selectedObject)) {
							objectSelection.select(objectsInCombo.indexOf(selectedObject));
						} else {
							objectSelection.select(0);
						}
					} else {
						next.setEnabled(false);
						previous.setEnabled(false);
						clearAllButton.setEnabled(false);
						updateInfoBox();
					}
				} else {
					objectSelection.removeAll();
					next.setEnabled(false);
					previous.setEnabled(false);
					updateInfoBox();
				}
				mainComposite.layout();
			}
		}

		/**
		 * Updates the text in the info box based on the current status of the stepping control.
		 */
		private void updateInfoBox() {
			if (controlShown) {
				String msg = "";
				if (isInputSet() && objectSelection.getSelectionIndex() != -1) {
					if (occurrence == 0 && visibleOccurrences != 0) {
						msg = "Found " + visibleOccurrences + " occurrence";
						if (visibleOccurrences > 1) {
							msg += "s";
						}

					} else if (occurrence != 0) {
						msg = occurrence + "/" + visibleOccurrences;
					} else {
						msg = "No occurrences found";
					}
					if (filteredOccurrences > 0) {
						msg += " (" + filteredOccurrences + " filtered out)";
					}
				} else if (objectSelection.getItemCount() == 0) {
					msg = "No object to locate";
				} else {
					msg = "No invocation loaded";
				}
				info.setText(msg);
				mainComposite.layout();
			}
		}

		/**
		 * @return the controlShown
		 */
		public boolean isControlShown() {
			return controlShown;
		}

	}
}
