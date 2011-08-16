package org.pillarone.riskanalytics.graph.formeditor.ui.view;

/**
 * Created by IntelliJ IDEA.
 * User: martin.melchior
 * Date: 04.04.11
 * Time: 15:08
 * To change this template use File | Settings | File Templates.
 */
public interface GraphModelEditable {

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
