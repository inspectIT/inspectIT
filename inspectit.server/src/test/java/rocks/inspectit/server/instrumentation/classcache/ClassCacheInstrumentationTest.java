package rocks.inspectit.server.instrumentation.classcache;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.server.instrumentation.classcache.ClassCache;
import rocks.inspectit.server.instrumentation.classcache.ClassCacheInstrumentation;
import rocks.inspectit.server.instrumentation.classcache.ClassCacheLookup;
import rocks.inspectit.server.instrumentation.config.ClassCacheSearchNarrower;
import rocks.inspectit.server.instrumentation.config.applier.IInstrumentationApplier;
import rocks.inspectit.shared.all.instrumentation.classcache.AnnotationType;
import rocks.inspectit.shared.all.instrumentation.classcache.ClassType;
import rocks.inspectit.shared.all.instrumentation.classcache.ImmutableClassType;
import rocks.inspectit.shared.all.instrumentation.classcache.InterfaceType;
import rocks.inspectit.shared.all.instrumentation.classcache.MethodType;
import rocks.inspectit.shared.all.instrumentation.classcache.Type;
import rocks.inspectit.shared.all.instrumentation.config.impl.AgentConfig;
import rocks.inspectit.shared.all.instrumentation.config.impl.InstrumentationDefinition;
import rocks.inspectit.shared.all.instrumentation.config.impl.MethodInstrumentationConfig;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.cs.ci.assignment.AbstractClassSensorAssignment;

@SuppressWarnings({ "all", "unchecked" })
public class ClassCacheInstrumentationTest extends TestBase {

	private static final String FQN = "FQN";

	@InjectMocks
	ClassCacheInstrumentation instrumentation;

	@Mock
	Logger log;

	@Mock
	ClassCache classCache;

	@Mock
	ClassCacheLookup lookup;

	@Mock
	AgentConfig agentConfiguration;

	@Mock
	ClassType classType;

	@Mock
	IInstrumentationApplier instrumentationApplier;

	@Mock
	ClassCacheSearchNarrower searchNarrower;

	@Mock
	AbstractClassSensorAssignment<?> assignment;

	@BeforeMethod
	public void setup() throws Exception {
		when(classCache.getLookupService()).thenReturn(lookup);

		Answer<Object> callableAnswer = new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				Callable<?> callable = (Callable<?>) invocation.getArguments()[0];
				return callable.call();
			}
		};
		doAnswer(callableAnswer).when(classCache).executeWithReadLock(Mockito.<Callable<?>> anyObject());
		doAnswer(callableAnswer).when(classCache).executeWithWriteLock(Mockito.<Callable<?>> anyObject());

		instrumentation.init(classCache);
	}

	public static class AddAndGetInstrumentationResult extends ClassCacheInstrumentationTest {

		@Test
		public void notInitialized() {
			when(classType.isInitialized()).thenReturn(false);

			InstrumentationDefinition result = instrumentation.addAndGetInstrumentationResult(classType, agentConfiguration, Collections.singleton(instrumentationApplier));

			assertThat(result, is(nullValue()));
		}

		@Test
		public void notInstrumented() {
			when(classType.isInitialized()).thenReturn(true);
			when(instrumentationApplier.addInstrumentationPoints(agentConfiguration, classType)).thenReturn(false);

			InstrumentationDefinition result = instrumentation.addAndGetInstrumentationResult(classType, agentConfiguration, Collections.singleton(instrumentationApplier));

			assertThat(result, is(nullValue()));
		}

		@Test
		public void instrumented() {
			Collection<MethodInstrumentationConfig> configs = mock(Collection.class);
			when(classType.isInitialized()).thenReturn(true);
			when(classType.getFQN()).thenReturn(FQN);
			when(classType.hasInstrumentationPoints()).thenReturn(true);
			when(classType.getInstrumentationPoints()).thenReturn(configs);
			when(instrumentationApplier.addInstrumentationPoints(agentConfiguration, classType)).thenReturn(true);

			InstrumentationDefinition result = instrumentation.addAndGetInstrumentationResult(classType, agentConfiguration, Collections.singleton(instrumentationApplier));

			assertThat(result, is(notNullValue()));
			assertThat(result.getClassName(), is(FQN));
			assertThat(result.getMethodInstrumentationConfigs(), is(configs));
		}

	}

	public static class RemoveInstrumentationPoints extends ClassCacheInstrumentationTest {

		@Test
		public void removeAll() throws Exception {
			MethodType methodType = mock(MethodType.class);
			when(classType.isClass()).thenReturn(true);
			when(classType.castToClass()).thenReturn(classType);
			when(classType.isInitialized()).thenReturn(true);
			when(classType.getMethods()).thenReturn(Collections.singleton(methodType));
			doReturn(Collections.singleton(classType)).when(lookup).findAll();

			instrumentation.removeInstrumentationPoints();

			// must be write lock
			verify(classCache, times(1)).executeWithWriteLock(Mockito.<Callable<?>> any());
			verify(methodType, times(1)).setMethodInstrumentationConfig(null);
		}

		@Test
		public void removeNothingWhenEmpty() throws Exception {
			doReturn(Collections.emptyList()).when(lookup).findAll();

			instrumentation.removeInstrumentationPoints();

			// not touching the write lock
			verify(classCache, times(0)).executeWithWriteLock(Mockito.<Callable<?>> any());
		}

		@Test
		public void removeNothingForAnnotationTypes() throws Exception {
			AnnotationType annotationType = new AnnotationType("");

			instrumentation.removeInstrumentationPoints(Collections.singleton(annotationType), Collections.singleton(instrumentationApplier));

			// must be write lock
			verify(classCache, times(1)).executeWithWriteLock(Mockito.<Callable<?>> any());
			verifyZeroInteractions(instrumentationApplier);
		}

		@Test
		public void removeNothingForInterfaceTypes() throws Exception {
			InterfaceType interfaceType = new InterfaceType("");

			instrumentation.removeInstrumentationPoints(Collections.singleton(interfaceType), Collections.singleton(instrumentationApplier));

			// must be write lock
			verify(classCache, times(1)).executeWithWriteLock(Mockito.<Callable<?>> any());
			verifyZeroInteractions(instrumentationApplier);
		}

	}

	public static class AddInstrumentationPoints extends ClassCacheInstrumentationTest {

		@Test
		public void add() throws Exception {
			when(classType.isClass()).thenReturn(true);
			when(classType.castToClass()).thenReturn(classType);
			when(classType.isInitialized()).thenReturn(true);
			when(instrumentationApplier.addInstrumentationPoints(agentConfiguration, classType)).thenReturn(true);
			doReturn(Collections.singleton(classType)).when(lookup).findAll();

			Collection<? extends ImmutableClassType> result = instrumentation.addInstrumentationPoints(agentConfiguration, Collections.singleton(instrumentationApplier));

			// assert result
			assertThat((Collection<ClassType>) result, hasItem(classType));

			// must be write lock
			verify(classCache, times(1)).executeWithWriteLock(Mockito.<Callable<?>> any());
			verify(instrumentationApplier, times(1)).addInstrumentationPoints(agentConfiguration, classType);
			verify(instrumentationApplier, times(1)).getSensorAssignment();
			verifyNoMoreInteractions(instrumentationApplier);
		}

		@Test
		public void searchNarrowAdd() throws Exception {
			when(classType.isClass()).thenReturn(true);
			when(classType.castToClass()).thenReturn(classType);
			when(classType.isInitialized()).thenReturn(true);
			when(instrumentationApplier.addInstrumentationPoints(agentConfiguration, classType)).thenReturn(true);
			doReturn(assignment).when(instrumentationApplier).getSensorAssignment();
			doReturn(Collections.singleton(classType)).when(searchNarrower).narrowByClassSensorAssignment(classCache, assignment);

			Collection<? extends ImmutableClassType> result = instrumentation.addInstrumentationPoints(agentConfiguration, Collections.singleton(instrumentationApplier));

			// assert result
			assertThat((Collection<ClassType>) result, hasItem(classType));

			// must be write lock
			verify(classCache, times(1)).executeWithWriteLock(Mockito.<Callable<?>> any());
			verify(instrumentationApplier, times(1)).addInstrumentationPoints(agentConfiguration, classType);
			verify(instrumentationApplier, times(1)).getSensorAssignment();
			verifyNoMoreInteractions(instrumentationApplier);
		}

		@Test
		public void addNothingWhenInstrumenterDoesNotAdd() throws Exception {
			when(classType.isClass()).thenReturn(true);
			when(classType.castToClass()).thenReturn(classType);
			when(classType.isInitialized()).thenReturn(true);
			when(instrumentationApplier.addInstrumentationPoints(agentConfiguration, classType)).thenReturn(false);
			doReturn(Collections.singleton(classType)).when(lookup).findAll();

			Collection<? extends ImmutableClassType> result = instrumentation.addInstrumentationPoints(agentConfiguration, Collections.singleton(instrumentationApplier));

			// assert result
			assertThat((Collection<ClassType>) result, is(empty()));

			// must be write lock
			verify(classCache, times(1)).executeWithWriteLock(Mockito.<Callable<?>> any());
			verify(instrumentationApplier, times(1)).addInstrumentationPoints(agentConfiguration, classType);
			verify(instrumentationApplier, times(1)).getSensorAssignment();
			verifyNoMoreInteractions(instrumentationApplier);
		}

		@Test
		public void searchNarrowAddNothingWhenInstrumenterDoesNotAdd() throws Exception {
			when(classType.isClass()).thenReturn(true);
			when(classType.castToClass()).thenReturn(classType);
			when(classType.isInitialized()).thenReturn(true);
			when(instrumentationApplier.addInstrumentationPoints(agentConfiguration, classType)).thenReturn(false);
			doReturn(assignment).when(instrumentationApplier).getSensorAssignment();
			doReturn(Collections.singleton(classType)).when(searchNarrower).narrowByClassSensorAssignment(classCache, assignment);

			Collection<? extends ImmutableClassType> result = instrumentation.addInstrumentationPoints(agentConfiguration, Collections.singleton(instrumentationApplier));

			// assert result
			assertThat((Collection<ClassType>) result, is(empty()));

			// must be write lock
			verify(classCache, times(1)).executeWithWriteLock(Mockito.<Callable<?>> any());
			verify(instrumentationApplier, times(1)).addInstrumentationPoints(agentConfiguration, classType);
			verify(instrumentationApplier, times(1)).getSensorAssignment();
			verifyNoMoreInteractions(instrumentationApplier);
		}

		@Test
		public void addNothingForNonInitializedType() throws Exception {
			when(classType.isInitialized()).thenReturn(false);
			doReturn(Collections.singleton(classType)).when(lookup).findAll();

			Collection<? extends ImmutableClassType> result = instrumentation.addInstrumentationPoints(agentConfiguration, Collections.singleton(instrumentationApplier));

			// assert result
			assertThat(result, is(empty()));

			// must be write lock
			verify(classCache, times(1)).executeWithWriteLock(Mockito.<Callable<?>> any());
			verify(instrumentationApplier, times(1)).getSensorAssignment();
			verifyNoMoreInteractions(instrumentationApplier);

		}

		@Test
		public void searchNarrowAddNothingForNonInitializedType() throws Exception {
			when(classType.isInitialized()).thenReturn(false);
			doReturn(assignment).when(instrumentationApplier).getSensorAssignment();
			doReturn(Collections.singleton(classType)).when(searchNarrower).narrowByClassSensorAssignment(classCache, assignment);

			Collection<? extends ImmutableClassType> result = instrumentation.addInstrumentationPoints(agentConfiguration, Collections.singleton(instrumentationApplier));

			// assert result
			assertThat(result, is(empty()));

			// must be write lock
			verify(classCache, times(1)).executeWithWriteLock(Mockito.<Callable<?>> any());
			verify(instrumentationApplier, times(1)).getSensorAssignment();
			verifyNoMoreInteractions(instrumentationApplier);

		}

		@Test
		public void addNothingWhenEmpty() throws Exception {
			doReturn(Collections.emptyList()).when(lookup).findAll();

			Collection<? extends ImmutableClassType> result = instrumentation.addInstrumentationPoints(agentConfiguration, Collections.singleton(instrumentationApplier));

			// assert result
			assertThat(result, is(empty()));

			// not touching the write lock
			verify(classCache, times(0)).executeWithWriteLock(Mockito.<Callable<?>> any());
			verify(instrumentationApplier, times(1)).getSensorAssignment();
			verifyNoMoreInteractions(instrumentationApplier);
		}

		@Test
		public void searchNarrowAddNothingWhenEmpty() throws Exception {
			doReturn(assignment).when(instrumentationApplier).getSensorAssignment();
			doReturn(Collections.emptyList()).when(searchNarrower).narrowByClassSensorAssignment(classCache, assignment);

			Collection<? extends ImmutableClassType> result = instrumentation.addInstrumentationPoints(agentConfiguration, Collections.singleton(instrumentationApplier));

			// assert result
			assertThat(result, is(empty()));

			// not touching the write lock
			verify(classCache, times(0)).executeWithWriteLock(Mockito.<Callable<?>> any());
			verify(instrumentationApplier, times(1)).getSensorAssignment();
			verifyNoMoreInteractions(instrumentationApplier);
		}

		@Test
		public void addNothingForNonClassTypes() throws Exception {
			AnnotationType annotationType = new AnnotationType("");
			InterfaceType interfaceType = new InterfaceType("");
			List<Type> types = new ArrayList<Type>();
			types.add(annotationType);
			types.add(interfaceType);
			doReturn(types).when(lookup).findAll();

			Collection<? extends ImmutableClassType> result = instrumentation.addInstrumentationPoints(agentConfiguration, Collections.singleton(instrumentationApplier));

			// assert result
			assertThat(result, is(empty()));

			// must be write lock
			verify(classCache, times(1)).executeWithWriteLock(Mockito.<Callable<?>> any());
			verify(instrumentationApplier, times(1)).getSensorAssignment();
			verifyNoMoreInteractions(instrumentationApplier);
		}

		@Test
		public void searchNarrowAddNothingForNonClassTypes() throws Exception {
			AnnotationType annotationType = new AnnotationType("");
			InterfaceType interfaceType = new InterfaceType("");
			List<Type> types = new ArrayList<Type>();
			types.add(annotationType);
			types.add(interfaceType);
			doReturn(assignment).when(instrumentationApplier).getSensorAssignment();
			doReturn(types).when(searchNarrower).narrowByClassSensorAssignment(classCache, assignment);

			Collection<? extends ImmutableClassType> result = instrumentation.addInstrumentationPoints(agentConfiguration, Collections.singleton(instrumentationApplier));

			// assert result
			assertThat(result, is(empty()));

			// must be write lock
			verify(classCache, times(1)).executeWithWriteLock(Mockito.<Callable<?>> any());
			verify(instrumentationApplier, times(1)).getSensorAssignment();
			verifyNoMoreInteractions(instrumentationApplier);
		}
	}

	public static class GetInstrumentationResults extends ClassCacheInstrumentationTest {

		@Test
		public void collect() throws Exception {
			Collection<MethodInstrumentationConfig> configs = mock(Collection.class);
			when(classType.isClass()).thenReturn(true);
			when(classType.castToClass()).thenReturn(classType);
			when(classType.isInitialized()).thenReturn(true);
			when(classType.getFQN()).thenReturn(FQN);
			when(classType.hasInstrumentationPoints()).thenReturn(true);
			when(classType.getInstrumentationPoints()).thenReturn(configs);
			doReturn(Collections.singleton(classType)).when(lookup).findAll();

			Collection<InstrumentationDefinition> result = instrumentation.getInstrumentationResults();

			// assert result
			assertThat(result, hasSize(1));
			InstrumentationDefinition instrumentationResult = result.iterator().next();
			assertThat(instrumentationResult.getClassName(), is(FQN));
			assertThat(instrumentationResult.getMethodInstrumentationConfigs(), is(configs));

			// read lock is enough
			verify(classCache, times(1)).executeWithReadLock(Mockito.<Callable<?>> any());
		}

		@Test
		public void collectNothingForNonInitializedType() throws Exception {
			when(classType.isInitialized()).thenReturn(false);
			doReturn(Collections.singleton(classType)).when(lookup).findAll();

			assertThat(instrumentation.getInstrumentationResults(), is(empty()));

			// must be read lock
			verify(classCache, times(1)).executeWithReadLock(Mockito.<Callable<?>> any());
			verifyZeroInteractions(log);
		}

		@Test
		public void collectNothingForNonClassType() throws Exception {
			when(classType.isClass()).thenReturn(false);
			when(classType.isInitialized()).thenReturn(true);
			doReturn(Collections.singleton(classType)).when(lookup).findAll();

			assertThat(instrumentation.getInstrumentationResults(), is(empty()));

			// must be read lock
			verify(classCache, times(1)).executeWithReadLock(Mockito.<Callable<?>> any());
		}

		@Test
		public void collectNothingWhenEmpty() throws Exception {
			doReturn(Collections.emptyList()).when(lookup).findAll();

			assertThat(instrumentation.getInstrumentationResults(), is(empty()));

			// not touching the read lock
			verify(classCache, times(0)).executeWithReadLock(Mockito.<Callable<?>> any());
		}

		@Test
		public void collectNothingForAnnotationTypes() throws Exception {
			doReturn(Collections.singleton(new AnnotationType(""))).when(lookup).findAll();

			assertThat(instrumentation.getInstrumentationResults(), is(empty()));

			// must be read lock
			verify(classCache, times(1)).executeWithReadLock(Mockito.<Callable<?>> any());
		}

		@Test
		public void collectNothingForInterfaceTypes() throws Exception {
			doReturn(Collections.singleton(new InterfaceType(""))).when(lookup).findAll();

			assertThat(instrumentation.getInstrumentationResults(), is(empty()));

			// must be read lock
			verify(classCache, times(1)).executeWithReadLock(Mockito.<Callable<?>> any());
		}

	}

	public static class GetInstrumentationResultsWithHashes extends ClassCacheInstrumentationTest {

		@Test
		public void collect() throws Exception {
			Collection<MethodInstrumentationConfig> configs = mock(Collection.class);
			Set<String> hashes = mock(Set.class);
			when(classType.isClass()).thenReturn(true);
			when(classType.castToClass()).thenReturn(classType);
			when(classType.isInitialized()).thenReturn(true);
			when(classType.getFQN()).thenReturn(FQN);
			when(classType.hasInstrumentationPoints()).thenReturn(true);
			when(classType.getInstrumentationPoints()).thenReturn(configs);
			when(classType.getHashes()).thenReturn(hashes);
			doReturn(Collections.singleton(classType)).when(lookup).findAll();

			Map<Collection<String>, InstrumentationDefinition> result = instrumentation.getInstrumentationResultsWithHashes();

			// assert result
			assertThat(result.size(), is(1));
			Entry<Collection<String>, InstrumentationDefinition> entry = result.entrySet().iterator().next();
			assertThat((Set<String>) entry.getKey(), is(hashes));
			assertThat(entry.getValue().getClassName(), is(FQN));
			assertThat(entry.getValue().getMethodInstrumentationConfigs(), is(configs));

			// read lock is enough
			verify(classCache, times(1)).executeWithReadLock(Mockito.<Callable<?>> any());
		}

		@Test
		public void collectNothingForNonInitializedType() throws Exception {
			when(classType.isInitialized()).thenReturn(false);
			doReturn(Collections.singleton(classType)).when(lookup).findAll();

			assertThat(instrumentation.getInstrumentationResultsWithHashes().entrySet(), is(empty()));

			// must be read lock
			verify(classCache, times(1)).executeWithReadLock(Mockito.<Callable<?>> any());
		}

		@Test
		public void collectNothingForNonClassType() throws Exception {
			when(classType.isClass()).thenReturn(false);
			when(classType.isInitialized()).thenReturn(true);
			doReturn(Collections.singleton(classType)).when(lookup).findAll();

			assertThat(instrumentation.getInstrumentationResultsWithHashes().entrySet(), is(empty()));

			// must be read lock
			verify(classCache, times(1)).executeWithReadLock(Mockito.<Callable<?>> any());
		}

		@Test
		public void collectNothingWhenEmpty() throws Exception {
			doReturn(Collections.emptyList()).when(lookup).findAll();

			assertThat(instrumentation.getInstrumentationResultsWithHashes().entrySet(), is(empty()));

			// not touching the read lock
			verify(classCache, times(0)).executeWithReadLock(Mockito.<Callable<?>> any());
		}

		@Test
		public void collectNothingForAnnotationTypes() throws Exception {
			doReturn(Collections.singleton(new AnnotationType(""))).when(lookup).findAll();

			assertThat(instrumentation.getInstrumentationResultsWithHashes().entrySet(), is(empty()));

			// must be read lock
			verify(classCache, times(1)).executeWithReadLock(Mockito.<Callable<?>> any());
		}

		@Test
		public void collectNothingForInterfaceTypes() throws Exception {
			doReturn(Collections.singleton(new InterfaceType(""))).when(lookup).findAll();

			assertThat(instrumentation.getInstrumentationResultsWithHashes().entrySet(), is(empty()));

			// must be read lock
			verify(classCache, times(1)).executeWithReadLock(Mockito.<Callable<?>> any());
		}

	}
}
