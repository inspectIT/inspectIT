package rocks.inspectit.shared.all.communication.data.eum;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.mockito.InjectMocks;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import rocks.inspectit.shared.all.testbase.TestBase;

@SuppressWarnings("PMD")
public class EumSpanDeserializerTest extends TestBase {

	static final long ELEMENT_ID = 11;
	static final long PARENT_ID = 12;
	static final long TRACE_ID = 13;
	static final long ENTER_TIMESTAMP = 1234567L;
	static final double DURATION = 5.5;

	static final String FUNCTION_NAME = "myFunc";
	static final String EVENT_TYPE = "load";

	static final JsonNodeFactory FAC = JsonNodeFactory.instance;

	@InjectMocks
	EumSpanDeserializer deserializer;

	public ObjectNode buildEventListenerRecord() {
		ObjectNode rec = FAC.objectNode();
		rec.put("type", "listenerExecution");
		rec.put("id", Long.toHexString(ELEMENT_ID));
		rec.put("traceId", Long.toHexString(TRACE_ID));
		rec.put("parentId", Long.toHexString(PARENT_ID));
		rec.put("enterTimestamp", ENTER_TIMESTAMP);
		rec.put("duration", DURATION);
		rec.put("functionName", FUNCTION_NAME);
		rec.put("eventType", EVENT_TYPE);
		return rec;
	}

	public EUMSpan performDeserialization(ObjectNode nodeToParse) throws IOException {
		String json = nodeToParse.toString();
		InputStream stream = new ByteArrayInputStream(json.getBytes());
		ObjectMapper mapper = new ObjectMapper();
		JsonParser parser = mapper.getFactory().createParser(stream);
		DeserializationContext ctxt = mapper.getDeserializationContext();
		return deserializer.deserialize(parser, ctxt);
	}

	public static class Deserialize extends EumSpanDeserializerTest {

		@Test
		public void testAllDataAvailable() throws IOException {
			ObjectNode node = buildEventListenerRecord();
			EUMSpan span = performDeserialization(node);

			assertThat(span.getDetails(), instanceOf(JSEventListenerExecution.class));
			assertThat(span.getSpanIdent().getId(), equalTo(ELEMENT_ID));
			assertThat(span.getSpanIdent().getTraceId(), equalTo(TRACE_ID));
			assertThat(span.getParentSpanId(), equalTo(PARENT_ID));
			assertThat(span.getTimeStamp().getTime(), equalTo(ENTER_TIMESTAMP));
			assertThat(span.getDuration(), equalTo(DURATION));
			JSEventListenerExecution exec = (JSEventListenerExecution) span.getDetails();
			assertThat(exec.getFunctionName(), equalTo(FUNCTION_NAME));
			assertThat(exec.getEventType(), equalTo(EVENT_TYPE));
		}

		@Test
		public void testDefaultTraceId() throws IOException {
			ObjectNode node = buildEventListenerRecord();
			node.remove("traceId");
			EUMSpan span = performDeserialization(node);

			assertThat(span.getDetails(), instanceOf(JSEventListenerExecution.class));
			assertThat(span.getSpanIdent().getId(), equalTo(ELEMENT_ID));
			assertThat(span.getSpanIdent().getTraceId(), equalTo(ELEMENT_ID));
			assertThat(span.getParentSpanId(), equalTo(PARENT_ID));
			assertThat(span.getTimeStamp().getTime(), equalTo(ENTER_TIMESTAMP));
			assertThat(span.getDuration(), equalTo(DURATION));
			JSEventListenerExecution exec = (JSEventListenerExecution) span.getDetails();
			assertThat(exec.getFunctionName(), equalTo(FUNCTION_NAME));
			assertThat(exec.getEventType(), equalTo(EVENT_TYPE));
		}

		@Test
		public void testDefaultParentId() throws IOException {
			ObjectNode node = buildEventListenerRecord();
			node.remove("parentId");
			EUMSpan span = performDeserialization(node);

			assertThat(span.getDetails(), instanceOf(JSEventListenerExecution.class));
			assertThat(span.getSpanIdent().getId(), equalTo(ELEMENT_ID));
			assertThat(span.getSpanIdent().getTraceId(), equalTo(TRACE_ID));
			assertThat(span.getParentSpanId(), equalTo(0L));
			assertThat(span.getTimeStamp().getTime(), equalTo(ENTER_TIMESTAMP));
			assertThat(span.getDuration(), equalTo(DURATION));
			JSEventListenerExecution exec = (JSEventListenerExecution) span.getDetails();
			assertThat(exec.getFunctionName(), equalTo(FUNCTION_NAME));
			assertThat(exec.getEventType(), equalTo(EVENT_TYPE));
		}

		@Test(expectedExceptions = JsonProcessingException.class)
		public void testMissingId() throws IOException {
			ObjectNode node = buildEventListenerRecord();
			node.remove("id");
			performDeserialization(node);
		}
	}
}
