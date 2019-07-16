import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Filename:   GraphImpl.java
 * Project:    p4
 * Course:     cs400 
 * Authors:    Kelly East, CS400 instructors
 * Due Date:   11/17/2018
 * 
 * T is the label of a vertex, and List<T> is a list of
 * adjacent vertices for that vertex.
 *
 * Additional credits: Geeks for Geeks documentation on JSON parsing
 * 					   Resources provided on Canvas
 *
 * Bugs or other notes: No known bugs
 *
 * @param <T> type of a vertex
 */
public class GraphImpl<T> implements GraphADT<T> {

    // YOU MAY ADD ADDITIONAL private members
    // YOU MAY NOT ADD ADDITIONAL public members

    /**
     * Store the vertices and the vertice's adjacent vertices
     */
    private Map<T, List<T>> verticesMap; 
   
    
    /**
     * Construct and initialize and empty Graph
     */ 
    public GraphImpl() {
        verticesMap = new HashMap<T, List<T>>();
    }
    
    /**
     * Add new vertex to the graph.
     *
     * If vertex is null or already exists,
     * method ends without adding a vertex or 
     * throwing an exception.
     * 
     * Valid argument conditions:
     * 1. vertex is non-null
     * 2. vertex is not already in the graph 
     * 
     * @param vertex the vertex to be added
     */
    public void addVertex(T vertex) {

    	if (vertex == null || hasVertex(vertex) == true) {
    		//nothing should be added
            return;
        }  
    	
    	//add vertex
    	verticesMap.put(vertex, new LinkedList<T>());
    	
    }
    
    /**
     * Remove a vertex and all associated 
     * edges from the graph.
     * 
     * If vertex is null or does not exist,
     * method ends without removing a vertex, edges, 
     * or throwing an exception.
     * 
     * Valid argument conditions:
     * 1. vertex is non-null
     * 2. vertex is not already in the graph 
     *  
     * @param vertex the vertex to be removed
     */
    public void removeVertex(T vertex) {
    	
    	if (vertex == null || hasVertex(vertex) == false) {
            //nothing should be removed
    		return;
        }  
    	
    	//remove edges from this vertex to other courses
		List<T> vertexList = getAdjacentVerticesOf(vertex);
		for (int i=0; i < vertexList.size(); i++) {
			removeEdge(vertex, vertexList.get(i));
		}
    
    	//remove edges from other courses to this vertex
		Set<T> verticesSet = getAllVertices();
		Iterator<T> setItr = verticesSet.iterator();
		while (setItr.hasNext()) {
			T sourceVertex = setItr.next();
			if (hasEdge(sourceVertex,vertex) == true) {
				removeEdge(sourceVertex,vertex);
			}
		}
		
		//remove the vertex
    	verticesMap.remove(vertex, getAdjacentVerticesOf(vertex));

    }

    /**
     * Add the edge from vertex1 to vertex2
     * to this graph.  (edge is directed and unweighted)
     * If either vertex does not exist,
     * no edge is added and no exception is thrown.
     * If the edge exists in the graph,
     * no edge is added and no exception is thrown.
     * 
     * Valid argument conditions:
     * 1. neither vertex is null
     * 2. both vertices are in the graph 
     * 3. the edge is not in the graph
     *  
     * @param vertex1 the first vertex (src)
     * @param vertex2 the second vertex (dst)
     */
    public void addEdge(T vertex1, T vertex2) {
    	
    	//handle special cases
    	if (vertex1 == null || vertex2 == null) {
            //vertex is null, nothing should be added
    		return;
        } 
    	
    	if (hasVertex(vertex1) == false || hasVertex(vertex2) == false) {
    		//vertex does not exist, nothing is added
    		return;
    	}
    	
    	if (hasEdge(vertex1, vertex2) == true) {
    		//edge already exist, do not add
    		return;
    	}
    	
    	//add the edge
    	List<T> verticesList = getAdjacentVerticesOf(vertex1);
    	verticesList.add(vertex2);
    	
    }
    
    /**
     * Remove the edge from vertex1 to vertex2
     * from this graph.  (edge is directed and unweighted)
     * If either vertex does not exist,
     * or if an edge from vertex1 to vertex2 does not exist,
     * no edge is removed and no exception is thrown.
     * 
     * Valid argument conditions:
     * 1. neither vertex is null
     * 2. both vertices are in the graph 
     * 3. the edge from vertex1 to vertex2 is in the graph
     *  
     * @param vertex1 the first vertex
     * @param vertex2 the second vertex
     */
    public void removeEdge(T vertex1, T vertex2) {
    	
    	//handle special cases
    	if (vertex1 == null || vertex2 == null) {
    		//vertex is null, nothing should be removed
            return;
        } 
    	
    	if (hasVertex(vertex1) == false || hasVertex(vertex2) == false) {
    		//vertex does not exist, nothing should be removed
    		return;
    	}
    	
    	if (hasEdge(vertex1, vertex2) == false) {
    		//no edge exists to remove, nothing should be removed
    		return;
    	}
    	
    	List<T> verticesList = getAdjacentVerticesOf(vertex1);
    	verticesList.remove(vertex2);
    }    
    
    
    /**
     * Returns a Set that contains all the vertices
     * 
     * @return a java.util.Set<T> where T represents the vertex type
     */
    public Set<T> getAllVertices() {
    	
        return verticesMap.keySet();
    }

    /**
     * Get all the neighbor (adjacent) vertices of a vertex
     * 
     * @param vertex the specified vertex
     * @return an List<T> of all the adjacent vertices for specified vertex
     */
    public List<T> getAdjacentVerticesOf(T vertex) {

    	if (vertex == null || hasVertex(vertex) == false) {
    		//vertex is null or does not exist, return null
    		return null;
    	}
    	
        return verticesMap.get(vertex);
    }
    
    /**
     * Search a set to see if a vertex is included
     * 
     * @param vertex the specified vertex
     * @return true if the verticesSet includes the vertex
     */
    public boolean hasVertex(T vertex) {
        
    	//setup needed variables for set, iterator, and found
    	Set<T> verticesSet = getAllVertices();
    	Iterator<T> setItr = verticesSet.iterator();
    	boolean found = false;
    	
    	//loop through set, stop if vertex is found
    	while (setItr.hasNext() && found == false) {
    		if (setItr.next().equals(vertex)) {
    			found = true;
    			return true;
    		}
    	}
        return false;
    }
    
    /**
     * Returns the number of vertices in this graph.
     * @return number of vertices in graph.
     */
    public int order() {
    	return verticesMap.size();
    }
    
    /**
     * Returns the number of edges in this graph.
     * @return number of edges in the graph.
     */
    public int size() {
    	Iterator<Map.Entry<T, List<T>>> itr1 = verticesMap.entrySet().iterator(); 
    	int edgeCount = 0;
		
    	while (itr1.hasNext()) { 
			Map.Entry<T, List<T>> vertex = itr1.next(); 
			List<T> vertexList = vertex.getValue();
			edgeCount = edgeCount + vertexList.size();
		} 
    	
        return edgeCount;
    }
    
    
    /**
     * Prints the graph for the reference
     * DO NOT EDIT THIS FUNCTION
     * DO ENSURE THAT YOUR verticesMap is being used 
     * to represent the vertices and edges of this graph.
     */
    public void printGraph() {

        for ( T vertex : verticesMap.keySet() ) {
            if ( verticesMap.get(vertex).size() != 0) {
                for (T edges : verticesMap.get(vertex)) {
                    System.out.println(vertex + " -> " + edges + " ");
                }
            } else {
                System.out.println(vertex + " -> " + " " );
            }
        }
    }
    
    //Returns true if an edge exists between the two vertices specified 
    private boolean hasEdge(T vertex1, T vertex2) {

		List<T> vertex1List = getAdjacentVerticesOf(vertex1);
		
		for (int i=0; i < vertex1List.size(); i++) {
			if (vertex1List.get(i).equals(vertex2)) {
				return true;
			}
		}

    	return false;
    }
    
    //main method used for testing
    public static void main(String[] args) {
    	GraphImpl<Integer> testGraph = new GraphImpl<Integer>();
    	//add vertices
    	testGraph.addVertex(200);
    	testGraph.addVertex(300);
    	testGraph.addVertex(400);
    	testGraph.addVertex(760);
    	testGraph.addVertex(540);
    	testGraph.addVertex(790);
    	
    	//add edges
    	testGraph.addEdge(300, 200);
    	testGraph.addEdge(400, 300);
    	testGraph.addEdge(760, 400);
    	testGraph.addEdge(760, 540);
    	
    	//test print
    	testGraph.printGraph();
    	
    	//test hasVertex
    	System.out.println(testGraph.hasVertex(200));
    	System.out.println(testGraph.hasVertex(100));
    	
    	//test size, order
    	System.out.println(testGraph.size());
    	System.out.println(testGraph.order());
    	
    	//test remove vertex
    	testGraph.removeVertex(300);
    	testGraph.printGraph();
    	System.out.println(testGraph.size());
    	System.out.println(testGraph.order());
    	System.out.println(testGraph.hasVertex(300));
    	
    	
    	
    	
    }
}
