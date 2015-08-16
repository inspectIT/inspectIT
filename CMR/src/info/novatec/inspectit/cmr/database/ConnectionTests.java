package info.novatec.inspectit.cmr.database;

import java.util.List;
import java.util.ArrayList;

import info.novatec.inspectit.cmr.usermanagement.Privilege;
import info.novatec.inspectit.cmr.usermanagement.Role;
import info.novatec.inspectit.cmr.usermanagement.User;


/*!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * 
 * The database needs to be deleted after each run of this "test", 
 * otherwise exception because of the attempt to write duplicate values 
 * into the UNIQUE fields.
 * 
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 */

/**
 * Class for testing some of the functionality.
 * @author Joshua Hartmann
 *
 */
public final class ConnectionTests {
	/**
	 * Constructor for the class, testing only.
	 */
	private ConnectionTests() {

	}

	/**
	 * 
	 * @param args The arguments
	 * @throws Exception SQLException
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("Testing the database");

		UserDataHandler udh = new UserDataHandler();

		Privilege write = new Privilege(1, "write", "");
		Privilege read = new Privilege(2, "read", "");
		Privilege create = new Privilege(3, "create", "");
		Privilege delete = new Privilege(4, "delete", "");

		List<Privilege> privilegesges = new ArrayList<Privilege>();
		privilegesges.add(write);
		privilegesges.add(read);
		privilegesges.add(create);
		privilegesges.add(delete);		

		Role role = new Role(1, "PowerUser", privilegesges);		

		udh.inserNewPrivilege(write);
		udh.inserNewPrivilege(read);
		udh.inserNewPrivilege(create);
		udh.inserNewPrivilege(delete);		
		udh.insertNewRole(role);

		udh.insertNewUser("Bob", "Bob is cool", "bob@mail.com", role);

		User queriedUser = udh.getUserFromName("Bob");

		if (null != queriedUser) { //Bob was found!
			System.out.println(queriedUser.toString());
		} else {
			System.out.println("Something went wrong");
		}
	}
}
