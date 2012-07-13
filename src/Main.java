import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
		
//		Integer nonTerminalCount = 0;
//		for (int i=0; i<proteins.size(); i++){
//			if (!membraneProteins.contains(i) && !transcriptionProteins.contains(i))
//				nonTerminalCount ++;
//		}
		
		if (args.length > 0 && args[0].equals("enrichment")){
			HashMap<String, List<String>> annotations = readAnnotations();
			List<String> path = readPath();
			System.out.println(new EnrichmentMeter(proteins, annotations, path).measure());
			return;
		}
		
		if (args.length > 0 && args[0].equals("repeat")){
			Integer pathLength = 4;
			Integer times = 500;
			PathFinder finder = null;
			if (args.length > 1){
				if (args[1].equals("optimalkhop"))
					finder = new OptimalKPathFinder(proteins.size(), edges, membraneProteins, transcriptionProteins);
				else if(args[1].equals("normal"))
					finder = new PathFinder(proteins.size(), edges, membraneProteins, transcriptionProteins);
			}
			if (args.length > 2)
				pathLength = new Integer(args[2]);
			if (args.length > 3)
				times = new Integer(args[3]);
			finder.repeatIteration(pathLength, times);
			return;
		}
		
		List<PathFinder> finders = new ArrayList<PathFinder>();
		List<Integer> lengths = new ArrayList<Integer>();
		List<Double> confidences = new ArrayList<Double>();
		
		if (args.length > 0 && args[0].equals("khopcomparison")){
			if (args.length > 1)
				confidences.add(new Double(args[1]));
			else
				confidences = Arrays.asList(0.7, 0.9, 0.99);
			if (args.length > 2)
				lengths.add(new Integer(args[2]));
			else
				lengths = Arrays.asList(6, 7, 8, 9);
			
			System.out.print("Will try for lengths: ");
			for (Integer length : lengths)
				System.out.print("" + length + " ");
			System.out.print(", For confidences: ");
			for (Double confidence : confidences)
				System.out.print("" + confidence + " ");
			System.out.println();
			System.out.println("======================");
			System.out.println();
			
			for (Integer length : lengths){
				for (Double confidence : confidences){
					List<Integer[]> colorsQueue = new ArrayList<Integer[]>();
					PathFinder finder;
					PathResult result;
					System.out.print("(" + length + ", " + confidence + ") - Khop: ");
					finder = new KHopPathFinder(proteins.size(), edges, membraneProteins, transcriptionProteins);
					result = finder.runWithExtraParams(length, confidence, false, false, true, colorsQueue);
					System.out.print(result.iterationsCount + " iterations, " + result.runtime + " milliseconds");
					
					System.out.print(" - MultiKhop: ");
					finder = new MultiKPathFinder(proteins.size(), edges, membraneProteins, transcriptionProteins);
					result = finder.runWithExtraParams(length, confidence, false, true, false, colorsQueue);
					System.out.print(result.iterationsCount + " iterations, " + result.runtime + " milliseconds");
					
					System.out.println();
				}
			}
			return;
		}
		
		if (args.length == 2 && args[0].equals("multikhop") && args[1].equals("stats")){
			MultiKPathFinder statsFinder = new MultiKPathFinder(proteins.size(), edges, membraneProteins, transcriptionProteins);
			Integer sampleSize = 500;
			Integer pathLength = 10;
			List<MinKPath> paths = statsFinder.runKStats(pathLength, sampleSize);
			return;
		}
		
		if (args.length == 2 && args[0].equals("multikhop") && args[1].equals("k-values-analysis")){
			MultiKPathFinder analysisFinder = new MultiKPathFinder(proteins.size(), edges, membraneProteins, transcriptionProteins);
			Integer sampleSize = 500;
			Integer pathLength = 10;
			String coloringOrder = MultiKPathFinder.RANDOM_COLORING_ORDER;
			String coloringScheme = MultiKPathFinder.MAX_K_COLORING_SCHEME;
			Integer maxK = 1;
			try {
				analysisFinder.runKValuesAnalysis(pathLength, sampleSize, coloringOrder, coloringScheme, maxK);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		
		finders = new ArrayList<PathFinder>();
		
		if (args.length > 3 && args[3].equals("shuffle"))
			edges = shuffledEdges();
		
		if (args.length > 0){
			if (args[0].equals("optimalkhop"))
				finders.add(new OptimalKPathFinder(proteins.size(), edges, membraneProteins, transcriptionProteins));
			if (args[0].equals("minkhop"))
				finders.add(new MinKPathFinder(proteins.size(), edges, membraneProteins, transcriptionProteins));
			if (args[0].equals("khop"))
				finders.add(new KHopPathFinder(proteins.size(), edges, membraneProteins, transcriptionProteins));
			else if (args[0].equals("multikhop"))
				finders.add(new MultiKPathFinder(proteins.size(), edges, membraneProteins, transcriptionProteins));
			else if (args[0].equals("normal"))
				finders.add(new PathFinder(proteins.size(), edges, membraneProteins, transcriptionProteins));
		}
		if (args.length > 1){
			confidences.add(new Double(args[1]));
		}
		if (args.length > 2){
			lengths.add(new Integer(args[2]));
		}
		
		if (lengths.isEmpty())
			lengths = Arrays.asList(6, 7, 8, 9);
		if (confidences.isEmpty())
			confidences = Arrays.asList(0.7, 0.9, 0.99);
		if (finders.isEmpty())
			finders = Arrays.asList(new MultiKPathFinder(proteins.size(), edges, membraneProteins, transcriptionProteins),
									new KHopPathFinder(proteins.size(), edges, membraneProteins, transcriptionProteins),
									new PathFinder(proteins.size(), edges, membraneProteins, transcriptionProteins));
		
		runExperiments(finders, lengths, confidences);
	}
	
	private static void runExperiments(List<PathFinder> finders, List<Integer> lengths, List<Double> confidences){
		System.out.print("Will try: ");
		for (PathFinder finder : finders)
			System.out.print(finder.getClass().getName() + " ");
		System.out.print(", For lengths: ");
		for (Integer length : lengths)
			System.out.print("" + length + " ");
		System.out.print(", For confidences: ");
		for (Double confidence : confidences)
			System.out.print("" + confidence + " ");
		System.out.println();
		System.out.println("======================");
		System.out.println();
		
		PathResult result;
		for (PathFinder finder : finders){
			System.out.println(finder.getClass().getName());
			for (Integer length : lengths){
				for (Double confidence : confidences){
					System.out.print("(" + length + ", " + confidence + "): ");
					result = finder.run(length, confidence);
					System.out.println(result.iterationsCount + " iterations, " + result.runtime + " milliseconds");
					System.out.println("" + result.paths.size() + "pathways");
					IterationResult iteration = null;
					while ((iteration = result.paths.poll()) != null){
						for (Integer node : iteration.path)
							System.out.print(proteins.get(node) + " ");
						System.out.println(iteration.distance);
					}
					System.out.println();
				}
			}
		}
	}
	
	private static HashMap<String, List<String>> readAnnotations(){
		HashMap<String, List<String>> annotations = new HashMap<String, List<String>>();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream("data/annotations.txt"))));
			String line = null;
			while ((line = reader.readLine()) != null){
				String[] parts = line.trim().split("\\s+");
				if (!parts[1].trim().startsWith("GO:"))
					continue;
				List<String> terms = annotations.get(parts[0].trim());
				if (terms == null)
					terms = new ArrayList<String>();
				terms.add(parts[1].trim());
				annotations.put(parts[0].trim(), terms);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return annotations;
	}
	
	private static List<String> readPath(){
		List<String> path = null;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream("data/path.txt"))));
			String line = reader.readLine();
			path = Arrays.asList(line.split("\\s+"));
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return path;
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
			BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream("data/interactions.txt"))));
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
	
	private static ArrayList<InputEdge> shuffledEdges(){
		ArrayList<InputEdge> shuffled = new ArrayList<InputEdge>();
		List<Integer> pool = new ArrayList<Integer>();
		for (InputEdge edge : edges){
			pool.add(edge.to);
		}
		for (InputEdge edge : edges){
			int random = (int) Math.floor(pool.size() * Math.random());
			shuffled.add(new InputEdge(edge.from, pool.get(random), edge.score));
			pool.remove(random);
		}
		if (!pool.isEmpty()) // sanity check
			System.out.println("SOMETHING IS WRONG.. some nodes left out unshuffled");
		return shuffled;
	}
	
}
