package org.pillarone.riskanalytics.graph.formeditor.ui.model;

import com.ulcjava.base.application.tree.DefaultTreeModel;
import junit.framework.TestCase;

/**
 * 
 */
public class ComponentTypeTreeModelFactoryTest extends TestCase {

    public void testGetTree() {
        DefaultTreeModel treeModel = ComponentTypeTreeModelFactory.getTree();
        assertNotNull(treeModel.getRoot());
        assertTrue(treeModel.getChildCount(treeModel.getRoot())>0);
    }


}
