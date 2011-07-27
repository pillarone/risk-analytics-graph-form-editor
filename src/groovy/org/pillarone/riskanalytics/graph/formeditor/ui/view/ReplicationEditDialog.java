package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.applicationframework.application.form.BeanFormDialog;
import com.ulcjava.base.application.ClientContext;
import com.ulcjava.base.application.ULCButton;
import com.ulcjava.base.application.ULCDialog;
import com.ulcjava.base.application.ULCWindow;
import com.ulcjava.base.application.event.*;
import com.ulcjava.base.application.util.KeyStroke;
import org.pillarone.riskanalytics.core.packets.Packet;
import org.pillarone.riskanalytics.graph.core.graph.model.ComposedComponentGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.model.InPort;
import org.pillarone.riskanalytics.graph.core.graph.model.Port;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.ReplicationFormModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.beans.ReplicationBean;
import org.pillarone.riskanalytics.graph.formeditor.util.GraphModelUtilities;


public class ReplicationEditDialog extends ULCDialog {
    private BeanFormDialog<ReplicationFormModel> fBeanForm;
    private ULCButton fCancel;
    private ComposedComponentGraphModel fGraphModel;

    public ReplicationEditDialog(ULCWindow parent, ComposedComponentGraphModel graphModel) {
        super(parent);
        boolean metalLookAndFeel = "Metal".equals(ClientContext.getLookAndFeelName());
        if (!metalLookAndFeel && ClientContext.getLookAndFeelSupportsWindowDecorations()) {
            setUndecorated(true);
            setWindowDecorationStyle(ULCDialog.PLAIN_DIALOG);
        }
        fGraphModel = graphModel;
        createBeanView();
        setTitle("Replication");
        setLocationRelativeTo(parent);
    }

    @SuppressWarnings("serial")
    private void createBeanView() {
        ReplicationFormModel formModel = new ReplicationFormModel(new ReplicationBean(), fGraphModel);
        ReplicationForm form = new ReplicationForm(formModel, fGraphModel);
        fBeanForm = new BeanFormDialog<ReplicationFormModel>(form);
        add(fBeanForm.getContentPane());
        fCancel = new ULCButton("Cancel");
        fCancel.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent event) {
                fBeanForm.reset();
                setVisible(false);
            }
        });

        fBeanForm.addToButtons(fCancel);
        IActionListener action = new IActionListener() {
            public void actionPerformed(ActionEvent event) {
                if(fBeanForm.getModel().hasErrors()) return;
                ReplicationBean bean = (ReplicationBean) fBeanForm.getModel().getBean();
                Port inner = GraphModelUtilities.getPortFromName(bean.getInner(), fGraphModel);
                Class<? extends Packet> packetClass = inner.getPacketType();
                if (inner instanceof InPort) {
                    Port replica = fGraphModel.createOuterInPort(packetClass, bean.getOuter());
                    fGraphModel.createConnection(replica, inner);
                } else {
                    Port replica = fGraphModel.createOuterOutPort(packetClass, bean.getOuter());
                    fGraphModel.createConnection(inner, replica);
                }
                setVisible(false);
            }
        };

        fBeanForm.addSaveActionListener(action);
        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
        form.registerKeyboardAction(enter, action);

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

    public BeanFormDialog<ReplicationFormModel> getBeanForm() {
        return fBeanForm;
    }

}
