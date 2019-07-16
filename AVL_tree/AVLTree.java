
/**
 * Filename:   AVLTree.java
 * Project:    p2
 * Authors:    Debra Deppeler, Kelly East (kgeast@wisc.edu)
 *
 * Semester:   Fall 2018
 * Course:     CS400
 * Lecture:    004 (at Epic)
 * 
 * Due Date:   Before 10pm on September 24, 2018
 * Version:    1.0
 * 
 * Credits:    AVL visualizer to help validate that program is working as expected:
 *             https://www.cs.usfca.edu/~galles/visualization/AVLtree.html
 *             
 *             CS department AVL tree reference page:
 *             https://pages.cs.wisc.edu/~deppeler/cs400/readings/AVL-Trees/index.html
 *             
 *             Geeks for Geeks articles on AVL trees from class slides for background: 
 *             https://www.geeksforgeeks.org/avl-tree-set-1-insertion/
 *             https://www.geeksforgeeks.org/avl-tree-set-2-deletion/
 * 
 * Bugs:       no known bugs
 */

import java.lang.IllegalArgumentException;
import java.util.ArrayList;

/** This class utilizes the BSTNode class and implements the functionality of an
 * AVL tree. 
 * @param <K>
 */

public class AVLTree<K extends Comparable<K>> implements AVLTreeADT<K> {
	//instance variables
	private BSTNode<K> root; //the root or head of the AVL tree
	
	/**
	 * constructor for an empty AVL Tree
	 */
	public AVLTree() {
	} // creates an empty AVL Tree
	
	/** This class represents a tree node. Sets up the node with a constructor
	 * and getters and setters for all relevant variables. Tracks key, height,
	 * and the left and right children of the node.
	 * @param <K> 
	 */
	class BSTNode<K> {
		/* fields */
		private K key;	// A value to be inserted into the tree
		private int height;	// An integer representing the height of the node
		private BSTNode<K> left, right;	// Stores the left and right nodes
										// of the current node
		
		/**
		 * Constructor for a BST node.
		 * @param key
		 */
		BSTNode(K key) {
			// TODO: implement constructor
			this.key = key;
			//node initially has a height of one
			height = 1;
			left = null;
			right = null;
		}

		/* accessors */
		//get left, get right, getKey, getHeight
		public BSTNode<K> getLeft() {
			return left;
		}
		
		public BSTNode<K> getRight() {
			return right;
		}
		
		public K getKey() {
			return key;
		}
		
		public int getHeight() {
			return height;
		}
		
		/* mutators */
		//set left, set right, setKey, set Height
		public void setLeft(BSTNode<K> left) {
			this.left = left;
		}
		
		public void setRight(BSTNode<K> right) {
			this.right = right;
		}
		
		public void setHeight(int height) {
			this.height = height;
		}
		
		public void setKey(K key) {
			this.key = key;
		}
	}
	
	
	/**
	 * isEmpty returns true for an empty tree and false if a tree
	 * has been populated with one or more nodes.
	 * @return true if the tree is empty, otherwise false
	 */
	@Override
	public boolean isEmpty() {
		return (root == null);
	}

	/**
	 * inserts and instance of BSTNode<K> with the value given through 
	 * parameter key. The insert will conform to the search order of an AVL tree.
	 * This method throws and exception if the user tries to insert a duplicate key
	 * or an illegal argument. A private recursive helper method is also used here.
	 */
	@Override
	public void insert(K key) throws DuplicateKeyException, IllegalArgumentException {
		root = insertRecursive(root, key);
    }
    
	/**
	 * helper method for insert(K key)
	 */
    private BSTNode<K> insertRecursive(BSTNode<K> current, K key) throws DuplicateKeyException	{
    	
    	//--------------------------------------------------------------
    	//Perform initial BST insertion
    	//--------------------------------------------------------------
    	
    	//handle case where there is no root yet (empty tree)
    	if (current == null) {
    		current = new BSTNode<K>(key);
    		return current;
    	}
    	
    	//handle case where we attempt to add a duplicate
    	else if (current.getKey().equals(key)) {
    		String warning = "WARNING: failed to insert duplicate key: " + key + ".";
    		throw new DuplicateKeyException(warning);
    	}
    	
    	//handle less than case (root less than target)
    	else if (current.getKey().compareTo(key) < 0) {
    		//we should call our recursive method on current
    		current.setRight(insertRecursive(current.getRight(), key));
    		} //end else if
    	
    	//handle case where root is greater than target
    	else {
    		current.setLeft(insertRecursive(current.getLeft(), key));
    	} //end else
    	
    	//--------------------------------------------------------------
    	//Update the height
    	//--------------------------------------------------------------
    	
    	current.setHeight(updateHeight(current));
    	
    	//--------------------------------------------------------------
    	//Get the balance factor for the current node, then check for balance
    	//--------------------------------------------------------------
    	
		int balance = getBalance(current); 
		
		// Check if node is unbalanced, and pursue the right case for rotation 
		// Left Left Case 
		if (balance > 1 && key.compareTo(current.getLeft().getKey()) < 0) {
		    return rotateRight(current); 
		}
		
		// Right Right Case 
		if (balance < -1 && key.compareTo(current.getRight().getKey()) > 0) {
		    return rotateLeft(current); 
		}
		
		// Left Right Case 
		if (balance > 1 && key.compareTo(current.getLeft().getKey()) > 0) { 
		    current.setLeft(rotateLeft(current.getLeft()));
			return rotateRight(current); 
		} 
		
		// Right Left Case 
		if (balance < -1 && key.compareTo(current.getRight().getKey()) < 0) { 
		    current.setRight(rotateRight(current.getRight()));
			return rotateLeft(current); 
		} 
		
		/* return the node pointer (unchanged) */    	
    	return current;
    	
    } //end insertRecursive
    
    /** Rotates node current to the left, making its right child into its parent.
     * @param current; the former parent
     * @return the new parent (formerly a's right child)
     */
    public BSTNode<K> rotateLeft(BSTNode<K> current) {
        BSTNode<K> newParent = current.getRight();
        current.setRight(newParent.getLeft());
        newParent.setLeft(current);
        current.setHeight(updateHeight(current));
        newParent.setHeight(updateHeight(current));
        return newParent;
    }
    
    /** Rotates node current to the right, making its left child into its parent.
     * @param current; the former parent
     * @return the new parent (formerly current's left child)
     */
    public BSTNode<K> rotateRight(BSTNode<K> current) {
        BSTNode<K> newParent = current.getLeft();
        current.setLeft(newParent.getRight());
        newParent.setRight(current);
        current.setHeight(updateHeight(current));
        newParent.setHeight(updateHeight(current));
        return newParent;
    }
    
    /** Get balance factor of the current node
     * @param current; the node being evaluated
     * @return balance factor (integer)
     */ 
    public int getBalance(BSTNode<K> current) { 
        if (current == null) 
            return 0; 
        
        //start with height of 0 - helps handle case where there 
        //is no left and/or right child
        int leftHeight = 0;
        int rightHeight = 0;
        
        //update left height if left child does exist
        if (current.getLeft() != null) {
        	leftHeight = current.getLeft().getHeight();
        }
        
        //update the right height if right child does exist
        if (current.getRight() != null) {
        	rightHeight = current.getRight().getHeight();
        }
        
        //return the balance factor (left height - right height)
        return leftHeight - rightHeight; 
    } 
    
    /** Returns the new height for a given node.
     * Called during rotation and insertion/deletion
     * @param current; the node being evaluated
     * @return new height (integer)
     */ 
    public int updateHeight(BSTNode<K> current) {
    	//case where there are no children
    	if (current.getLeft() == null && current.getRight() == null) {
    		return 1;
    	}
    	
    	//case where left and right child both exist
    	//height of node is the max height between left and right + 1
    	if (current.getLeft() != null && current.getRight() != null) {
        	return (Math.max(current.getLeft().getHeight(), 
        			current.getRight().getHeight()) + 1);
    	}
    	
    	//if only a right child exists, height of node is right height + 1
    	else if (current.getLeft() == null) {
    		return (current.getRight().getHeight() + 1);
    	}
    	
    	//if we get there, there is only a left child and 
    	//the height is left height + 1
    	else {
    		return (current.getLeft().getHeight() + 1);
    	}
    }

	/**
	 * This method finds the node with the key matching the passed in parameter
	 * and removes the node from the tree. This method will then re-balance the tree
	 * to maintain the AVL tree structure. This method uses a recursive helper method.
	 * @param K key - they key of the node to be deleted
	 */
	@Override
	public void delete(K key) throws IllegalArgumentException {
		root = deleteRecursive(root, key);
	}
		
	/**
	 * helper method for delete(K key) 
	 * also calls a helper method removeAndPromote
	 * @param BSTNode<K> current - current node being evaluated
	 * @param K key - they key of the node to be deleted
	 * @return the node to be deleted
	 */
	private BSTNode<K> deleteRecursive(BSTNode<K> current, K key) throws IllegalArgumentException {
		
		//--------------------------------------------------------------
    	//Perform initial BST deletion
    	//--------------------------------------------------------------
		
		if (current == null) {
			return null; // tree is unchanged
		} 
		else if (key.compareTo(current.getKey()) == 0){
			//case where node matches key for deletion
			current = removeAndPromote(current);
		} 
		else if (key.compareTo(current.getKey()) < 0) {
			//case where key is less than node key, go left
			current.setLeft(deleteRecursive(current.getLeft(), key));
		}    
		else {
			//case where key is greater than node key, go right
			current.setRight(deleteRecursive(current.getRight(), key));
		} 
		
		if (current == null) {
			return current;
		}
  
        //--------------------------------------------------------------
    	//Update height
    	//-------------------------------------------------------------- 
        current.setHeight(updateHeight(current)); 
  
        //--------------------------------------------------------------
    	//Get the balance factor for the current node and check if it is unbalanced
    	//--------------------------------------------------------------
        int balance = getBalance(current); 
  
        // Check if node is unbalanced and pursue the appropriate case to re-balance 
        // Left Left Case 
        if (balance > 1 && getBalance(current.getLeft()) >= 0) {
            return rotateRight(current); 
        }
  
        // Left Right Case 
        if (balance > 1 && getBalance(current.getLeft()) < 0) { 
            current.setLeft(rotateLeft(current.getLeft())); 
            return rotateRight(current); 
        } 
  
        // Right Right Case 
        if (balance < -1 && getBalance(current.getRight()) <= 0) {
            return rotateLeft(current); 
        }
  
        // Right Left Case 
        if (balance < -1 && getBalance(current.getRight()) > 0) { 
            current.setRight(rotateRight(current.getRight())); 
            return rotateLeft(current); 
        } 
  
        return current; 
	}
	
	/**
	 * returns the smallest node in this tree
	 * @param BSTNode<K> current - the current node being evaluated, starting node for search
	 * @return the smallest node
	 */
	public BSTNode<K> smallestInTree(BSTNode<K> current){
		while (current.getLeft() != null) {
			current = current.getLeft();
		}
		return current;
	}

	/**
	 * removes the current Node and promotes the smallest node in the right subtree
	 * calls a helper method smallestInTree
	 * also calls deleteRecurisve to remove the promoted node
	 * @param current the node to remove
	 * @return the updated promoted node
	 */
	private BSTNode<K> removeAndPromote(BSTNode<K> current) {
		// If both children are null, return null
		if ((current.getLeft() == null) && (current.getRight() == null)) {
			return null;
		}
		
		// If the left child is null, return the right child
		if (current.getLeft() == null) {
			return current.getRight();
		}
		
		// If the right child is null, return the left child
		if (current.getRight() == null) {
			return current.getLeft();
		}

		// If current has 2 children, find the left-most value in the right subtree
		BSTNode<K> smallestRight = smallestInTree(current.getRight());  

		// promote the smallestRight by setting the key of current 
		current.setKey(smallestRight.getKey());
		
		// remove the smallestRight from the right subtree
		current.setRight(deleteRecursive(current.getRight(), smallestRight.getKey()));
		
		// return the updated promoted node
		return current;

	}// removeAndPromote
	
	/**
	 * A search method that is used to look-up a BSTNode with the given key in the
	 * current AVL tree and returns true related node if present. Otherwise, return false to
	 * indicate that no node was found for that key. A private, recursive helper method 
	 * is used by this method.
	 * @param key - the value being searched for
	 * @return true if the key is found in the tree, otherwise false
	 */
	@Override
	public boolean search(K key) throws IllegalArgumentException {
		return (searchRecursive(root, key));
		//searchRecursive will return true if the key is found, 
		//false if no node was found matching the key
	}
	
	/**
	 * Helper method for search(K key)
	 * @param current- the current node being evaluated; key - the value being searched for
	 * @return true if the key is found in the tree, otherwise false
	 */
	private boolean searchRecursive(BSTNode<K> current, K key) {
				
		if (current == null) {
				//System.out.println("Reached the null case");
				return false;
			}
			 
		//evaluate the current node to see if it is a match
	    else if (current.getKey().equals(key)) {
	    	//System.out.println("Recursive = for key: " + key + " we return the equals case. Current was: " + current.getKey());
	    	return true;
	   	}
			
	   	//less than case (root less than key)
	    else if (current.getKey().compareTo(key) < 0) {
	    	//call our recursive method on current
	    	//System.out.println("Recursive = for key: " + key + " we return the less than case. Current was: " + current.getKey());
	    	return searchRecursive(current.getRight(), key);
	    	} //end if
	   	
	    //case where root is greater than key
	    else {
	    	//System.out.println("Recursive = for key: " + key + " we return the greater than case. Current was: " + current.getKey());
	    	//call our recursive method on current
	    	return searchRecursive(current.getLeft(), key);
	    } //end else
	    	
	} 

	/**
	 * Performs in-order traversal of AVL Tree, and builds a string of the keys
	 * @return a String with all the keys, in order, with exactly one space between keys
	 */
	@Override
	public String print() {
		//create ArrayList to collect the keys in order
		ArrayList<K> result = new ArrayList<K>();
		result = printRecursive(root, result);
		
		String inorderString = "";
		
		//Loop through the result array and build the in order string
		for (int i=0; i < result.size(); i++) {
			inorderString += result.get(i) + " ";
		}
		
		//use trim to remove the trailing space when returning
    	return inorderString.trim();
    }
    
	/**
	 * Helper method for print()
	 * Performs in-order traversal of AVL Tree, and builds an ArrayList of the keys
	 * @param current (node passed from main print method), result (the ArrayList of keys)
	 * @return a String with all the keys, in order, with exactly one space between keys
	 */
    private ArrayList<K> printRecursive(BSTNode<K> current, ArrayList<K> result){
    	
    	if (current == null) {
			return result;
		}
		
    	//traverse left tree
		printRecursive(current.getLeft(), result);
		//handle the root
		result.add(current.getKey());
		//traverse right tree
		printRecursive(current.getRight(), result);
		
		return result;
    }

	/**
	 * This method traverses the tree in order to check for a balanced tree. 
	 * If the balance factor for each node is -1, 0, or 1, the tree is balanced.
	 * This method uses a recursive helper method.
	 * @return true for a balanced tree, otherwise false
	 */
	@Override
	public boolean checkForBalancedTree() {
		return checkForBalancedTreeRecursive(root);
	}
	
	/**
	 * helper method for checkForBalancedTree()
	 * @param BSTNode<K> current - the current node being evaluated
	 * @return true for a balanced tree, otherwise false
	 */
	private boolean checkForBalancedTreeRecursive(BSTNode<K> current) {
		if (current == null) {
			return true;
		}
		
    	//traverse left tree
		checkForBalancedTreeRecursive(current.getLeft());
		//handle the root
		//System.out.println("Balance factor for node " + current.getKey() + " is " + getBalance(current));
		if (Math.abs(getBalance(current)) > 1){
			return false;
		}
		//traverse right tree
		checkForBalancedTreeRecursive(current.getRight());
		
		return true;
	}
	
	/**
	 * Checks for Binary Search Tree by confirming that the left side of the tree 
	 * is less than the root and the right side of the tree is greater than the root.
	 * Uses a recursive helper method.
	 * @return true if AVL tree is binary search tree.
	 */
	@Override
	public boolean checkForBinarySearchTree() {
		return checkForBinarySearchTreeRecursive(root);
	}
	
	/**
	 * Helper method for checkForBinarySearchTree. Checks for Binary Search Tree 
	 * by confirming that the left side of the tree is less than the root and 
	 * the right side of the tree is greater than the root. 
	 * @return true if AVL tree is binary search tree.
	 */
	public boolean checkForBinarySearchTreeRecursive(BSTNode<K> current) {
		if (current == null) {
			return true;
		}
		
    	//traverse left tree
		checkForBinarySearchTreeRecursive(current.getLeft());
		
		//handle the root
		//tests that the left side of the tree is less than the root
		if (current.getLeft() != null) {
			if (current.getLeft().getKey().compareTo(current.getKey()) > 0) {
				return false;
			}
		}
		//tests that the right side of the tree is greater than the root
		if (current.getRight() != null) {
			if (current.getRight().getKey().compareTo(current.getKey()) < 0){
				return false;
			}
		}
		//traverse right tree
		checkForBinarySearchTreeRecursive(current.getRight());
		
		return true;
	}

	// prints a tree diagram sideways on your screen
	// source:  Building Java Programs, 4th Ed., by Reges and Stepp, Ch 17
	private void printSideways() {
		System.out.println("-------------------------------------------------------");
		recursivePrintSideways(root, "");
		System.out.println("-------------------------------------------------------");

		}

	private void recursivePrintSideways(BSTNode<K> current, String indent) {
		if (current != null) {
			recursivePrintSideways(current.getRight(), indent + "    ");
			System.out.println(indent + current.getKey());
			recursivePrintSideways(current.getLeft(), indent + "    ");
		}
	}

	
	public static void main(String [] args) throws IllegalArgumentException, DuplicateKeyException {
		System.out.println("-------------------------------------------------------");
		System.out.println("-------------------------------------------------------");
		System.out.println("First round of testing - AVL tree of strings");
		System.out.println("-------------------------------------------------------");
		System.out.println("-------------------------------------------------------");
		System.out.println();
		
		AVLTree<String> AVL1 = new AVLTree<String>();
		
		System.out.println("-------------------------------------------------------");
		System.out.println("Test isEmpty on new Tree: ");
		System.out.println("-------------------------------------------------------");
		System.out.println("Expected result: true");
		System.out.println("Returned by program: " + AVL1.isEmpty());
		if (AVL1.isEmpty() == true) {
			System.out.println("Check for isEmpty passed.");
			}
		else {System.out.println("Check for isEmpty failed. "
				+ "Output did not match expected");}
		
		System.out.println("-------------------------------------------------------");
		System.out.println("Test insert: ");
		System.out.println("Order inserted: a > b > c > k > f > z > i > j > y > l");
		AVL1.insert("a");
		AVL1.insert("b");
		AVL1.insert("c");
		AVL1.insert("k");
		AVL1.insert("f");
		AVL1.insert("z");
		AVL1.insert("i");
		AVL1.insert("j");
		AVL1.insert("y");
		AVL1.insert("l");
		AVL1.printSideways();
		
		System.out.println("Test balanced tree: ");
		System.out.println("-------------------------------------------------------");
		System.out.println("Expected result: true");
		boolean balancedExpected = true;
		System.out.println("Returned by program: "+ AVL1.checkForBalancedTree());
		if (balancedExpected == AVL1.checkForBalancedTree()) {
			System.out.println("Check for balanced tree test passed.");
			System.out.println("This indicates insertion was done correctly");
			}
		else {System.out.println("Check for balanced tree test failed. "
				+ "Output did not match expected");}
		
		System.out.println("-------------------------------------------------------");
		System.out.println("Test binary search tree: ");
		System.out.println("-------------------------------------------------------");
		System.out.println("Expected result: true");
		boolean binaryExpected = true;
		System.out.println("Returned by program: "+ AVL1.checkForBinarySearchTree());
		if (binaryExpected == AVL1.checkForBinarySearchTree()) {
			System.out.println("Check for binary search tree test passed.");
			}
		else {System.out.println("Check for binary search tree test failed. "
				+ "Output did not match expected");}
		
		System.out.println("-------------------------------------------------------");
		System.out.println("Test print in order: ");
		System.out.println("-------------------------------------------------------");
		System.out.println("Expected: ");
		String printExpected = "a b c f i j k l y z";
		System.out.println("a b c f i j k l y z");
		System.out.println("Returned by program: ");
		System.out.println(AVL1.print());
		if (printExpected.equals(AVL1.print())) {System.out.println("Print in order test passed.");}
		else {System.out.println("Print in order test failed. Output did not match expected");}
		
		System.out.println("-------------------------------------------------------");
		System.out.println("Test deletion: ");
		System.out.println("Order removed: f > i > b");
		AVL1.delete("f");
		AVL1.delete("i");
		AVL1.delete("b");
		AVL1.printSideways();
		
		System.out.println("Test balanced tree post-deletion: ");
		System.out.println("-------------------------------------------------------");
		System.out.println("Expected result: true");
		boolean balancedExpected2 = true;
		System.out.println("Returned by program: "+ AVL1.checkForBalancedTree());
		if (balancedExpected2 == AVL1.checkForBalancedTree()) {
			System.out.println("Check for balanced tree test passed.");
			System.out.println("This indicates deletion was done correctly");
			}
		else {System.out.println("Check for balanced tree test failed. "
				+ "Output did not match expected");}
		
		System.out.println("-------------------------------------------------------");
		System.out.println("Test print in order post-deletion: ");
		System.out.println("-------------------------------------------------------");
		System.out.println("Expected: ");
		String printExpected2 = "a c j k l y z";
		System.out.println("a c j k l y z");
		System.out.println("Returned by program: ");
		System.out.println(AVL1.print());
		if (printExpected2.equals(AVL1.print())) {System.out.println("Print in order test passed.");}
		else {System.out.println("Print in order test failed. Output did not match expected");}
		
		System.out.println("-------------------------------------------------------");
		System.out.println("Test search: ");
		System.out.println("-------------------------------------------------------");
		System.out.println("Searching AVL tree for key l"); 
		System.out.println("Expected result: true");
		boolean searchExpected = true;
		System.out.println("Returned by program: "+ AVL1.search("l"));
		if (searchExpected == AVL1.search("l")) {System.out.println("Search test 1 (l) passed.");}
		else {System.out.println("Search test 1 (l) failed. Output did not match expected");}
		
		System.out.println();
		System.out.println("Searching AVL tree for key P"); 
		System.out.println("Expected result: false");
		boolean searchExpected2 = false;
		System.out.println("Returned by program: "+ AVL1.search("P"));
		if (searchExpected2 == AVL1.search("P")) {System.out.println("Search test 2 (P) passed.");}
		else {System.out.println("Search test 2 (P) failed. Output did not match expected");}
		
		System.out.println("-------------------------------------------------------");
		System.out.println("Test isEmpty on populated Tree: ");
		System.out.println("-------------------------------------------------------");
		System.out.println("Expected result: false");
		System.out.println("Returned by program: " + AVL1.isEmpty());
		if (AVL1.isEmpty() == false) {
			System.out.println("Check for isEmpty passed.");
			}
		else {System.out.println("Check for isEmpty failed. "
				+ "Output did not match expected");}
		
		System.out.println();
		System.out.println("-------------------------------------------------------");
		System.out.println("-------------------------------------------------------");
		System.out.println("Second round of testing - AVL tree of integers");
		System.out.println("-------------------------------------------------------");
		System.out.println("-------------------------------------------------------");
		System.out.println();
		
		AVLTree<Integer> AVL2 = new AVLTree<Integer>();
		
		System.out.println("-------------------------------------------------------");
		System.out.println("Test isEmpty on new Tree: ");
		System.out.println("-------------------------------------------------------");
		System.out.println("Expected result: true");
		System.out.println("Returned by program: " + AVL2.isEmpty());
		if (AVL2.isEmpty() == true) {
			System.out.println("Check for isEmpty passed.");
			}
		else {System.out.println("Check for isEmpty failed. "
				+ "Output did not match expected");}
		
		System.out.println("-------------------------------------------------------");
		System.out.println("Test insert: ");
		System.out.println("Order inserted: 10 > 9 > 8 > 7 > 6 > 5 > 4 > 3 > 2 > 1");
		AVL2.insert(10);
		AVL2.insert(9);
		AVL2.insert(8);
		AVL2.insert(7);
		AVL2.insert(6);
		AVL2.insert(5);
		AVL2.insert(4);
		AVL2.insert(3);
		AVL2.insert(2);
		AVL2.insert(1);
		AVL2.printSideways();
		
		System.out.println("Test balanced tree: ");
		System.out.println("-------------------------------------------------------");
		System.out.println("Expected result: true");
		boolean balancedExpected3 = true;
		System.out.println("Returned by program: "+ AVL2.checkForBalancedTree());
		if (balancedExpected3 == AVL2.checkForBalancedTree()) {
			System.out.println("Check for balanced tree test passed.");
			System.out.println("This indicates insertion was done correctly");
			}
		else {System.out.println("Check for balanced tree test failed. "
				+ "Output did not match expected");}
		
		System.out.println("-------------------------------------------------------");
		System.out.println("Test binary search tree: ");
		System.out.println("-------------------------------------------------------");
		System.out.println("Expected result: true");
		boolean binaryExpected2 = true;
		System.out.println("Returned by program: "+ AVL2.checkForBinarySearchTree());
		if (binaryExpected2 == AVL2.checkForBinarySearchTree()) {
			System.out.println("Check for binary search tree test passed.");
			}
		else {System.out.println("Check for binary search tree test failed. "
				+ "Output did not match expected");}
		
		System.out.println("-------------------------------------------------------");
		System.out.println("Test print in order: ");
		System.out.println("-------------------------------------------------------");
		System.out.println("Expected: ");
		String printExpected3 = "1 2 3 4 5 6 7 8 9 10";
		System.out.println("1 2 3 4 5 6 7 8 9 10");
		System.out.println("Returned by program: ");
		System.out.println(AVL2.print());
		if (printExpected3.equals(AVL2.print())) {System.out.println("Print in order test passed.");}
		else {System.out.println("Print in order test failed. Output did not match expected");}
		
		System.out.println("-------------------------------------------------------");
		System.out.println("Test deletion: ");
		System.out.println("Order removed: 5 > 6 > 9");
		AVL2.delete(5);
		AVL2.delete(6);
		AVL2.delete(9);
		AVL2.printSideways();
		
		System.out.println("Test balanced tree post-deletion: ");
		System.out.println("-------------------------------------------------------");
		System.out.println("Expected result: true");
		boolean balancedExpected4 = true;
		System.out.println("Returned by program: "+ AVL2.checkForBalancedTree());
		if (balancedExpected4 == AVL2.checkForBalancedTree()) {
			System.out.println("Check for balanced tree test passed.");
			System.out.println("This indicates deletion was done correctly");
			}
		else {System.out.println("Check for balanced tree test failed. "
				+ "Output did not match expected");}
		
		System.out.println("-------------------------------------------------------");
		System.out.println("Test print in order post-deletion: ");
		System.out.println("-------------------------------------------------------");
		System.out.println("Expected: ");
		String printExpected4 = "1 2 3 4 7 8 10";
		System.out.println("1 2 3 4 7 8 10");
		System.out.println("Returned by program: ");
		System.out.println(AVL2.print());
		if (printExpected4.equals(AVL2.print())) {System.out.println("Print in order test passed.");}
		else {System.out.println("Print in order test failed. Output did not match expected");}
		
		System.out.println("-------------------------------------------------------");
		System.out.println("Test search: ");
		System.out.println("-------------------------------------------------------");
		System.out.println("Searching AVL tree for key 3"); 
		System.out.println("Expected result: true");
		boolean searchExpected3 = true;
		System.out.println("Returned by program: "+ AVL2.search(3));
		if (searchExpected3 == AVL2.search(3)) {System.out.println("Search test 1 (3) passed.");}
		else {System.out.println("Search test 1 (3) failed. Output did not match expected");}
		
		System.out.println();
		System.out.println("Searching AVL tree for key P"); 
		System.out.println("Expected result: false");
		boolean searchExpected4 = false;
		System.out.println("Returned by program: "+ AVL2.search(15));
		if (searchExpected4 == AVL2.search(15)) {System.out.println("Search test 2 (15) passed.");}
		else {System.out.println("Search test 2 (15) failed. Output did not match expected");}
		
		System.out.println("-------------------------------------------------------");
		System.out.println("Test isEmpty on populated Tree: ");
		System.out.println("-------------------------------------------------------");
		System.out.println("Expected result: false");
		System.out.println("Returned by program: " + AVL2.isEmpty());
		if (AVL2.isEmpty() == false) {
			System.out.println("Check for isEmpty passed.");
			}
		else {System.out.println("Check for isEmpty failed. "
				+ "Output did not match expected");}
	} //end main
}