package info.novatec.inspectit.security;

import java.io.Serializable;

/**
 * Implementation of AVL Tree.
 * 
 * @author Clemens Geibel
 * @author Thomas Sachs
 * @author Mario Rose
 */

public class AVLTree implements Serializable {
	
	/**
	 * UID for Serialization. 
	 */
	private static final long serialVersionUID = 7162234592002092681L;
	/**
	 * Root of Tree.
	 */
	private Node root;

	/**
	 * Default constructor.
	 */
	public AVLTree() {
		root = null;
	}

	/**
	 * Insert operation.
	 * 
	 * @param data
	 *            Data to insert.
	 */
	public void insert(String data) {
		insert(root, null, data);
	}

	/**
	 * Recursive insert operation.
	 * 
	 * @param node
	 *            Next node to try to insert the data.
	 * 
	 * @param parent
	 * 			  Parentnode of node
	 * 
	 * @param data
	 *            Data to insert.
	 */
	private void insert(Node node, Node parent, String data) {
		if (node == null) {
			node = new Node(data, parent);
			rebalance(node);
		} else if (data.compareTo(node.getData()) < 0) {
			insert(node.getLeft(), node, data);
		} else {
			insert(node.getRight(), node, data);
		}
	}

	/**
	 * Check, if the Tree needs to be rebalanced.
	 * 
	 * @param node
	 *            Current Node, that will be checked.
	 */
	private void rebalance(Node node) {
		Node parentNode = node.getParent();
		if (parentNode == null) {
			return;
		}
		boolean isLeft = node.getData().compareTo(parentNode.getData()) < 0;
		int parentBalance = parentNode.getBalance();
		if (isLeft) {
			parentBalance--;
		} else {
			parentBalance++;
		}

		parentNode.setBalance(parentBalance);

		int nodeBalance = node.getBalance();
		if (parentBalance == -2 && nodeBalance == -1) {
			rotateRight(parentNode);
		} else if (parentBalance == 2 && nodeBalance == 1) {
			rotateLeft(parentNode);
		} else if (parentBalance == -2 && nodeBalance == 1) {
			rotateLeft(node);
			rotateRight(parentNode);
		} else if (parentBalance == 2 && nodeBalance == -1) {
			rotateRight(node);
			rotateLeft(parentNode);
		}
		rebalance(parentNode);
	}

	/**
	 * Rotate right operation.
	 * 
	 * @param node
	 *            Node that will be rotated.
	 */
	private void rotateRight(Node node) {

		Node tempLeftNode = node.getLeft();

		node.getLeft().setParent(node.getParent());
		node.setLeft(tempLeftNode.getRight());
		node.getLeft().setRight(node);

	}

	/**
	 * Rotate left operation.
	 * 
	 * @param node
	 *            Node that will be rotated.
	 */
	private void rotateLeft(Node node) {
		Node tempRightNode = node.getRight();

		node.getRight().setParent(node.getParent());
		node.setRight(tempRightNode.getLeft());
		node.getRight().setLeft(node);
	}

	/**
	 * Contains operation.
	 * 
	 * @param data
	 *            Data to insert.
	 *
	 * @return boolean true, if a node contains the String
	 */
	public boolean contains(String data) {
		return contains(root, data);
	}

	/**
	 * Recursive contains operation.
	 * 
	 * @param node
	 *            Next node to compare the data.
	 * 
	 * @param data
	 *            Data to compare.
	 * 
	 * @return boolean true, if a node contains the String
	 */
	private boolean contains(Node node, String data) {
		if (node == null) {
			return false;
		} else if (node.getData().equals(data)) {
			return true;
		} else if (data.compareTo(node.getData()) < 0) {
			return contains(node.getLeft(), data);
		} else {
			return contains(node.getRight(), data);
		}
	}
	
	public void print() { 
		printRec(this.root);
		}
	
	public void printRec(Node node){
		if(node.getData()!=null){
		System.out.println(node.getData());
		} else {
			return;
		}
		if(node.getLeft()!=null){
			printRec(node.getLeft());
			
		}
		if(node.getRight()!=null){
			printRec(node.getLeft());
			
		}
		
		return;
		
	}
}