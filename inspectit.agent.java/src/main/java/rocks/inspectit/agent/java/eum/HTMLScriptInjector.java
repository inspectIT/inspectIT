package rocks.inspectit.agent.java.eum;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A lightweight html parser for injecting a script into a streamed html file on the fly. Features:
 * - early out as soon as the file is detected to be non-html (e.g. the data is image data or a
 * xml-file) - detects and ignores comments in the file - tries to place the tag in the head tag, if
 * it is not present the tag will be placed in the body tag - supports encoded character data if the
 * encoding is given - avoid double-injection by checking if the string starting at the injection
 * point matches exactly the tag to inject
 *
 * @author Jonas Kunz
 */
public class HTMLScriptInjector {

	/**
	 * The logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(HTMLScriptInjector.class);

	/**
	 * The parser operates as a status machine. These are the statuses the parser knows.
	 *
	 * @author Jonas Kunz
	 */
	private enum Status {
		/**
		 * Initialisation status, the parser has not started or has not found anything it can base
		 * its decision on.
		 */
		INIT,
		/**
		 * Status saying that the document is non-html. No Injection will be performed, the parser
		 * has terminated.
		 */
		UNPARESABLE,

		/**
		 * The parser has entered the html-tag and will look for the head / body tags.
		 */
		HTML_TAG_ENTERED,

		/**
		 * The injection point has been found, the parser has terminated. The "pos" carret will
		 * point at the position to insert the tag.
		 */
		INJECTION_POINT_FOUND,
	}

	/**
	 * The default encoding used by the servlet api.
	 */
	private static final String DEFAULT_ENCODING = "ISO-8859-1";

	/**
	 * constant used for code readability.
	 */
	private static final byte[] NO_LEFTOVER_CHARACTER_BYTES = {};

	/**
	 * A StringBuffer buffering of previous data. Not everything needs to be buffered, for example
	 * already closed tags can be omitted.
	 */
	// Warning can be ignored as we regularly clear the buffer.
	// in future will be replace with ringbuffer for better performance
	@SuppressWarnings({ "PMD", "stringbuffer" })
	private StringBuffer src;

	/**
	 * The carret representing the current scan position.
	 */
	private Carret carret;

	/**
	 * The current status of the parser.
	 */
	private Status status;

	/**
	 * The tag which will be injected if a position is found.
	 */
	private String tagToInject;

	/**
	 * the left over of previous writes of incomplete encoded characters (e.g. only the first byte
	 * of an encoded special character).
	 */
	private byte[] leftOver = NO_LEFTOVER_CHARACTER_BYTES;

	/**
	 * The decoder used for decoding encoded character data.
	 */
	private CharsetDecoder charDecoder;

	/**
	 * The endecoder used for encoding character data.
	 */
	private CharsetEncoder charEncoder;

	/**
	 * Creates a new Parser.
	 *
	 * @param tagToInject
	 *            The tag to inject
	 */
	public HTMLScriptInjector(String tagToInject) {
		this.tagToInject = tagToInject;
		src = new StringBuffer();
		carret = new Carret(src, 0);
		status = Status.INIT;
	}

	/**
	 * Sets the encoding. Should be used before the first encoded data was given to this parser.
	 *
	 * @param charsetName
	 *            the name of the characterset e.g. UTF-8 or whatever
	 */
	public void setEncoding(String charsetName) {
		Charset chars = Charset.forName(charsetName);
		charEncoder = chars.newEncoder();
		charDecoder = chars.newDecoder();
	}

	/**
	 * Appends the given string to the html document. If this String contains the tag-injection
	 * position, the tag will be injected and the modified String is returned.
	 *
	 * @param str
	 *            the string containing the next characters of the document
	 * @return the original string if it did not contain the injection point, or the original stirng
	 *         with tag inserted at the correct position.
	 */
	public String performInjection(String str) {
		// in case of messed up encoding we terminate
		if (leftOver != NO_LEFTOVER_CHARACTER_BYTES) {
			LOG.debug("Messed up document encoding, aborting tag injection."); // NOPMD
			status = Status.UNPARESABLE;
			src.delete(0, src.length());
			return str;
		}
		if (hasTerminated()) {
			return str;
		} else {
			int offset = src.length();
			src.append(str);
			process();
			if (hasInjectionPointBeenFound()) {
				int injectPos = carret.getOffset() - offset;
				String before = str.substring(0, injectPos);
				String after = str.substring(injectPos);
				// avoid double injection
				if (after.startsWith(tagToInject)) {
					return str;
				} else {
					return before + tagToInject + after;
				}
			} else {
				return str;
			}
		}
	}

	/**
	 * Appends the given encoded string to the html document. If this String contains the
	 * tag-injection position, the tag will be injected and the modified String is returned.
	 *
	 * @param encodedChars
	 *            the encoded string containing the next characters of the document
	 * @return the original data if it did not contain the injection point, or the original string
	 *         with tag inserted at the correct position encoded correctly.
	 */
	public byte[] performInjection(byte[] encodedChars) {
		if (charDecoder == null) {
			setEncoding(DEFAULT_ENCODING);
		}
		if (hasTerminated()) {
			return encodedChars;
		} else {

			int previousLeftoverSize = leftOver.length;

			String decodedStr = decodeWithLeftOver(encodedChars);

			// non decodeable chars were found
			if (decodedStr == null) {
				status = Status.UNPARESABLE;
				src.setLength(0);
				return encodedChars;
			} else {
				int offset = src.length();
				src.append(decodedStr);
				process();
				if (hasInjectionPointBeenFound()) {
					int injectPos = carret.getOffset() - offset;
					String before = decodedStr.substring(0, injectPos);
					String after = decodedStr.substring(injectPos);
					// avoid double injection
					if (after.startsWith(tagToInject)) {
						return encodedChars;
					} else {
						byte[] modifiedData;
						try {

							ByteBuffer bb = charEncoder.encode(CharBuffer.wrap(before + tagToInject + after));
							// make sure not to reflush the bytes of the previous leftover
							bb.position(previousLeftoverSize);
							modifiedData = new byte[bb.limit() - bb.position()];
							bb.get(modifiedData);
						} catch (CharacterCodingException e) {
							// should not happen, as we were previously able to decode the same
							// string (except for the new script tag
							throw new RuntimeException(e);
						}
						return modifiedData;
					}
				} else {
					return encodedChars;
				}
			}

		}
	}

	/**
	 * Decodes the given encoded string, while taking the leftover of the previous write into
	 * account.
	 *
	 * @param data
	 *            the encoded character data
	 * @return the decoded character data
	 */
	private String decodeWithLeftOver(byte[] data) {
		String decodedStr;
		ByteBuffer input;
		if (leftOver.length != 0) {
			input = ByteBuffer.allocate(leftOver.length + data.length);
			input.put(leftOver);
			input.put(data);
			input.position(0);
		} else {
			input = ByteBuffer.wrap(data);
		}
		int maxCharCount = (int) Math.ceil(input.limit() * charDecoder.maxCharsPerByte());
		CharBuffer out = CharBuffer.allocate(maxCharCount);
		CoderResult result = charDecoder.decode(input, out, false);
		if (result.isError()) {
			return null;
		} else {
			int len = out.position();
			out.position(0);
			char[] resultBuffer = new char[len];
			out.get(resultBuffer);
			decodedStr = new String(resultBuffer);
			if (input.remaining() == 0) {
				leftOver = NO_LEFTOVER_CHARACTER_BYTES; // no leftover, everything complete
			} else {
				leftOver = new byte[input.remaining()];
				input.get(leftOver);
			}
		}
		return decodedStr;
	}

	/**
	 * Performs the actual searching on the underlying text buffer.
	 */
	private void process() {

		if (hasTerminated()) {
			return;
		}

		boolean canContinue = !carret.endReached();
		while (canContinue && !hasTerminated()) {
			canContinue = skipAllCommentsAndWhitespaces() && !carret.endReached();
			if (canContinue) {
				// we expect everything except for whitespaces to be a tag
				if (carret.startsWithCheckCase("<")) {
					if (status == Status.INIT) {
						canContinue = handleTopLevelTag() && !carret.endReached();
					} else if (status == Status.HTML_TAG_ENTERED) {
						canContinue = handleHTMLTag() && !carret.endReached();
					}
				} else {
					LOG.debug("Document contained something not in a tag, cannot be HTML. Terminating Parsing."); // NOPMD
					status = Status.UNPARESABLE;
					carret.goTo(src.length());
					canContinue = false;
				}
			}
		}

		if (hasTerminated()) {
			LOG.debug("Parser Termination Status: " + status); // NOPMD
			if ((status == Status.INJECTION_POINT_FOUND) && LOG.isDebugEnabled()) {
				LOG.debug("Injection position: " + carret); // NOPMD
			}
			// clear the buffer to save memory;
			src.delete(0, src.length());
		}

	}

	/**
	 * @return true if the parser has finished (the injection point was found or the document is
	 *         non-html or unparseable)
	 */
	public boolean hasTerminated() {
		return (status == Status.UNPARESABLE) || (status == Status.INJECTION_POINT_FOUND);
	}

	/**
	 * @return true, if an injection point has been found
	 */
	public boolean hasInjectionPointBeenFound() {
		return status == Status.INJECTION_POINT_FOUND;
	}

	/**
	 * Handles tags at the top level. Supported tags are the !DOCTYPE html *anythinghere* tag and
	 * the html tag itself.
	 *
	 * @return true if this tag could be parsed, false if some data is missing at the current point
	 *         of time (buffer the tag until the next write).
	 */
	private boolean handleTopLevelTag() {
		// ignore if it is a comment, otherwise proceed with parsing the opening tag

		Carret end = carret.copy();
		boolean isClosed = end.walkToCharCheckCase('>');

		if (isClosed) {
			// check for special DOCTYPE tag
			if (carret.startsWithIgnoreCase("<!DOCTYPE")) {
				carret.walkAfterMatchIgnoreCase("<!DOCTYPE");
				carret.walkAfterWhitespaces();
				boolean isHTML = carret.startsWithIgnoreCase("html");
				if (isHTML) {
					LOG.trace("DOCTYPE HTML tag detected!");
					carret.goTo(end.getOffset() + 1);
					return true;
				} else {
					LOG.trace("Doctype different from html detected, aborting.");
					carret.goTo(src.length());
					status = Status.UNPARESABLE;
					return false;
				}
			} else {
				// we have a normal tag
				carret.walkAfterCharCheckCase('<');
				carret.walkAfterWhitespaces();
				boolean isImmediatelyClosed = end.get(-1) == '/';
				Carret tagEnd = end.copy();
				// go back before the closing part
				if (isImmediatelyClosed) {
					tagEnd.goN(-1);
				}
				tagEnd.goN(-1);
				tagEnd.walkBackBeforeWhitespaces();
				if (carret.startsWithIgnoreCase("html")) {
					LOG.trace("Detect html tag, parsing contents");
					end.walkAfterCharCheckCase('>');
					carret.goTo(end.getOffset());
					status = Status.HTML_TAG_ENTERED;
					return true;
				} else {
					LOG.trace("Found the following top-level-tag: " + src.substring(carret.getOffset(), tagEnd.getOffset() + 1));
					LOG.trace("aborting as it is non standard-conform.");
					carret.goTo(src.length());
					status = Status.UNPARESABLE;
					return false;
				}
			}
		} else {
			// wait until it is complete, parse next time
			return false;
		}
	}

	/**
	 * handles html-level tags, supported ones are the head and body tag.
	 *
	 * @return true if this tag could be parsed, false if some data is missing at the current point
	 *         of time (buffer the tag until the next write).
	 */
	private boolean handleHTMLTag() {
		// ignore if it is a comment, otherwise proceed with parsing the opening tag
		Carret end = carret.copy();
		boolean isClosed = end.walkToCharCheckCase('>');
		if (isClosed) {

			carret.walkAfterCharCheckCase('<');
			carret.walkAfterWhitespaces();
			boolean isImmediatelyClosed = end.get(-1) == '/';
			Carret tagEnd = end.copy();
			// go back before the closing part
			if (isImmediatelyClosed) {
				tagEnd.goN(-1);
			}
			tagEnd.goN(-1);
			tagEnd.walkBackBeforeWhitespaces();
			if ((carret.startsWithIgnoreCase("head") || carret.startsWithIgnoreCase("body")) && !isImmediatelyClosed) {

				end.walkAfterCharCheckCase('>');

				status = Status.INJECTION_POINT_FOUND;
				carret.goTo(end.getOffset());
				return false;

			} else {
				LOG.trace("Found the following html-level-tag: " + src.substring(carret.getOffset(), tagEnd.getOffset() + 1));
				LOG.trace("Only head or body tags are expected here, aborting");
				carret.goTo(src.length());
				status = Status.UNPARESABLE;
				return false;
			}

		} else {
			// wait until it is complete, parse next time
			return false;
		}
	}

	/**
	 * @return true if continuing is possible, so (a) it was a comment and the comment end was also
	 *         found ot (b) it was not a comment
	 */
	private boolean skipAllCommentsAndWhitespaces() {
		carret.walkAfterWhitespaces();
		while (carret.startsWithCheckCase("<!--")) {
			Carret end = carret.copy();
			boolean endFound = end.walkAfterMatchCheckCase("-->");
			if (endFound) {
				carret.goTo(end.getOffset());
				carret.walkAfterWhitespaces();
			} else {
				return false;
			}
		}
		return true;
	}

}
