package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.base.application.*;
import com.ulcjava.base.application.event.ActionEvent;
import com.ulcjava.base.application.event.IActionListener;
import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.model.filters.ComponentNodeFilterFactory;
import org.pillarone.riskanalytics.graph.core.graph.model.filters.IComponentNodeFilter;


public class ModelFilterPane extends ULCBoxPane {
    private AbstractGraphModel fGraphModel;
    public ModelFilterPane(AbstractGraphModel model) {
        super(1,1);
        fGraphModel = model;
        this.add(ULCBoxPane.BOX_EXPAND_EXPAND, createFilterSelectionPane());
    }

    private ULCComponent createFilterSelectionPane() {
        ULCBoxPane rootPane = new ULCBoxPane(2,4);
        rootPane.setBorder(BorderFactory.createTitledBorder("Filter Model"));
        rootPane.add(BOX_LEFT_TOP, new ULCLabel("Filter Type: "));
        final ULCComboBox filterType = new ULCComboBox(ComponentNodeFilterFactory.getFilterModelNames());
        rootPane.add(BOX_LEFT_TOP, filterType);
        rootPane.add(BOX_LEFT_TOP, new ULCLabel("Value: "));
        final ULCTextField filterValue = new ULCTextField(10);
        filterValue.setEditable(false);
        // TODO: Validation of what has been entered
        filterType.addActionListener(
                new IActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        String filterModelName = (String) filterType.getSelectedItem();
                        if (filterModelName.equalsIgnoreCase(ComponentNodeFilterFactory.NONE)) {
                            filterValue.setEditable(false);
                        } else {
                            filterValue.setEditable(true);
                        }
                    }
                }
        );
        rootPane.add(BOX_LEFT_TOP, filterValue);

        rootPane.add(2, ULCFiller.createVerticalGlue());

        ULCButton clear = new ULCButton("Clear");
        clear.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent event) {
                filterValue.setText("");
                filterType.setSelectedItem(ComponentNodeFilterFactory.NONE);
                fGraphModel.clearNodeFilters();
            }
        });
        rootPane.add(BOX_LEFT_BOTTOM, clear);

        ULCButton apply = new ULCButton("Apply");
        apply.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent event) {
                String expr = filterValue.getText();
                String filterModelName = (String) filterType.getSelectedItem();
                IComponentNodeFilter filter = ComponentNodeFilterFactory.getFilter(filterModelName, expr);
                if (filter != null) {
                    fGraphModel.clearNodeFilters();
                    fGraphModel.addNodeFilter(filter);
                }
            }
        });
        rootPane.add(BOX_RIGHT_BOTTOM, apply);
        return rootPane;
    }
}
