package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.base.application.*;
import com.ulcjava.base.application.border.ULCAbstractBorder;
import com.ulcjava.base.application.event.*;
import com.ulcjava.base.application.util.Color;
import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.model.ComponentNode;
import org.pillarone.riskanalytics.graph.core.graph.model.InPort;
import org.pillarone.riskanalytics.graph.core.graph.model.Port;

import java.util.ArrayList;
import java.util.List;


public class ConnectNodesDialog extends ULCDialog {

    private ULCBoxPane fButtonPane;
    private ULCBoxPane fContentPane;
    private ULCButton fSaveButton;
    private ArrayList<IActionListener> fSaveActions;
    private ULCComboBox fBox1;
    private ULCTextField fNode1;
    private ULCComboBox fBox2;
    private ULCTextField fNode2;
    private AbstractGraphModel fGraphModel;


    public ConnectNodesDialog(ULCWindow parent, AbstractGraphModel model) {
        super(parent);
        boolean metalLookAndFeel = "Metal".equals(ClientContext.getLookAndFeelName());
        if (!metalLookAndFeel && ClientContext.getLookAndFeelSupportsWindowDecorations()) {
            setUndecorated(true);
            setWindowDecorationStyle(ULCDialog.PLAIN_DIALOG);
        }
        createView();
        setTitle("Connect Nodes");
        setLocationRelativeTo(parent);
        fGraphModel = model;
    }

    public void setNodes(ComponentNode node1, ComponentNode node2) {
        fNode1.setText(node1.getName());
        List<Port> ports1 = getPorts(node1);
        fBox1.setModel(new PortsComboBoxModel(ports1));
        fNode2.setText(node2.getName());
        List<Port> ports2 = getPorts(node2);
        fBox2.setModel(new PortsComboBoxModel(ports2));
    }

    public Port getSelectedPort1() {
        return ((PortsComboBoxModel) fBox1.getModel()).getSelectedPort();
    }

    public Port getSelectedPort2() {
        return ((PortsComboBoxModel) fBox2.getModel()).getSelectedPort();
    }

    private List<Port> getPorts(ComponentNode node) {
        List<Port> ports = new ArrayList<Port>();
        ports.addAll(node.getInPorts());
        ports.addAll(node.getOutPorts());
        return ports;
    }

    private ConnectNodesDialog getThis() {
        return this;
    }

    @SuppressWarnings("serial")
    private void createView() {
        fSaveActions = new ArrayList<IActionListener>();

        fContentPane = new ULCBoxPane(true);
        fContentPane.setBorder(createBorder(10, 10, 10, 10));

        ULCBoxPane node1Selection = new ULCBoxPane(false);
        node1Selection.setBorder(createBorder(0, 10, 0, 10));
        node1Selection.add(ULCBoxPane.BOX_EXPAND_EXPAND, new ULCLabel("Node:"));
        fNode1 = new ULCTextField();
        fNode1.setColumns(15);
        fNode1.setEditable(false);
        node1Selection.add(ULCBoxPane.BOX_EXPAND_EXPAND, fNode1);
        node1Selection.add(ULCBoxPane.BOX_EXPAND_TOP, ULCFiller.createHorizontalGlue());
        fBox1 = new ULCComboBox();
        node1Selection.add(ULCBoxPane.BOX_RIGHT_EXPAND, fBox1);
        fContentPane.add(ULCBoxPane.BOX_EXPAND_EXPAND, node1Selection);

        ULCBoxPane node2Selection = new ULCBoxPane(false);
        node2Selection.setBorder(createBorder(0, 10, 0, 10));
        node2Selection.add(ULCBoxPane.BOX_EXPAND_EXPAND, new ULCLabel("Node: "));
        fNode2 = new ULCTextField();
        fNode2.setColumns(15);
        fNode2.setEditable(false);
        node2Selection.add(ULCBoxPane.BOX_EXPAND_EXPAND, fNode2);
        node2Selection.add(ULCBoxPane.BOX_EXPAND_TOP, ULCFiller.createHorizontalGlue());
        fBox2 = new ULCComboBox();
        node2Selection.add(ULCBoxPane.BOX_RIGHT_EXPAND, fBox2);
        fContentPane.add(ULCBoxPane.BOX_EXPAND_EXPAND, node2Selection);

        ULCBoxPane buttonRow = new ULCBoxPane(false);
        buttonRow.add(ULCBoxPane.BOX_EXPAND_TOP, ULCFiller.createHorizontalGlue());
        fButtonPane = createButtonPane();
        buttonRow.add(ULCBoxPane.BOX_RIGHT_EXPAND, fButtonPane);
        fContentPane.add(ULCBoxPane.BOX_EXPAND_EXPAND, buttonRow);

        this.add(fContentPane);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new IWindowListener() {
            public void windowClosing(WindowEvent event) {
                setVisible(false);
            }
        });
        pack();
    }

    @SuppressWarnings("serial")
    private ULCBoxPane createButtonPane() {
        ULCButton cancelButton = new ULCButton("cancel");
        cancelButton.addActionListener(
                new IActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        getThis().setVisible(false);
                    }
                }
        );
        fSaveButton = new ULCButton("ok");
        setDefaultButton(fSaveButton);
        fSaveButton.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent event) {
                // validate
                Port p1 = getSelectedPort1();
                Port p2 = getSelectedPort2();
                if (!fGraphModel.isConnected(p1,p2) && p1.allowedToConnectTo(p2)) {
                    Port from, to;
                    if (p1 instanceof InPort) {
                        from = p2;
                        to = p1;
                    } else {
                        from = p1;
                        to = p2;
                    }
                    fGraphModel.createConnection(from, to);
                    for (IActionListener listener : fSaveActions) {
                        listener.actionPerformed(event);
                    }
                    setVisible(false);
                } else {
                    fBox2.setBackground(Color.yellow);
                    fBox2.setToolTipText("Specified ports cannot be connected.");
                }
            }
        });

        ULCBoxPane boxPane1 = new ULCBoxPane(false);
        boxPane1.setBorder(createBorder(0, 10, 0, 10));
        boxPane1.add(ULCBoxPane.BOX_RIGHT_EXPAND, fSaveButton);

        ULCBoxPane boxPane2 = new ULCBoxPane(false);
        boxPane2.setBorder(createBorder(0, 10, 0, 10));
        boxPane2.add(ULCBoxPane.BOX_LEFT_EXPAND, cancelButton);

        ULCBoxPane boxPane = new ULCBoxPane(false);
        boxPane.setBorder(createBorder(0, 10, 0, 10));
        boxPane.add(ULCBoxPane.BOX_LEFT_EXPAND, boxPane2);
        boxPane.add(ULCBoxPane.BOX_EXPAND_TOP, ULCFiller.createHorizontalGlue());
        boxPane.add(ULCBoxPane.BOX_RIGHT_EXPAND, boxPane1);

        return boxPane;
    }

    private ULCAbstractBorder createBorder(int top, int left, int buttom, int right) {
        return BorderFactory.createEmptyBorder(top, left, buttom, right);
    }

    public void addSaveActionListener(IActionListener saveActionListener) {
        fSaveActions.add(saveActionListener);
    }

    private class PortsComboBoxModel implements IComboBoxModel {

        private List<Port> fPorts;
        private Port fSelectedPort;

        protected PortsComboBoxModel(List<Port> ports) {
            fPorts = ports;
            fSelectedPort = ports.get(0);
        }

        public int getSize() {
            return fPorts.size();
        }

        public Object getElementAt(int index) {
            return fPorts.get(index).toString();
        }

        public void addListDataListener(IListDataListener listener) {
        }

        public void removeListDataListener(IListDataListener listener) {
        }

        public void setSelectedItem(Object item) {
            for (Port p : fPorts) {
                if (p.toString().equals((String) item)) {
                    fSelectedPort = p;
                    break;
                }
            }
        }

        public Object getSelectedItem() {
            return fSelectedPort.toString();
        }

        public Port getSelectedPort() {
            return fSelectedPort;
        }
    }
}
