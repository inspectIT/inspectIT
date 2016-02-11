package info.novatec.inspectit.rcp.storage.util;

import java.util.StringTokenizer;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

/**
 * {@link MultipartEntityUtil} provides simple methods for multipart entities.
 * 
 * @author Ivan Senic
 * 
 */
public final class MultipartEntityUtil {

	/**
	 * Key to find the boundary in the Content-Type of the HTTP response.
	 */
	private static final String BOUNDARY_KEY = "boundary=";

	/**
	 * Private constructor.
	 */
	private MultipartEntityUtil() {
	}

	/**
	 * Checks if the {@link HttpEntity} holds the multipart/byterange HTTP response.
	 * 
	 * @param httpEntity
	 *            {@link HttpEntity} that holds a response.
	 * @return True if it has the "multipart" marker in the response Content-Type header.
	 */
	public static boolean isMultipart(HttpEntity httpEntity) {
		return httpEntity.getContentType().getValue().indexOf("multipart") != -1;
	}

	/**
	 * Extracts the string that denotes the boundary of the multipart response from Content-Type
	 * header.
	 * 
	 * @param httpEntity
	 *            {@link HttpEntity} that holds a response.
	 * @return Boundary word, or null if the Content-Type header does not define it.
	 */
	public static String getBoundary(HttpEntity httpEntity) {
		Header contentTypeHeader = httpEntity.getContentType();
		if (contentTypeHeader == null) {
			return null;
		}
		StringTokenizer tokenizer = new StringTokenizer(contentTypeHeader.getValue(), ";");
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken().trim();
			int boundaryIndex = token.indexOf(BOUNDARY_KEY);
			if (boundaryIndex != -1) {
				String boundaryString = token.substring(boundaryIndex + BOUNDARY_KEY.length());
				return boundaryString;
			}
		}
		return null;
	}

}
