package rocks.inspectit.agent.java.eum.html;

import rocks.inspectit.agent.java.eum.html.HTMLTokenParser.Result;
import rocks.inspectit.agent.java.eum.html.HTMLTokenParser.Token;
import rocks.inspectit.agent.java.util.CharacterRingBuffer;


/**
 * A lightweight html parser for injecting a script into a streamed html file on the fly. Features:
 * <ul>
 * <li>early out as soon as the file is detected to be non-html (e.g. the data is image data or a
 * xml-file)
 * <li>detects and ignores comments in the file
 * <li>tries to place the tag in the head tag, if it is not present the tag will be placed in the
 * body tag
 * <li>avoid double-injection by checking if the string starting at the injection point matches
 * exactly the tag to inject
 * </ul>
 *
 * @author Jonas Kunz
 */
public class StreamedHTMLScriptInjector {

	/**
	 * The ringbuffer used for storing the html source. Data gets erased from the beginning as soon
	 * as it has been identified as a valid HTML token to keep the memory footprint low.
	 */
	private CharacterRingBuffer htmlSource;

	/**
	 * The token parser used to parse the HTML tokens.
	 */
	private HTMLTokenParser tokenParser;

	/**
	 * Specification of the available states of the injector.
	 *
	 * @author Jonas Kunz
	 *
	 */
	private enum Status {
		/**
		 * See {@link StreamedHTMLScriptInjector#scanHtmlPreamble()}.
		 */
		SCAN_HTML_PREAMBLE,

		/**
		 * See {@link StreamedHTMLScriptInjector#scanForHtmlTag()}.
		 */
		SCAN_FOR_HTML_TAG,

		/**
		 * See {@link StreamedHTMLScriptInjector#scanForHeadTag()}.
		 */
		SCAN_FOR_HEAD_TAG,

		/**
		 * See {@link StreamedHTMLScriptInjector#scanForBodyTag()}.
		 */
		SCAN_FOR_BODY_TAG,

		/**
		 * State to mark that an injection point has been found. The injection point is after the
		 * last parsed token.
		 */
		INJECTION_POINT_FOUND,

		/**
		 * Marks that the injection has terminated, either successful or without an injection.
		 */
		TERMINATED,

	}

	/**
	 * Stores the {@link Status} of the parser.
	 */
	private Status status;

	/**
	 * The script tag to be injected.
	 */
	private String tagToInject;

	/**
	 * Creates and initializes a new injector.
	 *
	 * @param tagToInject
	 *            the tag which this injector should try to inject.
	 */
	public StreamedHTMLScriptInjector(String tagToInject) {
		htmlSource = new CharacterRingBuffer();
		tokenParser = new HTMLTokenParser(htmlSource, 0);
		status = Status.SCAN_HTML_PREAMBLE;
		this.tagToInject = tagToInject;
	}

	/**
	 * Tries to perform an injection on the given source code. <br>
	 * The html file may be split arbitarily by calling this method for each part of the code in
	 * order. If the injector has already finished, it returns immediately without additional
	 * overhead.
	 *
	 * @param htmlData
	 *            the new data to append to the internal buffer of html source
	 * @return null, if no injection was performed. Otherwise, a new modified String containing the
	 *         injected tag is returned.
	 */
	public String performInjection(CharSequence htmlData) {
		if (hasTerminated()) {
			return null;
		}
		Caret caret = tokenParser.getCaret();
		int sourceAppendPos = htmlSource.length();
		htmlSource.append(htmlData);

		while (!hasTerminated()) {
			Result tokenParsingResult = tokenParser.parseToken();
			switch (tokenParsingResult) {
			case FAILURE:
				abortInjectionPointSearch();
				return null;
			case INCOMPLETE:
				return null;
			case SUCCESS:
				processToken();
				if (status == Status.INJECTION_POINT_FOUND) {
					String returnValue;
					// attempt to prevent double injection
					if (!caret.startsWithCheckCase(tagToInject)) {
						StringBuilder newHtmlData = new StringBuilder();
						int normalizedCaretPos = caret.getOffset() - sourceAppendPos;
						newHtmlData.append(htmlData.subSequence(0, normalizedCaretPos));
						newHtmlData.append(tagToInject);
						newHtmlData.append(htmlData.subSequence(normalizedCaretPos, htmlData.length()));
						returnValue = newHtmlData.toString();
					} else {
						returnValue = null; // NOPMD
					}
					abortInjectionPointSearch();
					return returnValue;
				} else if (status != Status.TERMINATED) {
					// free processed token
					htmlSource.erase(caret.getOffset());
					sourceAppendPos -= caret.getOffset();
					// reposition the caret at the old position
					caret.goTo(0);
					tokenParser.resetState(); // get ready for parsing the next token
				}
				break;
			default:
				throw new RuntimeException("Unhandled token parsing result: " + tokenParsingResult);
			}
		}
		return null;
	}

	/**
	 * Aborts the search for an injection point.
	 */
	protected void abortInjectionPointSearch() {
		status = Status.TERMINATED;
		// free unnecessary resource
		tokenParser = null; // NOPMD
	}

	/**
	 * @return true, if the injector has finished, either successfully or without injection
	 */
	public boolean hasTerminated() {
		return status == Status.TERMINATED;
	}

	/**
	 * Processes the last token parsed by the {@link #tokenParser}.
	 */
	private void processToken() {
		if (tokenParser.getParsedTokenType() == Token.COMMENT) {
			return;
		}
		switch (status) {
		case SCAN_HTML_PREAMBLE:
			scanHtmlPreamble();
			break;
		case SCAN_FOR_HTML_TAG:
			scanForHtmlTag();
			break;
		case SCAN_FOR_HEAD_TAG:
			scanForHeadTag();
			break;
		case SCAN_FOR_BODY_TAG:
			scanForBodyTag();
			break;
		default:
			throw new RuntimeException("Unhandled parser state: " + status);
		}
	}

	/**
	 * Scans for an opening html tag, skipping preamble tags like or !DOCTYPE.<br>
	 * HTML delivered as XML (starting with an ?xml tag) is currently not supported
	 */
	private void scanHtmlPreamble() {
		// Preamble checking based on the information on this page
		// http://wiki.selfhtml.org/wiki/HTML/Dokumentstruktur_und_Aufbau#HTML5
		// we also allow html without preamble, directly starting with the <html> tag
		if (CharSequenceUtil.checkEqualIgnoreCase(tokenParser.getTagType(), "!DOCTYPE")) {
			// Doctypes are formated as opening tags
			if (tokenParser.getParsedTokenType() != Token.START_TAG) {
				abortInjectionPointSearch();
				return;
			}
			// we accept any doctype starting with "html"
			if (!CharSequenceUtil.checkEqualIgnoreCase(tokenParser.getTagArguments(), 0, 4, "html", 0, 4)) {
				abortInjectionPointSearch();
				return;
			}
			// DOCTYPE okay, proceed with the next tag scanning for the html tag
			status = Status.SCAN_FOR_HTML_TAG;
		} else {
			// no preamble tag found, we assume the html is starting immediately
			status = Status.SCAN_FOR_HTML_TAG;
			processToken();
		}
	}

	/**
	 * Tries to find an opening html tag.
	 */
	private void scanForHtmlTag() {
		if (CharSequenceUtil.checkEqualIgnoreCase(tokenParser.getTagType(), "html")) {
			if (tokenParser.getParsedTokenType() != Token.START_TAG) {
				abortInjectionPointSearch();
				return;
			}
			status = Status.SCAN_FOR_HEAD_TAG;
		} else {
			// current token is not the html tag, we assume the document starts immediately with the
			// head
			status = Status.SCAN_FOR_HEAD_TAG;
			processToken();
		}
	}

	/**
	 * Tries to find an opening head tag. Omitting both the head and the body tag is currently not
	 * supported.
	 */
	private void scanForHeadTag() {
		if (CharSequenceUtil.checkEqualIgnoreCase(tokenParser.getTagType(), "head")) {
			if (tokenParser.getParsedTokenType() != Token.START_TAG) {
				abortInjectionPointSearch();
				return;
			}
			// Perform injection after start of the head tag
			status = Status.INJECTION_POINT_FOUND;
		} else {
			// current token is not the head tag, we assume it must be the body tag (head is empty)
			status = Status.SCAN_FOR_BODY_TAG;
			processToken();
		}
	}

	/**
	 * Tries to find an opening body tag. Omitting both the head and the body tag is currently not
	 * supported.
	 */
	private void scanForBodyTag() {
		if (CharSequenceUtil.checkEqualIgnoreCase(tokenParser.getTagType(), "body")) {
			if (tokenParser.getParsedTokenType() != Token.START_TAG) {
				abortInjectionPointSearch();
				return;
			}
			// Perform injection after start of the head tag
			status = Status.INJECTION_POINT_FOUND;
		} else {
			// current token is not the head tag, we assume it must be the body tag (head is empty)
			abortInjectionPointSearch();
		}
	}

}
