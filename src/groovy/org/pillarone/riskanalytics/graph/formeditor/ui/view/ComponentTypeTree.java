package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.base.application.ULCBoxPane;
import com.ulcjava.base.application.ULCScrollPane;
import com.ulcjava.base.application.ULCTree;
import com.ulcjava.base.application.tree.AbstractTreeModel;
import com.ulcjava.base.application.tree.DefaultMutableTreeNode;
import com.ulcjava.base.application.tree.DefaultTreeModel;
import com.ulcjava.base.application.tree.ITreeModel;
import com.ulcjava.base.application.util.Dimension;
import org.pillarone.riskanalytics.graph.formeditor.util.PaletteUtilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ComponentTypeTree extends ULCBoxPane {

	private ITreeModel fTreeModel;
	
	public ComponentTypeTree() {
		super();		
		createTreeModel();
		createView();
	}

	private void createView() {
		ULCTree tree = new ULCTree();
		tree.setDragEnabled(true);
		tree.setModel(fTreeModel);
        ULCScrollPane treeScrollPane = new ULCScrollPane(tree);
        treeScrollPane.setMinimumSize(new Dimension(200, 600));
        this.add(ULCBoxPane.BOX_EXPAND_EXPAND, treeScrollPane);
		this.setVisible(true);
	}
	
	
	private void createTreeModel() {
		Map<String,List<String>> typeMap = getTypeMap(); 
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
		for (Entry<String, List<String>> e : typeMap.entrySet()) {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(e.getKey());
			for (String leaf : e.getValue()) {
				node.add(new DefaultMutableTreeNode(leaf, true));
			}
			root.add(node);
		}
		fTreeModel = new DefaultTreeModel(root);		
	}
	
	private Map<String,List<String>> getTypeMap() {
		Map<String,List<String>> typeMap = new HashMap<String, List<String>>();
		List<String> componentNames = PaletteUtilities.getAvailableComponentNames(true);
		for (String path : componentNames) {
			int index = path.lastIndexOf(".");
			String name = path.substring(index+1);
			String packageName = path.substring(0,index);
			if (!typeMap.containsKey(packageName)) {
				typeMap.put(packageName, new ArrayList<String>());
			}
			typeMap.get(packageName).add(name);
		}
		return typeMap;
	}
	
	private class ComponentNamesTree extends AbstractTreeModel {

		public Object getRoot() {
			// TODO Auto-generated method stub
			return null;
		}

		public Object getChild(Object parent, int index) {
			// TODO Auto-generated method stub
			return null;
		}

		public int getChildCount(Object parent) {
			// TODO Auto-generated method stub
			return 0;
		}

		public boolean isLeaf(Object node) {
			// TODO Auto-generated method stub
			return false;
		}

		public int getIndexOfChild(Object parent, Object child) {
			// TODO Auto-generated method stub
			return 0;
		}
		
	}
}
