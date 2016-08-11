package rocks.inspectit.ui.rcp.editor.inputdefinition.extra;

import com.google.common.base.Objects;

/**
 * Factory for {@link InputDefinitionExtraMarker}s.
 *
 * @author Ivan Senic
 *
 */
public final class InputDefinitionExtrasMarkerFactory {

	/**
	 * Private constructor.
	 */
	private InputDefinitionExtrasMarkerFactory() {
	}

	/**
	 * Marker for {@link NavigationSteppingInputDefinitionExtra}.
	 */
	public static final InputDefinitionExtraMarker<NavigationSteppingInputDefinitionExtra> NAVIGATION_STEPPING_EXTRAS_MARKER = new InputDefinitionExtraMarker<NavigationSteppingInputDefinitionExtra>() {
		@Override
		public Class<NavigationSteppingInputDefinitionExtra> getInputDefinitionExtraClass() {
			return NavigationSteppingInputDefinitionExtra.class;
		}

	};

	/**
	 * Marker for {@link ExceptionTypeInputDefinitionExtra}.
	 */
	public static final InputDefinitionExtraMarker<ExceptionTypeInputDefinitionExtra> EXCEPTION_TYPE_EXTRAS_MARKER = new InputDefinitionExtraMarker<ExceptionTypeInputDefinitionExtra>() {
		@Override
		public Class<ExceptionTypeInputDefinitionExtra> getInputDefinitionExtraClass() {
			return ExceptionTypeInputDefinitionExtra.class;
		}

	};

	/**
	 * Marker for {@link CombinedInvocationsInputDefinitionExtra}.
	 */
	public static final InputDefinitionExtraMarker<CombinedInvocationsInputDefinitionExtra> COMBINED_INVOCATIONS_EXTRAS_MARKER = new InputDefinitionExtraMarker<CombinedInvocationsInputDefinitionExtra>() {
		@Override
		public Class<CombinedInvocationsInputDefinitionExtra> getInputDefinitionExtraClass() {
			return CombinedInvocationsInputDefinitionExtra.class;
		}

	};

	/**
	 * Marker for {@link SqlStatementInputDefinitionExtra}.
	 */
	public static final InputDefinitionExtraMarker<SqlStatementInputDefinitionExtra> SQL_STATEMENT_EXTRAS_MARKER = new InputDefinitionExtraMarker<SqlStatementInputDefinitionExtra>() {
		@Override
		public Class<SqlStatementInputDefinitionExtra> getInputDefinitionExtraClass() {
			return SqlStatementInputDefinitionExtra.class;
		}

	};

	/**
	 * Marker for {@link HttpChartingInputDefinitionExtra}.
	 */
	public static final InputDefinitionExtraMarker<HttpChartingInputDefinitionExtra> HTTP_CHARTING_EXTRAS_MARKER = new InputDefinitionExtraMarker<HttpChartingInputDefinitionExtra>() {
		@Override
		public Class<HttpChartingInputDefinitionExtra> getInputDefinitionExtraClass() {
			return HttpChartingInputDefinitionExtra.class;
		}

	};

	/**
	 * Marker for {@link TimerDataChartingInputDefinitionExtra}.
	 */
	public static final InputDefinitionExtraMarker<TimerDataChartingInputDefinitionExtra> TIMER_DATA_CHARTING_EXTRAS_MARKER = new InputDefinitionExtraMarker<TimerDataChartingInputDefinitionExtra>() {
		@Override
		public Class<TimerDataChartingInputDefinitionExtra> getInputDefinitionExtraClass() {
			return TimerDataChartingInputDefinitionExtra.class;
		}

	};

	/**
	 * Marker for {@link RemoteInvocationInputDefinitionExtra}.
	 */
	public static final InputDefinitionExtraMarker<RemoteInvocationInputDefinitionExtra> REMOTE_INVOCATION_EXTRAS_MARKER = new InputDefinitionExtraMarker<RemoteInvocationInputDefinitionExtra>() {
		@Override
		public Class<RemoteInvocationInputDefinitionExtra> getInputDefinitionExtraClass() {
			return RemoteInvocationInputDefinitionExtra.class;
		}

	};

	/**
	 * Abstract class for input definition extras marker.
	 *
	 * @author Ivan Senic
	 *
	 * @param <E>
	 *            Type of input definition extra.
	 */
	public abstract static class InputDefinitionExtraMarker<E extends IInputDefinitionExtra> {

		/**
		 * @return Returns the class type of the input definition extra.
		 */
		public abstract Class<E> getInputDefinitionExtraClass();

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			return Objects.hashCode(getInputDefinitionExtraClass());
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object object) {
			if (this == object) {
				return true;
			}
			if (object == null) {
				return false;
			}
			if (getClass() != object.getClass()) {
				return false;
			}
			InputDefinitionExtraMarker<?> that = (InputDefinitionExtraMarker<?>) object;
			return Objects.equal(this.getInputDefinitionExtraClass(), that.getInputDefinitionExtraClass());

		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			return Objects.toStringHelper(this).add("inputDefintionExtraClass", getInputDefinitionExtraClass()).toString();
		}

	}

}
