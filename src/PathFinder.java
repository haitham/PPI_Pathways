import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

public class PathFinder {
	
	public static final Double infinity = 100000000.0;
	public static final Double roundOffError = 0.00000001;
	protected Integer networkSize;
	protected Double[][] network;
	protected Double[][] minDistance;
	protected Integer[][] minNode;
	protected List<Integer> startNodes;
	protected List<Integer> endNodes;
	protected Integer pathLength;
	protected List<ComparableList<Integer>> colorSets;
	protected HashMap<String, Integer> colorSetsIndex;
	protected Integer[] nodeColors;
	protected boolean tabulating;
	protected boolean producingColors;
	protected boolean consumingColors;
	protected List<Integer[]> colorsQueue;
	
	protected class IterationResultComparator implements Comparator<IterationResult>{

		public int compare(IterationResult o1, IterationResult o2) {
			return o1.distance.compareTo(o2.distance);
		}
		
	}
	
	public PathFinder(Integer size, List<InputEdge> edges, List<Integer> startNodes, List<Integer> endNodes) {
		tabulating = true;
		networkSize = size;
		this.startNodes = startNodes;
		this.endNodes = endNodes;
		// initialize network
		this.network = new Double[size][size];
		for (int i=0; i<size; i++){
			for (int j=0; j<size; j++){
				network[i][j] = infinity;
			}
		}
		// insert network data
		for (InputEdge edge : edges){
			network[edge.to][edge.from] = -1 * Math.log(edge.score);
			network[edge.from][edge.to] = network[edge.to][edge.from];
		}
	}
	
	protected void initialize(Integer pathLength){
		this.pathLength = pathLength;
		colorSets = PathFinder.powerset(pathLength);
		colorSetsIndex = new HashMap<String, Integer>();
		for (int i=0; i<colorSets.size(); i++){
			colorSetsIndex.put(colorSetKey(colorSets.get(i)), i);
		}
	}
	
	public void repeatIteration(Integer pathLength, Integer times){
		initialize(pathLength);
		Double failureProbability = 1.0;
		Double minDistance = infinity;
		IterationResult[] results = new IterationResult[times];
		List<Integer> optimalIterations = new ArrayList<Integer>();
		for (int i=0; i<times; i++){
//			Long startIteration = System.currentTimeMillis();
			IterationResult result = iteration();
//			Long endIteration = System.currentTimeMillis();
			//update failure probability
//			failureProbability = failureProbability * (1 - result.successProbability);
			if (result.distance > minDistance - roundOffError && result.distance < minDistance + roundOffError){ //equals
				optimalIterations.add(i);
			}
			else if (result.distance < minDistance){
				minDistance = result.distance;
				optimalIterations = new ArrayList<Integer>();
				optimalIterations.add(i);
			}
//			System.out.println("" + i + "\t\t" + failureProbability + "\t\t" + minDistance + "\t\t" + result.distance + ":" + result.path.toString() + "\t\t" + (endIteration - startIteration));
//			System.out.println(i);
			results[i] = result;
		}
		Integer winnerIndex = optimalIterations.get((int)Math.floor((Math.random() * optimalIterations.size())));
		IterationResult winner = results[winnerIndex];
		boolean winnerFound = false;
		for (int i=0; i<times; i++){
			failureProbability = failureProbability * (1.0 - results[i].successProbability);
			String found = "";
			if (winnerFound)
				found = "1";
			else{
				if (results[i].path.equals(winner.path)){
					found = "1";
					winnerFound = true;
				} else
					found = "0";
			}
			Integer optimal = optimalIterations.contains(i) ? 1 : 0;
			System.out.println("" + i + "\t" + (1.0 - failureProbability) + "\t" + results[i].path.toString() + "\t" + results[i].distance + "\t" + optimal + "\t" + found);
		}
	}
	
	public PathResult run(Integer pathLength, Double confidence){
		// Initializing and hashing color sets
		initialize(pathLength);
		
		Integer iterationsCount = 0;
		Double failureProbability = 1.0;
		PriorityQueue<IterationResult> results = new PriorityQueue<IterationResult>(1, new IterationResultComparator());
		
		Long startTime = System.currentTimeMillis();
		while (1-failureProbability < confidence){
			iterationsCount ++;
			//New iteration
			IterationResult result = iteration();
			//add its result if it's not included already
			if (!results.contains(result)){
				results.add(result);
			}
			//update failure probability
			failureProbability = failureProbability * (1 - result.successProbability);
//			System.out.println("LOG: (" + pathLength + ", " + confidence + ") iteration: " + iterationsCount + ", confidence: " + (1-failureProbability));
		}
		
		return new PathResult(results, iterationsCount, System.currentTimeMillis() - startTime);
	}
	
	public PathResult runWithExtraParams(Integer pathLength, Double confidence, boolean tabulating, boolean colorConsuming, boolean colorProducing, List<Integer[]> colorsQueue){
		this.tabulating = tabulating;
		this.producingColors = colorProducing;
		this.consumingColors = colorConsuming;
		this.colorsQueue = colorsQueue;
		return this.run(pathLength, confidence);
	}
	
	protected void colorAllNodes(){
		nodeColors = new Integer[networkSize];
		for (int i=0; i<networkSize; i++){
			nodeColors[i] = (int) Math.ceil(pathLength * Math.random());
		}
	}
	
	protected IterationResult iteration(){
		// Color all nodes
		if (consumingColors){
			if (colorsQueue.isEmpty())
				colorAllNodes();
			else
				nodeColors = colorsQueue.remove(0);
		} else {
			colorAllNodes();
			if (producingColors)
				colorsQueue.add(nodeColors);
		}

		// calculate success probability of this iteration
		Double successProbability = successProbability();
		
//		if (tabulating){
			//run DP
//			List<Integer> path = tabulate();
//			return new IterationResult(path, minDistance[colorSets.size()-1][path.get(path.size()-1)], successProbability);
//		} else {
			return new IterationResult(new ArrayList<Integer>(), 1.0, successProbability);
//		}
	}
	
	protected Double successProbability(){
		Double result = 1.0;
		//m!/m^m
		for (int i=pathLength; i>0; i--){
			result = result * i / pathLength;
		}
		return result;
	}
	
	//Dynamic programming part
	protected List<Integer> tabulate(){
		// Initializing DP structures
		minDistance = new Double[colorSets.size()][networkSize];
		minNode = new Integer[colorSets.size()][networkSize];
		
		//tabulation
		for (int i=1; i<colorSets.size(); i++){
			ComparableList<Integer> set = colorSets.get(i);
			for (int v=0; v<networkSize; v++){
				if (!set.contains(nodeColors[v])){
					continue; // hole - illegal case
				}
				if (set.size() < 2){ // base case
					if (startNodes.contains(v)){
						minDistance[i][v] = 0.0;
					} else {
						minDistance[i][v] = infinity;
					}
				} else { //general case
					Double currentMinDistance = infinity;
					Integer currentMinNode = null;
					ComparableList<Integer> subset = set.cloneWithout(nodeColors[v]); //color subset
					Integer subsetIndex = colorSetsIndex.get(colorSetKey(subset)); //Its index
					for (int u=0; u<networkSize; u++){
						if (subset.contains(nodeColors[u])){
							Double newDistance = minDistance[subsetIndex][u] + network[u][v];
							if (newDistance < currentMinDistance){
								currentMinDistance = newDistance;
								currentMinNode = u;
							}
						}
					}
					minDistance[i][v] = currentMinDistance;
					minNode[i][v] = currentMinNode;
				}
			}
		}
		
		//Backtrack choosing from endnodes
		Integer min = endNodes.get(0);
		for (Integer node : endNodes){
			if (minDistance[colorSets.size()-1][node] < min){
				min = node;
			}
		}
		List<Integer> path = new LinkedList<Integer>();
		ComparableList<Integer> set = colorSets.get(colorSets.size()-1);
		while (min != null){
			path.add(0, min);
			Integer setIndex = colorSetsIndex.get(colorSetKey(set));
			set = set.cloneWithout(nodeColors[min]);
			min = minNode[setIndex][min];
		}
		if (!startNodes.contains(path.get(0))){
			System.out.println("EXCEPTION!!! - There should be mistake - path doesn't start with a start node");
		}
		return path;
	}
	
	
	// Generate powerset of {1,..,size}
	public static List<ComparableList<Integer>> powerset(Integer size) {
		List<Integer> list = new ArrayList<Integer>();
		for (int i=1; i<=size; i++){
			list.add(i);
		}
		List<ComparableList<Integer>> ps = new ArrayList<ComparableList<Integer>>();
		ps.add(new ComparableList<Integer>());   // add the empty set
		
		for (Integer item : list) {
			List<ComparableList<Integer>> newPs = new ArrayList<ComparableList<Integer>>();
	 
			for (ComparableList<Integer> subset : ps) {
				newPs.add(subset);
	 
				ComparableList<Integer> newSubset = new ComparableList<Integer>(subset);
				newSubset.add(item);
				newPs.add(newSubset);
			}
	 
			ps = newPs;
		}
		Collections.sort(ps);
		return ps;
	}
	
	//Generate color set key
	protected String colorSetKey(ComparableList<Integer> colorSet){
		StringBuffer key = new StringBuffer();
		for (Integer color : colorSet){
			key.append(color);
			key.append("-");
		}
		return key.toString();
	}
	
}
