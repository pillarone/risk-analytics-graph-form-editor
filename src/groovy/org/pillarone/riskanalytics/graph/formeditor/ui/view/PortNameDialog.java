package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.applicationframework.application.form.BeanFormDialog;
import com.ulcjava.base.application.ClientContext;
import com.ulcjava.base.application.ULCButton;
import com.ulcjava.base.application.ULCDialog;
import com.ulcjava.base.application.ULCWindow;
import com.ulcjava.base.application.event.*;
import com.ulcjava.base.application.util.KeyStroke;
import org.pillarone.riskanalytics.graph.core.graph.model.*;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.ConnectionFormModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.PortNameFormModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.beans.ConnectionBean;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.beans.NameBean;
import org.pillarone.riskanalytics.graph.formeditor.util.GraphModelUtilities;


public class PortNameDialog extends ULCDialog {
    private BeanFormDialog<PortNameFormModel> fBeanForm;
    private ULCButton fCancel;
    private ComposedComponentGraphModel fGraphModel;
    private Port fPort;

    public PortNameDialog(ULCWindow parent, ComposedComponentGraphModel graphModel, Port port) {
        super(parent);
        boolean metalLookAndFeel = "Metal".equals(ClientContext.getLookAndFeelName());
        if (!metalLookAndFeel && ClientContext.getLookAndFeelSupportsWindowDecorations()) {
            setUndecorated(true);
            setWindowDecorationStyle(ULCDialog.PLAIN_DIALOG);
        }

        fGraphModel = graphModel;
        createBeanView();
        setTitle("Port Name");
        setLocationRelativeTo(parent);
        fPort = port;
    }

    @SuppressWarnings("serial")
    private void createBeanView() {
        PortNameFormModel formModel = new PortNameFormModel(new NameBean(), fGraphModel);
        PortNameForm form = new PortNameForm(formModel);
        fBeanForm = new BeanFormDialog<PortNameFormModel>(form);
        add(fBeanForm.getContentPane());
        fCancel = new ULCButton("Cancel");
        fCancel.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent event) {
                fBeanForm.reset();
                setVisible(false);
            }
        });
        fBeanForm.addToButtons(fCancel);

        IActionListener saveActionListener = new IActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (fBeanForm.getModel().hasErrors()) return;
                NameBean bean = (NameBean) fBeanForm.getModel().getBean();
                boolean isInPort = fPort instanceof InPort;
                Class packetType = fPort.getPacketType();
                Port replicate = isInPort ? new InPort() : new OutPort();
                replicate.setPacketType(packetType);
                replicate.setName(bean.getName());
                replicate.setComposedComponentOuterPort(true);
                fGraphModel.addOuterPort(replicate);
                if (isInPort) {
                    fGraphModel.createConnection(replicate, fPort);
                } else {
                    fGraphModel.createConnection(fPort, replicate);
                }
                setVisible(false);
            }
        };
        fBeanForm.addSaveActionListener(saveActionListener);
        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
        form.registerKeyboardAction(enter, saveActionListener);
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

    public BeanFormDialog<PortNameFormModel> getBeanForm() {
        return fBeanForm;
    }

}
