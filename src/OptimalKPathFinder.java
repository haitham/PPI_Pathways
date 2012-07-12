import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OptimalKPathFinder extends MultiKPathFinder {

	public OptimalKPathFinder(Integer size, List<InputEdge> edges,
			List<Integer> startNodes, List<Integer> endNodes) {
		super(size, edges, startNodes, endNodes);
	}

	public void repeatIteration(Integer pathLength, Integer times){
		initialize(pathLength);
//		initializeColorPooling();
		Double failureProbability = 1.0;
//		Double sharanFailureProbability = 1.0;
//		Double sharanConstant = sharanSuccessProbability();
//		Double minDistance = infinity;
		Long start = System.currentTimeMillis();
		for (int i=0; i<times; i++){
//			Long startIteration = System.currentTimeMillis();
			IterationResult result = iteration();
			Long endIteration = System.currentTimeMillis();
			//update failure probability
			failureProbability = failureProbability * (1 - result.successProbability);
//			sharanFailureProbability = sharanFailureProbability * (1 - sharanConstant);
//			if (result.distance < minDistance){
//				minDistance = result.distance;
//			}
			System.out.println("" + i /*+ "\t" + (1.0-sharanFailureProbability)*/ + "\t" + (1.0-failureProbability) /*+ "\t" + minDistance + "\t" + result.distance + "\t" + result.path.toString() */+ "\t" + (endIteration - start));
		}
	}
	
	protected Double sharanSuccessProbability(){
		Double result = 1.0;
		//m!/m^m
		for (int i=pathLength; i>0; i--){
			result = result * i / pathLength;
		}
		return result;
	}
	
	protected IterationResult iteration(){
//		Long iterationStart = System.currentTimeMillis();
//		long coloringTime = 0;
		// Color all nodes
		if (consumingColors){
			if (colorsQueue.isEmpty())
				colorAllNodes();
			else
				nodeColors = colorsQueue.remove(0);
		} else {
//			Long coloringStart = System.currentTimeMillis();
			colorAllNodes();
//			coloringTime = System.currentTimeMillis() - coloringStart;
			if (producingColors)
				colorsQueue.add(nodeColors);
		}

		if (!tabulating){
			//sanity check
			System.out.println("Error: not tabulating");
			return null;
		}
		
		//run DP
		List<Integer> path = tabulate();
		
		Integer placement[] = new Integer[path.size()];
		placement = path.toArray(placement);
		
		// calculate success probability of this iteration
		Double successProbability = successProbability(placement);
//		Long iterationTime = System.currentTimeMillis() - iterationStart;
//		System.out.println("" + iterationTime + "\t" + coloringTime);
		
		return new IterationResult(path, minDistance[colorSets.size()-1][path.get(path.size()-1)], successProbability);
	}

	protected Double successProbability(Integer[] path) {
		// collect final k values
		nodeKValues = collectKValues();
		Double probability = 1.0 / numberOfColorings(path);
		for (int i = 1; i <= pathLength; i++) { // factorial of pathlength
			probability = probability * i;
		}
		return probability;
	}

}
