package info.novatec.inspectit.cmr.usermanagement;


//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.ResultSet;
//import java.sql.Statement;
//import java.util.List;


/*!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * 
 * Database will be saved in ~/usermanagement_exp or as specified.
 * 
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 */

/**
 * NOT IN USE
 * Class to handle different user data, parts should only be accessible with according permissions.
 * @author Joshua Hartmann
 *
 */

public class UserDataHandler {
//	
//	/**
//	 * The location of the database on the system.
//	 */
//	private String dbLocation = "jdbc:h2:~/usermanagement_exp"; //normally is given in default.xml
//	/**
//	 * The user of the in databseLocation given database.
//	 */
//	private String dbUser = "inspectit"; //normally is given in default.xml
//	/**
//	 * The password of the in databseLocation given database.
//	 */
//	private String dbPassword = "inspectit"; //normally is given in default.xml
//	/**
//	 * Representing the connection to the specified database.
//	 */
//	private Connection dbConnection = null;
//
//	/**
//	 * The name for the table in which the users are stored.
//	 */
//	private final String userTableName = "USERS";
//	/**
//	 * The name for the table in which the roles are stored.
//	 */
//	private final String roleTableName = "ROLES";
//	/**
//	 * The name for the table in which the permissions are stored.
//	 */
//	private final String permissionTableName = "PRIVILEGES";
//
//	/**
//	 * The name for the table in which the permissions assigned to one role are stored.
//	 */
//	private final String rolePermissionMapTableName = "ROLES_PRIVILEGES_MAP";
//
//	/**
//	 * Constructor using unchanged database credentials.
//	 * Should be used, when the database credentials are parsed out of the default.xml
//	 * @throws Exception SQLException
//	 */
//	public UserDataHandler() throws Exception {
//		Class.forName("org.h2.Driver");
//		dbConnection = DriverManager.getConnection(this.dbLocation, this.dbUser, this.dbPassword);
//
//		//TODO fix the lengths of the fields
//		String queryCreateUserTable = "CREATE TABLE IF NOT EXISTS " + userTableName + "(Id int NOT NULL AUTO_INCREMENT UNIQUE, Name varchar(255) NOT NULL UNIQUE, Password varchar(100) NOT NULL, Email varchar(255) NOT NULL, Role int NOT NULL,  PRIMARY KEY(Id));";
//		String queryCreateRoleTable = "CREATE TABLE IF NOT EXISTS " + roleTableName + "(Id int NOT NULL AUTO_INCREMENT UNIQUE, Title varchar(255) NOT NULL UNIQUE, PRIMARY KEY(Id));";
//		String queryCreatePermissionTable = "CREATE TABLE IF NOT EXISTS " + permissionTableName + "(Id int NOT NULL UNIQUE, Title varchar(255) NOT NULL UNIQUE, Description varchar(512), PRIMARY KEY(Id));";
//		String queryCreateRolePermissionMap = "CREATE TABLE IF NOT EXISTS " + rolePermissionMapTableName + "(RoleId int NOT NULL, PermissionId int NOT NULL);";
//
//		Statement statement = dbConnection.createStatement();
//
//		statement.execute(queryCreateUserTable);
//		statement.execute(queryCreateRoleTable);
//		statement.execute(queryCreatePermissionTable);
//		statement.execute(queryCreateRolePermissionMap);
//
//		statement.close();
//	}
//
//	/**
//	 * The constructor for UserDataHandler [should not be used later on, then the credentials
//	 * are retrieved out of the configuration.
//	 * Creates the used tables if needed
//	 * @param databaseLocation The location the database should be stored.
//	 * @param databaseUser The user for the given database.
//	 * @param databasePassword The password for the given database.
//	 * @throws Exception SQLException
//	 */
//	public UserDataHandler(String databaseLocation, String databaseUser, String databasePassword)  throws Exception {
//		super();
//		this.dbLocation = databaseLocation;
//		this.dbUser = databaseUser;
//		this.dbPassword = databasePassword;
//
//		Class.forName("org.h2.Driver");
//		dbConnection = DriverManager.getConnection(this.dbLocation, this.dbUser, this.dbPassword);
//
//		//TODO fix the lengths of the fields
//		String queryCreateUserTable = "CREATE TABLE IF NOT EXISTS " + userTableName + "(Id int NOT NULL AUTO_INCREMENT UNIQUE, Name varchar(255) NOT NULL UNIQUE, Password varchar(100) NOT NULL, Email varchar(255) NOT NULL, Role int NOT NULL,  PRIMARY KEY(Id));";
//		String queryCreateRoleTable = "CREATE TABLE IF NOT EXISTS " + roleTableName + "(Id int NOT NULL AUTO_INCREMENT UNIQUE, Title varchar(255) NOT NULL UNIQUE, PRIMARY KEY(Id));";
//		String queryCreatePermissionTable = "CREATE TABLE IF NOT EXISTS " + permissionTableName + "(Id int NOT NULL UNIQUE, Title varchar(255) NOT NULL UNIQUE, Description varchar(512), PRIMARY KEY(Id));";
//		String queryCreateRolePermissionMap = "CREATE TABLE IF NOT EXISTS " + rolePermissionMapTableName + "(RoleId int NOT NULL, PermissionId int NOT NULL);";
//
//		Statement statement = dbConnection.createStatement();
//
//		statement.execute(queryCreateUserTable);
//		statement.execute(queryCreateRoleTable);
//		statement.execute(queryCreatePermissionTable);
//		statement.execute(queryCreateRolePermissionMap);
//
//		statement.close();
//	}
//
//
//	/**
//	 * Queries the database for the desired user specified by the name.
//	 * @param name The name of the desired user.
//	 * @return The user to the given name, null if user not found.
//	 * @throws Exception SQLException in case of errors in db Connection.
//	 */
//	public User getUserFromName(String name) throws Exception {
//		User user = null;
//
//		Statement statement = dbConnection.createStatement();
//
//		String querySearchUser = "SELECT * FROM " + userTableName + " WHERE NAME = '" + name + "';";
//
//		ResultSet rsSearchUser = statement.executeQuery(querySearchUser);
//
//		while (!rsSearchUser.isClosed() && rsSearchUser.next()) {
//			//Get properties of the selected user
//			int userId = rsSearchUser.getInt("Id");
//			String userName = rsSearchUser.getString("Name");
//			String userPassword = rsSearchUser.getString("Password");
//			String userEmail = rsSearchUser.getString("Email");
//			int userRoleId = rsSearchUser.getInt("Role");
//			
//			/*
//			//get the corresponding role, including it's permissions
//			String queryGetRolePermissions = "SELECT * FROM " + permissionTableName + " WHERE ID IN (SELECT PRIVILEGEID FROM " + rolePermissionMapTableName + " WHERE roleID = '" + userRoleId + "');";
//			ResultSet rsGetRolePermissions = statement.executeQuery(queryGetRolePermissions);
//			
//			Set<Permission> permissions = Collections.emptySet(); 
//			//Adds each permission to the role
//			while (rsGetRolePermissions.next()) {
//				int permissionId = rsGetRolePermissions.getInt("Id");
//				String permissionTitle = rsGetRolePermissions.getString("Title");
//				String permissionDescription = rsGetRolePermissions.getString("Description");				
//				Permission permission = new Permission(permissionId, permissionTitle, permissionDescription);				
//				permissions.add(permission);
//			}
//
//			//get the role title
//			String queryGetRoleTitle = "SELECT Title FROM " + roleTableName + " WHERE ID = '" + userRoleId + "';";			
//			ResultSet rsGetRoleTitle = statement.executeQuery(queryGetRoleTitle);			
//			String roleTitle = "n/a";			
//			while (rsGetRoleTitle.next()) {
//				roleTitle = rsGetRoleTitle.getString("Title");
//			}			
//			Role userRole = new Role(userRoleId, roleTitle, permissions);
//			//finally, create the user object
//			 * 
//			 */
//			
//			user = new User(userName, userPassword, userEmail, userRoleId);
//
//			//close the result sets in this loop
//			//rsGetRolePermissions.close();
//			//rsGetRoleTitle.close();
//		}
//
//		//close result set and statement of this method
//		rsSearchUser.close();
//		statement.close();
//
//		return user;
//	}
//
//	/**
//	 * Searches in the database for the role with the given id.
//	 * @param id The id of the role.
//	 * @return The desired role or null if not found
//	 */
//	public Role getRoleById(int id) {
//		//TODO implement
//		return null;
//	}
//
//	/**
//	 * Searches in the database for the role with the given title.
//	 * @param title The title of the role.
//	 * @return The desired role or null if not found
//	 */
//	public Role getRoleByTitle(String title) {
//		//TODO implement
//		return null;
//	}
//
//	/**
//	 * Searches in the database for the Permission with the given id.
//	 * @param id The id of the Permission.
//	 * @return The desired Permission or null if not found
//	 */
//	public Permission getPermissionById(int id) {
//		//TODO implement
//		return null;
//	}
//
//	/**
//	 * Searches in the database for the Permission with the given title.
//	 * @param title The title of the Permission.
//	 * @return The desired Permission or null if not found
//	 */
//	public Permission getPermissionByTitle(String title) {
//		//TODO implement
//		return null;
//	}
//
//
//
//	/**
//	 * Inserts a new user into the database.
//	 * @param name The name of the new user
//	 * @param password The password (plain text, hashed in insertion process) of the new user
//	 * @param email The email-address of the new user
//	 * @param role The role the user should be attached to
//	 * @return True if successful, false if not
//	 * @throws Exception SQLException
//	 */
//	public boolean insertNewUser(String name, String password, String email, Role role) throws Exception  {
//		//TODO [Maybe] add error codes for unsuccessful insertion attempts.
//
//		//TODO hash the password		
//		String hashedPassword = password;
//
//		String queryInsertUser = "INSERT INTO " + userTableName + " (NAME, PASSWORD, EMAIL, ROLE)  VALUES ( '" + name + "', '" + hashedPassword + "', '" + email + "', " + role.getId() + ");";
//
//		Statement statement = dbConnection.createStatement();
//
//		int rowCountInsertUser = statement.executeUpdate(queryInsertUser);
//
//		statement.close();
//
//		return rowCountInsertUser > 0;
//	}
//
//	/**
//	 * Inserts a new role into the database.
//	 * @param title The title of the new role
//	 * @param permissions The set of permissions the role should get
//	 * @return True if successful, false if not
//	 * @throws Exception SQLException
//	 */
//	public boolean insertNewRole(String title, List<Permission> permissions) throws Exception {
//		//TODO [Maybe] add error codes for unsuccessful insertion attempts.
//
//		String queryInsertRole = "INSERT INTO " + roleTableName + " (TITLE)  VALUES ('" + title + "');";
//
//		//insert the role
//		Statement statement = dbConnection.createStatement();		
//		int rowCountInsertRole = statement.executeUpdate(queryInsertRole);
//
//		//retrieve the id of the inserted role
//		String queryInsertedId = "SELECT SCOPE_IDENTITY()";
//		ResultSet rsInsertedId = statement.executeQuery(queryInsertedId);
//		int roleId = -1;
//
//		while (rsInsertedId.next()) {
//			roleId = rsInsertedId.getInt(1);
//		}
//
//		boolean allRolePermissionMappingsInserted = true;
//		//insert all the connections of permissions		
//		for (Permission permission : permissions) {
//			String queryInsertRolePermissionMapping = "INSERT INTO " + rolePermissionMapTableName + " (RoleId, PermissionId)  VALUES (" + roleId + ", " + permission.getId() + ");";
//			int rowCountInsertRolePermissionMapping = statement.executeUpdate(queryInsertRolePermissionMapping);
//			//check if all were inserted
//			allRolePermissionMappingsInserted = allRolePermissionMappingsInserted && (rowCountInsertRolePermissionMapping > 0);
//
//		}
//		//close statement and result sets
//		statement.close();
//		rsInsertedId.close();
//
//		return rowCountInsertRole > 0 && allRolePermissionMappingsInserted;
//	}
//
//	/**
//	 * Inserts a new role into the database.
//	 * @param role The role object to be inserted.
//	 * @return True if successful, false if not
//	 * @throws Exception SQLException
//	 */
//	public boolean insertNewRole(Role role) throws Exception {
//		//TODO [Maybe] add error codes for unsuccessful insertion attempts.		
//		return insertNewRole(role.getTitle(), role.getPermissions());
//	}
//
//
//	/**
//	 * Inserts a new permission into the database.
//	 * @param id The [UNIQUE!] identifier characterizing the permission.
//	 * @param title A short title for the permission (should be unique, easier to differ the permissions)
//	 * @param description A more detailed description of the permission and its functional scope
//	 * @return True if successful, false if not
//	 * @throws Exception SQLException
//	 */
//	public boolean inserNewPermission(int id, String title, String description) throws Exception {
//		//TODO [Maybe] add error codes for unsuccessful insertion attempts.
//
//		String queryInsertPermission = "INSERT INTO " + permissionTableName + " (ID, TITLE, DESCRIPTION)  VALUES ('" + id + "', '" + title + "', '" + description + "');";
//
//		//insert the permission
//		Statement statement = dbConnection.createStatement();		
//		int rowCountInsertPermission = statement.executeUpdate(queryInsertPermission);
//
//		//close statement and result sets
//		statement.close();
//		return rowCountInsertPermission > 0;		
//	}
//
//	/**
//	 * Inserts a new permission into the database.
//	 * @param permission The permission to be inserted.
//	 * @return True if successful, false if not
//	 * @throws Exception SQLException
//	 */
//	public boolean inserNewPermission(Permission permission) throws Exception {
//		//TODO [Maybe] add error codes for unsuccessful insertion attempts.
//
//		return inserNewPermission(permission.getId(), permission.getTitle(), permission.getDescription());
//	}
//
//	/**
//	 * Changes the role of the given user to the given role.
//	 * @param user The user to be modified
//	 * @param newRole The role to be applied
//	 */
//	public void changeRoleOfUser(User user, Role newRole) {
//		//TODO switch the role of the given user
//	}
//
//	/**
//	 * Adds a Permission to a existing Role.
//	 * @param role The role which should get another permission
//	 * @param permission The new permission
//	 */
//	public void addPermissionToRole(Role role, Permission permission) {
//		//TODO add permission to the given role
//	}
//
//	/**
//	 * Adds a set of Permissions to a existing Role.
//	 * @param role The role which should get another permission
//	 * @param permissions The set of new permission
//	 */
//	public void addPermissionToRole(Role role, List<Permission> permissions) {
//		//TODO add permission to the given role
//	}
//
//	/**
//	 * Retrieve a list of all the current permissions.
//	 * @return A list of all permissions in the database.
//	 */
//	public List<Permission> getAllPermissions() {
//		//TODO return a list of all the current permissions
//		return null;
//	}
//
//	/**
//	 * Retrieve a list of all the current roles.
//	 * @return A list of all roles in the database.
//	 */
//	public List<Permission> getAllRoles() {
//		//TODO return a list of all the current roles
//		return null;
//	}
//
//	/**
//	 * Closes the databseConnection if necessary.
//	 * {@inheritDoc}
//	 */
//	protected void finalize() throws Throwable {
//		if (null != dbConnection) {
//			dbConnection.close();
//		}
//
//		super.finalize();
//	}
}
