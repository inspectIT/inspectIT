package rocks.inspectit.agent.java.eum.html;

/**
 *
 * Written according to {@link https://www.w3.org/TR/html5/syntax.html}. This parser does not fully
 * implement the HTML standard, however it is designed to support the most common patterns and to be
 * easily extensible if another case has to be added.
 *
 *
 * @author Jonas Kunz
 *
 */
public class HtmlTokenParser {

	/**
	 * Specifies the available token types supported by this parser.
	 *
	 * @author Jonas Kunz
	 */
	public enum Token {

		/**
		 * An opening tag, e.g. "&lt;div&gt;"
		 */
		START_TAG,

		/**
		 * A closing tag, e.g. "&lt;/div&gt;"
		 */
		END_TAG,

		/**
		 * A standalone tag, e.g. "&lt;img .../&gt;"
		 */
		STANDALONE_TAG,

		/**
		 * A html comment, e.g. "&lt;!-- this is a comment --&gt;".
		 */
		COMMENT
	}

	/**
	 * Specification of the results with which the parser can terminate.
	 *
	 * @author Jonas Kunz
	 *
	 */
	public enum Result {
		/**
		 * The parser has ran into the end of the provided character buffer and could not complete
		 * reading the token.
		 */
		INCOMPLETE,

		/**
		 * The provided source html contained syntax errors, therefore no token was parsed.
		 */
		FAILURE,

		/**
		 * A single token was parsed successfully and the results may be queried.
		 */
		SUCCESS
	}

	/**
	 * Internal status storing the progress in parsing the token.
	 *
	 * @author Jonas Kunz
	 *
	 */
	private enum Status {

		/**
		 * See {@link HtmlTokenParser#scanForOpeningBrace()}.
		 */
		SCAN_FOR_OPENING_BRACE,

		/**
		 * See {@link HtmlTokenParser#scanTagType()}.
		 */
		SCAN_TAG_TYPE,

		/**
		 * See {@link HtmlTokenParser#scanTagArguments()}.
		 */
		SCAN_TAG_ARGUMENTS,

		/**
		 * See {@link HtmlTokenParser#scanForCommentEnd()}.
		 */
		SCAN_FOR_COMMENT_END,

		/**
		 * See {@link HtmlTokenParser#scanForClosingBrace()()}.
		 */
		SCAN_FOR_CLOSING_BRACE,

		/**
		 * The parser has successfully completed parsing a token.
		 */
		COMPLETE,

		/**
		 * The token contained syntax error and therefore is unparseable.
		 */
		UNPARSEABLE

	}

	/**
	 * The underlying HTML source code.
	 */
	private CharSequence srcHtml;

	/**
	 * The caret specifying the current reading position within {@link HtmlTokenParser#srcHtml}.
	 */
	private Caret caret;

	/**
	 * The current {@link Status} of the parser. Stored to know where to continue.
	 */
	private Status status;

	/**
	 * The {@link Token} type of the current token being parsed.
	 */
	private Token parsedTokenType;

	/**
	 * The subsequence of {@link #srcHtml} denoting the tag type of the last token parsed. If the
	 * token is not a tag, this string is empty.
	 */
	private CharSequence tagType;

	/**
	 * The subsequence of {@link #srcHtml} denoting the tag arguments of the last token parsed. If
	 * the token is not a tag or does not store any arguments, this string is empty.
	 */
	private CharSequence arguments;

	/**
	 * Creates a new token parser.
	 *
	 * @param srcHtml
	 *            the input HTML source.
	 * @param readingPosition
	 *            the offset within the html source to start parsing at.
	 */
	public HtmlTokenParser(CharSequence srcHtml, int readingPosition) {
		this.caret = new Caret(srcHtml, readingPosition);
		this.srcHtml = srcHtml;
		resetState(); //NOPMD
	}

	/**
	 * Resets the state of the parser to be ready for parsing the next token. This does not affect
	 * the reading caret's position.
	 */
	public final void resetState() {
		status = Status.SCAN_FOR_OPENING_BRACE;
		parsedTokenType = null; //NOPMD
		tagType = "";
		arguments = "";
	}

	/**
	 * Returns the current reading position. This caret may be modified, but only if the parser is
	 * finished with the current token!
	 *
	 * @return the caret representing the current reading position
	 */
	public Caret getCaret() {
		return caret;
	}

	/**
	 * Queries the parsing results in form of the parsed token type. May only be called when the
	 * parser has terminated with {@link Result#SUCCESS}.
	 *
	 * @return the token type of the ast parsed token
	 */
	public Token getParsedTokenType() {
		if (status != Status.COMPLETE) {
			throw new IllegalStateException("Parsing has not completed yet!");
		}
		return parsedTokenType;
	}

	/**
	 * Queries the parsing results in form of the tag type (e.g. "div" for "&lt;div&gt");. May only
	 * be called when the parser has terminated with {@link Result#SUCCESS}.
	 *
	 * @return the tag type of the last parsed token
	 */
	public CharSequence getTagType() {
		if (status != Status.COMPLETE) {
			throw new IllegalStateException("Parsing has not completed yet!");
		}
		return tagType;
	}

	/**
	 * Queries the parsing results in form of the arguments specified in the tag. May only be called
	 * when the parser has terminated with {@link Result#SUCCESS}.
	 *
	 * @return the tag arguments of the last parsed token
	 */
	public CharSequence getTagArguments() {
		if (status != Status.COMPLETE) {
			throw new IllegalStateException("Parsing has not completed yet!");
		}
		return arguments;
	}

	/**
	 * Tries to read a token based on the current position within the underlying html source. This
	 * results in {@link Result#SUCCESS} if the parsed succeeded in parsing a token, or
	 * {@link Result#FAILURE} if the html source has an unknown or invalid syntax. If the parser
	 * reached the end of the underlying html source without completing parsing a token,
	 * {@link Result#INCOMPLETE} is returned. In this case, this method can be called again to
	 * continue parsing after the underlying CharSequence with the html source has been updated.<br>
	 * To reset the state for parsing the next token, use {@link #resetState()}
	 *
	 * @return the {@link Result} of the parsing process
	 */
	public Result parseToken() {
		process();
		switch (status) {
		case COMPLETE:
			return Result.SUCCESS;
		case UNPARSEABLE:
			return Result.FAILURE;
		default:
			return Result.INCOMPLETE;
		}
	}

	/**
	 * The method executing the state machine.
	 */
	private void process() {
		boolean completed;
		do {
			switch (status) {
			case SCAN_FOR_OPENING_BRACE:
				completed = scanForOpeningBrace();
				break;
			case SCAN_FOR_COMMENT_END:
				completed = scanForCommentEnd();
				break;
			case SCAN_TAG_TYPE:
				completed = scanTagType();
				break;
			case SCAN_TAG_ARGUMENTS:
				completed = scanTagArguments();
				break;
			case SCAN_FOR_CLOSING_BRACE:
				completed = scanForClosingBrace();
				break;
			case COMPLETE:
				return;
			case UNPARSEABLE:
				return;
			default:
				throw new IllegalStateException("Unhandled state by parser!");
			}
		} while (completed);
	}

	/**
	 * Tries to scan for an opening brace after skipping all preceding whitespace characters. A
	 * correct opening brace is either "&lt;" or "&lt;/".
	 *
	 * @return false, if the parsing could not be completed because the end of the HTML source was
	 *         reached. True otherwise.
	 */
	private boolean scanForOpeningBrace() {
		caret.walkAfterWhitespaces();
		if (caret.wayToEnd() >= 2) {
			if (caret.startsWithCheckCase("</")) {
				caret.goN(2);
				parsedTokenType = Token.END_TAG;
				status = Status.SCAN_TAG_TYPE;
			} else if (caret.startsWithCheckCase("<")) {
				caret.goN(1);
				parsedTokenType = Token.START_TAG;
				status = Status.SCAN_TAG_TYPE;
			} else {
				status = Status.UNPARSEABLE;
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Tries to scan for the end of a comment denoted by the "--&gt;" sequence.
	 *
	 * @return false, if the parsing could not be completed because the end of the HTML source was
	 *         reached. True otherwise.
	 */
	private boolean scanForCommentEnd() {
		int pos = caret.getOffset();
		if (caret.walkAfterMatchCheckCase("-->")) {
			status = Status.COMPLETE;
			return true;
		} else {
			caret.goTo(pos);
			return false;
		}
	}

	/**
	 * Scans the tag type directly following the opening brace. Also detects comments as they have
	 * the special tag type "!--".
	 *
	 * @return false, if the parsing could not be completed because the end of the HTML source was
	 *         reached. True otherwise.
	 */
	private boolean scanTagType() {
		// we need at least two characters for a decision
		if (caret.wayToEnd() < 2) {
			return false;
		}
		if (caret.startsWithCheckCase("!-")) {
			// we need three characters (!--) to be sure
			if (caret.wayToEnd() < 3) {
				return false;
			}
			if (caret.startsWithCheckCase("!--") && (parsedTokenType != Token.END_TAG)) {
				caret.goN("!--".length());
				parsedTokenType = Token.COMMENT;
				status = Status.SCAN_FOR_COMMENT_END;
			} else {
				status = Status.UNPARSEABLE;
			}
			return true;
		} else {
			int begin = caret.getOffset();
			// according to the HTML spec, tag names consist of [0-9a-zA-Z] characters.
			// we also allow a '?' or '!' at the beginning because of !DOCTYPE and ?XML.
			if ((caret.get(0) == '!') || (caret.get(0) == '?')) {
				caret.goN(1);
			}
			while (true) {
				char c = caret.get(0);
				if (((c >= '0') && (c <= '9')) || ((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z'))) {
					caret.goN(1);
					if (caret.endReached()) {
						caret.goTo(begin);
						return false;
					}
				} else {
					// a tag name may only be followed by a whitespace, / or >
					if (Character.isWhitespace(c) || (c == '/') || (c == '>')) {
						break;
					} else {
						status = Status.UNPARSEABLE;
						return true;
					}
				}
			}
			int end = caret.getOffset();
			if (begin == end) {
				status = Status.UNPARSEABLE; // empty tagnames are invalid
			} else {
				status = Status.SCAN_TAG_ARGUMENTS;
				this.tagType = srcHtml.subSequence(begin, end);
			}
			return true;
		}
	}

	/**
	 * Scans all tag arguments following the tag type up to the end of the tag, e.g. "href=..." for
	 * "&lt;a href=... &gt;". The syntax of this argument string is not checked.
	 *
	 * @return false, if the parsing could not be completed because the end of the HTML source was
	 *         reached. True otherwise.
	 */
	private boolean scanTagArguments() {
		caret.walkAfterWhitespaces();
		int argumentsBegin = caret.getOffset();
		// we walk forward until we meet a '/' or an '>' character, except if they are within
		// quotes.
		// for the <? xml .. ?> we also include the quotation mark as ending
		// specified arguments are correct
		while (true) {
			if (caret.endReached()) {
				caret.goTo(argumentsBegin);
				return false;
			} else if (caret.startsWithCheckCase("/") || caret.startsWithCheckCase("?") || caret.startsWithCheckCase(">")) {
				break;
			} else if (caret.startsWithCheckCase("\'")) {
				caret.goN(1);
				caret.walkAfterCharCheckCase('\'');
			} else if (caret.startsWithCheckCase("\"")) {
				caret.goN(1);
				caret.walkAfterCharCheckCase('\"');
			} else {
				caret.goN(1);
			}
		}
		caret.goN(-1); // walk back before '/' or '>'
		caret.walkBackBeforeWhitespaces();
		int argumentsEnd = Math.max(argumentsBegin, caret.getOffset() + 1);
		if ((parsedTokenType == Token.END_TAG) && (argumentsEnd > argumentsBegin)) {
			status = Status.UNPARSEABLE; // End tags must not contain arguments
		} else {
			this.arguments = srcHtml.subSequence(argumentsBegin, argumentsEnd);
			caret.goN(1);
			status = Status.SCAN_FOR_CLOSING_BRACE;
		}
		return true;

	}

	/**
	 * Scans for the closing brace finishing the tag. This can be either "&gt;&" or "/&gt;&" for
	 * standalone tags.
	 *
	 * @return false, if the parsing could not be completed because the end of the HTML source was
	 *         reached. True otherwise.
	 */
	private boolean scanForClosingBrace() {
		caret.walkAfterWhitespaces();
		if (caret.endReached()) {
			return false;
		}
		if (caret.startsWithCheckCase(">")) {
			caret.goN(1);
			status = Status.COMPLETE;
			return true;
		} else if (caret.wayToEnd() >= 2) {
			if (caret.startsWithCheckCase("/>") || caret.startsWithCheckCase("?>")) {
				if (parsedTokenType != Token.END_TAG) {
					caret.goN(2);
					parsedTokenType = Token.STANDALONE_TAG;
					status = Status.COMPLETE;
				} else {
					status = Status.UNPARSEABLE; //a tag must not be standalone and closing at the same time
				}
				return true;
			} else {
				status = Status.UNPARSEABLE;
				return true;
			}
		}
		return false;
	}

}
