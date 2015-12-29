package info.novatec.inspectit.jpa;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("unchecked")
public class ListStringConverterTest {

	private ListStringConverter converter;

	@BeforeMethod
	public void init() {
		converter = new ListStringConverter();
	}

	@Test
	public void emptyList() {
		List<Object> original = Collections.emptyList();
		List<?> result = converter.convertToEntityAttribute(converter.convertToDatabaseColumn(original));

		assertThat(result, is(not(nullValue())));
		assertThat(result, is(empty()));
	}

	@Test
	public void emptyString() {
		List<Object> original = new ArrayList<Object>();
		original.add("");
		List<?> result = converter.convertToEntityAttribute(converter.convertToDatabaseColumn(original));

		assertThat(result, is(not(nullValue())));
		assertThat(result, is(not(empty())));
		assertThat(result, hasSize(1));
		assertThat((List<String>) result, hasItem(""));
	}

	@Test
	public void manyStrings() {
		List<Object> original = new ArrayList<Object>();
		original.add("one");
		original.add("two");
		original.add("three");
		List<?> result = converter.convertToEntityAttribute(converter.convertToDatabaseColumn(original));

		assertThat(result, is(not(nullValue())));
		assertThat(result, is(not(empty())));
		assertThat(result, hasSize(3));
		assertThat((List<String>) result, hasItem("one"));
		assertThat((List<String>) result, hasItem("two"));
		assertThat((List<String>) result, hasItem("three"));
	}

	@Test
	public void mixed() {
		List<Object> original = new ArrayList<Object>();
		original.add("one");
		original.add(" ");
		original.add("two");
		original.add("");
		List<?> result = converter.convertToEntityAttribute(converter.convertToDatabaseColumn(original));

		assertThat(result, is(not(nullValue())));
		assertThat(result, is(not(empty())));
		assertThat(result, hasSize(4));
		assertThat((List<String>) result, hasItem("one"));
		assertThat((List<String>) result, hasItem("two"));
		assertThat((List<String>) result, hasItem(""));
		assertThat((List<String>) result, hasItem(" "));
	}

}
