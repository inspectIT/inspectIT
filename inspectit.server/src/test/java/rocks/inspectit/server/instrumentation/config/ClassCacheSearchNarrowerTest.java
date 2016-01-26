package rocks.inspectit.server.instrumentation.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.instrumentation.classcache.ClassCache;
import rocks.inspectit.server.instrumentation.classcache.ClassCacheLookup;
import rocks.inspectit.shared.all.instrumentation.classcache.AnnotationType;
import rocks.inspectit.shared.all.instrumentation.classcache.ClassType;
import rocks.inspectit.shared.all.instrumentation.classcache.ImmutableClassType;
import rocks.inspectit.shared.all.instrumentation.classcache.InterfaceType;
import rocks.inspectit.shared.all.instrumentation.classcache.MethodType;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.assignment.impl.ExceptionSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.MethodSensorAssignment;

@SuppressWarnings({ "PMD", "unchecked" })
public class ClassCacheSearchNarrowerTest extends TestBase {

	@InjectMocks
	ClassCacheSearchNarrower narrower;

	@Mock
	MethodSensorAssignment methodSensorAssignment;

	@Mock
	ExceptionSensorAssignment exceptionSensorAssignment;

	@Mock
	ClassCache classCache;

	@Mock
	ClassCacheLookup lookup;

	@BeforeMethod
	public void setup() {
		when(classCache.getLookupService()).thenReturn(lookup);
	}

	public class NarrowByClassSensorAssignment extends ClassCacheSearchNarrowerTest {

		@Test
		public void byDirectName() {
			String className = "info.novatec.MyClass";
			when(methodSensorAssignment.getClassName()).thenReturn(className);
			when(methodSensorAssignment.isInterf()).thenReturn(false);
			when(methodSensorAssignment.isSuperclass()).thenReturn(false);
			ClassType classType = new ClassType(className, "hash", 0);
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
			InterfaceType interfaceType = new InterfaceType(interfaceName);
			ClassType classType = new ClassType("initialized", "hash", 0);
			classType.addInterface(interfaceType);
			doReturn(Collections.singleton(interfaceType)).when(lookup).findInterfaceTypesByPattern(eq(interfaceName), anyBoolean());

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
			InterfaceType interfaceType = new InterfaceType(interfaceName);
			ClassType classType = new ClassType("not-initialized");
			classType.addInterface(interfaceType);
			doReturn(Collections.singleton(interfaceType)).when(lookup).findInterfaceTypesByPattern(eq(interfaceName), anyBoolean());

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
			InterfaceType interfaceType = new InterfaceType(interfaceName);
			InterfaceType indirectInterfaceType = new InterfaceType("indirectInterfaceType");
			indirectInterfaceType.addSuperInterface(interfaceType);
			ClassType classType = new ClassType("initialized", "hash", 0);
			classType.addInterface(indirectInterfaceType);
			doReturn(Collections.singleton(interfaceType)).when(lookup).findInterfaceTypesByPattern(eq(interfaceName), anyBoolean());

			Collection<? extends ImmutableClassType> result = narrower.narrowByClassSensorAssignment(classCache, methodSensorAssignment);

			assertThat(result, hasSize(1));
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
			InterfaceType interfaceType = new InterfaceType(interfaceName);
			InterfaceType indirectInterfaceType = new InterfaceType("indirectInterfaceType");
			indirectInterfaceType.addSuperInterface(interfaceType);
			ClassType classType = new ClassType("non-initialized");
			classType.addInterface(indirectInterfaceType);
			doReturn(Collections.singleton(interfaceType)).when(lookup).findInterfaceTypesByPattern(eq(interfaceName), anyBoolean());

			Collection<? extends ImmutableClassType> result = narrower.narrowByClassSensorAssignment(classCache, methodSensorAssignment);

			assertThat(result, is(empty()));

			verify(lookup, times(1)).findInterfaceTypesByPattern(interfaceName, false);
			verifyNoMoreInteractions(lookup);
		}

		@Test
		public void initializedBySuperClassInterface() {
			String interfaceName = "info.novatec.MyClass";
			when(methodSensorAssignment.getClassName()).thenReturn(interfaceName);
			when(methodSensorAssignment.isInterf()).thenReturn(true);
			when(methodSensorAssignment.isSuperclass()).thenReturn(false);
			InterfaceType interfaceType = new InterfaceType(interfaceName);
			ClassType superClassType = new ClassType("superclass", "superhash", 0);
			ClassType classType = new ClassType("initialized", "hash", 0);
			classType.addSuperClass(superClassType);
			superClassType.addInterface(interfaceType);
			doReturn(Collections.singleton(interfaceType)).when(lookup).findInterfaceTypesByPattern(eq(interfaceName), anyBoolean());

			Collection<? extends ImmutableClassType> result = narrower.narrowByClassSensorAssignment(classCache, methodSensorAssignment);

			assertThat(result, hasSize(2));
			assertThat((Collection<ClassType>) result, hasItem(classType));
			assertThat((Collection<ClassType>) result, hasItem(superClassType));

			verify(lookup, times(1)).findInterfaceTypesByPattern(interfaceName, false);
			verifyNoMoreInteractions(lookup);
		}

		@Test
		public void initializedBySuperClassInterfaceIndirect() {
			String interfaceName = "info.novatec.MyClass";
			when(methodSensorAssignment.getClassName()).thenReturn(interfaceName);
			when(methodSensorAssignment.isInterf()).thenReturn(true);
			when(methodSensorAssignment.isSuperclass()).thenReturn(false);
			InterfaceType interfaceType = new InterfaceType(interfaceName);
			InterfaceType indirectInterfaceType = new InterfaceType("indirectInterfaceType");
			indirectInterfaceType.addSuperInterface(interfaceType);
			ClassType superClassType = new ClassType("superclass", "superhash", 0);
			ClassType classType = new ClassType("initialized", "hash", 0);
			classType.addSuperClass(superClassType);
			superClassType.addInterface(interfaceType);
			doReturn(Collections.singleton(interfaceType)).when(lookup).findInterfaceTypesByPattern(eq(interfaceName), anyBoolean());

			Collection<? extends ImmutableClassType> result = narrower.narrowByClassSensorAssignment(classCache, methodSensorAssignment);

			assertThat(result, hasSize(2));
			assertThat((Collection<ClassType>) result, hasItem(classType));
			assertThat((Collection<ClassType>) result, hasItem(superClassType));

			verify(lookup, times(1)).findInterfaceTypesByPattern(interfaceName, false);
			verifyNoMoreInteractions(lookup);
		}

		@Test
		public void initializedBySuperClassInterfaceAndInterfaceIndirect() {
			String interfaceName = "info.novatec.MyClass";
			when(methodSensorAssignment.getClassName()).thenReturn(interfaceName);
			when(methodSensorAssignment.isInterf()).thenReturn(true);
			when(methodSensorAssignment.isSuperclass()).thenReturn(false);
			InterfaceType interfaceType = new InterfaceType(interfaceName);
			InterfaceType indirectInterfaceType = new InterfaceType("indirectInterfaceType");
			indirectInterfaceType.addSuperInterface(interfaceType);
			ClassType superClassType = new ClassType("superclass", "superhash", 0);
			ClassType classType = new ClassType("initialized", "hash", 0);
			classType.addSuperClass(superClassType);
			classType.addInterface(indirectInterfaceType);
			superClassType.addInterface(interfaceType);
			doReturn(Collections.singleton(interfaceType)).when(lookup).findInterfaceTypesByPattern(eq(interfaceName), anyBoolean());

			Collection<? extends ImmutableClassType> result = narrower.narrowByClassSensorAssignment(classCache, methodSensorAssignment);

			assertThat(result, hasSize(2));
			assertThat((Collection<ClassType>) result, hasItem(classType));
			assertThat((Collection<ClassType>) result, hasItem(superClassType));

			verify(lookup, times(1)).findInterfaceTypesByPattern(interfaceName, false);
			verifyNoMoreInteractions(lookup);
		}

		@Test
		public void initializedBySuperClass() {
			String superClassName = "info.novatec.MyClass";
			when(methodSensorAssignment.getClassName()).thenReturn(superClassName);
			when(methodSensorAssignment.isInterf()).thenReturn(false);
			when(methodSensorAssignment.isSuperclass()).thenReturn(true);
			ClassType superClassType = new ClassType(superClassName);
			ClassType classType = new ClassType("initialized", "hash", 0);
			classType.addSuperClass(superClassType);
			doReturn(Collections.singleton(superClassType)).when(lookup).findClassTypesByPattern(eq(superClassName), anyBoolean());

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
			ClassType superClassType = new ClassType(superClassName);
			ClassType classType = new ClassType("non-initialized");
			classType.addSuperClass(superClassType);
			doReturn(Collections.singleton(superClassType)).when(lookup).findClassTypesByPattern(eq(superClassName), anyBoolean());

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
			ClassType superClassType = new ClassType(superClassName);
			ClassType indirectSuperClassType = new ClassType("indirectSuperClassType");
			indirectSuperClassType.addSuperClass(superClassType);
			ClassType classType = new ClassType("initialized", "hash", 0);
			classType.addSuperClass(indirectSuperClassType);
			doReturn(Collections.singleton(superClassType)).when(lookup).findClassTypesByPattern(eq(superClassName), anyBoolean());

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
			ClassType superClassType = new ClassType(superClassName);
			ClassType indirectSuperClassType = new ClassType("indirectSuperClassType", "hash", 0);
			indirectSuperClassType.addSuperClass(superClassType);
			ClassType classType = new ClassType("initialized", "hash", 0);
			classType.addSuperClass(indirectSuperClassType);
			doReturn(Collections.singleton(superClassType)).when(lookup).findClassTypesByPattern(eq(superClassName), anyBoolean());

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
			ClassType superClassType = new ClassType(superClassName);
			ClassType indirectSuperClassType = new ClassType("indirectSuperClassType");
			indirectSuperClassType.addSuperClass(superClassType);
			ClassType classType = new ClassType("non-initialized");
			classType.addSuperClass(indirectSuperClassType);
			doReturn(Collections.singleton(superClassType)).when(lookup).findClassTypesByPattern(eq(superClassName), anyBoolean());

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
			AnnotationType annotationType = new AnnotationType(annotationName);
			ClassType classType = new ClassType("initialized", "hash", 0);
			classType.addAnnotation(annotationType);
			doReturn(Collections.singleton(annotationType)).when(lookup).findAnnotationTypesByPattern(eq(annotationName), anyBoolean());

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
			AnnotationType annotationType = new AnnotationType(annotationName);
			ClassType classType = new ClassType("non-initialized");
			classType.addAnnotation(annotationType);
			doReturn(Collections.singleton(annotationType)).when(lookup).findAnnotationTypesByPattern(eq(annotationName), anyBoolean());

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
			AnnotationType annotationType = new AnnotationType(annotationName);
			ClassType superClassType = new ClassType("superClass");
			superClassType.addAnnotation(annotationType);
			ClassType classType = new ClassType("initialized", "hash", 0);
			classType.addSuperClass(superClassType);
			doReturn(Collections.singleton(annotationType)).when(lookup).findAnnotationTypesByPattern(eq(annotationName), anyBoolean());

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
			AnnotationType annotationType = new AnnotationType(annotationName);
			ClassType superClassType = new ClassType("superClass");
			superClassType.addAnnotation(annotationType);
			ClassType classType = new ClassType("non-initialized");
			classType.addSuperClass(superClassType);
			doReturn(Collections.singleton(annotationType)).when(lookup).findAnnotationTypesByPattern(eq(annotationName), anyBoolean());

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
			AnnotationType annotationType = new AnnotationType(annotationName);
			InterfaceType interfaceType = new InterfaceType("interface");
			interfaceType.addAnnotation(annotationType);
			ClassType classType = new ClassType("initialized", "hash", 0);
			classType.addInterface(interfaceType);
			doReturn(Collections.singleton(annotationType)).when(lookup).findAnnotationTypesByPattern(eq(annotationName), anyBoolean());

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
			AnnotationType annotationType = new AnnotationType(annotationName);
			InterfaceType interfaceType = new InterfaceType("interface");
			interfaceType.addAnnotation(annotationType);
			ClassType classType = new ClassType("non-initialized");
			classType.addInterface(interfaceType);
			doReturn(Collections.singleton(annotationType)).when(lookup).findAnnotationTypesByPattern(eq(annotationName), anyBoolean());

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
			AnnotationType annotationType = new AnnotationType(annotationName);
			MethodType methodType = new MethodType();
			methodType.addAnnotation(annotationType);
			ClassType classType = new ClassType("initialized", "hash", 0);
			classType.addMethod(methodType);
			doReturn(Collections.singleton(annotationType)).when(lookup).findAnnotationTypesByPattern(eq(annotationName), anyBoolean());

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
			AnnotationType annotationType = new AnnotationType(annotationName);
			MethodType methodType = new MethodType();
			methodType.addAnnotation(annotationType);
			ClassType classType = new ClassType("non-initialized");
			classType.addMethod(methodType);
			doReturn(Collections.singleton(annotationType)).when(lookup).findAnnotationTypesByPattern(eq(annotationName), anyBoolean());

			Collection<? extends ImmutableClassType> result = narrower.narrowByClassSensorAssignment(classCache, methodSensorAssignment);

			assertThat(result, is(empty()));

			verify(lookup, times(1)).findAnnotationTypesByPattern(annotationName, false);
			verifyNoMoreInteractions(lookup);
		}
	}

}
