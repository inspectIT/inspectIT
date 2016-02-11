package info.novatec.inspectit.agent.sensor.method.http;

import info.novatec.inspectit.agent.sensor.method.http.StartEndMarker.MutableInteger;

/**
 * Provides a thread local means to store the information if we returned to the end of a given
 * method an invocation.
 * 
 * @author Stefan Siegl
 */
public class StartEndMarker extends ThreadLocal<MutableInteger> {

	/**
	 * Increase the counter. Use this at the point that you want marked.
	 */
	public void markCall() {
		super.get().increase();
	}

	/**
	 * Increase the counter.
	 */
	public void markEndCall() {
		super.get().decrease();
	}

	/**
	 * checks if we already returned to the method that marked the first call.
	 * 
	 * @return checks if we already returned to the method that marked the first call.
	 */
	public boolean matchesFirst() {
		return super.get().getValue() == 0;
	}

	/**
	 * Checks if we already marked a call.
	 * 
	 * @return Checks if we already marked a call
	 */
	public boolean isMarkerSet() {
		return super.get().isSet();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected MutableInteger initialValue() {
		return new MutableInteger();
	}

	/**
	 * Simple realization of a mutable integer object. We cannot use the one of the commons project
	 * here as we will have a new dependency.
	 * 
	 * @author Stefan Siegl
	 */
	static class MutableInteger {
		/** The wrapped integer. */
		private int value;

		/**
		 * Marks the first call to the counter. This is necessary in the cases where we have a non
		 * http providing method at first (for this case, lets say no other method has the http
		 * sensor). This means that the second after body would be reached with counter value 0,
		 * which would return true for the check if the counter is returned to its starting
		 * position.
		 */
		private boolean set = false;

		/** Constructor. */
		public MutableInteger() {
			value = 0;
		}

		/**
		 * Returns the value.
		 * 
		 * @return the value.
		 */
		public int getValue() {
			return value;
		}

		public void setValue(int value) {
			this.value = value;
		}

		/**
		 * Increases the value and sets the marker to be accessed.
		 */
		public void increase() {
			value++;
			if (!set) {
				set = true;
			}
		}

		/**
		 * Decreases the value and sets the marker to be accessed.
		 */
		public void decrease() {
			value--;
		}

		public boolean isSet() {
			return set;
		}
	}
}
