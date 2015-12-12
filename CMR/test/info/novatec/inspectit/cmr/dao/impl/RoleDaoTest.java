package info.novatec.inspectit.cmr.dao.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.List;

import info.novatec.inspectit.cmr.dao.RoleDao;
import info.novatec.inspectit.cmr.test.AbstractTransactionalTestNGLogSupport;
import info.novatec.inspectit.communication.data.cmr.Role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(locations = { "classpath:spring/spring-context-global.xml", "classpath:spring/spring-context-database.xml", "classpath:spring/spring-context-beans.xml",
		"classpath:spring/spring-context-processors.xml", "classpath:spring/spring-context-storage-test.xml" })
public class RoleDaoTest extends AbstractTransactionalTestNGLogSupport {

	@Autowired
	RoleDao roleDao;

	/**
	 * Tests that the saving and deleting the {@link Role} works.
	 */
	@Test
	public void saveAndDeleteRole() {
        Role role1 = new Role(1, "Normal-User", null);
        Role role2 = new Role(2, "Power-User", null);

        roleDao.saveOrUpdate(role1);
        roleDao.saveOrUpdate(role2);
        Long id1 = role1.getId();
        Long id2 = role2.getId();

        System.out.println(id1);
        System.out.println(id2);
        
        List<Role> roles = roleDao.loadAll();
        
        for(Role r : roles){
            System.out.println(r.getId());
        }
        
        assertThat(role1.getId(), is(greaterThan(0L)));

        roleDao.delete(role1);
        roleDao.delete(role2);

        assertThat(roleDao.findByTitle("Normal-User"), is(nullValue()));
        assertThat(roleDao.findByTitle("Power-User"), is(nullValue()));
        
    }
	
	
	//TODO more test
}
