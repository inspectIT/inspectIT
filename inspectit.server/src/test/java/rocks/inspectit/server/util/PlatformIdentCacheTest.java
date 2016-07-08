package rocks.inspectit.server.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import java.util.Collection;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Small test for {@link PlatformIdentCache}.
 *
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class PlatformIdentCacheTest extends TestBase {

	private static final Long PLATFORM_ID = 1L;

	@InjectMocks
	PlatformIdentCache platformIdentCache;

	@Mock
	PlatformIdent platformIdent;

	@Mock
	PlatformIdent platformIdent2;

	public class MarkDirty extends PlatformIdentCacheTest {

		@Test
		public void emptyCache() {
			when(platformIdent.getId()).thenReturn(PLATFORM_ID);

			platformIdentCache.markDirty(platformIdent);

			assertThat(platformIdentCache.getCleanPlatformIdents(), is(empty()));
		}

		@Test
		public void mark() {
			when(platformIdent.getId()).thenReturn(PLATFORM_ID);
			platformIdentCache.markClean(platformIdent);

			platformIdentCache.markDirty(platformIdent);

			assertThat(platformIdentCache.getCleanPlatformIdents(), is(empty()));
		}

		@Test
		public void markAnother() {
			when(platformIdent.getId()).thenReturn(PLATFORM_ID);
			when(platformIdent2.getId()).thenReturn(1212L);
			platformIdentCache.markClean(platformIdent);
			platformIdentCache.markClean(platformIdent2);

			platformIdentCache.markDirty(platformIdent);

			assertThat(platformIdentCache.getCleanPlatformIdents(), hasSize(1));
			assertThat(platformIdentCache.getCleanPlatformIdents(), hasItem(platformIdent2));
		}
	}

	public class MarkClean extends PlatformIdentCacheTest {

		@Test
		public void mark() {
			when(platformIdent.getId()).thenReturn(PLATFORM_ID);

			platformIdentCache.markClean(platformIdent);

			assertThat(platformIdentCache.getCleanPlatformIdents(), hasSize(1));
			assertThat(platformIdentCache.getCleanPlatformIdents(), hasItem(platformIdent));
		}

		@Test
		public void markTwice() {
			when(platformIdent.getId()).thenReturn(PLATFORM_ID);

			platformIdentCache.markClean(platformIdent);
			platformIdentCache.markClean(platformIdent);

			assertThat(platformIdentCache.getCleanPlatformIdents(), hasSize(1));
			assertThat(platformIdentCache.getCleanPlatformIdents(), hasItem(platformIdent));
		}
	}

	public class Remove extends PlatformIdentCacheTest {

		@Test
		public void emptyCache() {
			when(platformIdent.getId()).thenReturn(PLATFORM_ID);

			platformIdentCache.remove(platformIdent);

			assertThat(platformIdentCache.getCleanPlatformIdents(), is(empty()));
		}

		@Test
		public void remove() {
			when(platformIdent.getId()).thenReturn(PLATFORM_ID);
			platformIdentCache.markClean(platformIdent);

			platformIdentCache.remove(platformIdent);

			assertThat(platformIdentCache.getCleanPlatformIdents(), is(empty()));
		}

		@Test
		public void removeWithTwo() {
			when(platformIdent.getId()).thenReturn(PLATFORM_ID);
			when(platformIdent2.getId()).thenReturn(1212L);
			platformIdentCache.markClean(platformIdent);
			platformIdentCache.markClean(platformIdent2);

			platformIdentCache.remove(platformIdent);

			assertThat(platformIdentCache.getCleanPlatformIdents(), hasSize(1));
			assertThat(platformIdentCache.getCleanPlatformIdents(), hasItem(platformIdent2));
		}
	}

	public class GetCleanPlatformIdents extends PlatformIdentCacheTest {

		@Test
		public void emptyCache() {
			assertThat(platformIdentCache.getCleanPlatformIdents(), is(empty()));
		}

		@Test
		public void get() {
			when(platformIdent.getId()).thenReturn(PLATFORM_ID);
			platformIdentCache.markClean(platformIdent);

			Collection<PlatformIdent> cleanPlatformIdents = platformIdentCache.getCleanPlatformIdents();

			assertThat(cleanPlatformIdents, hasSize(1));
			assertThat(cleanPlatformIdents, hasItem(platformIdent));
		}

		@Test
		public void getTwo() {
			when(platformIdent.getId()).thenReturn(PLATFORM_ID);
			when(platformIdent2.getId()).thenReturn(1212L);
			platformIdentCache.markClean(platformIdent);
			platformIdentCache.markClean(platformIdent2);

			Collection<PlatformIdent> cleanPlatformIdents = platformIdentCache.getCleanPlatformIdents();

			assertThat(cleanPlatformIdents, hasSize(2));
			assertThat(cleanPlatformIdents, hasItem(platformIdent));
			assertThat(cleanPlatformIdents, hasItem(platformIdent2));
		}
	}

}
