package rocks.inspectit.ui.rcp.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.Bullet;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.widgets.Display;

/**
 * LineStyleListener for {@link StyledText} that draws line numbers.
 *
 * @author Alexander Wert
 *
 */
public class LineNumbersStyleListener implements LineStyleListener {

	/**
	 * Text to apply the line style to.
	 */
	private StyledText styledText;

	/**
	 * Constructor.
	 *
	 * @param styledText
	 *            Text to apply the line style to.
	 */
	public LineNumbersStyleListener(StyledText styledText) {
		super();
		this.styledText = styledText;
		this.styledText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				// redraw line numbers
				LineNumbersStyleListener.this.styledText.redraw();
			}
		});
	}

	@Override
	public void lineGetStyle(LineStyleEvent event) {
		StyleRange range = new StyleRange();
		range.foreground = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
		int maxNumLine = styledText.getLineCount();
		int bulletLength = Integer.toString(maxNumLine).length();
		// right padding
		int widthBullet = ((bulletLength + 1) * styledText.getLineHeight()) / 2;
		range.metrics = new GlyphMetrics(0, 0, widthBullet);
		event.bullet = new Bullet(ST.BULLET_TEXT, range);
		event.bullet.text = String.format("%" + bulletLength + "s", styledText.getLineAtOffset(event.lineOffset) + 1);
	}

	/**
	 * Applies {@link LineNumbersStyleListener} to given text.
	 * 
	 * @param styledText
	 *            Text to apply the line style to.
	 */
	public static void apply(StyledText styledText) {
		styledText.addLineStyleListener(new LineNumbersStyleListener(styledText));
	}
}
