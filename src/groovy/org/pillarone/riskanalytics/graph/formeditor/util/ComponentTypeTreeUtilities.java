package org.pillarone.riskanalytics.graph.formeditor.util;

import com.ulcjava.base.application.tree.DefaultMutableTreeNode;
import com.ulcjava.base.application.tree.TreePath;

/**
 * Created by IntelliJ IDEA.
 * User: martin.melchior
 * Date: 24.03.11
 * Time: 18:54
 * To change this template use File | Settings | File Templates.
 */
public class ComponentTypeTreeUtilities {


    public static String getComponentTypeName(TreePath path) {
        Object[] elms = path.getPath();
        if (elms.length > 1) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) elms[1];
            StringBuffer buffer = new StringBuffer(node.toString());
            for (int i = 2; i < elms.length; i++) {
                buffer.append(".");
                node = (DefaultMutableTreeNode) elms[i];
                buffer.append(node.toString());
            }
            return buffer.toString();
        } else {
            return null;
        }
    }

}
