package info.novatec.inspectit.cmr.dao.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import info.novatec.inspectit.cmr.dao.UserDao;
import info.novatec.inspectit.cmr.test.AbstractTransactionalTestNGLogSupport;
import info.novatec.inspectit.communication.data.cmr.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(locations = { "classpath:spring/spring-context-global.xml", "classpath:spring/spring-context-database.xml", "classpath:spring/spring-context-beans.xml",
		"classpath:spring/spring-context-processors.xml", "classpath:spring/spring-context-storage-test.xml" })
public class UserDaoTest extends AbstractTransactionalTestNGLogSupport {

	@Autowired
	UserDao userDao;

	/**
	 * Tests that the saving and deleting the {@link User} works.
	 * @throws NoSuchAlgorithmException 
	 */
	@Test
	public void saveAndDeleteUser() throws NoSuchAlgorithmException {
		User user = new User("TestName", "TestPassword", "email", 1);

		userDao.saveOrUpdate(user);
		
		List<User> users = userDao.loadAll();
        
        for(User u : users){

            System.out.println(u.toString());
        }
        
        User retrievedUser = userDao.load("TestName");
        
//        List<User> listOfUsers = userDao.findByEmail("email");
//        
//        for (User u : listOfUsers) {
//        	System.out.println(u.toString());
//        }
        
        System.out.println("Name of user: " + retrievedUser.getName());
        

		userDao.delete(user);

		assertThat(userDao.load("TestName"), is(nullValue()));
	}
}