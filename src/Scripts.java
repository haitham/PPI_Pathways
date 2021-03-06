import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;


public class Scripts {

	public static void main(String[] args) {
//		millisecondsToSeconds("data/times/mint_rat");
//		consolidateParallel("data/iterations/mint_hsa_6/parallel", 21, 2);
//		filterParallel("data/iterations/mint_hsa_8/parallel", 125);
		summarizeIterations("mint_hsa_8", 500, 20, 2, 0.2207872834051713);
//		restoreGODatabase();
//		refineMint("10116");
	}
	
	private static class Iteration{
		Double oldProbablity;
		Double newProbablity;
		Double bestDistance;
		Double success;
		
		public Iteration(Double oldProbability, Double newProbability, Double bestDistance) {
			this.oldProbablity = oldProbability;
			this.newProbablity = newProbability;
			this.bestDistance = bestDistance;
		}
	}
	
	public static void millisecondsToSeconds(String dir){
		List<String> filenames = Arrays.asList(new File(dir).list());
		try {
			for (String filename : filenames){
				if (!filename.endsWith(".out"))
					continue;
				BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(dir + "/" + filename))));
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new DataOutputStream(new FileOutputStream(dir + "/seconds/" + filename))));
				String line = null;
				while ((line = reader.readLine()) != null){
					String[] parts = line.split("\\s+");
					Double milli = new Double(parts[parts.length-1]);
					parts[parts.length-1] = "" + milli/1000.0;
					for (int i=0; i<parts.length; i++){
						writer.write(parts[i] + "\t");
					}
					writer.write("\n");
				}
				reader.close();
				writer.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void consolidateParallel(String dir, Integer outName, Integer batchSize){
		List<String> filenames = Arrays.asList(new File(dir).list());
		try {
			Integer fileCount = 0;
			Integer iterationNumber = 0;
			Double sharan = 0.0;
			Double me = 0.0;
			Double min = 1000000.0;
			Integer outCount = 0;
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new DataOutputStream(new FileOutputStream(dir + "/consolidated/" + outName + ".out"))));
			for (String filename : filenames){
				if (filename.equals("consolidated"))
					continue;
				fileCount ++;
				BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(dir + "/" + filename))));
				String line = null;
				while ((line = reader.readLine()) != null){
					String[] parts = line.split("\\s+");
					sharan = 1.0 - (1.0 - sharan)*(1.0 - new Double(parts[1]));
					me = 1.0 - (1.0 - me)*(1.0 - new Double(parts[2]));
					Double score = new Double(parts[3]);
					if (score < min)
						min = score;
					writer.write(iterationNumber + "\t" + sharan + "\t" + me + "\t" + min + "\t" + parts[4] + "\n");
					iterationNumber ++;
				}
				reader.close();
				if (fileCount == batchSize){
					outCount ++;
					iterationNumber = 0;
					System.out.println(outCount);
					sharan = 0.0;
					me = 0.0;
					min = 1000000.0;
					outName ++;
					writer.close();
					writer = new BufferedWriter(new OutputStreamWriter(new DataOutputStream(new FileOutputStream(dir + "/consolidated/" + outName + ".out"))));
					fileCount = 0;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void summarizeIterations(String dir, Integer iterationsCount, Integer experimentsCount, Integer skipper, Double targetDistance){
		Iteration[][] iterations = new Iteration[iterationsCount][experimentsCount];
		try {
			BufferedReader[] readers = new BufferedReader[experimentsCount];
			for (int i=0; i<experimentsCount; i++){
//				String order = "" + (i+1);
//				while (order.length() != ("" + experimentsCount).length())
//					order = "0" + order;
				readers[i] = new BufferedReader(new InputStreamReader(new FileInputStream("data/iterations/" + dir + "/" + (i+1) + ".out")));
			}
			for (int i=0; i<iterationsCount; i++){
				for (int e=0; e<experimentsCount; e++){
					try{
						String line = readers[e].readLine();
						String[] parts = line.trim().split("\\s+");
						Double oldProbability = new Double(parts[1].trim());
						Double newProbability = new Double(parts[2].trim());
						Double bestDistance = new Double(parts[3].trim());
						iterations[i][e] = new Iteration(oldProbability, newProbability, bestDistance);
					} catch (Exception ex){
						ex.printStackTrace();
					}
				}
			}
			for (int i=0; i<experimentsCount; i++){
				readers[i].close();
			}
			for (int i=0; i<iterationsCount; i++){
				for (int e=0; e<experimentsCount; e++){
					if (iterations[i][e].bestDistance <= (targetDistance + 0.0000001))
						iterations[i][e].success = 1.0;
					else
						iterations[i][e].success = 0.0;
				}
			}
			for (int i=0; i<iterationsCount; i++){
				Double newProbability = 0.0;
				Double oldProbability = 0.0;
				Double success = 0.0;
				List<Double> probs = new ArrayList<Double>();
				for (int e=0; e<experimentsCount; e++){
					newProbability += iterations[i][e].newProbablity;
					oldProbability += iterations[i][e].oldProbablity;
					success += iterations[i][e].success;
					probs.add(iterations[i][e].newProbablity);
				}
				Collections.sort(probs);
				Double minSkipper = 0.0;
				for (int k=skipper; k<experimentsCount; k++)
					minSkipper += probs.get(k);
				Double maxSkipper = 0.0;
				for (int k=0; k<experimentsCount-skipper; k++)
					maxSkipper += probs.get(k);
				
				oldProbability = oldProbability / (1.0 * experimentsCount);
				newProbability = newProbability / (1.0 * experimentsCount);
				success = success / (1.0 * experimentsCount);
				minSkipper = minSkipper / (1.0 * (experimentsCount - skipper));
				maxSkipper = maxSkipper / (1.0 * (experimentsCount - skipper));
				System.out.println("" + oldProbability + "\t" + newProbability + "\t" + minSkipper + "\t" + maxSkipper + "\t" + success);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void countRepeatedPairs(){
		try{
			FileInputStream iStream = new FileInputStream("data/homosapiens_custom.txt");
			BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(iStream)));
			String line = null;
			HashMap<String, Integer> hash = new HashMap<String, Integer>();
			HashMap<Integer, Integer> counts = new HashMap<Integer, Integer>();
			while ((line = reader.readLine()) != null){
				String[] parts = line.trim().split("\\s+");
				String key = parts[0] + ";;" + parts[1];
				Integer count = hash.get(key);
				if (count == null){
					count = hash.get(parts[1] + ";;" + parts[0]);
				}
				if (count == null)
					count = 0;
				count ++;
				hash.put(key, count);
			}
			reader.close();
			for (String pair : hash.keySet()){
				Integer count = hash.get(pair);
				Integer total = counts.get(count);
				if (total == null)
					total = 0;
				total ++;
				counts.put(count, total);
			}
			for (Integer count : counts.keySet())
				System.out.println("" + count + "\t" + counts.get(count));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void probabilityHistogram(){
		try {
			Integer count = 0;
			HashMap<String, Integer> histogram = new HashMap<String, Integer>();
			BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream("data/samplescores.txt"))));;
			String line = null;
			while ((line = reader.readLine()) != null){
				line = line.trim();
				Integer freq = histogram.get(line);
				if (freq == null)
					freq = 0;
				freq ++;
				histogram.put(line, freq);
				count++;
			}
			List<String> scores = new ArrayList<String>(histogram.keySet());
			Collections.sort(scores);
			for (String score : scores){
				System.out.println(score);
			}
			for (String score : scores){
				System.out.println(1.0*histogram.get(score)/count);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void restoreGODatabase(){
		for (File file: new File("C:/Users/hgabr/Desktop/go_201207-assocdb-tables").listFiles()){
			if (file.getName().indexOf(".sql") > 0){
				try {
					Process process = Runtime.getRuntime().exec("cmd /c mysql -u root go < " + "c:\\Users\\hgabr\\Desktop\\go_201207-assocdb-tables\\" + file.getName());
					System.out.println(process.waitFor());
					BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
					String line = null;
					while ((line = reader.readLine()) != null){
						System.out.println(line);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		for (File file: new File("C:/Users/hgabr/Desktop/go_201207-assocdb-tables").listFiles()){
			if (file.getName().indexOf(".txt") > 0){
				try {
					System.out.println(file.getName());
					Process process = Runtime.getRuntime().exec("cmd /c mysqlimport -u root -L go " + "c:\\Users\\hgabr\\Desktop\\go_201207-assocdb-tables\\" + file.getName());
					System.out.println(process.waitFor());
					BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
					String line = null;
					while ((line = reader.readLine()) != null){
						System.out.println(line);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void extractDipMintIntersection(){
		try{
			HashMap<String, String> mint = new HashMap<String, String>();
			FileInputStream iStream = new FileInputStream("data/mint_hsa.txt");
			BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(iStream)));
			String line = null;
			while ((line = reader.readLine()) != null){
				String[] parts = line.trim().split("\\s+");
				mint.put(parts[0] + ";;" + parts[1], parts[2]);
			}
			reader.close();
			iStream = new FileInputStream("data/dip_hsa.txt");
			reader = new BufferedReader(new InputStreamReader(new DataInputStream(iStream)));
			FileOutputStream oStream = new FileOutputStream("data/homosapiens_custom.txt");
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new DataOutputStream(oStream)));
			while ((line = reader.readLine()) != null){
				String[] parts = line.trim().split("\\s+");
				String score1 = mint.get(parts[0] + ";;" + parts[1]);
				String score2 = mint.get(parts[1] + ";;" + parts[0]);
				if (score1 != null)
					writer.write(parts[0] + "\t" + parts[1] + "\t" + score1 + "\n");
				else if (score2 != null)
					writer.write(parts[0] + "\t" + parts[1] + "\t" + score2 + "\n");
			}
			writer.close();
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void sifToCustom(){
		try{
			FileInputStream iStream = new FileInputStream("data/homosapiens_custom.txt.sif");
			BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(iStream)));
			FileOutputStream oStream = new FileOutputStream("data/homosapiens_custom.txt");
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new DataOutputStream(oStream)));
			String line = null;
			while ((line = reader.readLine()) != null){
				String[] parts = line.trim().split("\\s+");
				writer.write(parts[0] + "\t" + parts[2] + "\t" + parts[1] + "\n");
			}
			writer.close();
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void refineMint(String taxaId){
		try{
			FileInputStream iStream = new FileInputStream("data/mint/" + taxaId + "_all.graph");
			BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(iStream)));
			FileOutputStream oStream = new FileOutputStream("data/mint_custom.txt");
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new DataOutputStream(oStream)));
			String line = null;
			while ((line = reader.readLine()) != null){
				String[] parts = line.trim().split("\\s+");
				if (!parts[2].equals("-"))
					writer.write(parts[0] + "\t" + parts[1] + "\t" + parts[2] + "\n");
			}
			writer.close();
		} catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	public static void filterHomosapiens(){
		try{
			FileInputStream iStream = new FileInputStream("data/homosapiens_dip.txt");
			BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(iStream)));
			FileOutputStream oStream = new FileOutputStream("data/homosapiens_custom.txt");
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new DataOutputStream(oStream)));
			String line = reader.readLine(); // header line
			HashMap<String, Double> scores = new HashMap<String, Double>();
			HashMap<String, Boolean> ids = new HashMap<String, Boolean>();
			while ((line = reader.readLine()) != null){
				String[] parts = line.split("\t+");
				if (parts.length > 17){ //sanity check
					System.out.println("ERROR! more than 16 column: " + parts.length + " parts");
					for (int i=0; i<parts.length; i++){
						System.out.println(parts[i]);
					}
					return;
				}
				if (parts[14].trim().indexOf("dip-quality-status:") < 0){ // sanity check
					System.out.println("ERROR! no status: " + parts[14]);
					return;
				}
				if (parts[15].trim().split("dip:").length == 1){ // sanity check
					System.out.println("ERROR! non-dip score: " + parts[15]);
					return;
				}
				if (parts[0].indexOf("uniprotkb:") > -1 && parts[1].indexOf("uniprotkb:") > -1 && parts[9].indexOf("taxid:9606") > -1 && parts[10].indexOf("taxid:9606") > -1){
					String[] protein1Ids = parts[0].trim().split("\\|");
					String id1 = null;
					for (int i=0; i<protein1Ids.length; i++){
						if (protein1Ids[i].indexOf("uniprotkb:") > -1){
							id1 = protein1Ids[i].trim().split(":")[1];
							break;
						}
					}
					String[] protein2Ids = parts[1].trim().split("\\|");
					String id2 = null;
					for (int i=0; i<protein2Ids.length; i++){
						if (protein2Ids[i].indexOf("uniprotkb:") > -1){
							id2 = protein2Ids[i].trim().split(":")[1];
							break;
						}
					}
					if (id1.compareTo(id2) == 0){ // same protein!
						continue;
					} else if (id1.compareTo(id2) < 0){
						String temp = id1;
						id1 = id2;
						id2 = temp;
					}
					ids.put(id1, true);
					ids.put(id2, true);
					Double score = scores.get(id1 + "+" + "id2");
					if (score != null){ //sanity check
						System.out.println("ERROR! repeated interaction: " + id1 + " " + id2);
					}
					scores.put(id1 + "+" + id2, new Double(parts[15].trim().split("dip:")[1].split("\\|")[0].trim().split("\\(")[0].trim()));
				}
			}
			for (String id : ids.keySet()){
				System.out.print("'" + id + "', ");
			}
			for (String key : scores.keySet()){
				writer.write(key.split("\\+")[0] + "\t" + key.split("\\+")[1] + "\t" + scores.get(key) + "\n");
			}
			reader.close();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void filterParallel(String dir, int count){
		List<String> filenames = Arrays.asList(new File(dir).list());
		try {
			Integer outName = 0;
			for (String filename : filenames){
				if (filename.equals("complete"))
					continue;
				BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(dir + "/" + filename))));
				String line = null;
				List<String> lines = new ArrayList<String>();
				while ((line = reader.readLine()) != null){
					lines.add(line);
				}
				reader.close();
				if (lines.size() == count){
					outName ++;
					BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new DataOutputStream(new FileOutputStream(dir + "/complete/" + outName))));
					for (String out : lines){
						writer.write(out + "\n");
					}
					writer.close();
				}
			}
			System.out.println(outName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void filterYeast(){
		List<String> filenames = Arrays.asList(new File("data/Nets").list());
		try {
			FileOutputStream oStream = new FileOutputStream("data/yeast.txt");
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new DataOutputStream(oStream)));
			FileOutputStream rStream = new FileOutputStream("data/repeated.txt");
			BufferedWriter rWriter = new BufferedWriter(new OutputStreamWriter(new DataOutputStream(rStream)));
			FileOutputStream symStream = new FileOutputStream("data/symmetric.txt");
			BufferedWriter symWriter = new BufferedWriter(new OutputStreamWriter(new DataOutputStream(symStream)));
			HashMap<String, String> all = new HashMap<String, String>();
			for (String filename : filenames){
				FileInputStream iStream = new FileInputStream("data/Nets/" + filename);
				BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(iStream)));
				String line;
				while ((line = reader.readLine()) != null){
					if (line.indexOf("taxid:4932") > -1){
						String[] parts = line.split("\\s+");
						if (parts[0].equals(parts[1])){
							symWriter.write(line + "\n\n");
							continue;
						}
						String repeatedLine = all.get(parts[1] + "-" + parts[0]);
						if ( repeatedLine == null){
							all.put(parts[0] + "-" + parts[1], line);
							writer.write(line + "\n");
						} else {
							rWriter.write(repeatedLine + "\n" + line + "\n\n");
						}
					}
				}
				reader.close();
			}
			writer.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
