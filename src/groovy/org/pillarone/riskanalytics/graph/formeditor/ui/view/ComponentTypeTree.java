package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.base.application.*;
import com.ulcjava.base.application.event.ActionEvent;
import com.ulcjava.base.application.event.IActionListener;
import com.ulcjava.base.application.tree.*;
import com.ulcjava.base.application.util.Dimension;
import org.pillarone.riskanalytics.graph.core.palette.model.ComponentDefinition;
import org.pillarone.riskanalytics.graph.core.palette.service.PaletteService;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.ComponentTypeTreeModelFactory;
import org.pillarone.riskanalytics.graph.formeditor.util.ComponentTypeTreeUtilities;
import org.pillarone.riskanalytics.graph.formeditor.util.PaletteUtilities;

import java.util.*;
import java.util.Map.Entry;

public class ComponentTypeTree extends AbstractComponentDefinitionTree {

    public ComponentTypeTree(GraphModelEditor parent) {
        super(parent);
    }

    public ITreeModel getTreeModel() {
        return ComponentTypeTreeModelFactory.getTree();
    }
}
