/**
 * Filename:   HashTable.java
 * Project:    p3
 * Authors:    Kelly East, setup from CS400 Canvas site
 *
 * Semester:   Fall 2018
 * Course:     CS400 Section 004
 * 
 * Due Date:   October 27, 2018; 11:59PM
 * Version:    1.0
 * 
 * Credits:    CS400 lecture notes
 * 			   Hash table reference site: https://pages.cs.wisc.edu/~deppeler/cs400/readings/Hashing/
 * 			   Stack Overflow for help with use of args string array in a main method and 
 * 			   ideas for finding the next prime
 * 
 * Bugs:       No known bugs
 */


import java.util.ArrayList;
import java.util.NoSuchElementException;


// This is a hash table program designed to store key and value pairs in a hash table
// structure. This table utilizes buckets to handle collisions. The buckets are stored as 
// an array list, and within each bucket, a list framework is used with a head node that 
// has a variable next, pointing to the next node.
// 
// This program also uses the java hashCode function for determining the hash index.
// The private method "getIndex" holds the hashing algorithm. It works as follows:
// 		1) a hash code is generate for the key using the built in java function
//  		2) an index is determined by taking the modulo of that hashCode with the 
//		   number of buckets at the given time
//		3) to eliminate any negatives, the absolute value of that index is returned
//		   as the final index
  
public class HashTable<K extends Comparable<K>, V> implements HashTableADT<K, V> {
	
	class Node<K,V>{
		K key; 			//the location where the value should be stored
		V value; 		//value to be stored
		Node<K,V> next; //pointer to the next node
		
		//constructor
		public Node(K key, V value){
			this.key = key;
			this.value = value;
		}
	}
	
	//instance variables for HashTable
	private ArrayList<Node<K,V>> buckets;    //store the chain of buckets
	private int numBuckets; 					//number of buckets in ArrayList
	private int size;						//size of ArrayList
	private double loadFactor;			    //used to determine when to resize
	
	//no-arg constructor
	public HashTable() {
		buckets = new ArrayList<>();
		numBuckets = 11; //start with a prime number of buckets for best performance
		size = 0;
		loadFactor = 0.75; //ideal load factor is between 0.7 and 0.8
		
		//we need to setup our Array to be empty at the start
		for (int i = 0; i < numBuckets; i++) {
			buckets.add(null);
		}
	}
	
	//Constructor that accepts initial capacity and load factor
	public HashTable(int initialCapacity, double loadFactor) {
			numBuckets = initialCapacity;
			this.loadFactor = loadFactor;
			buckets = new ArrayList<>();
			size = 0;
			
			//we need to setup our Array to be empty at the start
			for (int i = 0; i < numBuckets; i++) {
				buckets.add(null);
			}
	}

	/** insert a <key,value> pair entry into the hash table 
	 * if the key already exists in the table, 
	 * replace existing value for that key with the 
	 * value specified in this call to put.
	 * 
	 * permits null values but not null keys and permits the same value 
	 * to be paired with different key
	 * 
	 * throw IllegalArgumentException when key is null
	 */
	@Override
	public void put(K key, V value) throws IllegalArgumentException {
		
		//handle null keys
		if (key == null) {
			throw new IllegalArgumentException("Cannot insert a null key");
		}
		
		//
		//find the bucket for this key/value pair
		//
		
		int hashIndex = getIndex(key);
		
		//return the current list of nodes in this bucket
		Node<K,V> head = buckets.get(hashIndex);
		
		//check to see that this key does not already exist, in which case
		//we just want to update the value
		while (head != null) {
			if (head.key.equals(key)){
				//if we get here, the key already exists, we should
				//update the value to the value in this key/value pairing
				head.value = value;
				return;
			}
			head = head.next;
		}
		
		//
		//add the new key/value pair
		//
		
		//reset head to the start of the list housed in this bucket
		head = buckets.get(hashIndex);
		//create our new node to add
		Node<K,V> newNode = new Node<K,V>(key, value);
		//add the node to the front of the list at this bucket
		newNode.next = head;
		//update this bucket to house the updated list
		buckets.set(hashIndex, newNode); 
		//update ArrayList size
		size++; 
		
		//
		//check the load factor to make sure we do not need to update table size
		//
		double sizeAsDouble = 1.0*size; //convert to a double to work with loadFactor
		
		if (sizeAsDouble / numBuckets >= loadFactor) {
			//we need to update our array/bucket size
			ArrayList<Node<K,V>> temp = buckets; //house old array
			buckets = new ArrayList<>(); //create new bucket array
			numBuckets = nextPrime(numBuckets*2); //update the number of buckets
			size = 0; //size of the new array is zero at this point
			//setup our new array to be empty at the start
			for (int i = 0; i < numBuckets; i++) {
				buckets.add(null);
			}
			
			//transfer the nodes
			for (int i = 0; i < temp.size(); i++) {
				Node<K,V> head2 = temp.get(i);
				while (head2 != null) {
					put(head2.key, head2.value);
					head2 = head2.next;
				} //end while
			} //end for
				
		} //end loadFactor section
		
		
		
	}
	
	/** return the value associated with the given key.
			 * throw IllegalArgumentException if key is null 
			 * throw NoSuchElementException if key does not exist 
			 */
	@Override
	public V get(K key) throws IllegalArgumentException, NoSuchElementException {
		//handle null keys
		if (key == null) {
			throw new IllegalArgumentException("Cannot insert a null key");
		}
		
		//begin search for key at the start of the correct bucket
		int hashIndex = getIndex(key); //get the hashIndex to find the right bucket
		
		//return the current list of nodes in this bucket
		Node<K,V> head = buckets.get(hashIndex);
		
		while (head != null) {
			if (head.key.equals(key)){
				//the key exists, return the value
				return head.value;
			}
			head = head.next;
		}
		
		//if we get here, the key did not exist. Error and return message
		throw new NoSuchElementException("The key does not exist in this hash table");

	}
	
	/** remove the (key,value) entry for the specified key
	 * throw IllegalArgumentException if key is null 
	 * throw NoSuchElementException if key does not exist in the tree 
	 */
	@Override
	public void remove(K key) throws IllegalArgumentException, NoSuchElementException {
		//handle null keys
		if (key == null) {
			throw new IllegalArgumentException("Cannot insert a null key");
		}
		
		//begin search for key at the start of the correct bucket
		int hashIndex = getIndex(key);
		
		//return the current list of nodes in this bucket
		Node<K,V> head = buckets.get(hashIndex);
		
		//handle the case where the head is our key and we just need to update the head
		if (head.key.equals(key)) {

			if (head.next != null) {
				//update the head to the next value
				head = head.next;
				//update bucket to point to new list
				buckets.set(hashIndex, head);
				size --; //decrease size, key was removed
				return;
			}
			else {
				//this is the case where the key we are removing was the only
				//value in the bucket. Set list in this bucket to null
				head = null;
				buckets.set(hashIndex, head);
				size --;
				return;
			}
		}
		
		//handle all other cases
		while (head != null) {
			if (head.next.key.equals(key)){
				//update head.next to point to the value after the key
				if (head.next.next != null) {
					head.next = head.next.next;
					size--;
					return;
				}
				else {
					head.next = null;
					size--;
					return;
				}
			}
			head = head.next;
		}
		
		//if we get here, the key did not exist. Throw an error and return a message.
		throw new NoSuchElementException("The key does not exist in this hash table");
		
	}
	
	/** @return  the number of keys in the hash table */
	@Override
	public int size() {
		return size;
	}
	
	//
	// Start of helper methods
	//
	
	/** Hashing algorithm: use the java hashCode function on the key,
	 *  then take the modulo of this value with the number of buckets and 
	 *  return the positive version of this value
	 * @return the hash index */
	private int getIndex(K key) {
		int hashCode = key.hashCode();
		int hashIndex = hashCode % numBuckets;
		return Math.abs(hashIndex);
	}
	
	/** Takes in an int and returns the next prime. Uses a helper method isPrime
	 * @return the next prime number as an int */
	private static int nextPrime(int inputValue) {
		if (isPrime(inputValue)) {
			//if this is already prime, return as is
			return inputValue;
		}
		else {
			//recursive method to find next prime, incrementing by one
			return nextPrime(inputValue+1);
		}
	}

	/** method to determine if a number is prime or not
	 * @return true if prime, else false  */
	private static boolean isPrime(int inputValue) {
		for (int i = 2; i < inputValue/2; i++) {
			//try dividing the input value by everything between 2 and half it's value
			//if any succeed (meaning inputValue % i == 0) the value is not prime
			if (inputValue % i == 0) {
				return false;
			}
		}
		return true;
	}
	
	/** Prints out the array of buckets and a comma delimited 
	 * list of the values in each bucket
	 * @return  nothing. prints to screen*/
	private void printBuckets() {
		if (buckets.size() == 0) {
			return;
		}
		
		for (int i = 0; i < buckets.size(); i++) {
			Node<K,V> head = buckets.get(i);
			System.out.print("Bucket at index " + i + ": ");
			while (head != null) {
				System.out.print(head.value + " , ");
				head = head.next;
			} //end while
			System.out.println();
		} //end for
		
	}
	
	//This is just used for testing
	public static void main(String args[]) {
		//System.out.println("Testing nextPrime");
		//System.out.println("Using input of 16");
		//System.out.println(nextPrime(16));
		
		HashTable<String, Integer> map = new HashTable<>(3, 0.75); 
        map.put("this",1 ); 
        map.put("coder",2 ); 
        map.put("this",4 ); 
        map.put("hi",5 ); 
        map.put("kelly",28 ); 
        map.put("lauren",24 ); 
        map.put("matt",29 ); 
        map.put("chewie",3 ); 
        map.put("twenty",20 ); 
        map.put("thirty",30 ); 
        map.put("forty",40 ); 
        map.put("fifty",50 ); 
        map.put("sixty",60 ); 
        map.put("seventy",70 ); 
        map.put("eighty",80 ); 
        map.put("ninety",90 ); 
        map.put("ten",10 ); 
        map.put("eleven",11 ); 
        map.put("twelve",12 ); 
        map.put("thirteen",13 ); 
        map.put("fourteen",14 ); 
        map.put("fifteem",15 ); 
        map.put("sixteen",16 ); 
        map.put("seventeen",17 ); 
        System.out.println("Hash table size = " + map.size()); 
        System.out.println(map.get("this"));
        map.printBuckets();
        System.out.println();
        System.out.println("--------------------------------------------");
        System.out.println("Testing remove:");
        System.out.println("--------------------------------------------");
        //map.remove("this"); 
        map.remove("matt");
        map.remove("twelve");
        map.remove("seventeen");
        System.out.println("Hash table size = " + map.size()); 
        map.printBuckets();
        //System.out.println(map.remove("this")); 
        //System.out.println(map.size()); 
        //System.out.println(map.isEmpty()); 
        
        HashTable<Integer, Integer>map2 = new HashTable<>(3, 0.75); 
        map2.put(0,0); 
        map2.put(-1,2 ); 
        map2.put(8000,4 ); 
        map2.put(5,5 ); 
        map2.put(6,28 ); 
        map2.put(7,24 ); 
        map2.put(8,29 ); 
        map2.put(-5,3 ); 
        System.out.println("Hash table size = " + map2.size()); 
        System.out.println(map2.get(-1));
        System.out.println(map2.get(0));
        map2.printBuckets();
        System.out.println();
        System.out.println("--------------------------------------------");
        System.out.println("Testing remove:");
        System.out.println("--------------------------------------------");
        //map.remove("this"); 
        map2.remove(0);
        map2.remove(-1);
        map2.remove(8000);
        System.out.println("Hash table size = " + map2.size()); 
        map2.printBuckets();
	}
		
}
