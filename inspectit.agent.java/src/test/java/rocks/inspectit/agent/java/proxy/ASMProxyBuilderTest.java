package rocks.inspectit.agent.java.proxy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.mockito.InjectMocks;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.proxy.impl.ASMProxyBuilder;
import rocks.inspectit.agent.java.proxy.impl.InvalidProxyDescriptionException;
import rocks.inspectit.agent.java.proxy.impl.ProxyBuildPlanImpl;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Jonas Kunz
 *
 */
public class ASMProxyBuilderTest extends TestBase {

	@InjectMocks
	ASMProxyBuilder builder = new ASMProxyBuilder();

	static AtomicInteger counter = new AtomicInteger(0);

	CorrectProxySubject subject;
	ClassToProxy proxy;

	@BeforeMethod
	public void initProxy() throws InvalidProxyDescriptionException {
		subject = new CorrectProxySubject();
		IProxyBuildPlan plan = ProxyBuildPlanImpl.create(CorrectProxySubject.class, "proxy_" + counter.getAndIncrement(), ClassToProxy.class.getClassLoader());
		proxy = (ClassToProxy) builder.createProxyClass(plan).createProxy(subject);

	}

	@Test
	public void testMethodRenaming() {
		assertThat(proxy.doubleInt(3), equalTo(6));
	}

	@Test
	public void testProtectedMethodProxying() throws IOException {
		assertThat(proxy.sayHello(), equalTo("Hello!"));
	}

	@Test
	public void testReturnTypeCasting() throws IOException {
		assertThat(proxy.createStringBuffer(), allOf(instanceOf(StringBuffer.class), notNullValue()));
	}

	@Test
	public void testArgumentCasting() throws IOException {
		StringBuffer a = new StringBuffer("hello ");
		proxy.appendToStringBuffer(a, "World!");
		assertThat(a.toString(), equalTo("hello World!"));
	}

	@Test(expectedExceptions = FileNotFoundException.class)
	public void testExceptionPassing() throws IOException {
		((InterfaceToProxy) proxy).throwException();
	}



}
