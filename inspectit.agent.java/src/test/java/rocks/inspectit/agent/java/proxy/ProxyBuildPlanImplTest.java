package rocks.inspectit.agent.java.proxy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

import java.nio.CharBuffer;

import org.testng.annotations.Test;

import rocks.inspectit.agent.java.proxy.impl.InvalidProxyDescriptionException;
import rocks.inspectit.agent.java.proxy.impl.ProxyBuildPlanImpl;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Jonas Kunz
 *
 */
public class ProxyBuildPlanImplTest extends TestBase {

	private static class BasicSubject implements IProxySubject {

		@Override
		public Object[] getProxyConstructorArguments() {
			return new Object[] {};
		}

		@Override
		public void proxyLinked(Object proxyObject, IRuntimeLinker linker) {

		}

	}

	public static class CreateTest extends ProxyBuildPlanImplTest {

		@Test
		public void testCorrectSuperClassAndInterfaces() throws InvalidProxyDescriptionException {
			@ProxyFor(superClass = "java.io.OutputStream", implementedInterfaces = { "java.lang.Runnable", "java.lang.Readable" })
			class Subject extends BasicSubject {
				@ProxyMethod
				public void run() {

				}

				@ProxyMethod
				public int read(CharBuffer buf) {
					return 0;
				}

				@ProxyMethod
				public void write(int value) {

				}
			}

			IProxyBuildPlan bp = ProxyBuildPlanImpl.create(Subject.class, "myname", ProxyBuildPlanImplTest.class.getClassLoader());
			assertThat(bp.getSuperClass().getName(), equalTo("java.io.OutputStream"));
			assertThat(bp.getImplementedInterfaces(), containsInAnyOrder(new Class<?>[] { java.lang.Runnable.class, java.lang.Readable.class }));

		}

		@Test
		public void testMethodRenaming() throws InvalidProxyDescriptionException {
			@ProxyFor
			class Subject extends BasicSubject {
				@ProxyMethod(methodName = "equals")
				public boolean thisActuallyisEquals(Object other) {
					return false;
				}
			}

			IProxyBuildPlan bp = ProxyBuildPlanImpl.create(Subject.class, "myname", ProxyBuildPlanImplTest.class.getClassLoader());

			assertThat(bp.getMethods().size(), equalTo(1));
			assertThat(bp.getMethods().get(0).getMethodName(), equalTo("equals"));
		}

		@Test(expectedExceptions = { InvalidProxyDescriptionException.class })
		public void testMethodRenamingDuplicateDetection() throws InvalidProxyDescriptionException {
			@ProxyFor(superClass = "java.io.OutputStream")
			class Subject extends BasicSubject {
				@ProxyMethod(methodName = "write")
				public void thisActuallyisWrite(int value) {
				}

				@ProxyMethod(methodName = "write")
				public void thisActuallyisWriteToo(int value) {
				}
			}

			ProxyBuildPlanImpl.create(Subject.class, "myname", ProxyBuildPlanImplTest.class.getClassLoader());
		}

		@Test(expectedExceptions = { InvalidProxyDescriptionException.class })
		public void testMissingProxyFor() throws InvalidProxyDescriptionException {
			class Subject extends BasicSubject {
				@ProxyMethod(methodName = "write")
				public void thisActuallyIsWrite(int value) {
				}
			}
			ProxyBuildPlanImpl.create(Subject.class, "myname", ProxyBuildPlanImplTest.class.getClassLoader());
		}

		@Test
		public void testDefaultSuperClass() throws InvalidProxyDescriptionException {
			@ProxyFor(implementedInterfaces = { "java.io.Closeable" })
			class Subject extends BasicSubject {
				@ProxyMethod
				public void close() {
				}
			}

			IProxyBuildPlan bp = ProxyBuildPlanImpl.create(Subject.class, "myname", ProxyBuildPlanImplTest.class.getClassLoader());
			assertThat(bp.getSuperClass().getName(), equalTo(Object.class.getName()));
		}

		@Test(expectedExceptions = { InvalidProxyDescriptionException.class })
		public void testMissingAbstractSuperclassMethod() throws InvalidProxyDescriptionException {
			@ProxyFor(superClass = "java.io.OutputStream")
			class Subject extends BasicSubject {
				// missing write() method
			}

			ProxyBuildPlanImpl.create(Subject.class, "myname", ProxyBuildPlanImplTest.class.getClassLoader());
		}

		@Test(expectedExceptions = { InvalidProxyDescriptionException.class })
		public void testMissingInterfaceMethod() throws InvalidProxyDescriptionException {
			@ProxyFor(implementedInterfaces = { "java.io.Closeable" })
			class Subject extends BasicSubject {
				// missing close() method
			}

			ProxyBuildPlanImpl.create(Subject.class, "myname", ProxyBuildPlanImplTest.class.getClassLoader());
		}

		@Test
		public void testSuperClassConstructorParameters() throws InvalidProxyDescriptionException {
			@ProxyFor(superClass = "java.io.FileOutputStream", constructorParameterTypes = { "java.io.File" })
			class Subject extends BasicSubject {
			}

			IProxyBuildPlan bp = ProxyBuildPlanImpl.create(Subject.class, "myname", ProxyBuildPlanImplTest.class.getClassLoader());
			assertThat(bp.getConstructorParameterTypes().size(), equalTo(1));
			assertThat(bp.getConstructorParameterTypes().get(0).getName(), equalTo("java.io.File"));
		}

		@Test(expectedExceptions = { InvalidProxyDescriptionException.class })
		public void testSuperClassConstructorInvalidParameters() throws InvalidProxyDescriptionException {
			@ProxyFor(superClass = "java.io.FileOutputStream", constructorParameterTypes = { "java.io.Closeable" })
			class Subject extends BasicSubject {
			}

			ProxyBuildPlanImpl.create(Subject.class, "myname", ProxyBuildPlanImplTest.class.getClassLoader());
		}

		@Test
		public void testParameterCorrectCasting() throws InvalidProxyDescriptionException {
			@ProxyFor()
			class Subject extends BasicSubject {
				@ProxyMethod(parameterTypes = { "java.io.File" })
				public void castMe(Object thisIsActuallyAFile) {

				}
			}
			IProxyBuildPlan bp = ProxyBuildPlanImpl.create(Subject.class, "myname", ProxyBuildPlanImplTest.class.getClassLoader());
			assertThat(bp.getMethods().size(), equalTo(1));
			assertThat(bp.getMethods().get(0).getParameterTypes().size(), equalTo(1));
			assertThat(bp.getMethods().get(0).getParameterTypes().get(0).getName(), equalTo("java.io.File"));

		}

		@Test(expectedExceptions = { InvalidProxyDescriptionException.class })
		public void testParameterInvalidCasting() throws InvalidProxyDescriptionException {
			@ProxyFor()
			class Subject extends BasicSubject {
				@ProxyMethod(parameterTypes = { "java.lang.Object" })
				public void castMe(java.io.File thisCouldBeNotAFile) {

				}
			}
			ProxyBuildPlanImpl.create(Subject.class, "myname", ProxyBuildPlanImplTest.class.getClassLoader());

		}

		@Test
		public void testReturnTypeCorrectCasting() throws InvalidProxyDescriptionException {
			@ProxyFor()
			class Subject extends BasicSubject {
				@ProxyMethod(returnType = "java.io.File")
				public Object castMe() {
					return null;
				}
			}
			IProxyBuildPlan bp = ProxyBuildPlanImpl.create(Subject.class, "myname", ProxyBuildPlanImplTest.class.getClassLoader());
			assertThat(bp.getMethods().size(), equalTo(1));
			assertThat(bp.getMethods().get(0).getReturnType().getName(), equalTo("java.io.File"));

		}


		@Test
		public void testReturnTypeInvalidCasting() throws InvalidProxyDescriptionException {
			@ProxyFor()
			class Subject extends BasicSubject {
				@ProxyMethod(returnType = "java.io.OutputStream")
				public java.io.File castMe() {
					return null;
				}
			}
			ProxyBuildPlanImpl.create(Subject.class, "myname", ProxyBuildPlanImplTest.class.getClassLoader());

		}

		@Test(expectedExceptions = InvalidProxyDescriptionException.class)
		public void testNonOptionalMethod() throws InvalidProxyDescriptionException {
			@ProxyFor
			class Subject extends BasicSubject {
				@ProxyMethod(parameterTypes = { "this.does.not.Exist" })
				public void methodWithMissingParameterType(Object param) {
				}
			}
			ProxyBuildPlanImpl.create(Subject.class, "myname", ProxyBuildPlanImplTest.class.getClassLoader());
		}

		@Test
		public void testOptionalMethod() throws InvalidProxyDescriptionException {
			@ProxyFor
			class Subject extends BasicSubject {
				@ProxyMethod(parameterTypes = { "this.does.not.Exist" }, isOptional = true)
				public void methodWithMissingParameterType(Object param) {
				}
			}
			IProxyBuildPlan bp = ProxyBuildPlanImpl.create(Subject.class, "myname", ProxyBuildPlanImplTest.class.getClassLoader());
			assertThat(bp.getMethods().size(), equalTo(0));
		}
	}

}
