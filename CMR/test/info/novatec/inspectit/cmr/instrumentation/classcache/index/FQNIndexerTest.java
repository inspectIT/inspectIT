package info.novatec.inspectit.cmr.instrumentation.classcache.index;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import info.novatec.inspectit.instrumentation.classcache.ClassType;

import java.util.Collection;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("PMD")
public class FQNIndexerTest {

	protected FQNIndexer<ClassType> indexer;

	@BeforeMethod
	public void init() {
		indexer = new FQNIndexer<>();
	}

	public class Lookup extends FQNIndexerTest {

		@Test
		public void sameFqnTwice() {
			ClassType stringType1 = new ClassType(String.class.getName());
			ClassType stringType2 = new ClassType(String.class.getName());
			indexer.index(stringType1);
			indexer.index(stringType2);

			ClassType lookup = indexer.lookup(String.class.getName());

			assertThat(indexer, hasSize(1));
			assertThat(lookup == stringType2, is(true));
		}

		@Test
		public void lookup() {
			ClassType stringType = new ClassType(String.class.getName());
			ClassType objectType = new ClassType(Object.class.getName());
			ClassType thisType = new ClassType(FQNIndexer.class.getName());
			indexer.index(stringType);
			indexer.index(thisType);
			indexer.index(new ClassType("a"));
			indexer.index(new ClassType("java.nolang"));
			indexer.index(objectType);

			ClassType stringLookup = indexer.lookup(stringType.getFQN());
			ClassType objectLookup = indexer.lookup(objectType.getFQN());
			ClassType thisTypeLookup = indexer.lookup(thisType.getFQN());

			assertThat(indexer, hasSize(5));
			assertThat(stringLookup, is(stringType));
			assertThat(objectLookup, is(objectType));
			assertThat(thisTypeLookup, is(thisType));
		}

		@Test
		public void notFound() {
			ClassType stringType = new ClassType(String.class.getName());
			ClassType objectType = new ClassType(Object.class.getName());
			ClassType thisType = new ClassType(FQNIndexer.class.getName());
			indexer.index(stringType);
			indexer.index(thisType);
			indexer.index(objectType);

			ClassType lookup = indexer.lookup("nonExistingType");

			assertThat(indexer, hasSize(3));
			assertThat(lookup, is(nullValue()));
		}

		@Test
		public void replace() {
			ClassType stringType = new ClassType(String.class.getName());
			ClassType objectType = new ClassType(Object.class.getName());
			ClassType thisType = new ClassType(FQNIndexer.class.getName());
			ClassType secondObjectType = new ClassType(Object.class.getName());
			indexer.index(stringType);
			indexer.index(thisType);
			indexer.index(objectType);
			indexer.index(secondObjectType);

			ClassType lookup = indexer.lookup(Object.class.getName());

			assertThat(indexer, hasSize(3));
			assertThat(System.identityHashCode(lookup), is(System.identityHashCode(secondObjectType)));
		}
	}

	public class FindAll extends FQNIndexerTest {

		@Test
		public void findAll() {
			ClassType stringType = new ClassType(String.class.getName());
			ClassType objectType = new ClassType(Object.class.getName());
			ClassType thisType = new ClassType(FQNIndexer.class.getName());
			indexer.index(stringType);
			indexer.index(thisType);
			indexer.index(objectType);

			Collection<ClassType> types = indexer.findAll();

			assertThat(types, hasSize(3));
			assertThat(types, hasItem(stringType));
			assertThat(types, hasItem(objectType));
			assertThat(types, hasItem(thisType));
		}
	}

	public class FindByWildCard extends FQNIndexerTest {

		@Test
		public void found() {
			ClassType stringType = new ClassType(String.class.getName());
			ClassType objectType = new ClassType(Object.class.getName());
			ClassType thisType = new ClassType(FQNIndexer.class.getName());
			indexer.index(stringType);
			indexer.index(thisType);
			indexer.index(new ClassType("a"));
			indexer.index(new ClassType("java.nolang"));
			indexer.index(objectType);

			// middle
			Collection<ClassType> results = indexer.findStartsWith("java.lang");
			assertThat(results, hasSize(2));
			for (ClassType classType : results) {
				assertThat(classType.getFQN().startsWith("java.lang"), is(true));
			}

			// end
			results = indexer.findStartsWith("java.nolang");
			assertThat(results, hasSize(1));
			for (ClassType classType : results) {
				assertThat(classType.getFQN().startsWith("java.nolang"), is(true));
			}

			// begin
			results = indexer.findStartsWith("a");
			assertThat(results, hasSize(1));
			for (ClassType classType : results) {
				assertThat(classType.getFQN().startsWith("a"), is(true));
			}
		}

		@Test
		public void notFound() {
			ClassType stringType = new ClassType(String.class.getName());
			ClassType objectType = new ClassType(Object.class.getName());
			ClassType thisType = new ClassType(FQNIndexer.class.getName());
			indexer.index(stringType);
			indexer.index(thisType);
			indexer.index(new ClassType("a"));
			indexer.index(new ClassType("java.nolang"));
			indexer.index(objectType);

			// middle
			Collection<ClassType> results = indexer.findStartsWith("aa");
			assertThat(results, is(empty()));

			results = indexer.findStartsWith("java.lang.something");
			assertThat(results, is(empty()));

			results = indexer.findStartsWith("ww");
			assertThat(results, is(empty()));
		}
	}

}
