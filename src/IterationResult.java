import java.util.List;

public class IterationResult {
	List<Integer> path;
	Double distance;
	Double successProbability;
	public IterationResult(List<Integer> path, Double distance, Double successProbability) {
		this.path = path;
		this.distance = distance;
		this.successProbability = successProbability;
	}
	public boolean equals(Object other){
		IterationResult otherResult = ((IterationResult)other);
		if (this.path.size() != otherResult.path.size()){
			return false;
		}
		for (int i=0; i<path.size(); i++){
			if (this.path.get(i) != otherResult.path.get(i)){
				return false;
			}
		}
		return true;
	}
	
}
