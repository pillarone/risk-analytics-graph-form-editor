package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.applicationframework.application.ApplicationActionMap;
import com.ulcjava.applicationframework.application.ApplicationContext;
import com.ulcjava.base.application.*;
import com.ulcjava.base.application.event.ActionEvent;
import com.ulcjava.base.application.event.IActionListener;
import com.ulcjava.base.application.tree.*;
import com.ulcjava.base.application.util.Dimension;
import org.pillarone.riskanalytics.graph.formeditor.util.ComponentTypeTreeUtilities;
import org.pillarone.riskanalytics.graph.formeditor.util.PaletteUtilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ComponentTypeTree extends ULCBoxPane {

	private ITreeModel fTreeModel;
    private ULCTree fTree;
    private ComponentTypeTreeCellRenderer fTreeCellRenderer;
    private FormEditorModelsView fParent;

	public ComponentTypeTree(FormEditorModelsView parent) {
		super();
        fParent = parent;
		createTreeModel();
		createView();
	}

    public void configureCellRenderer(IActionListener listener) {
    }

	private void createView() {
        // create tree
		fTree = new ULCTree();
		fTree.setDragEnabled(true);
		fTree.setModel(fTreeModel);
        fTree.getSelectionModel().setSelectionMode(ULCTreeSelectionModel.SINGLE_TREE_SELECTION);
        fTreeCellRenderer = new ComponentTypeTreeCellRenderer();
        fTreeCellRenderer.setShowComponentMenuListener(new ShowComponentAction());
        fTree.setCellRenderer(fTreeCellRenderer);

        ULCScrollPane treeScrollPane = new ULCScrollPane(fTree);
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

    private class ShowComponentAction implements IActionListener {

        public void actionPerformed(ActionEvent actionEvent) {
            TreePath selectedNode = fTree.getSelectionModel().getSelectionPath();
            String clazzName = ComponentTypeTreeUtilities.getComponentTypeName(selectedNode);
            try {
                boolean success = fParent.importComponentType(clazzName);
                if (!success) {
                    ULCAlert alert = new ULCAlert("No class loaded",
                                        "No class with name " + clazzName + " could be loaded as graph model.", "ok");
                    alert.show();
                }
            } catch(ClassNotFoundException ex1) {
                ULCAlert alert = new ULCAlert("No class loaded",
                            "Class not found.", "ok");
                alert.show();
            } catch (Exception ex3) {
                ULCAlert alert = new ULCAlert("No class loaded",
                            "Unkown exception. ", "ok");
                alert.show();
            }
        }
    }
}
