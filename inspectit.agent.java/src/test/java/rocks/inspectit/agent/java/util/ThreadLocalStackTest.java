package rocks.inspectit.agent.java.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.util.ThreadLocalStack;

@SuppressWarnings("PMD")
public class ThreadLocalStackTest {

	private ThreadLocalStack<Object> threadLocalStack;

	@BeforeMethod(firstTimeOnly = true)
	public void initTestClass() {
		threadLocalStack = new ThreadLocalStack<Object>();
	}

	@Test
	public void emptyStack() {
		Object object = threadLocalStack.get();

		assertThat(object, is(notNullValue()));
		assertThat(object, is(instanceOf(LinkedList.class)));
	}

	@Test(dependsOnMethods = "emptyStack")
	public void oneValue() {
		Object object = mock(Object.class);
		threadLocalStack.push(object);

		Object returnValue = threadLocalStack.pop();

		assertThat(returnValue, is(notNullValue()));
		assertThat(returnValue, is(object));
		verifyZeroInteractions(object);
	}

	@Test(dependsOnMethods = "emptyStack", expectedExceptions = { NoSuchElementException.class })
	public void noSuchElement() {
		threadLocalStack.pop();
	}

	@Test(dependsOnMethods = "emptyStack", invocationCount = 10, threadPoolSize = 10)
	public void stackTest() {
		Object objectOne = mock(Object.class);
		Object objectTwo = mock(Object.class);
		Object objectThree = mock(Object.class);

		threadLocalStack.push(objectOne);
		threadLocalStack.push(objectTwo);
		threadLocalStack.push(objectThree);

		Object returnValueOne = threadLocalStack.pop();
		Object returnValueTwo = threadLocalStack.pop();
		Object returnValueThree = threadLocalStack.pop();

		assertThat(returnValueOne, is(objectThree));
		assertThat(returnValueTwo, is(objectTwo));
		assertThat(returnValueThree, is(objectOne));

		verifyZeroInteractions(objectOne);
		verifyZeroInteractions(objectTwo);
		verifyZeroInteractions(objectThree);
	}

	@Test(dependsOnMethods = "emptyStack")
	public void getAndRemoveFirst() {
		Object objectOne = mock(Object.class);
		Object objectTwo = mock(Object.class);
		Object objectThree = mock(Object.class);

		threadLocalStack.push(objectOne);
		threadLocalStack.push(objectTwo);
		threadLocalStack.push(objectThree);

		assertThat(threadLocalStack.getAndRemoveFirst(), is(objectOne));
		assertThat(threadLocalStack.getAndRemoveFirst(), is(objectTwo));
		assertThat(threadLocalStack.getAndRemoveFirst(), is(objectThree));

		verifyZeroInteractions(objectOne);
		verifyZeroInteractions(objectTwo);
		verifyZeroInteractions(objectThree);
	}

}
