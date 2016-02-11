package info.novatec.inspectit.jpa;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasValue;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("unchecked")
public class MapStringConverterTest {

	private MapStringConverter converter;

	@BeforeMethod
	public void init() {
		converter = new MapStringConverter();
	}

	@Test
	public void emptyList() {
		Map<Object, Object> original = Collections.emptyMap();
		Map<?, ?> result = converter.convertToEntityAttribute(converter.convertToDatabaseColumn(original));

		assertThat(result, is(not(nullValue())));
		assertThat(result.size(), is(0));
	}

	@Test
	public void emptyStrings() {
		Map<Object, Object> original = new HashMap<Object, Object>();
		original.put("", "");
		Map<?, ?> result = converter.convertToEntityAttribute(converter.convertToDatabaseColumn(original));

		assertThat(result, is(not(nullValue())));
		assertThat(result.size(), is(1));
		assertThat((Map<String, String>) result, hasKey(""));
		assertThat((Map<String, String>) result, hasValue(""));
	}

	@Test
	public void mixed() {
		Map<Object, Object> original = new HashMap<Object, Object>();
		original.put("one", "");
		original.put("two", " ");
		original.put("three", "four");
		original.put("", "five");
		original.put(" ", " ");
		Map<?, ?> result = converter.convertToEntityAttribute(converter.convertToDatabaseColumn(original));

		assertThat(result, is(not(nullValue())));
		assertThat(result.size(), is(5));
		assertThat((Map<String, String>) result, hasEntry("one", ""));
		assertThat((Map<String, String>) result, hasEntry("two", " "));
		assertThat((Map<String, String>) result, hasEntry("three", "four"));
		assertThat((Map<String, String>) result, hasEntry("", "five"));
		assertThat((Map<String, String>) result, hasEntry(" ", " "));
	}

}
