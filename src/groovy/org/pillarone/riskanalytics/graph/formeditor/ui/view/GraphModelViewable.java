package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.base.application.ULCBoxPane;
import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.model.ComponentNode;
import org.pillarone.riskanalytics.graph.core.graph.model.Connection;
import org.pillarone.riskanalytics.graph.core.graph.model.filters.IComponentNodeFilter;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: martin.melchior
 * Date: 04.04.11
 * Time: 15:22
 * To change this template use File | Settings | File Templates.
 */
public interface GraphModelViewable {

    public ULCBoxPane getView();

    public void setVisible(boolean visible);

    public void injectGraphModel(AbstractGraphModel model);

}
