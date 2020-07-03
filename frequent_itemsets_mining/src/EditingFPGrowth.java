import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class EditingFPGrowth {

	private String file_name = "input10000.txt";
	private int support = 0;
	private HashMap<Integer, Integer> bucketCount = new HashMap<>();
	private List<String> stringArrayList = new ArrayList<>();
	private List<Integer> conditionalPattern = new ArrayList<>();
	private HashMap<String, Integer> conditionalFpTree = null;
	private Pattern regexPattern = Pattern.compile(",");
	private OutputStream out = null;

	/**
	 * Main Function -- Initiate Call To Init Function
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		EditingFPGrowth fpGrowth = new EditingFPGrowth();
		fpGrowth.init();
		long endTime = System.currentTimeMillis();
		System.out.println((endTime - startTime) + " Milliseconds ");
	}

	/**
	 * StartUp Function (Driver Function)
	 */
	private void init() {
		try {
			out = new BufferedOutputStream(new FileOutputStream("output.txt"));
			readBasket();
			generateOrderedItemSets();
			conditionalPattern.addAll(bucketCount.keySet());
			Collections.reverse(conditionalPattern);
			System.out.println(stringArrayList);
		} catch (Exception e) {
			// Handle
			e.printStackTrace();
		}
	}

	

	/**
	 * Read Transaction for first time and count all item and remove item violating
	 * support rule
	 * 
	 * @throws IOException
	 */
	private void readBasket() throws IOException {
		Path path = Paths.get(file_name);
		SeekableByteChannel seekableByteChannel = Files.newByteChannel(path, StandardOpenOption.READ);
		long filesize = new File(file_name).length();
		ByteBuffer bf = ByteBuffer.allocate((int) filesize);
		byte bt[];
		byte[] subarray;
		int intIndex = 0;
		while (seekableByteChannel.read(bf) > 0) {
			bf.flip();
			bt = bf.array();
			for (int j = 0; j < bt.length; j++) {
				if (bt[j] == (char) '\n') {
					subarray = Arrays.copyOfRange(bt, intIndex, j);
					if (intIndex != 0) {
						String[] data = regexPattern.split(new String(subarray), 2)[1].split(",");
						for (int i = 0; i < data.length; i++) {
							if (!data[i].trim().isEmpty()) {
								if (bucketCount.containsKey(Integer.parseInt(data[i].trim()))) {
									bucketCount.put(Integer.parseInt(data[i].trim()),
											bucketCount.get(Integer.parseInt(data[i].trim())) + 1);
								} else {
									bucketCount.put(Integer.parseInt(data[i].trim()), 1);
								}
							}
						}
					}
					intIndex = j + 1;
				}
			}
			bf.clear();
		}
		bucketCount.entrySet().removeIf(val -> val.getValue() < support);
		bucketCount = sortByValue(bucketCount);
		for (Map.Entry<Integer, Integer> entry : bucketCount.entrySet()) {
			out.write(("{" + entry.getKey() + "} - " + entry.getValue() + "\n").getBytes());
		}
	}

	/**
	 * For Generating Ordered Items Sets
	 * 
	 * @throws IOException
	 */
	private void generateOrderedItemSets() throws IOException {
		Path path = Paths.get(file_name);
		SeekableByteChannel seekableByteChannel = Files.newByteChannel(path, StandardOpenOption.READ);
		long filesize = new File(file_name).length();
		ByteBuffer bf = ByteBuffer.allocate((int) filesize);
		byte bt[];
		byte[] subarray;
		String[] str;
		int intIndex = 0;
		while (seekableByteChannel.read(bf) > 0) {
			bf.flip();
			bt = bf.array();
			for (int j = 0; j < bt.length; j++) {
				if (bt[j] == (char) '\n') {
					subarray = Arrays.copyOfRange(bt, intIndex, j);
					if (intIndex == 0) {
						str = new String(subarray).split(" ", 2);
						support = Integer.valueOf(str[1].trim());
					} else {
						String tempData = new String(subarray);
						String data = regexPattern.split(tempData, 2)[1];
						List<String> local = new ArrayList<>();
						for (int key : bucketCount.keySet()) {
							if (data.contains(Integer.toString(key))) {
								local.add(Integer.toString(key));
							}
						}
						stringArrayList.add(String.join(",",local));
					}
					intIndex = j + 1;
				}
			}
			bf.clear();
		}
	}

	/**
	 * Reference : For Sorting HashMap By Value
	 * 
	 * @param hm
	 * @see https://www.geeksforgeeks.org/sorting-a-hashmap-according-to-values/
	 * @return
	 */
	private HashMap<Integer, Integer> sortByValue(HashMap<Integer, Integer> hm) {
		// Create a list from elements of HashMap
		List<Map.Entry<Integer, Integer>> list = new LinkedList<Map.Entry<Integer, Integer>>(hm.entrySet());

		// Sort the list
		Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>() {
			public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});
		HashMap<Integer, Integer> temp = new LinkedHashMap<Integer, Integer>();
		for (Map.Entry<Integer, Integer> aa : list) {
			temp.put(aa.getKey(), aa.getValue());
		}
		return temp;
	}

}