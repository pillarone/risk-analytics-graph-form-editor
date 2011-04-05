package org.pillarone.riskanalytics.graph.formeditor.ui.model;

import com.ulcjava.base.application.tree.DefaultTreeModel;
import junit.framework.TestCase;

/**
 * Created by IntelliJ IDEA.
 * User: martin.melchior
 * Date: 05.04.11
 * Time: 16:41
 * To change this template use File | Settings | File Templates.
 */
public class ComponentTypeTreeModelFactoryTest extends TestCase {

    public void testGetTree() {
        DefaultTreeModel treeModel = ComponentTypeTreeModelFactory.getTree();
        assertNotNull(treeModel.getRoot());
        assertTrue(treeModel.getChildCount(treeModel.getRoot())>0);
    }


}
