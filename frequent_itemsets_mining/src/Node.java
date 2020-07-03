import java.util.ArrayList;
import java.util.List;

/**
 * @author MayankJariwala
 *
 */
public class Node {

	private String itemName;
	private int itemCount;
	private Node root;
	private List<Node> childrens = new ArrayList<>();
	private Node nextNode;
	private Node parentNode;

	public Node() {

	}

	public Node(String itemName) {
		this.setItemName(itemName);
	}

	/**
	 * @return the nextNode
	 */
	public Node getNextNode() {
		return nextNode;
	}

	/**
	 * @param nextNode the nextNode to set
	 */
	public void setNextNode(Node nextNode) {
		this.nextNode = nextNode;
	}

	
	/**
	 * @return the root
	 */
	public Node getRoot() {
		return root;
	}

	/**
	 * @param root the root to set
	 */
	public void setRoot(Node root) {
		this.root = root;
	}

	/**
	 * @return the childrens
	 */
	public List<Node> getChildrens() {
		return childrens;
	}

	/**
	 * @param childrens the childrens to set
	 */
	public void setChildrens(List<Node> childrens) {
		this.childrens = childrens;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public int getItemCount() {
		return itemCount;
	}

	public void setItemCount(int itemCount) {
		this.itemCount = itemCount;
	}

	public Node getParentNode() {
		return parentNode;
	}

	public void setParentNode(Node parentNode) {
		this.parentNode = parentNode;
	}

}
