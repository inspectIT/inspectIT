package rocks.inspectit.agent.java.eum.html;

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
 * Decorates the {@link StreamedHTMLScriptInjector} with the functionality to also perform injection
 * on binary data as long as the character encoding is known. Simultaneously, this injector also
 * accepts already decoded Character data just as the {@link StreamedHTMLScriptInjector}.
 *
 * @author Jonas Kunz
 *
 */
public class DecodingHtmlScriptInjector extends StreamedHTMLScriptInjector {

	/**
	 * Logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(DecodingHtmlScriptInjector.class);

	/**
	 * Constant used for code readability.
	 */
	private static final byte[] NO_LEFTOVER_CHARACTER_BYTES = {};

	/**
	 * The charset name of the encoding of the binary data.
	 */
	private String charSet;

	/**
	 * The decoder used for decoding encoded character data.
	 */
	private CharsetDecoder charDecoder;

	/**
	 * The encoder used for encoding character data.
	 */
	private CharsetEncoder charEncoder;

	/**
	 * Buffer for storing encoded characters in. This buffer is reused as long as the injector is
	 * active to avoid reallocating a buffer for each call.
	 */
	private ByteBuffer encodeBuffer;

	/**
	 * Buffer for storing decoded characters in. This buffer is reused as long as the injector is
	 * active to avoid reallocating a buffer for each call.
	 */
	private CharBuffer decodeBuffer;

	/**
	 * The left-over of previous writes of incomplete encoded characters (e.g. only the first byte
	 * of an encoded two byte character).
	 */
	private byte[] leftOver = NO_LEFTOVER_CHARACTER_BYTES;

	/**
	 * Creates and initializes a new injector.
	 *
	 * @param tagToInject
	 *            the tag which this injector should try to inject.
	 * @param defaultCharset
	 *            the default encoding to assume if no other is specified
	 */
	public DecodingHtmlScriptInjector(String tagToInject, String defaultCharset) {
		super(tagToInject);
		charSet = defaultCharset;
	}

	/**
	 * Changes the encoding of the accepted binary data. This method may only be called if no binary
	 * data has been passed to this injector yet!
	 *
	 * @param charsetName
	 *            the name of the charset, accepted by {@link Charset#forName(String)}.
	 */
	public void setCharacterEncoding(String charsetName) {
		if (!codersInitialized()) {
			charSet = charsetName;
		} else {
			throw new IllegalStateException("Decoding has already begun!");
		}
	}

	/**
	 * Decodes the given encoded character data and tries to inject the script tag into it.
	 *
	 * @param encodedHtmlData
	 *            the character encoded with the previously specified encoding
	 * @return null, if no injection was performed. Otherwise, a copy of the input encoded data with
	 *         the script tag inserted.
	 */
	public byte[] performInjection(byte[] encodedHtmlData) {
		return performInjection(encodedHtmlData, 0, encodedHtmlData.length);
	}

	/**
	 * Decodes the given encoded character data and tries to inject the script tag into it.
	 *
	 * @param encodedHtmlData
	 *            the character encoded with the previously specified encoding
	 * @param offset
	 *            the offset of the data within encodedHtmlData in bytes
	 * @param len
	 *            the length of the data within encodedHtmlData in bytes
	 * @return @return null, if no injection was performed. Otherwise, a copy of the input encoded
	 *         data (starting at the given offset with the given length) with the script tag
	 *         inserted.
	 */
	public byte[] performInjection(byte[] encodedHtmlData, int offset, int len) {
		if (hasTerminated()) {
			return null;
		}

		// remember the previous left-over so we can remove it again if we perform an injection
		int previousLeftoverSize = leftOver.length;
		CharSequence decodedStr = decodeWithLeftOver(encodedHtmlData, offset, len);

		// non decodeable chars were found
		if (decodedStr == null) {
			abortInjectionPointSearch();
			return null;
		}

		// perform the string-based injection
		CharSequence injectionResult = super.performInjection(decodedStr);

		if (injectionResult != null) {

			try {
				ByteBuffer bb = charEncoder.encode(CharBuffer.wrap(injectionResult));
				// make sure not to reflush the bytes of the previous leftover
				bb.position(previousLeftoverSize);
				byte[] modifiedData = new byte[bb.limit() - bb.position()];
				bb.get(modifiedData);
				return modifiedData;
			} catch (CharacterCodingException e) {
				// should not happen, as we were previously able to decode the same
				// string (except for the new script tag)
				throw new RuntimeException(e);
			}

		} else {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String performInjection(CharSequence htmlData) {
		if (hasTerminated()) {
			return null;
		}
		if (leftOver == NO_LEFTOVER_CHARACTER_BYTES) {
			return super.performInjection(htmlData);
		} else {
			// we have some undecodeable bytes left: abort injection
			abortInjectionPointSearch();
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void abortInjectionPointSearch() {
		super.abortInjectionPointSearch();
		leftOver = NO_LEFTOVER_CHARACTER_BYTES;
		encodeBuffer = null; // NOPMD
		decodeBuffer = null; // NOPMD
	}

	/**
	 * Tries to fetch the decoders for the charset if this was not done yet.
	 */
	private void fetchCoders() {
		if (!codersInitialized()) {
			try {
				Charset chars = Charset.forName(charSet);
				charEncoder = chars.newEncoder();
				charDecoder = chars.newDecoder();
			} catch (Exception e) {
				LOG.error("Error fetching decoder for charset " + charSet, e);
			}
		}
	}

	/**
	 * Decodes the given binary data using hte configured decoder. Takes left-over bytes of the
	 * previous decode operation stored in {@link #leftOver} into account and updates it for new
	 * left-over bytes.
	 *
	 * @param data
	 *            the data to decode
	 * @param offset
	 *            the offset within data to start at in bytes
	 * @param length
	 *            the number of bytes to decode of data
	 *
	 * @return the decoded characters, if successful, null otherwise.
	 */
	private CharSequence decodeWithLeftOver(byte[] data, int offset, int length) {
		// Fetch den- and encoder for the configured character set
		fetchCoders();
		if (!codersInitialized()) {
			return null; // error, cannot decode as the charset was not supported.
		}

		CharSequence decodedStr;
		ByteBuffer input;

		if (leftOver.length != 0) {
			// we have some bytes remaining form the previous data, add them to the decoding buffer
			int totalSize = leftOver.length + length;
			if ((encodeBuffer == null) || (encodeBuffer.capacity() < totalSize)) {
				encodeBuffer = ByteBuffer.allocate(totalSize);
			} else {
				encodeBuffer.clear();
			}
			encodeBuffer.put(leftOver);
			encodeBuffer.put(data, offset, length);
			encodeBuffer.position(0);
			input = encodeBuffer;
		} else {
			// no bytes remaining from the previous input, simply wrap the newest input
			input = ByteBuffer.wrap(data, offset, length);
		}

		// resize the result buffer if required
		int maxCharCount = (int) Math.ceil(input.limit() * charDecoder.maxCharsPerByte());
		if ((decodeBuffer == null) || (decodeBuffer.capacity() < maxCharCount)) {
			decodeBuffer = CharBuffer.allocate(maxCharCount);
		} else {
			decodeBuffer.clear();
		}

		CoderResult result = charDecoder.decode(input, decodeBuffer, false);
		if (result.isError()) {
			return null; // the byte array contained sequences which do not represent valid
			// characters
		} else {
			int len = decodeBuffer.position();
			decodeBuffer.position(0);
			decodedStr = decodeBuffer.subSequence(0, len);
			if (input.remaining() == 0) {
				leftOver = NO_LEFTOVER_CHARACTER_BYTES; // no leftover, everything complete
			} else {
				leftOver = new byte[input.remaining()]; // some leftover, store it
				input.get(leftOver);
			}
		}
		return decodedStr;
	}

	/**
	 * @return true, if the decoders have been initialized and the decoding process has laready
	 *         begun.
	 */
	private boolean codersInitialized() {
		return (charEncoder != null) && (charDecoder != null);
	}

}
