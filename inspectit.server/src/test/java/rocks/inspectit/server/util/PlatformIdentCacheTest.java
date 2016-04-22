package rocks.inspectit.server.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import rocks.inspectit.server.test.AbstractTestNGLogSupport;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;

/**
 * Small test for {@link PlatformIdentCache}.
 *
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class PlatformIdentCacheTest extends AbstractTestNGLogSupport {

	private PlatformIdentCache platformIdentCache;

	@Mock
	private PlatformIdent platformIdent;

	@BeforeClass
	public void init() {
		MockitoAnnotations.initMocks(this);
		platformIdentCache = new PlatformIdentCache();
	}

	/**
	 * Tests the simple set of actions on the {@link PlatformIdentCache}.
	 */
	@Test
	public void cache() {
		Mockito.when(platformIdent.getId()).thenReturn(-1L);
		int initialSize = platformIdentCache.getSize();

		platformIdentCache.markClean(platformIdent);
		assertThat(platformIdentCache.getSize(), is(equalTo(initialSize + 1)));
		assertThat(platformIdentCache.getCleanPlatformIdents(), contains(platformIdent));
		assertThat(platformIdentCache.getDirtyPlatformIdents(), is(empty()));

		platformIdentCache.markClean(platformIdent);
		assertThat(platformIdentCache.getSize(), is(equalTo(initialSize + 1)));

		platformIdentCache.markDirty(platformIdent);
		assertThat(platformIdentCache.getSize(), is(equalTo(initialSize + 1)));
		assertThat(platformIdentCache.getDirtyPlatformIdents(), contains(platformIdent));

		platformIdentCache.remove(platformIdent);
		assertThat(platformIdentCache.getSize(), is(equalTo(initialSize)));
	}

}
