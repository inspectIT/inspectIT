package info.novatec.inspectit.cmr.processor.impl;

import info.novatec.inspectit.cmr.processor.AbstractCmrDataProcessor;
import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.data.InvocationSequenceData;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * This processor enriches {@link InvocationSequenceData} instances (i.e. roots of invocation
 * sequences) with business context information (i.e. corresponding application and business
 * transaction).
 *
 * @author Alexander Wert
 *
 */
public class BusinessContextRecognitionProcessor extends AbstractCmrDataProcessor {

	/**
	 * {@link ExpressionEvaluation} instance.
	 */
	@Autowired
	private ExpressionEvaluation evaluation;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, EntityManager entityManager) {
		InvocationSequenceData invocSequence = (InvocationSequenceData) defaultData;
		evaluation.assignBusinessContext(invocSequence);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return defaultData instanceof InvocationSequenceData;
	}

}
