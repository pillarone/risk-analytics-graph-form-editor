package org.pillarone.riskanalytics.graph.formeditor.util;

import com.canoo.ulc.graph.model.Port;
import com.canoo.ulc.graph.model.Vertex;
import com.canoo.ulc.graph.shared.PortConstraint;
import com.canoo.ulc.graph.shared.PortTemplate;
import com.canoo.ulc.graph.shared.PortType;
import com.canoo.ulc.graph.shared.ShapeTemplate;
import com.ulcjava.base.application.util.Dimension;
import com.ulcjava.base.application.util.Point;
import com.ulcjava.base.application.util.Rectangle;
import org.jetbrains.annotations.NotNull;
import org.pillarone.riskanalytics.graph.core.graph.util.*;
import org.pillarone.riskanalytics.graph.core.graph.util.UIUtils;
import org.pillarone.riskanalytics.graph.core.palette.model.ComponentDefinition;
import org.pillarone.riskanalytics.graph.core.palette.service.PaletteService;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: martin.melchior
 * Date: 11.08.11
 * Time: 15:14
 * To change this template use File | Settings | File Templates.
 */
public class VisualSceneUtilities {

    public static Vertex createVertex(Point mouseLocation, String componentTypePath) {
        Vertex vertex = new Vertex("internal" + new Date().getTime() + "_" + Math.random());
        vertex.setStyle("swimlane");
        if (mouseLocation==null) {
            mouseLocation = new Point(0,0);
        }
        vertex.setRectangle(new Rectangle(mouseLocation, new Dimension(200, 200)));
        vertex.setTemplateId(componentTypePath);

        // add the ports
        ComponentDefinition definition = PaletteService.getInstance().getComponentDefinition(componentTypePath);
        for (Map.Entry<Field, Class> entry : GroovyUtils.obtainPorts(definition, "in").entrySet()) {
            String id = "in_port_" + new Date().getTime() + "_"+Math.random();
            Port port = new Port(id, PortType.IN, entry.getValue().getName(), UIUtils.formatDisplayName(entry.getKey().getName()));
            port.addConstraint(new PortConstraint(entry.getValue().getName(), 0, 100)); // TODO - set the correct constraint here
            vertex.addPort(port);
        }
        for (Map.Entry<Field, Class> entry : GroovyUtils.obtainPorts(definition, "out").entrySet()) {
            String id = "out_port_" + new Date().getTime() + "_"+Math.random();
            Port port = new Port(id, PortType.OUT, entry.getValue().getName(), UIUtils.formatDisplayName(entry.getKey().getName()));
            port.addConstraint(new PortConstraint(entry.getValue().getName(), 0, Integer.MAX_VALUE));
            vertex.addPort(port);
        }
        return vertex;
    }
}
