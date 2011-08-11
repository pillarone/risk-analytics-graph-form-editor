package org.pillarone.riskanalytics.graph.formeditor.ui.view;


import com.canoo.ulc.detachabletabbedpane.server.ITabListener;
import com.canoo.ulc.detachabletabbedpane.server.TabEvent;
import com.canoo.ulc.detachabletabbedpane.server.ULCCloseableTabbedPane;
import com.canoo.ulc.detachabletabbedpane.server.ULCDetachableTabbedPane;
import com.canoo.ulc.graph.IGraphSelectionListener;
import com.canoo.ulc.graph.ULCGraphOutline;
import com.canoo.ulc.graph.model.Vertex;
import com.ulcjava.applicationframework.application.AbstractBean;
import com.ulcjava.applicationframework.application.Action;
import com.ulcjava.applicationframework.application.ApplicationActionMap;
import com.ulcjava.applicationframework.application.ApplicationContext;
import com.ulcjava.base.application.*;
import com.ulcjava.base.application.event.ActionEvent;
import com.ulcjava.base.application.event.IActionListener;
import com.ulcjava.base.application.event.KeyEvent;
import com.ulcjava.base.application.util.Dimension;
import com.ulcjava.base.application.util.KeyStroke;
import org.pillarone.riskanalytics.core.simulation.item.Parameterization;
import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.model.ModelGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.model.filters.ComponentNodeFilterFactory;
import org.pillarone.riskanalytics.graph.core.graph.model.filters.IComponentNodeFilter;
import org.pillarone.riskanalytics.graph.formeditor.ui.handlers.TypeTransferHandler;
import org.pillarone.riskanalytics.graph.formeditor.util.ProbeSimulationService;
import org.pillarone.riskanalytics.graph.formeditor.util.UIUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SingleModelMultiEditView extends AbstractBean {
    private ApplicationContext fApplicationContext;

    private AbstractGraphModel fGraphModel;
    private boolean fIsModel;

    private ULCBoxPane fMainView;
    private SingleModelVisualView fVisualEditorView;
    private SingleModelFormView fFormEditorView;
    private SingleModelTextView fTextEditorView;
    private ULCCloseableTabbedPane fDataSetSheets;
    private ULCCloseableTabbedPane fResultSheets;

    public SingleModelMultiEditView(ApplicationContext ctx, AbstractGraphModel model) {
        super();
        fApplicationContext = ctx;
        createView();
        injectGraphModel(model);
    }

    public void setTransferHandler(TypeTransferHandler transferHandler) {
        fFormEditorView.setTransferHandler(transferHandler);
    }

    /*private ULCToolBar createToolBar() {
        String[] actionNames = new String[]{"saveAction", "exportToApplication"};
        ULCToolBar toolBar = new ULCToolBar(ULCToolBar.HORIZONTAL);
        toolBar.setFloatable(false);
        ApplicationActionMap actionMap = fApplicationContext.getActionMap(this);
        for (String actionName : actionNames) {
            IAction action = actionMap.get(actionName);
            ULCButton button = new ULCButton();
            button.setAction(action);
            button.setName(actionName + ".ToolBarButton");
            button.setVerticalTextPosition(ULCButton.BOTTOM);
            button.setHorizontalTextPosition(ULCButton.CENTER);
            button.setFocusable(false);
            button.setMargin(new Insets(0, 15, 0, 15));
            button.setBorderPainted(false);
            toolBar.add(button);
        }
        return toolBar;
    }*/

    public void createView() {
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
                fGraphModel.clearNodeFilters();
                fGraphModel.addNodeFilter(filter);
            }
        });
        modelFilterTool.add(ULCBoxPane.BOX_LEFT_CENTER, clear);

        IActionListener action = new IActionListener() {
            public void actionPerformed(ActionEvent event) {
                String expr = filterValue.getText();
                String filterModelName = (String) filterType.getSelectedItem();
                IComponentNodeFilter filter = ComponentNodeFilterFactory.getFilter(filterModelName, expr);
                if (filter != null) {
                    fGraphModel.clearNodeFilters();
                    fGraphModel.addNodeFilter(filter);
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
        fVisualEditorView = new SingleModelVisualView(fApplicationContext);
        final ULCComponent visualView = fVisualEditorView.getView();
        cardPane.addCard("Visual", visualView);

        formSelectButton.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent event) {
                //fFormEditorView.injectGraphModel(fGraphModel);
                fFormEditorView.setVisible(true);
                cardPane.setSelectedComponent(formView);
            }
        });
        textSelectButton.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent event) {
                fTextEditorView.injectGraphModel(fGraphModel);
                fTextEditorView.setVisible(true);
                cardPane.setSelectedComponent(textView);
            }
        });
        visualSelectButton.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent event) {
//                fVisualEditorView.injectGraphModel(fGraphModel);
                fVisualEditorView.setVisible(true);
                cardPane.setSelectedComponent(visualView);
            }
        });

        cardPane.setSelectedComponent(visualView);

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
        ULCSplitPane splitPane2 = new ULCSplitPane();
        lower.add(ULCBoxPane.BOX_EXPAND_EXPAND, splitPane2);

        // the property editing area --> comments, help, ...etc.
        ULCSplitPane propertiesAreaSplitPane = new ULCSplitPane();
        propertiesAreaSplitPane.setDividerSize(10);
        propertiesAreaSplitPane.setOneTouchExpandable(true);
        propertiesAreaSplitPane.setDividerLocation(0.5);

        ULCDetachableTabbedPane leftTabbedPane = new ULCDetachableTabbedPane();
        ULCDetachableTabbedPane rightTabbedPane = new ULCDetachableTabbedPane();

        // help
        final HelpView helpView = new HelpView();
        IGraphSelectionListener selectionListener = new IGraphSelectionListener() {
            public void selectionChanged() {
                Set<Vertex> selectedVertices = fVisualEditorView.getULCGraph().getSelectionModel().getSelectedVertices();
                if (selectedVertices.size() == 1) {
                    String templateId = ((Vertex) selectedVertices.toArray()[0]).getTemplateId();
                    helpView.updateView(templateId);
                }
            }
        };
        fVisualEditorView.getULCGraph().getSelectionModel().addGraphSelectionListener(selectionListener);
        fFormEditorView.addVertexHelpListener(helpView);

        // comments
        ULCTextArea comments = new ULCTextArea();
        comments.setEditable(true);
        comments.setText("No comments available yet");
        // parameters
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
        // results
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
        leftTabbedPane.addTab("Help", new ULCScrollPane(helpView.getContent()));
        leftTabbedPane.addTab("Parameters", data);
        rightTabbedPane.addTab("Results", results);
        rightTabbedPane.addTab("Comments", comments);
        leftTabbedPane.setSelectedIndex(0);
        rightTabbedPane.setSelectedIndex(0);

        propertiesAreaSplitPane.setLeftComponent(leftTabbedPane);
        propertiesAreaSplitPane.setRightComponent(rightTabbedPane);
        ULCBoxPane propertyPane = new ULCBoxPane(1, 1);
        propertyPane.add(ULCBoxPane.BOX_EXPAND_EXPAND, propertiesAreaSplitPane);
        splitPane2.setLeftComponent(propertyPane);

        // satellite view - this is a bit ugly that I need to get and inject the component wrapping the ulc graph here!
        ULCGraphOutline satelliteView = new ULCGraphOutline(fVisualEditorView.getULCGraphComponent());
        splitPane2.setRightComponent(satelliteView);
        splitPane2.setDividerSize(10);
        splitPane2.setOneTouchExpandable(true);
        splitPane2.setDividerLocation(0.7);

        splitPane.setBottomComponent(lower);
        fMainView.add(ULCBoxPane.BOX_EXPAND_EXPAND, splitPane);
    }

    public void injectGraphModel(AbstractGraphModel model) {
        fGraphModel = model;
        fIsModel = model instanceof ModelGraphModel;
        fVisualEditorView.injectGraphModel(model);
        fFormEditorView.injectGraphModel(model);
        fTextEditorView.injectGraphModel(model);
    }

    public ULCBoxPane getView() {
        return fMainView;
    }

    public AbstractGraphModel getGraphModel() {
        return fGraphModel;
    }

    public void addParameterSet(Parameterization p, String name) {
        if (fGraphModel instanceof ModelGraphModel) {
            DataTable dataTable;
            if (p == null) {
                dataTable = new DataTable((ModelGraphModel) fGraphModel, 1, name);
            } else {
                dataTable = new DataTable((ModelGraphModel) fGraphModel, p);
            }
            fDataSetSheets.addTab(name, dataTable);
        }
    }

    public void addSimulationResult(Map output, String name) {
        SimulationResultTable resultTable = new SimulationResultTable(output);
        ULCScrollPane resultScrollPane = new ULCScrollPane(resultTable);
        ULCBoxPane resultTablePane = new ULCBoxPane(true);
        resultTablePane.add(ULCBoxPane.BOX_EXPAND_EXPAND, resultScrollPane);
        resultTablePane.setBorder(BorderFactory.createEmptyBorder());
        fResultSheets.addTab(name, resultTablePane);
    }

    public Parameterization getSelectedParametrization() {
        ULCComponent comp = fDataSetSheets.getSelectedComponent();
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
    public void simulateAction() {
        if (fGraphModel instanceof ModelGraphModel) {
            ModelGraphModel model = (ModelGraphModel) fGraphModel;
            Parameterization parametrization = this.getSelectedParametrization();
            if (parametrization != null) {
                ProbeSimulationService simulationService = new ProbeSimulationService();
                try {
                    simulationService.getSimulationRunner(model, parametrization).start();
                    Map output = simulationService.getOutput();
                    this.addSimulationResult(output, "results");
                } catch (Exception ex) {
                    ULCAlert alert = new ULCAlert("Simulation failed",
                            "Reason: " + ex.getMessage(), "ok");
                    alert.show();
                }
            }
        }
    }


    /*@Action
    public void saveAction() {
        String text = GraphModelUtilities.getGroovyModelCode(fGraphModel);
        saveOutput(fGraphModel.getName() + ".groovy", text, UlcUtilities.getWindowAncestor(fMainView));
    }

    private void saveOutput(String name, final String text, final ULCWindow ancestor) {
        FileChooserConfig config = new FileChooserConfig();
        config.setDialogTitle("Save file as");
        config.setDialogType(FileChooserConfig.SAVE_DIALOG);
        config.setSelectedFile(name);

        IFileChooseHandler chooser = new IFileChooseHandler() {
            public void onSuccess(String[] filePaths, String[] fileNames) {
                String selectedFile = filePaths[0];
                IFileStoreHandler fileStoreHandler =
                        new IFileStoreHandler() {
                            public void prepareFile(java.io.OutputStream stream) throws Exception {
                                try {
                                    stream.write(text.getBytes());
                                } catch (UnsupportedOperationException t) {
                                    new ULCAlert(ancestor, "Export failed", t.getMessage(), "Ok").show();
                                } catch (Throwable t) {
                                    new ULCAlert(ancestor, "Export failed", t.getMessage(), "Ok").show();
                                } finally {
                                    stream.close();
                                }
                            }

                            public void onSuccess(String filePath, String fileName) {
                            }

                            public void onFailure(int reason, String description) {
//	        			new ULCAlert(ancestor, "Export failed", description, "Ok").show();
                            }
                        };
                try {
                    ClientContext.storeFile(fileStoreHandler, selectedFile);
                } catch (Exception ex) {

                }
            }

            public void onFailure(int reason, String description) {
                new ULCAlert(ancestor, "Export failed", description, "Ok").show();
            }
        };
        ClientContext.chooseFile(chooser, config, ancestor);
    }*/

    /**
     * Deploys the model in RA application.
     */
    /*@Action
    public void exportToApplication() {
        if (fIsModel) {
            try {
                GraphModelUtilities.exportToApplication((ModelGraphModel) fGraphModel);
            } catch (Exception ex) {
                ULCAlert alert = new ULCAlert("Model not deployed.", "Model could not be deployed. Reason: " + ex.getMessage(), "ok");
                alert.show();
            }
        } else {
            ULCAlert alert = new ULCAlert("Graph Model cannot be deployed.", "Graph model is a ComposedComponent - these cannot be deployed and run.", "ok");
            alert.show();
        }
    }*/
}
