package info.novatec.inspectit.rcp.details;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

/**
 * Our own HTML-like table to use for displaying one part of the details for an element.
 * 
 * @see #DetailsTable(Composite, FormToolkit, String, int)
 * @see #addContentRow(String, Image, DetailsCellContent[])
 * 
 * @author Ivan Senic
 * 
 */
public class DetailsTable extends SectionPart {

	/**
	 * Width of the row title.
	 */
	private static final int ROW_TITLE_WIDTH_HINT = 150;

	/**
	 * Maximum suggested width for the text box displaying large text.
	 */
	private static final int CONTENT_CONTROL_MAX_WIDTH = 600;

	/**
	 * Maximum suggested height for the text box displaying large text.
	 */
	private static final int CONTENT_CONTROL_MAX_HEIGHT = 100;

	/**
	 * {@link FormText} to create elements.
	 */
	private FormToolkit toolkit;

	/**
	 * Number of columns in the information area. Note that this number should not include the
	 * column used for row title.
	 */
	private int columns;

	/**
	 * Content composite.
	 */
	private Composite contentComposite;

	/**
	 * Copy string builder.
	 */
	private StringBuilder copyStringBuilder = new StringBuilder(); // NOPMD

	/**
	 * Default constructor.
	 * 
	 * @param parent
	 *            Composite to create on.
	 * @param toolkit
	 *            {@link FormToolkit}
	 * @param heading
	 *            Name of the table that will be displayed as header.
	 * @param columns
	 *            Number of columns in the information area. Note that this number should not
	 *            include the column used for row title.
	 */
	public DetailsTable(Composite parent, FormToolkit toolkit, String heading, int columns) {
		super(parent, toolkit, Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
		this.columns = columns;
		this.toolkit = toolkit;

		getSection().setLayout(new TableWrapLayout());

		createHeading(heading);

		copyStringBuilder.append(heading);
		copyStringBuilder.append('\n');

		initTable();
	}

	/**
	 * Adds one row to the table..
	 * 
	 * @param title
	 *            Row title.
	 * @param image
	 *            Row image displayed next to the row title. Can be <code>null</code> for no image.
	 * @param cellContents
	 *            Array of {@link DetailsCellContent} objects representing the data in the row. Note
	 *            that the provided array should have same length as the {@link #columns} value
	 *            provided in the constructor.
	 */
	public void addContentRow(String title, Image image, DetailsCellContent[] cellContents) {
		createRowHeading(title, image);
		if (null != title) {
			copyStringBuilder.append(title);
		}
		copyStringBuilder.append('\t');

		// add each column
		for (int i = 0; i < columns; i++) {
			if (i < cellContents.length) {
				DetailsCellContent cellContent = cellContents[i];
				TableWrapData tableWrapData = getLayoutData(cellContent);
				if (cellContent.isLongText()) {
					Text text = toolkit.createText(contentComposite, cellContent.getText(), SWT.READ_ONLY | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
					tableWrapData.grabHorizontal = true;
					tableWrapData.maxWidth = CONTENT_CONTROL_MAX_WIDTH;
					tableWrapData.maxHeight = CONTENT_CONTROL_MAX_HEIGHT;
					text.setLayoutData(tableWrapData);
				} else {
					FormText formText = toolkit.createFormText(contentComposite, false);
					fillFormText(formText, cellContent);
					formText.setLayoutData(tableWrapData);
				}
				copyStringBuilder.append(cellContent.getText());
			} else {
				toolkit.createLabel(contentComposite, "");
			}

			if (i < columns - 1) {
				copyStringBuilder.append('\t');
			}
		}
		copyStringBuilder.append('\n');
	}

	/**
	 * Adds one row to the table by inserting SWT table as content.
	 * 
	 * @param title
	 *            Row title.
	 * @param image
	 *            Row image displayed next to the row title. Can be <code>null</code> for no image.
	 * @param cols
	 *            Number of columns in the SWT table
	 * @param headers
	 *            Titles for the columns.
	 * @param rows
	 *            Rows data. Each string array represents one row in the table.
	 */
	public void addContentTable(String title, Image image, int cols, String[] headers, List<String[]> rows) {
		createRowHeading(title, image);
		if (null != title) {
			copyStringBuilder.append(title);
		}

		Table table = toolkit.createTable(contentComposite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.V_SCROLL | SWT.VIRTUAL);
		table.setHeaderVisible(true);

		TableWrapData tableWrapData = new TableWrapData();
		tableWrapData.colspan = this.columns;
		tableWrapData.maxWidth = CONTENT_CONTROL_MAX_WIDTH;
		tableWrapData.maxHeight = CONTENT_CONTROL_MAX_HEIGHT;
		table.setLayoutData(tableWrapData);

		for (int i = 0; i < cols; i++) {
			TableColumn tableColumn = new TableColumn(table, SWT.LEFT);
			tableColumn.setWidth(CONTENT_CONTROL_MAX_WIDTH / cols - 10);
			tableColumn.setResizable(true);
			if (i < headers.length) {
				tableColumn.setText(headers[i]);
			}
		}

		for (String[] row : rows) {
			new TableItem(table, SWT.NONE).setText(row);

			copyStringBuilder.append('\t');
			for (int i = 0; i < row.length; i++) {
				copyStringBuilder.append(row[i]);
				if (i < row.length - 1) {
					copyStringBuilder.append('\t');
				}
			}
			copyStringBuilder.append('\n');
		}
	}

	/**
	 * @param cellContent
	 *            {@link DetailsCellContent}
	 * @return Returns the {@link TableWrapData} based in the information in the
	 *         {@link DetailsCellContent}.
	 */
	private TableWrapData getLayoutData(DetailsCellContent cellContent) {
		TableWrapData tableWrapData = new TableWrapData();
		tableWrapData.colspan = cellContent.getColspan();
		tableWrapData.grabHorizontal = cellContent.isGrab();
		return tableWrapData;
	}

	/**
	 * Fills {@link FormText} with content based on the {@link DetailsCellContent}.
	 * 
	 * @param formText
	 *            {@link FormText} to fill.
	 * @param cellContent
	 *            {@link DetailsCellContent} describing the content.
	 */
	private void fillFormText(FormText formText, DetailsCellContent cellContent) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("<form><p>");
		if (null != cellContent.getText()) {
			String text = StringUtils.replaceEach(cellContent.getText(), new String[] { "<", ">", "&" }, new String[] { "&lt;", "&gt;", "&amp;" });
			stringBuilder.append(text);
		}

		if (null != cellContent.getImage()) {
			Label label = new Label(formText, SWT.NONE);
			label.setImage(cellContent.getImage());
			if (null != cellContent.getImageToolTip()) {
				label.setToolTipText(cellContent.getImageToolTip());
			}
			stringBuilder.append("<control href=\"ctrl\"/>");
			formText.setControl("ctrl", label);
		}
		stringBuilder.append("</p></form>");
		formText.setText(stringBuilder.toString(), true, false);
	}

	/**
	 * Initializes the {@link #contentComposite}.
	 */
	private void initTable() {
		contentComposite = toolkit.createComposite(getSection());
		contentComposite.setLayoutData(new TableWrapData(TableWrapData.FILL));
		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = columns + 1;
		layout.horizontalSpacing = 2;
		layout.verticalSpacing = 2;
		contentComposite.setLayout(layout);

		getSection().setClient(contentComposite);
	}

	/**
	 * Creates header.
	 * 
	 * @param heading
	 *            Heading to use.
	 */
	private void createHeading(String heading) {
		getSection().setText(heading);
	}

	/**
	 * Creates row heading.
	 * 
	 * @param title
	 *            Title to use.
	 * @param image
	 *            Image to use. Can be <code>null</code>.
	 */
	private void createRowHeading(String title, Image image) {
		// first create help composite
		Composite composite = toolkit.createComposite(contentComposite);
		composite.setLayoutData(new TableWrapData(TableWrapData.FILL));
		GridLayout compositeLayout = new GridLayout(1, true);
		compositeLayout.marginHeight = 0;
		compositeLayout.marginBottom = 0;
		compositeLayout.horizontalSpacing = 0;
		compositeLayout.verticalSpacing = 0;
		composite.setLayout(compositeLayout);

		// create row text in bold
		FormText rowText = toolkit.createFormText(composite, false);
		if (null != image) {
			rowText.setText("<form><p><img href=\"img\"/> <span color=\"headingColor\">" + title + "</span></p></form>", true, false);
			rowText.setImage("img", image);
		} else {
			rowText.setText("<form><p><span color=\"headingColor\">" + title + "</span></p></form>", true, false);
		}
		rowText.setColor("headingColor", toolkit.getColors().getColor(IFormColors.TITLE));

		GridData rowTextGridData = getFixedWidthGridData(ROW_TITLE_WIDTH_HINT);
		rowTextGridData.verticalAlignment = SWT.TOP;
		rowText.setLayoutData(rowTextGridData);
	}

	/**
	 * Creates {@link GridData} with fixed width.
	 * 
	 * @param width
	 *            wanted width.
	 * @return {@link GridData}
	 */
	private GridData getFixedWidthGridData(int width) {
		GridData gridData = new GridData();
		gridData.minimumWidth = width;
		gridData.widthHint = width;
		return gridData;
	}

	/**
	 * Returns String of table content for copying purposes.
	 * 
	 * @return Returns String of table content for copying purposes.
	 */
	public String getCopyString() {
		return copyStringBuilder.toString();
	}

}
