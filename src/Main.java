import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class Main {
	
	private static List<Integer> membraneProteins;
	private static List<Integer> transcriptionProteins;
	private static HashMap<String, Integer> proteinIndex;
	private static List<String> proteins;
	private static ArrayList<InputEdge> edges;

	public static void main(String[] args) {
		membraneProteins = new ArrayList<Integer>();
		transcriptionProteins = new ArrayList<Integer>();
		proteinIndex = new HashMap<String, Integer>();
		proteins = new ArrayList<String>();
		edges = new ArrayList<InputEdge>();
		
		readInteractions();
		readTerminals();
		PathFinder finder = null;
		
		if (args.length == 2 && args[0].equals("multikhop") && args[1].equals("stats")){
			MultiKPathFinder statsFinder = new MultiKPathFinder(proteins.size(), edges, membraneProteins, transcriptionProteins);
			Integer sampleSize = 1000;
			Integer pathLength = 6;
			List<MinKPath> paths = statsFinder.runKStats(pathLength, sampleSize);
			return;
		}
		
		if (args.length > 0 && args[0].equals("khop")){
			System.out.println("K-Hop");
			 finder = new KHopPathFinder(proteins.size(), edges, membraneProteins, transcriptionProteins);
//			PathFinder[] randomFinders = new PathFinder[20];
//			for (int i=0; i<randomFinders.length; i++){
//				randomFinders[i] = new KHopPathFinder(proteins.size(), shuffledEdges(), membraneProteins, transcriptionProteins);
//			}
		} else if (args.length > 0 && args[0].equals("multikhop")){
			
		} else {
			System.out.println("Normal");
			finder = new PathFinder(proteins.size(), edges, membraneProteins, transcriptionProteins);
//			PathFinder[] randomFinders = new PathFinder[20];
//			for (int i=0; i<randomFinders.length; i++){
//				randomFinders[i] = new PathFinder(proteins.size(), shuffledEdges(), membraneProteins, transcriptionProteins);
//			}
		}
		
		PathResult result;
		List<Integer> lengths = Arrays.asList(/*6, 7,*/ 8);
		List<Double> confidences = Arrays.asList(/*0.7, 0.9,*/ 0.99);
		
		for (Integer length : lengths){
			for (Double confidence : confidences){
				System.out.print("(" + length + ", " + confidence + "): ");
				result = finder.run(length, confidence);
//				Integer randomWins = 0;
//				for (int i=0; i<randomFinders.length; i++){
//					PathResult randomResult = randomFinders[i].run(length, confidence);
//					if (randomResult.paths.peek().distance < result.paths.peek().distance){
//						randomWins ++;
//					}
//				}
				System.out.print(result.iterationsCount + " iterations, " + result.runtime + " milliseconds");
				System.out.println();
			}
		}
		
	}
	
	private static void readTerminals(){
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream("data/membrane.txt"))));
			String line = null;
			while ((line = reader.readLine()) != null){
				Integer protein = proteinIndex.get(line.trim());
				if (protein != null){
					membraneProteins.add(protein);
				}
			}
			reader.close();
			reader = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream("data/transcription.txt"))));
			while ((line = reader.readLine()) != null){
				Integer protein = proteinIndex.get(line.trim());
				if (protein != null){
					transcriptionProteins.add(protein);
				}
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void readInteractions(){
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream("data/homosapiens_custom.txt"))));
			String line = null;
			Integer proteinCounter = 0;
			while ((line = reader.readLine()) != null){
				String[] parts = line.split("\\s+");
				Integer from = proteinIndex.get(parts[0].trim());
				if (from == null){
					from = proteinCounter;
					proteins.add(parts[0].trim());
					proteinIndex.put(parts[0], from);
					proteinCounter ++;
				}
				Integer to = proteinIndex.get(parts[1].trim());
				if (to == null){
					to = proteinCounter;
					proteins.add(parts[1].trim());
					proteinIndex.put(parts[1], to);
					proteinCounter ++;
				}
				edges.add(new InputEdge(from, to, new Double(parts[2].trim())));
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static List<InputEdge> shuffledEdges(){
		List<InputEdge> shuffledEdges = new ArrayList<InputEdge>();
		List<Integer[]> pairs = new ArrayList<Integer[]>();
		for (int i=0; i<proteins.size(); i++){
			for (int j=i+1; j<proteins.size(); j++){
				pairs.add(new Integer[]{i, j});
			}
		}
		for (InputEdge edge : edges){
			Integer[] pair = pairs.remove((int) Math.floor(pairs.size() * Math.random()));
			shuffledEdges.add(new InputEdge(pair[0], pair[1], edge.score));
		}
		return shuffledEdges;
	}
	
}
