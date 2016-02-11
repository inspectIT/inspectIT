package info.novatec.inspectit.agent.analyzer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import info.novatec.inspectit.agent.analyzer.impl.SimpleMatchPattern;

import org.testng.annotations.Test;

@SuppressWarnings("PMD")
public class SimpleMatchPatternTest {

	@Test
	public void trailingPattern() {
		SimpleMatchPattern matchPattern = new SimpleMatchPattern("*test");
		assertThat(matchPattern.match("testtest"), is(true));
		assertThat(matchPattern.match("123test"), is(true));
		assertThat(matchPattern.match("test"), is(true));
		assertThat(matchPattern.match("hello"), is(false));
		assertThat(matchPattern.match(""), is(false));
	}

	@Test
	public void middlePattern() {
		SimpleMatchPattern matchPattern = new SimpleMatchPattern("test*");
		assertThat(matchPattern.match("testtest"), is(true));
		assertThat(matchPattern.match("test123"), is(true));
		assertThat(matchPattern.match("test"), is(true));
		assertThat(matchPattern.match("hello"), is(false));
		assertThat(matchPattern.match(""), is(false));
	}

	@Test
	public void leadingPattern() {
		SimpleMatchPattern matchPattern = new SimpleMatchPattern("*test*");
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
		SimpleMatchPattern matchPattern = new SimpleMatchPattern("test*hello*world");
		assertThat(matchPattern.match("test1hello2world"), is(true));
		assertThat(matchPattern.match("testhelloworld"), is(true));
		assertThat(matchPattern.match("test123helloworld"), is(true));
		assertThat(matchPattern.match("hello"), is(false));
		assertThat(matchPattern.match(""), is(false));
	}

	@Test
	public void everythingPattern() {
		SimpleMatchPattern matchPattern = new SimpleMatchPattern("*");
		assertThat(matchPattern.match("test1hello2world"), is(true));
		assertThat(matchPattern.match("testhelloworld"), is(true));
		assertThat(matchPattern.match("test123helloworld"), is(true));
		assertThat(matchPattern.match("hello"), is(true));
		assertThat(matchPattern.match(""), is(true));
	}

	@Test
	public void enhancedTests() {
		SimpleMatchPattern matchPattern = new SimpleMatchPattern("vsa.nprod.stamm.priv.regelstruktur.server.logic.*.*Evaluator");
		assertThat(matchPattern.match("vsa.nprod.stamm.priv.regelstruktur.server.logic.rechnungssplitting.RechnungsSplittingEvaluator"), is(true));
	}

}
