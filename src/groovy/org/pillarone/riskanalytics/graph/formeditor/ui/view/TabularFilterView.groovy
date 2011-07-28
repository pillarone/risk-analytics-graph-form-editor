package org.pillarone.riskanalytics.graph.formeditor.ui.view

import com.ulcjava.base.application.ULCBoxPane
import com.ulcjava.base.application.ULCTextField
import com.ulcjava.base.application.ULCButton
import com.ulcjava.base.application.util.Dimension
import org.pillarone.riskanalytics.graph.formeditor.util.UIUtils
import com.ulcjava.base.application.event.IActionListener
import com.ulcjava.base.application.event.ActionEvent
import com.ulcjava.base.application.event.KeyEvent
import com.ulcjava.base.application.event.IKeyListener
import com.ulcjava.base.application.ULCAlert

/**
 * @author fouad.jaada@intuitive-collaboration.com
 */
class TabularFilterView {

    ULCBoxPane content
    ULCTextField searchTextField
    ULCButton cleanButton

    public TabularFilterView() {
        init()
    }

    public void init() {
        initComponents()
        layoutComponents()
        attachListeners()
    }

    protected void initComponents() {
        content = new ULCBoxPane(2, 1)
        content.setPreferredSize(new Dimension(220, 20))

        searchTextField = new ULCTextField()

        cleanButton = new ULCButton(UIUtils.getIcon("delete-active.png"))
        cleanButton.setPreferredSize(new Dimension(16, 16));
        cleanButton.setContentAreaFilled(false);
        cleanButton.setOpaque(false);


    }

    protected void layoutComponents() {
        content.add(ULCBoxPane.BOX_EXPAND_CENTER, searchTextField);
        content.add(ULCBoxPane.BOX_LEFT_CENTER, cleanButton)
    }

    protected void attachListeners() {
        cleanButton.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent event) {
                new ULCAlert("TODO", "action not implemented yet", "OK").show()
            }
        });

        searchTextField.addKeyListener([keyTyped: {KeyEvent keyEvent ->
            new ULCAlert("TODO", "action not implemented yet", "OK").show()
        }] as IKeyListener)

    }
}
