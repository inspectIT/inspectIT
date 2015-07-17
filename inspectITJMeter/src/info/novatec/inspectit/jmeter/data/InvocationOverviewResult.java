package info.novatec.inspectit.jmeter.data;

import java.util.List;

// NOCHKALL
public class InvocationOverviewResult extends ResultBase {
	public long count;
	public List<Long> invocationIds;

	public InvocationOverviewResult(long platformId, long count, List<Long> invocationIds) {
		super(platformId);
		this.count = count;
		this.invocationIds = invocationIds;
	}
}
