package rocks.inspectit.agent.java.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import org.testng.annotations.Test;

import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Tests the {@link ClassUtil} class.
 *
 * @author Ivan Senic
 *
 */
public class ClassUtilTest extends TestBase {

	public class SearchInterface extends ClassUtilTest {

		@Test
		public void classNull() {
			String interfaceName = SearchedInterface.class.getCanonicalName();

			Class<?> found = ClassUtil.searchInterface(null, interfaceName);

			assertThat(found, is(nullValue()));
		}

		@Test
		public void classWithoutInterface() {
			String interfaceName = SearchedInterface.class.getCanonicalName();

			Class<?> found = ClassUtil.searchInterface(ClassWithoutInterface.class, interfaceName);

			assertThat(found, is(nullValue()));
		}

		@Test
		public void classWithoutSomeInterface() {
			String interfaceName = SearchedInterface.class.getCanonicalName();

			Class<?> found = ClassUtil.searchInterface(ClassWithSomeInterface.class, interfaceName);

			assertThat(found, is(nullValue()));
		}

		@Test
		public void classWithInterface() {
			String interfaceName = SearchedInterface.class.getCanonicalName();

			Class<?> found = ClassUtil.searchInterface(ClassWithSearchedInterface.class, interfaceName);

			assertThat(SearchedInterface.class.equals(found), is(true));
		}

		@Test
		public void classWithInterfaceOther() {
			String interfaceName = "other";

			Class<?> found = ClassUtil.searchInterface(ClassWithSearchedInterface.class, interfaceName);

			assertThat(found, is(nullValue()));
		}

		@Test
		public void classWithSubInterface() {
			String interfaceName = SearchedInterface.class.getCanonicalName();

			Class<?> found = ClassUtil.searchInterface(ClassWithSubInterface.class, interfaceName);

			assertThat(SearchedInterface.class.equals(found), is(true));
		}

		@Test
		public void classWithSubInterfaceOther() {
			String interfaceName = "other";

			Class<?> found = ClassUtil.searchInterface(ClassWithSubInterface.class, interfaceName);

			assertThat(found, is(nullValue()));
		}

		@Test
		public void superclassExtendsClassWithSearchedInterface() {
			String interfaceName = SearchedInterface.class.getCanonicalName();

			Class<?> found = ClassUtil.searchInterface(SuperclassExtendsClassWithSearchedInterface.class, interfaceName);

			assertThat(SearchedInterface.class.equals(found), is(true));
		}

		@Test
		public void superclassExtendsClassWithSearchedInterfaceOther() {
			String interfaceName = "other";

			Class<?> found = ClassUtil.searchInterface(SuperclassExtendsClassWithSearchedInterface.class, interfaceName);

			assertThat(found, is(nullValue()));
		}

		@Test
		public void superclassExtendsClassClassWithSubInterface() {
			String interfaceName = SearchedInterface.class.getCanonicalName();

			Class<?> found = ClassUtil.searchInterface(SuperclassExtendsClassClassWithSubInterface.class, interfaceName);

			assertThat(SearchedInterface.class.equals(found), is(true));
		}

		@Test
		public void superclassExtendsClassClassWithSubInterfaceOther() {
			String interfaceName = "other";

			Class<?> found = ClassUtil.searchInterface(SuperclassExtendsClassClassWithSubInterface.class, interfaceName);

			assertThat(found, is(nullValue()));
		}

	}

	interface SomeInterface {}
	interface SearchedInterface {}
	interface SubInterface extends SearchedInterface {}
	public static class ClassWithoutInterface {}
	public static class ClassWithSomeInterface implements SomeInterface {}
	public static class ClassWithSearchedInterface implements SomeInterface, SearchedInterface {}
	public static class ClassWithSubInterface implements SomeInterface, SubInterface {}
	public static class SuperclassExtendsClassWithSearchedInterface extends ClassWithSearchedInterface implements SomeInterface {}
	public static class SuperclassExtendsClassClassWithSubInterface extends ClassWithSubInterface implements SomeInterface {}

}
