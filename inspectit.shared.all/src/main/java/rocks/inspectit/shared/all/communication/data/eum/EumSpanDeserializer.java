package rocks.inspectit.shared.all.communication.data.eum;

import java.io.IOException;
import java.sql.Timestamp;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;

import rocks.inspectit.shared.all.tracing.data.SpanIdent;

/**
 * Specialized deserializer for EumSpans. When serialized, the properties of the EUMSpan and its
 * details are merged in a single JSON object. This deserializer handles this correctly.
 *
 * @author Jonas Kunz
 *
 */
public class EumSpanDeserializer extends StdDeserializer<EUMSpan> {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 7362569697693773107L;

	/**
	 * Mapper used for deserializing the details.
	 */
	private final transient ObjectMapper om = new ObjectMapper();

	/**
	 * Default Constructor.
	 */
	EumSpanDeserializer() {
		super(EUMSpan.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EUMSpan deserialize(JsonParser jp, DeserializationContext context) throws IOException, JsonProcessingException {
		EUMSpan result = new EUMSpan();
		JsonNode node = jp.readValueAsTree();
		if (node instanceof ObjectNode) {

			long id;
			long parentId = 0;
			long traceId = 0;
			ObjectNode obj = (ObjectNode) node;
			JsonNode idNode = obj.get("id");
			JsonNode parentIdNode = obj.get("parentId");
			JsonNode traceIdNode = obj.get("traceId");
			try {
				if (idNode == null) {
					throw new JsonParseException(jp, "Id for EUMSpan is missing!");
				}
				id = Long.parseLong(idNode.asText(), 16);
				if (traceIdNode != null) {
					traceId = Long.parseLong(traceIdNode.asText(), 16);
				} else {
					traceId = id;
				}
				if (parentIdNode != null) {
					parentId = Long.parseLong(parentIdNode.asText(), 16);
				} else {
					parentId = 0;
				}
			} catch (NumberFormatException ne) {
				throw new JsonParseException(jp, "Could not parse EUM Span, expected numeric values.", ne); // NOPMD
			}
			result.setSpanIdent(new SpanIdent(id, traceId));
			result.setParentSpanId(parentId);

			JsonNode enterTimestampNode = obj.get("enterTimestamp");
			if ((enterTimestampNode != null) && (enterTimestampNode.asDouble() != 0)) {
				double enterTime = enterTimestampNode.asDouble();
				result.setTimeStamp(new Timestamp(Math.round(enterTime)));
			}
			JsonNode durationNode = obj.get("duration");
			if (durationNode != null) {
				result.setDuration(durationNode.asDouble());
			}
			// decode the details normally annotation based
			result.setDetails(om.treeToValue(obj, AbstractEUMSpanDetails.class));

			return result;
		} else {
			throw new JsonParseException(jp, "Could not parse EUM Span, expected object node instead of value node.");
		}
	}

}