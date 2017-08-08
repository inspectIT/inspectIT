package rocks.inspectit.shared.cs.data.invocationtree;

import java.sql.Timestamp;

import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.testbase.TestBase;
import rocks.inspectit.shared.all.tracing.data.ClientSpan;
import rocks.inspectit.shared.all.tracing.data.PropagationType;
import rocks.inspectit.shared.all.tracing.data.ServerSpan;
import rocks.inspectit.shared.all.tracing.data.Span;
import rocks.inspectit.shared.all.tracing.data.SpanIdent;

/**
 * Base class for the invocation tree tests providing useful methods for creating invocation trees.
 *
 * @author Marius Oehler
 *
 */
@SuppressWarnings("PMD")
public class InvocationTreeTestBase extends TestBase {

	static int idSequence = 1;

	static int timeSequence = 1;

	ServerSpan createServerSpan(Span parent) {
		ServerSpan span = new ServerSpan();
		span.setSpanIdent(new SpanIdent(idSequence++, 0));
		span.setTimeStamp(new Timestamp(timeSequence++));
		span.setPropagationType(PropagationType.PROCESS);
		if (parent != null) {
			span.setParentSpanId(parent.getSpanIdent().getId());
		}
		return span;
	}

	ServerSpan createSdkSpan(Span parent) {
		ServerSpan span = createServerSpan(parent);
		span.setPropagationType(null);
		return span;
	}

	ClientSpan createClientSpan(Span parent) {
		ClientSpan span = new ClientSpan();
		span.setSpanIdent(new SpanIdent(idSequence++, 0));
		span.setTimeStamp(new Timestamp(timeSequence++));
		span.setPropagationType(PropagationType.PROCESS);
		if (parent != null) {
			span.setParentSpanId(parent.getSpanIdent().getId());
		}
		return span;
	}

	InvocationSequenceData createSequence(Span span) {
		InvocationSequenceData data = new InvocationSequenceData();
		data.setId(idSequence++);
		if (span != null) {
			data.setSpanIdent(span.getSpanIdent());
		}
		data.setTimeStamp(new Timestamp(timeSequence++));
		return data;
	}

}
