package rocks.inspectit.agent.java.proxy;

import java.io.FileNotFoundException;

/**
 * @author Jonas Kunz
 *
 */
@ProxyFor(superClass = "moved.rocks.inspectit.agent.java.proxy.ClassToProxy", implementedInterfaces = { "moved.rocks.inspectit.agent.java.proxy.InterfaceToProxy" }, constructorParameterTypes = {
		"java.lang.String" })
@SuppressWarnings({ "PMD" })
public class CorrectProxySubject implements IProxySubject {

	public Object proxy;
	public IRuntimeLinker linker;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] getProxyConstructorArguments() {
		return new Object[] { "Hello" };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void proxyLinked(Object proxy, IRuntimeLinker linker) {
		this.proxy = proxy;
		this.linker = linker;
	}

	// test renaming and primitive arguments and return type
	@ProxyMethod(methodName = "doubleInt")
	public int doubleInt_renamed(int a) {
		return 2 * a;
	}

	// fake method to check for issues with method renaming
	public int doubleInt(int a) {
		return a;
	}

	// Test Object return type and proxying of methods with limited visibility
	@ProxyMethod
	public String sayHello() {
		return "Hello!";
	}

	// Test ability to down-cast cast return types in case they are of a type only known at runtime
	@ProxyMethod(returnType = "java.lang.StringBuffer")
	public Object createStringBuffer() {
		return new StringBuffer();
	}

	// Test ability to up-cast cast argument types in case they are of a type only known at runtime
	@ProxyMethod(parameterTypes = { "java.lang.StringBuffer", "java.lang.String" })
	public void appendToStringBuffer(Object sb, Object stringToAppend) {
		((StringBuffer) sb).append((String) stringToAppend);
	}

	// overwrite itnerface mthod with more specific exception and throw it
	@ProxyMethod
	public void throwException() throws FileNotFoundException {
		throw new FileNotFoundException("testtext");
	}

}
