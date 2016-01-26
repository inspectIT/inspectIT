package info.novatec.inspectit.cmr.instrumentation.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import info.novatec.inspectit.ci.assignment.impl.ExceptionSensorAssignment;
import info.novatec.inspectit.ci.assignment.impl.MethodSensorAssignment;
import info.novatec.inspectit.cmr.instrumentation.classcache.ClassCache;
import info.novatec.inspectit.cmr.instrumentation.classcache.ClassCacheLookup;
import info.novatec.inspectit.instrumentation.classcache.ClassType;
import info.novatec.inspectit.instrumentation.classcache.ImmutableAnnotationType;
import info.novatec.inspectit.instrumentation.classcache.ImmutableClassType;
import info.novatec.inspectit.instrumentation.classcache.ImmutableInterfaceType;
import info.novatec.inspectit.instrumentation.classcache.ImmutableMethodType;
import info.novatec.inspectit.instrumentation.classcache.InterfaceType;
import info.novatec.inspectit.testbase.TestBase;

import java.util.Collection;
import java.util.Collections;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings({ "PMD", "unchecked" })
public class ClassCacheSearchNarrowerTest extends TestBase {

	@InjectMocks
	protected ClassCacheSearchNarrower narrower;

	@Mock
	protected MethodSensorAssignment methodSensorAssignment;

	@Mock
	protected ExceptionSensorAssignment exceptionSensorAssignment;

	@Mock
	protected ClassCache classCache;

	@Mock
	protected ClassCacheLookup lookup;

	@Mock
	protected ClassType classType;

	@Mock
	protected ImmutableClassType superClassType;

	@Mock
	protected ImmutableInterfaceType interfaceType;

	@Mock
	protected ImmutableAnnotationType annotationType;

	@Mock
	protected ImmutableMethodType methodType;

	@BeforeMethod
	public void setup() {
		when(classCache.getLookupService()).thenReturn(lookup);

		when(classType.isType()).thenReturn(true);
		when(classType.isClass()).thenReturn(true);
		when(classType.castToClass()).thenReturn(classType);
		when(classType.castToType()).thenReturn(classType);

		when(superClassType.isType()).thenReturn(true);
		when(superClassType.isClass()).thenReturn(true);
		when(superClassType.castToClass()).thenReturn(classType);
		when(superClassType.castToType()).thenReturn(classType);

		when(interfaceType.isType()).thenReturn(true);
		when(interfaceType.isInterface()).thenReturn(true);
		when(interfaceType.castToInterface()).thenReturn(interfaceType);
		when(interfaceType.castToType()).thenReturn(interfaceType);

		when(annotationType.isType()).thenReturn(true);
		when(annotationType.isAnnotation()).thenReturn(true);
		when(annotationType.castToAnnotation()).thenReturn(annotationType);
		when(annotationType.castToType()).thenReturn(annotationType);

		when(methodType.isMethodType()).thenReturn(true);
		when(methodType.castToMethodType()).thenReturn(methodType);
	}

	public class Narrow extends ClassCacheSearchNarrowerTest {

		@Test
		public void byDirectName() {
			String className = "info.novatec.MyClass";
			when(methodSensorAssignment.getClassName()).thenReturn(className);
			when(methodSensorAssignment.isInterf()).thenReturn(false);
			when(methodSensorAssignment.isSuperclass()).thenReturn(false);
			doReturn(Collections.singleton(classType)).when(lookup).findClassTypesByPattern(eq(className), anyBoolean());

			Collection<? extends ImmutableClassType> result = narrower.narrowByClassSensorAssignment(classCache, methodSensorAssignment);

			assertThat(result, hasSize(1));
			assertThat(result.iterator().next(), is((ImmutableClassType) classType));

			verify(lookup, times(1)).findClassTypesByPattern(className, true);
			verifyNoMoreInteractions(lookup);
		}

		@Test
		public void initializedByInterface() {
			String interfaceName = "info.novatec.MyClass";
			when(methodSensorAssignment.getClassName()).thenReturn(interfaceName);
			when(methodSensorAssignment.isInterf()).thenReturn(true);
			when(methodSensorAssignment.isSuperclass()).thenReturn(false);
			doReturn(Collections.singleton(interfaceType)).when(lookup).findInterfaceTypesByPattern(eq(interfaceName), anyBoolean());
			doReturn(Collections.singleton(classType)).when(interfaceType).getImmutableRealizingClasses();
			when(classType.isInitialized()).thenReturn(true);

			Collection<? extends ImmutableClassType> result = narrower.narrowByClassSensorAssignment(classCache, methodSensorAssignment);

			assertThat(result, hasSize(1));
			assertThat(result.iterator().next(), is((ImmutableClassType) classType));

			verify(lookup, times(1)).findInterfaceTypesByPattern(interfaceName, false);
			verifyNoMoreInteractions(lookup);
		}

		@Test
		public void nonInitializedByInterface() {
			String interfaceName = "info.novatec.MyClass";
			when(methodSensorAssignment.getClassName()).thenReturn(interfaceName);
			when(methodSensorAssignment.isInterf()).thenReturn(true);
			when(methodSensorAssignment.isSuperclass()).thenReturn(false);
			doReturn(Collections.singleton(interfaceType)).when(lookup).findInterfaceTypesByPattern(eq(interfaceName), anyBoolean());
			doReturn(Collections.singleton(classType)).when(interfaceType).getImmutableRealizingClasses();
			when(classType.isInitialized()).thenReturn(false);

			Collection<? extends ImmutableClassType> result = narrower.narrowByClassSensorAssignment(classCache, methodSensorAssignment);

			assertThat(result, is(empty()));

			verify(lookup, times(1)).findInterfaceTypesByPattern(interfaceName, false);
			verifyNoMoreInteractions(lookup);
		}

		@Test
		public void initializedByInterfaceIndirect() {
			String interfaceName = "info.novatec.MyClass";
			when(methodSensorAssignment.getClassName()).thenReturn(interfaceName);
			when(methodSensorAssignment.isInterf()).thenReturn(true);
			when(methodSensorAssignment.isSuperclass()).thenReturn(false);
			InterfaceType indirectInterfaceType = mock(InterfaceType.class);
			doReturn(Collections.singleton(interfaceType)).when(lookup).findInterfaceTypesByPattern(eq(interfaceName), anyBoolean());
			doReturn(Collections.singleton(indirectInterfaceType)).when(interfaceType).getImmutableSubInterfaces();
			doReturn(Collections.singleton(classType)).when(indirectInterfaceType).getImmutableRealizingClasses();
			when(classType.isInitialized()).thenReturn(true);

			Collection<? extends ImmutableClassType> result = narrower.narrowByClassSensorAssignment(classCache, methodSensorAssignment);

			assertThat(result, hasSize(1));
			assertThat(result.iterator().next(), is((ImmutableClassType) classType));
			assertThat(result.iterator().next(), is((ImmutableClassType) classType));

			verify(lookup, times(1)).findInterfaceTypesByPattern(interfaceName, false);
			verifyNoMoreInteractions(lookup);
		}

		@Test
		public void nonInitializedByInterfaceIndirect() {
			String interfaceName = "info.novatec.MyClass";
			when(methodSensorAssignment.getClassName()).thenReturn(interfaceName);
			when(methodSensorAssignment.isInterf()).thenReturn(true);
			when(methodSensorAssignment.isSuperclass()).thenReturn(false);
			InterfaceType indirectInterfaceType = mock(InterfaceType.class);
			doReturn(Collections.singleton(interfaceType)).when(lookup).findInterfaceTypesByPattern(eq(interfaceName), anyBoolean());
			doReturn(Collections.singleton(indirectInterfaceType)).when(interfaceType).getImmutableSubInterfaces();
			doReturn(Collections.singleton(classType)).when(indirectInterfaceType).getImmutableRealizingClasses();
			when(classType.isInitialized()).thenReturn(false);

			Collection<? extends ImmutableClassType> result = narrower.narrowByClassSensorAssignment(classCache, methodSensorAssignment);

			assertThat(result, is(empty()));

			verify(lookup, times(1)).findInterfaceTypesByPattern(interfaceName, false);
			verifyNoMoreInteractions(lookup);
		}

		@Test
		public void initializedBySuperClass() {
			String superClassName = "info.novatec.MyClass";
			when(methodSensorAssignment.getClassName()).thenReturn(superClassName);
			when(methodSensorAssignment.isInterf()).thenReturn(false);
			when(methodSensorAssignment.isSuperclass()).thenReturn(true);
			doReturn(Collections.singleton(superClassType)).when(lookup).findClassTypesByPattern(eq(superClassName), anyBoolean());
			doReturn(Collections.singleton(classType)).when(superClassType).getImmutableSubClasses();
			when(classType.isInitialized()).thenReturn(true);

			Collection<? extends ImmutableClassType> result = narrower.narrowByClassSensorAssignment(classCache, methodSensorAssignment);

			assertThat(result, hasSize(1));
			assertThat(result.iterator().next(), is((ImmutableClassType) classType));

			verify(lookup, times(1)).findClassTypesByPattern(superClassName, false);
			verifyNoMoreInteractions(lookup);
		}

		@Test
		public void nonInitializedBySuperClass() {
			String superClassName = "info.novatec.MyClass";
			when(methodSensorAssignment.getClassName()).thenReturn(superClassName);
			when(methodSensorAssignment.isInterf()).thenReturn(false);
			when(methodSensorAssignment.isSuperclass()).thenReturn(true);
			doReturn(Collections.singleton(superClassType)).when(lookup).findClassTypesByPattern(eq(superClassName), anyBoolean());
			doReturn(Collections.singleton(classType)).when(superClassType).getImmutableSubClasses();
			when(classType.isInitialized()).thenReturn(false);

			Collection<? extends ImmutableClassType> result = narrower.narrowByClassSensorAssignment(classCache, methodSensorAssignment);

			assertThat(result, is(empty()));

			verify(lookup, times(1)).findClassTypesByPattern(superClassName, false);
			verifyNoMoreInteractions(lookup);
		}

		@Test
		public void initializedBySuperClassIndirect() {
			String superClassName = "info.novatec.MyClass";
			when(methodSensorAssignment.getClassName()).thenReturn(superClassName);
			when(methodSensorAssignment.isInterf()).thenReturn(false);
			when(methodSensorAssignment.isSuperclass()).thenReturn(true);
			ClassType indirectSuperClassType = mock(ClassType.class);
			doReturn(Collections.singleton(superClassType)).when(lookup).findClassTypesByPattern(eq(superClassName), anyBoolean());
			doReturn(Collections.singleton(indirectSuperClassType)).when(superClassType).getImmutableSubClasses();
			doReturn(Collections.singleton(classType)).when(indirectSuperClassType).getImmutableSubClasses();
			when(classType.isInitialized()).thenReturn(true);
			when(superClassType.isInitialized()).thenReturn(false);

			Collection<? extends ImmutableClassType> result = narrower.narrowByClassSensorAssignment(classCache, methodSensorAssignment);

			assertThat(result, hasSize(1));
			assertThat(result.iterator().next(), is((ImmutableClassType) classType));

			verify(lookup, times(1)).findClassTypesByPattern(superClassName, false);
			verifyNoMoreInteractions(lookup);
		}

		@Test
		public void initializedBySuperClassIndirectBoth() {
			String superClassName = "info.novatec.MyClass";
			when(methodSensorAssignment.getClassName()).thenReturn(superClassName);
			when(methodSensorAssignment.isInterf()).thenReturn(false);
			when(methodSensorAssignment.isSuperclass()).thenReturn(true);
			ClassType indirectSuperClassType = mock(ClassType.class);
			doReturn(Collections.singleton(superClassType)).when(lookup).findClassTypesByPattern(eq(superClassName), anyBoolean());
			doReturn(Collections.singleton(indirectSuperClassType)).when(superClassType).getImmutableSubClasses();
			doReturn(Collections.singleton(classType)).when(indirectSuperClassType).getImmutableSubClasses();
			when(classType.isInitialized()).thenReturn(true);
			when(indirectSuperClassType.isInitialized()).thenReturn(true);

			Collection<? extends ImmutableClassType> result = narrower.narrowByClassSensorAssignment(classCache, methodSensorAssignment);

			assertThat(result, hasSize(2));
			assertThat((Collection<ImmutableClassType>) result, hasItem((ImmutableClassType) classType));
			assertThat((Collection<ImmutableClassType>) result, hasItem((ImmutableClassType) indirectSuperClassType));

			verify(lookup, times(1)).findClassTypesByPattern(superClassName, false);
			verifyNoMoreInteractions(lookup);
		}

		@Test
		public void nonInitializedBySuperClassIndirect() {
			String superClassName = "info.novatec.MyClass";
			when(methodSensorAssignment.getClassName()).thenReturn(superClassName);
			when(methodSensorAssignment.isInterf()).thenReturn(false);
			when(methodSensorAssignment.isSuperclass()).thenReturn(true);
			ClassType indirectSuperClassType = mock(ClassType.class);
			doReturn(Collections.singleton(superClassType)).when(lookup).findClassTypesByPattern(eq(superClassName), anyBoolean());
			doReturn(Collections.singleton(indirectSuperClassType)).when(superClassType).getImmutableSubClasses();
			doReturn(Collections.singleton(classType)).when(indirectSuperClassType).getImmutableSubClasses();
			when(classType.isInitialized()).thenReturn(false);
			when(superClassType.isInitialized()).thenReturn(false);

			Collection<? extends ImmutableClassType> result = narrower.narrowByClassSensorAssignment(classCache, methodSensorAssignment);

			assertThat(result, is(empty()));

			verify(lookup, times(1)).findClassTypesByPattern(superClassName, false);
			verifyNoMoreInteractions(lookup);
		}

		@Test
		public void initializedByAnnotation() {
			String className = "*";
			String annotationName = "info.novatec.MyAnnotation";
			when(methodSensorAssignment.getClassName()).thenReturn(className);
			when(methodSensorAssignment.isInterf()).thenReturn(false);
			when(methodSensorAssignment.isSuperclass()).thenReturn(false);
			when(methodSensorAssignment.getAnnotation()).thenReturn(annotationName);
			doReturn(Collections.singleton(annotationType)).when(lookup).findAnnotationTypesByPattern(eq(annotationName), anyBoolean());
			doReturn(Collections.singleton(classType)).when(annotationType).getImmutableAnnotatedTypes();
			when(classType.isInitialized()).thenReturn(true);

			Collection<? extends ImmutableClassType> result = narrower.narrowByClassSensorAssignment(classCache, methodSensorAssignment);

			assertThat(result, hasSize(1));
			assertThat(result.iterator().next(), is((ImmutableClassType) classType));

			verify(lookup, times(1)).findAnnotationTypesByPattern(annotationName, false);
			verifyNoMoreInteractions(lookup);
		}

		@Test
		public void nonInitializedByAnnotation() {
			String className = "*";
			String annotationName = "info.novatec.MyAnnotation";
			when(methodSensorAssignment.getClassName()).thenReturn(className);
			when(methodSensorAssignment.isInterf()).thenReturn(false);
			when(methodSensorAssignment.isSuperclass()).thenReturn(false);
			when(methodSensorAssignment.getAnnotation()).thenReturn(annotationName);
			doReturn(Collections.singleton(annotationType)).when(lookup).findAnnotationTypesByPattern(eq(annotationName), anyBoolean());
			doReturn(Collections.singleton(classType)).when(annotationType).getImmutableAnnotatedTypes();
			when(classType.isInitialized()).thenReturn(false);

			Collection<? extends ImmutableClassType> result = narrower.narrowByClassSensorAssignment(classCache, methodSensorAssignment);

			assertThat(result, is(empty()));

			verify(lookup, times(1)).findAnnotationTypesByPattern(annotationName, false);
			verifyNoMoreInteractions(lookup);
		}

		@Test
		public void initializedBySuperClassAnnotation() {
			String className = "*";
			String annotationName = "info.novatec.MyAnnotation";
			when(methodSensorAssignment.getClassName()).thenReturn(className);
			when(methodSensorAssignment.isInterf()).thenReturn(false);
			when(methodSensorAssignment.isSuperclass()).thenReturn(false);
			when(methodSensorAssignment.getAnnotation()).thenReturn(annotationName);
			doReturn(Collections.singleton(annotationType)).when(lookup).findAnnotationTypesByPattern(eq(annotationName), anyBoolean());
			doReturn(Collections.singleton(superClassType)).when(annotationType).getImmutableAnnotatedTypes();
			doReturn(Collections.singleton(classType)).when(superClassType).getImmutableSubClasses();
			when(classType.isInitialized()).thenReturn(true);
			when(superClassType.isInitialized()).thenReturn(false);

			Collection<? extends ImmutableClassType> result = narrower.narrowByClassSensorAssignment(classCache, methodSensorAssignment);

			assertThat(result, hasSize(1));
			assertThat(result.iterator().next(), is((ImmutableClassType) classType));

			verify(lookup, times(1)).findAnnotationTypesByPattern(annotationName, false);
			verifyNoMoreInteractions(lookup);
		}

		@Test
		public void nonInitializedBySuperClassAnnotation() {
			String className = "*";
			String annotationName = "info.novatec.MyAnnotation";
			when(methodSensorAssignment.getClassName()).thenReturn(className);
			when(methodSensorAssignment.isInterf()).thenReturn(false);
			when(methodSensorAssignment.isSuperclass()).thenReturn(false);
			when(methodSensorAssignment.getAnnotation()).thenReturn(annotationName);
			doReturn(Collections.singleton(annotationType)).when(lookup).findAnnotationTypesByPattern(eq(annotationName), anyBoolean());
			doReturn(Collections.singleton(superClassType)).when(annotationType).getImmutableAnnotatedTypes();
			doReturn(Collections.singleton(classType)).when(superClassType).getImmutableSubClasses();
			when(classType.isInitialized()).thenReturn(false);
			when(superClassType.isInitialized()).thenReturn(false);

			Collection<? extends ImmutableClassType> result = narrower.narrowByClassSensorAssignment(classCache, methodSensorAssignment);

			assertThat(result, is(empty()));

			verify(lookup, times(1)).findAnnotationTypesByPattern(annotationName, false);
			verifyNoMoreInteractions(lookup);
		}

		@Test
		public void initializedByInterfaceAnnotation() {
			String className = "*";
			String annotationName = "info.novatec.MyAnnotation";
			when(methodSensorAssignment.getClassName()).thenReturn(className);
			when(methodSensorAssignment.isInterf()).thenReturn(false);
			when(methodSensorAssignment.isSuperclass()).thenReturn(false);
			when(methodSensorAssignment.getAnnotation()).thenReturn(annotationName);
			doReturn(Collections.singleton(annotationType)).when(lookup).findAnnotationTypesByPattern(eq(annotationName), anyBoolean());
			doReturn(Collections.singleton(interfaceType)).when(annotationType).getImmutableAnnotatedTypes();
			doReturn(Collections.singleton(classType)).when(interfaceType).getImmutableRealizingClasses();
			when(classType.isInitialized()).thenReturn(true);

			Collection<? extends ImmutableClassType> result = narrower.narrowByClassSensorAssignment(classCache, methodSensorAssignment);

			assertThat(result, hasSize(1));
			assertThat(result.iterator().next(), is((ImmutableClassType) classType));

			verify(lookup, times(1)).findAnnotationTypesByPattern(annotationName, false);
			verifyNoMoreInteractions(lookup);
		}

		@Test
		public void nonInitializedByInterfaceAnnotation() {
			String className = "*";
			String annotationName = "info.novatec.MyAnnotation";
			when(methodSensorAssignment.getClassName()).thenReturn(className);
			when(methodSensorAssignment.isInterf()).thenReturn(false);
			when(methodSensorAssignment.isSuperclass()).thenReturn(false);
			when(methodSensorAssignment.getAnnotation()).thenReturn(annotationName);
			doReturn(Collections.singleton(annotationType)).when(lookup).findAnnotationTypesByPattern(eq(annotationName), anyBoolean());
			doReturn(Collections.singleton(interfaceType)).when(annotationType).getImmutableAnnotatedTypes();
			doReturn(Collections.singleton(classType)).when(interfaceType).getImmutableRealizingClasses();
			when(classType.isInitialized()).thenReturn(false);

			Collection<? extends ImmutableClassType> result = narrower.narrowByClassSensorAssignment(classCache, methodSensorAssignment);

			assertThat(result, is(empty()));

			verify(lookup, times(1)).findAnnotationTypesByPattern(annotationName, false);
			verifyNoMoreInteractions(lookup);
		}

		@Test
		public void initializedByMethodAnnotation() {
			String className = "*";
			String annotationName = "info.novatec.MyAnnotation";
			when(methodSensorAssignment.getClassName()).thenReturn(className);
			when(methodSensorAssignment.isInterf()).thenReturn(false);
			when(methodSensorAssignment.isSuperclass()).thenReturn(false);
			when(methodSensorAssignment.getAnnotation()).thenReturn(annotationName);
			doReturn(Collections.singleton(annotationType)).when(lookup).findAnnotationTypesByPattern(eq(annotationName), anyBoolean());
			doReturn(Collections.singleton(methodType)).when(annotationType).getImmutableAnnotatedTypes();
			doReturn(classType).when(methodType).getImmutableClassOrInterfaceType();
			when(classType.isInitialized()).thenReturn(true);

			Collection<? extends ImmutableClassType> result = narrower.narrowByClassSensorAssignment(classCache, methodSensorAssignment);

			assertThat(result, hasSize(1));
			assertThat(result.iterator().next(), is((ImmutableClassType) classType));

			verify(lookup, times(1)).findAnnotationTypesByPattern(annotationName, false);
			verifyNoMoreInteractions(lookup);
		}

		@Test
		public void nonInitializedByMethodAnnotation() {
			String className = "*";
			String annotationName = "info.novatec.MyAnnotation";
			when(methodSensorAssignment.getClassName()).thenReturn(className);
			when(methodSensorAssignment.isInterf()).thenReturn(false);
			when(methodSensorAssignment.isSuperclass()).thenReturn(false);
			when(methodSensorAssignment.getAnnotation()).thenReturn(annotationName);
			doReturn(Collections.singleton(annotationType)).when(lookup).findAnnotationTypesByPattern(eq(annotationName), anyBoolean());
			doReturn(Collections.singleton(methodType)).when(annotationType).getImmutableAnnotatedTypes();
			doReturn(classType).when(methodType).getImmutableClassOrInterfaceType();
			when(classType.isInitialized()).thenReturn(false);

			Collection<? extends ImmutableClassType> result = narrower.narrowByClassSensorAssignment(classCache, methodSensorAssignment);

			assertThat(result, is(empty()));

			verify(lookup, times(1)).findAnnotationTypesByPattern(annotationName, false);
			verifyNoMoreInteractions(lookup);
		}
	}

}
