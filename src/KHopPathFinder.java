import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class KHopPathFinder extends PathFinder {
	
	private Integer k;
//	protected Integer initialK;
	protected Integer maxK;
	protected HashMap<Integer, List<Integer>> degreeNodeIndex;
	protected Integer[] nodeKValues;
	
	public static final String RANDOM_COLORING_ORDER = "RandomOrder";
	public static final String DEGREE_COLORING_ORDER = "HighestDegreeFirst";
	public static final String RANDOM_COLORING_SCHEME = "RandomColor";
	public static final String MAX_K_COLORING_SCHEME = "MaxKColor";
	protected static String default_coloring_order = RANDOM_COLORING_ORDER;
	protected static String default_coloring_scheme = RANDOM_COLORING_SCHEME;

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
	
	protected void colorAllNodes(String coloringOrder, String coloringScheme) {
		nodeColors = new Integer[networkSize];
		if (RANDOM_COLORING_ORDER.equals(coloringOrder)){
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
		//collect final k values
		nodeKValues = collectKValues();
	}
	
	protected void colorAllNodes(){
		colorAllNodes(default_coloring_order, default_coloring_scheme);
	}

	private Integer[] collectKValues() {
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
		List<Integer> kHopNeigborhood = kHopNodes(node, k, new HashMap<Integer, Boolean>());
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
		//TODO calculate k
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
