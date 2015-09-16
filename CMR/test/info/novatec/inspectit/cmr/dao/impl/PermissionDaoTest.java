package info.novatec.inspectit.cmr.dao.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.List;


import info.novatec.inspectit.cmr.dao.PermissionDao;
import info.novatec.inspectit.cmr.test.AbstractTransactionalTestNGLogSupport;
import info.novatec.inspectit.cmr.usermanagement.Permission;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(locations = { "classpath:spring/spring-context-global.xml", "classpath:spring/spring-context-database.xml", "classpath:spring/spring-context-beans.xml",
		"classpath:spring/spring-context-processors.xml", "classpath:spring/spring-context-storage-test.xml" })
public class PermissionDaoTest extends AbstractTransactionalTestNGLogSupport {

	@Autowired
	PermissionDao permissionDao;

	/**
	 * Tests that the saving and deleting the {@link Permission} works.
	 */
	@Test
	public void saveAndDeletePermission() {
		
		Permission permission1 = new Permission("aPermission", null);

		permissionDao.saveOrUpdate(permission1);
		
		Long id1 = permission1.getId();

		System.out.println(id1);

		List<Permission> mylist = permissionDao.loadAll();
		
		for(Permission p : mylist){
			System.out.println(p);
		}
		
		assertThat(permission1.getId(), is(greaterThan(0L)));

		permissionDao.delete(permission1);

		assertThat(permissionDao.get(id1), is(nullValue()));

		
	}
	
	
	
}
