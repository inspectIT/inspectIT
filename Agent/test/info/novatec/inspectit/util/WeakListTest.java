package info.novatec.inspectit.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("PMD")
public class WeakListTest {

	private WeakList<Object> weakList;

	@BeforeMethod
	public void initTestClass() {
		weakList = new WeakList<Object>();
	}

	@Test
	public void oneElement() {
		Object object = new Object();
		weakList.add(object);
		Object returnValue = weakList.get(0);

		assertThat(returnValue, is(notNullValue()));
		assertThat(returnValue, is(object));
	}

	@Test
	public void clearNoGC() {
		Object objectOne = new Object();
		Object objectTwo = new Object();
		Object objectThree = new Object();

		weakList.add(objectOne);
		weakList.add(objectTwo);
		weakList.add(objectThree);

		weakList.clear();

		assertThat(weakList, is(empty()));
	}

	@Test
	public void clearWithGC() {
		Object objectOne = new Object();
		Object objectTwo = new Object();
		Object objectThree = new Object();

		weakList.add(objectOne);
		weakList.add(objectTwo);
		weakList.add(objectThree);

		objectOne = null;
		objectTwo = null;
		objectThree = null;

		System.gc();

		weakList.clear();

		assertThat(weakList, is(empty()));
	}

	@Test
	public void containsNoGC() {
		Object objectOne = new Object();
		Object objectTwo = new Object();

		weakList.add(objectOne);
		weakList.add(objectTwo);

		objectOne = null;

		assertThat(weakList, hasItem(objectTwo));
	}

	@Test
	public void containsWithGC() {
		Object objectOne = new Object();
		Object objectTwo = new Object();

		weakList.add(objectOne);
		weakList.add(objectTwo);

		objectOne = null;

		System.gc();

		assertThat(weakList, hasItem(objectTwo));
	}

	@Test(expectedExceptions = { IndexOutOfBoundsException.class })
	public void outOfBounds() {
		assertThat(weakList.get(5), is(nullValue()));
	}

}
