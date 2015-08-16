/**
 * 
 */
package info.novatec.inspectit.cmr.database;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import info.novatec.inspectit.cmr.usermanagement.User;
import info.novatec.inspectit.cmr.usermanagement.Role;
import info.novatec.inspectit.cmr.usermanagement.Privilege;


/*!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * 
 * Database will be saved in ~/usermanagement_exp or as specified.
 * 
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 */

/**
 * Class to handle different user data, parts should only be accessible with according privileges.
 * @author Joshua Hartmann
 *
 */
public class UserDataHandler {
	/**
	 * The location of the database on the system.
	 */
	private String dbLocation = "jdbc:h2:~/usermanagement_exp"; //normally is given in default.xml
	/**
	 * The user of the in databseLocation given database.
	 */
	private String dbUser = "inspectit"; //normally is given in default.xml
	/**
	 * The password of the in databseLocation given database.
	 */
	private String dbPassword = "inspectit"; //normally is given in default.xml
	/**
	 * Representing the connection to the specified database.
	 */
	private Connection dbConnection = null;

	/**
	 * The name for the table in which the users are stored.
	 */
	private final String userTableName = "USERS";
	/**
	 * The name for the table in which the roles are stored.
	 */
	private final String roleTableName = "ROLES";
	/**
	 * The name for the table in which the privileges are stored.
	 */
	private final String privilegeTableName = "PRIVILEGES";

	/**
	 * The name for the table in which the privileges assigned to one role are stored.
	 */
	private final String rolePrivilegeMapTableName = "ROLES_PRIVILEGES_MAP";

	/**
	 * Constructor using unchanged database credentials.
	 * Should be used, when the database credentials are parsed out of the default.xml
	 * @throws Exception SQLException
	 */
	public UserDataHandler() throws Exception {
		Class.forName("org.h2.Driver");
		dbConnection = DriverManager.getConnection(this.dbLocation, this.dbUser, this.dbPassword);

		//TODO fix the lengths of the fields
		String queryCreateUserTable = "CREATE TABLE IF NOT EXISTS " + userTableName + "(Id int NOT NULL AUTO_INCREMENT UNIQUE, Name varchar(255) NOT NULL UNIQUE, Password varchar(100) NOT NULL, Email varchar(255) NOT NULL, Role int NOT NULL,  PRIMARY KEY(Id));";
		String queryCreateRoleTable = "CREATE TABLE IF NOT EXISTS " + roleTableName + "(Id int NOT NULL AUTO_INCREMENT UNIQUE, Title varchar(255) NOT NULL UNIQUE, PRIMARY KEY(Id));";
		String queryCreatePrivilegeTable = "CREATE TABLE IF NOT EXISTS " + privilegeTableName + "(Id int NOT NULL UNIQUE, Title varchar(255) NOT NULL UNIQUE, Description varchar(512), PRIMARY KEY(Id));";
		String queryCreateRolePrivilegeMap = "CREATE TABLE IF NOT EXISTS " + rolePrivilegeMapTableName + "(RoleId int NOT NULL, PrivilegeId int NOT NULL);";

		Statement statement = dbConnection.createStatement();

		statement.execute(queryCreateUserTable);
		statement.execute(queryCreateRoleTable);
		statement.execute(queryCreatePrivilegeTable);
		statement.execute(queryCreateRolePrivilegeMap);

		statement.close();
	}

	/**
	 * The constructor for UserDataHandler [should not be used later on, then the credentials
	 * are retrieved out of the configuration.
	 * Creates the used tables if needed
	 * @param databaseLocation The location the database should be stored.
	 * @param databaseUser The user for the given database.
	 * @param databasePassword The password for the given database.
	 * @throws Exception SQLException
	 */
	public UserDataHandler(String databaseLocation, String databaseUser, String databasePassword)  throws Exception {
		super();
		this.dbLocation = databaseLocation;
		this.dbUser = databaseUser;
		this.dbPassword = databasePassword;

		Class.forName("org.h2.Driver");
		dbConnection = DriverManager.getConnection(this.dbLocation, this.dbUser, this.dbPassword);

		//TODO fix the lengths of the fields
		String queryCreateUserTable = "CREATE TABLE IF NOT EXISTS " + userTableName + "(Id int NOT NULL AUTO_INCREMENT UNIQUE, Name varchar(255) NOT NULL UNIQUE, Password varchar(100) NOT NULL, Email varchar(255) NOT NULL, Role int NOT NULL,  PRIMARY KEY(Id));";
		String queryCreateRoleTable = "CREATE TABLE IF NOT EXISTS " + roleTableName + "(Id int NOT NULL AUTO_INCREMENT UNIQUE, Title varchar(255) NOT NULL UNIQUE, PRIMARY KEY(Id));";
		String queryCreatePrivilegeTable = "CREATE TABLE IF NOT EXISTS " + privilegeTableName + "(Id int NOT NULL UNIQUE, Title varchar(255) NOT NULL UNIQUE, Description varchar(512), PRIMARY KEY(Id));";
		String queryCreateRolePrivilegeMap = "CREATE TABLE IF NOT EXISTS " + rolePrivilegeMapTableName + "(RoleId int NOT NULL, PrivilegeId int NOT NULL);";

		Statement statement = dbConnection.createStatement();

		statement.execute(queryCreateUserTable);
		statement.execute(queryCreateRoleTable);
		statement.execute(queryCreatePrivilegeTable);
		statement.execute(queryCreateRolePrivilegeMap);

		statement.close();
	}


	/**
	 * Queries the database for the desired user specified by the name.
	 * @param name The name of the desired user.
	 * @return The user to the given name, null if user not found.
	 * @throws Exception SQLException in case of errors in db Connection.
	 */
	public User getUserFromName(String name) throws Exception {
		User user = null;

		Statement statement = dbConnection.createStatement();

		String querySearchUser = "SELECT * FROM " + userTableName + " WHERE NAME = '" + name + "';";

		ResultSet rsSearchUser = statement.executeQuery(querySearchUser);

		while (!rsSearchUser.isClosed() && rsSearchUser.next()) {
			//Get properties of the selected user
			int userId = rsSearchUser.getInt("Id");
			String userName = rsSearchUser.getString("Name");
			String userPassword = rsSearchUser.getString("Password");
			String userEmail = rsSearchUser.getString("Email");
			int userRoleId = rsSearchUser.getInt("Role");

			//get the corresponding role, including it's privileges
			String queryGetRolePrivileges = "SELECT * FROM " + privilegeTableName + " WHERE ID IN (SELECT PRIVILEGEID FROM " + rolePrivilegeMapTableName + " WHERE roleID = '" + userRoleId + "');";
			ResultSet rsGetRolePrivileges = statement.executeQuery(queryGetRolePrivileges);

			List<Privilege> privileges = new ArrayList<Privilege>(); 
			//Adds each privilege to the role
			while (rsGetRolePrivileges.next()) {
				int privilegeId = rsGetRolePrivileges.getInt("Id");
				String privilegeTitle = rsGetRolePrivileges.getString("Title");
				String privilegeDescription = rsGetRolePrivileges.getString("Description");				
				Privilege privilege = new Privilege(privilegeId, privilegeTitle, privilegeDescription);				
				privileges.add(privilege);
			}

			//get the role title
			String queryGetRoleTitle = "SELECT Title FROM " + roleTableName + " WHERE ID = '" + userRoleId + "';";			
			ResultSet rsGetRoleTitle = statement.executeQuery(queryGetRoleTitle);			
			String roleTitle = "n/a";			
			while (rsGetRoleTitle.next()) {
				roleTitle = rsGetRoleTitle.getString("Title");
			}			
			Role userRole = new Role(userRoleId, roleTitle, privileges);
			//finally, create the user object
			user = new User(userName, userPassword, userEmail, userId, userRole);

			//close the result sets in this loop
			//rsGetRolePrivileges.close();
			//rsGetRoleTitle.close();
		}

		//close result set and statement of this method
		rsSearchUser.close();
		statement.close();

		return user;
	}

	/**
	 * Searches in the database for the role with the given id.
	 * @param id The id of the role.
	 * @return The desired role or null if not found
	 */
	public Role getRoleById(int id) {
		//TODO implement
		return null;
	}

	/**
	 * Searches in the database for the role with the given title.
	 * @param title The title of the role.
	 * @return The desired role or null if not found
	 */
	public Role getRoleByTitle(String title) {
		//TODO implement
		return null;
	}

	/**
	 * Searches in the database for the Privilege with the given id.
	 * @param id The id of the Privilege.
	 * @return The desired Privilege or null if not found
	 */
	public Privilege getPrivilegeById(int id) {
		//TODO implement
		return null;
	}

	/**
	 * Searches in the database for the Privilege with the given title.
	 * @param title The title of the Privilege.
	 * @return The desired Privilege or null if not found
	 */
	public Privilege getPrivilegeByTitle(String title) {
		//TODO implement
		return null;
	}



	/**
	 * Inserts a new user into the database.
	 * @param name The name of the new user
	 * @param password The password (plain text, hashed in insertion process) of the new user
	 * @param email The email-address of the new user
	 * @param role The role the user should be attached to
	 * @return True if successful, false if not
	 * @throws Exception SQLException
	 */
	public boolean insertNewUser(String name, String password, String email, Role role) throws Exception  {
		//TODO [Maybe] add error codes for unsuccessful insertion attempts.

		//TODO hash the password		
		String hashedPassword = password;

		String queryInsertUser = "INSERT INTO " + userTableName + " (NAME, PASSWORD, EMAIL, ROLE)  VALUES ( '" + name + "', '" + hashedPassword + "', '" + email + "', " + role.getId() + ");";

		Statement statement = dbConnection.createStatement();

		int rowCountInsertUser = statement.executeUpdate(queryInsertUser);

		statement.close();

		return rowCountInsertUser > 0;
	}

	/**
	 * Inserts a new role into the database.
	 * @param title The title of the new role
	 * @param privileges The set of privileges the role should get
	 * @return True if successful, false if not
	 * @throws Exception SQLException
	 */
	public boolean insertNewRole(String title, List<Privilege> privileges) throws Exception {
		//TODO [Maybe] add error codes for unsuccessful insertion attempts.

		String queryInsertRole = "INSERT INTO " + roleTableName + " (TITLE)  VALUES ('" + title + "');";

		//insert the role
		Statement statement = dbConnection.createStatement();		
		int rowCountInsertRole = statement.executeUpdate(queryInsertRole);

		//retrieve the id of the inserted role
		String queryInsertedId = "SELECT SCOPE_IDENTITY()";
		ResultSet rsInsertedId = statement.executeQuery(queryInsertedId);
		int roleId = -1;

		while (rsInsertedId.next()) {
			roleId = rsInsertedId.getInt(1);
		}

		boolean allRolePrivilegeMappingsInserted = true;
		//insert all the connections of privileges		
		for (Privilege privilege : privileges) {
			String queryInsertRolePrivilegeMapping = "INSERT INTO " + rolePrivilegeMapTableName + " (RoleId, PrivilegeId)  VALUES (" + roleId + ", " + privilege.getId() + ");";
			int rowCountInsertRolePrivilegeMapping = statement.executeUpdate(queryInsertRolePrivilegeMapping);
			//check if all were inserted
			allRolePrivilegeMappingsInserted = allRolePrivilegeMappingsInserted && (rowCountInsertRolePrivilegeMapping > 0);

		}
		//close statement and result sets
		statement.close();
		rsInsertedId.close();

		return rowCountInsertRole > 0 && allRolePrivilegeMappingsInserted;
	}

	/**
	 * Inserts a new role into the database.
	 * @param role The role object to be inserted.
	 * @return True if successful, false if not
	 * @throws Exception SQLException
	 */
	public boolean insertNewRole(Role role) throws Exception {
		//TODO [Maybe] add error codes for unsuccessful insertion attempts.		
		return insertNewRole(role.getTitle(), role.getPrivileges());
	}


	/**
	 * Inserts a new privilege into the database.
	 * @param id The [UNIQUE!] identifier characterizing the privilege.
	 * @param title A short title for the privilege (should be unique, easier to differ the privileges)
	 * @param description A more detailed description of the privilege and its functional scope
	 * @return True if successful, false if not
	 * @throws Exception SQLException
	 */
	public boolean inserNewPrivilege(int id, String title, String description) throws Exception {
		//TODO [Maybe] add error codes for unsuccessful insertion attempts.

		String queryInsertPrivilege = "INSERT INTO " + privilegeTableName + " (ID, TITLE, DESCRIPTION)  VALUES ('" + id + "', '" + title + "', '" + description + "');";

		//insert the privilege
		Statement statement = dbConnection.createStatement();		
		int rowCountInsertPrivilege = statement.executeUpdate(queryInsertPrivilege);

		//close statement and result sets
		statement.close();
		return rowCountInsertPrivilege > 0;		
	}

	/**
	 * Inserts a new privilege into the database.
	 * @param privilege The privilege to be inserted.
	 * @return True if successful, false if not
	 * @throws Exception SQLException
	 */
	public boolean inserNewPrivilege(Privilege privilege) throws Exception {
		//TODO [Maybe] add error codes for unsuccessful insertion attempts.

		return inserNewPrivilege(privilege.getId(), privilege.getTitle(), privilege.getDescription());
	}

	/**
	 * Changes the role of the given user to the given role.
	 * @param user The user to be modified
	 * @param newRole The role to be applied
	 */
	public void changeRoleOfUser(User user, Role newRole) {
		//TODO switch the role of the given user
	}

	/**
	 * Adds a Privilege to a existing Role.
	 * @param role The role which should get another privilege
	 * @param privilege The new privilege
	 */
	public void addPrivilegeToRole(Role role, Privilege privilege) {
		//TODO add privilege to the given role
	}

	/**
	 * Adds a set of Privileges to a existing Role.
	 * @param role The role which should get another privilege
	 * @param privileges The set of new privilege
	 */
	public void addPrivilegeToRole(Role role, List<Privilege> privileges) {
		//TODO add privilege to the given role
	}

	/**
	 * Retrieve a list of all the current privileges.
	 * @return A list of all privileges in the database.
	 */
	public List<Privilege> getAllPrivileges() {
		//TODO return a list of all the current privileges
		return null;
	}

	/**
	 * Retrieve a list of all the current roles.
	 * @return A list of all roles in the database.
	 */
	public List<Privilege> getAllRoles() {
		//TODO return a list of all the current roles
		return null;
	}

	/**
	 * Closes the databseConnection if necessary.
	 * {@inheritDoc}
	 */
	protected void finalize() throws Throwable {
		if (null != dbConnection) {
			dbConnection.close();
		}

		super.finalize();
	}
}
