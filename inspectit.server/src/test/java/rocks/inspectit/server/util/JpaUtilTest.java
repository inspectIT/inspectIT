package rocks.inspectit.server.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.persistence.EntityManager;

import org.mockito.Mock;
import org.testng.annotations.Test;

import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Tests the {@link JpaUtil} class.
 *
 * @author Ivan Senic
 */
public class JpaUtilTest extends TestBase {

	@Mock
	EntityManager entityManager;

	public class Delete extends JpaUtilTest {

		@Test
		public void contains() {
			Object object = mock(Object.class);
			when(entityManager.contains(object)).thenReturn(true);

			JpaUtil.delete(entityManager, object);

			verify(entityManager).contains(object);
			verify(entityManager).remove(object);
			verifyNoMoreInteractions(entityManager);
		}

		@Test
		public void doesNotcontain() {
			Object object = mock(Object.class);
			Object merged = mock(Object.class);
			when(entityManager.contains(object)).thenReturn(false);
			when(entityManager.merge(object)).thenReturn(merged);

			JpaUtil.delete(entityManager, object);

			verify(entityManager).contains(object);
			verify(entityManager).merge(object);
			verify(entityManager).remove(merged);
			verifyNoMoreInteractions(entityManager);
		}
	}
}
