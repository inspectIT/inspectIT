package info.novatec.inspectit.cmr.dao.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import javax.persistence.PersistenceException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import info.novatec.inspectit.cmr.dao.PermissionDao;
import info.novatec.inspectit.cmr.test.AbstractTransactionalTestNGLogSupport;
import info.novatec.inspectit.communication.data.cmr.Permission;

@ContextConfiguration(locations = { "classpath:spring/spring-context-global.xml", "classpath:spring/spring-context-database.xml", "classpath:spring/spring-context-beans.xml",
		"classpath:spring/spring-context-processors.xml", "classpath:spring/spring-context-storage-test.xml" })
@SuppressWarnings("PMD")
public class PermissionDaoTest extends AbstractTransactionalTestNGLogSupport {

	@Autowired
	PermissionDao permissionDao;

	/**
	 * Tests that saving and deleting the {@link Permission} works.
	 */
	@Test
	public void saveAndDeletePermission() {		
		Permission permission1 = new Permission("aPermission", "");

		permissionDao.saveOrUpdate(permission1);
				
		assertThat(permission1.getId(), is(greaterThan(0L)));

		permissionDao.delete(permission1);

		assertThat(permissionDao.findByTitle(permission1.getTitle()), is(nullValue()));
	}
	/**
	 * Trying to insert permissions with same titles should fail.
	 */
	@Test(expectedExceptions = {PersistenceException.class})
	public void insertingNotUniquePermissions(){
		Permission p1 = new Permission("Example", "A Example Permission");
		Permission p2 = new Permission("Example", "Another Example Permission");
		
		permissionDao.saveOrUpdate(p1);
		permissionDao.saveOrUpdate(p2);
		
		permissionDao.loadAll();
	}
	
	/**
	 * Tests that updating the {@link Permission} works.
	 */
	@Test	
	public void updateTitleAndDescription(){
		Permission p1 = new Permission("Example", "A Example Permission");
		permissionDao.saveOrUpdate(p1);
		
		p1.setTitle("A new Title");
		p1.setDescription("A new Description");
		
		permissionDao.saveOrUpdate(p1);
		
		Permission p2 = permissionDao.findById(p1.getId());
		
		assertThat(p2.getTitle(), is(equalTo("A new Title")));
		assertThat(p2.getDescription(), is(equalTo("A new Description")));
	}
	
	//TODO more tests
}
