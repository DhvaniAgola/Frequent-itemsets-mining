import java.io.File;
import java.io.IOException;
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

public class ParsingTree {

	private String file_name = "website.txt";
	private int support = 0;
	private Pattern regexPattern = Pattern.compile(",");
//	private OutputStream out = null;
	private int baskets = 0;

	public static void main(String[] args) {
		ParsingTree tre = new ParsingTree();
		tre.init();
	}

	private void init() {
		try {
			List<Node> headerTable = buildHeaderTable();
			String[] orderItemSets = generateOrderedItemSets(headerTable);
			Node root = buildFPTree(orderItemSets, headerTable);

			if (root.getChildrens().size() == 0) {
				System.out.println("Tree Empty");
				return;
			}

			System.out.println("Tree Generated");
			for (Node n : headerTable) {
				if (n.getItemName().equals("3")) {
					while (n.getNextNode() != null) {
						System.out.println(n.getNextNode().getItemName() + " : " + n.getNextNode().getItemCount());
						n = n.getNextNode();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Comparator<Node> NodeCountComparator = new Comparator<Node>() {

		public int compare(Node s1, Node s2) {
			int item1 = s1.getItemCount();
			int item2 = s2.getItemCount();
			return item2 - item1;
		}
	};

	/**
	 * Building FP Tree through Ordered ItemSets
	 */
	private Node buildFPTree(String[] orderedItemSets, List<Node> headerTable) {
		Node node = new Node();
		Node curr_node = node;
		for (int i = 0; i < orderedItemSets.length; i++) {
			String[] nameArray = orderedItemSets[i].split(",");
			for (int j = 0; j < nameArray.length; j++) {
				Node tmp = findChild(nameArray[j], curr_node);
				if (null == tmp) {
					tmp = new Node();
					tmp.setItemName(nameArray[j]);
					tmp.setParentNode(curr_node);
					curr_node.getChildrens().add(tmp);
					addAdjacentNode(tmp, headerTable);
				}
				curr_node = tmp;
				tmp.setItemCount(tmp.getItemCount() + 1);
			}
			curr_node = node;
		}
		return node;
	}

	private void addAdjacentNode(Node n, List<Node> headerTable) {
		Node treeNode = null;
		for (Node eachNode : headerTable) {
			if (eachNode.getItemName().equals(n.getItemName())) {
				treeNode = eachNode;
				while (null != treeNode.getNextNode()) {
					treeNode = treeNode.getNextNode();
				}
				treeNode.setNextNode(n);
			}
		}
	}

	private void recursivePrintTree(Node curr_node) {
		if (curr_node.getItemName() != null) {
			System.out.println("Parent Node  : "
					+ (curr_node.getParentNode() == null ? null : curr_node.getParentNode().getItemName())
					+ " :  Current Node : " + curr_node.getItemName() + ":" + curr_node.getItemCount() + " ");
		}
		// Termination condition is internally getChildrens Method which has no child
		curr_node.getChildrens().forEach(i -> {
			recursivePrintTree(i);
		});
	}

	/**
	 * Find Child in Current if there then check for exsiting or else return null
	 * 
	 * @param item
	 * @param curr_node
	 * @return Node (foundNode or Null)
	 */
	private Node findChild(String item, Node curr_node) {
		if (!curr_node.getChildrens().isEmpty()) {
			for (Node eachChildInCurrentNode : curr_node.getChildrens()) {
				if (eachChildInCurrentNode.getItemName().equalsIgnoreCase(item)) {
					return eachChildInCurrentNode;
				}
			}
		}
		return null;
	}

	/**
	 * Read Transaction for first time and count all item and remove item violating
	 * support rule
	 * 
	 * @throws IOException
	 */
	private List<Node> buildHeaderTable() throws IOException {
		List<Node> childrens = new ArrayList<>();
		Map<String, Integer> itemMapCount = new HashMap<>();
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
						baskets = Integer.valueOf(str[0].trim());
					} else {
						String[] data = regexPattern.split(new String(subarray), 2)[1].split(",");
						for (int i = 0; i < data.length; i++) {
							if (!data[i].trim().isEmpty()) {
								if (itemMapCount.containsKey(data[i].trim())) {
									itemMapCount.put(data[i].trim(), itemMapCount.get(data[i].trim()) + 1);
								} else {
									itemMapCount.put(data[i].trim(), 1);
								}
							}
						}
					}
					intIndex = j + 1;
				}
			}
			bf.clear();
		}
		itemMapCount.forEach((k, v) -> {
			if (v >= support) {
				Node newNode = new Node();
				newNode.setItemName(k);
				newNode.setItemCount(v);
				childrens.add(newNode);
			}
		});
		Collections.sort(childrens, NodeCountComparator);
		return childrens;
	}

	/**
	 * For Generating Ordered Items Sets
	 * 
	 * @throws IOException
	 */
	private String[] generateOrderedItemSets(List<Node> NodeList) throws IOException {
		Path path = Paths.get(file_name);
		SeekableByteChannel seekableByteChannel = Files.newByteChannel(path, StandardOpenOption.READ);
		long filesize = new File(file_name).length();
		ByteBuffer bf = ByteBuffer.allocate((int) filesize);
		byte bt[];
		byte[] subarray;
		int intIndex = 0;
		String local = "";
		int size = 0;
		String[] orderedItemsSets = new String[baskets];
		while (seekableByteChannel.read(bf) > 0) {
			bf.flip();
			bt = bf.array();
			for (int j = 0; j < bt.length; j++) {
				if (bt[j] == (char) '\n') {
					subarray = Arrays.copyOfRange(bt, intIndex, j);
					if (intIndex != 0) {
						String tempData = new String(subarray);
						String data = regexPattern.split(tempData, 2)[1];
						local = "";
						for (Node eachNode : NodeList) {
							if (data.contains(eachNode.getItemName())) {
								local += eachNode.getItemName() + ",";
							}
						}
//						stringArrayList.add(local);
						orderedItemsSets[size++] = local;
					}
					intIndex = j + 1;
				}
			}
			bf.clear();
		}
		return orderedItemsSets;
	}

}
