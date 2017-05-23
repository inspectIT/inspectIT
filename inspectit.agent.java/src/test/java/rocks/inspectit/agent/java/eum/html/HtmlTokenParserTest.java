package rocks.inspectit.agent.java.eum.html;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.testng.annotations.Test;

import rocks.inspectit.agent.java.eum.html.HtmlTokenParser.Result;
import rocks.inspectit.agent.java.eum.html.HtmlTokenParser.Token;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Jonas Kunz
 *
 */
public class HtmlTokenParserTest extends TestBase {

	HtmlTokenParser parser;

	public static class ParseToken extends HtmlTokenParserTest {

		@Test
		public void testOffsetSupport() {
			parser = new HtmlTokenParser("This part should be completely ignored. <div />", "This part should be completely ignored.".length());

			Result result = parser.parseToken();

			assertThat(result, equalTo(Result.SUCCESS));
			assertThat(parser.getParsedTokenType(), equalTo(Token.STANDALONE_TAG));
			assertThat(parser.getTagType(), equalTo((CharSequence) "div"));
			assertThat(parser.getTagArguments(), equalTo((CharSequence) ""));
		}

		@Test
		public void testTagNameDetectionValidTag() {
			StringBuilder src = new StringBuilder("<thizIAZacor");
			parser = new HtmlTokenParser(src, 0);

			Result resultIncomplete = parser.parseToken();

			assertThat(resultIncomplete, equalTo(Result.INCOMPLETE));
			src.append("rectName02134 arg1>");

			Result resultComplete = parser.parseToken();

			assertThat(resultComplete, equalTo(Result.SUCCESS));
			assertThat(parser.getTagType(), equalTo((CharSequence) "thizIAZacorrectName02134"));
		}

		@Test
		public void testTagNameDetectionInvalidChar() {
			parser = new HtmlTokenParser("<invalidnäme arg1>", 0);

			Result result = parser.parseToken();

			assertThat(result, equalTo(Result.FAILURE));
		}

		@Test
		public void testTagNameDetectionValidTagSpaceHandling() {
			parser = new HtmlTokenParser("< tag_name_must_not_have_spaces_to_the_opening_brace>", 0);

			Result result = parser.parseToken();

			assertThat(result, equalTo(Result.FAILURE));
		}

		@Test
		public void testOpeningTag() {
			parser = new HtmlTokenParser("<img src = \"cool images source \" >", 0);

			Result result = parser.parseToken();

			assertThat(result, equalTo(Result.SUCCESS));
			assertThat(parser.getParsedTokenType(), equalTo(Token.START_TAG));
			assertThat(parser.getTagType(), equalTo((CharSequence) "img"));
			assertThat(parser.getTagArguments(), equalTo((CharSequence) "src = \"cool images source \""));
		}

		@Test
		public void testDoubleComment() {
			parser = new HtmlTokenParser("<!--commentA--><!--commentB-->", 0);

			Result result = parser.parseToken();

			assertThat(result, equalTo(Result.SUCCESS));
			assertThat(parser.getParsedTokenType(), equalTo(Token.COMMENT));
			assertThat(parser.getCaret().getOffset(), equalTo("<!--commentA-->".length()));
		}

		@Test
		public void testIncompleteOpeningTag() {
			StringBuilder src = new StringBuilder("<");
			parser = new HtmlTokenParser(src, 0);

			Result resultIncomplete = parser.parseToken();

			assertThat(resultIncomplete, equalTo(Result.INCOMPLETE));
			src.append("div>");

			Result resultComplete = parser.parseToken();

			assertThat(resultComplete, equalTo(Result.SUCCESS));
			assertThat(parser.getParsedTokenType(), equalTo(Token.START_TAG));
			assertThat(parser.getTagType(), equalTo((CharSequence) "div"));
			assertThat(parser.getTagArguments(), equalTo((CharSequence) ""));
		}

		@Test
		public void testIncompleteDoctype() {

			StringBuilder src = new StringBuilder("<!");
			parser = new HtmlTokenParser(src, 0);

			Result resultIncomplete = parser.parseToken();

			assertThat(resultIncomplete, equalTo(Result.INCOMPLETE));
			src.append("DOCTYPE HTML>");

			Result resultComplete = parser.parseToken();

			assertThat(resultComplete, equalTo(Result.SUCCESS));
			assertThat(parser.getParsedTokenType(), equalTo(Token.START_TAG));
			assertThat(parser.getTagType(), equalTo((CharSequence) "!DOCTYPE"));

		}

		@Test
		public void testIncompleteComment() {

			StringBuilder src = new StringBuilder("<!");
			parser = new HtmlTokenParser(src, 0);

			Result resultIncompleteA = parser.parseToken();

			assertThat(resultIncompleteA, equalTo(Result.INCOMPLETE));

			src.append("-"); // NOPMD
			Result resultIncompleteB = parser.parseToken();

			assertThat(resultIncompleteB, equalTo(Result.INCOMPLETE));

			src.append("- tricked! this actually is a comment with random -> and -- and > ! "); // NOPMD
			Result resultIncompleteC = parser.parseToken();

			assertThat(resultIncompleteC, equalTo(Result.INCOMPLETE));

			src.append("-->");
			Result result = parser.parseToken();

			assertThat(result, equalTo(Result.SUCCESS));
			assertThat(parser.getParsedTokenType(), equalTo(Token.COMMENT));
		}

		@Test
		public void testInvalidCommentOpening() {
			parser = new HtmlTokenParser("</!-- this is not a valid comment opening! -->", 0);

			Result result = parser.parseToken();

			assertThat(result, equalTo(Result.FAILURE));
		}

		@Test
		public void testXMLTag() {
			StringBuilder src = new StringBuilder("<?");
			parser = new HtmlTokenParser(src, 0);

			Result incompleteResultA = parser.parseToken();

			assertThat(incompleteResultA, equalTo(Result.INCOMPLETE));

			src.append("xml version=\"1.0\" encoding=\"UTF-8\"  "); // NOPMD
			Result incompleteResultB = parser.parseToken();

			assertThat(incompleteResultB, equalTo(Result.INCOMPLETE));

			src.append("?"); // NOPMD
			Result incompleteResultC = parser.parseToken();

			assertThat(incompleteResultC, equalTo(Result.INCOMPLETE));

			src.append(">"); // NOPMD
			Result result = parser.parseToken();

			assertThat(result, equalTo(Result.SUCCESS));
			assertThat(parser.getParsedTokenType(), equalTo(Token.STANDALONE_TAG));
			assertThat(parser.getTagType(), equalTo((CharSequence) "?xml"));
			assertThat(parser.getTagArguments(), equalTo((CharSequence) "version=\"1.0\" encoding=\"UTF-8\""));
		}

		@Test
		public void testQuotedArguments() {
			StringBuilder src = new StringBuilder("<div arg1 = \"contains weird stuff like / or > or />");
			parser = new HtmlTokenParser(src, 0);

			Result resultIncomplete = parser.parseToken();

			assertThat(resultIncomplete, equalTo(Result.INCOMPLETE));
			src.append("\"  arg2 = \'contains weird stuff like / or > or />\' >");

			Result result = parser.parseToken();

			assertThat(result, equalTo(Result.SUCCESS));
			assertThat(parser.getParsedTokenType(), equalTo(Token.START_TAG));
			assertThat(parser.getTagType(), equalTo((CharSequence) "div"));
			assertThat(parser.getTagArguments(), equalTo((CharSequence) "arg1 = \"contains weird stuff like / or > or />\"  arg2 = \'contains weird stuff like / or > or />\'"));
		}

		@Test
		public void testEndTagWithArgumetns() {
			parser = new HtmlTokenParser("</div end tags may not contain arguments >", 0);

			Result results = parser.parseToken();

			assertThat(results, equalTo(Result.FAILURE));
		}

		@Test
		public void testComplexDoctypeTag() {
			StringBuilder src = new StringBuilder("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
			parser = new HtmlTokenParser(src, 0);

			Result result = parser.parseToken();

			assertThat(result, equalTo(Result.SUCCESS));
			assertThat(parser.getParsedTokenType(), equalTo(Token.START_TAG));
			assertThat(parser.getTagType(), equalTo((CharSequence) "!DOCTYPE"));
			assertThat(parser.getTagArguments(), equalTo((CharSequence) "html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\""));
		}

		@Test
		public void testIncompleteClosingTag() {
			StringBuilder src = new StringBuilder("<");
			parser = new HtmlTokenParser(src, 0);

			Result incompleteResult = parser.parseToken();

			assertThat(incompleteResult, equalTo(Result.INCOMPLETE));
			src.append("/div>");

			Result result = parser.parseToken();

			assertThat(result, equalTo(Result.SUCCESS));
			assertThat(parser.getParsedTokenType(), equalTo(Token.END_TAG));
			assertThat(parser.getTagType(), equalTo((CharSequence) "div"));
			assertThat(parser.getTagArguments(), equalTo((CharSequence) ""));
		}

		@Test
		public void testInvalidTagOpening() {
			StringBuilder src = new StringBuilder("(div />");
			parser = new HtmlTokenParser(src, 0);

			Result result = parser.parseToken();

			assertThat(result, equalTo(Result.FAILURE));
		}

		@Test
		public void testCaretMoving() {
			parser = new HtmlTokenParser("This part should be completely ignored. <div />", 0);
			parser.getCaret().goN("This part should be completely ignored.".length());

			Result result = parser.parseToken();

			assertThat(result, equalTo(Result.SUCCESS));
			assertThat(parser.getParsedTokenType(), equalTo(Token.STANDALONE_TAG));
			assertThat(parser.getTagType(), equalTo((CharSequence) "div"));
			assertThat(parser.getTagArguments(), equalTo((CharSequence) ""));
		}
	}

	public class ResetState extends HtmlTokenParserTest {

		@Test
		public void testMultipleTagsParsing() {
			parser = new HtmlTokenParser("<html> <head/>", 0);

			Result resultA = parser.parseToken();

			assertThat(resultA, equalTo(Result.SUCCESS));
			assertThat(parser.getParsedTokenType(), equalTo(Token.START_TAG));
			assertThat(parser.getTagType(), equalTo((CharSequence) "html"));

			parser.resetState();
			Result resultB = parser.parseToken();

			assertThat(resultB, equalTo(Result.SUCCESS));
			assertThat(parser.getParsedTokenType(), equalTo(Token.STANDALONE_TAG));
			assertThat(parser.getTagType(), equalTo((CharSequence) "head"));
		}

	}

}
