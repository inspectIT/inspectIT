package info.novatec.inspectit.security;

import java.io.Serializable;

/**
 * Node of AVL Tree.
 * 
 * @author Clemens Geibel
 * @author Thomas Sachs
 */
public class Node implements Serializable {
	
	/**
	 * UID for Serialization. 
	 */
	private static final long serialVersionUID = 5773607507714396780L;

	/**
	 * ParentNode.
	 */
	private Node parent;
	
	/**
	 * Left Node.
	 */
	private Node right;
	
	/**
	 * Right Node.
	 */
	private Node left;
	
	/**
	 * Balance factor of the Node.
	 */
	private int balance;
	
	/**
	 * Data of the node.
	 */
	private String data;
	
	/**
	 * Default constructor without child nodes.
	 * 
	 * @param data
	 * 			Data of the node.
	 */
	public Node(String data, Node parent) {
		this.data = data;
		this.parent = parent;
		this.left = null;
		this.right = null;
		this.balance = 0;
	}
	
	
	public Node() {
		
	}
	
	/**
	 * Data getter.
	 * 
	 * @return 
	 * 		  Returns the data.
	 */
	public String getData() {
		return data;
	}
	
	/**
	 * Right Child getter.
	 * 
	 * @return 
	 * 		  Returns the right Child of Node.
	 */
	public Node getRight() {
		return right;
	}

	/**
	 * Right Child Setter.
	 * 
	 * @param right
	 * 		
	 */
	public void setRight(Node right) {
		this.right = right;
	}

	/**
	 * Left Child getter.
	 * 
	 * @return Returns the Left Child of Node.
	 */
	public Node getLeft() {
		return left;
	}

	/**
	 * Left Child Setter.
	 * 
	 * @param left
	 * 		
	 */
	public void setLeft(Node left) {
		this.left = left;
	}

	/**
	 * Parent of Node Getter.
	 * 
	 * @return 
	 * 		  Parent of Node
	 */
	public Node getParent() {
		return parent;
	}

	/**
	 * Parent of Node Setter
	 * 
	 * @param parent
	 * 
	 */
	public void setParent(Node parent) {
		this.parent = parent;
	}

	/**
	 * Node Balance Getter
	 * 
	 * @return
	 * 		  Balance of Node as Integer
	 */
	public int getBalance() {
		return balance;
	}

	/**
	 * Node Balance Setter
	 * 
	 * @param balance
	 * 		  
	 */
	public void setBalance(int balance) {
		this.balance = balance;
	}
	
}
