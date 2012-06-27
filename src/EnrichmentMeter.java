import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EnrichmentMeter {
	
	public static final String UNIPROTKB = "UniProtKB";
	private HashMap<String, List<String>> proteinAnnotations;
	private HashMap<String, List<String>> termAnnotations;
	private List<String> path;
	
	// testing
	public static void main(String[] args){
//		System.out.println(Math.exp(logCombination(1, 1)));
//		System.out.println(hypergeometric(1500, 0, 15, 1));
		
//		System.out.println(hypergeometric(1500, 300, 15, 10));
//		System.out.println( (1.0/Math.exp(logCombination(1500, 15))) * Math.exp(logCombination(300, 10)) * Math.exp(logCombination((1500-300), (15-10))) );
	}
	
	public EnrichmentMeter(List<String> proteins, HashMap<String, List<String>> annotations, List<String> path){
		this.path = path;
		proteinAnnotations = new HashMap<String, List<String>>();
		termAnnotations = new HashMap<String, List<String>>();
		for (String protein : proteins){
			List<String> terms = annotations.get(protein);
			if (terms == null)
				terms = new ArrayList<String>();
			proteinAnnotations.put(protein, terms);
			for (String term : terms){
				List<String> myProteins = termAnnotations.get(term);
				if (myProteins == null)
					myProteins = new ArrayList<String>();
				myProteins.add(protein);
				termAnnotations.put(term, myProteins);
			}
		}
	}
	
	public Double measure(){
		Double minEnrichment = 1.01;
		for (String term : termAnnotations.keySet()){
			Double enrichment = measureTerm(term);
			if (enrichment < minEnrichment)
				minEnrichment = enrichment;
		}
		return minEnrichment;
	}
	
	private Double measureTerm(String term){
		Integer annotatedSize = 0;
		for (String protein : path){
			if (proteinAnnotations.get(protein).contains(term))
				annotatedSize += 1;
		}
		Double enrichment = 0.0;
		for (int i=annotatedSize; i<=path.size(); i++){
			enrichment += hypergeometric(proteinAnnotations.keySet().size(), termAnnotations.get(term).size(), path.size(), i);
		}
		return enrichment;
	}
	
	// Hypergeometric P(X = successfulDraws | population, successfulpopulation, draws)
	public static Double hypergeometric(Integer population, Integer successPopulation, Integer draws, Integer successfulDraws){
		Double result = logCombination(successPopulation, successfulDraws);
		result += logCombination((population - successPopulation), (draws - successfulDraws));
		result -= logCombination(population, draws);
		return Math.exp(result);
	}
	
	// ln(n C r)
	public static Double logCombination(Integer n, Integer r){
		return logPermutation(n, r) - logPermutation(r, r);
	}
	
	// ln(n P r)
	public static Double logPermutation(Integer n, Integer r){
		Double result = 0.0;
		for (int i=0; i<r; i++)
			result += Math.log(n - i);
		return result;
	}
	
}
