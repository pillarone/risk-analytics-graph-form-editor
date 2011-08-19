package org.pillarone.riskanalytics.graph.formeditor.ui.model;

import com.ulcjava.base.application.tree.DefaultTreeModel;
import junit.framework.TestCase;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.palette.ComponentTypeTreeModelFactory;

/**
 * 
 */
public class ComponentTypeTreeModelFactoryTest extends TestCase {

    public void testGetTree() {
        DefaultTreeModel treeModel = ComponentTypeTreeModelFactory.getPackageTree();
        assertNotNull(treeModel.getRoot());
        assertTrue(treeModel.getChildCount(treeModel.getRoot())>0);
    }
}
