package info.novatec.inspectit.cmr.instrumentation.classcache;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import info.novatec.inspectit.cmr.instrumentation.classcache.index.FQNIndexer;
import info.novatec.inspectit.cmr.instrumentation.classcache.index.HashIndexer;
import info.novatec.inspectit.instrumentation.classcache.ImmutableAnnotationType;
import info.novatec.inspectit.instrumentation.classcache.ImmutableClassType;
import info.novatec.inspectit.instrumentation.classcache.ImmutableInterfaceType;
import info.novatec.inspectit.instrumentation.classcache.ImmutableType;
import info.novatec.inspectit.instrumentation.classcache.Type;
import info.novatec.inspectit.testbase.TestBase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
			Collection<Type> types = new ArrayList<Type>();
			types.add(type);
			return types;
		}
	};

	@InjectMocks
	protected ClassCacheLookup lookup;

	@Mock
	protected Logger log;

	@Mock
	protected ClassCache classCache;

	@Mock
	protected FQNIndexer<Type> fqnIndexer;

	@Mock
	protected HashIndexer hashIndexer;

	@Mock
	protected Type type;

	@Mock
	protected ImmutableClassType classType;

	@Mock
	protected ImmutableInterfaceType interfaceType;

	@Mock
	protected ImmutableAnnotationType annotationType;

	@BeforeMethod
	public void setup() throws Exception {
		Answer<Object> callableAnswer = new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				Callable<?> callable = (Callable<?>) invocation.getArguments()[0];
				return callable.call();
			}
		};
		doAnswer(callableAnswer).when(classCache).executeWithReadLock(Mockito.<Callable<?>> anyObject());
		doAnswer(callableAnswer).when(classCache).executeWithWriteLock(Mockito.<Callable<?>> anyObject());

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
			verify(classCache, times(1)).executeWithReadLock(Mockito.<Callable<?>> anyObject());
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
			verify(classCache, times(1)).executeWithReadLock(Mockito.<Callable<?>> anyObject());
			verifyNoMoreInteractions(hashIndexer, classCache);
			verifyZeroInteractions(fqnIndexer);
		}
	}

	public class FindByPattern extends ClassCacheLookupTest {

		@Test
		public void directInitialized() throws Exception {
			String pattern = "pattern";
			when(fqnIndexer.lookup(pattern)).thenReturn(type);
			when(type.isInitialized()).thenReturn(true);

			Collection<? extends ImmutableType> intializedTypes = lookup.findByPattern(pattern, true);
			Collection<? extends ImmutableType> nonInitializedTypes = lookup.findByPattern(pattern, false);

			assertThat(intializedTypes, hasSize(1));
			assertThat(nonInitializedTypes, hasSize(1));

			verify(fqnIndexer, times(2)).lookup(pattern);
			verify(classCache, times(2)).executeWithReadLock(Mockito.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}

		@Test
		public void directNotInitialized() throws Exception {
			String pattern = "pattern";
			when(fqnIndexer.lookup(pattern)).thenReturn(type);
			when(type.isInitialized()).thenReturn(false);

			Collection<? extends ImmutableType> intializedTypes = lookup.findByPattern(pattern, true);
			Collection<? extends ImmutableType> nonInitializedTypes = lookup.findByPattern(pattern, false);

			assertThat(intializedTypes, hasSize(0));
			assertThat(nonInitializedTypes, hasSize(1));

			verify(fqnIndexer, times(2)).lookup(pattern);
			verify(classCache, times(2)).executeWithReadLock(Mockito.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}

		@Test
		public void directNotFound() throws Exception {
			String pattern = "pattern";
			when(fqnIndexer.lookup(pattern)).thenReturn(type);
			when(type.isInitialized()).thenReturn(true);

			Collection<? extends ImmutableType> types = lookup.findByPattern("somethingElse", false);

			assertThat(types, hasSize(0));

			verify(fqnIndexer, times(1)).lookup("somethingElse");
			verify(classCache, times(1)).executeWithReadLock(Mockito.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}

		@Test
		public void wildCardInitialized() throws Exception {
			String pattern = "pat*tern";
			when(fqnIndexer.findStartsWith("pat")).thenAnswer(typeSearchAnswer);
			when(type.isInitialized()).thenReturn(true);
			when(type.getFQN()).thenReturn("pattttern");

			Collection<? extends ImmutableType> initializedTypes = lookup.findByPattern(pattern, true);
			Collection<? extends ImmutableType> nonInitializedTypes = lookup.findByPattern(pattern, false);

			assertThat(initializedTypes, hasSize(1));
			assertThat(nonInitializedTypes, hasSize(1));

			verify(fqnIndexer, times(2)).findStartsWith("pat");
			verify(classCache, times(2)).executeWithReadLock(Mockito.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}

		@Test
		public void wildCardNotInitialized() throws Exception {
			String pattern = "pat*tern";
			when(fqnIndexer.findStartsWith("pat")).thenAnswer(typeSearchAnswer);
			when(type.isInitialized()).thenReturn(false);
			when(type.getFQN()).thenReturn("pattttern");

			Collection<? extends ImmutableType> initializedTypes = lookup.findByPattern(pattern, true);
			Collection<? extends ImmutableType> nonInitializedTypes = lookup.findByPattern(pattern, false);

			assertThat(initializedTypes, hasSize(0));
			assertThat(nonInitializedTypes, hasSize(1));

			verify(fqnIndexer, times(2)).findStartsWith("pat");
			verify(classCache, times(2)).executeWithReadLock(Mockito.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}

		@Test
		public void wildCardNotMatched() throws Exception {
			String pattern = "pat*tern";
			when(fqnIndexer.findStartsWith("pat")).thenAnswer(typeSearchAnswer);
			when(type.isInitialized()).thenReturn(true);
			when(type.getFQN()).thenReturn("patsomething");

			Collection<? extends ImmutableType> initializedTypes = lookup.findByPattern(pattern, true);
			Collection<? extends ImmutableType> nonInitializedTypes = lookup.findByPattern(pattern, false);

			assertThat(initializedTypes, hasSize(0));
			assertThat(nonInitializedTypes, hasSize(0));

			verify(fqnIndexer, times(2)).findStartsWith("pat");
			verify(classCache, times(2)).executeWithReadLock(Mockito.<Callable<?>> anyObject());

			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}
	}

	public class ClassTypesByPattern extends ClassCacheLookupTest {

		@Test
		public void wildCardInitialized() throws Exception {
			String pattern = "pat*tern";
			when(fqnIndexer.findStartsWith("pat")).thenAnswer(typeSearchAnswer);
			when(type.getFQN()).thenReturn("pattttern");
			when(type.isClass()).thenReturn(true);
			when(type.castToClass()).thenReturn(classType);
			when(type.isInitialized()).thenReturn(true);

			Collection<? extends ImmutableClassType> initializedTypes = lookup.findClassTypesByPattern(pattern, true);
			Collection<? extends ImmutableClassType> allTypes = lookup.findClassTypesByPattern(pattern, false);

			assertThat(initializedTypes, hasSize(1));
			assertThat(allTypes, hasSize(1));

			verify(fqnIndexer, times(2)).findStartsWith("pat");
			verify(classCache, times(2)).executeWithReadLock(Mockito.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}

		@Test
		public void wildCardNotInitialized() throws Exception {
			String pattern = "pat*tern";
			when(fqnIndexer.findStartsWith("pat")).thenAnswer(typeSearchAnswer);
			when(type.getFQN()).thenReturn("pattttern");
			when(type.isClass()).thenReturn(true);
			when(type.castToClass()).thenReturn(classType);
			when(type.isInitialized()).thenReturn(false);

			Collection<? extends ImmutableClassType> initializedTypes = lookup.findClassTypesByPattern(pattern, true);
			Collection<? extends ImmutableClassType> allTypes = lookup.findClassTypesByPattern(pattern, false);

			assertThat(allTypes, hasSize(1));
			assertThat(initializedTypes, hasSize(0));

			verify(fqnIndexer, times(2)).findStartsWith("pat");
			verify(classCache, times(2)).executeWithReadLock(Mockito.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}

		@Test
		public void wildCardNotFound() throws Exception {
			String pattern = "pat*tern";
			when(fqnIndexer.findStartsWith("pat")).thenAnswer(typeSearchAnswer);
			when(type.getFQN()).thenReturn("pattttern");
			when(type.isClass()).thenReturn(false);
			when(type.isInitialized()).thenReturn(true);

			Collection<? extends ImmutableClassType> initializedTypes = lookup.findClassTypesByPattern(pattern, true);
			Collection<? extends ImmutableClassType> allTypes = lookup.findClassTypesByPattern(pattern, false);

			assertThat(initializedTypes, hasSize(0));
			assertThat(allTypes, hasSize(0));

			verify(fqnIndexer, times(2)).findStartsWith("pat");
			verify(classCache, times(2)).executeWithReadLock(Mockito.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}
	}

	public class InterfaceTypesByPattern extends ClassCacheLookupTest {

		@Test
		public void wildCardInitialized() throws Exception {
			String pattern = "pat*tern";
			when(fqnIndexer.findStartsWith("pat")).thenAnswer(typeSearchAnswer);
			when(type.getFQN()).thenReturn("pattttern");
			when(type.isInterface()).thenReturn(true);
			when(type.castToInterface()).thenReturn(interfaceType);
			when(type.isInitialized()).thenReturn(true);

			Collection<? extends ImmutableInterfaceType> initializedTypes = lookup.findInterfaceTypesByPattern(pattern, true);
			Collection<? extends ImmutableInterfaceType> allTypes = lookup.findInterfaceTypesByPattern(pattern, false);

			assertThat(initializedTypes, hasSize(1));
			assertThat(allTypes, hasSize(1));

			verify(fqnIndexer, times(2)).findStartsWith("pat");
			verify(classCache, times(2)).executeWithReadLock(Mockito.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}

		@Test
		public void wildCardNotInitialized() throws Exception {
			String pattern = "pat*tern";
			when(fqnIndexer.findStartsWith("pat")).thenAnswer(typeSearchAnswer);
			when(type.getFQN()).thenReturn("pattttern");
			when(type.isInterface()).thenReturn(true);
			when(type.castToInterface()).thenReturn(interfaceType);
			when(type.isInitialized()).thenReturn(false);

			Collection<? extends ImmutableInterfaceType> initializedTypes = lookup.findInterfaceTypesByPattern(pattern, true);
			Collection<? extends ImmutableInterfaceType> allTypes = lookup.findInterfaceTypesByPattern(pattern, false);

			assertThat(allTypes, hasSize(1));
			assertThat(initializedTypes, hasSize(0));

			verify(fqnIndexer, times(2)).findStartsWith("pat");
			verify(classCache, times(2)).executeWithReadLock(Mockito.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}

		@Test
		public void wildCardNotFound() throws Exception {
			String pattern = "pat*tern";
			when(fqnIndexer.findStartsWith("pat")).thenAnswer(typeSearchAnswer);
			when(type.getFQN()).thenReturn("pattttern");
			when(type.isInterface()).thenReturn(false);
			when(type.isInitialized()).thenReturn(true);

			Collection<? extends ImmutableInterfaceType> initializedTypes = lookup.findInterfaceTypesByPattern(pattern, true);
			Collection<? extends ImmutableInterfaceType> allTypes = lookup.findInterfaceTypesByPattern(pattern, false);

			assertThat(initializedTypes, hasSize(0));
			assertThat(allTypes, hasSize(0));

			verify(fqnIndexer, times(2)).findStartsWith("pat");
			verify(classCache, times(2)).executeWithReadLock(Mockito.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}
	}

	public class AnnotationTypesByPattern extends ClassCacheLookupTest {

		@Test
		public void wildCardInitialized() throws Exception {
			String pattern = "pat*tern";
			when(fqnIndexer.findStartsWith("pat")).thenAnswer(typeSearchAnswer);
			when(type.getFQN()).thenReturn("pattttern");
			when(type.isAnnotation()).thenReturn(true);
			when(type.castToAnnotation()).thenReturn(annotationType);
			when(type.isInitialized()).thenReturn(true);

			Collection<? extends ImmutableAnnotationType> initializedTypes = lookup.findAnnotationTypesByPattern(pattern, true);
			Collection<? extends ImmutableAnnotationType> allTypes = lookup.findAnnotationTypesByPattern(pattern, false);

			assertThat(initializedTypes, hasSize(1));
			assertThat(allTypes, hasSize(1));

			verify(fqnIndexer, times(2)).findStartsWith("pat");
			verify(classCache, times(2)).executeWithReadLock(Mockito.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}

		@Test
		public void wildCardNonInitialized() throws Exception {
			String pattern = "pat*tern";
			when(fqnIndexer.findStartsWith("pat")).thenAnswer(typeSearchAnswer);
			when(type.getFQN()).thenReturn("pattttern");
			when(type.isAnnotation()).thenReturn(true);
			when(type.castToAnnotation()).thenReturn(annotationType);
			when(type.isInitialized()).thenReturn(false);

			Collection<? extends ImmutableAnnotationType> initializedTypes = lookup.findAnnotationTypesByPattern(pattern, true);
			Collection<? extends ImmutableAnnotationType> allTypes = lookup.findAnnotationTypesByPattern(pattern, false);

			assertThat(allTypes, hasSize(1));
			assertThat(initializedTypes, hasSize(0));

			verify(fqnIndexer, times(2)).findStartsWith("pat");
			verify(classCache, times(2)).executeWithReadLock(Mockito.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}

		@Test
		public void wildCardNotFound() throws Exception {
			String pattern = "pat*tern";
			when(fqnIndexer.findStartsWith("pat")).thenAnswer(typeSearchAnswer);
			when(type.getFQN()).thenReturn("pattttern");
			when(type.isAnnotation()).thenReturn(false);
			when(type.isInitialized()).thenReturn(true);

			Collection<? extends ImmutableAnnotationType> initializedTypes = lookup.findAnnotationTypesByPattern(pattern, true);
			Collection<? extends ImmutableAnnotationType> allTypes = lookup.findAnnotationTypesByPattern(pattern, false);

			assertThat(initializedTypes, hasSize(0));
			assertThat(allTypes, hasSize(0));

			verify(fqnIndexer, times(2)).findStartsWith("pat");
			verify(classCache, times(2)).executeWithReadLock(Mockito.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}
	}

	public class ExceptionTypesByPattern extends ClassCacheLookupTest {

		@Test
		public void wildCardInitialized() throws Exception {
			String pattern = "pat*tern";
			when(fqnIndexer.findStartsWith("pat")).thenAnswer(typeSearchAnswer);
			when(type.getFQN()).thenReturn("pattttern");
			when(type.isClass()).thenReturn(true);
			when(type.castToClass()).thenReturn(classType);
			when(classType.isException()).thenReturn(true);
			when(type.isInitialized()).thenReturn(true);

			Collection<? extends ImmutableClassType> initializedTypes = lookup.findExceptionTypesByPattern(pattern, true);
			Collection<? extends ImmutableClassType> allTypes = lookup.findExceptionTypesByPattern(pattern, false);

			assertThat(initializedTypes, hasSize(1));
			assertThat(allTypes, hasSize(1));

			verify(fqnIndexer, times(2)).findStartsWith("pat");
			verify(classCache, times(2)).executeWithReadLock(Mockito.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}

		@Test
		public void wildCardNonInitialized() throws Exception {
			String pattern = "pat*tern";
			when(fqnIndexer.findStartsWith("pat")).thenAnswer(typeSearchAnswer);
			when(type.getFQN()).thenReturn("pattttern");
			when(type.isClass()).thenReturn(true);
			when(type.castToClass()).thenReturn(classType);
			when(classType.isException()).thenReturn(true);
			when(type.isInitialized()).thenReturn(false);

			Collection<? extends ImmutableClassType> initializedTypes = lookup.findExceptionTypesByPattern(pattern, true);
			Collection<? extends ImmutableClassType> allTypes = lookup.findExceptionTypesByPattern(pattern, false);

			assertThat(allTypes, hasSize(1));
			assertThat(initializedTypes, hasSize(0));

			verify(fqnIndexer, times(2)).findStartsWith("pat");
			verify(classCache, times(2)).executeWithReadLock(Mockito.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}

		@Test
		public void wildCardNotFound() throws Exception {
			String pattern = "pat*tern";
			when(fqnIndexer.findStartsWith("pat")).thenAnswer(typeSearchAnswer);
			when(type.getFQN()).thenReturn("pattttern");
			when(type.isClass()).thenReturn(true);
			when(type.castToClass()).thenReturn(classType);
			when(classType.isException()).thenReturn(false);
			when(type.isInitialized()).thenReturn(true);

			Collection<? extends ImmutableClassType> initializedTypes = lookup.findExceptionTypesByPattern(pattern, true);
			Collection<? extends ImmutableClassType> allTypes = lookup.findExceptionTypesByPattern(pattern, false);

			assertThat(initializedTypes, hasSize(0));
			assertThat(allTypes, hasSize(0));

			verify(fqnIndexer, times(2)).findStartsWith("pat");
			verify(classCache, times(2)).executeWithReadLock(Mockito.<Callable<?>> anyObject());
			verifyNoMoreInteractions(fqnIndexer, classCache);
			verifyZeroInteractions(hashIndexer);
		}
	}

}
