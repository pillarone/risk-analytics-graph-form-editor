package org.pillarone.riskanalytics.graph.formeditor.ui.view.dialogs;

import com.ulcjava.applicationframework.application.form.BeanFormDialog;
import com.ulcjava.base.application.ClientContext;
import com.ulcjava.base.application.ULCButton;
import com.ulcjava.base.application.ULCDialog;
import com.ulcjava.base.application.ULCWindow;
import com.ulcjava.base.application.event.*;
import com.ulcjava.base.application.util.KeyStroke;
import org.pillarone.riskanalytics.graph.core.graph.model.*;
import org.pillarone.riskanalytics.graph.core.graph.util.UIUtils;
import org.pillarone.riskanalytics.graph.core.palette.model.ComponentDefinition;
import org.pillarone.riskanalytics.graph.core.palette.service.PaletteService;
import org.pillarone.riskanalytics.graph.formeditor.ui.IWatchList;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.NodeFormModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.beans.NodeBean;
import org.pillarone.riskanalytics.graph.formeditor.util.GraphModelUtilities;


public class NodeEditDialog extends ULCDialog {
    private BeanFormDialog<NodeFormModel> fBeanForm;
    private ULCButton fCancel;
    private final AbstractGraphModel fGraphModel;
    private ComponentNode fEditedNode;
    private IWatchList fWatchList;

    public NodeEditDialog(ULCWindow parent, AbstractGraphModel model) {
        super(parent);
        boolean metalLookAndFeel = "Metal".equals(ClientContext.getLookAndFeelName());
        if (!metalLookAndFeel && ClientContext.getLookAndFeelSupportsWindowDecorations()) {
            setUndecorated(true);
            setWindowDecorationStyle(ULCDialog.PLAIN_DIALOG);
        }

        fGraphModel = model;
        createBeanView();
        setTitle("Component Node");
        setLocationRelativeTo(parent);
    }

    public ComponentNode getEditedNode() {
        return fEditedNode;
    }

    public void setEditedNode(ComponentNode node) {
        this.fEditedNode = node;
    }

    private static boolean isConsistent(ComponentNode node, NodeBean bean) {
        return bean.getName().equals(node.getName())
                && bean.getComponentType().equals(node.getType().getTypeClass().getName());
    }


    @SuppressWarnings("serial")
    private void createBeanView() {
        NodeFormModel model = new NodeFormModel(new NodeBean(), fGraphModel);
        NodeForm form = new NodeForm(model, fGraphModel instanceof ModelGraphModel);
        fBeanForm = new BeanFormDialog<NodeFormModel>(form);
        add(fBeanForm.getContentPane());

        // cancel
        fCancel = new ULCButton("Cancel");
        IActionListener cancelAction = new IActionListener() {
            public void actionPerformed(ActionEvent event) {
                fBeanForm.reset();
                setVisible(false);
            }
        };
        fCancel.addActionListener(cancelAction);
        fBeanForm.addToButtons(fCancel);
        /*KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        form.registerKeyboardAction(esc, cancelAction);*/

        // ok
        IActionListener action = new IActionListener() {
            public void actionPerformed(ActionEvent event) {
                NodeBean bean = fBeanForm.getModel().getBean();
                if (fBeanForm.getModel().hasErrors()) return;
                if (fEditedNode != null) {
                    String technicalName = UIUtils.formatTechnicalName(bean.getName(), ComponentNode.class, fGraphModel instanceof ComposedComponentGraphModel);
                    if (!fEditedNode.getName().equals(technicalName)) {
                        String oldPath = GraphModelUtilities.getPath(fEditedNode, fGraphModel);
                        String newPath = oldPath.substring(0, oldPath.lastIndexOf(fEditedNode.getName()))+technicalName;
                        adjustWatches(oldPath, newPath);
                        fGraphModel.changeNodeProperty(fEditedNode, "name", fEditedNode.getName(), technicalName);
                    }
                    if (!fEditedNode.getType().getTypeClass().getName().equals(bean.getComponentType())) {
                        ComponentDefinition newType = PaletteService.getInstance().getComponentDefinition(bean.getComponentType());
                        for (Connection connection : fGraphModel.getEmergingConnections(fEditedNode)) {
                            fGraphModel.removeConnection(connection);
                        }
                        fGraphModel.changeNodeProperty(fEditedNode, "type", fEditedNode.getType(), newType);

                        /*// new node needs to be created and the old one replaced
                        ComponentNode newNode = GraphModelUtilities.replaceComponentNode(fEditedNode, bean.getName(), bean.getComponentType(), fGraphModel);
                        if (bean.getComponentType().equals(fEditedNode.getType().getTypeClass().getName())) {
                            fEditedNode.setName(bean.getName());
                        } else {
                            ComponentNode newNode = GraphModelUtilities.replaceComponentNode(fEditedNode, bean.getName(), bean.getComponentType(), fGraphModel);
                            newNode.setComment(bean.getComment());
                            fEditedNode = newNode;
                        }*/
                    } else {
                        fEditedNode.setComment(bean.getComment());
                    }
                } else {
                    ComponentDefinition definition = PaletteService.getInstance().getComponentDefinition(bean.getComponentType());
                    String technicalName = UIUtils.formatTechnicalName(bean.getName(), ComponentNode.class, fGraphModel instanceof ComposedComponentGraphModel);
                    ComponentNode newNode = fGraphModel.createComponentNode(definition, technicalName);
                    newNode.setComment(bean.getComment());
                    newNode.setRectangle(bean.getPosition());
                }
                setVisible(false);
            }
        };
        fBeanForm.addSaveActionListener(action);
        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
        form.registerKeyboardAction(enter, action);

        form.addKeyListener();

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new IWindowListener() {
            public void windowClosing(WindowEvent event) {
                fBeanForm.interceptIfDirty(new Runnable() {
                    public void run() {
                        setVisible(false);
                    }
                });
            }
        });
        pack();
    }

    public BeanFormDialog<NodeFormModel> getBeanForm() {
        return fBeanForm;
    }

    public void setWatchList(IWatchList watchList) {
        this.fWatchList = watchList;
    }

    public void adjustWatches(String oldPath, String newPath) {
        if (fWatchList != null) {
            fWatchList.editWatch(oldPath, newPath);
        }
    }

}
