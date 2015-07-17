package info.novatec.inspectit.jmeter.data;

// NOCHKALL
public abstract class CountOnlyResult extends ResultBase {

	protected int count;

	public CountOnlyResult(long platformId, int count) {
		super(platformId);
		this.count = count;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

}
