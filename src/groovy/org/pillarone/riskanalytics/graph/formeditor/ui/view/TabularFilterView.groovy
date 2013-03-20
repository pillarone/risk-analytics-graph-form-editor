package org.pillarone.riskanalytics.graph.formeditor.ui.view

import com.ulcjava.base.application.ULCBoxPane
import com.ulcjava.base.application.ULCButton
import com.ulcjava.base.application.ULCComponent
import com.ulcjava.base.application.ULCTextField
import com.ulcjava.base.application.event.ActionEvent
import com.ulcjava.base.application.event.IActionListener
import com.ulcjava.base.application.event.KeyEvent
import com.ulcjava.base.application.util.Dimension
import com.ulcjava.base.application.util.KeyStroke
import org.pillarone.riskanalytics.graph.formeditor.ui.ISearchListener
import org.pillarone.riskanalytics.graph.formeditor.util.UIUtils

/**
 * @author fouad.jaada@intuitive-collaboration.com
 */
class TabularFilterView {

    ULCBoxPane content
    ULCTextField searchTextField
    ULCButton cleanButton
    List<ISearchListener> searchListeners

    public TabularFilterView() {
        init()
        searchListeners = []
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
                searchListeners.each {ISearchListener searchListener ->
                    searchTextField.setText("")
                    searchListener.search(null)
                }
            }
        });

        IActionListener actionListener = new IActionListener() {
            void actionPerformed(ActionEvent actionEvent) {
                searchListeners.each {ISearchListener searchListener ->
                    searchListener.search(searchTextField.getText())
                }
            }
        }

        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
        searchTextField.registerKeyboardAction(actionListener, enter, ULCComponent.WHEN_FOCUSED);

    }

    public void addSearchListener(ISearchListener searchListener) {
        searchListeners << searchListener
    }

    public void removeSearchListener(ISearchListener searchListener) {
        searchListeners.remove(searchListener)
    }
}
