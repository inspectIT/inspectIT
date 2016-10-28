package rocks.inspectit.server.instrumentation.classcache;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;

import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.instrumentation.classcache.index.FqnIndexer;
import rocks.inspectit.server.instrumentation.classcache.index.HashIndexer;
import rocks.inspectit.shared.all.instrumentation.classcache.ImmutableAnnotationType;
import rocks.inspectit.shared.all.instrumentation.classcache.ImmutableClassType;
import rocks.inspectit.shared.all.instrumentation.classcache.ImmutableInterfaceType;
import rocks.inspectit.shared.all.instrumentation.classcache.ImmutableType;
import rocks.inspectit.shared.all.instrumentation.classcache.Type;
import rocks.inspectit.shared.all.pattern.IMatchPattern;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * Test for the {@link ClassCacheLookup}.
 *
 * @author Ivan Senic
 *
 */
@SuppressWarnings("PMD")
public class ClassCacheLookupTest extends TestBase {

	protected final Answer<Collection<Type>> typeSearchAnswer = new Answer<Collection<Type>>() {
		@Override
		public Collection<Type> answer(InvocationOnMock invocation) throws Throwable {
			Collection<Type> types = new ArrayList<>();
			types.add(type);
			return types;
		}
	};

	@InjectMocks
	ClassCacheLookup lookup;

	@Mock
	Logger log;

	@Mock
	ClassCache classCache;

	@Mock
	FqnIndexer<Type> fqnIndexer;

	@Mock
	HashIndexer hashIndexer;

	@Mock
	Type type;

	@Mock
	ImmutableClassType classType;

	@Mock
	ImmutableInterfaceType interfaceType;

	@Mock
	ImmutableAnnotationType annotationType;

	@BeforeMethod
	public void setup() throws Exception {
		Answer<Object> callableAnswer = new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				Callable<?> callable = (Callable<?>) invocation.getArguments()[0];
				return callable.call();
			}
		};
		doAnswer(callableAnswer).when(classCache).executeWithReadLock(Matchers.<Callable<?>> anyObject());
		doAnswer(callableAnswer).when(classCache).executeWithWriteLock(Matchers.<Callable<?>> anyObject());

		lookup.init(classCache);
		verify(classCache, times(1)).registerNodeChangeListener(fqnIndexer);
		verify(classCache, times(1)).registerNodeChangeListener(hashIndexer);
	}

	public class FindByFqn extends ClassCacheLookupTest {

		@Test
		public void find() throws Exception {
			String fqn = "fqn";
			when(fqnIndexer.lookup(fqn)).thenReturn(type);

			ImmutableType type = lookup.findByFQN(fqn);

			assertThat((Type) type, is(type));

			verify(fqnIndexer, times(1)).lookup(fqn);
			verify(classCache, times(1)).executeWithReadLock(Matchers.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}
	}

	public class FindByHash extends ClassCacheLookupTest {

		@Test
		public void find() throws Exception {
			String hash = "hash";
			when(hashIndexer.lookup(hash)).thenReturn(type);

			ImmutableType type = lookup.findByHash(hash);

			assertThat((Type) type, is(type));

			verify(hashIndexer, times(1)).lookup(hash);
			verify(classCache, times(1)).executeWithReadLock(Matchers.<Callable<?>> anyObject());
			verifyNoMoreInteractions(hashIndexer, classCache);
			verifyZeroInteractions(fqnIndexer);
		}
	}

	public class FindByPattern extends ClassCacheLookupTest {

		@Test
		public void initializedOnlyInitializedTypes() throws Exception {
			String pattern = "pattern";
			when(fqnIndexer.findByPattern(Matchers.<IMatchPattern> any())).thenAnswer(typeSearchAnswer);
			when(type.isInitialized()).thenReturn(true);

			Collection<? extends ImmutableType> intializedTypes = lookup.findByPattern(pattern, true);

			assertThat(intializedTypes, hasSize(1));

			verify(fqnIndexer, times(1)).findByPattern(Matchers.<IMatchPattern> any());
			verify(classCache, times(1)).executeWithReadLock(Matchers.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}

		@Test
		public void initializedAllTypes() throws Exception {
			String pattern = "pattern";
			when(fqnIndexer.findByPattern(Matchers.<IMatchPattern> any())).thenAnswer(typeSearchAnswer);
			when(type.isInitialized()).thenReturn(true);

			Collection<? extends ImmutableType> nonInitializedTypes = lookup.findByPattern(pattern, false);

			assertThat(nonInitializedTypes, hasSize(1));

			verify(fqnIndexer, times(1)).findByPattern(Matchers.<IMatchPattern> any());
			verify(classCache, times(1)).executeWithReadLock(Matchers.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}

		@Test
		public void notInitializedOnlyInitializedTypes() throws Exception {
			String pattern = "pattern";
			when(fqnIndexer.findByPattern(Matchers.<IMatchPattern> any())).thenAnswer(typeSearchAnswer);
			when(type.isInitialized()).thenReturn(false);

			Collection<? extends ImmutableType> intializedTypes = lookup.findByPattern(pattern, true);

			assertThat(intializedTypes, is(empty()));

			verify(fqnIndexer, times(1)).findByPattern(Matchers.<IMatchPattern> any());
			verify(classCache, times(1)).executeWithReadLock(Matchers.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}

		@Test
		public void notInitializedAllTypes() throws Exception {
			String pattern = "pattern";
			when(fqnIndexer.findByPattern(Matchers.<IMatchPattern> any())).thenAnswer(typeSearchAnswer);
			when(type.isInitialized()).thenReturn(false);

			Collection<? extends ImmutableType> nonInitializedTypes = lookup.findByPattern(pattern, false);

			assertThat(nonInitializedTypes, hasSize(1));

			verify(fqnIndexer, times(1)).findByPattern(Matchers.<IMatchPattern> any());
			verify(classCache, times(1)).executeWithReadLock(Matchers.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}

		@Test
		public void notFound() throws Exception {
			when(fqnIndexer.findByPattern(Matchers.<IMatchPattern> any())).thenReturn(Collections.<Type> emptyList());
			when(type.isInitialized()).thenReturn(true);

			Collection<? extends ImmutableType> types = lookup.findByPattern("somethingElse", false);

			assertThat(types, is(empty()));

			verify(fqnIndexer, times(1)).findByPattern(Matchers.<IMatchPattern> any());
			verify(classCache, times(1)).executeWithReadLock(Matchers.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}
	}

	public class ClassTypesByPattern extends ClassCacheLookupTest {

		@Test
		public void initializedOnlyInitializedTypes() throws Exception {
			String pattern = "pat*tern";
			when(fqnIndexer.findByPattern(Matchers.<IMatchPattern> any())).thenAnswer(typeSearchAnswer);
			when(type.getFQN()).thenReturn("pattttern");
			when(type.isClass()).thenReturn(true);
			when(type.castToClass()).thenReturn(classType);
			when(type.isInitialized()).thenReturn(true);

			Collection<? extends ImmutableClassType> initializedTypes = lookup.findClassTypesByPattern(pattern, true);

			assertThat(initializedTypes, hasSize(1));

			verify(fqnIndexer, times(1)).findByPattern(Matchers.<IMatchPattern> any());
			verify(classCache, times(1)).executeWithReadLock(Matchers.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}

		@Test
		public void initializedAllTypes() throws Exception {
			String pattern = "pat*tern";
			when(fqnIndexer.findByPattern(Matchers.<IMatchPattern> any())).thenAnswer(typeSearchAnswer);
			when(type.getFQN()).thenReturn("pattttern");
			when(type.isClass()).thenReturn(true);
			when(type.castToClass()).thenReturn(classType);
			when(type.isInitialized()).thenReturn(true);

			Collection<? extends ImmutableClassType> allTypes = lookup.findClassTypesByPattern(pattern, false);

			assertThat(allTypes, hasSize(1));

			verify(fqnIndexer, times(1)).findByPattern(Matchers.<IMatchPattern> any());
			verify(classCache, times(1)).executeWithReadLock(Matchers.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}

		@Test
		public void notInitializedOnlyInitializedTypes() throws Exception {
			String pattern = "pat*tern";
			when(fqnIndexer.findByPattern(Matchers.<IMatchPattern> any())).thenAnswer(typeSearchAnswer);
			when(type.getFQN()).thenReturn("pattttern");
			when(type.isClass()).thenReturn(true);
			when(type.castToClass()).thenReturn(classType);
			when(type.isInitialized()).thenReturn(false);

			Collection<? extends ImmutableClassType> initializedTypes = lookup.findClassTypesByPattern(pattern, true);

			assertThat(initializedTypes, is(empty()));

			verify(fqnIndexer, times(1)).findByPattern(Matchers.<IMatchPattern> any());
			verify(classCache, times(1)).executeWithReadLock(Matchers.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}

		@Test
		public void notInitializedAllTypes() throws Exception {
			String pattern = "pat*tern";
			when(fqnIndexer.findByPattern(Matchers.<IMatchPattern> any())).thenAnswer(typeSearchAnswer);
			when(type.getFQN()).thenReturn("pattttern");
			when(type.isClass()).thenReturn(true);
			when(type.castToClass()).thenReturn(classType);
			when(type.isInitialized()).thenReturn(false);

			Collection<? extends ImmutableClassType> allTypes = lookup.findClassTypesByPattern(pattern, false);

			assertThat(allTypes, hasSize(1));

			verify(fqnIndexer, times(1)).findByPattern(Matchers.<IMatchPattern> any());
			verify(classCache, times(1)).executeWithReadLock(Matchers.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}

		@Test
		public void notFoundOnlyInitializedTypes() throws Exception {
			String pattern = "pat*tern";
			when(fqnIndexer.findByPattern(Matchers.<IMatchPattern> any())).thenAnswer(typeSearchAnswer);
			when(type.getFQN()).thenReturn("pattttern");
			when(type.isClass()).thenReturn(false);
			when(type.isInitialized()).thenReturn(true);

			Collection<? extends ImmutableClassType> initializedTypes = lookup.findClassTypesByPattern(pattern, true);

			assertThat(initializedTypes, is(empty()));

			verify(fqnIndexer, times(1)).findByPattern(Matchers.<IMatchPattern> any());
			verify(classCache, times(1)).executeWithReadLock(Matchers.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}

		@Test
		public void notFoundAllTypes() throws Exception {
			String pattern = "pat*tern";
			when(fqnIndexer.findByPattern(Matchers.<IMatchPattern> any())).thenAnswer(typeSearchAnswer);
			when(type.getFQN()).thenReturn("pattttern");
			when(type.isClass()).thenReturn(false);
			when(type.isInitialized()).thenReturn(true);

			Collection<? extends ImmutableClassType> allTypes = lookup.findClassTypesByPattern(pattern, false);

			assertThat(allTypes, is(empty()));

			verify(fqnIndexer, times(1)).findByPattern(Matchers.<IMatchPattern> any());
			verify(classCache, times(1)).executeWithReadLock(Matchers.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}
	}

	public class InterfaceTypesByPattern extends ClassCacheLookupTest {

		@Test
		public void initializedOnlyInitializedTypes() throws Exception {
			String pattern = "pat*tern";
			when(fqnIndexer.findByPattern(Matchers.<IMatchPattern> any())).thenAnswer(typeSearchAnswer);
			when(type.getFQN()).thenReturn("pattttern");
			when(type.isInterface()).thenReturn(true);
			when(type.castToInterface()).thenReturn(interfaceType);
			when(type.isInitialized()).thenReturn(true);

			Collection<? extends ImmutableInterfaceType> initializedTypes = lookup.findInterfaceTypesByPattern(pattern, true);

			assertThat(initializedTypes, hasSize(1));

			verify(fqnIndexer, times(1)).findByPattern(Matchers.<IMatchPattern> any());
			verify(classCache, times(1)).executeWithReadLock(Matchers.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}

		@Test
		public void initializedAllTypes() throws Exception {
			String pattern = "pat*tern";
			when(fqnIndexer.findByPattern(Matchers.<IMatchPattern> any())).thenAnswer(typeSearchAnswer);
			when(type.getFQN()).thenReturn("pattttern");
			when(type.isInterface()).thenReturn(true);
			when(type.castToInterface()).thenReturn(interfaceType);
			when(type.isInitialized()).thenReturn(true);

			Collection<? extends ImmutableInterfaceType> allTypes = lookup.findInterfaceTypesByPattern(pattern, false);

			assertThat(allTypes, hasSize(1));

			verify(fqnIndexer, times(1)).findByPattern(Matchers.<IMatchPattern> any());
			verify(classCache, times(1)).executeWithReadLock(Matchers.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}

		@Test
		public void notInitializedOnlyInitializedTypes() throws Exception {
			String pattern = "pat*tern";
			when(fqnIndexer.findByPattern(Matchers.<IMatchPattern> any())).thenAnswer(typeSearchAnswer);
			when(type.getFQN()).thenReturn("pattttern");
			when(type.isInterface()).thenReturn(true);
			when(type.castToInterface()).thenReturn(interfaceType);
			when(type.isInitialized()).thenReturn(false);

			Collection<? extends ImmutableInterfaceType> initializedTypes = lookup.findInterfaceTypesByPattern(pattern, true);

			assertThat(initializedTypes, is(empty()));

			verify(fqnIndexer, times(1)).findByPattern(Matchers.<IMatchPattern> any());
			verify(classCache, times(1)).executeWithReadLock(Matchers.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}

		@Test
		public void notInitializedAllTypes() throws Exception {
			String pattern = "pat*tern";
			when(fqnIndexer.findByPattern(Matchers.<IMatchPattern> any())).thenAnswer(typeSearchAnswer);
			when(type.getFQN()).thenReturn("pattttern");
			when(type.isInterface()).thenReturn(true);
			when(type.castToInterface()).thenReturn(interfaceType);
			when(type.isInitialized()).thenReturn(false);

			Collection<? extends ImmutableInterfaceType> allTypes = lookup.findInterfaceTypesByPattern(pattern, false);

			assertThat(allTypes, hasSize(1));

			verify(fqnIndexer, times(1)).findByPattern(Matchers.<IMatchPattern> any());
			verify(classCache, times(1)).executeWithReadLock(Matchers.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}

		@Test
		public void notFoundOnlyInitializedTypes() throws Exception {
			String pattern = "pat*tern";
			when(fqnIndexer.findByPattern(Matchers.<IMatchPattern> any())).thenAnswer(typeSearchAnswer);
			when(type.getFQN()).thenReturn("pattttern");
			when(type.isInterface()).thenReturn(false);
			when(type.isInitialized()).thenReturn(true);

			Collection<? extends ImmutableInterfaceType> initializedTypes = lookup.findInterfaceTypesByPattern(pattern, true);

			assertThat(initializedTypes, is(empty()));

			verify(fqnIndexer, times(1)).findByPattern(Matchers.<IMatchPattern> any());
			verify(classCache, times(1)).executeWithReadLock(Matchers.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}

		@Test
		public void notFoundAllTypes() throws Exception {
			String pattern = "pat*tern";
			when(fqnIndexer.findByPattern(Matchers.<IMatchPattern> any())).thenAnswer(typeSearchAnswer);
			when(type.getFQN()).thenReturn("pattttern");
			when(type.isInterface()).thenReturn(false);
			when(type.isInitialized()).thenReturn(true);

			Collection<? extends ImmutableInterfaceType> allTypes = lookup.findInterfaceTypesByPattern(pattern, false);

			assertThat(allTypes, is(empty()));

			verify(fqnIndexer, times(1)).findByPattern(Matchers.<IMatchPattern> any());
			verify(classCache, times(1)).executeWithReadLock(Matchers.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}
	}

	public class AnnotationTypesByPattern extends ClassCacheLookupTest {

		@Test
		public void initializedOnlyInitializedTypes() throws Exception {
			String pattern = "pat*tern";
			when(fqnIndexer.findByPattern(Matchers.<IMatchPattern> any())).thenAnswer(typeSearchAnswer);
			when(type.getFQN()).thenReturn("pattttern");
			when(type.isAnnotation()).thenReturn(true);
			when(type.castToAnnotation()).thenReturn(annotationType);
			when(type.isInitialized()).thenReturn(true);

			Collection<? extends ImmutableAnnotationType> initializedTypes = lookup.findAnnotationTypesByPattern(pattern, true);

			assertThat(initializedTypes, hasSize(1));

			verify(fqnIndexer, times(1)).findByPattern(Matchers.<IMatchPattern> any());
			verify(classCache, times(1)).executeWithReadLock(Matchers.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}

		@Test
		public void initializedAllTypes() throws Exception {
			String pattern = "pat*tern";
			when(fqnIndexer.findByPattern(Matchers.<IMatchPattern> any())).thenAnswer(typeSearchAnswer);
			when(type.getFQN()).thenReturn("pattttern");
			when(type.isAnnotation()).thenReturn(true);
			when(type.castToAnnotation()).thenReturn(annotationType);
			when(type.isInitialized()).thenReturn(true);

			Collection<? extends ImmutableAnnotationType> initializedTypes = lookup.findAnnotationTypesByPattern(pattern, true);

			assertThat(initializedTypes, hasSize(1));

			verify(fqnIndexer, times(1)).findByPattern(Matchers.<IMatchPattern> any());
			verify(classCache, times(1)).executeWithReadLock(Matchers.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}

		@Test
		public void nonInitializedOnlyInitializedTypes() throws Exception {
			String pattern = "pat*tern";
			when(fqnIndexer.findByPattern(Matchers.<IMatchPattern> any())).thenAnswer(typeSearchAnswer);
			when(type.getFQN()).thenReturn("pattttern");
			when(type.isAnnotation()).thenReturn(true);
			when(type.castToAnnotation()).thenReturn(annotationType);
			when(type.isInitialized()).thenReturn(false);

			Collection<? extends ImmutableAnnotationType> initializedTypes = lookup.findAnnotationTypesByPattern(pattern, true);

			assertThat(initializedTypes, is(empty()));

			verify(fqnIndexer, times(1)).findByPattern(Matchers.<IMatchPattern> any());
			verify(classCache, times(1)).executeWithReadLock(Matchers.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}

		@Test
		public void nonInitializedAllTypes() throws Exception {
			String pattern = "pat*tern";
			when(fqnIndexer.findByPattern(Matchers.<IMatchPattern> any())).thenAnswer(typeSearchAnswer);
			when(type.getFQN()).thenReturn("pattttern");
			when(type.isAnnotation()).thenReturn(true);
			when(type.castToAnnotation()).thenReturn(annotationType);
			when(type.isInitialized()).thenReturn(false);

			Collection<? extends ImmutableAnnotationType> allTypes = lookup.findAnnotationTypesByPattern(pattern, false);

			assertThat(allTypes, hasSize(1));

			verify(fqnIndexer, times(1)).findByPattern(Matchers.<IMatchPattern> any());
			verify(classCache, times(1)).executeWithReadLock(Matchers.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}

		@Test
		public void notFoundOnlyInitializedTypes() throws Exception {
			String pattern = "pat*tern";
			when(fqnIndexer.findByPattern(Matchers.<IMatchPattern> any())).thenAnswer(typeSearchAnswer);
			when(type.getFQN()).thenReturn("pattttern");
			when(type.isAnnotation()).thenReturn(false);
			when(type.isInitialized()).thenReturn(true);

			Collection<? extends ImmutableAnnotationType> initializedTypes = lookup.findAnnotationTypesByPattern(pattern, true);

			assertThat(initializedTypes, is(empty()));

			verify(fqnIndexer, times(1)).findByPattern(Matchers.<IMatchPattern> any());
			verify(classCache, times(1)).executeWithReadLock(Matchers.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}

		@Test
		public void notFoundAllTypes() throws Exception {
			String pattern = "pat*tern";
			when(fqnIndexer.findByPattern(Matchers.<IMatchPattern> any())).thenAnswer(typeSearchAnswer);
			when(type.getFQN()).thenReturn("pattttern");
			when(type.isAnnotation()).thenReturn(false);
			when(type.isInitialized()).thenReturn(true);

			Collection<? extends ImmutableAnnotationType> allTypes = lookup.findAnnotationTypesByPattern(pattern, false);

			assertThat(allTypes, is(empty()));

			verify(fqnIndexer, times(1)).findByPattern(Matchers.<IMatchPattern> any());
			verify(classCache, times(1)).executeWithReadLock(Matchers.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}
	}

	public class ExceptionTypesByPattern extends ClassCacheLookupTest {

		@Test
		public void initializedOnlyInitializedTypes() throws Exception {
			String pattern = "pat*tern";
			when(fqnIndexer.findByPattern(Matchers.<IMatchPattern> any())).thenAnswer(typeSearchAnswer);
			when(type.getFQN()).thenReturn("pattttern");
			when(type.isClass()).thenReturn(true);
			when(type.castToClass()).thenReturn(classType);
			when(classType.isException()).thenReturn(true);
			when(type.isInitialized()).thenReturn(true);

			Collection<? extends ImmutableClassType> initializedTypes = lookup.findExceptionTypesByPattern(pattern, true);

			assertThat(initializedTypes, hasSize(1));

			verify(fqnIndexer, times(1)).findByPattern(Matchers.<IMatchPattern> any());
			verify(classCache, times(1)).executeWithReadLock(Matchers.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}

		@Test
		public void initializedAllTypes() throws Exception {
			String pattern = "pat*tern";
			when(fqnIndexer.findByPattern(Matchers.<IMatchPattern> any())).thenAnswer(typeSearchAnswer);
			when(type.getFQN()).thenReturn("pattttern");
			when(type.isClass()).thenReturn(true);
			when(type.castToClass()).thenReturn(classType);
			when(classType.isException()).thenReturn(true);
			when(type.isInitialized()).thenReturn(true);

			Collection<? extends ImmutableClassType> initializedTypes = lookup.findExceptionTypesByPattern(pattern, true);

			assertThat(initializedTypes, hasSize(1));

			verify(fqnIndexer, times(1)).findByPattern(Matchers.<IMatchPattern> any());
			verify(classCache, times(1)).executeWithReadLock(Matchers.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}

		@Test
		public void nonInitializedOnlyInitializedTypes() throws Exception {
			String pattern = "pat*tern";
			when(fqnIndexer.findByPattern(Matchers.<IMatchPattern> any())).thenAnswer(typeSearchAnswer);
			when(type.getFQN()).thenReturn("pattttern");
			when(type.isClass()).thenReturn(true);
			when(type.castToClass()).thenReturn(classType);
			when(classType.isException()).thenReturn(true);
			when(type.isInitialized()).thenReturn(false);

			Collection<? extends ImmutableClassType> initializedTypes = lookup.findExceptionTypesByPattern(pattern, true);

			assertThat(initializedTypes, is(empty()));

			verify(fqnIndexer, times(1)).findByPattern(Matchers.<IMatchPattern> any());
			verify(classCache, times(1)).executeWithReadLock(Matchers.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}

		@Test
		public void nonInitializedAllTypes() throws Exception {
			String pattern = "pat*tern";
			when(fqnIndexer.findByPattern(Matchers.<IMatchPattern> any())).thenAnswer(typeSearchAnswer);
			when(type.getFQN()).thenReturn("pattttern");
			when(type.isClass()).thenReturn(true);
			when(type.castToClass()).thenReturn(classType);
			when(classType.isException()).thenReturn(true);
			when(type.isInitialized()).thenReturn(false);

			Collection<? extends ImmutableClassType> allTypes = lookup.findExceptionTypesByPattern(pattern, false);

			assertThat(allTypes, hasSize(1));

			verify(fqnIndexer, times(1)).findByPattern(Matchers.<IMatchPattern> any());
			verify(classCache, times(1)).executeWithReadLock(Matchers.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}

		@Test
		public void notFoundOnlyInitializedTypes() throws Exception {
			String pattern = "pat*tern";
			when(fqnIndexer.findByPattern(Matchers.<IMatchPattern> any())).thenAnswer(typeSearchAnswer);
			when(type.getFQN()).thenReturn("pattttern");
			when(type.isClass()).thenReturn(true);
			when(type.castToClass()).thenReturn(classType);
			when(classType.isException()).thenReturn(false);
			when(type.isInitialized()).thenReturn(true);

			Collection<? extends ImmutableClassType> initializedTypes = lookup.findExceptionTypesByPattern(pattern, true);

			assertThat(initializedTypes, is(empty()));

			verify(fqnIndexer, times(1)).findByPattern(Matchers.<IMatchPattern> any());
			verify(classCache, times(1)).executeWithReadLock(Matchers.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}

		@Test
		public void notFoundOnlyAllTypes() throws Exception {
			String pattern = "pat*tern";
			when(fqnIndexer.findByPattern(Matchers.<IMatchPattern> any())).thenAnswer(typeSearchAnswer);
			when(type.getFQN()).thenReturn("pattttern");
			when(type.isClass()).thenReturn(true);
			when(type.castToClass()).thenReturn(classType);
			when(classType.isException()).thenReturn(false);
			when(type.isInitialized()).thenReturn(true);

			Collection<? extends ImmutableClassType> allTypes = lookup.findExceptionTypesByPattern(pattern, false);

			assertThat(allTypes, is(empty()));

			verify(fqnIndexer, times(1)).findByPattern(Matchers.<IMatchPattern> any());
			verify(classCache, times(1)).executeWithReadLock(Matchers.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}
	}

	/**
	 * Tests the {@link ClassCacheLookup#findInstrumentedTypes()} method.
	 */
	public class FindInstrumentedTypes extends ClassCacheLookupTest {

		@Test
		@SuppressWarnings("unchecked")
		public void find() throws Exception {
			when(fqnIndexer.findAll()).thenReturn(Arrays.asList(type));
			when(type.isClass()).thenReturn(true);
			when(type.isInitialized()).thenReturn(true);
			when(type.castToClass()).thenReturn(classType);
			when(classType.hasInstrumentationPoints()).thenReturn(true);

			Collection<ImmutableType> result = (Collection<ImmutableType>) lookup.findInstrumentedTypes();

			verify(classCache).executeWithReadLock(any(Callable.class));
			verify(fqnIndexer).findAll();
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
			assertThat(result, hasSize(1));
			assertThat(result, hasItem(type));
		}

		@Test
		@SuppressWarnings("unchecked")
		public void noInstrumentedTypes() throws Exception {
			when(fqnIndexer.findAll()).thenReturn(Arrays.asList(type));
			when(type.isClass()).thenReturn(true);
			when(type.isInitialized()).thenReturn(true);
			when(type.castToClass()).thenReturn(classType);
			when(classType.hasInstrumentationPoints()).thenReturn(false);

			Collection<? extends ImmutableType> result = lookup.findInstrumentedTypes();

			verify(classCache).executeWithReadLock(any(Callable.class));
			verify(fqnIndexer).findAll();
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
			assertThat(result, hasSize(0));
		}

		@Test
		@SuppressWarnings("unchecked")
		public void typeNotAClass() throws Exception {
			when(fqnIndexer.findAll()).thenReturn(Arrays.asList(type));
			when(type.isClass()).thenReturn(false);

			Collection<? extends ImmutableType> result = lookup.findInstrumentedTypes();

			verify(classCache).executeWithReadLock(any(Callable.class));
			verify(fqnIndexer).findAll();
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
			assertThat(result, hasSize(0));
		}

		@Test
		@SuppressWarnings("unchecked")
		public void typeNotInitialized() throws Exception {
			when(fqnIndexer.findAll()).thenReturn(Arrays.asList(type));
			when(type.isClass()).thenReturn(true);
			when(type.isInitialized()).thenReturn(false);

			Collection<? extends ImmutableType> result = lookup.findInstrumentedTypes();

			verify(classCache).executeWithReadLock(any(Callable.class));
			verify(fqnIndexer).findAll();
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
			assertThat(result, hasSize(0));
		}

		@Test
		@SuppressWarnings("unchecked")
		public void noClassesLoaded() throws Exception {
			Collection<? extends ImmutableType> result = lookup.findInstrumentedTypes();

			verify(classCache).executeWithReadLock(any(Callable.class));
			verify(fqnIndexer).findAll();
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
			assertThat(result, hasSize(0));
		}

		@Test
		@SuppressWarnings("unchecked")
		public void exceptionInCallable() throws Exception {
			when(fqnIndexer.findAll()).thenThrow(RuntimeException.class);

			Collection<? extends ImmutableType> result = lookup.findInstrumentedTypes();

			verify(classCache).executeWithReadLock(any(Callable.class));
			verify(fqnIndexer).findAll();
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
			assertThat(result, hasSize(0));
		}
	}
}
