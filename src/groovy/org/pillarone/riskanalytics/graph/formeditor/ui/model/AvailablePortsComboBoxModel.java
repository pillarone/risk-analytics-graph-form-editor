package org.pillarone.riskanalytics.graph.formeditor.ui.model;

import com.ulcjava.base.application.IComboBoxModel;
import com.ulcjava.base.application.event.IListDataListener;
import com.ulcjava.base.application.event.ListDataEvent;
import org.pillarone.riskanalytics.graph.core.graph.model.*;
import org.pillarone.riskanalytics.graph.formeditor.util.GraphModelUtilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AvailablePortsComboBoxModel implements IComboBoxModel {

    private static final int ALL = 3;
    private static final int IN = 1;
    private static final int OUT = 2;

    private AbstractGraphModel fGraphModel = null;
    private List<Port> availablePorts = null;
    private Port selectedPort = null;
    private static Comparator<Port> PORTCOMPARATOR = null;
    private int portsToInclude = ALL;
    private List<IListDataListener> fListDataListeners;

    static {
        PORTCOMPARATOR = new Comparator<Port>() {
            public int compare(Port p1, Port p2) {
                String port1Name = GraphModelUtilities.getPortName(p1);
                String port2Name = GraphModelUtilities.getPortName(p2);
                return port1Name.compareTo(port2Name);
            }
        };
    }

    public AvailablePortsComboBoxModel(AbstractGraphModel model, boolean in) {
        this.fGraphModel = model;
        this.portsToInclude = in ? IN : OUT;
        model.addGraphModelChangeListener(new NodeListener());
        availablePorts = getAvailablePorts(fGraphModel, portsToInclude);
        if (availablePorts != null && availablePorts.size() > 0) {
            selectedPort = availablePorts.get(0);
        }
    }

    public AvailablePortsComboBoxModel(AbstractGraphModel model) {
        this.fGraphModel = model;
        this.portsToInclude = ALL;
        model.addGraphModelChangeListener(new NodeListener());
        availablePorts = getAvailablePorts(fGraphModel, portsToInclude);
        if (availablePorts != null && availablePorts.size() > 0) {
            selectedPort = availablePorts.get(0);
        }
    }

    public int getSize() {
        return availablePorts.size();
    }

    public Object getElementAt(int index) {
        return GraphModelUtilities.getPortName(availablePorts.get(index));
    }

    public void addListDataListener(IListDataListener listener) {
        if (fListDataListeners == null) {
            fListDataListeners = new ArrayList<IListDataListener>();
        }
        fListDataListeners.add(listener);
    }

    public void removeListDataListener(IListDataListener listener) {
        if (fListDataListeners != null && fListDataListeners.contains(listener)) {
            fListDataListeners.remove(listener);
        }
    }

    public void setSelectedItem(Object anItem) {
        if (anItem instanceof String) {
            String name = (String) anItem;
            selectedPort = GraphModelUtilities.getPortFromName(name, fGraphModel);
        } else if (anItem instanceof Port) {
            selectedPort = (Port) anItem;
        }
    }

    public Object getSelectedItem() {
        return GraphModelUtilities.getPortName(selectedPort);
    }

    public Port getSelectedPort() {
        return selectedPort;
    }

    private class NodeListener implements IGraphModelChangeListener {
        public void nodeAdded(ComponentNode node) {
            availablePorts = getAvailablePorts(fGraphModel, portsToInclude);
            ListDataEvent e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, availablePorts.size() - 1);
            for (IListDataListener listener : fListDataListeners) {
                listener.contentsChanged(e);
            }
        }

        public void nodeRemoved(ComponentNode node) {
            availablePorts = getAvailablePorts(fGraphModel, portsToInclude);
            if (node == selectedPort.getComponentNode()) {
                selectedPort = availablePorts != null && availablePorts.size() > 0 ? availablePorts.get(0) : null;
            }
            ListDataEvent e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, availablePorts.size() - 1);
            for (IListDataListener listener : fListDataListeners) {
                listener.contentsChanged(e);
            }
        }

        public void nodesSelected(List<ComponentNode> nodes) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void connectionsSelected(List<Connection> connections) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void selectionCleared() {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void filtersApplied() {
            // nothing to do here
        }

        public void nodePropertyChanged(ComponentNode node, String propertyName, Object oldValue, Object newValue) {
            if (availablePorts.size()>0 && availablePorts.get(0).getComponentNode()==node) {
                ListDataEvent e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, availablePorts.size() - 1);
                for (IListDataListener listener : fListDataListeners) {
                    listener.contentsChanged(e);
                }
            }
        }

        public void connectionAdded(Connection c) {
            // TODO: possibly use this for validation ?
        }

        public void connectionRemoved(Connection c) {
            // TODO: possibly use this for validation ?
        }
    }

    private static void sortPortList(List<Port> ports) {
        if (ports != null) {
            Collections.sort(ports, PORTCOMPARATOR);
        }
    }

    public static List<Port> getAvailablePorts(AbstractGraphModel model, int portsToInclude) {
        List<Port> availablePorts = new ArrayList<Port>();
        for (ComponentNode node : model.getAllComponentNodes()) {
            if (portsToInclude == IN || portsToInclude == ALL) {
                availablePorts.addAll(node.getInPorts());
            }
            if (portsToInclude == OUT || portsToInclude == ALL) {
                availablePorts.addAll(node.getOutPorts());
            }
        }
        sortPortList(availablePorts);
        return availablePorts;
    }
}
