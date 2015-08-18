package info.novatec.inspectit.cmr.dao.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
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
	public void deleteAndDeletePermission() {
		Permission permission1 = new Permission("Normal-User", null);
		Permission permission2 = new Permission("Power-User", null);

		permissionDao.saveOrUpdate(permission1);
		permissionDao.saveOrUpdate(permission2);
		Long id1 = permission1.getId();
		Long id2 = permission2.getId();

		System.out.println(id1);
		System.out.println(id2);

		assertThat(permission1.getId(), is(greaterThan(0L)));

		permissionDao.delete(permission1);
		permissionDao.delete(permission2);

		assertThat(permissionDao.load(id1), is(nullValue()));
		assertThat(permissionDao.load(id2), is(nullValue()));
		
	}
	
	
	
	//TODO more tests
}
