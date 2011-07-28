package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.canoo.ulc.graph.ULCGraphPalette;
import com.canoo.ulc.graph.shared.PortConstraint;
import com.canoo.ulc.graph.shared.PortTemplate;
import com.canoo.ulc.graph.shared.PortType;
import com.canoo.ulc.graph.shared.ShapeTemplate;
import org.pillarone.riskanalytics.graph.core.graph.util.UIUtils;
import org.pillarone.riskanalytics.graph.core.palette.model.ComponentDefinition;
import org.pillarone.riskanalytics.graph.core.palette.service.PaletteService;
import org.pillarone.riskanalytics.graph.formeditor.util.GroovyUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class ComponentWidgetsView extends ULCGraphPalette {
    public ComponentWidgetsView() {
        super();
        List<ComponentDefinition> allComponentDefinitions = PaletteService.getInstance().getAllComponentDefinitions();

        for (ComponentDefinition definition : allComponentDefinitions) {
            final ShapeTemplate shapeTemplate = new ShapeTemplate(ShapeTemplate.ShapeType.Container, definition.getTypeClass().getName(), definition.getTypeClass().getSimpleName());

            for (Map.Entry<Field, Class> entry : GroovyUtils.obtainPorts(definition, "in").entrySet()) {
                shapeTemplate.addPortTemplate(
                        new PortTemplate(PortType.IN, entry.getValue().getName(), UIUtils.formatDisplayName(entry.getKey().getName()), Arrays.asList(new PortConstraint(entry.getValue().getName(), 0, 100)))
                );
            }

            for (Map.Entry<Field, Class> entry : GroovyUtils.obtainPorts(definition, "out").entrySet()) {
                shapeTemplate.addPortTemplate(
                        new PortTemplate(PortType.OUT, entry.getValue().getName(), UIUtils.formatDisplayName(entry.getKey().getName()), Arrays.asList(new PortConstraint(entry.getValue().getName(), 0, 100)))
                );
            }

            addShapeTemplate(shapeTemplate);
        }
        setPreferredWidth(50);
    }
}
