package info.novatec.inspectit.jmeter.data;

// NOCHKALL
public class ResultBase implements InspectITResultMarker {

	private long platformId;

	public ResultBase(long platformId) {
		super();
		this.platformId = platformId;
	}

	public long getPlatformId() {
		return platformId;
	}

	public void setPlatformId(long platformId) {
		this.platformId = platformId;
	}
}
