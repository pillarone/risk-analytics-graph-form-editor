package org.pillarone.riskanalytics.graph.formeditor.ui.model.treetable

import org.pillarone.riskanalytics.graph.core.graph.model.GraphElement
import org.pillarone.riskanalytics.graph.formeditor.ui.model.EnumGraphElementInfo
import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel
import org.pillarone.riskanalytics.graph.core.graph.model.ComponentNode
import org.pillarone.riskanalytics.graph.core.graph.model.ComposedComponentNode
import org.pillarone.riskanalytics.graph.core.graph.model.Port
import org.pillarone.riskanalytics.graph.core.graph.model.ModelGraphModel
import org.pillarone.riskanalytics.graph.core.graph.model.ComposedComponentGraphModel
import org.pillarone.riskanalytics.graph.core.graph.model.InPort
import org.pillarone.riskanalytics.graph.core.graph.model.OutPort
import org.pillarone.riskanalytics.graph.core.graph.util.IntegerRange

class GraphElementNode extends SimpleTableTreeNode {

    GraphElement element
    String type
    String info
    boolean isVisible

    public GraphElementNode(GraphElement element, String propertyName) {
        super(propertyName)
        this.element = element
        updateColumnValues()
        isVisible = true
    }

    public void setName(String newName) {
        this.@name = newName
    }

    public Object getValueAt(int column) {
        switch (column) {
            case NodesTableTreeModel.NAMEID:
                return this.displayName
            case NodesTableTreeModel.TYPEID:
                return this.type
            case NodesTableTreeModel.INFOID:
                return null
            default:
                return null
        }
    }

    public void updateColumnValues() {
        // type
        switch (element) {
            case AbstractGraphModel:
                this.type = ((AbstractGraphModel) element).getDisplayName() + " (" + ((AbstractGraphModel) element).getPackageName() + ")"
                break
            case ComponentNode:
                ComponentNode node = (ComponentNode) element
                this.type = node.getType().getTypeClass().getSimpleName() + " (" + node.getType().getTypeClass().getPackage().getName() + ")"
                break
            case Port:
                Port port = (Port) element;
                this.type = port.getPacketType().getSimpleName() + " (" + port.getPacketType().getPackage().getName() + ")"
                break
        }

        // info
        switch (element) {
            case ComposedComponentNode:
            case ComposedComponentGraphModel:
                this.info = EnumGraphElementInfo.CC.getDisplayValue()
                break
            case ModelGraphModel:
                this.info = EnumGraphElementInfo.M.getDisplayValue()
                break
            case ComponentNode:
                this.info = EnumGraphElementInfo.C.getDisplayValue()
                break
            case OutPort:
                this.info = EnumGraphElementInfo.OUT.getDisplayValue()
                break
            case InPort:
                InPort port = (InPort) element
                this.info = EnumGraphElementInfo.IN.getDisplayValue()
                IntegerRange range = port.getConnectionCardinality()
                if (range != null) {
                    int numOfConn = port.getConnectionCount()
                    if (numOfConn == range.getFrom() && numOfConn == range.getTo()) {
                        this.info = EnumGraphElementInfo.IN.getDisplayValue()
                    } else if (numOfConn < range.getFrom()) {
                        this.info = EnumGraphElementInfo.IN_MORE_NEEDED.getDisplayValue()
                    } else if (numOfConn > range.getTo()) {
                        this.info = EnumGraphElementInfo.IN_LESS_NEEDED.getDisplayValue()
                    } else {
                        this.info = EnumGraphElementInfo.IN_MORE_POSSIBLE.getDisplayValue()
                    }
                }
                break

        }
    }

}