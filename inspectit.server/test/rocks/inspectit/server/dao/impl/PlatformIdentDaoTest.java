package info.novatec.inspectit.cmr.dao.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import info.novatec.inspectit.cmr.dao.PlatformIdentDao;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.test.AbstractTransactionalTestNGLogSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(locations = { "classpath:spring/spring-context-global.xml", "classpath:spring/spring-context-database.xml", "classpath:spring/spring-context-beans.xml",
		"classpath:spring/spring-context-processors.xml", "classpath:spring/spring-context-storage-test.xml" })
public class PlatformIdentDaoTest extends AbstractTransactionalTestNGLogSupport {

	@Autowired
	PlatformIdentDao platformIdentDao;

	/**
	 * Tests that the saving and deleting the {@link PlatformIdent} works.
	 */
	@Test
	public void deletePlatformIdent() {
		PlatformIdent platformIdent = new PlatformIdent();
		platformIdent.setAgentName("TestPlatform");

		platformIdentDao.saveOrUpdate(platformIdent);
		Long id = platformIdent.getId();

		assertThat(platformIdent.getId(), is(greaterThan(0L)));

		platformIdentDao.delete(platformIdent);

		assertThat(platformIdentDao.load(id), is(nullValue()));
	}

}
