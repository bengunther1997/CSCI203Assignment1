import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

/*container for unique word and counter*/
class Word {
	String word;
	int count = 1;

	Word(String word) {
		this.word = word;
	}

	int compareTo(Word word) {
		// used by insert function to order alphabetically
		if (this.word.compareTo(word.word) < 0) {
			return -1;
		} else if (this.word.compareTo(word.word) > 0) {
			return 1;
		} else {
			return 0;
		}
	}
}

/* implementation of AVL node */
class AVLNode {
	Word key;
	int height;
	AVLNode leftNode;
	AVLNode rightNode;

	AVLNode(Word key) {
		this.key = key;
	}
}

/* implementation of AVL tree */
class AVLTree {
	AVLNode rootNode;

	static AVLNode insertNode(AVLNode node, Word key) {
		// recursive insert function looks through tree and places new leaf
		// the tree is rebalanced after each insert except for when count...
		// is incremented or root added (in which case balancing is pointless)
		if (node == null) {
			return new AVLNode(key);
		} else if (node.key.compareTo(key) == 1) {
			node.leftNode = insertNode(node.leftNode, key);
		} else if (node.key.compareTo(key) == -1) {
			node.rightNode = insertNode(node.rightNode, key);
		} else {
			node.key.count++;
			return node;
		}
		return rebalanceTree(node);
	}

	/* simple height update function */
	/* takes a node and recalculates its height */
	static void updateHeight(AVLNode node) {
		node.height = 1 + Math.max(getNodeHeight(node.leftNode), getNodeHeight(node.rightNode));
	}

	/* returns the height value of a non-null node */
	static int getNodeHeight(AVLNode node) {
		if (node == null) {
			return -1;
		} else {
			return node.height;
		}
	}

	/*
	 * check tree balance - used to determine whether right or left rotation is
	 * needed
	 */
	static int checkBalance(AVLNode node) {
		if (node == null) {
			return 0;
		} else {
			return getNodeHeight(node.rightNode) - getNodeHeight(node.leftNode);
		}
	}

	/*
	 * there are 4 rebalancing cases - this determines which is needed and executes
	 * the case
	 */
	static AVLNode rebalanceTree(AVLNode node) {
		updateHeight(node);
		int balance = checkBalance(node);
		if (balance > 1) {
			if (getNodeHeight(node.rightNode.rightNode) > getNodeHeight(node.rightNode.leftNode)) {// case 1
				node = rotateLeft(node);
			} else {// case 2
				node.rightNode = rotateRight(node.rightNode);
				node = rotateLeft(node);
			}
		} else if (balance < -1) {
			if (getNodeHeight(node.leftNode.leftNode) > getNodeHeight(node.leftNode.rightNode)) {// case 3
				node = rotateRight(node);
			} else {// case 4
				node.leftNode = rotateLeft(node.leftNode);
				node = rotateRight(node);
			}
		}
		return node;
	}

	/* performs right rotation on passed node */
	static AVLNode rotateRight(AVLNode y) {
		AVLNode x = y.leftNode;
		AVLNode z = x.rightNode;
		x.rightNode = y;
		y.leftNode = z;
		updateHeight(y);
		updateHeight(x);
		return x;
	}

	/* performs left rotation on passed node */
	static AVLNode rotateLeft(AVLNode y) {
		// System.err.println("Rotate LEFT: " + y.key.word);
		AVLNode x = y.rightNode;
		AVLNode z = x.leftNode;
		x.leftNode = y;
		y.rightNode = z;
		updateHeight(y);
		updateHeight(x);
		return x;
	}
}

public class ass1 {
	public static AVLTree avl = new AVLTree();
	public static Word[] data = new Word[50000];
	public static int dataCount = 0;
	public static int totWords = 0;

	/* query user for file name */
	public static String getFileName() {
		Scanner input = new Scanner(System.in);
		System.out.print("Enter filename: ");
		String fileName = input.nextLine();
		input.close();
		return fileName;
	}

	/* performs in-order traversal of avl tree and migrates data to array */
	public static void readTreeToArray(AVLNode node) {
		// do nothing if node is null
		if (node == null)
			return;

		// look through left child first
		readTreeToArray(node.leftNode);

		// move this nodes data to array
		data[dataCount] = node.key;
		dataCount++;

		// look through right child next
		readTreeToArray(node.rightNode);
	}

	/* primary merge sort driver function - called on array of words */
	public static void mergeSort(Word[] array, int dataCount) {
		// if the array only has 1 item, or is empty, then it is already sorted
		if (dataCount < 2) {
			return;
		}
		// split into two - divide and conquer approach
		int midIdx = dataCount / 2;
		Word[] lArray = new Word[midIdx];
		Word[] rArray = new Word[dataCount - midIdx];

		for (int i = 0; i < midIdx; i++) {
			lArray[i] = array[i];
		}
		for (int i = midIdx; i < dataCount; i++) {
			rArray[i - midIdx] = array[i];
		}
		// recursive call to merge sort - the base case is when max 2 items in array
		mergeSort(lArray, midIdx);
		mergeSort(rArray, dataCount - midIdx);
		// finally rejoin sorted arrays
		merge(array, lArray, rArray, midIdx, dataCount - midIdx);
	}

	/* dedicated merge function - only called by mergeSort driver */
	public static void merge(Word[] array, Word[] lArray, Word[] rArray, int left, int right) {

		int lArrayIdx = 0, rArrayIdx = 0, arrayIdx = 0;
		// standard implementation of merge process
		while (lArrayIdx < left && rArrayIdx < right) {
			if (lArray[lArrayIdx].count >= rArray[rArrayIdx].count) {
				array[arrayIdx++] = lArray[lArrayIdx++];
			} else {
				array[arrayIdx++] = rArray[rArrayIdx++];
			}
		}
		while (lArrayIdx < left) {
			array[arrayIdx++] = lArray[lArrayIdx++];
		}
		while (rArrayIdx < right) {
			array[arrayIdx++] = rArray[rArrayIdx++];
		}
	}

	public static void main(String[] args) {
		try {
			// file reading stuff here
			BufferedReader indata = new BufferedReader(new FileReader(getFileName()));
			String line, words[];
			while ((line = indata.readLine()) != null) {
				// skip line if empty
				if (line.compareTo("") == 0) {
					continue;
				}
				// preprocessing
				words = line.replaceAll("[^a-zA-Z ]", "").toLowerCase().split(" ");

				// add word to avl tree
				for (int i = 0; i < words.length; i++) {
					avl.rootNode = AVLTree.insertNode(avl.rootNode, new Word(words[i]));
					totWords++;
				}
			}
		} catch (FileNotFoundException e) {
			// handle file not found case
			e.printStackTrace();
			return;
		} catch (IOException e) {
			// handle io problems
			e.printStackTrace();
			return;
		}
		// move to array and sort
		readTreeToArray(avl.rootNode);
		mergeSort(data, dataCount);

		// data output as required
		if (dataCount <= 20) {
			// output everything when <20 items to prevent output overlap...
			// or array out of bounds problems
			for (int i = 0; i < dataCount; i++) {
				System.out.println(data[i].count + " x " + data[i].word);
			}
		} else {
			for (int i = 0; i < 10; i++) {
				System.out.println(data[i].count + " x " + data[i].word);
			}
			for (int i = dataCount - 10; i < dataCount; i++) {
				System.out.println(data[i].count + " x " + data[i].word);
			}
		}
		// always output total and unique words fyi
		// this is interesting but not required for desired output
		System.out.println("FINAL WORD COUNT: " + totWords);
		System.out.println("UNIQUE WORDS: " + dataCount);
	}
}
