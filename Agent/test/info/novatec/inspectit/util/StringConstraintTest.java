package info.novatec.inspectit.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import info.novatec.inspectit.agent.AbstractLogSupport;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ObjectUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("PMD")
public class StringConstraintTest extends AbstractLogSupport {

	private StringConstraint constraint;

	private Map<String, Object> parameter;

	private int stringLength;

	@BeforeMethod
	public void init() {
		stringLength = new Random().nextInt(1000) + 1;
		parameter = new HashMap<String, Object>();
		parameter.put("stringLength", String.valueOf(stringLength));
		constraint = new StringConstraint(parameter);
	}

	@Test
	public void stringTooLong() {
		String testStr = fillString('x', stringLength + 1);
		String resultString = constraint.crop(testStr);
		String ending = "...";

		assertThat(resultString.length(), is(stringLength + ending.length()));
		assertThat(resultString, endsWith(ending));
	}

	@Test
	public void stringTooLongWithFinalChar() {
		char finalChar = '\'';
		String testStr = fillString('x', stringLength + 1);
		testStr += finalChar;
		String resultString = constraint.cropKeepFinalCharacter(testStr, finalChar);
		String ending = "..." + finalChar;

		assertThat(resultString.length(), is(stringLength + ending.length()));
		assertThat(resultString, endsWith(ending));
	}

	@Test
	public void stringShortEnough() {
		String testStr = fillString('x', stringLength - 1);
		String resultString = constraint.crop(testStr);

		assertThat("Same istances", resultString == testStr);
		assertThat(resultString.length(), is(equalTo(testStr.length())));
	}

	@Test
	public void stringExactSize() {
		String testStr = fillString('x', stringLength);
		String resultString = constraint.crop(testStr);

		assertThat(resultString, is(equalTo(testStr)));
		assertThat(resultString.length(), is(equalTo(testStr.length())));
	}

	@Test
	public void stringLengthOf0() {
		parameter.put("stringLength", String.valueOf(0));
		StringConstraint constr = new StringConstraint(parameter);

		String testStr = fillString('x', 100);
		String resultString = constr.crop(testStr);

		assertThat(resultString, isEmptyString());
	}

	@Test
	public void stringLengthOf0WithFinalChar() {
		char finalChar = '\'';
		parameter.put("stringLength", String.valueOf(0));
		StringConstraint constr = new StringConstraint(parameter);

		String testStr = finalChar + fillString('x', 50) + finalChar;
		String resultString = constr.cropKeepFinalCharacter(testStr, finalChar);

		assertThat(resultString, isEmptyString());
	}

	@Test
	public void stringIsNull() {
		String testStr = null;
		String resultString = constraint.crop(testStr);

		assertThat(resultString, is(nullValue()));
	}

	@Test
	public void cropStringMapNoCropping() {

		constraint = new StringConstraint(Collections.<String, Object> singletonMap("stringLength", "20"));

		final String param1 = "p1";
		final String param2 = "p2";
		final String param3 = "p3";
		final String param1VReal = "value";
		final String param2VReal1 = "value5";
		final String param2VReal2 = "value6";
		final String param3VReal1 = "value7";
		final String param3VReal2 = "value8";
		final String[] param1V = new String[] { param1VReal };
		final String[] param2V = new String[] { param2VReal1, param2VReal2 };
		final String[] param3V = new String[] { param3VReal1, param3VReal2 };
		final Map<String, String[]> parameterMap = new HashMap<String, String[]>();
		MapUtils.putAll(parameterMap, new Object[][] { { param1, param1V }, { param2, param2V }, { param3, param3V } });

		Map<String, String[]> result = constraint.crop(parameterMap);

		assertThat(result.size(), is(equalTo(parameterMap.size())));
		assertThat("Same compared by ObjectUtils", ObjectUtils.equals(result, parameterMap));
		assertThat(result.get(param1), is(param1V));
		assertThat(result.get(param2), is(param2V));
		assertThat(result.get(param3), is(param3V));
		assertThat(result.get(param1)[0], is(param1VReal));
		assertThat(result.get(param2)[0], is(param2VReal1));
		assertThat(result.get(param2)[1], is(param2VReal2));
		assertThat(result.get(param3)[0], is(param3VReal1));
		assertThat(result.get(param3)[1], is(param3VReal2));
	}

	@Test
	/** Tests whether the first entry is correctly copied to new map */
	public void cropStringMapCropSecondEntry() {
		constraint = new StringConstraint(Collections.<String, Object> singletonMap("stringLength", "20"));

		final String param1 = "p1";
		final String param2 = "p2";
		final String param3 = "p3";
		final String param1VReal = "value";
		final String param2VReal1 = "I am really very long and need to be cropped";
		final String param2VReal2 = "value6";
		final String param3VReal1 = "value7";
		final String param3VReal2 = "value8";
		final String[] param1V = new String[] { param1VReal };
		final String[] param2V = new String[] { param2VReal1, param2VReal2 };
		final String[] param3V = new String[] { param3VReal1, param3VReal2 };
		final Map<String, String[]> parameterMap = new HashMap<String, String[]>();
		MapUtils.putAll(parameterMap, new Object[][] { { param1, param1V }, { param2, param2V }, { param3, param3V } });

		Map<String, String[]> result = constraint.crop(parameterMap);

		assertThat(result.size(), is(equalTo(parameterMap.size())));
		assertThat("Not same by ObjectUtils, need to be cropped", !ObjectUtils.equals(result, parameterMap));
		assertThat(result.get(param1), is(param1V));
		assertThat(result.get(param2), is(not(param2V)));
		assertThat(result.get(param3), is(param3V));
		assertThat(result.get(param1)[0], is(param1VReal));
		assertThat(result.get(param2)[0], is(not(param2VReal1)));
		assertThat(result.get(param2)[1], is(param2VReal2));
		assertThat(result.get(param3)[0], is(param3VReal1));
		assertThat(result.get(param3)[1], is(param3VReal2));
	}

	private String fillString(char character, int count) {
		// creates a string of 'x' repeating characters
		char[] chars = new char[count];
		while (count > 0) {
			chars[--count] = character;
		}
		return new String(chars);
	}
}
