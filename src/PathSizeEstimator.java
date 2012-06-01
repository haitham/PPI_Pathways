import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PathSizeEstimator {
	
	Double[][] network;
	List<Integer> startNodes;
	List<Integer> endNodes;
	Integer maxLength;
	boolean[] isEnd;
	List<List<Integer>> adjacency;
	
	public PathSizeEstimator(Double[][] network, List<Integer> startNodes, List<Integer> endNodes, Integer maxLength) {
		this.network = network.clone();
		this.startNodes = startNodes;
		this.endNodes = endNodes;
		this.maxLength = maxLength;
		adjacency = new ArrayList<List<Integer>>();
		for (int i=0; i<network.length; i++){
			List<Integer> adjacent = new ArrayList<Integer>();
			for (int j=0; j<network[i].length; j++){
				if (network[i][j] < PathFinder.infinity - 1)
					adjacent.add(j);
			}
			adjacency.add(adjacent);
		}
		this.isEnd = new boolean[network.length];
		for (int i=0; i<isEnd.length; i++)
			isEnd[i] = false;
		for (Integer node : endNodes)
			isEnd[node] = true;
	}
	
	public Integer numberOfPaths(){
		
		
		return null;
	}
	
	private HashMap<Integer, Double> lengthDistribution(){
		while (true){
			
		}
	}
	
}
