package org.pillarone.riskanalytics.graph.formeditor.ui.model;


import com.ulcjava.base.application.tree.DefaultTreeModel;
import com.ulcjava.base.application.tree.ITreeNode;
import org.codehaus.groovy.grails.commons.ApplicationHolder;
import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.persistence.GraphPersistenceService;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class ModelRepositoryTreeModel extends DefaultTreeModel {

    private Map<ModelRepositoryTreeNode, AbstractGraphModel> leaves;

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
            treeModel.addNode(model);
        }
        return treeModel;
    }

    public Map<ModelRepositoryTreeNode, AbstractGraphModel> getLeaves() {
        return leaves;
    }

    public void addNode(AbstractGraphModel model) {
        final ModelRepositoryTreeNode root = (ModelRepositoryTreeNode) getRoot();
        final ModelRepositoryTreeNode newNode = new ModelRepositoryTreeNode(model);
        newNode.setLeaf(true);
        insertNodeInto(newNode, root, root.getChildCount());
        leaves.put(newNode, model);
    }

    public ModelRepositoryTreeNode getModelNode(AbstractGraphModel model) {
        for (ModelRepositoryTreeNode node : leaves.keySet()) {
            if (leaves.get(node).equals(model)) {
                return node;
            }
        }
        return null;
    }

    public ModelRepositoryTreeNode getModelNode(String modelName, String packageName) {
        for (Iterator<ModelRepositoryTreeNode> it = leaves.keySet().iterator(); it.hasNext();) {
            ModelRepositoryTreeNode node = it.next();
            AbstractGraphModel model = leaves.get(node);
            if (model.getName().equals(modelName) && model.getPackageName().equals(packageName)) {
                return node;
            }
        }
        return null;
    }
}
