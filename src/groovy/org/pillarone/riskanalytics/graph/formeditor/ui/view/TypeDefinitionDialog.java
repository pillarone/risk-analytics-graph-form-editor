package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.applicationframework.application.form.BeanFormDialog;
import com.ulcjava.base.application.*;
import com.ulcjava.base.application.event.*;
import com.ulcjava.base.application.util.KeyStroke;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.TypeDefinitionFormModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.beans.TypeDefinitionBean;

import java.util.Collection;


public class TypeDefinitionDialog extends ULCDialog {
    private BeanFormDialog<TypeDefinitionFormModel> fBeanForm;
    public TypeDefinitionForm form;
    private ULCButton fCancel;
    private Collection<TypeDefinitionBean> fTypeDefs;

    public TypeDefinitionDialog(ULCWindow parent, Collection<TypeDefinitionBean> typeDefs) {
        super(parent);
        boolean metalLookAndFeel = "Metal".equals(ClientContext.getLookAndFeelName());
        if (!metalLookAndFeel && ClientContext.getLookAndFeelSupportsWindowDecorations()) {
            setUndecorated(true);
            setWindowDecorationStyle(ULCDialog.PLAIN_DIALOG);
        }

        fTypeDefs = typeDefs;
        createBeanView();
        setTitle("Add new type (model | composed component)");
        setLocationRelativeTo(parent);
    }

    @SuppressWarnings("serial")
    private void createBeanView() {
        TypeDefinitionFormModel model = new TypeDefinitionFormModel(new TypeDefinitionBean());
        form = new TypeDefinitionForm(model);
        fBeanForm = new BeanFormDialog<TypeDefinitionFormModel>(form);
        add(fBeanForm.getContentPane());
        fCancel = new ULCButton("Cancel");
        fCancel.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent event) {
                fBeanForm.reset();
                setVisible(false);
            }
        });
        fBeanForm.addToButtons(fCancel);

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
        fBeanForm.getModel().getBean().setBaseType("Model");
        pack();
    }

    public BeanFormDialog<TypeDefinitionFormModel> getBeanForm() {
        return fBeanForm;
    }

    public TypeDefinitionForm getTypeDefinitionForm(){
        return form;
    }

}
