package info.novatec.inspectit.cmr.dao.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import info.novatec.inspectit.cmr.dao.RoleDao;
import info.novatec.inspectit.cmr.test.AbstractTransactionalTestNGLogSupport;
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
        Role role1 = new Role(1, "Normal-User", null, "");
        Role role2 = new Role(2, "Power-User", null, "");

        roleDao.saveOrUpdate(role1);
        roleDao.saveOrUpdate(role2);

        assertThat(role1.getId(), is(greaterThan(0L)));

        roleDao.delete(role1);
        roleDao.delete(role2);

        assertThat(roleDao.findByTitle("Normal-User"), is(nullValue()));
        assertThat(roleDao.findByTitle("Power-User"), is(nullValue()));
        
    }
	
	
	//TODO more test
}
