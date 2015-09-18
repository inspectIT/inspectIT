package info.novatec.inspectit.cmr.usermanagement;
import org.apache.commons.codec.binary.Hex;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Used for hashing the sensitive data.
 * 
 * @author Andreas Herzog
 *
 */
public abstract class Permutation {
	public static void main(String[] args) throws NoSuchAlgorithmException{
		System.out.println(hashString("myPassword"));
	}
	
	
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
	
	public static String hashString(String password){
		try{
			return new String(Hex.encodeHex(hash(password)));
		}
		catch(NoSuchAlgorithmException nsaEx){
			//Maybe Log it
			return "";
		}
	}
}
