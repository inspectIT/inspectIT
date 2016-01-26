package rocks.inspectit.shared.all.pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.testng.annotations.Test;

@SuppressWarnings("PMD")
public class WildcardMatchPatternTest {

	@Test
	public void trailingPattern() {
		WildcardMatchPattern matchPattern = new WildcardMatchPattern("*test");
		assertThat(matchPattern.match("testtest"), is(true));
		assertThat(matchPattern.match("123test"), is(true));
		assertThat(matchPattern.match("test"), is(true));
		assertThat(matchPattern.match("hello"), is(false));
		assertThat(matchPattern.match(""), is(false));
	}

	@Test
	public void middlePattern() {
		WildcardMatchPattern matchPattern = new WildcardMatchPattern("test*");
		assertThat(matchPattern.match("testtest"), is(true));
		assertThat(matchPattern.match("test123"), is(true));
		assertThat(matchPattern.match("test"), is(true));
		assertThat(matchPattern.match("hello"), is(false));
		assertThat(matchPattern.match(""), is(false));
	}

	@Test
	public void leadingPattern() {
		WildcardMatchPattern matchPattern = new WildcardMatchPattern("*test*");
		assertThat(matchPattern.match("testtesttest"), is(true));
		assertThat(matchPattern.match("testtest"), is(true));
		assertThat(matchPattern.match("123test123"), is(true));
		assertThat(matchPattern.match("test123"), is(true));
		assertThat(matchPattern.match("test"), is(true));
		assertThat(matchPattern.match("hello"), is(false));
		assertThat(matchPattern.match(""), is(false));
	}

	@Test
	public void mixedPattern() {
		WildcardMatchPattern matchPattern = new WildcardMatchPattern("test*hello*world");
		assertThat(matchPattern.match("test1hello2world"), is(true));
		assertThat(matchPattern.match("testhelloworld"), is(true));
		assertThat(matchPattern.match("test123helloworld"), is(true));
		assertThat(matchPattern.match("hello"), is(false));
		assertThat(matchPattern.match(""), is(false));
	}

	@Test
	public void everythingPattern() {
		WildcardMatchPattern matchPattern = new WildcardMatchPattern("*");
		assertThat(matchPattern.match("test1hello2world"), is(true));
		assertThat(matchPattern.match("testhelloworld"), is(true));
		assertThat(matchPattern.match("test123helloworld"), is(true));
		assertThat(matchPattern.match("hello"), is(true));
		assertThat(matchPattern.match(""), is(true));
	}

	@Test
	public void enhancedTests() {
		WildcardMatchPattern matchPattern = new WildcardMatchPattern("vsa.nprod.stamm.priv.regelstruktur.server.logic.*.*Evaluator");
		assertThat(matchPattern.match("vsa.nprod.stamm.priv.regelstruktur.server.logic.rechnungssplitting.RechnungsSplittingEvaluator"), is(true));
	}

	@Test
	public void templateSet() {
		assertThat(new WildcardMatchPattern("*").getPattern(), is("*"));
		assertThat(new WildcardMatchPattern("test").getPattern(), is("test"));
	}

}
