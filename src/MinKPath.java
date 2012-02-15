import java.util.List;

public class MinKPath {
	private Integer startNode;
	private Integer endNode;
	private List<Integer> innerNodes;
	
	public MinKPath(Integer start, Integer end, List<Integer> inner) {
		this.startNode = start;
		this.endNode = end;
		this.innerNodes = inner;
	}

	public Integer getStartNode() {
		return startNode;
	}

	public Integer getEndNode() {
		return endNode;
	}

	public List<Integer> getInnerNodes() {
		return innerNodes;
	}
}
