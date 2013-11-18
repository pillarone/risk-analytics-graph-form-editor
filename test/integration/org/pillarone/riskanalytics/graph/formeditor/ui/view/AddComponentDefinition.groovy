package org.pillarone.riskanalytics.graph.formeditor.ui.view

import com.ulcjava.testframework.ServerSideCommand
import org.pillarone.riskanalytics.graph.core.palette.model.ComponentDefinition
import org.pillarone.riskanalytics.graph.core.palette.service.PaletteService

class AddComponentDefinition extends ServerSideCommand {
    private Class typeClass

    AddComponentDefinition(Class typeClass) {
        this.typeClass = typeClass
    }

    @Override
    protected void proceedOnServer() {
        PaletteService.instance.addComponentDefinition(new ComponentDefinition(typeClass: typeClass))
    }
}
