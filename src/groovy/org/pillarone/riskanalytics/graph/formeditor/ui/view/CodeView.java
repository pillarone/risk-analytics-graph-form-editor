package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.base.application.ULCBoxPane;
import com.ulcjava.base.application.ULCScrollPane;
import com.ulcjava.base.application.ULCTextArea;

public class CodeView extends ULCBoxPane {

    private ULCTextArea fTextArea;

    public CodeView() {
        super(true);
        createView();
    }

    private void createView() {
        fTextArea = new ULCTextArea();
        ULCScrollPane scrollPane = new ULCScrollPane(fTextArea);
        this.add(BOX_EXPAND_EXPAND, scrollPane);
        this.setVisible(true);
    }

    public void setText(String text) {
        fTextArea.setText(text);
    }

    public String getText() {
        return fTextArea.getText();
    }
}
