package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.applicationframework.application.form.BeanFormDialog;
import com.ulcjava.base.application.ClientContext;
import com.ulcjava.base.application.ULCButton;
import com.ulcjava.base.application.ULCDialog;
import com.ulcjava.base.application.ULCWindow;
import com.ulcjava.base.application.event.ActionEvent;
import com.ulcjava.base.application.event.IActionListener;
import com.ulcjava.base.application.event.IWindowListener;
import com.ulcjava.base.application.event.WindowEvent;
import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.model.ComponentNode;
import org.pillarone.riskanalytics.graph.core.graph.model.ModelGraphModel;
import org.pillarone.riskanalytics.graph.core.palette.model.ComponentDefinition;
import org.pillarone.riskanalytics.graph.core.palette.service.PaletteService;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.NodeFormModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.beans.NodeBean;
import org.pillarone.riskanalytics.graph.formeditor.util.GraphModelUtilities;


public class NodeEditDialog extends ULCDialog {
    private BeanFormDialog<NodeFormModel> fBeanForm;
    private ULCButton fCancel;
    private final AbstractGraphModel fGraphModel;
    private ComponentNode fEditedNode;
    
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
    	NodeForm form = new NodeForm(model);
        fBeanForm = new BeanFormDialog<NodeFormModel>(form);
        add(fBeanForm.getContentPane());
        fCancel = new ULCButton("Cancel");
        fCancel.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent event) {
                fBeanForm.reset();
                setVisible(false);
            }
        });        
        fBeanForm.addToButtons(fCancel);
        fBeanForm.addSaveActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent event) {
            	NodeBean bean = (NodeBean)fBeanForm.getModel().getBean();
                if (fEditedNode != null) {
                	if (fEditedNode != null) {
                		if (!isConsistent(fEditedNode, bean)) {
                			GraphModelUtilities.replaceComponentNode(fEditedNode, bean.getName(), bean.getComponentType(), fGraphModel);
                		}                		
                		if (fGraphModel instanceof ModelGraphModel && bean.isStarter()) {
                			((ModelGraphModel) fGraphModel).getStartComponents().add(fEditedNode);
                		}                		
                	}
                } else {
                	ComponentDefinition definition = PaletteService.getInstance().getComponentDefinition(bean.getComponentType());
                	ComponentNode newNode = fGraphModel.createComponentNode(definition, bean.getName());
                	if (fGraphModel instanceof ModelGraphModel && bean.isStarter()) {
            			((ModelGraphModel) fGraphModel).getStartComponents().add(fEditedNode);                		
                	}
                }
                setVisible(false);
            }
        });
        
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
    
}
