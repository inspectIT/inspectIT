package info.novatec.inspectit.rcp.tester;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.expressions.PropertyTester;

/**
 * Tester for Permissions.
 * 
 * @author Lucca Hellriegel
 * @author Thomas Sachs
 * @author Mario Rose
 *
 */
public class PermissionTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		
		AVLTree userTree = getUserTreeFromServer();

		return userTree.contains(property);
	}

}
