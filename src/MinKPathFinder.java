import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.swing.text.DefaultEditorKit.CutAction;

public class MinKPathFinder extends MultiKPathFinder {
	
	public MinKPathFinder(Integer size, List<InputEdge> edges, List<Integer> startNodes, List<Integer> endNodes) {
		super(size, edges, startNodes, endNodes);
	}
	
	private class ReachableNode{
		Integer id;
		List<Integer> loopNodes;
		List<List<Integer>> paths;
		public ReachableNode(Integer id, List<Integer> loopNodes) {
			this.id = id;
			this.loopNodes = loopNodes;
		}
		
		public ReachableNode(Integer id, List<List<Integer>> paths, List<Integer> loopNodes) {
			this.id = id;
			this.loopNodes = loopNodes;
			this.paths = paths;
		}
	}
	
	protected Double successProbability(){
		//collect final k values
		nodeKValues = collectKValues();
		//Hashes for representing reachable sets in 0:pathLength hops from start and end
		List<HashMap<Integer, ReachableNode>> startReachableNodes = reachableNodes(startNodes);
		List<HashMap<Integer, ReachableNode>> endReachableNodes = reachableNodes(endNodes);
		//Candidate lists: sorted intersection lists of source reachable nodes in i hops and end reachable nodes in pathLegth-i-1 hops
		List<List<Integer>> candidateLists = getCandidateLists(startReachableNodes, endReachableNodes);
		////////////////////////////
		//  PRINTING
		try{
			FileOutputStream oStream = new FileOutputStream("data/hoplists.txt");
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new DataOutputStream(oStream)));
			for (List<Integer> candidateList : candidateLists){
				for (Integer candidate : candidateList){
					writer.write("" + candidate + ":" + nodeKValues[candidate] + " ");
				}
				writer.write("\n");
			}
			writer.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		////////////////////////////
		//extracting candidates
		Integer[] placement = new Integer[pathLength];
		for (int i=0; i<pathLength; i++)
			placement[i] = candidateLists.get(i).get(0);
		//continuing with chromatic number and success probability
		long numberOfColorings = numberOfColorings(placement);
		Double probability = 1.0/numberOfColorings;
		for (int i=1; i<=pathLength; i++){ // factorial of pathlength
			probability = probability * i;
		}
		return 1.0;
	}
	
	private List<List<Integer>> getCandidateLists(List<HashMap<Integer, ReachableNode>> startReachableNodes, List<HashMap<Integer, ReachableNode>> endReachableNodes) {
		List<List<Integer>> candidateLists = new ArrayList<List<Integer>>();
		for (int i=0; i<pathLength; i++){
			List<Integer> candidates = new ArrayList<Integer>();
			for (Integer startHopNode : startReachableNodes.get(i).keySet()){
				for (Integer endHopNode : endReachableNodes.get(pathLength-i-1).keySet()){
					if (startHopNode.equals(endHopNode)){ // match - add to candidates
						boolean added = false;
						for (int k=0; k<candidates.size(); k++){
							if (nodeKValues[candidates.get(k)] > nodeKValues[startHopNode]){
								candidates.add(k, startHopNode);
								added = true;
								break;
							}
						}
						if (!added)
							candidates.add(startHopNode);
						break;
					}
				}
			}
			candidateLists.add(candidates);
		}
		return candidateLists;
	}

	private List<HashMap<Integer, ReachableNode>> reachableNodes(List<Integer> fromNodes){
		final Integer PATH_LISTS_LIMIT = pathLength;
		
		List<HashMap<Integer, ReachableNode>> reachableNodes = new ArrayList<HashMap<Integer, ReachableNode>>();
		//fromNodes: reachable in zero hops
		HashMap<Integer, ReachableNode> currentNodes = new HashMap<Integer, ReachableNode>();
		for (Integer node : fromNodes)
			currentNodes.put(node, new ReachableNode(node, new ArrayList<List<Integer>>() , new ArrayList<Integer>()));
		reachableNodes.add(currentNodes);
		
		// building the hop lists that keep track of full paths
		System.out.println("hops\tnodes\tpaths\ttime");
		for (int i=0; i<PATH_LISTS_LIMIT; i++){
			long start = System.currentTimeMillis();
			currentNodes = new HashMap<Integer, ReachableNode>();
			HashMap<Integer, ReachableNode> previousNodes = reachableNodes.get(i);
			for (Integer previous : previousNodes.keySet()){
				List<List<Integer>> previousPaths = previousNodes.get(previous).paths;
				for (List<Integer> path : previousPaths){
					path.add(previous);
				}
				if (previousPaths.isEmpty()){
					List<Integer> path = new ArrayList<Integer>();
					path.add(previous);
					previousPaths.add(path);
				}
				
				for (int j=0; j<networkSize; j++){
					// check if node can be reached with no loops
					List<List<Integer>> looplessPaths = new ArrayList<List<Integer>>();
					for (List<Integer> path : previousPaths){
						if (!path.contains(j))
							looplessPaths.add(path);
					}
					if (looplessPaths.isEmpty()) // all previous path close a loop if this node is added
						continue;
					// adding this node with its loopless paths to the next hop
					ReachableNode node = currentNodes.get(j);
					if (node != null)
						looplessPaths.addAll(node.paths);
					currentNodes.put(j, new ReachableNode(j, looplessPaths, new ArrayList<Integer>()));
				}
			}
			reachableNodes.add(currentNodes);
			long end = System.currentTimeMillis();
			Integer pathCount = 0;
			for (Integer node : currentNodes.keySet()){
				pathCount += currentNodes.get(node).paths.size();
			}
			System.out.println("" + (i+1) + "\t" + currentNodes.keySet().size() + "\t" + pathCount + "\t" + (end-start));
		}
		
		//building the rest of hops
		for (int i=PATH_LISTS_LIMIT; i<pathLength-1; i++){
			//collecting all adjacent nodes to all nodes in previous hop
			currentNodes = new HashMap<Integer, ReachableNode>();
			HashMap<Integer, ReachableNode> previousNodes = reachableNodes.get(i);
			for (Integer previous : previousNodes.keySet()){
				List<Integer> previousLoopNodes = previousNodes.get(previous).loopNodes;
				previousLoopNodes.add(previous);
				for (int j=0; j<networkSize; j++){
					if (network[previous][j] < infinity - 1){
						if (previousLoopNodes.contains(j)) //closing a loop for sure
							continue;
						ReachableNode node = currentNodes.get(j);
						if (node == null){ //record it with previous nodes
							currentNodes.put(j, new ReachableNode(j, previousLoopNodes));
						} else { //take intersection of previous nodes
							List<Integer> currentLoopNodes = new ArrayList<Integer>();
							for (Integer loopNode : previousLoopNodes){
								if (node.loopNodes.contains(loopNode))
									currentLoopNodes.add(loopNode);
							}
							currentNodes.put(j, new ReachableNode(j, currentLoopNodes));
						}
					}
				}
			}
			reachableNodes.add(currentNodes);
		}
		return reachableNodes;
	}
}
