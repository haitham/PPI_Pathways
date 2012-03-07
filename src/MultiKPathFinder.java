import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.RestoreAction;

public class MultiKPathFinder extends KHopPathFinder {
	
	private Boolean[] terminal;
	private HashMap<Integer, Integer> kFrequencies;
	
	public MultiKPathFinder(Integer size, List<InputEdge> edges, List<Integer> startNodes, List<Integer> endNodes) {
		super(size, edges, startNodes, endNodes);
		degreeNodeIndex = new HashMap<Integer, List<Integer>>();
		markTerminalNodes();
	}
	
	private void markTerminalNodes(){
		terminal = new Boolean[networkSize];
		for (int i=0; i<networkSize; i++){
			terminal[i] = false;
		}
		for (Integer node: startNodes){
			terminal[node] = true;
		}
		for (Integer node: endNodes){
			terminal[node] = true;
		}
	}
	
	public void runKValuesAnalysis(Integer pathLength, Integer repeat, String coloringOrder, String coloringScheme, Integer maxK) throws IOException{
		if (maxK != null)
			this.maxK = maxK;
		this.pathLength = pathLength;
		kFrequencies = new HashMap<Integer, Integer>();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new DataOutputStream(new FileOutputStream("" + pathLength + "-" + coloringOrder + "-" + coloringScheme + (maxK == null ? "" : "-" + maxK)))));
		writer.write("============================================================\n");
		writer.write("K values for path length: " + pathLength + ", coloring order: " + coloringOrder + ", coloring scheme: " + coloringScheme + (maxK == null ? "" : ", max K: " + maxK) + "\n");
		writer.write("============================================================\n");
		for (int i=0; i<repeat; i++){
			System.out.println(i);
			colorAllNodes(coloringOrder, coloringScheme);
			for(int j=0; j<networkSize; j++){
				Integer kFrequency = kFrequencies.get(nodeKValues[j]);
				if (kFrequency == null)
					kFrequency = 0;
				kFrequencies.put(nodeKValues[j], kFrequency + 1);
				writer.write("" + nodeKValues[j]);
				writer.write(" ");
			}
			writer.write("\n");
		}
		writer.write("\n");
		writer.write("K values histogram:\n");
		for (Integer k : kFrequencies.keySet()){
			writer.write("" + k + ": " + kFrequencies.get(k) + "\n");
		}
		writer.close();
	}
	
	public List<MinKPath> runKStats(Integer pathLength, Integer repeat){
		this.pathLength = pathLength;
		List<MinKPath> paths = new ArrayList<MinKPath>();
		System.out.println("============================================================");
		System.out.println("Results for path length: " + pathLength);
		System.out.println("============================================================");
		Integer sumStart = 0;
		Integer sumEnd = 0;
		Integer sumMax = 0;
		for (int i=0; i<repeat; i++){
			colorAllNodes();
			MinKPath path = getMinKPath();
			paths.add(path);
			System.out.print(nodeKValues[path.getStartNode()]);
			System.out.print("  ");
			Integer maxK = -1;
			for (Integer node : path.getInnerNodes()){
				if (maxK < nodeKValues[node])
					maxK = nodeKValues[node];
				System.out.print(nodeKValues[node]);
				System.out.print("  ");
			}
			System.out.print(nodeKValues[path.getEndNode()]);
			System.out.print("  |  ");
			System.out.println("Start:" + nodeKValues[path.getStartNode()] + "  MaxInner:" + maxK + "  End:" + nodeKValues[path.getEndNode()]);
			sumStart += nodeKValues[path.getStartNode()];
			sumEnd += nodeKValues[path.getEndNode()];
			sumMax += maxK;
		}
		System.out.println("============================================================");
		System.out.println("Average k value for start node: " + (double)sumStart / (double)repeat);
		System.out.println("Average k value for end node: " + (double)sumEnd / (double)repeat);
		System.out.println("Average maximum k value for an inner node: " + (double)sumMax / (double)repeat);
		System.out.println();
		System.out.println();
		return paths;	
	}
	
	protected Double successProbability(){
		List<Integer[]> placements = getDistinctPlacements(getMinKPath());
		long maxNumberOfColorings = 0;
		for (Integer[] placement : placements){
			long colorings = numberOfColorings(placement);
			if (colorings > maxNumberOfColorings)
				maxNumberOfColorings = colorings;
		}
		Double probability = 1.0/maxNumberOfColorings;
		for (int i=1; i<=pathLength; i++){ // factorial of pathlength
			probability = probability * i;
		}
		return probability;
	}
	
	// Implementation of the chromatic polynomial
	protected long numberOfColorings(Integer[] placement){
		boolean[][] constraintGraph = new boolean[placement.length][placement.length];
		List<Integer[]> edges = new ArrayList<Integer[]>();
		
		for (int i=0; i<placement.length; i++){
			for (int j=i-nodeKValues[placement[i]]; j<=i+nodeKValues[placement[i]]; j++){
				if (j<0 || j==i || j>=placement.length)
					continue;
				if (!constraintGraph[i][j]){
					constraintGraph[i][j] = true;
					constraintGraph[j][i] = true;
					Integer[] edge = {i,j};
					edges.add(edge);
				}
			}
		}
		return chromaticNumber(edges, constraintGraph.length);
	}
	
	long chromaticNumber(List<Integer[]> edges, Integer size){
		if (size == 0){
			return 0;
		}
		if (edges.isEmpty()){
			long result = 1;
			for (int i=0; i<size; i++)
				result = result * pathLength;
			return result;
		}
		
		// G - uv
		Integer[] edge = edges.remove(edges.size()-1);
		Integer u = edge[0];
		Integer v = edge[1];
		long cutNumber = chromaticNumber(edges, size);
		
		//G / uv
		boolean[][] mergedGraph = mergeNodes(edges, size, u, v);
		List<Integer[]> mergedEdges = new ArrayList<Integer[]>();
		for (int i=0; i<mergedGraph.length; i++){
			for (int j=i; j<mergedGraph[i].length; j++){
				if (mergedGraph[i][j]){
					Integer[] newEdge = {i, j};
					mergedEdges.add(newEdge);
				}
			}
		}
		long mergedNumber = chromaticNumber(mergedEdges, size-1);
		
		//to maintain previous stack state
		edges.add(edge);
		
		return cutNumber - mergedNumber;
	}
	
	boolean[][] mergeNodes(List<Integer[]> edges, Integer originslSize, Integer u, Integer v){
		boolean[][] mergedGraph = new boolean[originslSize-1][originslSize-1];
		//mark the higher node as the one to go, the other is to stay
		Integer toGo = Math.max(u, v);
		Integer toStay = Math.min(u, v);
		for (Integer[] edge : edges){
			Integer i;
			Integer j;
			if (edge[0] == toGo)
				i = toStay;
			else if (edge[0] > toGo)
				i = edge[0] - 1;
			else
				i = edge[0];
			if (edge[1] == toGo)
				j = toStay;
			else if (edge[1] > toGo)
				j = edge[1] - 1;
			else
				j = edge[1];
			if (i != j){
				mergedGraph[i][j] = true;
				mergedGraph[j][i] = true;
			}
		}
		return mergedGraph;
	}
	
	private List<Integer[]> getDistinctPlacements(MinKPath minPath){
		// hash the nodes of k values
		HashMap<Integer, List<Integer>> kNodes = new HashMap<Integer, List<Integer>>();
		for (Integer node : minPath.getInnerNodes()){
			List<Integer> thisKNodes = kNodes.get(nodeKValues[node]);
			if (thisKNodes == null)
				thisKNodes = new ArrayList<Integer>();
			thisKNodes.add(node);
			kNodes.put(nodeKValues[node], thisKNodes);
		}
		
		// recursively find all distinct placements
		Integer[] placement = new Integer[minPath.length()];
		placement[0] = minPath.getStartNode();
		placement[placement.length-1] = minPath.getEndNode();
		return completeDistinctPlacements(kNodes, placement, 1);
	}
	
	private List<Integer[]> completeDistinctPlacements(HashMap<Integer, List<Integer>> kNodes, Integer[] placement, Integer place){
		List<Integer[]> results = new ArrayList<Integer[]>();
		// Try each k value at this place
		for (Integer k : kNodes.keySet()){
			List<Integer> thisKnodes = kNodes.get(k);
			if (thisKnodes.isEmpty())
				continue;
			Integer[] newPlacement = placement.clone();
			//Try a node with this k value, remove it from available nodes
			newPlacement[place] = thisKnodes.remove(thisKnodes.size()-1);
			kNodes.put(k, thisKnodes);
			
			//recursively advance to next place, retrieving a list of possible placements with current place set
			results.addAll(completeDistinctPlacements(kNodes, newPlacement, place+1));
			if (results.isEmpty())
				results.add(newPlacement);
			
			//revert available nodes for future choice
			thisKnodes.add(newPlacement[place]);
			kNodes.put(k, thisKnodes);
		}
		return results;
	}
	
	private MinKPath getMinKPath(){
		return new MinKPath(getMinKTerminalNode(startNodes), getMinKTerminalNode(endNodes), getMinKInnerNodes());
	}
	
	private Integer getMinKTerminalNode(List<Integer> terminalList){
		Integer minKNode = terminalList.get(0);
		for (Integer node : terminalList){
			if (nodeKValues[node] < nodeKValues[minKNode]){
				minKNode = node;
			}
		}
		return minKNode;
	}
	
	private List<Integer> getMinKInnerNodes(){
		List<Integer> innerNodes = new ArrayList<Integer>();
		Integer innerLength = pathLength-2;
		boolean initialized = false;
		Integer node = 0;
		for (node=0; !initialized; node++){
			if (terminal[node])
					continue;
			boolean inserted = false;
			for (int j=0; j<innerNodes.size(); j++){
				if (nodeKValues[node] < nodeKValues[innerNodes.get(j)]){
					innerNodes.add(j, node);
					inserted = true;
					break;
				}
			}
			if (!inserted)
				innerNodes.add(node);
			if (innerNodes.size() == innerLength)
				initialized = true;
		}
		for (node=node-1; node<networkSize; node++){
			if (terminal[node])
				continue;
			for (int j=0; j<innerNodes.size(); j++){
				if (nodeKValues[node] < nodeKValues[innerNodes.get(j)]){
					innerNodes.add(j, node);
					innerNodes.remove(innerNodes.size()-1);
					break;
				}
			}
			node ++;
		}
		return innerNodes;
	}
	
}
