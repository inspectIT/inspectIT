package info.novatec.inspectit.jmeter.data;

import info.novatec.inspectit.communication.data.InvocationSequenceData;

// NOCHKALL
public class InvocationDetailResult implements InspectITResultMarker {
	long id;
	double duration;
	long childCount;

	public InvocationDetailResult(long id, double duration, long childCount) {
		super();
		this.id = id;
		this.duration = duration;
		this.childCount = childCount;
	}

	public InvocationDetailResult(InvocationSequenceData data) {
		super();
		if (null != data) {
			this.id = data.getId();
			this.duration = data.getDuration();
			this.childCount = data.getChildCount();
		}
	}
}
