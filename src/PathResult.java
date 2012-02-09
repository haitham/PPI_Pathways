import java.util.PriorityQueue;

public class PathResult {
	PriorityQueue<IterationResult> paths;
	Integer iterationsCount;
	Long runtime;
	public PathResult(PriorityQueue<IterationResult> paths, Integer iterationsCount, Long runtime) {
		this.paths = paths;
		this.iterationsCount = iterationsCount;
		this.runtime = runtime;
	}
}
