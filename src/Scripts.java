import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class Scripts {

	public static void main(String[] args) {
		filterHomosapiens();
	}
	
	public static void restoreGODatabase(){
		for (File file: new File("C:/Users/hgabr/Desktop/go_201111-assocdb-tables/go_201111-assocdb-tables").listFiles()){
			if (file.getName().indexOf(".sql") > 0){
				try {
					Process process = Runtime.getRuntime().exec("cmd /c mysql -u root go < " + "C:\\Users\\hgabr\\Desktop\\go_201111-assocdb-tables\\go_201111-assocdb-tables\\" + file.getName());
					System.out.println(process.waitFor());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		for (File file: new File("C:/Users/hgabr/Desktop/go_201111-assocdb-tables/go_201111-assocdb-tables").listFiles()){
			if (file.getName().indexOf(".txt") > 0){
				try {
					System.out.println(file.getName());
					Process process = Runtime.getRuntime().exec("cmd /c mysqlimport -u root -L go " + "C:\\Users\\hgabr\\Desktop\\go_201111-assocdb-tables\\go_201111-assocdb-tables\\" + file.getName());
					System.out.println(process.waitFor());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
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
