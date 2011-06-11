package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.canoo.ulc.graph.ULCGraphPalette;
import com.canoo.ulc.graph.shared.ShapeTemplate;
import org.pillarone.riskanalytics.graph.core.palette.model.ComponentDefinition;
import org.pillarone.riskanalytics.graph.core.palette.service.PaletteService;

/**
 *
 */
public class ComponentWidgetsView extends ULCGraphPalette {
    public ComponentWidgetsView() {
        super();
        for (ComponentDefinition definition : PaletteService.getInstance().getAllComponentDefinitions()) {
            this.addShapeTemplate(new ShapeTemplate(ShapeTemplate.ShapeType.Container, definition.getTypeClass().getName(), definition.getTypeClass().getSimpleName()));
        }

    }
}
