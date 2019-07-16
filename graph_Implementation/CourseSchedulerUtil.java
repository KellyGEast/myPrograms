
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


/**
 * Filename:   CourseSchedulerUtil.java
 * Project:    p4
 * Authors:    Debra Deppeler, Kelly East
 * 
 * Course:     cs400 
 * Due Date:   11/17/2018
 *
 * Additional credits: Geeks for Geeks documentation on JSON parsing
 * 					   Resources provided on Canvas
 *
 * Bugs or other notes: No known bugs
 * 
 * Use this class for implementing Course Planner
 * @param <T> represents type
 */

public class CourseSchedulerUtil<T> {
    
    // can add private but not public members
    
    /**
     * Graph object
     */
    private GraphImpl<T> graphImpl;
    private Entity[] entities;
    
    
    /**
     * constructor to initialize a graph object
     */
    public CourseSchedulerUtil() {
        this.graphImpl = new GraphImpl<T>();
    }
    
    /**
    * createEntity method is for parsing the input json file 
    * @return array of Entity object which stores information 
    * about a single course including its name and its prerequisites
    * @throws Exception like FileNotFound, JsonParseException
    */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Entity[] createEntity(String fileName) throws Exception {
        
    	if (fileName == null) {
    		throw new Exception("No fileName entered");
    	}
    	
    	try {
    	// parsing file "JSONExample.json" 
    	Object obj = new JSONParser().parse(new FileReader(fileName)); 

    	// type casting obj to JSONObject 
    	JSONObject jo = (JSONObject) obj; 
    				
    	//getting courses
    	JSONArray courseArray = (JSONArray) jo.get("courses");
    	
    	//set temp ArrayList for courses
		ArrayList<Entity> tempArray = new ArrayList<Entity>();
		ArrayList<String> tempPrereq = new ArrayList<String>();
		
		//Iterate through courses
		Iterator courseItr = courseArray.iterator();
		while (courseItr.hasNext()) {
			JSONObject courseObj = (JSONObject) courseItr.next();
			//System.out.println(courseObj);
			String courseName = (String)courseObj.get("name");
			JSONArray preArray = (JSONArray) courseObj.get("prerequisites");
			
			String[] stringArray = new String[preArray.size()];
			for (int i = 0; i < preArray.size(); i++) {
				stringArray[i] = (String) preArray.get(i);
				tempPrereq.add((String)preArray.get(i));
			}
			
			Entity newEntity = new Entity();
			newEntity.setName(courseName);
			newEntity.setPrerequisites(stringArray);
			tempArray.add(newEntity);

		}
		
		//check that all pre-reqs are added
		boolean foundPrereq = false;
		for (int i = 0; i < tempPrereq.size(); i++) {
			foundPrereq = false;
			for (int j = 0; j < tempArray.size() && !foundPrereq; j++) {
				if (tempPrereq.get(i).equals(tempArray.get(j).getName())) {
					foundPrereq = true;
				}
			}
			if (!foundPrereq) {
				Entity newEntity = new Entity();
				newEntity.setName(tempPrereq.get(i));
				newEntity.setPrerequisites(new String[0]);
				tempArray.add(newEntity);
			}
		}
		
		//create the entity array
		entities = new Entity[tempArray.size()];
		for (int i = 0; i < tempArray.size(); i ++) {
			entities[i] = tempArray.get(i);
			//System.out.println(entityArray[i].getName() + ", " + entityArray[i].getPrerequisites());
		}    			
		
		return entities;

    	} catch (Exception e) {
			System.out.println("Error opening or parsing file. Exception thrown: " + e);
			return null;
    	}

    }
    
    
    
    /**
     * Construct a directed graph from the created entity object 
     * @param entities which has information about a single course 
     * including its name and its prerequisites
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void constructGraph(Entity[] entities) {
    	
    	//create graph
    	graphImpl = new GraphImpl<T>();
    	
    	//add vertices
    	for (int i = 0; i < entities.length; i++) {
    		graphImpl.addVertex((T)entities[i].getName());
    	}
    	
    	//add edges
    	for (int i = 0; i < entities.length; i++) {
    		T vertex1 = (T)entities[i].getName();
    		T[] preArray = (T[]) entities[i].getPrerequisites();
    		for (int j = 0; j < preArray.length; j++) {
    			T vertex2 = preArray[j];
    			graphImpl.addEdge(vertex1, vertex2);
    		}
    	}
    	//graphImpl.printGraph();
    }
    
    
    /**
     * Returns all the unique available courses
     * @return the sorted list of all available courses
     */
    public Set<T> getAllCourses() {
    	return graphImpl.getAllVertices();
    }
    
    
    /**
     * To check whether all given courses can be completed or not
     * @return boolean true if all given courses can be completed,
     * otherwise false
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
	public boolean canCoursesBeCompleted() throws Exception {
    	boolean valid = true; //create a tracking variable, assume false unless set to true
    	
    	for (int i = 0; i < entities.length && valid !=false; i++) {
    		
    		ArrayList<T> visited = new ArrayList<T>(); //use to track the visited courses
    		T course = (T) entities[i].getName();
    		
    		valid = canCoursesBeCompletedHelper(course, visited);
    	}

    	return valid;
    }
    
    
	@SuppressWarnings("unchecked")
	private boolean canCoursesBeCompletedHelper(T course, ArrayList<T> visited)  { 

		// If we have already visited this course, there is a cycle in the graph
		// course cannot be completed, return false
		for (int i = 0; i < visited.size(); i++) {
			if (course.equals(visited.get(i))) {
				return false; 
			}
		}
		
		//course is not in visited list, add course to list
		visited.add(course);
		
		//get prereq list
		List<T> prereqList = (List<T>) graphImpl.getAdjacentVerticesOf((T)course);
		
		//call helper on each of the prereq courses
		for (int j = 0; j < prereqList.size(); j++) {
			if (!canCoursesBeCompletedHelper((T) prereqList.get(j), visited)) {
				//course cannot be completed, return false
				return false;
			}
		}
		
		//if we did not return false by this point, then all courses can be completed
		return true;
		
	}
    
    
    /**
     * The order of courses in which the courses has to be taken
     * @return the list of courses in the order it has to be taken
     * @throws Exception when courses can't be completed in any order
     */
    @SuppressWarnings("unchecked")
	public List<T> getSubjectOrder() throws Exception {
        
    	if (!canCoursesBeCompleted()) {
    		throw new Exception("Courses cannot be completed in any order");
    	}
    	
    	//create an array list to track in
    	ArrayList<T> courseOrder = new ArrayList<T>();
    	
    	for (int i = 0; i < entities.length; i++) {
    		T courseName = (T) entities[i].getName();
    		//call helper on each course
    		courseOrder = getSubjectOrderHelper(courseName, courseOrder);
    	}
    	
        return courseOrder;

    }
    
    @SuppressWarnings("unchecked")
	private ArrayList<T> getSubjectOrderHelper(T courseName, ArrayList<T> visited){
    	
    	//get prereqs
    	List<T> prereqs = graphImpl.getAdjacentVerticesOf(courseName);
    	
		//call helper on each of the prereq courses
		for (int j = 0; j < prereqs.size(); j++) {
			getSubjectOrderHelper(prereqs.get(j),visited); 
		}
		
		boolean found = false;
		for (int i = 0; i < visited.size(); i++) {
			if (courseName.equals(visited.get(i))) {
				found = true;
			}
		}
		
		if (!found) {
			//course has not been added yet - this check is to prevent duplicates
			visited.add(courseName);
		}
		
		return visited;
    }

        
    /**
     * The minimum course required to be taken for a given course
     * @param courseName 
     * @return the number of minimum courses needed for a given course
     */
    public int getMinimalCourseCompletion(T courseName) throws Exception {
    	
    	try {
        return getMinimalCourseCompletionHelper(courseName)-1; // subtract 1 to remove 
        													   // the initial course
        } catch (Exception e) {
        	return -1;
        }
    }
    
    private int getMinimalCourseCompletionHelper (T courseName) {
    	
    	// start with a count of 0
    	int count = 0;
    	
    	// get prereqs
    	List<T> prereqs = graphImpl.getAdjacentVerticesOf(courseName);
    	
    	// create a list to track what has been visited already
    	ArrayList<T> visited = new ArrayList<T>();
    	
    	// call helper on prereqs, tally count
    	for (int i = 0; i < prereqs.size(); i++) {
    		count = count + getMinimalCourseCompletionHelper(prereqs.get(i));
    	}
    	
    	// check for counting a class more than once
    	for (int j = 0; j < visited.size(); j++){
    		if (courseName.equals(visited.get(j))) {
    			// we already counted this course, do not count again
    			return count;
    		}
    	}
    	
    	// track that we added this course, increment the count
    	visited.add(courseName);
    	return count + 1;
    }
    
    //main method used for testing
    @SuppressWarnings("rawtypes")
	public static void main(String[] args) {
    	Entity [] entities = null;
    	CourseSchedulerUtil<String> tester = new CourseSchedulerUtil<String>();
    	try {
			entities = tester.createEntity("valid.json");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
    	tester.constructGraph(entities);
    	
        Set<String> result = tester.getAllCourses();
        //System.out.println(result);
        Iterator<String> setItr = result.iterator();
		while (setItr.hasNext()) {
			System.out.println(setItr.next());
		}
		
		try {
			System.out.println(tester.canCoursesBeCompleted());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			List<String> coursesList = tester.getSubjectOrder();
			for (int i = 0; i < coursesList.size(); i++) {
				System.out.println(coursesList.get(i));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			System.out.println(tester.getMinimalCourseCompletion("CS760"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		

    }

    
}
