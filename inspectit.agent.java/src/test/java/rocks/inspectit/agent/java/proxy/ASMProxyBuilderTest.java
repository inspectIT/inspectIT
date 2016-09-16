package rocks.inspectit.agent.java.proxy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
	Object proxy;

	@BeforeMethod
	public void initProxy() throws InvalidProxyDescriptionException {
		subject = new CorrectProxySubject();
		ClassLoader cl = new RenamingClassLoader(this.getClass().getClassLoader(), "moved.", ClassToProxy.class, InterfaceToProxy.class);
		IProxyBuildPlan plan = ProxyBuildPlanImpl.create(CorrectProxySubject.class, "proxy_" + counter.getAndIncrement(), cl);
		proxy = builder.createProxyClass(plan).createProxy(subject);

	}

	@Test
	public void testMethodRenaming() throws Throwable {
		assertThat(callProxy("doubleInt", 3), equalTo((Object) 6));
	}

	@Test
	public void testProtectedMethodProxying() throws Throwable {
		assertThat(callProxy("sayHello"), equalTo((Object) "Hello!"));
	}

	@Test
	public void testReturnTypeCasting() throws Throwable {
		assertThat(callProxy("createStringBuffer"), allOf(instanceOf(StringBuffer.class), notNullValue()));
	}

	@Test
	public void testArgumentCasting() throws Throwable {
		StringBuffer a = new StringBuffer("hello ");
		callProxy("appendToStringBuffer", a, "World!");
		assertThat(a.toString(), equalTo("hello World!"));
	}

	@Test(expectedExceptions = FileNotFoundException.class)
	public void testExceptionPassing() throws Throwable {
		callProxy("throwException");
	}

	private Object callProxy(String methodName, Object... params) throws Throwable {
		Class<?>[] paramTypes = new Class<?>[params.length];
		for (int i = 0; i < params.length; i++) {
			Class<? extends Object> class1 = params[i].getClass();
			if (class1 == Integer.class) {
				class1 = int.class;
			}
			paramTypes[i] = class1;
		}
		try {
			Method declaredMethod = proxy.getClass().getDeclaredMethod(methodName, paramTypes);
			declaredMethod.setAccessible(true);
			return declaredMethod.invoke(proxy, params);
		} catch (InvocationTargetException e) {
			throw e.getCause();
		} catch (Exception e) {
			throw e;
		}
	}

}
