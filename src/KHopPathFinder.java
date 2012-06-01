import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;


public class KHopPathFinder extends PathFinder {
	
	private Integer k;
//	protected Integer initialK;
	protected Integer maxK;
	protected HashMap<Integer, List<Integer>> degreeNodeIndex;
	protected Integer[] nodeKValues;
	protected boolean colorPooling;
	protected PriorityQueue<Coloring> colorPool;
	protected static Integer colorPoolSize = 10000;
	protected Coloring currentColoring;
	protected Integer[][][] kHopNodes;
	
	public static final String RANDOM_COLORING_ORDER = "RandomOrder";
	public static final String DEGREE_COLORING_ORDER = "HighestDegreeFirst";
	public static final String RANDOM_COLORING_SCHEME = "RandomColor";
	public static final String MAX_K_COLORING_SCHEME = "MaxKColor";
	protected static String default_coloring_order = RANDOM_COLORING_ORDER;
	protected static String default_coloring_scheme = RANDOM_COLORING_SCHEME;
	
	protected class Coloring implements Comparable<Coloring>{
		Integer[] nodeColors;
		Integer[] nodeKValues;
		Integer score;
		public Coloring(Integer[] nodeColors, Integer[] nodeKValues) {
			this.nodeColors = nodeColors;
			this.nodeKValues = nodeKValues;
		}
		
		public int compareTo(Coloring coloring) {
			return coloring.score().compareTo(this.score());
		}
		
		public Integer score(){
			if (this.score != null)
				return this.score;
			Integer score = 0;
			for (int i=0; i<nodeKValues.length; i++)
				score += nodeKValues[i];
			this.score = score;
			return score;
		}
	}
	
	protected void initialize(Integer pathLength){
		super.initialize(pathLength);
		kHopNodes = new Integer[networkSize][maxK+2][];
		for (int i=0; i<networkSize; i++){
			for (int j=0; j<kHopNodes[i].length; j++){
				List<Integer> neighbors = kHopNodes(i, j, new HashMap<Integer, Boolean>());
				kHopNodes[i][j] = new Integer[neighbors.size()];
				kHopNodes[i][j] = neighbors.toArray(kHopNodes[i][j]);
			}
		}
	}

	public KHopPathFinder(Integer size, List<InputEdge> edges, List<Integer> startNodes, List<Integer> endNodes) {
		super(size, edges, startNodes, endNodes);
//		initialK = 4;
		maxK = 4;
//		indexNodeDegrees();
	}
	
	private void indexNodeDegrees(){
		Integer[] degree = new Integer[networkSize];
		for (int i=0; i<networkSize; i++){
			degree[i] = 0;
			for (int j=0; j<networkSize; j++){
				if (network[i][j] < infinity - 1){
					degree[i] ++;
				}
			}
		}
		for (int i=0; i<degree.length; i++){
			List<Integer> degreeNodes = degreeNodeIndex.get(degree[i]);
			if (degreeNodes == null)
				degreeNodes = new ArrayList<Integer>();
			degreeNodes.add(i);
			degreeNodeIndex.put(degree[i], degreeNodes);
		}
	}
	
//	OBSOLETE CODE!!!
//	TO BE REMOVED
//	protected void colorAllNodes(){
//		k = initialK;
//		Boolean colorSuccess = false;
//		while (!colorSuccess){
//			nodeColors = new Integer[networkSize];
//			colorSuccess = true; //default
//			for (int i=0; i<networkSize; i++){
//				List<Integer> availableColors = kHopAvailableColors(i, k);
//				if (availableColors.size() == 0){
//					k --;
//					colorSuccess = false;
//					break;
//				} else {
//					nodeColors[i] = availableColors.get((int)Math.floor(availableColors.size() * Math.random()));
//				}
//			}
//		}
//	}
	
	protected void initializeColorPooling(){
		colorPool = new PriorityQueue<Coloring>();
		Long start = System.currentTimeMillis();
		for (int i=0; i<colorPoolSize; i++){
//			Long startIteration = System.currentTimeMillis();
			colorAllNodes();
			Integer[] kValues = collectKValues();
			colorPool.add(new Coloring(this.nodeColors, kValues));
//			System.out.println("" + i + ": " + (System.currentTimeMillis() - startIteration));
		}
		System.out.println("Network size = " + networkSize + ", Total time taken for generating " + colorPoolSize + " colorings = " + (System.currentTimeMillis() - start) + "milliseconds");
		nodeColors = null;
		nodeKValues = null;
		colorPooling = true;
//		System.exit(0);
	}
	
	public PathResult runWithPooling(Integer pathLength, Double confidence){
		initializeColorPooling();
		return this.run(pathLength, confidence);
	}
	
	protected void colorAllNodes(String coloringOrder, String coloringScheme) {
		if (colorPooling){
			currentColoring = colorPool.remove();
			nodeColors = currentColoring.nodeColors;
			return;
		}
		nodeColors = new Integer[networkSize];
		if (RANDOM_COLORING_ORDER.equals(coloringOrder)){
			for (int i=0; i<networkSize; i++){
				colorNode(i, coloringScheme);
			}
			/*  Tamer: testing
			// Obsolete code: randomizing a node is not needed anymore since we use random coloring strategy for each node
			List<Integer> uncoloredNodes = new ArrayList<Integer>();
			for (int i=0; i<networkSize; i++){
				uncoloredNodes.add(i);
			}
			for (int i=0; i<networkSize; i++){
				Integer randomNode = uncoloredNodes.get((int)Math.floor(Math.random() * uncoloredNodes.size()));
				uncoloredNodes.remove(randomNode);
				colorNode(randomNode, coloringScheme);
			}
			if (!uncoloredNodes.isEmpty()){ //sanity check
				System.out.println(uncoloredNodes.size());
				System.out.println("ERROR: finished coloring and still some nodes are uncolored");
			}
			*/
		} else if (DEGREE_COLORING_ORDER.equals(coloringOrder)){
			List<Integer> degrees = new ArrayList<Integer>(degreeNodeIndex.keySet());
			Collections.sort(degrees);
			for (int i=degrees.size()-1; i>=0; i--){
				List<Integer> nodes = degreeNodeIndex.get(degrees.get(i));
				for (Integer node : nodes){
					colorNode(node, coloringScheme);
				}
			}
		}
	}
	
	protected void colorAllNodes(){
		colorAllNodes(default_coloring_order, default_coloring_scheme);
	}

	protected Integer[] collectKValues() {
		if (colorPooling)
			return currentColoring.nodeKValues;
		Integer[] kValues = new Integer[networkSize];
		for (int i=0; i<networkSize; i++){
			Integer currentK = maxK + 1;
			Boolean shrinking = true;
			// shrink this node's k neighborhood as max as it could get
			while (shrinking){
				currentK --;
				if (kHopAvailableColors(i, currentK).size() > 0){
					shrinking = false;
				}
			}
			kValues[i] = currentK;
		}
		return kValues;
	}
	
	private void colorNode(Integer node, String coloringScheme) {
		if (RANDOM_COLORING_SCHEME.equals(coloringScheme)){
			nodeColors[node] = (int) Math.ceil(pathLength * Math.random());
		} else if (MAX_K_COLORING_SCHEME.equals(coloringScheme)){
			Integer currentK = maxK;
			Boolean kTooLarge = true;
			while (kTooLarge){
				List<Integer> availableColors = kHopAvailableColors(node, currentK);
				if (availableColors.size() == 0){
					currentK --;
				} else {
					nodeColors[node] = availableColors.get((int)Math.floor(availableColors.size() * Math.random()));
					kTooLarge = false;
				}
			}
		}
	}

	protected List<Integer> kHopAvailableColors(Integer node, Integer k) {
		List<Integer> kHopNeigborhood = Arrays.asList(kHopNodes[node][k]); //kHopNodes(node, k, new HashMap<Integer, Boolean>());
		Boolean[] available = new Boolean[pathLength+1];
		available[0] = false;
		for (int i=1; i<available.length; i++){
			available[i] = true;
		}
		for (Integer neighbor : kHopNeigborhood){
			if (nodeColors[neighbor] != null){
				available[nodeColors[neighbor]] = false;
			}
		}
		List<Integer> result = new ArrayList<Integer>();
		for (int i=1; i<available.length; i++){
			if (available[i] == true){
				result.add(i);
			}
		}
		return result;
	}
	
	protected List<Integer> kHopNodes(Integer node, Integer k, HashMap<Integer, Boolean> visited){
		List<Integer> nodes = new ArrayList<Integer>();
		nodes.add(node);
		visited.put(node, true);
		if (k == 0){
			return nodes;
		}
		for (int i=0; i<networkSize; i++){
			if (network[node][i] < infinity-1 && visited.get(i) == null){ // -1 for double precision errors
				nodes.addAll(kHopNodes(i, k-1, visited));
			}
		}
		return nodes;
	}
	
	protected Double successProbability(){
		//collect final k values
		nodeKValues = collectKValues();
		k = Collections.min(Arrays.asList(nodeKValues));
		Double result = 1.0;
		//(m-k)!/(m-k)^(m-k)
		Integer mMinusK = pathLength - k;
		for (int i=mMinusK; i>0; i--){
			result = result * i / (mMinusK);
		}
		return result;
	}

}
