
package ecnu.modana.alsmc.generatePA;

import java.util.ArrayList;
import java.util.List;

public class TreeNode 
{
	public String nodeId;
	public int f;//end in this node
	public int n;//pass this node
	public int tcheck;//satisfied or not {0,1}
	public int num;

	public List<String> endList = null;
	public List<TreeNode> seqList = null;
	public List<TreeNode> childList = null;
	
	public TreeNode() {
		super();
		childList = new ArrayList<TreeNode>();
		endList = new ArrayList<>();
		seqList = new ArrayList<>();
	}
	public TreeNode(String nodeId) {
		super();
		this.nodeId = nodeId;
		childList = new ArrayList<TreeNode>();
	}
	
	public  Integer getN() {
		return n;
	}
	
}
