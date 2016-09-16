package rocks.inspectit.agent.java.proxy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.proxy.impl.RuntimeLinker;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Jonas Kunz
 *
 */
public class RuntimeLinkerTest extends TestBase {

	@Mock
	Logger log;

	@Mock
	IProxyBuilder proxyBuilder;

	@InjectMocks
	RuntimeLinker linker;

	@BeforeMethod
	public void initMocks(){
		when(proxyBuilder.createProxyClass(any(IProxyBuildPlan.class))).thenAnswer(new Answer<IProxyClassInfo>() {

			public IProxyClassInfo answer(InvocationOnMock invocation) throws Throwable {
				IProxyClassInfo info = new IProxyClassInfo() {

					class ProxyClass extends ClassToProxy {

						public ProxyClass(String pleaseSayHello) {
							super(pleaseSayHello);
						}

						@Override
						public int doubleInt(int a) {
							return 0;
						}

						@Override
						public StringBuffer createStringBuffer() {
							return null;
						}

						@Override
						public void appendToStringBuffer(StringBuffer sb, String stringToAppend) {

						}

					}

					public Class<?> getProxyClass() {
						return ProxyClass.class;
					}

					public Object createProxy(IProxySubject proxySubject) {
						return new ProxyClass("Hello");
					}
				};
				return info;
			}
		});
	}

	@Test
	public void testProxyCaching() {
		linker.createProxy(CorrectProxySubject.class, new CorrectProxySubject(), ClassToProxy.class.getClassLoader());
		linker.createProxy(CorrectProxySubject.class, new CorrectProxySubject(), ClassToProxy.class.getClassLoader());

		verify(proxyBuilder, times(1)).createProxyClass(any(IProxyBuildPlan.class));
	}

	@Test
	public void testProxyInitialization() {
		CorrectProxySubject subject = mock(CorrectProxySubject.class);
		Object proxy = linker.createProxy(CorrectProxySubject.class, subject, ClassToProxy.class.getClassLoader());
		verify(subject, times(1)).proxyLinked(proxy, linker);
	}

	@Test
	public void testProxyInstanceofCheck() {
		Object proxy = linker.createProxy(CorrectProxySubject.class, new CorrectProxySubject(), ClassToProxy.class.getClassLoader());

		assertThat(linker.isProxyInstance(proxy, CorrectProxySubject.class), equalTo(true));
		assertThat(linker.isProxyInstance(new Object(), CorrectProxySubject.class), equalTo(false));

	}

}
