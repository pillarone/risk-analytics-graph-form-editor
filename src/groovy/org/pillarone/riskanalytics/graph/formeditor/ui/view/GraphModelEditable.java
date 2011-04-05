package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.applicationframework.application.Action;
import com.ulcjava.base.application.ULCAlert;
import com.ulcjava.base.application.event.IWindowListener;
import com.ulcjava.base.application.event.WindowEvent;
import org.pillarone.riskanalytics.graph.core.graph.model.ComponentNode;
import org.pillarone.riskanalytics.graph.core.graph.model.Connection;
import org.pillarone.riskanalytics.graph.core.graph.model.ModelGraphModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.handlers.TypeTransferHandler;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.beans.ConnectionBean;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.beans.NodeBean;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.beans.ReplicationBean;
import org.pillarone.riskanalytics.graph.formeditor.util.GraphModelUtilities;

/**
 * Created by IntelliJ IDEA.
 * User: martin.melchior
 * Date: 04.04.11
 * Time: 15:08
 * To change this template use File | Settings | File Templates.
 */
public interface GraphModelEditable {

    public void setTransferHandler(TypeTransferHandler transferHandler);

    public void newNodeAction();

    public void newNodeAction(String componentType);

    public void modifyNodeAction();

    public void connectSelectedNodesAction();

    public void removeNodeAction();

    public void newConnectionAction();

    public void newReplicationAction();

    public void modifyConnectionAction();

    public void removeConnectionAction();

}
