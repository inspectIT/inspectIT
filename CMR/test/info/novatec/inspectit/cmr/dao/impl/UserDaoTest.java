package info.novatec.inspectit.cmr.dao.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import javax.persistence.PersistenceException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import info.novatec.inspectit.cmr.dao.UserDao;
import info.novatec.inspectit.cmr.test.AbstractTransactionalTestNGLogSupport;
import info.novatec.inspectit.communication.data.cmr.User;

@ContextConfiguration(locations = { "classpath:spring/spring-context-global.xml", "classpath:spring/spring-context-database.xml", "classpath:spring/spring-context-beans.xml",
		"classpath:spring/spring-context-processors.xml", "classpath:spring/spring-context-storage-test.xml" })
@SuppressWarnings("PMD")
public class UserDaoTest extends AbstractTransactionalTestNGLogSupport {

	@Autowired
	UserDao userDao;

	/**
	 * Tests that the saving and deleting the {@link User} works.
	 */
	@Test
	public void saveAndDeleteUser() {
		User user = new User("TestPassword", "email", 1, false);
		userDao.saveOrUpdate(user);
		userDao.delete(user);
		assertThat(userDao.findByEmail("email"), is(nullValue()));
	}
	
	/**
	 * Trying to insert Users with same emails should fail.
	 */
	@Test(expectedExceptions = {PersistenceException.class})
	public void insertingNotUniqueRole(){
		User user1 = new User("password", "email", 1, false);
		User user2 = new User("password", "email", 1, false);
		
		userDao.saveOrUpdate(user1);
		userDao.saveOrUpdate(user2);
		
		userDao.loadAll();
	}
	
	/**
	 * Tests that updating the {@link User} works.
	 */
	@Test	
	public void updateTitleAndDescription(){
		User user1 = new User("password", "email", 1, false);
		userDao.saveOrUpdate(user1);
		
		user1.setEmail("new email");
		user1.setLocked(true);
		user1.setPassword("new password");
		user1.setRoleId(2);
		
		userDao.saveOrUpdate(user1);
		
		User user2 = userDao.findByEmail(user1.getEmail());

		assertThat(user2.getEmail(), is(equalTo("new email")));
		assertThat(user2.isLocked(), is(true));
		assertThat(user2.getPassword(), is(equalTo("new password")));
		assertThat(user2.getRoleId(), is(equalTo((long)2)));
	}
}