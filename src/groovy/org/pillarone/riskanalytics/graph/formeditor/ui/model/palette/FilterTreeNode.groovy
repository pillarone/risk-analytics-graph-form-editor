package org.pillarone.riskanalytics.graph.formeditor.ui.model.palette

import com.ulcjava.base.application.tree.ITreeNode

/**
 * @author fouad.jaada@intuitive-collaboration.com
 */
class FilterTreeNode {

    FilterTreeNode parent
    List<FilterTreeNode> childNodes
    List<Integer> activeIndices
    int originalChildIndex
    ITreeNode originalNode

    public FilterTreeNode() {
        childNodes = []
        activeIndices = []
    }
}
