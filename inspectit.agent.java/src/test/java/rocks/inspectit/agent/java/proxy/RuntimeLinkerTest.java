package rocks.inspectit.agent.java.proxy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.proxy.impl.ASMProxyBuilder;
import rocks.inspectit.agent.java.proxy.impl.RuntimeLinker;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Jonas Kunz
 *
 */
public class RuntimeLinkerTest extends TestBase {


	@Mock
	Logger log;

	@Spy
	ASMProxyBuilder proxyBuilder;

	@InjectMocks
	RuntimeLinker linker;

	RenamingClassLoader fakeRootLoader;


	@BeforeMethod
	public void initMocks(){
		fakeRootLoader = new RenamingClassLoader(this.getClass().getClassLoader(), "moved.", ClassToProxy.class, InterfaceToProxy.class);
	}

	public static class CreateProxy extends RuntimeLinkerTest {

		@Test
		public void testProxyCaching() {
			linker.createProxy(CorrectProxySubject.class, new CorrectProxySubject(), fakeRootLoader);
			linker.createProxy(CorrectProxySubject.class, new CorrectProxySubject(), fakeRootLoader);

			verify(proxyBuilder, times(1)).createProxyClass(any(IProxyBuildPlan.class));
		}

		@Test
		public void testProxyParentClassLoaderCaching() {
			linker.createProxy(CorrectProxySubject.class, new CorrectProxySubject(), fakeRootLoader);
			linker.createProxy(CorrectProxySubject.class, new CorrectProxySubject(), new ClassLoader(fakeRootLoader) {
			});

			verify(proxyBuilder, times(1)).createProxyClass(any(IProxyBuildPlan.class));
		}

		@Test
		public void testProxyInitialization() {
			CorrectProxySubject proxySubject = spy(new CorrectProxySubject());

			Object proxy = linker.createProxy(CorrectProxySubject.class, proxySubject, fakeRootLoader);

			verify(proxySubject, times(1)).getProxyConstructorArguments();
			verify(proxySubject, times(1)).proxyLinked(proxy, linker);

		}

		@Test
		public void testProxyInvalidPlanHandling() {
			// classloader does not contain the required depdendencies
			Object proxyA = linker.createProxy(CorrectProxySubject.class, new CorrectProxySubject(), this.getClass().getClassLoader());
			Object proxyB = linker.createProxy(CorrectProxySubject.class, new CorrectProxySubject(), this.getClass().getClassLoader());

			verify(proxyBuilder, times(0)).createProxyClass(any(IProxyBuildPlan.class));
			assertThat(proxyA, equalTo(null));
			assertThat(proxyB, equalTo(null));

		}

		@Test
		public void testProxyGenerationErrorHandling() {
			doReturn(null).when(proxyBuilder).createProxyClass(any(IProxyBuildPlan.class));

			Object proxyA = linker.createProxy(CorrectProxySubject.class, new CorrectProxySubject(), fakeRootLoader);
			Object proxyB = linker.createProxy(CorrectProxySubject.class, new CorrectProxySubject(), fakeRootLoader);

			verify(proxyBuilder, times(1)).createProxyClass(any(IProxyBuildPlan.class));
			assertThat(proxyA, equalTo(null));
			assertThat(proxyB, equalTo(null));

		}

	}

	public static class IsProxyInstance extends RuntimeLinkerTest {

		@Test
		public void testProxyInstanceofCheck() {
			Object proxy = linker.createProxy(CorrectProxySubject.class, new CorrectProxySubject(), fakeRootLoader);

			assertThat(linker.isProxyInstance(proxy, CorrectProxySubject.class), equalTo(true));
			assertThat(linker.isProxyInstance(new Object(), CorrectProxySubject.class), equalTo(false));
			assertThat(linker.isProxyInstance(null, CorrectProxySubject.class), equalTo(false));
		}

	}


}
