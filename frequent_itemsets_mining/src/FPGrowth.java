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

public class FPGrowth {

	private String file_name = "website.txt";
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
		FPGrowth fpGrowth = new FPGrowth();
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
			for (int s = 0; s < conditionalPattern.size(); s++) {
				HashMap<String, Integer> freeHashMap = new LinkedHashMap<>();
				conditionalFpTree = new HashMap<>();
				for (int k = 0; k < stringArrayList.size(); k++) {
					if (stringArrayList.get(k).contains(conditionalPattern.get(s).toString())) {
						String pattern = stringArrayList.get(k).substring(0,
								stringArrayList.get(k).indexOf(conditionalPattern.get(s).toString()));
						if (!pattern.isEmpty()) {
							String[] eachItem = regexPattern.split(pattern);
							for (int i = 0; i < eachItem.length; i++) {
								if (freeHashMap.containsKey(eachItem[i])) {
									freeHashMap.put(eachItem[i], freeHashMap.get(eachItem[i]) + 1);
								} else {
									freeHashMap.put(eachItem[i], 1);
								}
							}
							if (conditionalFpTree.containsKey(pattern)) {
								conditionalFpTree.put(pattern, conditionalFpTree.get(pattern) + 1);
							} else {
								conditionalFpTree.put(pattern, 1);
							}
						}
					}
				}
				freeHashMap.entrySet().removeIf(val -> val.getValue() < support);
				if (!conditionalFpTree.isEmpty() && !freeHashMap.isEmpty()) {
					formPairandWriteOnConsole(conditionalPattern.get(s).toString(), freeHashMap, conditionalFpTree);
				}
			}
		} catch (Exception e) {
			// Handle
			e.printStackTrace();
		}
	}

	private void formPairandWriteOnConsole(String key, HashMap<String, Integer> freeHashMap,
			HashMap<String, Integer> conditionalFpTree) {
		String[] keyArray = freeHashMap.keySet().toArray(new String[freeHashMap.size()]);
		int length = keyArray.length;
		for (int i = 1; i <= length; i++) {
			pair(keyArray, length, i, key, conditionalFpTree);
		}
	}

	private void pairGenerator(String arr[], String data[], int start, int end, int index, int r, String key,
			HashMap<String, Integer> conditionalFpTree) {
		String dataPair = "";
		if (index == r) {
			for (int j = 0; j < r; j++) {
				dataPair += data[j] + ",";
			}
			String[] dairPairSet = regexPattern.split(dataPair);
			String[] keysArray = conditionalFpTree.keySet().toArray(new String[conditionalFpTree.size()]);
			int count = 0;
			for (int s = 0; s < keysArray.length; s++) {
				boolean flag = true;
				for (int k = 0; k < dairPairSet.length; k++) {
					if (keysArray[s].indexOf(dairPairSet[k]) == -1) {
						flag = false;
						break;
					}
				}
				if (flag) {
					count += conditionalFpTree.get(keysArray[s]);
				}
			}
			if (count >= support) {
				try {
					out.write(("{" + key + "," + dataPair.substring(0, dataPair.length() - 1) + "} - " + count + "\n")
							.getBytes());
					 System.out.println((key + "," + dataPair + ": " + count ));
					out.flush();
				} catch (Exception e) {

				}
			}
			return;
		}
		for (int i = start; i <= end && end - i + 1 >= r - index; i++) {
			data[index] = arr[i];
			pairGenerator(arr, data, i + 1, end, index + 1, r, key, conditionalFpTree);
		}
	}

	private void pair(String arr[], int n, int r, String key, HashMap<String, Integer> conditionalFpTree) {
		String data[] = new String[r];
		pairGenerator(arr, data, 0, n - 1, 0, r, key, conditionalFpTree);
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
						String local = "";
						for (int key : bucketCount.keySet()) {
							if (data.contains(Integer.toString(key))) {
								local += Integer.toString(key) + ",";
							}
						}
						stringArrayList.add(local);
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