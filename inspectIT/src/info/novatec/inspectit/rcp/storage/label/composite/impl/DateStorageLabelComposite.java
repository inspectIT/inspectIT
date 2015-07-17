package info.novatec.inspectit.rcp.storage.label.composite.impl;

import info.novatec.inspectit.rcp.storage.label.composite.AbstractStorageLabelComposite;
import info.novatec.inspectit.storage.label.AbstractStorageLabel;
import info.novatec.inspectit.storage.label.DateStorageLabel;
import info.novatec.inspectit.storage.label.type.AbstractStorageLabelType;

import java.util.Date;

import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

/**
 * Composite for selecting the {@link DateStorageLabel}.
 * 
 * @author Ivan Senic
 * 
 */
public class DateStorageLabelComposite extends AbstractStorageLabelComposite {

	/**
	 * Label type.
	 */
	private AbstractStorageLabelType<Date> dateStorageLabelType;

	/**
	 * {@link CDateTime} for selecting Date.
	 */
	private CDateTime cDateTime;

	/**
	 * Should a label be displayed next to the selection widget.
	 */
	private boolean showLabel;

	/**
	 * Default constructor.
	 * 
	 * @param parent
	 *            Parent.
	 * @param style
	 *            Style.
	 * @param dateStorageLabelType
	 *            Storage label type.
	 * @see Composite#Composite(Composite, int)
	 */
	public DateStorageLabelComposite(Composite parent, int style, AbstractStorageLabelType<Date> dateStorageLabelType) {
		this(parent, style, dateStorageLabelType, true);
	}

	/**
	 * Secondary constructor. Defines if the label should be displayed in the composite.
	 * 
	 * @param parent
	 *            Parent.
	 * @param style
	 *            Style.
	 * @param dateStorageLabelType
	 *            Storage label type.
	 * @param showLabel
	 *            Should label be displayed next to the selection widget.
	 * @see Composite#Composite(Composite, int)
	 */
	public DateStorageLabelComposite(Composite parent, int style, AbstractStorageLabelType<Date> dateStorageLabelType, boolean showLabel) {
		super(parent, style);
		this.dateStorageLabelType = dateStorageLabelType;
		this.showLabel = showLabel;
		initComposite();
	}

	/**
	 * Initializes the composite.
	 */
	private void initComposite() {
		if (showLabel) {
			GridLayout gl = new GridLayout(2, false);
			this.setLayout(gl);

			new Label(this, SWT.NONE).setText("Enter date:");
		} else {
			GridLayout gl = new GridLayout(1, false);
			this.setLayout(gl);
		}
		cDateTime = new CDateTime(this, CDT.BORDER | CDT.DROP_DOWN | CDT.TAB_FIELDS);
		cDateTime.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		cDateTime.setSelection(new Date());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractStorageLabel<?> getStorageLabel() {
		return new DateStorageLabel(cDateTime.getSelection(), dateStorageLabelType);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isInputValid() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addListener(Listener pageCompletionListener) {
		cDateTime.addListener(SWT.Modify, pageCompletionListener);
	}

}
