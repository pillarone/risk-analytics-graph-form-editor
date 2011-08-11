package org.pillarone.riskanalytics.graph.formeditor.ui.view

import org.pillarone.riskanalytics.graph.formeditor.ui.model.palette.TypeTreeNode
import com.canoo.ulc.graph.shared.ShapeTemplate

/**
 * @author fouad.jaada@intuitive-collaboration.com
 */
interface INameFilter {

    public boolean accept(TypeTreeNode typeTreeNode)

    public boolean accept(ShapeTemplate shapeTemplate)

}
