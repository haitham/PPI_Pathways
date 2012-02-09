import java.util.ArrayList;
import java.util.List;


public class ComparableList <T extends Comparable> extends ArrayList <T> implements Comparable<ComparableList<T>> {

	public ComparableList() {
		super();
	}

	public ComparableList(List<T> subset) {
		super(subset);
	}

	public int compareTo(ComparableList<T> other) {
		if (this.size() < other.size()){
			return -1;
		}
		if (this.size() > other.size()){
			return 1;
		}
		for (int i=0; i<this.size(); i++){
			if (this.get(i).compareTo(other.get(i)) < 0){
				return -1;
			}
			if (this.get(i).compareTo(other.get(i)) > 0){
				return 1;
			}
		}
		return 0;
	}
	
	public ComparableList<T> cloneWithout(T alien){
		ComparableList<T> result = new ComparableList<T>();
		for (T t : this){
			if (!alien.equals(t)){
				result.add(t);
			}
		}
		return result;
	}

}
