package info.novatec.inspectit.rcp.details;

import org.eclipse.swt.graphics.Image;

/**
 * Defines content of one cell in the details table.
 * 
 * @author Ivan Senic
 * 
 */
public class DetailsCellContent {

	/**
	 * Text to display.
	 */
	private String text;

	/**
	 * Image to display.
	 */
	private Image image;

	/**
	 * Tool-tip on the image.
	 */
	private String imageToolTip;

	/**
	 * Denotes if the content might be a long text. If this option is used no image can be displayed
	 * alongside the text. In addition, {@link #grab} will be ignored if {@link #longText} is set to
	 * true.
	 */
	private boolean longText;

	/**
	 * How much columns should cell span.
	 */
	private int colspan = 1;

	/**
	 * If cell should grab horizontal space.
	 */
	private boolean grab = true;

	/**
	 * No-arg constructor.
	 */
	public DetailsCellContent() {
	}

	/**
	 * Constructor defining only text.
	 * 
	 * @param text
	 *            Text to display.
	 */
	public DetailsCellContent(String text) {
		this.text = text;
	}

	/**
	 * Constructor defining only text and if long text options should be used.
	 * 
	 * @param text
	 *            Text to display.
	 * @param longText
	 *            If text is expected to be a long text, thus different display options will be
	 *            used.
	 */
	public DetailsCellContent(String text, boolean longText) {
		this.text = text;
		this.longText = longText;
	}

	/**
	 * Constructor defining only image.
	 * 
	 * @param image
	 *            Image to display.
	 * @param imageToolTip
	 *            Tool-tip on the image.
	 */
	public DetailsCellContent(Image image, String imageToolTip) {
		this.image = image;
		this.imageToolTip = imageToolTip;
	}

	/**
	 * Constructor defining text and image.
	 * 
	 * @param text
	 *            Text to display.
	 * @param image
	 *            Image to display.
	 * @param imageToolTip
	 *            Tool-tip on the image.
	 */
	public DetailsCellContent(String text, Image image, String imageToolTip) {
		this.text = text;
		this.image = image;
		this.imageToolTip = imageToolTip;
	}

	/**
	 * Gets {@link #text}.
	 * 
	 * @return {@link #text}
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets {@link #text}.
	 * 
	 * @param text
	 *            New value for {@link #text}
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * Gets {@link #image}.
	 * 
	 * @return {@link #image}
	 */
	public Image getImage() {
		return image;
	}

	/**
	 * Sets {@link #image}.
	 * 
	 * @param image
	 *            New value for {@link #image}
	 */
	public void setImage(Image image) {
		this.image = image;
	}

	/**
	 * Gets {@link #imageToolTip}.
	 * 
	 * @return {@link #imageToolTip}
	 */
	public String getImageToolTip() {
		return imageToolTip;
	}

	/**
	 * Sets {@link #imageToolTip}.
	 * 
	 * @param imageToolTip
	 *            New value for {@link #imageToolTip}
	 */
	public void setImageToolTip(String imageToolTip) {
		this.imageToolTip = imageToolTip;
	}

	/**
	 * Gets {@link #longText}.
	 * 
	 * @return {@link #longText}
	 */
	boolean isLongText() {
		return longText;
	}

	/**
	 * Sets {@link #longText}.
	 * 
	 * @param longText
	 *            New value for {@link #longText}
	 */
	void setLongText(boolean longText) {
		this.longText = longText;
	}

	/**
	 * Gets {@link #colspan}.
	 * 
	 * @return {@link #colspan}
	 */
	public int getColspan() {
		return colspan;
	}

	/**
	 * Sets {@link #colspan}.
	 * 
	 * @param colspan
	 *            New value for {@link #colspan}
	 */
	public void setColspan(int colspan) {
		this.colspan = colspan;
	}

	/**
	 * Gets {@link #grab}.
	 * 
	 * @return {@link #grab}
	 */
	public boolean isGrab() {
		return grab;
	}

	/**
	 * Sets {@link #grab}.
	 * 
	 * @param grab
	 *            New value for {@link #grab}
	 */
	public void setGrab(boolean grab) {
		this.grab = grab;
	}

}
