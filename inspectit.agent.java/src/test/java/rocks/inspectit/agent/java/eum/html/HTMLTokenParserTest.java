package rocks.inspectit.agent.java.eum.html;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.testng.annotations.Test;

import rocks.inspectit.agent.java.eum.html.HTMLTokenParser.Result;
import rocks.inspectit.agent.java.eum.html.HTMLTokenParser.Token;
import rocks.inspectit.shared.all.testbase.TestBase;

/**
 * @author Jonas Kunz
 *
 */
public class HTMLTokenParserTest extends TestBase {

	HTMLTokenParser parser;

	public static class ParseToken extends HTMLTokenParserTest {

		@Test
		public void testOffsetSupport() {
			parser = new HTMLTokenParser("This part should be completely ignored. <div />", "This part should be completely ignored.".length());
			assertThat(parser.parseToken(), equalTo(Result.SUCCESS));
			assertThat(parser.getParsedTokenType(), equalTo(Token.STANDALONE_TAG));
			assertThat(parser.getTagType(), equalTo((CharSequence) "div"));
			assertThat(parser.getTagArguments(), equalTo((CharSequence) ""));
		}

		@Test
		public void testTagNameDetection() {

			StringBuilder src = new StringBuilder("<thizIAZacor");
			parser = new HTMLTokenParser(src, 0);
			assertThat(parser.parseToken(), equalTo(Result.INCOMPLETE));
			src.append("rectName02134 arg1>");
			assertThat(parser.parseToken(), equalTo(Result.SUCCESS));
			assertThat(parser.getTagType(), equalTo((CharSequence) "thizIAZacorrectName02134"));

			parser = new HTMLTokenParser("<invalidnäme arg1>", 0);
			assertThat(parser.parseToken(), equalTo(Result.FAILURE));

			parser = new HTMLTokenParser("< tag_name_must_not_have_spaces_to_the_opening_brace>", 0);
			assertThat(parser.parseToken(), equalTo(Result.FAILURE));
		}

		@Test
		public void testOpeningTag() {
			parser = new HTMLTokenParser("<img src = \"cool images source \" >", 0);

			assertThat(parser.parseToken(), equalTo(Result.SUCCESS));
			assertThat(parser.getParsedTokenType(), equalTo(Token.START_TAG));
			assertThat(parser.getTagType(), equalTo((CharSequence) "img"));
			assertThat(parser.getTagArguments(), equalTo((CharSequence) "src = \"cool images source \""));
		}

		@Test
		public void testDoubleComment() {
			parser = new HTMLTokenParser("<!--commentA--><!--commentB-->", 0);

			assertThat(parser.parseToken(), equalTo(Result.SUCCESS));
			assertThat(parser.getParsedTokenType(), equalTo(Token.COMMENT));
			assertThat(parser.getCaret().getOffset(), equalTo("<!--commentA-->".length()));
		}

		@Test
		public void testIncompleteOpeningTag() {
			StringBuilder src = new StringBuilder("<");
			parser = new HTMLTokenParser(src, 0);

			assertThat(parser.parseToken(), equalTo(Result.INCOMPLETE));

			src.append("div>");

			assertThat(parser.parseToken(), equalTo(Result.SUCCESS));
			assertThat(parser.getParsedTokenType(), equalTo(Token.START_TAG));
			assertThat(parser.getTagType(), equalTo((CharSequence) "div"));
			assertThat(parser.getTagArguments(), equalTo((CharSequence) ""));
		}

		@Test
		public void testIncompleteCommentOrDoctype() {

			StringBuilder src = new StringBuilder("<!");
			parser = new HTMLTokenParser(src, 0);
			assertThat(parser.parseToken(), equalTo(Result.INCOMPLETE));
			src.append("DOCTYPE HTML>");
			assertThat(parser.parseToken(), equalTo(Result.SUCCESS));
			assertThat(parser.getParsedTokenType(), equalTo(Token.START_TAG));
			assertThat(parser.getTagType(), equalTo((CharSequence) "!DOCTYPE"));

			src = new StringBuilder("<!");
			parser = new HTMLTokenParser(src, 0);
			assertThat(parser.parseToken(), equalTo(Result.INCOMPLETE));
			src.append("-- tricked! this actually is a comment with random -> and -- and > ! -->");
			assertThat(parser.parseToken(), equalTo(Result.SUCCESS));
			assertThat(parser.getParsedTokenType(), equalTo(Token.COMMENT));
		}

		@Test
		public void testInvalidCommentOpening() {
			parser = new HTMLTokenParser("</!-- this is not a vlaid comment opening! -->", 0);
			assertThat(parser.parseToken(), equalTo(Result.FAILURE));
		}

		@Test
		public void testXMLTag() {
			StringBuilder src = new StringBuilder("<?");
			parser = new HTMLTokenParser(src, 0);
			assertThat(parser.parseToken(), equalTo(Result.INCOMPLETE));
			src.append("xml version=\"1.0\" encoding=\"UTF-8\"?>");
			assertThat(parser.parseToken(), equalTo(Result.SUCCESS));

			assertThat(parser.getParsedTokenType(), equalTo(Token.STANDALONE_TAG));
			assertThat(parser.getTagType(), equalTo((CharSequence) "?xml"));
			assertThat(parser.getTagArguments(), equalTo((CharSequence) "version=\"1.0\" encoding=\"UTF-8\""));
		}

		@Test
		public void testQuotedArguments() {
			StringBuilder src = new StringBuilder("<div arg1 = \"contains weird stuff like / or > or />");
			parser = new HTMLTokenParser(src, 0);
			assertThat(parser.parseToken(), equalTo(Result.INCOMPLETE));
			src.append("\"  arg2 = \'contains weird stuff like / or > or />\' >");
			assertThat(parser.parseToken(), equalTo(Result.SUCCESS));

			assertThat(parser.getParsedTokenType(), equalTo(Token.START_TAG));
			assertThat(parser.getTagType(), equalTo((CharSequence) "div"));
			assertThat(parser.getTagArguments(), equalTo((CharSequence) "arg1 = \"contains weird stuff like / or > or />\"  arg2 = \'contains weird stuff like / or > or />\'"));
		}

		@Test
		public void testEndTagWithArgumetns() {
			parser = new HTMLTokenParser("</div end tags may not contain arguments >", 0);
			assertThat(parser.parseToken(), equalTo(Result.FAILURE));
		}

		@Test
		public void testDoctypeTag() {
			StringBuilder src = new StringBuilder("<!");
			parser = new HTMLTokenParser(src, 0);
			assertThat(parser.parseToken(), equalTo(Result.INCOMPLETE));
			src.append("DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
			assertThat(parser.parseToken(), equalTo(Result.SUCCESS));

			assertThat(parser.getParsedTokenType(), equalTo(Token.START_TAG));
			assertThat(parser.getTagType(), equalTo((CharSequence) "!DOCTYPE"));
			assertThat(parser.getTagArguments(), equalTo((CharSequence) "html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\""));
		}

		@Test
		public void testIncompleteClosingTag() {
			StringBuilder src = new StringBuilder("<");
			parser = new HTMLTokenParser(src, 0);

			assertThat(parser.parseToken(), equalTo(Result.INCOMPLETE));

			src.append("/div>");

			assertThat(parser.parseToken(), equalTo(Result.SUCCESS));
			assertThat(parser.getParsedTokenType(), equalTo(Token.END_TAG));
			assertThat(parser.getTagType(), equalTo((CharSequence) "div"));
			assertThat(parser.getTagArguments(), equalTo((CharSequence) ""));
		}

		@Test
		public void testInvalidTagOpening() {
			StringBuilder src = new StringBuilder("(div />");
			parser = new HTMLTokenParser(src, 0);
			assertThat(parser.parseToken(), equalTo(Result.FAILURE));
		}
	}

	public class MoveReadingPosition extends HTMLTokenParserTest {

		@Test
		public void testCaretMoving() {
			parser = new HTMLTokenParser("This part should be completely ignored. <div />", 0);
			parser.getCaret().goN("This part should be completely ignored.".length());
			assertThat(parser.parseToken(), equalTo(Result.SUCCESS));
			assertThat(parser.getParsedTokenType(), equalTo(Token.STANDALONE_TAG));
			assertThat(parser.getTagType(), equalTo((CharSequence) "div"));
			assertThat(parser.getTagArguments(), equalTo((CharSequence) ""));
		}

	}

	public class ResetState extends HTMLTokenParserTest {

		@Test
		public void testMultipleTagsParsing() {
			parser = new HTMLTokenParser("<html> <head/>", 0);
			assertThat(parser.parseToken(), equalTo(Result.SUCCESS));
			assertThat(parser.getParsedTokenType(), equalTo(Token.START_TAG));
			assertThat(parser.getTagType(), equalTo((CharSequence) "html"));

			parser.resetState();

			assertThat(parser.parseToken(), equalTo(Result.SUCCESS));
			assertThat(parser.getParsedTokenType(), equalTo(Token.STANDALONE_TAG));
			assertThat(parser.getTagType(), equalTo((CharSequence) "head"));
		}

	}

}
