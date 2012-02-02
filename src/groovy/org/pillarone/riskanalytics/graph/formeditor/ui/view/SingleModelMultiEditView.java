package org.pillarone.riskanalytics.graph.formeditor.ui.view;


import com.canoo.ulc.detachabletabbedpane.server.ITabListener;
import com.canoo.ulc.detachabletabbedpane.server.TabEvent;
import com.canoo.ulc.detachabletabbedpane.server.ULCCloseableTabbedPane;
import com.canoo.ulc.detachabletabbedpane.server.ULCDetachableTabbedPane;
import com.ulcjava.applicationframework.application.AbstractBean;
import com.ulcjava.applicationframework.application.Action;
import com.ulcjava.applicationframework.application.ApplicationActionMap;
import com.ulcjava.applicationframework.application.ApplicationContext;
import com.ulcjava.applicationframework.application.form.BeanFormDialog;
import com.ulcjava.base.application.*;
import com.ulcjava.base.application.event.*;
import com.ulcjava.base.application.tabletree.ITableTreeNode;
import com.ulcjava.base.application.util.Dimension;
import com.ulcjava.base.application.util.KeyStroke;
import org.pillarone.riskanalytics.core.simulation.engine.SimulationRunner;
import org.pillarone.riskanalytics.core.simulation.item.Parameterization;
import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.model.ModelGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.model.filters.ComponentNodeFilterFactory;
import org.pillarone.riskanalytics.graph.core.graph.model.filters.IComponentNodeFilter;
import org.pillarone.riskanalytics.graph.formeditor.ui.*;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.DataNameFormModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.beans.NameBean;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.treetable.NodeNameFilter;
import org.pillarone.riskanalytics.graph.formeditor.ui.view.dialogs.DataNameForm;
import org.pillarone.riskanalytics.graph.formeditor.util.GraphModelUtilities;
import org.pillarone.riskanalytics.graph.formeditor.util.ProbeSimulationService;
import org.pillarone.riskanalytics.graph.formeditor.util.UIUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provides a multiple view on a single graph model.
 * It contains
 * <ul>
 *     <it>the actual view on the model (card pane with the visual, the form and the textual views)</it>
 *     <it>help</it>
 *     <it>comments</it>
 *     <it>data (with parameters to be viewed or edited)</it>
 *     <it>results from a probe simulation run either in the default result table or in the watch list.</it>
 * </ul>
 */
public class SingleModelMultiEditView extends AbstractBean implements IWatchList, ISaveListener, IModelRenameListener {
    private ApplicationContext fApplicationContext;

    private AbstractGraphModel fGraphModel;
    private boolean fIsModel;

    private ULCBoxPane fMainView;
    private SingleModelVisualView fVisualEditorView;
    private SingleModelFormView fFormEditorView;
    private SingleModelTextView fTextEditorView;

    private ULCDetachableTabbedPane fLeftTabbedPane;
    private HelpView fHelpView;
    private ULCCloseableTabbedPane fDataSetSheets;

    private ULCDetachableTabbedPane fRightTabbedPane;
    private CommentView fCommentView;
    private ULCCloseableTabbedPane fResultSheets;
    private WatchesTable fWatchesTable;

    private IActionListener f9_pressed;
    private List<ISaveListener> saveListeners = new ArrayList<ISaveListener>();
    private List<ISelectionListener> selectionListeners = new ArrayList<ISelectionListener>();
    private boolean readOnly = false;

    private IGraphModelHandler graphModelHandler;

    public SingleModelMultiEditView(ApplicationContext ctx, AbstractGraphModel model, IGraphModelHandler graphModelHandler) {
        super();
        fApplicationContext = ctx;
        boolean isModel = model instanceof ModelGraphModel;
        readOnly = GraphModelUtilities.isIncludedInRegistry(model);

        createView(isModel);
        createContextMenu();
        injectGraphModel(model);

        fVisualEditorView.setGraphModelHandler(graphModelHandler);
        fVisualEditorView.setWatchList(this);
        fFormEditorView.setWatchList(this);

        f9_pressed = new IActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                simulateAction(false);
            }
        };
        fMainView.registerKeyboardAction(f9_pressed, KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0, false), ULCComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    public ULCBoxPane getView() {
        return fMainView;
    }

    public HelpView getHelpView() {
        return fHelpView;
    }

    public void setHelpView(HelpView fHelpView) {
        this.fHelpView = fHelpView;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly() {
        this.readOnly = true;
        fFormEditorView.setReadOnly();
        fVisualEditorView.setReadOnly();
    }

    public void setGraphModelHandler(IGraphModelHandler graphModelHandler) {
        this.graphModelHandler = graphModelHandler;
        fFormEditorView.setGraphModelHandler(graphModelHandler);
        fVisualEditorView.setGraphModelHandler(graphModelHandler);
    }

    /**
     * Create the view.
     * @param isModel
     */
    private void createView(boolean isModel) {
        fMainView = new ULCBoxPane(true, 1);

        // split pane with the model views in the upper part and the "properties" in the lower
        ULCSplitPane splitPane = new ULCSplitPane(ULCSplitPane.VERTICAL_SPLIT);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(0.65);
        splitPane.setDividerSize(10);
        ULCBoxPane modelPane = new ULCBoxPane(true, 2);

        // toolbar pane
        // model filter tool
        ULCBoxPane modelFilterTool = new ULCBoxPane(false);
        ULCLabel filterTypeLabel = new ULCLabel("Filter Type: ");
        final ULCComboBox filterType = new ULCComboBox(ComponentNodeFilterFactory.getFilterModelNames());
        ULCLabel filterValueLabel = new ULCLabel("Value: ");
        final ULCTextField filterValue = new ULCTextField(10);
        filterValue.setEditable(false);
        ULCButton clear = new ULCButton(UIUtils.getIcon("delete-active.png"));
        clear.setPreferredSize(new Dimension(16, 16));
        clear.setContentAreaFilled(false);
        clear.setOpaque(false);
        final ULCButton refreshLayout = new ULCButton("Layout");

        // view selector
        ULCBoxPane viewSelector = new ULCBoxPane(false);
        viewSelector.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        ULCButtonGroup buttonGroup = new ULCButtonGroup();
        ULCRadioButton formSelectButton = new ULCRadioButton("Forms");
        formSelectButton.setGroup(buttonGroup);
        formSelectButton.setToolTipText("Edit (model | component) by filling in forms with nodes and connections.");
        ULCRadioButton textSelectButton = new ULCRadioButton("Code");
        textSelectButton.setGroup(buttonGroup);
        textSelectButton.setToolTipText("Inspect (model | component) by looking at its groovy code.");
        ULCRadioButton visualSelectButton = new ULCRadioButton("Visual", true);
        visualSelectButton.setGroup(buttonGroup);
        visualSelectButton.setToolTipText("Edit (model | component) in the graphical editor.");
        ULCBoxPane toolBarPane = new ULCBoxPane(false);
        toolBarPane.setBorder(BorderFactory.createEtchedBorder());

        // content pane with teh views on the model - initialize the different views
        final ULCCardPane cardPane = new ULCCardPane();
        fFormEditorView = new SingleModelFormView(fApplicationContext, readOnly);
        final ULCComponent formView = fFormEditorView.getView();
        fTextEditorView = new SingleModelTextView(fApplicationContext);
        final ULCComponent textView = fTextEditorView.getView();
        fVisualEditorView = new SingleModelVisualView(fApplicationContext, isModel, readOnly);
        final ULCComponent visualView = fVisualEditorView.getView();

        // lower pane - consisting of a property pane and a model filter pane
        ULCBoxPane lower = new ULCBoxPane(true, 1);
        ULCBoxPane propertyPane = new ULCBoxPane(1, 1);

        // the property editing area --> comments, help, ...etc.
        ULCSplitPane propertiesAreaSplitPane = new ULCSplitPane();
        propertiesAreaSplitPane.setDividerSize(10);
        propertiesAreaSplitPane.setOneTouchExpandable(true);
        propertiesAreaSplitPane.setDividerLocation(0.5);

        fLeftTabbedPane = new ULCDetachableTabbedPane();
        fRightTabbedPane = new ULCDetachableTabbedPane();
        fHelpView = new HelpView();
        fCommentView = new CommentView(readOnly);
        // parameters --> will be added on demand
        // results --> will be added on demand
        // watches --> will be added on demand


        // layout
        modelFilterTool.add(ULCBoxPane.BOX_LEFT_CENTER, filterTypeLabel);
        modelFilterTool.add(ULCBoxPane.BOX_LEFT_CENTER, filterType);
        modelFilterTool.add(ULCBoxPane.BOX_LEFT_CENTER, filterValueLabel);
        modelFilterTool.add(ULCBoxPane.BOX_LEFT_CENTER, filterValue);
        modelFilterTool.add(ULCBoxPane.BOX_LEFT_CENTER, clear);
        viewSelector.add(visualSelectButton);
        viewSelector.add(formSelectButton);
        viewSelector.add(textSelectButton);

        toolBarPane.add(ULCBoxPane.BOX_LEFT_CENTER, modelFilterTool);
        toolBarPane.add(ULCBoxPane.BOX_EXPAND_CENTER, ULCFiller.createHorizontalGlue());
        toolBarPane.add(ULCBoxPane.BOX_RIGHT_CENTER, refreshLayout);
        toolBarPane.add(ULCBoxPane.BOX_RIGHT_CENTER, viewSelector);

        cardPane.addCard("Form", formView);
        cardPane.addCard("Text", textView);
        cardPane.addCard("Visual", visualView);
        cardPane.setSelectedComponent(visualView);
        fVisualEditorView.setVisible(true);


        modelPane.add(ULCBoxPane.BOX_EXPAND_TOP, toolBarPane);
        modelPane.add(ULCBoxPane.BOX_EXPAND_EXPAND, cardPane);

        splitPane.setTopComponent(modelPane);

        fLeftTabbedPane.addTab("Help", new ULCScrollPane(getHelpView().getMainComponent()));
        fRightTabbedPane.addTab("Comments", new ULCScrollPane(fCommentView.getContent()));
        fLeftTabbedPane.setSelectedIndex(0);
        fRightTabbedPane.setSelectedIndex(0);

        propertiesAreaSplitPane.setLeftComponent(fLeftTabbedPane);
        propertiesAreaSplitPane.setRightComponent(fRightTabbedPane);
        propertyPane.add(ULCBoxPane.BOX_EXPAND_EXPAND, propertiesAreaSplitPane);
        lower.add(ULCBoxPane.BOX_EXPAND_EXPAND, propertyPane);

        splitPane.setBottomComponent(lower);
        fMainView.add(ULCBoxPane.BOX_EXPAND_EXPAND, splitPane);


        // attach listeners
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
        clear.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent event) {
                filterValue.setText("");
                filterType.setSelectedItem(ComponentNodeFilterFactory.NONE);
                IComponentNodeFilter filter = ComponentNodeFilterFactory.getFilter(ComponentNodeFilterFactory.NONE, null);
                fFormEditorView.applyFilter(filter);
                fVisualEditorView.applyFilter(filter);
                fFormEditorView.applyFilter(filter);
                for (ISelectionListener selectionListener : selectionListeners)
                    selectionListener.applyFilter(new NodeNameFilter(null));
            }
        });
        IActionListener action = new IActionListener() {
            public void actionPerformed(ActionEvent event) {
                String expr = filterValue.getText();
                String filterModelName = (String) filterType.getSelectedItem();
                IComponentNodeFilter filter = ComponentNodeFilterFactory.getFilter(filterModelName, expr);
                if (filter != null) {
                    fFormEditorView.applyFilter(filter);
                    fVisualEditorView.applyFilter(filter);
                    fTextEditorView.applyFilter(filter);
                    for (ISelectionListener selectionListener : selectionListeners)
                        selectionListener.applyFilter(new NodeNameFilter(expr));
                }
            }
        };
        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
        filterValue.registerKeyboardAction(action, enter, ULCComponent.WHEN_FOCUSED);

        refreshLayout.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent event) {
                fVisualEditorView.refreshLayout();
            }
        });

        formSelectButton.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent event) {
                //fFormEditorView.injectGraphModel(fGraphModel);
                cardPane.setSelectedComponent(formView);
                fFormEditorView.setVisible(true);
                fVisualEditorView.setVisible(false);
                refreshLayout.setEnabled(false);
                fTextEditorView.setVisible(false);

            }
        });
        textSelectButton.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent event) {
                fTextEditorView.injectGraphModel(fGraphModel); // here the situation is different -> on each switch to the text view the code is generated
                fTextEditorView.setVisible(true);
                fVisualEditorView.setVisible(false);
                refreshLayout.setEnabled(false);
                fFormEditorView.setVisible(false);
                cardPane.setSelectedComponent(textView);
            }
        });
        visualSelectButton.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent event) {
//                fVisualEditorView.injectGraphModel(fGraphModel);
                cardPane.setSelectedComponent(visualView);
                fVisualEditorView.setVisible(true);
                refreshLayout.setEnabled(true);
                fFormEditorView.setVisible(false);
                fTextEditorView.setVisible(false);
            }
        });
    }

    private void createContextMenu() {
        ULCPopupMenu menu = new ULCPopupMenu();
        ULCMenuItem renameModelItem = new ULCMenuItem("rename model");
        menu.add(renameModelItem);
        renameModelItem.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                graphModelHandler.renameModel(fGraphModel);
            }
        });
        fMainView.setComponentPopupMenu(menu);
    }

    private void createParametersView() {
        ULCBoxPane data = new ULCBoxPane();
        fDataSetSheets = new ULCCloseableTabbedPane();
        data.add(ULCBoxPane.BOX_EXPAND_EXPAND, fDataSetSheets);
        fLeftTabbedPane.addTab("Parameters", data);

        fDataSetSheets.addTabListener(new ITabListener() {
            public void tabClosing(TabEvent event) {
                int tabClosingIndex = event.getTabClosingIndex();
                ULCComponent component = event.getClosableTabbedPane().getComponentAt(tabClosingIndex);
                if (component instanceof ISelectionListener) {
                    fFormEditorView.removeSelectionListener((ISelectionListener) component);
                    fVisualEditorView.removeSelectionListener((ISelectionListener) component);
                }
                if (component instanceof ISaveListener)
                    saveListeners.remove((ISaveListener) component);
                if (component instanceof SimulationResultTable)
                    selectionListeners.remove(component);

                event.getClosableTabbedPane().closeCloseableTab(tabClosingIndex);
                if (fDataSetSheets.getTabCount() > 0) {
                    event.getClosableTabbedPane().setSelectedIndex(0);
                }
            }
        });
    }

    private void createResultsView() {
        ULCBoxPane results = new ULCBoxPane();
        fResultSheets = new ULCCloseableTabbedPane();
        results.add(ULCBoxPane.BOX_EXPAND_EXPAND, fResultSheets);
        fRightTabbedPane.addTab("Results", results);

        fResultSheets.addTabListener(new ITabListener() {
            public void tabClosing(TabEvent event) {
                int tabClosingIndex = event.getTabClosingIndex();

                ULCComponent component = event.getClosableTabbedPane().getComponentAt(tabClosingIndex);
                if (component instanceof ISelectionListener) {
                    fFormEditorView.removeSelectionListener((ISelectionListener) component);
                    fVisualEditorView.removeSelectionListener((ISelectionListener) component);
                }
                event.getClosableTabbedPane().closeCloseableTab(tabClosingIndex);
                if (fResultSheets.getTabCount() > 0) {
                    event.getClosableTabbedPane().setSelectedIndex(0);
                }
            }
        });
        fResultSheets.registerKeyboardAction(f9_pressed, KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0, false), ULCComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    /**
     * Creates a context menu for the parameterizations.
     */
    private void addParametersContextMenu() {
        ULCPopupMenu menu = new ULCPopupMenu();
        ApplicationActionMap actionMap = fApplicationContext.getActionMap(this);

        ULCMenuItem addItem = new ULCMenuItem("simulate");
        addItem.addActionListener(actionMap.get("simulateAction"));
        menu.add(addItem);

        fDataSetSheets.setComponentPopupMenu(menu);
    }


    /**
     * Activate the multiple view component with the given graph model.
     * @param model
     */
    private void injectGraphModel(AbstractGraphModel model) {
        fGraphModel = model;
        fIsModel = model instanceof ModelGraphModel;
        fVisualEditorView.injectGraphModel(model);
        fFormEditorView.injectGraphModel(model);
        fTextEditorView.injectGraphModel(model);

        fHelpView.injectGraphModel(model);
        fCommentView.setGraphModel(model);

        fFormEditorView.addSelectionListener(fVisualEditorView);
        fVisualEditorView.addSelectionListener(fFormEditorView);

        fFormEditorView.addSelectionListener(fHelpView);
        fVisualEditorView.addSelectionListener(fHelpView);
        //fTextEditorView.addSelectionListener(fHelpView);

        fFormEditorView.addSelectionListener(fCommentView);
        fVisualEditorView.addSelectionListener(fCommentView);

        fVisualEditorView.refreshLayout();
    }

    /**
     * Provide the graph model for the given multi-edit view.
     * @return
     */
    public AbstractGraphModel getGraphModel() {
        return fGraphModel;
    }

    /**
     * Add a given parameterization to the given view so that it is viewable in DataTable section.
     * Opens the DatasetNameDialog to ask for a name and then delegates to addParameterSet(Parameterization, String).
     * @param p
     */
    public void addParameterSet(Parameterization p) {
        DatasetNameDialog dialog = new DatasetNameDialog(UlcUtilities.getWindowAncestor(fMainView), p, "Dataset Name");
        dialog.setModal(true);
        dialog.setVisible(true);
    }

    /**
     * Add the given parameterization under the given name in the parameters section of the multi edit view.
     * @param p
     * @param name
     */
    public void addParameterSet(Parameterization p, String name) {
        if (!fLeftTabbedPane.anyTabContains("Parameters")) {
            createParametersView();
        }

        DataTable dataTable;
        if (p == null) {
            dataTable = new DataTable(fGraphModel, 1, name);
            fFormEditorView.addSelectionListener(dataTable);
            fVisualEditorView.addSelectionListener(dataTable);
            dataTable.addTreeSelectionListener(fVisualEditorView);
            saveListeners.add(dataTable);
        } else {
            p.setName(name);
            dataTable = new DataTable(fGraphModel, p);
        }
        dataTable.getTableTree().getViewPortTableTree().addActionListener(new DataTable.MDPTabStarter(fDataSetSheets));
        fDataSetSheets.addTab(name, dataTable);
        fDataSetSheets.setSelectedIndex(fDataSetSheets.getTabCount() - 1);
        fLeftTabbedPane.setSelectedIndex(fLeftTabbedPane.indexOfTab("Parameters"));
    }

    /**
     * Provides the selected parameterization (parameterization contained in the active tab).
     * @return
     */
    public Parameterization getSelectedParametrization() {
        ULCComponent comp = fDataSetSheets != null ? fDataSetSheets.getSelectedComponent() : null;
        if (comp != null) {
            return ((DataTable) fDataSetSheets.getSelectedComponent()).getParameterization();
        }
        return null;
    }

    /**
     * Adds the given map with the simulation output under the given name to the tabbed pane with the results.
     * @param output
     * @param name
     * @param inNewTab
     * @param periodLabels
     */
    public void addSimulationResult(Map output, String name, boolean inNewTab, List<String> periodLabels) {
        if (!fRightTabbedPane.anyTabContains("Results")) {
            createResultsView();
        }
        SimulationResultTable resultTable = new SimulationResultTable(output, periodLabels);

        ULCScrollPane resultScrollPane = new ULCScrollPane(resultTable);
        ULCBoxPane resultTablePane = new ULCBoxPane(true);
        resultTablePane.setBorder(BorderFactory.createEmptyBorder());

        resultTablePane.add(ULCBoxPane.BOX_EXPAND_EXPAND, resultScrollPane);
        int index = fResultSheets.indexOfTab(name);
        if (index < 0 || inNewTab) {
            fResultSheets.addTab(name, resultTablePane);
        } else {
            fResultSheets.setComponentAt(index, resultTablePane);
            fResultSheets.setSelectedIndex(index);
        }
        selectionListeners.add(resultTable);

        fFormEditorView.addSelectionListener(resultTable);
        fVisualEditorView.addSelectionListener(resultTable);

        boolean hasWatches = fWatchesTable != null && ((ITableTreeNode) fWatchesTable.getModel().getRoot()).getChildCount() > 0;
        if (hasWatches) {
            fWatchesTable.getModel().injectData(output, periodLabels);
            fWatchesTable.getTable().expandAll();
        }

        int resultIndex = fRightTabbedPane.indexOfTab("Results");
        int watchIndex = fRightTabbedPane.indexOfTab("Watches");
        int selectedIndex = fRightTabbedPane.getSelectedIndex();
        if (selectedIndex != resultIndex && selectedIndex != watchIndex) {
            if (hasWatches) {
                fRightTabbedPane.setSelectedIndex(watchIndex);
            } else {
                fRightTabbedPane.setSelectedIndex(resultIndex);
            }
        }
    }

    ////////////////////////////////////////////////
    // Implementation of the IWatchList-interface
    ////////////////////////////////////////////////

    /**
     * @see org.pillarone.riskanalytics.graph.formeditor.ui.IWatchList
     * @param path
     */
    public void addWatch(String path) {
        if (fWatchesTable == null) {
            fWatchesTable = new WatchesTable();
            ULCScrollPane watchesPane = new ULCScrollPane(fWatchesTable);
            fRightTabbedPane.addTab("Watches", watchesPane);
            fWatchesTable.registerKeyboardAction(f9_pressed, KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0, false), ULCComponent.WHEN_IN_FOCUSED_WINDOW);
        }
        fWatchesTable.getModel().addWatch(path);
        fWatchesTable.getTable().expandAll();
        fRightTabbedPane.setSelectedIndex(fRightTabbedPane.indexOfTab("Watches"));
    }

    /**
     * @see org.pillarone.riskanalytics.graph.formeditor.ui.IWatchList
     * @param path
     */
    public void removeWatch(String path) {
        if (fWatchesTable != null && fWatchesTable.getModel() != null)
            fWatchesTable.getModel().removeWatch(path);
    }

    /**
     * @see org.pillarone.riskanalytics.graph.formeditor.ui.IWatchList
     * @param oldPath
     * @param newPath
     */
    public void editWatch(String oldPath, String newPath) {
        if (fWatchesTable != null && fWatchesTable.getModel() != null)
            fWatchesTable.getModel().editWatch(oldPath, newPath);
    }

    /**
     * @see org.pillarone.riskanalytics.graph.formeditor.ui.IWatchList
     */
    public void removeAllWatches() {
        fWatchesTable.getModel().removeAllWatches();
    }

    /**
     * @see ISaveListener
     */
    public void save() {
        for (ISaveListener saveListener : saveListeners) {
            saveListener.save();
        }
    }

    @Action
    public void simulateAction(boolean newTab) {
        Parameterization parametrization = this.getSelectedParametrization();
        if (parametrization == null) {
            ULCAlert alert = new ULCAlert("No Simulation Done",
                    "Reason: Input parameters missing.", "ok");
            alert.show();
        } else {
            ProbeSimulationService simulationService = new ProbeSimulationService();
            try {
                SimulationRunner runner = simulationService.getSimulationRunner(fGraphModel, parametrization);
                runner.start();
                Map output = simulationService.getOutput();
                List<String> periodLabels = parametrization.getPeriodLabels();
                if (periodLabels == null) {
                    periodLabels = new ArrayList<String>();
                    for (int i = 0; i < parametrization.getPeriodCount(); i++) {
                        periodLabels.add(Integer.toString(i));
                    }
                    parametrization.setPeriodLabels(periodLabels);
                }
                this.addSimulationResult(output, "results_" + parametrization.getName(), newTab, periodLabels);
            } catch (Exception ex) {
                ULCAlert alert = new ULCAlert("Simulation failed",
                        "Reason: " + ex.getMessage(), "ok");
                alert.show();
            }
        }
    }

    public void modelRenamed(AbstractGraphModel modelWithNewName, String oldName, String oldPackageName) {
        fFormEditorView.modelRenamed(modelWithNewName, oldName, oldPackageName);
        fVisualEditorView.modelRenamed(modelWithNewName, oldName, oldPackageName);
    }


    private class DatasetNameDialog extends ULCDialog {
        private BeanFormDialog<DataNameFormModel> fBeanForm;
        private ULCButton fCancel;
        private Parameterization fParameterization;

        public DatasetNameDialog(ULCWindow parent, Parameterization parameterization, String title) {
            super(parent);
            fParameterization = parameterization;
            boolean metalLookAndFeel = "Metal".equals(ClientContext.getLookAndFeelName());
            if (!metalLookAndFeel && ClientContext.getLookAndFeelSupportsWindowDecorations()) {
                setUndecorated(true);
                setWindowDecorationStyle(ULCDialog.PLAIN_DIALOG);
            }

            createBeanView();
            setTitle(title);
            setLocationRelativeTo(parent);
        }

        @SuppressWarnings("serial")
        private void createBeanView() {
            DataNameFormModel formModel = new DataNameFormModel(new NameBean());
            DataNameForm form = new DataNameForm(formModel);
            fBeanForm = new BeanFormDialog<DataNameFormModel>(form);
            fCancel = new ULCButton("Cancel");

            add(fBeanForm.getContentPane());
            fBeanForm.addToButtons(fCancel);

            fCancel.addActionListener(new IActionListener() {
                public void actionPerformed(ActionEvent event) {
                    fBeanForm.reset();
                    setVisible(false);
                }
            });
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
