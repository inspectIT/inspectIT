package info.novatec.inspectit.cmr.instrumentation.classcache;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import info.novatec.inspectit.cmr.instrumentation.classcache.ClassCache;
import info.novatec.inspectit.cmr.instrumentation.classcache.ClassCacheLookup;
import info.novatec.inspectit.cmr.instrumentation.classcache.ClassCacheModification;
import info.novatec.inspectit.cmr.instrumentation.classcache.ClassCacheModificationException;
import info.novatec.inspectit.cmr.instrumentation.classcache.events.Events;
import info.novatec.inspectit.cmr.instrumentation.classcache.events.NodeEvent;
import info.novatec.inspectit.cmr.instrumentation.classcache.events.ReferenceEvent;
import info.novatec.inspectit.cmr.instrumentation.classcache.events.NodeEvent.NodeEventDetails;
import info.novatec.inspectit.cmr.instrumentation.classcache.events.NodeEvent.NodeEventType;
import info.novatec.inspectit.cmr.instrumentation.classcache.events.ReferenceEvent.ReferenceType;
import info.novatec.inspectit.cmr.test.AbstractTestNGLogSupport;
import info.novatec.inspectit.instrumentation.classcache.AnnotationType;
import info.novatec.inspectit.instrumentation.classcache.ClassType;
import info.novatec.inspectit.instrumentation.classcache.ImmutableType;
import info.novatec.inspectit.instrumentation.classcache.InterfaceType;
import info.novatec.inspectit.instrumentation.classcache.MethodType;
import info.novatec.inspectit.instrumentation.classcache.Modifiers;
import info.novatec.inspectit.instrumentation.classcache.Type;
import info.novatec.inspectit.instrumentation.classcache.TypeWithAnnotations;
import info.novatec.inspectit.instrumentation.classcache.TypeWithMethods;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Dimensions we need to test
 *
 * - general problems, the class cache should find and return an exception - generic adding (where
 * the type is not interesting) - generic merging - adding of methods - merging of methods -
 *
 * - entities -- types with methods -- types with annotations -- classtype specifics --
 * interfacetype specifics --
 *
 * @author Stefan Siegl
 *
 */
@SuppressWarnings("PMD")
public class ClassCacheModificationTest extends AbstractTestNGLogSupport {

	String fqn = "class";
	String hash = "hash";
	String hashStored = "hashStored";
	int modifiers = 0;

	@DataProvider
	public Object[][] types() {
		return new Object[][] { { ClassType.class }, { AnnotationType.class }, { InterfaceType.class } };
	}

	@DataProvider
	public Object[][] typesWithMethods() {
		return new Object[][] { { ClassType.class }, { InterfaceType.class } };
	}

	@DataProvider
	public Object[][] differentTypes() {
		return new Object[][] { { ClassType.class, AnnotationType.class }, { ClassType.class, InterfaceType.class }, { AnnotationType.class, ClassType.class },
				{ AnnotationType.class, InterfaceType.class }, { InterfaceType.class, ClassType.class }, { InterfaceType.class, AnnotationType.class } };
	}

	private ClassCacheModification service;

	@Mock
	private ClassCacheLookup lookup;

	@Mock
	private ClassCache cache;

	@Mock
	private Logger log;

	@BeforeMethod
	public void init() throws Exception {
		MockitoAnnotations.initMocks(this);

		Answer<Object> locksAnswer = new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				Callable<?> callable = (Callable<?>) invocation.getArguments()[0];
				return callable.call();
			}
		};
		doAnswer(locksAnswer).when(cache).executeWithReadLock(Mockito.<Callable<?>> any());
		doAnswer(locksAnswer).when(cache).executeWithWriteLock(Mockito.<Callable<?>> any());
		doReturn(lookup).when(cache).getLookupService();

		service = new ClassCacheModification();
		service.log = log;
		service.init(cache);
	}

	//
	// General error conditions
	//

	@Test(dataProvider = "types", expectedExceptions = { ClassCacheModificationException.class })
	public void ensureThatFQNIsNotNull(Class<? extends Type> type)
			throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassCacheModificationException {
		String fqn = null;
		String hash = "hash";
		int modifiers = 0;
		Type theClass = construct(type, fqn, hash, modifiers);
		service.merge(theClass);
	}

	@Test(dataProvider = "types", expectedExceptions = { ClassCacheModificationException.class })
	public void ensureThatNonNull(Class<? extends Type> type)
			throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassCacheModificationException {
		service.merge(null);
	}

	// TODO disabled because annotation as interface problem exists
	@Test(enabled = false, dataProvider = "differentTypes", expectedExceptions = { ClassCacheModificationException.class })
	public void ensureAddingDifferentTypesWithSameHashFails(Class<? extends Type> type1, Class<? extends Type> type2)
			throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassCacheModificationException {
		String hash = "hash";
		String fqn = "fqn";
		int modifiers = 0;

		Type storedClass = construct(type1, fqn, hash, modifiers);
		when(lookup.findByFQN(fqn)).thenReturn(storedClass);
		service.lookup = lookup;

		Type duplicateClass = construct(type2, fqn, hash, modifiers);

		service.merge(duplicateClass);
	}

	@Test(expectedExceptions = { ClassCacheModificationException.class })
	public void addingInvalidTypeFails() throws ClassCacheModificationException {
		String fqn = "fqn";

		Events events = service.merge(new Type(fqn) {

			@Override
			public void clearUnmeaningfulBackReferences() {
			}
		});

		assertEventsEmpty(events);
	}

	// -------------------------------
	//
	//
	// -------------------------------

	// Adding and merging of base entities situations: <br />
	// - not known entity that was initialized <br />
	// - not known entity that was not initialized
	// - known entity that is known as uninitialized and given as uninitialized
	// - known entity that is known as uninitialized and given as initialized
	// - known entity that is known as initialized and given as uninitialized
	// - known entity that is known as initialized and given as initialized
	// - same type should not be added twice
	// - merge modifiers

	@Test(dataProvider = "types")
	public void addNewInitializedTypeThatWasNotKnown(Class<? extends Type> type)
			throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassCacheModificationException {
		when(lookup.findByFQN(fqn)).thenReturn(null);
		service.lookup = lookup;

		Type theClass = construct(type, fqn, hash, modifiers);
		Events events = service.merge(theClass);

		Events expected = new Events();
		expected.addEvent(new NodeEvent(theClass, NodeEventType.NEW, NodeEventDetails.INITIALIZED, hash));

		assertEvents(events, expected);
	}

	@Test(dataProvider = "types", expectedExceptions = { ClassCacheModificationException.class })
	public void addNewUnInitializedTypeThatWasNotKnown(Class<? extends Type> type)
			throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassCacheModificationException {
		when(lookup.findByFQN(fqn)).thenReturn(null);
		service.lookup = lookup;

		Type theClass = construct(type, fqn);
		Events events = service.merge(theClass);

		Events expected = new Events();
		expected.addEvent(new NodeEvent(theClass, NodeEventType.NEW, NodeEventDetails.NOT_INITIALIZED, hash));

		assertEvents(events, expected);
	}

	@Test(dataProvider = "types")
	public void addNewInitializedTypeThatIsKnownUninitialized(Class<? extends Type> type)
			throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassCacheModificationException {
		Type notInitialized = construct(type, fqn);

		when(lookup.findByFQN(fqn)).thenReturn(notInitialized);
		service.lookup = lookup;

		Type initialized = construct(type, fqn, hash, modifiers);
		Events events = service.merge(initialized);

		Events expected = new Events();
		expected.addEvent(new NodeEvent(notInitialized, NodeEventType.CHANGED, NodeEventDetails.INITIALIZED, hash));

		assertEvents(events, expected);
	}

	@Test(dataProvider = "types")
	public void addNewInitializedTypeThatIsKnownInitializedWithDifferentHash(Class<? extends Type> type)
			throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassCacheModificationException {
		Type storedClass = construct(type, fqn, hashStored, modifiers);

		when(lookup.findByFQN(fqn)).thenReturn(storedClass);
		service.lookup = lookup;

		Type initialized = construct(type, fqn, hash, modifiers);
		Events events = service.merge(initialized);

		Events expected = new Events();
		expected.addEvent(new NodeEvent(storedClass, NodeEventType.CHANGED, NodeEventDetails.HASH_ADDED, hash));

		assertEvents(events, expected);
	}

	@Test(dataProvider = "types")
	public void addNewInitializedTypeThatIsKnownInitializedWithSameHash(Class<? extends Type> type)
			throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassCacheModificationException {
		Type storedClass = construct(type, fqn, hash, modifiers);
		when(lookup.findByFQN(fqn)).thenReturn(storedClass);
		service.lookup = lookup;

		Events events = service.merge(storedClass);

		assertEventsEmpty(events);
	}

	@Test(dataProvider = "types")
	public void mergeModifiers(Class<? extends Type> type)
			throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassCacheModificationException {
		String fqn = "fqn";
		int m = Modifiers.getModifiers(Modifier.PUBLIC);
		String hash = "hash";
		Type given = construct(type, fqn, hash, m);
		assertThat(Modifiers.isPublic(given.getModifiers()), is(true));

		String hashStored = "stored";
		int modStored = Modifiers.getModifiers(Modifier.PRIVATE);
		Type stored = construct(type, fqn, hashStored, modStored);

		assertThat(Modifiers.isPrivate(stored.getModifiers()), is(true));

		when(lookup.findByFQN(fqn)).thenReturn(stored);
		service.lookup = lookup;

		Events events = service.merge(given);

		// so now we expect that the stored on is both public and private
		assertThat(Modifiers.isPublic(stored.getModifiers()), is(true));
		assertThat(Modifiers.isPrivate(stored.getModifiers()), is(true));

		Events expected = new Events();
		expected.addEvent(new NodeEvent(stored, NodeEventType.CHANGED, NodeEventDetails.HASH_ADDED, hash));
		expected.addEvent(new NodeEvent(stored, NodeEventType.CHANGED, NodeEventDetails.MODIFIERS_CHANGED));

		assertEvents(events, expected);
	}

	// ------------------------------------------------------------------------------------
	// Reference section
	//
	// this sections provides tests for references, that are either there or not there or need to be
	// merged. Please note that we moved methods stuff away from here in its own section, as there
	// is a lot to do there
	//
	// facts
	// - the referred entities can only be given as uninitialized types, we will have a test that
	// giving an initialized referred type will log a warning
	//
	// --> handleTwoLevelInstanceAreNotSupported
	//
	// - all references have a direction (although they are ensured to be bi-directional). It is
	// not possible for the parser to give me the subclass of a given class, only the superclass.
	// Thus we have to ensure that giving us this "unmeaningful" references result in a log output
	// but no action.
	//
	// --> handleGivenSubclassIsNotSupported
	// --> handleGivenSubInterfaceIsNotSupported
	// --> handleGivenAnnotatedTypeIsNotSupported
	// --> handleGivenReferredClassIsNotSupported
	// --> handleGivenMethodThrowingThisExceptionIsNotSupported
	//
	// - dimensions we need to test every reference from
	// -- base type (stored) is initialized / not initialized / not there
	// -- given provides new reference / existing reference
	// -- referred type in given is there / not there
	//
	// base - given provides existing/new - ref. type available - meaningful
	// init - new - yes - y - mergeWithInitializedNewReferenceReferredTypeThere
	// init - new - not - y - mergeWithInitializedNewReferenceReferredTypeNotThere
	// init - exi - yes - y - mergeWithInitializedExistingReferenceReferredTypeThere
	// init - exi - not - n
	// noti - new - yes - y - mergeWithNotInitializedNewReferenceReferredTypeThere
	// noti - new - not - y - mergeWithNotInitializedNewReferenceReferredTypeNotThere
	// noti - exi - yes - n
	// noti - exi - not - n
	// nott - new - yes - y - addNewReferenceReferredTypeThere
	// nott - new - not - y - addNewReferenceReferredTypeNotThere
	// nott - exi - yes - n
	// nott - exi - not - n
	//
	// we need to do that for:
	// - annotation
	// - superclass
	// - superinterface
	// - realizedinterface
	//
	// what we need to ensure in addition:
	// - all merges and adds must be bidirectional, that is the referred type must be checked to
	// have the reference to this type as well.
	//
	// ------------------------------------------------------------------------------------

	// we keep this methods for easy template if another reference is to be added
	// @Test
	// public void mergeWithInitializedNewReferenceReferredTypeThere() throws
	// ClassCacheModificationException {
	//
	// }
	//
	// @Test
	// public void mergeWithInitializedNewReferenceReferredTypeNotThere() throws
	// ClassCacheModificationException {
	//
	// }
	//
	// @Test
	// public void mergeWithInitializedExistingReferenceReferredTypeThere() throws
	// ClassCacheModificationException {
	//
	// }
	//
	// @Test
	// public void mergeWithNotInitializedNewReferenceReferredTypeThere() throws
	// ClassCacheModificationException {
	//
	// }
	//
	// @Test
	// public void mergeWithNotInitializedNewReferenceReferredTypeNotThere() throws
	// ClassCacheModificationException {
	//
	// }
	//
	// @Test
	// public void addNewReferenceReferredTypeThere() throws ClassCacheModificationException {
	//
	// }
	//
	// @Test
	// public void addNewReferenceReferredTypeNotThere() throws ClassCacheModificationException {
	//
	// }

	//
	// Superclasses
	//

	@Test
	public void mergeWithInitializedNewSuperclassReferenceReferredTypeThere() throws ClassCacheModificationException {
		ClassType c = new ClassType(fqn, hash, modifiers);

		String fqnSuper = "fqnSuper";
		ClassType s = new ClassType(fqnSuper);
		c.addSuperClass(s);

		ClassType stored = new ClassType(fqn, hashStored, modifiers);
		String fqnAnotherSuperOfStored = "anothersuperofstored";
		ClassType anotherSuperOfStored = new ClassType(fqnAnotherSuperOfStored);
		stored.addSuperClass(anotherSuperOfStored);

		when(lookup.findByFQN(fqn)).thenReturn(stored);
		when(lookup.findByFQN(fqnSuper)).thenReturn(s);
		when(lookup.findByFQN(fqnAnotherSuperOfStored)).thenReturn(anotherSuperOfStored);
		service.lookup = lookup;

		Events events = service.merge(c);

		Events expected = new Events();
		expected.addEvent(new NodeEvent(c, NodeEventType.CHANGED, NodeEventDetails.HASH_ADDED, hash));
		expected.addEvent(new ReferenceEvent(stored, s, ReferenceType.SUPERCLASS));

		// assert bidirectional
		assertThat(stored.getSuperClasses().size(), is(2));
		assertThat(stored.getSuperClasses(), hasItem(s));
		assertThat(stored.getSuperClasses(), hasItem(anotherSuperOfStored));
		assertThat(s.getSubClasses(), hasItem(stored));
		assertThat(anotherSuperOfStored.getSubClasses(), hasItem(stored));

		assertEvents(events, expected);
	}

	@Test
	public void mergeWithInitializedNewSuperclassReferenceReferredTypeNotThere() throws ClassCacheModificationException {
		ClassType c = new ClassType(fqn, hash, modifiers);

		String fqnSuper = "fqnSuper";
		ClassType s = new ClassType(fqnSuper);
		c.addSuperClass(s);

		ClassType stored = new ClassType(fqn, hashStored, modifiers);
		String fqnAnotherSuperOfStored = "anothersuperofstored";
		ClassType anotherSuperOfStored = new ClassType(fqnAnotherSuperOfStored);
		stored.addSuperClass(anotherSuperOfStored);

		when(lookup.findByFQN(fqn)).thenReturn(stored);
		when(lookup.findByFQN(fqnSuper)).thenReturn(null);
		when(lookup.findByFQN(fqnAnotherSuperOfStored)).thenReturn(anotherSuperOfStored);
		service.lookup = lookup;

		Events events = service.merge(c);

		Events expected = new Events();
		expected.addEvent(new NodeEvent(c, NodeEventType.CHANGED, NodeEventDetails.HASH_ADDED, hash));
		expected.addEvent(new NodeEvent(s, NodeEventType.NEW, NodeEventDetails.NOT_INITIALIZED));
		expected.addEvent(new ReferenceEvent(stored, s, ReferenceType.SUPERCLASS));

		// assert bidirectional
		assertThat(stored.getSuperClasses().size(), is(2));
		assertThat(stored.getSuperClasses(), hasItem(s));
		assertThat(stored.getSuperClasses(), hasItem(anotherSuperOfStored));
		assertThat(s.getSubClasses(), hasItem(stored));
		assertThat(anotherSuperOfStored.getSubClasses(), hasItem(stored));

		assertEvents(events, expected);
	}

	@Test
	public void mergeWithInitializedExistingSuperclassReferenceReferredTypeThere() throws ClassCacheModificationException {
		ClassType c = new ClassType(fqn, hash, modifiers);

		String fqnSuper = "fqnSuper";
		ClassType s = new ClassType(fqnSuper);
		c.addSuperClass(s);

		ClassType stored = new ClassType(fqn, hashStored, modifiers);
		stored.addSuperClass(s);

		when(lookup.findByFQN(fqn)).thenReturn(stored);
		when(lookup.findByFQN(fqnSuper)).thenReturn(null);
		service.lookup = lookup;

		Events events = service.merge(c);

		Events expected = new Events();
		expected.addEvent(new NodeEvent(c, NodeEventType.CHANGED, NodeEventDetails.HASH_ADDED, hash));

		// assert bidirectional
		assertThat(stored.getSuperClasses().size(), is(1));
		assertThat(stored.getSuperClasses(), hasItem(s));
		assertThat(s.getSubClasses(), hasItem(stored));

		assertEvents(events, expected);
	}

	@Test
	public void mergeWithNotInitializedNewSuperclassReferenceReferredTypeThere() throws ClassCacheModificationException {
		ClassType c = new ClassType(fqn, hash, modifiers);

		String fqnSuper = "fqnSuper";
		ClassType s = new ClassType(fqnSuper);
		c.addSuperClass(s);

		ClassType stored = new ClassType(fqn);

		when(lookup.findByFQN(fqn)).thenReturn(stored);
		when(lookup.findByFQN(fqnSuper)).thenReturn(s);
		service.lookup = lookup;

		Events events = service.merge(c);

		Events expected = new Events();
		expected.addEvent(new NodeEvent(stored, NodeEventType.CHANGED, NodeEventDetails.INITIALIZED, hash));
		expected.addEvent(new ReferenceEvent(stored, s, ReferenceType.SUPERCLASS));

		// assert bidirectional
		assertThat(stored.getSuperClasses().size(), is(1));
		assertThat(stored.getSuperClasses(), hasItem(s));
		assertThat(s.getSubClasses(), hasItem(stored));

		assertEvents(events, expected);
	}

	@Test
	public void mergeWithNotInitializedNewSuperclassReferenceReferredTypeNotThere() throws ClassCacheModificationException {
		ClassType c = new ClassType(fqn, hash, modifiers);

		String fqnSuper = "fqnSuper";
		ClassType s = new ClassType(fqnSuper);
		c.addSuperClass(s);

		ClassType stored = new ClassType(fqn);

		when(lookup.findByFQN(fqn)).thenReturn(stored);
		when(lookup.findByFQN(fqnSuper)).thenReturn(null);
		service.lookup = lookup;

		Events events = service.merge(c);

		Events expected = new Events();
		expected.addEvent(new NodeEvent(stored, NodeEventType.CHANGED, NodeEventDetails.INITIALIZED, hash));
		expected.addEvent(new NodeEvent(s, NodeEventType.NEW, NodeEventDetails.NOT_INITIALIZED));
		expected.addEvent(new ReferenceEvent(stored, s, ReferenceType.SUPERCLASS));

		// assert bidirectional
		assertThat(stored.getSuperClasses().size(), is(1));
		assertThat(stored.getSuperClasses(), hasItem(s));
		assertThat(s.getSubClasses(), hasItem(stored));

		assertEvents(events, expected);
	}

	@Test
	public void addNewSuperclassReferenceReferredTypeThere() throws ClassCacheModificationException {
		ClassType c = new ClassType(fqn, hash, modifiers);

		String fqnSuper = "fqnSuper";
		ClassType s = new ClassType(fqnSuper);
		c.addSuperClass(s);

		when(lookup.findByFQN(fqn)).thenReturn(null);
		when(lookup.findByFQN(fqnSuper)).thenReturn(s);
		service.lookup = lookup;

		Events events = service.merge(c);

		Events expected = new Events();
		expected.addEvent(new NodeEvent(c, NodeEventType.NEW, NodeEventDetails.INITIALIZED, hash));
		expected.addEvent(new ReferenceEvent(c, s, ReferenceType.SUPERCLASS));

		// assert bidirectional
		assertThat(c.getSuperClasses().size(), is(1));
		assertThat(c.getSuperClasses(), hasItem(s));
		assertThat(s.getSubClasses(), hasItem(c));

		assertEvents(events, expected);
	}

	@Test
	public void addNewSuperclassReferenceReferredTypeNotThere() throws ClassCacheModificationException {
		ClassType c = new ClassType(fqn, hash, modifiers);

		String fqnSuper = "fqnSuper";
		ClassType s = new ClassType(fqnSuper);
		c.addSuperClass(s);

		when(lookup.findByFQN(fqn)).thenReturn(null);
		when(lookup.findByFQN(fqnSuper)).thenReturn(null);
		service.lookup = lookup;

		Events events = service.merge(c);

		Events expected = new Events();
		expected.addEvent(new NodeEvent(c, NodeEventType.NEW, NodeEventDetails.INITIALIZED, hash));
		expected.addEvent(new NodeEvent(s, NodeEventType.NEW, NodeEventDetails.NOT_INITIALIZED));
		expected.addEvent(new ReferenceEvent(c, s, ReferenceType.SUPERCLASS));

		// assert bidirectional
		assertThat(c.getSuperClasses().size(), is(1));
		assertThat(c.getSuperClasses(), hasItem(s));
		assertThat(s.getSubClasses(), hasItem(c));

		assertEvents(events, expected);
	}

	//
	// Super interface
	//

	@Test
	public void mergeWithInitializedNewSuperInterfaceReferredTypeThere() throws ClassCacheModificationException {
		InterfaceType c = new InterfaceType(fqn, hash, modifiers);

		String fqnSuper = "fqnSuper";
		InterfaceType s = new InterfaceType(fqnSuper);
		c.addSuperInterface(s);

		InterfaceType stored = new InterfaceType(fqn, hashStored, modifiers);
		String fqnAnotherSuperOfStored = "anothersuperofstored";
		InterfaceType anotherSuperOfStored = new InterfaceType(fqnAnotherSuperOfStored);
		stored.addSuperInterface(anotherSuperOfStored);

		when(lookup.findByFQN(fqn)).thenReturn(stored);
		when(lookup.findByFQN(fqnSuper)).thenReturn(s);
		when(lookup.findByFQN(fqnAnotherSuperOfStored)).thenReturn(anotherSuperOfStored);
		service.lookup = lookup;

		Events events = service.merge(c);

		Events expected = new Events();
		expected.addEvent(new NodeEvent(stored, NodeEventType.CHANGED, NodeEventDetails.HASH_ADDED, hash));
		expected.addEvent(new ReferenceEvent(stored, s, ReferenceType.SUPERINTERFACE));

		// assert bidirectional
		assertThat(stored.getSuperInterfaces().size(), is(2));
		assertThat(stored.getSuperInterfaces(), hasItem(s));
		assertThat(stored.getSuperInterfaces(), hasItem(anotherSuperOfStored));
		assertThat(s.getSubInterfaces(), hasItem(stored));
		assertThat(anotherSuperOfStored.getSubInterfaces(), hasItem(stored));

		assertEvents(events, expected);
	}

	@Test
	public void mergeWithInitializedNewSuperInterfaceReferredTypeNotThere() throws ClassCacheModificationException {
		InterfaceType c = new InterfaceType(fqn, hash, modifiers);

		String fqnSuper = "fqnSuper";
		InterfaceType s = new InterfaceType(fqnSuper);
		c.addSuperInterface(s);

		InterfaceType stored = new InterfaceType(fqn, hashStored, modifiers);
		String fqnAnotherSuperOfStored = "anothersuperofstored";
		InterfaceType anotherSuperOfStored = new InterfaceType(fqnAnotherSuperOfStored);
		stored.addSuperInterface(anotherSuperOfStored);

		when(lookup.findByFQN(fqn)).thenReturn(stored);
		when(lookup.findByFQN(fqnSuper)).thenReturn(null);
		when(lookup.findByFQN(fqnAnotherSuperOfStored)).thenReturn(anotherSuperOfStored);
		service.lookup = lookup;

		Events events = service.merge(c);

		Events expected = new Events();
		expected.addEvent(new NodeEvent(stored, NodeEventType.CHANGED, NodeEventDetails.HASH_ADDED, hash));
		expected.addEvent(new NodeEvent(s, NodeEventType.NEW, NodeEventDetails.NOT_INITIALIZED));
		expected.addEvent(new ReferenceEvent(stored, s, ReferenceType.SUPERINTERFACE));

		// assert bidirectional
		assertThat(stored.getSuperInterfaces().size(), is(2));
		assertThat(stored.getSuperInterfaces(), hasItem(s));
		assertThat(stored.getSuperInterfaces(), hasItem(anotherSuperOfStored));
		assertThat(s.getSubInterfaces(), hasItem(stored));
		assertThat(anotherSuperOfStored.getSubInterfaces(), hasItem(stored));

		assertEvents(events, expected);
	}

	@Test
	public void mergeWithInitializedExistingSuperInterfaceReferredTypeThere() throws ClassCacheModificationException {
		InterfaceType c = new InterfaceType(fqn, hash, modifiers);

		String fqnSuper = "fqnSuper";
		InterfaceType s = new InterfaceType(fqnSuper);
		c.addSuperInterface(s);

		InterfaceType stored = new InterfaceType(fqn, hashStored, modifiers);
		String fqnAnotherSuperOfStored = "anothersuperofstored";
		InterfaceType anotherSuperOfStored = new InterfaceType(fqnAnotherSuperOfStored);
		stored.addSuperInterface(anotherSuperOfStored);
		stored.addSuperInterface(s);

		when(lookup.findByFQN(fqn)).thenReturn(stored);
		when(lookup.findByFQN(fqnSuper)).thenReturn(s);
		when(lookup.findByFQN(fqnAnotherSuperOfStored)).thenReturn(anotherSuperOfStored);
		service.lookup = lookup;

		Events events = service.merge(c);

		Events expected = new Events();
		expected.addEvent(new NodeEvent(stored, NodeEventType.CHANGED, NodeEventDetails.HASH_ADDED, hash));
		// expected.addEvent(new NodeEvent(s, NodeEventType.NEW, NodeEventDetails.NOT_INITIALIZED));
		// expected.addEvent(new ReferenceEvent(stored, s, ReferenceType.SUPERINTERFACE));

		// assert bidirectional
		assertThat(stored.getSuperInterfaces().size(), is(2));
		assertThat(stored.getSuperInterfaces(), hasItem(s));
		assertThat(stored.getSuperInterfaces(), hasItem(anotherSuperOfStored));
		assertThat(s.getSubInterfaces(), hasItem(stored));
		assertThat(anotherSuperOfStored.getSubInterfaces(), hasItem(stored));

		assertEvents(events, expected);
	}

	@Test
	public void mergeWithNotInitializedNewSuperInterfaceReferredTypeThere() throws ClassCacheModificationException {
		InterfaceType c = new InterfaceType(fqn, hash, modifiers);

		String fqnSuper = "fqnSuper";
		InterfaceType s = new InterfaceType(fqnSuper);
		c.addSuperInterface(s);

		InterfaceType stored = new InterfaceType(fqn);

		when(lookup.findByFQN(fqn)).thenReturn(stored);
		when(lookup.findByFQN(fqnSuper)).thenReturn(s);
		service.lookup = lookup;

		Events events = service.merge(c);

		Events expected = new Events();
		expected.addEvent(new NodeEvent(stored, NodeEventType.CHANGED, NodeEventDetails.INITIALIZED, hash));
		// expected.addEvent(new NodeEvent(s, NodeEventType.NEW, NodeEventDetails.NOT_INITIALIZED));
		expected.addEvent(new ReferenceEvent(stored, s, ReferenceType.SUPERINTERFACE));

		// assert bidirectional
		assertThat(stored.getSuperInterfaces().size(), is(1));
		assertThat(stored.getSuperInterfaces(), hasItem(s));
		assertThat(s.getSubInterfaces(), hasItem(stored));

		assertEvents(events, expected);
	}

	@Test
	public void mergeWithNotInitializedNewSuperInterfaceReferredTypeNotThere() throws ClassCacheModificationException {
		InterfaceType c = new InterfaceType(fqn, hash, modifiers);

		String fqnSuper = "fqnSuper";
		InterfaceType s = new InterfaceType(fqnSuper);
		c.addSuperInterface(s);

		InterfaceType stored = new InterfaceType(fqn);

		when(lookup.findByFQN(fqn)).thenReturn(stored);
		when(lookup.findByFQN(fqnSuper)).thenReturn(null);
		service.lookup = lookup;

		Events events = service.merge(c);

		Events expected = new Events();
		expected.addEvent(new NodeEvent(stored, NodeEventType.CHANGED, NodeEventDetails.INITIALIZED, hash));
		expected.addEvent(new NodeEvent(s, NodeEventType.NEW, NodeEventDetails.NOT_INITIALIZED));
		expected.addEvent(new ReferenceEvent(stored, s, ReferenceType.SUPERINTERFACE));

		// assert bidirectional
		assertThat(stored.getSuperInterfaces().size(), is(1));
		assertThat(stored.getSuperInterfaces(), hasItem(s));
		assertThat(s.getSubInterfaces(), hasItem(stored));

		assertEvents(events, expected);
	}

	@Test
	public void addNewSuperInterfaceReferredTypeThere() throws ClassCacheModificationException {
		InterfaceType c = new InterfaceType(fqn, hash, modifiers);

		String fqnSuper = "fqnSuper";
		InterfaceType s = new InterfaceType(fqnSuper);
		c.addSuperInterface(s);

		when(lookup.findByFQN(fqnSuper)).thenReturn(s);
		service.lookup = lookup;

		Events events = service.merge(c);

		Events expected = new Events();
		expected.addEvent(new NodeEvent(c, NodeEventType.NEW, NodeEventDetails.INITIALIZED, hash));
		// expected.addEvent(new NodeEvent(s, NodeEventType.NEW, NodeEventDetails.NOT_INITIALIZED));
		expected.addEvent(new ReferenceEvent(c, s, ReferenceType.SUPERINTERFACE));

		// assert bidirectional
		assertThat(c.getSuperInterfaces().size(), is(1));
		assertThat(c.getSuperInterfaces(), hasItem(s));
		assertThat(s.getSubInterfaces(), hasItem(c));

		assertEvents(events, expected);
	}

	@Test
	public void addNewSuperInterfaceReferredTypeNotThere() throws ClassCacheModificationException {
		InterfaceType c = new InterfaceType(fqn, hash, modifiers);

		String fqnSuper = "fqnSuper";
		InterfaceType s = new InterfaceType(fqnSuper);
		c.addSuperInterface(s);

		when(lookup.findByFQN(fqnSuper)).thenReturn(s);
		service.lookup = lookup;

		Events events = service.merge(c);

		Events expected = new Events();
		expected.addEvent(new NodeEvent(c, NodeEventType.NEW, NodeEventDetails.INITIALIZED, hash));
		// expected.addEvent(new NodeEvent(s, NodeEventType.NEW, NodeEventDetails.NOT_INITIALIZED));
		expected.addEvent(new ReferenceEvent(c, s, ReferenceType.SUPERINTERFACE));

		// assert bidirectional
		assertThat(c.getSuperInterfaces().size(), is(1));
		assertThat(c.getSuperInterfaces(), hasItem(s));
		assertThat(s.getSubInterfaces(), hasItem(c));

		assertEvents(events, expected);
	}

	//
	// Annotation
	//

	@Test(dataProvider = "types")
	public void mergeWithInitializedNewAnnotationReferredTypeThere(Class<? extends Type> clazz)
			throws ClassCacheModificationException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		TypeWithAnnotations c = construct(clazz, fqn, hash, modifiers);

		String annotationFQN = "fqnAnnotation";
		AnnotationType annotation = new AnnotationType(annotationFQN);
		c.addAnnotation(annotation);

		TypeWithAnnotations stored = construct(clazz, fqn, hashStored, modifiers);
		String fqnAnnotherAnnotation = "fqnAnnotherAnnotation";
		AnnotationType annotherAnnotation = new AnnotationType(fqnAnnotherAnnotation);
		stored.addAnnotation(annotherAnnotation);

		// we assume that nothing is there yet
		when(lookup.findByFQN(fqn)).thenReturn((Type) stored);
		when(lookup.findByFQN(annotationFQN)).thenReturn(annotation);
		service.lookup = lookup;

		Events events = service.merge((Type) c);

		// assert bidirectional
		assertThat(stored.getAnnotations().size(), is(2));
		assertThat(stored.getAnnotations(), hasItem(annotation));
		assertThat(stored.getAnnotations(), hasItem(annotherAnnotation));
		assertThat(annotation.getAnnotatedTypes(), hasItem(stored));
		assertThat(annotherAnnotation.getAnnotatedTypes(), hasItem(stored));

		Events expected = new Events();
		expected.addEvent(new NodeEvent((Type) stored, NodeEventType.CHANGED, NodeEventDetails.HASH_ADDED, hash));
		// expected.addEvent(new NodeEvent(annotation, NodeEventType.NEW,
		// NodeEventDetails.NOT_INITIALIZED));
		expected.addEvent(new ReferenceEvent((Type) stored, annotation, ReferenceType.ANNOTATION));

		assertEvents(events, expected);
	}

	@Test(dataProvider = "types")
	public void mergeWithInitializedNewAnnotationReferredTypeNotThere(Class<? extends Type> clazz) throws Exception {
		TypeWithAnnotations c = construct(clazz, fqn, hash, modifiers);

		String annotationFQN = "fqnAnnotation";
		AnnotationType annotation = new AnnotationType(annotationFQN);
		c.addAnnotation(annotation);

		TypeWithAnnotations stored = construct(clazz, fqn, hashStored, modifiers);
		String fqnAnnotherAnnotation = "fqnAnnotherAnnotation";
		AnnotationType annotherAnnotation = new AnnotationType(fqnAnnotherAnnotation);
		stored.addAnnotation(annotherAnnotation);

		// we assume that nothing is there yet
		when(lookup.findByFQN(fqn)).thenReturn((Type) stored);
		when(lookup.findByFQN(annotationFQN)).thenReturn(null);
		service.lookup = lookup;

		Events events = service.merge((Type) c);

		// assert bidirectional
		assertThat(stored.getAnnotations().size(), is(2));
		assertThat(stored.getAnnotations(), hasItem(annotation));
		assertThat(stored.getAnnotations(), hasItem(annotherAnnotation));
		assertThat(annotation.getAnnotatedTypes(), hasItem(stored));
		assertThat(annotherAnnotation.getAnnotatedTypes(), hasItem(stored));

		Events expected = new Events();
		expected.addEvent(new NodeEvent((Type) stored, NodeEventType.CHANGED, NodeEventDetails.HASH_ADDED, hash));
		expected.addEvent(new NodeEvent(annotation, NodeEventType.NEW, NodeEventDetails.NOT_INITIALIZED));
		expected.addEvent(new ReferenceEvent((Type) stored, annotation, ReferenceType.ANNOTATION));

		assertEvents(events, expected);
	}

	@Test(dataProvider = "types")
	public void mergeWithInitializedExistingAnnotationReferredTypeThere(Class<? extends Type> clazz) throws Exception {
		TypeWithAnnotations c = construct(clazz, fqn, hash, modifiers);

		String annotationFQN = "fqnAnnotation";
		AnnotationType annotation = new AnnotationType(annotationFQN);
		c.addAnnotation(annotation);

		TypeWithAnnotations stored = construct(clazz, fqn, hashStored, modifiers);
		String fqnAnnotherAnnotation = "fqnAnnotherAnnotation";
		AnnotationType annotherAnnotation = new AnnotationType(fqnAnnotherAnnotation);
		stored.addAnnotation(annotherAnnotation);
		stored.addAnnotation(annotation);

		// we assume that nothing is there yet
		when(lookup.findByFQN(fqn)).thenReturn((Type) stored);
		when(lookup.findByFQN(annotationFQN)).thenReturn(annotation);
		service.lookup = lookup;

		Events events = service.merge((Type) c);

		// assert bidirectional
		assertThat(stored.getAnnotations().size(), is(2));
		assertThat(stored.getAnnotations(), hasItem(annotation));
		assertThat(stored.getAnnotations(), hasItem(annotherAnnotation));
		assertThat(annotation.getAnnotatedTypes(), hasItem(stored));
		assertThat(annotherAnnotation.getAnnotatedTypes(), hasItem(stored));

		Events expected = new Events();
		expected.addEvent(new NodeEvent((Type) stored, NodeEventType.CHANGED, NodeEventDetails.HASH_ADDED, hash));
		// expected.addEvent(new NodeEvent(annotation, NodeEventType.NEW,
		// NodeEventDetails.NOT_INITIALIZED));
		// expected.addEvent(new ReferenceEvent((Type) stored, annotation,
		// ReferenceType.ANNOTATION));

		assertEvents(events, expected);
	}

	@Test(dataProvider = "types")
	public void mergeWithNotInitializedNewAnnotationReferredTypeThere(Class<? extends Type> clazz) throws Exception {
		TypeWithAnnotations c = construct(clazz, fqn, hash, modifiers);

		String annotationFQN = "fqnAnnotation";
		AnnotationType annotation = new AnnotationType(annotationFQN);
		c.addAnnotation(annotation);

		TypeWithAnnotations stored = construct(clazz, fqn);

		// we assume that nothing is there yet
		when(lookup.findByFQN(fqn)).thenReturn((Type) stored);
		when(lookup.findByFQN(annotationFQN)).thenReturn(annotation);
		service.lookup = lookup;

		Events events = service.merge((Type) c);

		// assert bidirectional
		assertThat(stored.getAnnotations().size(), is(1));
		assertThat(stored.getAnnotations(), hasItem(annotation));
		assertThat(annotation.getAnnotatedTypes(), hasItem(stored));

		Events expected = new Events();
		expected.addEvent(new NodeEvent((Type) stored, NodeEventType.CHANGED, NodeEventDetails.INITIALIZED, hash));
		// expected.addEvent(new NodeEvent(annotation, NodeEventType.NEW,
		// NodeEventDetails.NOT_INITIALIZED));
		expected.addEvent(new ReferenceEvent((Type) stored, annotation, ReferenceType.ANNOTATION));

		assertEvents(events, expected);
	}

	@Test(dataProvider = "types")
	public void mergeWithNotInitializedNewAnnotationReferredTypeNotThere(Class<? extends Type> clazz) throws Exception {
		TypeWithAnnotations c = construct(clazz, fqn, hash, modifiers);

		String annotationFQN = "fqnAnnotation";
		AnnotationType annotation = new AnnotationType(annotationFQN);
		c.addAnnotation(annotation);

		TypeWithAnnotations stored = construct(clazz, fqn);

		// we assume that nothing is there yet
		when(lookup.findByFQN(fqn)).thenReturn((Type) stored);
		when(lookup.findByFQN(annotationFQN)).thenReturn(null);
		service.lookup = lookup;

		Events events = service.merge((Type) c);

		// assert bidirectional
		assertThat(stored.getAnnotations().size(), is(1));
		assertThat(stored.getAnnotations(), hasItem(annotation));
		assertThat(annotation.getAnnotatedTypes(), hasItem(stored));

		Events expected = new Events();
		expected.addEvent(new NodeEvent((Type) stored, NodeEventType.CHANGED, NodeEventDetails.INITIALIZED, hash));
		expected.addEvent(new NodeEvent(annotation, NodeEventType.NEW, NodeEventDetails.NOT_INITIALIZED));
		expected.addEvent(new ReferenceEvent((Type) stored, annotation, ReferenceType.ANNOTATION));

		assertEvents(events, expected);
	}

	@Test(dataProvider = "types")
	public void addNewAnnotationReferredTypeThere(Class<? extends Type> clazz) throws Exception {
		TypeWithAnnotations c = construct(clazz, fqn, hash, modifiers);

		String annotationFQN = "fqnAnnotation";
		AnnotationType annotation = new AnnotationType(annotationFQN);
		c.addAnnotation(annotation);

		// we assume that nothing is there yet
		when(lookup.findByFQN(fqn)).thenReturn((Type) null);
		when(lookup.findByFQN(annotationFQN)).thenReturn(annotation);
		service.lookup = lookup;

		Events events = service.merge((Type) c);

		// assert bidirectional
		assertThat(c.getAnnotations().size(), is(1));
		assertThat(c.getAnnotations(), hasItem(annotation));
		assertThat(annotation.getAnnotatedTypes(), hasItem(c));

		Events expected = new Events();
		expected.addEvent(new NodeEvent((Type) c, NodeEventType.NEW, NodeEventDetails.INITIALIZED, hash));
		// expected.addEvent(new NodeEvent(annotation, NodeEventType.NEW,
		// NodeEventDetails.NOT_INITIALIZED));
		expected.addEvent(new ReferenceEvent((Type) c, annotation, ReferenceType.ANNOTATION));

		assertEvents(events, expected);
	}

	@Test(dataProvider = "types")
	public void addNewAnnotationReferredTypeNotThere(Class<? extends Type> clazz) throws Exception {
		TypeWithAnnotations c = construct(clazz, fqn, hash, modifiers);

		String annotationFQN = "fqnAnnotation";
		AnnotationType annotation = new AnnotationType(annotationFQN);
		c.addAnnotation(annotation);

		// we assume that nothing is there yet
		when(lookup.findByFQN(fqn)).thenReturn((Type) null);
		when(lookup.findByFQN(annotationFQN)).thenReturn(null);
		service.lookup = lookup;

		Events events = service.merge((Type) c);

		// assert bidirectional
		assertThat(c.getAnnotations().size(), is(1));
		assertThat(c.getAnnotations(), hasItem(annotation));
		assertThat(annotation.getAnnotatedTypes(), hasItem(c));

		Events expected = new Events();
		expected.addEvent(new NodeEvent((Type) c, NodeEventType.NEW, NodeEventDetails.INITIALIZED, hash));
		expected.addEvent(new NodeEvent(annotation, NodeEventType.NEW, NodeEventDetails.NOT_INITIALIZED));
		expected.addEvent(new ReferenceEvent((Type) c, annotation, ReferenceType.ANNOTATION));

		assertEvents(events, expected);
	}

	//
	// Realized interface
	//

	@Test
	public void mergeWithInitializedNewRealizedInterfaceReferredTypeThere() throws ClassCacheModificationException {
		ClassType c = new ClassType(fqn, hash, modifiers);

		// note that the interface can only be non-initialized
		String fqnSuper = "fqnSuper";
		InterfaceType s = new InterfaceType(fqnSuper);
		c.addInterface(s);

		ClassType stored = new ClassType(fqn, hashStored, modifiers);
		String fqnAnotherInterface = "fqnAnotherInterface";
		InterfaceType anotherInterface = new InterfaceType(fqnAnotherInterface);
		stored.addInterface(anotherInterface);

		// we assume that nothing is there yet
		when(lookup.findByFQN(fqn)).thenReturn(stored);
		when(lookup.findByFQN(fqnSuper)).thenReturn(s);
		service.lookup = lookup;

		Events events = service.merge(c);

		Events expected = new Events();
		expected.addEvent(new NodeEvent(stored, NodeEventType.CHANGED, NodeEventDetails.HASH_ADDED, hash));
		// expected.addEvent(new NodeEvent(s, NodeEventType.NEW, NodeEventDetails.NOT_INITIALIZED));
		expected.addEvent(new ReferenceEvent(stored, s, ReferenceType.REALIZE_INTERFACE));

		// assert bidirectional
		assertThat(stored.getRealizedInterfaces().size(), is(2));
		assertThat(stored.getRealizedInterfaces(), hasItem(s));
		assertThat(stored.getRealizedInterfaces(), hasItem(anotherInterface));
		assertThat(s.getRealizingClasses(), hasItem(stored));
		assertThat(anotherInterface.getRealizingClasses(), hasItem(stored));

		assertEvents(events, expected);
	}

	@Test
	public void mergeWithInitializedNewRealizedInterfaceReferredTypeNotThere() throws ClassCacheModificationException {
		ClassType c = new ClassType(fqn, hash, modifiers);

		// note that the interface can only be non-initialized
		String fqnSuper = "fqnSuper";
		InterfaceType s = new InterfaceType(fqnSuper);
		c.addInterface(s);

		ClassType stored = new ClassType(fqn, hashStored, modifiers);
		String fqnAnotherInterface = "fqnAnotherInterface";
		InterfaceType anotherInterface = new InterfaceType(fqnAnotherInterface);
		stored.addInterface(anotherInterface);

		// we assume that nothing is there yet
		when(lookup.findByFQN(fqn)).thenReturn(stored);
		when(lookup.findByFQN(fqnSuper)).thenReturn(null);
		service.lookup = lookup;

		Events events = service.merge(c);

		Events expected = new Events();
		expected.addEvent(new NodeEvent(stored, NodeEventType.CHANGED, NodeEventDetails.HASH_ADDED, hash));
		expected.addEvent(new NodeEvent(s, NodeEventType.NEW, NodeEventDetails.NOT_INITIALIZED));
		expected.addEvent(new ReferenceEvent(stored, s, ReferenceType.REALIZE_INTERFACE));

		// assert bidirectional
		assertThat(stored.getRealizedInterfaces().size(), is(2));
		assertThat(stored.getRealizedInterfaces(), hasItem(s));
		assertThat(stored.getRealizedInterfaces(), hasItem(anotherInterface));
		assertThat(s.getRealizingClasses(), hasItem(stored));
		assertThat(anotherInterface.getRealizingClasses(), hasItem(stored));

		assertEvents(events, expected);
	}

	@Test
	public void mergeWithInitializedExistingRealizedInterfaceReferredTypeThere() throws ClassCacheModificationException {
		ClassType c = new ClassType(fqn, hash, modifiers);

		// note that the interface can only be non-initialized
		String fqnSuper = "fqnSuper";
		InterfaceType s = new InterfaceType(fqnSuper);
		c.addInterface(s);

		ClassType stored = new ClassType(fqn, hashStored, modifiers);
		String fqnAnotherInterface = "fqnAnotherInterface";
		InterfaceType anotherInterface = new InterfaceType(fqnAnotherInterface);
		stored.addInterface(anotherInterface);
		stored.addInterface(s);

		// we assume that nothing is there yet
		when(lookup.findByFQN(fqn)).thenReturn(stored);
		when(lookup.findByFQN(fqnSuper)).thenReturn(s);
		service.lookup = lookup;

		Events events = service.merge(c);

		Events expected = new Events();
		expected.addEvent(new NodeEvent(stored, NodeEventType.CHANGED, NodeEventDetails.HASH_ADDED, hash));
		// expected.addEvent(new NodeEvent(s, NodeEventType.NEW, NodeEventDetails.NOT_INITIALIZED));
		// expected.addEvent(new ReferenceEvent(stored, s, ReferenceType.REALIZE_INTERFACE));

		// assert bidirectional
		assertThat(stored.getRealizedInterfaces().size(), is(2));
		assertThat(stored.getRealizedInterfaces(), hasItem(s));
		assertThat(stored.getRealizedInterfaces(), hasItem(anotherInterface));
		assertThat(s.getRealizingClasses(), hasItem(stored));
		assertThat(anotherInterface.getRealizingClasses(), hasItem(stored));

		assertEvents(events, expected);
	}

	@Test
	public void mergeWithNotInitializedNewRealizedInterfaceReferredTypeThere() throws ClassCacheModificationException {
		ClassType c = new ClassType(fqn, hash, modifiers);

		// note that the interface can only be non-initialized
		String fqnSuper = "fqnSuper";
		InterfaceType s = new InterfaceType(fqnSuper);
		c.addInterface(s);

		ClassType stored = new ClassType(fqn);

		// we assume that nothing is there yet
		when(lookup.findByFQN(fqn)).thenReturn(stored);
		when(lookup.findByFQN(fqnSuper)).thenReturn(s);
		service.lookup = lookup;

		Events events = service.merge(c);

		Events expected = new Events();
		expected.addEvent(new NodeEvent(stored, NodeEventType.CHANGED, NodeEventDetails.INITIALIZED, hash));
		// expected.addEvent(new NodeEvent(s, NodeEventType.NEW, NodeEventDetails.NOT_INITIALIZED));
		expected.addEvent(new ReferenceEvent(stored, s, ReferenceType.REALIZE_INTERFACE));

		// assert bidirectional
		assertThat(stored.getRealizedInterfaces().size(), is(1));
		assertThat(stored.getRealizedInterfaces(), hasItem(s));
		assertThat(s.getRealizingClasses(), hasItem(stored));

		assertEvents(events, expected);
	}

	@Test
	public void mergeWithNotInitializedNewRealizedInterfaceReferredTypeNotThere() throws ClassCacheModificationException {
		ClassType c = new ClassType(fqn, hash, modifiers);

		// note that the interface can only be non-initialized
		String fqnSuper = "fqnSuper";
		InterfaceType s = new InterfaceType(fqnSuper);
		c.addInterface(s);

		ClassType stored = new ClassType(fqn);

		// we assume that nothing is there yet
		when(lookup.findByFQN(fqn)).thenReturn(stored);
		when(lookup.findByFQN(fqnSuper)).thenReturn(null);
		service.lookup = lookup;

		Events events = service.merge(c);

		Events expected = new Events();
		expected.addEvent(new NodeEvent(stored, NodeEventType.CHANGED, NodeEventDetails.INITIALIZED, hash));
		expected.addEvent(new NodeEvent(s, NodeEventType.NEW, NodeEventDetails.NOT_INITIALIZED));
		expected.addEvent(new ReferenceEvent(stored, s, ReferenceType.REALIZE_INTERFACE));

		// assert bidirectional
		assertThat(stored.getRealizedInterfaces().size(), is(1));
		assertThat(stored.getRealizedInterfaces(), hasItem(s));
		assertThat(s.getRealizingClasses(), hasItem(stored));

		assertEvents(events, expected);
	}

	@Test
	public void addNewRealizedInterfaceReferredTypeThere() throws ClassCacheModificationException {
		ClassType c = new ClassType(fqn, hash, modifiers);

		// note that the interface can only be non-initialized
		String fqnSuper = "fqnSuper";
		InterfaceType s = new InterfaceType(fqnSuper);
		c.addInterface(s);

		// we assume that nothing is there yet
		when(lookup.findByFQN(fqn)).thenReturn(null);
		when(lookup.findByFQN(fqnSuper)).thenReturn(s);
		service.lookup = lookup;

		Events events = service.merge(c);

		Events expected = new Events();
		expected.addEvent(new NodeEvent(c, NodeEventType.NEW, NodeEventDetails.INITIALIZED, hash));
		// expected.addEvent(new NodeEvent(s, NodeEventType.NEW, NodeEventDetails.NOT_INITIALIZED));
		expected.addEvent(new ReferenceEvent(c, s, ReferenceType.REALIZE_INTERFACE));

		// assert bidirectional
		assertThat(c.getRealizedInterfaces().size(), is(1));
		assertThat(c.getRealizedInterfaces(), hasItem(s));
		assertThat(s.getRealizingClasses(), hasItem(c));

		assertEvents(events, expected);
	}

	@Test
	public void addNewRealizedInterfaceReferredTypeNotThere() throws ClassCacheModificationException {
		ClassType c = new ClassType(fqn, hash, modifiers);

		// note that the interface can only be non-initialized
		String fqnSuper = "fqnSuper";
		InterfaceType s = new InterfaceType(fqnSuper);
		c.addInterface(s);

		// we assume that nothing is there yet
		when(lookup.findByFQN(fqn)).thenReturn(null);
		when(lookup.findByFQN(fqnSuper)).thenReturn(null);
		service.lookup = lookup;

		Events events = service.merge(c);

		Events expected = new Events();
		expected.addEvent(new NodeEvent(c, NodeEventType.NEW, NodeEventDetails.INITIALIZED, hash));
		expected.addEvent(new NodeEvent(s, NodeEventType.NEW, NodeEventDetails.NOT_INITIALIZED));
		expected.addEvent(new ReferenceEvent(c, s, ReferenceType.REALIZE_INTERFACE));

		// assert bidirectional
		assertThat(c.getRealizedInterfaces().size(), is(1));
		assertThat(c.getRealizedInterfaces(), hasItem(s));
		assertThat(s.getRealizingClasses(), hasItem(c));

		assertEvents(events, expected);
	}

	@Test
	public void handleTwoLevelInstanceAreNotSupported() throws ClassCacheModificationException {
		// that is a class, that has a superclass, that has a superclass
		String[] fqns = new String[] { "1", "2", "3" };
		int[] modifiers = new int[] { 0, 1, 2 };
		String[] hashes = new String[] { "a", "b", "c" };

		ClassType base = new ClassType(fqns[0], hashes[0], modifiers[0]);
		ClassType firstSuper = new ClassType(fqns[1], hashes[1], modifiers[1]);
		ClassType secondSuper = new ClassType(fqns[2], hashes[2], modifiers[2]);

		base.addSuperClass(firstSuper);
		firstSuper.addSuperClass(secondSuper);

		// we assume that nothing is there yet
		when(lookup.findByFQN(fqns[0])).thenReturn(null);
		when(lookup.findByFQN(fqns[1])).thenReturn(null);
		when(lookup.findByFQN(fqns[2])).thenReturn(null);
		service.lookup = lookup;

		Events events = service.merge(base);

		// We do not support multi-level structures!
		Events expected = new Events();
		expected.addEvent(new NodeEvent(base, NodeEventType.NEW, NodeEventDetails.INITIALIZED, hashes[0]));
		expected.addEvent(new NodeEvent(firstSuper, NodeEventType.NEW, NodeEventDetails.NOT_INITIALIZED));
		expected.addEvent(new ReferenceEvent(base, firstSuper, ReferenceType.SUPERCLASS));

		assertEvents(events, expected);
	}

	//
	// handle types coming from the lookup to be replaced
	//

	@Test(dataProvider = "types")
	public void annotationReplacedWithInitialized(Class<? extends Type> clazz)
			throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassCacheModificationException {
		TypeWithAnnotations t = construct(clazz, fqn, hash, modifiers);
		AnnotationType a = new AnnotationType("annotation");
		t.addAnnotation(a);

		AnnotationType initialized = new AnnotationType("annotation", hash, modifiers);

		when(lookup.findByFQN(fqn)).thenReturn(null);
		when(lookup.findByFQN("annotation")).thenReturn(initialized);

		service.lookup = lookup;
		service.merge((ImmutableType) t);

		// make sure not initialized reference is gone
		assertThat(t.getAnnotations(), hasSize(1));
		assertThat(t.getAnnotations().iterator().next().isInitialized(), is(true));
	}

	@Test
	public void superClassReplacedWithInitialized() throws ClassCacheModificationException {
		ClassType c = new ClassType(fqn, hash, modifiers);
		ClassType s = new ClassType("superClass");
		c.addSuperClass(s);

		ClassType initialized = new ClassType("superClass", hash, modifiers);

		when(lookup.findByFQN(fqn)).thenReturn(null);
		when(lookup.findByFQN("superClass")).thenReturn(initialized);

		service.lookup = lookup;
		service.merge(c);

		// make sure not initialized reference is gone
		assertThat(c.getSuperClasses(), hasSize(1));
		assertThat(c.getSuperClasses().iterator().next().isInitialized(), is(true));
	}

	@Test
	public void realizedInterfacesReplacedWithInitialized() throws ClassCacheModificationException {
		ClassType c = new ClassType(fqn, hash, modifiers);
		InterfaceType i = new InterfaceType("interface");
		c.addInterface(i);

		InterfaceType initialized = new InterfaceType("interface", hash, modifiers);

		when(lookup.findByFQN(fqn)).thenReturn(null);
		when(lookup.findByFQN("interface")).thenReturn(initialized);

		service.lookup = lookup;
		service.merge(c);

		// make sure not initialized reference is gone
		assertThat(c.getRealizedInterfaces(), hasSize(1));
		assertThat(c.getRealizedInterfaces().iterator().next().isInitialized(), is(true));
	}

	@Test
	public void superInterfacesReplacedWithInitialized() throws ClassCacheModificationException {
		InterfaceType i = new InterfaceType(fqn, hash, modifiers);
		InterfaceType s = new InterfaceType("interface");
		i.addSuperInterface(s);

		InterfaceType initialized = new InterfaceType("interface", hash, modifiers);

		when(lookup.findByFQN(fqn)).thenReturn(null);
		when(lookup.findByFQN("interface")).thenReturn(initialized);

		service.lookup = lookup;
		service.merge(i);

		// make sure not initialized reference is gone
		assertThat(i.getSuperInterfaces(), hasSize(1));
		assertThat(i.getSuperInterfaces().iterator().next().isInitialized(), is(true));
	}

	@Test(dataProvider = "typesWithMethods")
	public void methodAnnotatedReplacedWithWithInitialized(Class<? extends Type> clazz)
			throws ClassCacheModificationException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		TypeWithMethods t = (TypeWithMethods) construct(clazz, fqn, hash, modifiers);
		AnnotationType a = new AnnotationType("annotation");
		MethodType m = MethodType.build("name", modifiers, null, null, null, new HashSet<>(Arrays.asList(new AnnotationType[] { a })));
		t.addMethod(m);

		AnnotationType initialized = new AnnotationType("annotation", hash, modifiers);

		when(lookup.findByFQN(fqn)).thenReturn(null);
		when(lookup.findByFQN("annotation")).thenReturn(initialized);

		service.lookup = lookup;
		service.merge(t);

		assertThat(m.getAnnotations(), hasSize(1));
		assertThat(m.getAnnotations().iterator().next().isInitialized(), is(true));
	}

	@Test(dataProvider = "typesWithMethods")
	public void methodExceptionReplacedWithWithInitialized(Class<? extends Type> clazz)
			throws ClassCacheModificationException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		TypeWithMethods t = (TypeWithMethods) construct(clazz, fqn, hash, modifiers);
		ClassType e = new ClassType("exception");
		MethodType m = MethodType.build("name", modifiers, null, null, new HashSet<>(Arrays.asList(new ClassType[] { e })), null);
		t.addMethod(m);

		ClassType initialized = new ClassType("exception", hash, modifiers);

		when(lookup.findByFQN(fqn)).thenReturn(null);
		when(lookup.findByFQN("exception")).thenReturn(initialized);

		service.lookup = lookup;
		service.merge(t);

		assertThat(m.getExceptions(), hasSize(1));
		assertThat(m.getExceptions().iterator().next().isInitialized(), is(true));
	}

	//
	// handle invalid references that exist in the given element
	//

	@Test
	public void handleGivenSubclassIsNotSupported() throws ClassCacheModificationException {
		ClassType c = new ClassType(fqn, hash, modifiers);
		ClassType sub = new ClassType("something");
		c.addSubclass(sub);

		// this merge will - as there is nothing there in the storage - use the given reference
		service.merge(c);

		// we have to ensure that the subclasses are not passed on
		assertThat(c.getSubClasses(), is(empty()));
	}

	@Test
	public void handleGivenSubclassIsNotSupportedForInitialized() throws ClassCacheModificationException {
		ClassType c = new ClassType(fqn, hash, modifiers);
		ClassType sub = new ClassType("something");
		c.addSubclass(sub);

		ClassType stored = new ClassType(fqn, hashStored, modifiers);

		when(lookup.findByFQN(fqn)).thenReturn(stored);
		service.lookup = lookup;

		service.merge(c);

		// we have to ensure that the subclasses are not passed on
		assertThat(stored.getSubClasses(), is(empty()));
	}

	@Test
	public void handleGivenSubInterfaceIsNotSupported() throws ClassCacheModificationException {
		InterfaceType i = new InterfaceType(fqn, hash, modifiers);
		InterfaceType sub = new InterfaceType("somthing");
		i.addSubInterface(sub);

		// this merge will - as there is nothing there in the storage - use the given reference
		service.merge(i);

		// we have to ensure that the subclasses are not passed on
		assertThat(i.getSubInterfaces(), is(empty()));
	}

	@Test
	public void handleGivenSubInterfaceIsNotSupportedForInitialized() throws ClassCacheModificationException {
		InterfaceType i = new InterfaceType(fqn, hash, modifiers);
		InterfaceType sub = new InterfaceType("somthing");
		i.addSubInterface(sub);

		InterfaceType stored = new InterfaceType(fqn, hashStored, modifiers);

		when(lookup.findByFQN(fqn)).thenReturn(stored);
		service.lookup = lookup;

		// this merge will - as there is nothing there in the storage - use the given reference
		service.merge(i);

		// we have to ensure that the subclasses are not passed on
		assertThat(stored.getSubInterfaces(), is(empty()));
	}

	@Test(dataProvider = "types")
	public void handleGivenAnnotatedTypeIsNotSupported(Class<? extends Type> clazz) throws Exception {
		AnnotationType a = new AnnotationType(fqn, hash, modifiers);
		TypeWithAnnotations t = construct(clazz, "some");
		a.addAnnotatedType(t);

		// this merge will - as there is nothing there in the storage - use the given reference
		service.merge(a);

		// we have to ensure that the subclasses are not passed on
		assertThat(a.getAnnotatedTypes(), is(empty()));
	}

	@Test(dataProvider = "types")
	public void handleGivenAnnotatedTypeIsNotSupportedForInitialized(Class<? extends Type> clazz) throws Exception {
		AnnotationType a = new AnnotationType(fqn, hash, modifiers);
		TypeWithAnnotations t = construct(clazz, "some");
		a.addAnnotatedType(t);

		AnnotationType stored = new AnnotationType(fqn, hashStored, modifiers);

		when(lookup.findByFQN(fqn)).thenReturn(stored);

		// this merge will - as there is nothing there in the storage - use the given reference
		service.merge(a);

		// we have to ensure that the subclasses are not passed on
		assertThat(stored.getAnnotatedTypes(), is(empty()));
	}

	@Test
	public void handleGivenReferredClassIsNotSupported() throws ClassCacheModificationException {
		InterfaceType i = new InterfaceType(fqn, hash, modifiers);
		ClassType c = new ClassType("something");
		i.addRealizingClass(c);

		// InterfaceType stored = new InterfaceType(fqn, hashStored, modifiers);
		// when(lookup.findByFQN(fqn)).thenReturn(stored);

		service.merge(i);

		assertThat(i.getRealizingClasses(), is(empty()));
	}

	@Test
	public void handleGivenReferredClassIsNotSupportedForInitialized() throws ClassCacheModificationException {
		InterfaceType i = new InterfaceType(fqn, hash, modifiers);
		ClassType c = new ClassType("something");
		i.addRealizingClass(c);

		InterfaceType stored = new InterfaceType(fqn, hashStored, modifiers);
		when(lookup.findByFQN(fqn)).thenReturn(stored);

		service.merge(i);

		assertThat(stored.getRealizingClasses(), is(empty()));
	}

	@Test
	public void handleGivenMethodThrowingThisExceptionIsNotSupported() throws ClassCacheModificationException {
		ClassType c = new ClassType(fqn, hash, modifiers);
		MethodType m = MethodType.build("aa", modifiers, null, null, null, null);
		c.addMethodThrowingException(m);

		// ClassType stored = new ClassType(fqn, hashStored, modifiers);
		// when(lookup.findByFQN(fqn)).thenReturn(stored);
		// service.lookup = lookup;

		service.merge(c);

		// we have to ensure that the subclasses are not passed on
		assertThat(c.getMethodsThrowingThisException(), is(empty()));
	}

	@Test
	public void handleGivenMethodThrowingThisExceptionIsNotSupportedForInitialized() throws ClassCacheModificationException {
		ClassType c = new ClassType(fqn, hash, modifiers);
		MethodType m = MethodType.build("aa", modifiers, null, null, null, null);
		c.addMethodThrowingException(m);

		ClassType stored = new ClassType(fqn, hashStored, modifiers);
		when(lookup.findByFQN(fqn)).thenReturn(stored);
		service.lookup = lookup;

		service.merge(c);

		// we have to ensure that the subclasses are not passed on
		assertThat(stored.getMethodsThrowingThisException(), is(empty()));
	}

	// ------------------------------------------------------------------------------------
	// Method section - additional references
	//
	// facts
	// - methods always belongs to a type
	// - methods are always initialized, as it does not make sense to have an uninitialized method
	// - methods are identified by their name and parameter list (but not their return value,
	// annotation, exception)
	//
	// for stuff that is not a reference, the checks are smaller
	// - mergeReturnTypeThere, mergeReturnTypeNotThere
	// - mergeModifierThere, mergeModifierNotThere
	//
	// base - given
	// init - new - mergeWithInitializedNewMethodReturnType
	// init - exist - mergeWithInitializedExistingMethodReturnType
	// init - none - mergeWithInitializedNoMethodReturnType
	// noti - new - mergeWithNotInitializedNewMethodReturnType
	//
	//
	// - dimensions we need to test every reference from
	// -- base type (stored) is initialized / not initialized / not there
	// -- given provides new method / existing method
	// -- referred type (exception, annotation) of method of given is there / not
	// there
	//
	// base - given provides existing/new - ref. type available - meaningful
	// init - new - yes - y - mergeWithInitializedNewMethodReferredTypeThere
	// init - new - not - y - mergeWithInitializedNewMethodReferredTypeNotThere
	// init - exi - yes - y - mergeWithInitializedExistingMethodReferredTypeThere
	// init - exi - not - n
	// noti - new - yes - y - mergeWithNotInitializedNewMethodReferredTypeThere
	// noti - new - not - y - mergeWithNotInitializedNewMethodReferredTypeNotThere
	// noti - exi - yes - n
	// noti - exi - not - n
	// nott - new - yes - y - addNewMethodReferredTypeThere
	// nott - new - not - y - addNewMethodReferredTypeNotThere
	// nott - exi - yes - n
	// nott - exi - not - n

	@Test(dataProvider = "typesWithMethods")
	public void mergeWithInitializedNewMethodReturnType(Class<? extends Type> clazz) throws Exception {
		TypeWithMethods c = (TypeWithMethods) construct(clazz, fqn, hash, modifiers);

		String mName = "mName";
		int mMod = 7;
		String returnType = "void";
		List<String> params = Arrays.asList(new String[] { "java.util.List", "java.lang.String" });

		c.addMethod(MethodType.build(mName, mMod, returnType, params, null, null));

		String returnTypeStored = "java.lang.String";
		TypeWithMethods stored = (TypeWithMethods) construct(clazz, fqn, hashStored, modifiers);
		stored.addMethod(MethodType.build(mName, mMod, returnTypeStored, params, null, null));
		stored.addMethod(MethodType.build("someothermethod", mMod, returnTypeStored, params, null, null));

		when(lookup.findByFQN(fqn)).thenReturn(stored);
		service.lookup = lookup;

		Events events = service.merge(c);

		Events expected = new Events();
		expected.addEvent(new NodeEvent(stored, NodeEventType.CHANGED, NodeEventDetails.HASH_ADDED, hash));
		expected.addEvent(new NodeEvent(stored, NodeEventType.CHANGED, NodeEventDetails.METHOD_CHANGED_OR_ADDED));

		assertThat(stored.getMethods().size(), is(3));
		assertThat(stored.getMethods(), hasItem(MethodType.build(mName, mMod, returnType, params, null, null)));

		assertEvents(events, expected);
	}

	@Test(dataProvider = "typesWithMethods")
	public void mergeWithInitializedExistingMethodReturnType(Class<? extends Type> clazz) throws Exception {
		TypeWithMethods c = (TypeWithMethods) construct(clazz, fqn, hash, modifiers);

		String mName = "mName";
		int mMod = 7;
		String returnType = "void";
		List<String> params = Arrays.asList(new String[] { "java.util.List", "java.lang.String" });

		c.addMethod(MethodType.build(mName, mMod, returnType, params, null, null));

		String returnTypeStored = "void";
		TypeWithMethods stored = (TypeWithMethods) construct(clazz, fqn, hashStored, modifiers);
		stored.addMethod(MethodType.build(mName, mMod, returnTypeStored, params, null, null));
		stored.addMethod(MethodType.build("someothermethod", mMod, returnTypeStored, params, null, null));

		when(lookup.findByFQN(fqn)).thenReturn(stored);
		service.lookup = lookup;

		Events events = service.merge(c);

		Events expected = new Events();
		expected.addEvent(new NodeEvent(c, NodeEventType.CHANGED, NodeEventDetails.HASH_ADDED, hash));

		assertThat(stored.getMethods().size(), is(2));
		assertThat(stored.getMethods(), hasItem(MethodType.build(mName, mMod, returnType, params, null, null)));

		assertEvents(events, expected);
	}

	@Test(dataProvider = "typesWithMethods")
	public void mergeWithNotInitializedNewMethodReturnType(Class<? extends Type> clazz) throws Exception {
		TypeWithMethods c = (TypeWithMethods) construct(clazz, fqn, hash, modifiers);

		String mName = "mName";
		int mMod = 7;
		String returnType = "void";
		List<String> params = Arrays.asList(new String[] { "java.util.List", "java.lang.String" });
		c.addMethod(MethodType.build(mName, mMod, returnType, params, null, null));

		TypeWithMethods stored = (TypeWithMethods) construct(clazz, fqn);

		when(lookup.findByFQN(fqn)).thenReturn(stored);
		service.lookup = lookup;

		Events events = service.merge(c);

		Events expected = new Events();
		expected.addEvent(new NodeEvent(c, NodeEventType.CHANGED, NodeEventDetails.INITIALIZED, hash));
		expected.addEvent(new NodeEvent(stored, NodeEventType.CHANGED, NodeEventDetails.METHOD_CHANGED_OR_ADDED));

		String returnTypeExpected = "void";
		assertThat(stored.getMethods().size(), is(1));
		assertThat(stored.getMethods(), hasItem(MethodType.build(mName, mMod, returnTypeExpected, params, null, null)));

		assertEvents(events, expected);
	}

	//
	// Method modifiers
	//

	@Test(dataProvider = "typesWithMethods")
	public void mergeWithInitializedNewMethodModifier(Class<? extends Type> clazz) throws Exception {
		TypeWithMethods c = (TypeWithMethods) construct(clazz, fqn, hash, modifiers);

		String mName = "mName";
		String mReturnType = "mReturnType";
		int mMod = Modifiers.getModifiers(Modifier.PUBLIC | Modifier.STATIC);

		c.addMethod(MethodType.build(mName, mMod, mReturnType, null, null, null));

		int storedMod = Modifiers.getModifiers(Modifier.PRIVATE | Modifier.VOLATILE);
		TypeWithMethods stored = (TypeWithMethods) construct(clazz, fqn, hashStored, modifiers);
		stored.addMethod(MethodType.build(mName, storedMod, mReturnType, null, null, null));
		stored.addMethod(MethodType.build("someothermethod", mMod, mReturnType, null, null, null));

		when(lookup.findByFQN(fqn)).thenReturn(stored);
		service.lookup = lookup;

		Events events = service.merge(c);

		Events expected = new Events();
		expected.addEvent(new NodeEvent(c, NodeEventType.CHANGED, NodeEventDetails.HASH_ADDED, hash));
		expected.addEvent(new NodeEvent(stored, NodeEventType.CHANGED, NodeEventDetails.METHOD_CHANGED_OR_ADDED));

		assertThat(stored.getMethods().size(), is(2));
		int expMod = Modifiers.getModifiers(Modifier.PUBLIC | Modifier.VOLATILE | Modifier.STATIC);
		expMod = Modifiers.mergeModifiers(expMod, Modifiers.getModifiers(Modifier.PRIVATE));
		assertThat(stored.getMethods(), hasItem(MethodType.build(mName, expMod, mReturnType, null, null, null)));

		assertEvents(events, expected);
	}

	@Test(dataProvider = "typesWithMethods")
	public void mergeWithInitializedExistingMethodModifier(Class<? extends Type> clazz) throws Exception {
		TypeWithMethods c = (TypeWithMethods) construct(clazz, fqn, hash, modifiers);

		String mName = "mName";
		String mReturnType = "mReturnType";
		int mMod = Modifiers.getModifiers(Modifier.PUBLIC | Modifier.STATIC);

		c.addMethod(MethodType.build(mName, mMod, mReturnType, null, null, null));

		int storedMod = Modifiers.getModifiers(Modifier.PUBLIC | Modifier.STATIC);
		TypeWithMethods stored = (TypeWithMethods) construct(clazz, fqn, hashStored, modifiers);
		stored.addMethod(MethodType.build(mName, storedMod, mReturnType, null, null, null));
		stored.addMethod(MethodType.build("someothermethod", mMod, mReturnType, null, null, null));

		when(lookup.findByFQN(fqn)).thenReturn(stored);
		service.lookup = lookup;

		Events events = service.merge(c);

		Events expected = new Events();
		expected.addEvent(new NodeEvent(c, NodeEventType.CHANGED, NodeEventDetails.HASH_ADDED, hash));

		assertThat(stored.getMethods().size(), is(2));
		assertThat(stored.getMethods(), hasItem(MethodType.build(mName, Modifiers.getModifiers(Modifier.PUBLIC | Modifier.STATIC), mReturnType, null, null, null)));

		assertEvents(events, expected);
	}

	@Test(dataProvider = "typesWithMethods")
	public void mergeWithInitializedNoMethodModifier(Class<? extends Type> clazz) throws Exception {
		TypeWithMethods c = (TypeWithMethods) construct(clazz, fqn, hash, modifiers);

		String mName = "mName";
		String mReturnType = "mReturnType";
		int mMod = Modifiers.getModifiers(Modifier.PUBLIC | Modifier.STATIC);

		c.addMethod(MethodType.build(mName, 0, mReturnType, null, null, null));

		int storedMod = Modifiers.getModifiers(Modifier.PUBLIC | Modifier.STATIC);
		TypeWithMethods stored = (TypeWithMethods) construct(clazz, fqn, hashStored, modifiers);
		stored.addMethod(MethodType.build(mName, storedMod, mReturnType, null, null, null));
		stored.addMethod(MethodType.build("someothermethod", mMod, mReturnType, null, null, null));

		when(lookup.findByFQN(fqn)).thenReturn(stored);
		service.lookup = lookup;

		Events events = service.merge(c);

		Events expected = new Events();
		expected.addEvent(new NodeEvent(c, NodeEventType.CHANGED, NodeEventDetails.HASH_ADDED, hash));

		assertThat(stored.getMethods().size(), is(2));
		assertThat(stored.getMethods(), hasItem(MethodType.build(mName, Modifiers.getModifiers(Modifier.PUBLIC | Modifier.STATIC), mReturnType, null, null, null)));

		assertEvents(events, expected);
	}

	@Test(dataProvider = "typesWithMethods")
	public void mergeWithNotInitializedNewMethodModifier(Class<? extends Type> clazz) throws Exception {
		TypeWithMethods c = (TypeWithMethods) construct(clazz, fqn, hash, modifiers);

		String mName = "mName";
		int mMod = Modifiers.getModifiers(Modifier.PUBLIC | Modifier.STATIC);

		c.addMethod(MethodType.build(mName, mMod, null, null, null, null));

		TypeWithMethods stored = (TypeWithMethods) construct(clazz, fqn);

		when(lookup.findByFQN(fqn)).thenReturn(stored);
		service.lookup = lookup;

		Events events = service.merge(c);

		Events expected = new Events();
		expected.addEvent(new NodeEvent(c, NodeEventType.CHANGED, NodeEventDetails.INITIALIZED, hash));
		expected.addEvent(new NodeEvent(stored, NodeEventType.CHANGED, NodeEventDetails.METHOD_CHANGED_OR_ADDED));

		assertThat(stored.getMethods().size(), is(1));
		assertThat(stored.getMethods(), hasItem(MethodType.build(mName, Modifiers.getModifiers(Modifier.PUBLIC + Modifier.STATIC), null, null, null, null)));

		assertEvents(events, expected);
	}

	//
	// Method exception
	//

	@Test(dataProvider = "typesWithMethods")
	public void mergeWithInitializedNewMethodExceptionThere(Class<? extends Type> clazz) throws Exception {
		TypeWithMethods given = (TypeWithMethods) construct(clazz, fqn, hash, modifiers);

		TypeWithMethods stored = (TypeWithMethods) construct(clazz, fqn, hashStored, modifiers);

		String exceptionName = "ex";
		ClassType exception = new ClassType(exceptionName);

		String mName = "a";
		String mReturnType = "mReturnType";
		given.addMethod(MethodType.build(mName, 0, mReturnType, null, new HashSet<>(Arrays.asList(exception)), null));
		stored.addMethod(MethodType.build("anothername", 0, mReturnType, null, null, null));

		when(lookup.findByFQN(fqn)).thenReturn(stored);
		when(lookup.findByFQN(exceptionName)).thenReturn(exception);

		service.lookup = lookup;

		Events events = service.merge(given);

		// methods with different parameters are different methods and should not be merged.
		assertThat(stored.getMethods(), hasItem(MethodType.build(mName, 0, mReturnType, null, new HashSet<>(Arrays.asList(exception)), null)));
		assertThat(stored.getMethods().size(), is(2));

		Events expected = new Events();
		expected.addEvent(new NodeEvent(stored, NodeEventType.CHANGED, NodeEventDetails.HASH_ADDED, hash));
		// expected.addEvent(new NodeEvent((Type) exception, NodeEventType.NEW,
		// NodeEventDetails.NOT_INITIALIZED));
		expected.addEvent(new NodeEvent(stored, NodeEventType.CHANGED, NodeEventDetails.METHOD_CHANGED_OR_ADDED));

		assertEvents(events, expected);
	}

	@Test(dataProvider = "typesWithMethods")
	public void mergeWithInitializedNewMethodExceptionNotThere(Class<? extends Type> clazz) throws Exception {
		TypeWithMethods given = (TypeWithMethods) construct(clazz, fqn, hash, modifiers);

		TypeWithMethods stored = (TypeWithMethods) construct(clazz, fqn, hashStored, modifiers);

		String exceptionName = "ex";
		ClassType exception = new ClassType(exceptionName);

		String mName = "a";
		String mReturnType = "mReturnType";
		MethodType m = MethodType.build(mName, 0, mReturnType, null, new HashSet<>(Arrays.asList(exception)), null);

		given.addMethod(m);
		stored.addMethod(MethodType.build("anothername", 0, mReturnType, null, null, null));

		when(lookup.findByFQN(fqn)).thenReturn(stored);
		when(lookup.findByFQN(exceptionName)).thenReturn(null);

		service.lookup = lookup;

		Events events = service.merge(given);

		// methods with different parameters are different methods and should not be merged.
		assertThat(stored.getMethods(), hasItem(m));
		assertThat(stored.getMethods().size(), is(2));

		Events expected = new Events();
		expected.addEvent(new NodeEvent(stored, NodeEventType.CHANGED, NodeEventDetails.HASH_ADDED, hash));
		expected.addEvent(new NodeEvent(exception, NodeEventType.NEW, NodeEventDetails.NOT_INITIALIZED));
		expected.addEvent(new NodeEvent(stored, NodeEventType.CHANGED, NodeEventDetails.METHOD_CHANGED_OR_ADDED));

		assertEvents(events, expected);
	}

	@Test(dataProvider = "typesWithMethods")
	public void mergeWithInitializedExistingMethodExceptionThere(Class<? extends Type> clazz) throws Exception {
		TypeWithMethods given = (TypeWithMethods) construct(clazz, fqn, hash, modifiers);

		TypeWithMethods stored = (TypeWithMethods) construct(clazz, fqn, hashStored, modifiers);

		String exceptionName = "ex";
		ClassType exception = new ClassType(exceptionName);

		String mName = "a";
		String mReturnType = "mReturnType";
		MethodType m = MethodType.build(mName, 0, mReturnType, null, new HashSet<>(Arrays.asList(exception)), null);

		given.addMethod(m);
		stored.addMethod(MethodType.build("anothername", 0, mReturnType, null, null, null));

		when(lookup.findByFQN(fqn)).thenReturn(stored);
		when(lookup.findByFQN(exceptionName)).thenReturn(exception);

		service.lookup = lookup;

		Events events = service.merge(given);

		// methods with different parameters are different methods and should not be merged.
		assertThat(stored.getMethods(), hasItem(m));
		assertThat(stored.getMethods().size(), is(2));

		Events expected = new Events();
		expected.addEvent(new NodeEvent(stored, NodeEventType.CHANGED, NodeEventDetails.HASH_ADDED, hash));
		// expected.addEvent(new NodeEvent((Type) exception, NodeEventType.NEW,
		// NodeEventDetails.NOT_INITIALIZED));
		expected.addEvent(new NodeEvent(stored, NodeEventType.CHANGED, NodeEventDetails.METHOD_CHANGED_OR_ADDED));

		assertEvents(events, expected);
	}

	@Test(dataProvider = "typesWithMethods")
	public void mergeWithNotInitializedNewMethodExceptionThere(Class<? extends Type> clazz) throws Exception {
		TypeWithMethods given = (TypeWithMethods) construct(clazz, fqn, hash, modifiers);

		TypeWithMethods stored = (TypeWithMethods) construct(clazz, fqn);

		String exceptionName = "ex";
		ClassType exception = new ClassType(exceptionName);

		String mName = "a";
		String mReturnType = "mReturnType";
		MethodType m = MethodType.build(mName, 0, mReturnType, null, new HashSet<>(Arrays.asList(exception)), null);

		given.addMethod(m);

		when(lookup.findByFQN(fqn)).thenReturn(stored);
		when(lookup.findByFQN(exceptionName)).thenReturn(exception);

		service.lookup = lookup;

		Events events = service.merge(given);

		// methods with different parameters are different methods and should not be merged.
		assertThat(stored.getMethods(), hasItem(m));
		assertThat(stored.getMethods().size(), is(1));

		Events expected = new Events();
		expected.addEvent(new NodeEvent(stored, NodeEventType.CHANGED, NodeEventDetails.INITIALIZED, hash));
		// expected.addEvent(new NodeEvent((Type) exception, NodeEventType.NEW,
		// NodeEventDetails.NOT_INITIALIZED));
		expected.addEvent(new NodeEvent(stored, NodeEventType.CHANGED, NodeEventDetails.METHOD_CHANGED_OR_ADDED));

		assertEvents(events, expected);
	}

	@Test(dataProvider = "typesWithMethods")
	public void mergeWithNotInitializedNewMethodExceptionNotThere(Class<? extends Type> clazz) throws Exception {
		TypeWithMethods given = (TypeWithMethods) construct(clazz, fqn, hash, modifiers);

		TypeWithMethods stored = (TypeWithMethods) construct(clazz, fqn);

		String exceptionName = "ex";
		ClassType exception = new ClassType(exceptionName);

		String mName = "a";
		String mReturnType = "mReturnType";
		MethodType m = MethodType.build(mName, 0, mReturnType, null, new HashSet<>(Arrays.asList(exception)), null);

		given.addMethod(m);

		when(lookup.findByFQN(fqn)).thenReturn(stored);
		when(lookup.findByFQN(exceptionName)).thenReturn(null);

		service.lookup = lookup;

		Events events = service.merge(given);

		// methods with different parameters are different methods and should not be merged.
		assertThat(stored.getMethods(), hasItem(m));
		assertThat(stored.getMethods().size(), is(1));

		Events expected = new Events();
		expected.addEvent(new NodeEvent(stored, NodeEventType.CHANGED, NodeEventDetails.INITIALIZED, hash));
		expected.addEvent(new NodeEvent(exception, NodeEventType.NEW, NodeEventDetails.NOT_INITIALIZED));
		expected.addEvent(new NodeEvent(stored, NodeEventType.CHANGED, NodeEventDetails.METHOD_CHANGED_OR_ADDED));

		assertEvents(events, expected);
	}

	@Test(dataProvider = "typesWithMethods")
	public void addNewMethodExceptionThere(Class<? extends Type> clazz) throws Exception {
		TypeWithMethods given = (TypeWithMethods) construct(clazz, fqn, hash, modifiers);

		String exceptionName = "ex";
		ClassType exception = new ClassType(exceptionName);

		String mName = "a";
		String mReturnType = "mReturnType";
		MethodType m = MethodType.build(mName, 0, mReturnType, null, new HashSet<>(Arrays.asList(exception)), null);

		given.addMethod(m);

		when(lookup.findByFQN(fqn)).thenReturn(null);
		when(lookup.findByFQN(exceptionName)).thenReturn(exception);

		service.lookup = lookup;

		Events events = service.merge(given);

		// methods with different parameters are different methods and should not be merged.
		assertThat(given.getMethods(), hasItem(m));
		assertThat(given.getMethods().size(), is(1));

		Events expected = new Events();
		expected.addEvent(new NodeEvent(given, NodeEventType.NEW, NodeEventDetails.INITIALIZED, hash));
		// expected.addEvent(new NodeEvent((Type) exception, NodeEventType.NEW,
		// NodeEventDetails.NOT_INITIALIZED));
		// expected.addEvent(new NodeEvent((Type) given, NodeEventType.CHANGED,
		// NodeEventDetails.METHOD_CHANGED_OR_ADDED));

		assertEvents(events, expected);
	}

	@Test(dataProvider = "typesWithMethods")
	public void addNewMethodExceptionNotThere(Class<? extends Type> clazz) throws Exception {
		TypeWithMethods given = (TypeWithMethods) construct(clazz, fqn, hash, modifiers);

		String exceptionName = "ex";
		ClassType exception = new ClassType(exceptionName);

		String mName = "a";
		String mReturnType = "mReturnType";
		MethodType m = MethodType.build(mName, 0, mReturnType, null, new HashSet<>(Arrays.asList(exception)), null);

		given.addMethod(m);

		when(lookup.findByFQN(fqn)).thenReturn(null);
		when(lookup.findByFQN(exceptionName)).thenReturn(null);

		service.lookup = lookup;

		Events events = service.merge(given);

		// methods with different parameters are different methods and should not be merged.
		assertThat(given.getMethods(), hasItem(m));
		assertThat(given.getMethods().size(), is(1));

		Events expected = new Events();
		expected.addEvent(new NodeEvent(given, NodeEventType.NEW, NodeEventDetails.INITIALIZED, hash));
		expected.addEvent(new NodeEvent(exception, NodeEventType.NEW, NodeEventDetails.NOT_INITIALIZED));
		// expected.addEvent(new NodeEvent((Type) given, NodeEventType.CHANGED,
		// NodeEventDetails.METHOD_CHANGED_OR_ADDED));

		assertEvents(events, expected);
	}

	//
	// Method annotation
	//

	@Test(dataProvider = "typesWithMethods")
	public void mergeWithInitializedNewMethodAnnotationThere(Class<? extends Type> clazz) throws Exception {
		TypeWithMethods c = (TypeWithMethods) construct(clazz, fqn, hash, modifiers);

		String mName = "mName";
		String mReturnType = "mReturnType";
		int mMod = 7;

		String aName = "aName";
		AnnotationType a = new AnnotationType(aName);
		c.addMethod(MethodType.build(mName, mMod, mReturnType, null, null, new HashSet<>(Arrays.asList(new AnnotationType[] { a }))));

		TypeWithMethods stored = (TypeWithMethods) construct(clazz, fqn, hashStored, modifiers);
		stored.addMethod(MethodType.build("somename", 0, mReturnType, null, null, null));

		when(lookup.findByFQN(fqn)).thenReturn(stored);
		when(lookup.findByFQN(aName)).thenReturn(a);
		service.lookup = lookup;

		Events events = service.merge(c);

		Events expected = new Events();
		expected.addEvent(new NodeEvent(stored, NodeEventType.CHANGED, NodeEventDetails.HASH_ADDED, hash));
		expected.addEvent(new NodeEvent(stored, NodeEventType.CHANGED, NodeEventDetails.METHOD_CHANGED_OR_ADDED));

		assertThat(stored.getMethods().size(), is(2));
		assertThat(stored.getMethods(), hasItem(MethodType.build(mName, mMod, mReturnType, null, null, new HashSet<>(Arrays.asList(new AnnotationType[] { a })))));

		assertEvents(events, expected);
	}

	@Test(dataProvider = "typesWithMethods")
	public void mergeWithInitializedNewMethodAnnotationNotThere(Class<? extends Type> clazz) throws Exception {
		TypeWithMethods c = (TypeWithMethods) construct(clazz, fqn, hash, modifiers);

		String mName = "mName";
		int mMod = 7;

		String aName = "aName";
		String mReturnType = "mReturnType";
		AnnotationType a = new AnnotationType(aName);
		c.addMethod(MethodType.build(mName, mMod, mReturnType, null, null, new HashSet<>(Arrays.asList(new AnnotationType[] { a }))));

		TypeWithMethods stored = (TypeWithMethods) construct(clazz, fqn, hashStored, modifiers);
		stored.addMethod(MethodType.build(mName, mMod, mReturnType, null, null, null));
		stored.addMethod(MethodType.build("somename", 0, mReturnType, null, null, null));

		when(lookup.findByFQN(fqn)).thenReturn(stored);
		when(lookup.findByFQN(aName)).thenReturn(null);
		service.lookup = lookup;

		Events events = service.merge(c);

		Events expected = new Events();
		expected.addEvent(new NodeEvent(stored, NodeEventType.CHANGED, NodeEventDetails.HASH_ADDED, hash));
		expected.addEvent(new NodeEvent(a, NodeEventType.NEW, NodeEventDetails.NOT_INITIALIZED));
		expected.addEvent(new NodeEvent(stored, NodeEventType.CHANGED, NodeEventDetails.METHOD_CHANGED_OR_ADDED));

		assertThat(stored.getMethods().size(), is(2));
		assertThat(stored.getMethods(), hasItem(MethodType.build(mName, mMod, mReturnType, null, null, new HashSet<>(Arrays.asList(new AnnotationType[] { a })))));

		assertEvents(events, expected);
	}

	@Test(dataProvider = "typesWithMethods")
	public void mergeWithInitializedExistingMethodAnnotationThere(Class<? extends Type> clazz) throws Exception {
		TypeWithMethods c = (TypeWithMethods) construct(clazz, fqn, hash, modifiers);

		String mName = "mName";
		String mReturnType = "mReturnType";
		int mMod = 7;

		String aName = "aName";
		AnnotationType a = new AnnotationType(aName);
		c.addMethod(MethodType.build(mName, mMod, mReturnType, null, null, new HashSet<>(Arrays.asList(new AnnotationType[] { a }))));

		TypeWithMethods stored = (TypeWithMethods) construct(clazz, fqn, hashStored, modifiers);
		stored.addMethod(MethodType.build(mName, mMod, mReturnType, null, null, new HashSet<>(Arrays.asList(new AnnotationType[] { a }))));
		stored.addMethod(MethodType.build("somename", 0, mReturnType, null, null, null));

		when(lookup.findByFQN(fqn)).thenReturn(stored);
		when(lookup.findByFQN(aName)).thenReturn(a);
		service.lookup = lookup;

		Events events = service.merge(c);

		Events expected = new Events();
		expected.addEvent(new NodeEvent(stored, NodeEventType.CHANGED, NodeEventDetails.HASH_ADDED, hash));
		// expected.addEvent(new NodeEvent((Type) a, NodeEventType.NEW,
		// NodeEventDetails.NOT_INITIALIZED));
		// expected.addEvent(new NodeEvent((Type) stored, NodeEventType.CHANGED,
		// NodeEventDetails.METHOD_CHANGED_OR_ADDED));

		assertThat(stored.getMethods().size(), is(2));
		assertThat(stored.getMethods(), hasItem(MethodType.build(mName, mMod, mReturnType, null, null, new HashSet<>(Arrays.asList(new AnnotationType[] { a })))));

		assertEvents(events, expected);
	}

	@Test(dataProvider = "typesWithMethods")
	public void mergeWithNotInitializedNewMethodAnnotationThere(Class<? extends Type> clazz) throws Exception {
		TypeWithMethods c = (TypeWithMethods) construct(clazz, fqn, hash, modifiers);

		String mName = "mName";
		String mReturnType = "mReturnType";
		int mMod = 7;

		String aName = "aName";
		AnnotationType a = new AnnotationType(aName);
		c.addMethod(MethodType.build(mName, mMod, mReturnType, null, null, new HashSet<>(Arrays.asList(new AnnotationType[] { a }))));

		TypeWithMethods stored = (TypeWithMethods) construct(clazz, fqn);

		when(lookup.findByFQN(fqn)).thenReturn(stored);
		when(lookup.findByFQN(aName)).thenReturn(a);
		service.lookup = lookup;

		Events events = service.merge(c);

		Events expected = new Events();
		expected.addEvent(new NodeEvent(stored, NodeEventType.CHANGED, NodeEventDetails.INITIALIZED, hash));
		// expected.addEvent(new NodeEvent((Type) a, NodeEventType.NEW,
		// NodeEventDetails.NOT_INITIALIZED));
		expected.addEvent(new NodeEvent(stored, NodeEventType.CHANGED, NodeEventDetails.METHOD_CHANGED_OR_ADDED));

		assertThat(stored.getMethods().size(), is(1));
		assertThat(stored.getMethods(), hasItem(MethodType.build(mName, mMod, mReturnType, null, null, new HashSet<>(Arrays.asList(new AnnotationType[] { a })))));

		assertEvents(events, expected);
	}

	@Test(dataProvider = "typesWithMethods")
	public void mergeWithNotInitializedNewMethodAnnotationNotThere(Class<? extends Type> clazz) throws Exception {
		TypeWithMethods c = (TypeWithMethods) construct(clazz, fqn, hash, modifiers);

		String mName = "mName";
		String mReturnType = "mReturnType";
		int mMod = 7;

		String aName = "aName";
		AnnotationType a = new AnnotationType(aName);
		c.addMethod(MethodType.build(mName, mMod, mReturnType, null, null, new HashSet<>(Arrays.asList(new AnnotationType[] { a }))));

		TypeWithMethods stored = (TypeWithMethods) construct(clazz, fqn);

		when(lookup.findByFQN(fqn)).thenReturn(stored);
		when(lookup.findByFQN(aName)).thenReturn(null);
		service.lookup = lookup;

		Events events = service.merge(c);

		Events expected = new Events();
		expected.addEvent(new NodeEvent(stored, NodeEventType.CHANGED, NodeEventDetails.INITIALIZED, hash));
		expected.addEvent(new NodeEvent(a, NodeEventType.NEW, NodeEventDetails.NOT_INITIALIZED));
		expected.addEvent(new NodeEvent(stored, NodeEventType.CHANGED, NodeEventDetails.METHOD_CHANGED_OR_ADDED));

		assertThat(stored.getMethods().size(), is(1));
		assertThat(stored.getMethods(), hasItem(MethodType.build(mName, mMod, mReturnType, null, null, new HashSet<>(Arrays.asList(new AnnotationType[] { a })))));

		assertEvents(events, expected);
	}

	@Test(dataProvider = "typesWithMethods")
	public void addNewMethodAnnotationThere(Class<? extends Type> clazz) throws Exception {
		TypeWithMethods c = (TypeWithMethods) construct(clazz, fqn, hash, modifiers);

		String mName = "mName";
		String mReturnType = "mReturnType";
		int mMod = 7;

		String aName = "aName";
		AnnotationType a = new AnnotationType(aName);
		c.addMethod(MethodType.build(mName, mMod, mReturnType, null, null, new HashSet<>(Arrays.asList(new AnnotationType[] { a }))));

		when(lookup.findByFQN(fqn)).thenReturn(null);
		when(lookup.findByFQN(aName)).thenReturn(a);
		service.lookup = lookup;

		Events events = service.merge(c);

		Events expected = new Events();
		expected.addEvent(new NodeEvent(c, NodeEventType.NEW, NodeEventDetails.INITIALIZED, hash));
		// expected.addEvent(new NodeEvent((Type) a, NodeEventType.NEW,
		// NodeEventDetails.NOT_INITIALIZED));
		// expected.addEvent(new NodeEvent((Type) c, NodeEventType.CHANGED,
		// NodeEventDetails.METHOD_CHANGED_OR_ADDED));

		assertThat(c.getMethods().size(), is(1));
		assertThat(c.getMethods(), hasItem(MethodType.build(mName, mMod, mReturnType, null, null, new HashSet<>(Arrays.asList(new AnnotationType[] { a })))));

		assertEvents(events, expected);
	}

	@Test(dataProvider = "typesWithMethods")
	public void addNewMethodAnnotationNotThere(Class<? extends Type> clazz) throws Exception {
		TypeWithMethods c = (TypeWithMethods) construct(clazz, fqn, hash, modifiers);

		String mName = "mName";
		String mReturnType = "mReturnType";
		int mMod = 7;

		String aName = "aName";
		AnnotationType a = new AnnotationType(aName);
		c.addMethod(MethodType.build(mName, mMod, mReturnType, null, null, new HashSet<>(Arrays.asList(new AnnotationType[] { a }))));

		when(lookup.findByFQN(fqn)).thenReturn(null);
		when(lookup.findByFQN(aName)).thenReturn(null);
		service.lookup = lookup;

		Events events = service.merge(c);

		Events expected = new Events();
		expected.addEvent(new NodeEvent(c, NodeEventType.NEW, NodeEventDetails.INITIALIZED, hash));
		expected.addEvent(new NodeEvent(a, NodeEventType.NEW, NodeEventDetails.NOT_INITIALIZED));
		// expected.addEvent(new NodeEvent((Type) c, NodeEventType.CHANGED,
		// NodeEventDetails.METHOD_CHANGED_OR_ADDED));

		assertThat(c.getMethods().size(), is(1));
		assertThat(c.getMethods(), hasItem(MethodType.build(mName, mMod, mReturnType, null, null, new HashSet<>(Arrays.asList(new AnnotationType[] { a })))));

		assertEvents(events, expected);
	}

	private Type construct(Class<? extends Type> type, String fqn)
			throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Constructor<? extends Type> c = type.getConstructor(String.class);
		return c.newInstance(fqn);
	}

	private Type construct(Class<? extends Type> type, String fqn, String hash, int modifiers)
			throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Constructor<? extends Type> c = type.getConstructor(String.class, String.class, int.class);
		return c.newInstance(fqn, hash, modifiers);
	}

	private void assertEvents(Events events, Events expected) {
		assertThat(events, is(not(nullValue())));
		assertThat(events.nodeEvents.size(), is(expected.nodeEvents.size()));
		assertThat(events.referenceEvents.size(), is(expected.referenceEvents.size()));
		for (NodeEvent n : expected.nodeEvents) {
			assertThat(events.nodeEvents, hasItem(n));
		}
		for (ReferenceEvent r : expected.referenceEvents) {
			assertThat(events.referenceEvents, hasItem(r));
		}

		// also ensure that these events are passed to the notification
		for (NodeEvent nodeEvent : events.nodeEvents) {
			verify(cache, times(1)).informNodeChange(nodeEvent);
		}

		for (ReferenceEvent referenceEvent : events.referenceEvents) {
			verify(cache, times(1)).informReferenceChange(referenceEvent);
		}
	}

	private void assertEventsEmpty(Events events) {
		assertThat(events, is(not(nullValue())));
		assertThat(events.nodeEvents, is(empty()));
		assertThat(events.referenceEvents, is(empty()));
	}
}
