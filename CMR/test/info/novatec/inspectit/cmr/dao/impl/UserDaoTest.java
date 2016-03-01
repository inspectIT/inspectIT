package info.novatec.inspectit.cmr.dao.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.security.NoSuchAlgorithmException;

import info.novatec.inspectit.cmr.dao.UserDao;
import info.novatec.inspectit.cmr.test.AbstractTransactionalTestNGLogSupport;
import info.novatec.inspectit.communication.data.cmr.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(locations = { "classpath:spring/spring-context-global.xml", "classpath:spring/spring-context-database.xml", "classpath:spring/spring-context-beans.xml",
		"classpath:spring/spring-context-processors.xml", "classpath:spring/spring-context-storage-test.xml" })
@SuppressWarnings("PMD")
public class UserDaoTest extends AbstractTransactionalTestNGLogSupport {

	@Autowired
	UserDao userDao;

	/**
	 * Tests that the saving and deleting the {@link User} works.
	 * @throws NoSuchAlgorithmException 
	 */
	@Test
	public void saveAndDeleteUser() throws NoSuchAlgorithmException {
		User user = new User("TestPassword", "email", 1);

		userDao.saveOrUpdate(user);
		
        
//        List<User> listOfUsers = userDao.findByEmail("email");
//        
//        for (User u : listOfUsers) {
//        	System.out.println(u.toString());
//        }
        
        

		userDao.delete(user);

		assertThat(userDao.load("email"), is(nullValue()));
	}
}