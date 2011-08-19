package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.canoo.ulc.graph.ULCGraphPalette;
import com.canoo.ulc.graph.shared.PortConstraint;
import com.canoo.ulc.graph.shared.PortTemplate;
import com.canoo.ulc.graph.shared.PortType;
import com.canoo.ulc.graph.shared.ShapeTemplate;
import org.pillarone.riskanalytics.graph.core.graph.util.UIUtils;
import org.pillarone.riskanalytics.graph.core.palette.model.ComponentDefinition;
import org.pillarone.riskanalytics.graph.core.palette.service.IPaletteServiceListener;
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
public class ComponentWidgetsView extends ULCGraphPalette implements ISearchListener {
    public ComponentWidgetsView() {
        super();
        PaletteService paletteService = PaletteService.getInstance();
        List<ComponentDefinition> allComponentDefinitions = paletteService.getAllComponentDefinitions();

        for (ComponentDefinition definition : allComponentDefinitions) {
            final ShapeTemplate shapeTemplate = createShapeTemplate(definition);

            addShapeTemplate(shapeTemplate);
        }
        setPreferredWidth(50);
        paletteService.addPaletteServiceListener(new IPaletteServiceListener() {
            public void componentDefinitionAdded(ComponentDefinition definition) {
                addShapeTemplate(createShapeTemplate(definition));
            }
        });
    }

    protected ShapeTemplate createShapeTemplate(ComponentDefinition definition) {
        final ShapeTemplate shapeTemplate = new ShapeTemplate(ShapeTemplate.ShapeType.Container, definition.getTypeClass().getName(), definition.getTypeClass().getSimpleName());

        for (Map.Entry<Field, Class> entry : GroovyUtils.obtainPorts(definition, "in").entrySet()) {
            shapeTemplate.addPortTemplate(
                    new PortTemplate(PortType.IN, entry.getValue().getName(), UIUtils.formatDisplayName(entry.getKey().getName()), Arrays.asList(new PortConstraint(entry.getValue().getName(), 0, 100)))
            );
        }

        for (Map.Entry<Field, Class> entry : GroovyUtils.obtainPorts(definition, "out").entrySet()) {
            shapeTemplate.addPortTemplate( new PortTemplate(PortType.OUT, entry.getValue().getName(), UIUtils.formatDisplayName(entry.getKey().getName()), Arrays.asList(new PortConstraint(entry.getValue().getName(), 0, Integer.MAX_VALUE))));
        }
        return shapeTemplate;
    }


    public void search(String text) {
        System.out.println("looking for shape with name : "+text);
    }


}
