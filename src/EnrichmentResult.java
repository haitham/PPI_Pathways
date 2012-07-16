public class EnrichmentResult {
	private String term;
	private Double enrichment;
	
	public EnrichmentResult(String term, Double enrichment) {
		this.term = term;
		this.enrichment = enrichment;
	}
	
	public String term(){
		return this.term;
	}
	
	public Double enrichment(){
		return this.enrichment;
	}
}
