package org.pillarone.riskanalytics.graph.formeditor.ui;

import com.ulcjava.base.application.ULCBoxPane;
import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel;

/**
 * A view on a graph model should implement that interface.
 *
 * @author martin.melchior
 */
public interface GraphModelViewable {

    public ULCBoxPane getView();

    public void setVisible(boolean visible);

    public void injectGraphModel(AbstractGraphModel model);

}
