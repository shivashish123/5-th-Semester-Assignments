package cool;

import java.util.*;
import cool.Semantic;

// class for node of graph containing name of class and its adjacency list
class Vertex {
	public String name;
    public List<Vertex> adj;
    public Vertex(String name) {
        this.name = name;
        this.adj = new LinkedList<Vertex>();
    }
}
// class Graph for inheritance graph
public class Graph{
    // maps class names to vertex objects
	public Map<String, Vertex> vertexMap;

    public Graph() {
        vertexMap = new HashMap<String, Vertex>();
    }

    public void addEdge(String src, String des) {
        Vertex v = getVertex(src);
        Vertex w = getVertex(des);
        v.adj.add(w);
    }

    public Vertex getVertex(String name) {
        if (name == null) {
            return null;
        }

        Vertex v = vertexMap.get(name);
        if (v == null) {
            v = new Vertex(name);
            vertexMap.put(name, v);
        }
        return v;
    }

    public String detectCycle() {
        String isCycle = "no_cycle"; 
        Set<Vertex> visited = new HashSet<Vertex>();
        Set<Vertex> recurStack = new HashSet<Vertex>();
        boolean cycle = false;
        for (Vertex v : vertexMap.values()) {
            if (isCyclic(v, visited, recurStack)) {
                return v.name;
            }
        }
        return isCycle;
    }

    // In this, isCylic method is used to check if there are any cycles in inheritance graph. It uses two sets visited and recurStack. 
    // visited set contains all the vertices which would be visited while dfs while recurStack contains the vertices which would be visited 
    // while doing dfs from a vertex and if any vertex which is present in recurStack is visited again, 
    // it means presence of cycle. After all the neighbouring vertices of a vertex has been visited, the vertex is removed from recurStack
    public boolean isCyclic(Vertex v,Set<Vertex> visited,Set<Vertex> recurStack)  
    { 
        if (recurStack.contains(v)) 
            return true; 
  
        if (visited.contains(v)) 
            return false; 
              
        visited.add(v); 
        recurStack.add(v); 
          
        for (Vertex c: v.adj) 
            if (isCyclic(c, visited, recurStack)) 
                return true; 
                  
        recurStack.remove(v); 

        return false; 
    } 

    // In this conformance method, it checks if class1 conforms to class2. 
    // Basically, it traverses to the ancestors of first class until it encounters second class name which means that first is descendant of second. Hence it conforms to second.
    public boolean conformance(String class1,String class2) {
        if (class1 == null)
            return false;
        if (class1.equals("no_type"))
            return true;
        if (class2 == null)
            return false;
        // System.out.println(class1);
        // System.out.println(class2);
        AST.class_ c1;
        while (class1 != null)
        {
        	c1 = Semantic.classMap.get(class1);
            if (c1.name.equals(class2)) {
                return true;
            }
            class1 = c1.parent;
        }
        return false;
    }

    // This method takes two class names as parameters and find the lca of the two vertices in inheritance graph.
    // So here, we are storing the path of each vertex to the root of graph in a list. Then, we start comparing from the end of the lists
    // of paths of two vertices and keep traversing until a mismatch is found. So, the vertex previous to mismatch is the lca of two vertices.
    public String lca(String class1, String class2) {
        List<String> path1 = new ArrayList<String>(); //This includes class1 itself.
        List<String> path2 = new ArrayList<String>(); //This includes class2 itself.
        String c1 = class1;
        String c2 = class2;

        if (Semantic.classMap.get(c1) == null || Semantic.classMap.get(c2) == null) {
            return null;
        }
        while (c1 != null) {
            path1.add(c1);
            c1 = (Semantic.classMap.get(c1)).parent;
        }
        while (c2 != null) {
            path2.add(c2);
            c2 = (Semantic.classMap.get(c2)).parent;
        }
        String lowestCommonAncestor = null;
        int i=path1.size()-1,j=path2.size()-1;
        if(i<0 || j<0)
        	return null;
        while (i>=0 && j>=0) {
            String ancestor1 = path1.get(i);
            String ancestor2 = path2.get(j);

            if (!ancestor1.equals(ancestor2))
                break;
            lowestCommonAncestor = ancestor1;
            i--;
            j--;
        }
        return lowestCommonAncestor;
    }
}