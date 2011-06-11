package org.pillarone.riskanalytics.graph.formeditor.ui.model;


import com.ulcjava.base.application.tree.DefaultTreeModel;
import com.ulcjava.base.application.tree.ITreeNode;
import org.codehaus.groovy.grails.commons.ApplicationHolder;
import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.persistence.GraphPersistenceService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 */
public class ModelRepositoryTreeModel extends DefaultTreeModel {

    Map<ModelRepositoryTreeNode, AbstractGraphModel> leaves;

    private ModelRepositoryTreeModel(ITreeNode iTreeNode) {
        super(iTreeNode);
        leaves = new HashMap<ModelRepositoryTreeNode, AbstractGraphModel>();
    }

    public static ModelRepositoryTreeModel getInstance() {
        org.springframework.context.ApplicationContext ctx = ApplicationHolder.getApplication().getMainContext();
        GraphPersistenceService service = ctx.getBean(GraphPersistenceService.class);
        List<AbstractGraphModel> models = service.loadAll();
        ModelRepositoryTreeNode root = new ModelRepositoryTreeNode("", "Models under Development");
        ModelRepositoryTreeModel treeModel = new ModelRepositoryTreeModel(root);
        for (AbstractGraphModel model : models) {
            treeModel.addNode(root, model);
        }
        return treeModel;
    }

    private void addNode(ModelRepositoryTreeNode parent, AbstractGraphModel model) {
        String parentPath = parent.getPackageName();
        String path = model.getPackageName();
        if (parentPath.length()>=(path==null? 0 : path.length())) {
            ModelRepositoryTreeNode node = new ModelRepositoryTreeNode(model);
            node.setLeaf(true);
            insertNodeInto(node, parent, parent.getChildCount());
            leaves.put(node, model);
        } else {
            String reducedPath = path.substring(parentPath.length()==0 ? 0 : parentPath.length()+1);
            String[] pathElements = reducedPath.split(ModelRepositoryTreeNode.PATHSEP);
            String nodeName = pathElements[0];
            ModelRepositoryTreeNode node = getNode(parent, nodeName);
            if (node != null) {
                node.setLeaf(false);
                addNode(node, model);
            }
        }
    }

    private ModelRepositoryTreeNode getNode(ModelRepositoryTreeNode parent, String name) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            ModelRepositoryTreeNode nodeCandidate = (ModelRepositoryTreeNode) parent.getChildAt(i);
            if (nodeCandidate.getName().equals(name)) {
                return nodeCandidate;
            }
        }
        String parentPath = parent.getPackageName();
        String nodePath = parentPath==null || parentPath.length()==0
                            ? name
                            : parentPath+"."+name;
        ModelRepositoryTreeNode node = new ModelRepositoryTreeNode(nodePath, name);
        insertNodeInto(node, parent, parent.getChildCount());
        return node;
    }

    public void addNode(AbstractGraphModel model) {
        addNode((ModelRepositoryTreeNode) getRoot(),model);
    }

    public boolean containsModel(AbstractGraphModel model) {
        return leaves.values().contains(model);
    }
}
