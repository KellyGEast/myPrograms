
/**
 * Filename:   Profile.java
 * Project:    p3
 * Authors:    Kelly East, setup from CS400 Canvas site
 *
 * Semester:   Fall 2018
 * Course:     CS400 Section 004
 * 
 * Due Date:   October 27, 2018; 11:59PM
 * Version:    1.0
 * 
 * Credits:    CS400 canvas files for initial setup of method
 * 
 * Bugs:       No known bugs
 */

import java.util.ArrayList;
import java.util.TreeMap;

public class Profile<K extends Comparable<K>, V> {
	
	//instance variables
	HashTable<K, V> hashtable;
	TreeMap<K, V> treemap;
	
	/** constructor for the profile class
	 */
	public Profile() {
		// Instantiate hashtable and treemap
		hashtable = new HashTable<K,V>();
		treemap = new TreeMap<K,V>();
	}
	
	/** insert the key and value pair into both the hashtable and treemap
	 *  for performance comparison 
	 */
	public void insert(K key, V value) {
		// Insert K, V into both hashtable and treemap
		hashtable.put(key, value);
		treemap.put(key, value);
	}
	
	/** retrieve the key and value pair from both the hashtable and treemap
	 *  for performance comparison 
	 */
	public void retrieve(K key) {
		// get value V for key K from both hashtable and treemap
		hashtable.get(key);
		treemap.get(key);
	}
	
	/** main method used to test this class
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Expected 1 argument: <num_elements>");
			System.exit(1);
		}
		int numElements = Integer.parseInt(args[0]);
		System.out.println(numElements);
		
		Profile<Integer, Integer> profile = new Profile<Integer, Integer>();

        for (int i = 0; i < numElements; i++) {
            profile.insert(i, i);    
        }
        
        for (int i = 0; i < numElements; i++) {
            profile.retrieve(i);    
        }
		
		String msg = String.format("Successfully inserted and retreived %d elements into the hash table and treemap", numElements);
		System.out.println(msg);
	}
}
