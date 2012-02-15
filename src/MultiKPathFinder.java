import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MultiKPathFinder extends KHopPathFinder {
	
	private Integer[] nodeKValues;
	private Boolean[] terminal;
	
	public MultiKPathFinder(Integer size, List<InputEdge> edges, List<Integer> startNodes, List<Integer> endNodes) {
		super(size, edges, startNodes, endNodes);
		terminal = new Boolean[size];
		for (int i=0; i<size; i++){
			terminal[i] = false;
		}
		for (Integer node: startNodes){
			terminal[node] = true;
		}
		for (Integer node: endNodes){
			terminal[node] = true;
		}
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
	
	protected void colorAllNodes(){
		nodeColors = new Integer[networkSize];
		for (int i=0; i<networkSize; i++){
			colorNode(i);
		}
		//collect final k values
		nodeKValues = collectKValues();
	}

	private Integer[] collectKValues() {
		Integer[] kValues = new Integer[networkSize];
		for (int i=0; i<networkSize; i++){
			Integer currentK = initialK + 1;
			Boolean shrinking = true;
			// expand this node's k neighborhood as max as it could get
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

	//colors the node trying to max its k value
	private void colorNode(int node) {
		Integer currentK = initialK;
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
				if (nodeKValues[node] < nodeKValues[j]){
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
				if (nodeKValues[node] < nodeKValues[j]){
					innerNodes.add(j, node);
					innerNodes.remove(innerNodes.size()-1);
				}
			}
			node ++;
		}
		return innerNodes;
	}
	
}
