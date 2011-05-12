package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.base.application.*;
import com.ulcjava.base.application.event.ActionEvent;
import com.ulcjava.base.application.event.IActionListener;
import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.IComponentNodeFilter;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.IFilterChangedListener;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.filters.ComponentNodeFilterFactory;

import java.util.ArrayList;
import java.util.List;


public class ModelFilterPane extends ULCBoxPane {
    private List<IFilterChangedListener> fFilterChangedListeners;
    private AbstractGraphModel fGraphModel;
    public ModelFilterPane(AbstractGraphModel model) {
        super(1,1);
        fFilterChangedListeners = new ArrayList<IFilterChangedListener>();
        fGraphModel = model;
        this.add(ULCBoxPane.BOX_EXPAND_EXPAND, createFilterSelectionPane());
    }

    public void addFilterChangedListener(IFilterChangedListener listener) {
        if (listener != null && !fFilterChangedListeners.contains(listener)) {
            fFilterChangedListeners.add(listener);
        }
    }

    private ULCComponent createFilterSelectionPane() {
        ULCBoxPane rootPane = new ULCBoxPane(2,4);
        rootPane.setBorder(BorderFactory.createTitledBorder("Filter Model"));
        rootPane.add(BOX_LEFT_TOP, new ULCLabel("Filter Type: "));
        final ULCComboBox filterType = new ULCComboBox(ComponentNodeFilterFactory.getFilterModelNames());
        rootPane.add(BOX_LEFT_TOP, filterType);
        rootPane.add(BOX_LEFT_TOP, new ULCLabel("Value: "));
        final ULCTextField filterValue = new ULCTextField(20);
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
                filterChanged(ComponentNodeFilterFactory.getFilter(ComponentNodeFilterFactory.NONE, null));
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
                    filterChanged(filter);
                }
            }
        });
        rootPane.add(BOX_RIGHT_BOTTOM, apply);
        return rootPane;
    }

    final private void filterChanged(IComponentNodeFilter filter) {
        if (filter != null) {
            filter.setGraphModel(fGraphModel);
        }
        for (IFilterChangedListener listener : fFilterChangedListeners) {
            listener.applyFilter(filter);
        }
    }
}
