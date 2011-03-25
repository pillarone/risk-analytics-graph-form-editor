package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.applicationframework.application.form.BeanFormDialog;
import com.ulcjava.base.application.*;
import com.ulcjava.base.application.event.ActionEvent;
import com.ulcjava.base.application.event.IActionListener;
import com.ulcjava.base.application.event.IWindowListener;
import com.ulcjava.base.application.event.WindowEvent;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.TypeImportFormModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.beans.TypeImportBean;


public class TypeImportDialog extends ULCDialog {
    private BeanFormDialog<TypeImportFormModel> fBeanForm;
    private ULCButton fCancel;

    public TypeImportDialog(ULCWindow parent) {
        super(parent);
        boolean metalLookAndFeel = "Metal".equals(ClientContext.getLookAndFeelName());
        if (!metalLookAndFeel && ClientContext.getLookAndFeelSupportsWindowDecorations()) {
            setUndecorated(true);
            setWindowDecorationStyle(ULCDialog.PLAIN_DIALOG);
        }
        createBeanView();
        setTitle("Add new type (model | component)");
        setLocationRelativeTo(parent);
    }

    @SuppressWarnings("serial")
    private void createBeanView() {
        TypeImportFormModel model = new TypeImportFormModel(new TypeImportBean());
        TypeImportForm form = new TypeImportForm(model);
        fBeanForm = new BeanFormDialog<TypeImportFormModel>(form);
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

    public BeanFormDialog<TypeImportFormModel> getBeanForm() {
        return fBeanForm;
    }
}
