package org.pillarone.riskanalytics.graph.formeditor.ui.view.dialogs;

import com.ulcjava.applicationframework.application.form.BeanFormDialog;
import com.ulcjava.base.application.ClientContext;
import com.ulcjava.base.application.ULCButton;
import com.ulcjava.base.application.ULCDialog;
import com.ulcjava.base.application.ULCWindow;
import com.ulcjava.base.application.event.*;
import com.ulcjava.base.application.util.KeyStroke;
import org.pillarone.riskanalytics.graph.core.graph.model.ComposedComponentGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.model.InPort;
import org.pillarone.riskanalytics.graph.core.graph.model.OutPort;
import org.pillarone.riskanalytics.graph.core.graph.model.Port;
import org.pillarone.riskanalytics.graph.core.graph.util.UIUtils;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.PortNameFormModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.beans.NameBean;


public class PortNameDialog extends ULCDialog {
    private BeanFormDialog<PortNameFormModel> fBeanForm;
    private ULCButton fCancel;
    private ComposedComponentGraphModel fGraphModel;
    private Port fPort;

    public PortNameDialog(ULCWindow parent, ComposedComponentGraphModel graphModel, Port port) {
        super(parent);
        fPort = port;
        boolean metalLookAndFeel = "Metal".equals(ClientContext.getLookAndFeelName());
        if (!metalLookAndFeel && ClientContext.getLookAndFeelSupportsWindowDecorations()) {
            setUndecorated(true);
            setWindowDecorationStyle(ULCDialog.PLAIN_DIALOG);
        }

        fGraphModel = graphModel;
        createBeanView();
        setTitle("Port Name");
        setLocationRelativeTo(parent);

    }

    @SuppressWarnings("serial")
    private void createBeanView() {
        PortNameFormModel formModel = new PortNameFormModel(new NameBean(), fGraphModel, fPort.getPrefix());
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
                if (!validate()) return;
                NameBean bean = (NameBean) fBeanForm.getModel().getBean();
                boolean isInPort = fPort instanceof InPort;
                Class packetType = fPort.getPacketType();
                Port replicate = isInPort ? new InPort() : new OutPort();
                replicate.setPacketType(packetType);
                replicate.setName(UIUtils.formatTechnicalName(bean.getName(), replicate.getClass(), false));
                replicate.setComposedComponentOuterPort(true);
                fGraphModel.addOuterPort(replicate);
                if (isInPort) {
                    fGraphModel.createConnection(replicate, fPort);
                } else {
                    fGraphModel.createConnection(fPort, replicate);
                }
                setVisible(false);
            }

            private boolean validate() {
                if (fBeanForm.getModel().hasErrors()) return false;
                //if (Port.IN_PORT_PREFIX.equals(fBeanForm.getModel().getBean().getName())) return false;
                //if (Port.OUT_PORT_PREFIX.equals(fBeanForm.getModel().getBean().getName())) return false;
                return true;
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
