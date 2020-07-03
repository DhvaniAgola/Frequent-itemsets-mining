import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Silence
 *
 */
public class GenerateInputFile {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Random a = new Random();
		List<String> tempList = new ArrayList<>();
		BufferedWriter br = new BufferedWriter(new FileWriter("input1000.txt"));
		br.write(1000 + " " + 5);
		br.newLine();
		for (int i = 0; i < 1000; i++) {
			int itemSetCount = a.nextInt(97) + 3;
			for (int j = 1; j <= itemSetCount; j++) {
				int rand = a.nextInt(99);
				if (!tempList.contains(String.valueOf(rand))) {
					tempList.add(String.valueOf(rand));
				}
			}
			br.write(String.valueOf(i + 1) + "," + String.join(",", tempList));
			br.newLine();
			tempList.clear();
		}
		br.close();
	}

}
