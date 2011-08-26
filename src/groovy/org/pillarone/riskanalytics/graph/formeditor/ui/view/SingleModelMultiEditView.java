package org.pillarone.riskanalytics.graph.formeditor.ui.view;


import com.canoo.ulc.detachabletabbedpane.server.ITabListener;
import com.canoo.ulc.detachabletabbedpane.server.TabEvent;
import com.canoo.ulc.detachabletabbedpane.server.ULCCloseableTabbedPane;
import com.canoo.ulc.detachabletabbedpane.server.ULCDetachableTabbedPane;
import com.canoo.ulc.graph.ULCGraphOutline;
import com.ulcjava.applicationframework.application.AbstractBean;
import com.ulcjava.applicationframework.application.Action;
import com.ulcjava.applicationframework.application.ApplicationActionMap;
import com.ulcjava.applicationframework.application.ApplicationContext;
import com.ulcjava.applicationframework.application.form.BeanFormDialog;
import com.ulcjava.base.application.*;
import com.ulcjava.base.application.event.*;
import com.ulcjava.base.application.tabletree.ITableTreeNode;
import com.ulcjava.base.application.tree.TreePath;
import com.ulcjava.base.application.util.Dimension;
import com.ulcjava.base.application.util.KeyStroke;
import org.pillarone.riskanalytics.core.simulation.engine.SimulationRunner;
import org.pillarone.riskanalytics.core.simulation.item.Parameterization;
import org.pillarone.riskanalytics.core.util.MathUtils;
import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.model.ComposedComponentGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.model.ModelGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.model.Port;
import org.pillarone.riskanalytics.graph.core.graph.model.filters.ComponentNodeFilterFactory;
import org.pillarone.riskanalytics.graph.core.graph.model.filters.IComponentNodeFilter;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.DataNameFormModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.beans.NameBean;
import org.pillarone.riskanalytics.graph.formeditor.util.ProbeSimulationService;
import org.pillarone.riskanalytics.graph.formeditor.util.UIUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SingleModelMultiEditView extends AbstractBean {
    private ApplicationContext fApplicationContext;

    private AbstractGraphModel fGraphModel;
    private boolean fIsModel;

    private ULCBoxPane fMainView;
    private SingleModelVisualView fVisualEditorView;
    private SingleModelFormView fFormEditorView;
    private SingleModelTextView fTextEditorView;
    private ULCDetachableTabbedPane fLeftTabbedPane;
    private ULCDetachableTabbedPane fRightTabbedPane;
    private HelpView fHelpView;
    private CommentView fCommentView;
    private ULCCloseableTabbedPane fDataSetSheets;
    private ULCCloseableTabbedPane fResultSheets;
    private WatchesTable fWatchesTable;

    private IActionListener f9_pressed;

    public SingleModelMultiEditView(ApplicationContext ctx, AbstractGraphModel model) {
        super();
        fApplicationContext = ctx;
        boolean isModel = model instanceof ModelGraphModel;
        createView(isModel);
        injectGraphModel(model);
        f9_pressed = new IActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                simulateAction(false);
            }
        };
    }

    public void createView(boolean isModel) {
        fMainView = new ULCBoxPane(true, 1);

        ///////////////////
        // toolbar pane
        //////////////////

        // model filter tool
        ULCBoxPane modelFilterTool = new ULCBoxPane(false);
        modelFilterTool.add(ULCBoxPane.BOX_LEFT_CENTER, new ULCLabel("Filter Type: "));
        final ULCComboBox filterType = new ULCComboBox(ComponentNodeFilterFactory.getFilterModelNames());
        modelFilterTool.add(ULCBoxPane.BOX_LEFT_CENTER, filterType);
        modelFilterTool.add(ULCBoxPane.BOX_LEFT_CENTER, new ULCLabel("Value: "));
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
        modelFilterTool.add(ULCBoxPane.BOX_LEFT_CENTER, filterValue);

        ULCButton clear = new ULCButton(UIUtils.getIcon("delete-active.png"));
        clear.setPreferredSize(new Dimension(16, 16));
        clear.setContentAreaFilled(false);
        clear.setOpaque(false);
        clear.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent event) {
                filterValue.setText("");
                filterType.setSelectedItem(ComponentNodeFilterFactory.NONE);
                IComponentNodeFilter filter = ComponentNodeFilterFactory.getFilter(ComponentNodeFilterFactory.NONE, null);
                fFormEditorView.applyFilter(filter);
                fVisualEditorView.applyFilter(filter);
                fFormEditorView.applyFilter(filter);
            }
        });
        modelFilterTool.add(ULCBoxPane.BOX_LEFT_CENTER, clear);

        IActionListener action = new IActionListener() {
            public void actionPerformed(ActionEvent event) {
                String expr = filterValue.getText();
                String filterModelName = (String) filterType.getSelectedItem();
                IComponentNodeFilter filter = ComponentNodeFilterFactory.getFilter(filterModelName, expr);
                if (filter != null) {
                    fFormEditorView.applyFilter(filter);
                    fVisualEditorView.applyFilter(filter);
                    fTextEditorView.applyFilter(filter);
                }
            }
        };
        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
        filterValue.registerKeyboardAction(action, enter, ULCComponent.WHEN_FOCUSED);

        // button to select the view
        ULCBoxPane viewSelector = new ULCBoxPane(false);
        ULCRadioButton formSelectButton = new ULCRadioButton("Forms");
        formSelectButton.setToolTipText("Edit (model | component) by filling in forms with nodes and connections.");
        ULCRadioButton textSelectButton = new ULCRadioButton("Code");
        textSelectButton.setToolTipText("Inspect (model | component) by looking at its groovy code.");
        ULCRadioButton visualSelectButton = new ULCRadioButton("Visual", true);
        visualSelectButton.setToolTipText("Edit (model | component) in the graphical editor.");
        ULCButtonGroup buttonGroup = new ULCButtonGroup();
        formSelectButton.setGroup(buttonGroup);
        textSelectButton.setGroup(buttonGroup);
        visualSelectButton.setGroup(buttonGroup);
        viewSelector.add(visualSelectButton);
        viewSelector.add(formSelectButton);
        viewSelector.add(textSelectButton);
        viewSelector.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));

        // pack it in a toolbar pane:
        ULCBoxPane toolBarPane = new ULCBoxPane(false);
        // model filtering
        toolBarPane.add(ULCBoxPane.BOX_LEFT_CENTER, modelFilterTool);

        // view selector
        toolBarPane.add(ULCBoxPane.BOX_EXPAND_CENTER, ULCFiller.createHorizontalGlue());
        toolBarPane.add(ULCBoxPane.BOX_RIGHT_CENTER, viewSelector);
        toolBarPane.setBorder(BorderFactory.createEtchedBorder());

        //////////////////////////////////////////////////
        // content pane - initialize the different views
        //////////////////////////////////////////////////
        final ULCCardPane cardPane = new ULCCardPane();
        fFormEditorView = new SingleModelFormView(fApplicationContext);
        final ULCComponent formView = fFormEditorView.getView();
        cardPane.addCard("Form", formView);
        fTextEditorView = new SingleModelTextView(fApplicationContext);
        final ULCComponent textView = fTextEditorView.getView();
        cardPane.addCard("Text", textView);
        fVisualEditorView = new SingleModelVisualView(fApplicationContext, isModel);
        final ULCComponent visualView = fVisualEditorView.getView();
        cardPane.addCard("Visual", visualView);

        formSelectButton.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent event) {
                //fFormEditorView.injectGraphModel(fGraphModel);
                cardPane.setSelectedComponent(formView);
                fFormEditorView.setVisible(true);
                fVisualEditorView.setVisible(false);
                fTextEditorView.setVisible(false);

            }
        });
        textSelectButton.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent event) {
                fTextEditorView.injectGraphModel(fGraphModel); // here the situation is different -> on each switch to the text view the code is generated
                fTextEditorView.setVisible(true);
                fVisualEditorView.setVisible(false);
                fFormEditorView.setVisible(false);
                cardPane.setSelectedComponent(textView);
            }
        });
        visualSelectButton.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent event) {
//                fVisualEditorView.injectGraphModel(fGraphModel);
                cardPane.setSelectedComponent(visualView);
                fVisualEditorView.setVisible(true);
                fFormEditorView.setVisible(false);
                fTextEditorView.setVisible(false);
            }
        });

        cardPane.setSelectedComponent(visualView);
        fVisualEditorView.setVisible(true);

        ULCSplitPane splitPane = new ULCSplitPane(ULCSplitPane.VERTICAL_SPLIT);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(0.65);
        splitPane.setDividerSize(10);
        // the model editing area
        ULCBoxPane modelPane = new ULCBoxPane(true, 2);
        modelPane.add(ULCBoxPane.BOX_EXPAND_TOP, toolBarPane);
        modelPane.add(ULCBoxPane.BOX_EXPAND_EXPAND, cardPane);
        splitPane.setTopComponent(modelPane);

        // lower pane - consisting of a property pane and a model filter pane
        ULCBoxPane lower = new ULCBoxPane(true, 1);

        // the property editing area --> comments, help, ...etc.
        ULCSplitPane propertiesAreaSplitPane = new ULCSplitPane();
        propertiesAreaSplitPane.setDividerSize(10);
        propertiesAreaSplitPane.setOneTouchExpandable(true);
        propertiesAreaSplitPane.setDividerLocation(0.5);

        fLeftTabbedPane = new ULCDetachableTabbedPane();
        fRightTabbedPane = new ULCDetachableTabbedPane();

        // help
        fHelpView = new HelpView();

        // comments
        fCommentView = new CommentView();

        // parameters --> will be added on demand

        // results --> will be added on demand

        fLeftTabbedPane.addTab("Help", new ULCScrollPane(fHelpView.getContent()));
        fRightTabbedPane.addTab("Comments", new ULCScrollPane(fCommentView.getContent()));
        fLeftTabbedPane.setSelectedIndex(0);
        fRightTabbedPane.setSelectedIndex(0);
        // satellite view - this is a bit ugly that I need to get and inject the component wrapping the ulc graph here!
        //ULCGraphOutline satelliteView = new ULCGraphOutline(fVisualEditorView.getULCGraphComponent());
        //fRightTabbedPane.addTab("Info", satelliteView);

        propertiesAreaSplitPane.setLeftComponent(fLeftTabbedPane);
        propertiesAreaSplitPane.setRightComponent(fRightTabbedPane);
        ULCBoxPane propertyPane = new ULCBoxPane(1, 1);
        propertyPane.add(ULCBoxPane.BOX_EXPAND_EXPAND, propertiesAreaSplitPane);
        lower.add(ULCBoxPane.BOX_EXPAND_EXPAND, propertyPane);

        splitPane.setBottomComponent(lower);
        fMainView.add(ULCBoxPane.BOX_EXPAND_EXPAND, splitPane);
    }

    public void injectGraphModel(AbstractGraphModel model) {
        fGraphModel = model;
        fIsModel = model instanceof ModelGraphModel;
        fVisualEditorView.injectGraphModel(model);
        fFormEditorView.injectGraphModel(model);
        fTextEditorView.injectGraphModel(model);

        fFormEditorView.addSelectionListener(fVisualEditorView);
        fVisualEditorView.addSelectionListener(fFormEditorView);

        fFormEditorView.addSelectionListener(fHelpView);
        fVisualEditorView.addSelectionListener(fHelpView);
        //fTextEditorView.addSelectionListener(fHelpView);

        fFormEditorView.addSelectionListener(fCommentView);
        fVisualEditorView.addSelectionListener(fCommentView);
    }

    public ULCBoxPane getView() {
        return fMainView;
    }

    public AbstractGraphModel getGraphModel() {
        return fGraphModel;
    }

    public void addParameterSet(Parameterization p) {
        DataNameDialog dialog = new DataNameDialog(UlcUtilities.getWindowAncestor(fMainView), p);
        dialog.setModal(true);
        dialog.setVisible(true);
    }

    public void addParameterSet(Parameterization p, String name) {
        if (fGraphModel instanceof ComposedComponentGraphModel) {
            return;
        }
        if (!fLeftTabbedPane.anyTabContains("Parameters")) {
            ULCBoxPane data = new ULCBoxPane();
            fDataSetSheets = new ULCCloseableTabbedPane();
            fDataSetSheets.addTabListener(new ITabListener() {
                public void tabClosing(TabEvent event) {
                    event.getClosableTabbedPane().closeCloseableTab(event.getTabClosingIndex());
                    if (fDataSetSheets.getTabCount() > 0) {
                        event.getClosableTabbedPane().setSelectedIndex(0);
                    }
                }
            });
            data.add(ULCBoxPane.BOX_EXPAND_EXPAND, fDataSetSheets);
            fLeftTabbedPane.addTab("Parameters", data);
        }

        DataTable dataTable;
        if (p == null) {
            dataTable = new DataTable((ModelGraphModel) fGraphModel, 1, name);
        } else {
            p.setName(name);
            dataTable = new DataTable((ModelGraphModel) fGraphModel, p);
        }
        fDataSetSheets.addTab(name, dataTable);
        fDataSetSheets.setSelectedIndex(fDataSetSheets.getTabCount()-1);
        fLeftTabbedPane.setSelectedIndex(fLeftTabbedPane.indexOfTab("Parameters"));
    }

    public void addSimulationResult(Map output, String name, boolean inNewTab, List<String> periodLabels) {
        if (!fRightTabbedPane.anyTabContains("Results")) {
            ULCBoxPane results = new ULCBoxPane();
            fResultSheets = new ULCCloseableTabbedPane();
            fResultSheets.addTabListener(new ITabListener() {
                public void tabClosing(TabEvent event) {
                    event.getClosableTabbedPane().closeCloseableTab(event.getTabClosingIndex());
                    if (fResultSheets.getTabCount() > 0) {
                        event.getClosableTabbedPane().setSelectedIndex(0);
                    }
                }
            });
            results.add(ULCBoxPane.BOX_EXPAND_EXPAND, fResultSheets);
            results.registerKeyboardAction(f9_pressed, KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0, false), ULCComponent.WHEN_IN_FOCUSED_WINDOW);
            fRightTabbedPane.addTab("Results", results);
        }
        SimulationResultTable resultTable = new SimulationResultTable(output, periodLabels);
        ULCScrollPane resultScrollPane = new ULCScrollPane(resultTable);
        ULCBoxPane resultTablePane = new ULCBoxPane(true);
        resultTablePane.add(ULCBoxPane.BOX_EXPAND_EXPAND, resultScrollPane);
        resultTablePane.setBorder(BorderFactory.createEmptyBorder());
        int index = fResultSheets.indexOfTab(name);
        if (index<0 || inNewTab) {
            fResultSheets.addTab(name, resultTablePane);
        } else {
            fResultSheets.setComponentAt(index, resultTablePane);
            fResultSheets.setSelectedIndex(index);
        }
        fRightTabbedPane.setSelectedIndex(fRightTabbedPane.indexOfTab("Results"));
    }

    public void addWatch(String path) {
        if (fWatchesTable == null) {
            fWatchesTable = new WatchesTable();
            ULCScrollPane watchesPane = new ULCScrollPane(fWatchesTable);
            fRightTabbedPane.addTab("Watches", watchesPane);
            fVisualEditorView.setWatchList(fWatchesTable.getModel());
        }
        fWatchesTable.getModel().addWatch(path);
        fRightTabbedPane.setSelectedIndex(fRightTabbedPane.indexOfTab("Watches"));
    }

    public Parameterization getSelectedParametrization() {
        ULCComponent comp = fDataSetSheets != null ? fDataSetSheets.getSelectedComponent() : null;
        if (comp != null) {
            return ((DataTable) fDataSetSheets.getSelectedComponent()).getModel().getParametrization();
        }
        return null;
    }

    private void addParametersContextMenu() {
        ULCPopupMenu menu = new ULCPopupMenu();
        ApplicationActionMap actionMap = fApplicationContext.getActionMap(this);

        ULCMenuItem addItem = new ULCMenuItem("simulate");
        addItem.addActionListener(actionMap.get("simulateAction"));
        menu.add(addItem);

        fDataSetSheets.setComponentPopupMenu(menu);
    }

    @Action
    public void simulateAction(boolean newTab) {
        if (fGraphModel instanceof ModelGraphModel) {
            ModelGraphModel model = (ModelGraphModel) fGraphModel;
            model.resolveStartComponents();
            Parameterization parametrization = this.getSelectedParametrization();
            if (parametrization == null) {
                ULCAlert alert = new ULCAlert("No Simulation Done",
                            "Reason: Input parameters missing.", "ok");
                    alert.show();
            } else {
                ProbeSimulationService simulationService = new ProbeSimulationService();
                try {
                    SimulationRunner runner = simulationService.getSimulationRunner(model, parametrization);
                    runner.start();
                    Map output = simulationService.getOutput();
                    List<String> periodLabels = parametrization.getPeriodLabels();
                    if (periodLabels==null) {
                        periodLabels = new ArrayList<String>();
                        for (int i = 0; i < parametrization.getPeriodCount(); i++) {
                            periodLabels.add(Integer.toString(i));
                        }
                        parametrization.setPeriodLabels(periodLabels);
                    }
                    this.addSimulationResult(output, "results_"+parametrization.getName(), newTab, parametrization.getPeriodLabels());
                } catch (Exception ex) {
                    ULCAlert alert = new ULCAlert("Simulation failed",
                            "Reason: " + ex.getMessage(), "ok");
                    alert.show();
                }
            }
        }
    }

    private class DataNameDialog extends ULCDialog {
        private BeanFormDialog<DataNameFormModel> fBeanForm;
        private ULCButton fCancel;
        private Parameterization fParameterization;

        public DataNameDialog(ULCWindow parent, Parameterization parameterization) {
            super(parent);
            fParameterization = parameterization;
            boolean metalLookAndFeel = "Metal".equals(ClientContext.getLookAndFeelName());
            if (!metalLookAndFeel && ClientContext.getLookAndFeelSupportsWindowDecorations()) {
                setUndecorated(true);
                setWindowDecorationStyle(ULCDialog.PLAIN_DIALOG);
            }

            createBeanView();
            setTitle("Dataset Name");
            setLocationRelativeTo(parent);
        }

        @SuppressWarnings("serial")
        private void createBeanView() {
            DataNameFormModel formModel = new DataNameFormModel(new NameBean());
            DataNameForm form = new DataNameForm(formModel);
            fBeanForm = new BeanFormDialog<DataNameFormModel>(form);
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
                    NameBean bean = (NameBean) fBeanForm.getModel().getBean();
                    SingleModelMultiEditView.this.addParameterSet(fParameterization, bean.getName());
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
    }

}
