package info.novatec.inspectit.cmr.dao.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.ArrayList;

import javax.persistence.PersistenceException;

import info.novatec.inspectit.cmr.dao.RoleDao;
import info.novatec.inspectit.cmr.test.AbstractTransactionalTestNGLogSupport;
import info.novatec.inspectit.communication.data.cmr.Permission;
import info.novatec.inspectit.communication.data.cmr.Role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(locations = { "classpath:spring/spring-context-global.xml", "classpath:spring/spring-context-database.xml", "classpath:spring/spring-context-beans.xml",
		"classpath:spring/spring-context-processors.xml", "classpath:spring/spring-context-storage-test.xml" })
@SuppressWarnings("PMD")
public class RoleDaoTest extends AbstractTransactionalTestNGLogSupport {

	@Autowired
	RoleDao roleDao;

	/**
	 * Tests that the saving and deleting the {@link Role} works.
	 */
	@Test
	public void saveAndDeleteRole() {
        Role role1 = new Role("Example Role", new ArrayList<Permission>(), "");
        Role role2 = new Role("Another Example Role", new ArrayList<Permission>(), "");

        roleDao.saveOrUpdate(role1);
        roleDao.saveOrUpdate(role2);

        assertThat(role1.getId(), is(greaterThan(0L)));

        roleDao.delete(role1);
        roleDao.delete(role2);

        assertThat(roleDao.findByTitle("Example Role"), is(nullValue()));
        assertThat(roleDao.findByTitle("Another Example Role"), is(nullValue()));        
    }
	
	/**
	 * Trying to insert Roles with same titles should fail.
	 */
	@Test(expectedExceptions = {PersistenceException.class})
	public void insertingNotUniqueRole(){
		Role role1 = new Role("Example", new ArrayList<Permission>(), "A Example Role");
		Role role2 = new Role("Example", new ArrayList<Permission>(), "Another Example Role");
		
		roleDao.saveOrUpdate(role1);
		roleDao.saveOrUpdate(role2);
		
		roleDao.loadAll();
	}
	
	/**
	 * Tests that updating the {@link Role} works.
	 */
	@Test	
	public void updateTitleAndDescription(){
		Role role1 = new Role("Example", new ArrayList<Permission>(), "A Example Role");
		roleDao.saveOrUpdate(role1);
		
		role1.setTitle("A new Title");
		role1.setDescription("A new Description");
		
		roleDao.saveOrUpdate(role1);
		
		Role role2 = roleDao.findByID(role1.getId());
		
		assertThat(role2.getTitle(), is(equalTo("A new Title")));
		assertThat(role2.getDescription(), is(equalTo("A new Description")));
	}
	
	//TODO more test
}
