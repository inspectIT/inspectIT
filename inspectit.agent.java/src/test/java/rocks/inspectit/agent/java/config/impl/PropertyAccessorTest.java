package rocks.inspectit.agent.java.config.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.AbstractLogSupport;
import rocks.inspectit.agent.java.config.PropertyAccessException;
import rocks.inspectit.shared.all.communication.data.ParameterContentData;
import rocks.inspectit.shared.all.communication.data.ParameterContentType;
import rocks.inspectit.shared.all.instrumentation.config.impl.PropertyPath;
import rocks.inspectit.shared.all.instrumentation.config.impl.PropertyPathStart;

@SuppressWarnings("PMD")
public class PropertyAccessorTest extends AbstractLogSupport {

	private PropertyAccessor propertyAccessor;

	private Object resultValueMock;

	@BeforeClass
	public void initTestClass() {
		propertyAccessor = new PropertyAccessor();
		propertyAccessor.log = LoggerFactory.getLogger(PropertyAccessor.class);
	}

	@BeforeMethod
	public void initialize() {
		resultValueMock = Mockito.mock(Object.class);
	}

	@Test
	public void readFieldPersonObject() throws PropertyAccessException {
		Person person = new Person();
		person.setName("Dirk");

		PropertyPathStart start = new PropertyPathStart();
		start.setName("this");
		start.setContentType(ParameterContentType.FIELD);

		String result = propertyAccessor.getPropertyContent(start, person, null, resultValueMock);
		assertThat(result, is("Dirk"));
		Mockito.verifyZeroInteractions(resultValueMock);
	}

	@Test
	public void readFieldPersonName() throws PropertyAccessException {
		Person person = new Person();
		person.setName("Dirk");

		PropertyPathStart start = new PropertyPathStart();
		start.setName("this");
		start.setContentType(ParameterContentType.FIELD);

		PropertyPath path = new PropertyPath();
		path.setName("name");
		start.setPathToContinue(path);

		String result = propertyAccessor.getPropertyContent(start, person, null, resultValueMock);
		assertThat(result, is("Dirk"));
		Mockito.verifyZeroInteractions(resultValueMock);
	}

	@Test(expectedExceptions = { PropertyAccessException.class })
	public void nullStartPath() throws PropertyAccessException {
		propertyAccessor.getPropertyContent(null, null, null, null);
	}

	@Test(expectedExceptions = { PropertyAccessException.class })
	public void nullNeededClassObjectForFieldAccess() throws PropertyAccessException {
		PropertyPathStart start = new PropertyPathStart();
		start.setName("this");
		start.setContentType(ParameterContentType.FIELD);

		propertyAccessor.getPropertyContent(start, null, null, resultValueMock);
		Mockito.verifyZeroInteractions(resultValueMock);
	}

	/*
	 * We no longer throw exceptions in the case that the return value is null as this would
	 * completely remove the property accessor and this is not the desired behaviour.
	 */
	@Test
	public void nullNeededResultObjectForResultAccess() throws PropertyAccessException {
		PropertyPathStart start = new PropertyPathStart();
		start.setName("this");
		start.setContentType(ParameterContentType.RETURN);

		String result = propertyAccessor.getPropertyContent(start, null, null, null);
		assertThat(result, is(equalTo("null")));
	}

	@Test(expectedExceptions = { PropertyAccessException.class })
	public void nullNeededParameterObject() throws PropertyAccessException {
		PropertyPathStart start = new PropertyPathStart();
		start.setName("name");
		start.setSignaturePosition(0);
		start.setContentType(ParameterContentType.PARAM);

		propertyAccessor.getPropertyContent(start, null, null, resultValueMock);
		Mockito.verifyZeroInteractions(resultValueMock);
	}

	@Test(expectedExceptions = { PropertyAccessException.class })
	public void parameterArrayOutOfRange() throws PropertyAccessException {
		PropertyPathStart start = new PropertyPathStart();
		start.setName("name");
		start.setSignaturePosition(0);
		start.setContentType(ParameterContentType.PARAM);

		propertyAccessor.getPropertyContent(start, null, new Object[0], resultValueMock);
		Mockito.verifyZeroInteractions(resultValueMock);
	}

	@Test(expectedExceptions = { PropertyAccessException.class })
	public void missingContentType() throws PropertyAccessException {
		PropertyPathStart start = new PropertyPathStart();
		start.setName("name");
		start.setSignaturePosition(0);

		propertyAccessor.getPropertyContent(start, null, null, null);
	}

	@Test(expectedExceptions = { PropertyAccessException.class })
	public void analyzePersonAccessException() throws PropertyAccessException {
		Person person = new Person();
		person.setName("Dirk");

		PropertyPathStart start = new PropertyPathStart();
		start.setName("this");
		start.setContentType(ParameterContentType.FIELD);

		PropertyPath path = new PropertyPath();
		path.setName("surname");
		start.setPathToContinue(path);

		// name != surname -> exception
		propertyAccessor.getPropertyContent(start, person, null, resultValueMock);
		Mockito.verifyZeroInteractions(resultValueMock);
	}

	@Test
	public void analyzePersonParameter() throws PropertyAccessException {
		// create initial object relation
		Person peter = new Person("Peter");
		Person juergen = new Person("Juergen");
		peter.setChild(juergen);
		Person hans = new Person("Hans");
		juergen.setChild(hans);
		Person thomas = new Person("Thomas");
		hans.setChild(thomas);
		Person michael = new Person("Michael");
		thomas.setChild(michael);

		// create the test path
		PropertyPathStart start = new PropertyPathStart();
		start.setName("name");
		start.setSignaturePosition(1);
		start.setContentType(ParameterContentType.PARAM);

		PropertyPath pathOne = new PropertyPath("child");
		start.setPathToContinue(pathOne);

		PropertyPath pathTwo = new PropertyPath("child");
		pathOne.setPathToContinue(pathTwo);

		PropertyPath pathThree = new PropertyPath("child");
		pathTwo.setPathToContinue(pathThree);

		PropertyPath pathFour = new PropertyPath("child");
		pathThree.setPathToContinue(pathFour);

		// set the parameter array
		Object[] parameters = { null, peter };

		String result = propertyAccessor.getPropertyContent(start, new Object(), parameters, resultValueMock);
		assertThat(result, is("Michael"));
		Mockito.verifyZeroInteractions(resultValueMock);
	}

	@Test
	public void removePropertyAccessorFromList() {
		// create initial object relation
		Person peter = new Person("Peter");
		Person juergen = new Person("Hans");
		peter.setChild(juergen);

		// CopyOnWriteArrayList for thread safety
		List<PropertyPathStart> propertyAccessorList = new CopyOnWriteArrayList<PropertyPathStart>();

		// valid
		PropertyPathStart start = new PropertyPathStart();
		start.setName("name");
		start.setSignaturePosition(0);
		start.setContentType(ParameterContentType.PARAM);
		PropertyPath pathOne = new PropertyPath("child");
		start.setPathToContinue(pathOne);
		propertyAccessorList.add(start);

		// not valid
		start = new PropertyPathStart();
		start.setName("this");
		start.setContentType(ParameterContentType.FIELD);
		pathOne = new PropertyPath("notValid");
		start.setPathToContinue(pathOne);
		propertyAccessorList.add(start);

		// not valid as the second parameter will be null
		start = new PropertyPathStart();
		start.setName("name");
		start.setSignaturePosition(1);
		start.setContentType(ParameterContentType.PARAM);
		pathOne = new PropertyPath("child");
		start.setPathToContinue(pathOne);
		propertyAccessorList.add(start);

		assertThat(propertyAccessorList, hasSize(3));

		List<ParameterContentData> parameterContentList = propertyAccessor.getParameterContentData(propertyAccessorList, peter, new Object[] { peter }, resultValueMock);

		// size should be reduced to one
		assertThat(propertyAccessorList, hasSize(1));
		// so is the size of the parameter content
		assertThat(parameterContentList, is(notNullValue()));
		assertThat(parameterContentList, hasSize(1));
		// changed due to xstream, the ' at the beginning will be always removed
		// if displayed to the end-user.
		assertThat(parameterContentList.get(0).getContent(), is("Hans"));
		assertThat(parameterContentList.get(0).getSignaturePosition(), is(0));
		assertThat(parameterContentList.get(0).getName(), is("name"));
	}

	@Test
	public void invokeArrayLengthMethod() throws PropertyAccessException {
		// create initial object relation
		Person peter = new Person("Peter");
		String[] foreNames = new String[] { "Klaus", "Uwe" };
		peter.setForeNames(foreNames);

		PropertyPathStart start = new PropertyPathStart();
		start.setName("this");
		start.setContentType(ParameterContentType.FIELD);

		PropertyPath path = new PropertyPath();
		path.setName("foreNames");
		start.setPathToContinue(path);

		PropertyPath path2 = new PropertyPath();
		path2.setName("length()");
		path.setPathToContinue(path2);

		String result = propertyAccessor.getPropertyContent(start, peter, null, resultValueMock);
		assertThat(Integer.parseInt(result), is(2));
		Mockito.verifyZeroInteractions(resultValueMock);
	}

	@Test(expectedExceptions = { PropertyAccessException.class })
	public void invokeArrayLengthMethodOnNonArray() throws PropertyAccessException {
		// create initial object relation
		Person peter = new Person("Peter");
		String[] foreNames = new String[] { "Klaus", "Uwe" };
		peter.setForeNames(foreNames);

		PropertyPathStart start = new PropertyPathStart();
		start.setName("this");
		start.setContentType(ParameterContentType.FIELD);

		PropertyPath path = new PropertyPath();
		path.setName("name");
		start.setPathToContinue(path);

		PropertyPath path2 = new PropertyPath();
		path2.setName("length()");
		path.setPathToContinue(path2);

		// must result in an Exception as name is not an array
		propertyAccessor.getPropertyContent(start, peter, null, resultValueMock);
		Mockito.verifyZeroInteractions(resultValueMock);
	}

	@Test
	public void invokeListSizeMethod() throws PropertyAccessException {
		// create initial object relation
		Person peter = new Person("Peter");
		List<String> foreNames = new ArrayList<String>();
		foreNames.add("blub");
		foreNames.add("blub2");
		foreNames.add("blub3");
		peter.setForeNamesAsList(foreNames);

		PropertyPathStart start = new PropertyPathStart();
		start.setName("this");
		start.setContentType(ParameterContentType.FIELD);

		PropertyPath path = new PropertyPath();
		path.setName("foreNamesAsList");
		start.setPathToContinue(path);

		PropertyPath path2 = new PropertyPath();
		path2.setName("size()");
		path.setPathToContinue(path2);

		String result = propertyAccessor.getPropertyContent(start, peter, null, resultValueMock);
		assertThat(Integer.parseInt(result), is(3));
		Mockito.verifyZeroInteractions(resultValueMock);
	}

	@Test
	public void analyzeReturnValueString() throws PropertyAccessException {
		// valid
		PropertyPathStart start = new PropertyPathStart();
		start.setName("returnName");
		start.setContentType(ParameterContentType.RETURN);

		String result = propertyAccessor.getPropertyContent(start, null, null, "Peter");
		assertThat(result, is("Peter"));
	}

	@Test
	public void analyzeReturnValueVoidMethod() throws PropertyAccessException {
		// create initial object relation
		Person peter = new Person("Peter");

		// valid
		PropertyPathStart start = new PropertyPathStart();
		start.setName("setName");
		start.setContentType(ParameterContentType.RETURN);

		String result = propertyAccessor.getPropertyContent(start, null, null, peter);
		assertThat(result, is("Peter"));
	}

	@Test
	public void analyzeReturnValueObject() throws PropertyAccessException {
		// create initial object relation
		Person peter = new Person("Peter");
		Person juergen = new Person("Hans");
		peter.setChild(juergen);

		List<PropertyPathStart> propertyAccessorList = new ArrayList<PropertyPathStart>();

		// valid
		PropertyPathStart start = new PropertyPathStart();
		start.setName("return");
		start.setContentType(ParameterContentType.RETURN);
		PropertyPath pathOne = new PropertyPath("child");
		start.setPathToContinue(pathOne);
		propertyAccessorList.add(start);

		String result = propertyAccessor.getPropertyContent(start, null, null, peter);
		assertThat(result, is("Hans"));
	}

	@Test(expectedExceptions = { PropertyAccessException.class })
	public void invokeForbiddenMethod() throws PropertyAccessException {
		// create initial object relation
		Person peter = new Person("Peter");

		PropertyPathStart start = new PropertyPathStart();
		start.setName("this");
		start.setContentType(ParameterContentType.FIELD);

		PropertyPath path = new PropertyPath();
		path.setName("getName()");
		start.setPathToContinue(path);

		propertyAccessor.getPropertyContent(start, peter, null, resultValueMock);
		Mockito.verifyZeroInteractions(resultValueMock);
	}

	@Test
	public void concurentAccessOnPropertyAccessorList() {

		// create initial object relation
		Person peter = new Person("Peter");
		Person juergen = new Person("Jurgen");
		peter.setChild(juergen);

		// CopyOnWriteArrayList for thread safety. It's the same as the propertyAccessorList in
		// AbstractSensorConfig, as its operated on. In normal ArrayList, Fail-fast iterators throw
		// ConcurrentModificationException on a best-effort basis.
		// So it's not guaranteed, that there will be an exception on concurrent access, but the
		// results will be inconsistent.
		List<PropertyPathStart> propertyAccessorList = new CopyOnWriteArrayList<PropertyPathStart>();

		// valid
		PropertyPathStart start = new PropertyPathStart();
		start.setName("name");
		start.setSignaturePosition(0);
		start.setContentType(ParameterContentType.PARAM);
		PropertyPath pathOne = new PropertyPath("child");
		start.setPathToContinue(pathOne);
		propertyAccessorList.add(start);

		// not valid
		start = new PropertyPathStart();
		start.setName("this");
		start.setContentType(ParameterContentType.FIELD);
		pathOne = new PropertyPath("notValid");
		start.setPathToContinue(pathOne);
		propertyAccessorList.add(start);

		// not valid as the second parameter will be null
		start = new PropertyPathStart();
		start.setName("name");
		start.setSignaturePosition(1);
		start.setContentType(ParameterContentType.PARAM);
		pathOne = new PropertyPath("child");
		start.setPathToContinue(pathOne);
		propertyAccessorList.add(start);

		assertThat(propertyAccessorList, hasSize(3));

		// Creating concurrent access
		Iterator<PropertyPathStart> i = propertyAccessorList.iterator();
		// Access via iterator
		i.next();
		// Direct access
		List<ParameterContentData> parameterContentList = propertyAccessor.getParameterContentData(propertyAccessorList, peter, new Object[] { peter }, null);

		// Double check results, in case of missing exception
		// size should be reduced to one
		assertThat(propertyAccessorList, hasSize(1));
		// so is the size of the parameter content
		assertThat(parameterContentList, is(notNullValue()));
		assertThat(parameterContentList, hasSize(1));
		// changed due to xstream, the ' at the beginning will be always removed
		// if displayed to the end-user.
		assertThat(parameterContentList.get(0).getContent(), is("Jurgen"));
		assertThat(parameterContentList.get(0).getSignaturePosition(), is(0));
		assertThat(parameterContentList.get(0).getName(), is("name"));

	}

	@SuppressWarnings("unused")
	private static class Person {

		private String name;

		private Person child;

		private String[] foreNames;

		private List<String> foreNamesAsList;

		public Person() {
		}

		public Person(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Person getChild() {
			return child;
		}

		public void setChild(Person child) {
			this.child = child;
		}

		public String[] getForeNames() {
			return foreNames;
		}

		public void setForeNames(String[] foreNames) {
			this.foreNames = foreNames;
		}

		public List<String> getForeNamesAsList() {
			return foreNamesAsList;
		}

		public void setForeNamesAsList(List<String> foreNamesAsList) {
			this.foreNamesAsList = foreNamesAsList;
		}

		@Override
		public String toString() {
			return name;
		}

	}

}
