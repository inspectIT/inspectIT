package info.novatec.inspectit.rcp.ci.testers;

import info.novatec.inspectit.ci.business.impl.ApplicationDefinition;
import info.novatec.inspectit.ci.business.impl.BusinessTransactionDefinition;
import info.novatec.inspectit.rcp.provider.IApplicationProvider;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.expressions.PropertyTester;

/**
 * Property tester for the business context.
 *
 * @author Alexander Wert
 *
 */
public class BusinessContextTester extends PropertyTester {
	/**
	 * Tester property for the default application.
	 */
	public static final String IS_DEFAULT_APP_PROPERTY = "isDefaultApplication";

	/**
	 * Tester property for the default business transaction.
	 */
	public static final String IS_DEFAULT_BTX_PROPERTY = "isDefaultBusinessTransaction";

	/**
	 * Tester property for the moving up capability.
	 */
	public static final String CAN_MOVE_UP_PROPERTY = "canMoveUp";

	/**
	 * Tester property for the moving down capability.
	 */
	public static final String CAN_MOVE_DOWN_PROPERTY = "canMoveDown";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof IApplicationProvider) {
			IApplicationProvider applicationProvider = (IApplicationProvider) receiver;
			if (IS_DEFAULT_APP_PROPERTY.equals(property)) {
				return applicationProvider.getApplication().getId() == ApplicationDefinition.DEFAULT_ID;
			} else if (CAN_MOVE_UP_PROPERTY.equals(property)) {
				return applicationProvider.getIndexInParentList() > 0;
			} else if (CAN_MOVE_DOWN_PROPERTY.equals(property)) {
				int listSize = applicationProvider.getParentList().size();
				return applicationProvider.getIndexInParentList() < listSize - 2;
			}
		}

		if (receiver instanceof BusinessTransactionDefinition) {
			BusinessTransactionDefinition businessTransactionDef = (BusinessTransactionDefinition) receiver;
			if (IS_DEFAULT_BTX_PROPERTY.equals(property)) {
				return businessTransactionDef.getId() == BusinessTransactionDefinition.DEFAULT_ID;
			} else if (CAN_MOVE_UP_PROPERTY.equals(property) && ArrayUtils.isNotEmpty(args) && args[0] instanceof ApplicationDefinition) {
				return ((ApplicationDefinition) args[0]).getBusinessTransactionDefinitions().indexOf(businessTransactionDef) > 0;
			} else if (CAN_MOVE_DOWN_PROPERTY.equals(property) && ArrayUtils.isNotEmpty(args) && args[0] instanceof ApplicationDefinition) {
				int index = ((ApplicationDefinition) args[0]).getBusinessTransactionDefinitions().indexOf(businessTransactionDef);
				return index >= 0 && index < ((ApplicationDefinition) args[0]).getBusinessTransactionDefinitions().size() - 2;
			}
		}

		return false;
	}

}
