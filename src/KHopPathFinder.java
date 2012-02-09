import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class KHopPathFinder extends PathFinder {
	
	private Integer k;
	private Integer initialK;

	public KHopPathFinder(Integer size, List<InputEdge> edges, List<Integer> startNodes, List<Integer> endNodes) {
		super(size, edges, startNodes, endNodes);
		initialK = 4;
	}
	
	protected void colorAllNodes(){
		k = initialK;
		Boolean colorSuccess = false;
		while (!colorSuccess){
			nodeColors = new Integer[networkSize];
			colorSuccess = true; //default
			for (int i=0; i<networkSize; i++){
				List<Integer> availableColors = kHopAvailableColors(i, k);
				if (availableColors.size() == 0){
					k --;
					colorSuccess = false;
					break;
				} else {
					nodeColors[i] = availableColors.get((int)Math.floor(availableColors.size() * Math.random()));
				}
			}
		}
	}

	private List<Integer> kHopAvailableColors(Integer node, Integer k) {
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
	
	private List<Integer> kHopNodes(Integer node, Integer k, HashMap<Integer, Boolean> visited){
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
		Double result = 1.0;
		//(m-k)!/(m-k)^(m-k)
		Integer mMinusK = pathLength - k;
		for (int i=mMinusK; i>0; i--){
			result = result * i / (mMinusK);
		}
		return result;
	}

}
