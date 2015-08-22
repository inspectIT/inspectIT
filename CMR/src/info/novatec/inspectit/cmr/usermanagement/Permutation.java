package info.novatec.inspectit.cmr.usermanagement;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Used for hashing the sensitive data.
 * 
 * @author Andreas Herzog
 *
 */
public abstract class Permutation {
	/**
	 * Hashes a passed String using SHA-256 algorithm.
	 * 
	 * @param password as String
	 * @return byte[] of the password
	 * @throws NoSuchAlgorithmException should not happen.
	 */
	public static byte[] hash(String password) throws NoSuchAlgorithmException {
	    MessageDigest sha256 = MessageDigest.getInstance("SHA-256");        
	    byte[] passBytes = password.getBytes();
	    byte[] hash = sha256.digest(passBytes);
	    return hash;
	}
}
