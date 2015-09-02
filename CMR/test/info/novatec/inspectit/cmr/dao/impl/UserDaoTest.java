package info.novatec.inspectit.cmr.dao.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import info.novatec.inspectit.cmr.dao.UserDao;
import info.novatec.inspectit.cmr.test.AbstractTransactionalTestNGLogSupport;
import info.novatec.inspectit.cmr.usermanagement.User;

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
		User user2 = new User("TestName", "TestPassword", "email", 1);

		userDao.saveOrUpdate(user);
		userDao.saveOrUpdate(user2);
		Long id1 = user.getId();
		Long id2 = user2.getId();

		System.out.println(id1);
		System.out.println(id2);
		
		List<User> users = userDao.loadAll();
        
        for(User u : users){

            System.out.println(u.getId());
        }

		assertThat(user.getId(), is(greaterThan(0L)));

		userDao.delete(user);
		userDao.delete(user2);

		assertThat(userDao.load(id1), is(nullValue()));
		assertThat(userDao.load(id2), is(nullValue()));
		
	}
	
	//TODO More tests

}
