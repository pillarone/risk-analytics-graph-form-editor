package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.applicationframework.application.form.BeanFormDialog;
import com.ulcjava.base.application.ClientContext;
import com.ulcjava.base.application.ULCButton;
import com.ulcjava.base.application.ULCDialog;
import com.ulcjava.base.application.ULCWindow;
import com.ulcjava.base.application.event.*;
import com.ulcjava.base.application.util.KeyStroke;
import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.model.Port;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.ConnectionFormModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.beans.ConnectionBean;
import org.pillarone.riskanalytics.graph.formeditor.util.GraphModelUtilities;
import org.pillarone.riskanalytics.graph.formeditor.util.UIUtils;


public class ConnectionEditDialog extends ULCDialog {
    private BeanFormDialog<ConnectionFormModel> fBeanForm;
    private ULCButton fCancel;
    private AbstractGraphModel fGraphModel;

    public ConnectionEditDialog(ULCWindow parent, AbstractGraphModel graphModel) {
        super(parent);
        boolean metalLookAndFeel = "Metal".equals(ClientContext.getLookAndFeelName());
        if (!metalLookAndFeel && ClientContext.getLookAndFeelSupportsWindowDecorations()) {
            setUndecorated(true);
            setWindowDecorationStyle(ULCDialog.PLAIN_DIALOG);
        }

        fGraphModel = graphModel;
        createBeanView();
        setTitle("Connection");
        setLocationRelativeTo(parent);
    }

    @SuppressWarnings("serial")
    private void createBeanView() {
        ConnectionFormModel formModel = new ConnectionFormModel(new ConnectionBean(), fGraphModel);
        ConnectionForm form = new ConnectionForm(formModel, fGraphModel);
        fBeanForm = new BeanFormDialog<ConnectionFormModel>(form);
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
                ConnectionBean bean = (ConnectionBean) fBeanForm.getModel().getBean();
                Port from = UIUtils.getPortFromConnectionEntryName(bean.getFrom(), fGraphModel, false);
                Port to = UIUtils.getPortFromConnectionEntryName(bean.getTo(), fGraphModel, true);
                if (from.allowedToConnectTo(to)) {
                    fGraphModel.createConnection(from, to);
                    setVisible(false);
                }
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

    public BeanFormDialog<ConnectionFormModel> getBeanForm() {
        return fBeanForm;
    }

}
