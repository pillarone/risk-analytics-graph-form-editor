package org.pillarone.riskanalytics.graph.formeditor.ui.view;


import com.ulcjava.applicationframework.application.AbstractBean;
import com.ulcjava.applicationframework.application.ApplicationContext;
import com.ulcjava.base.application.*;
import com.ulcjava.base.application.event.ActionEvent;
import com.ulcjava.base.application.event.IActionListener;
import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.model.ModelGraphModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.handlers.TypeTransferHandler;

import java.util.Map;

public class SingleModelMultiEditView extends AbstractBean {
    private ApplicationContext fApplicationContext;

    private AbstractGraphModel fGraphModel;
    private boolean fIsModel;

    private ULCBoxPane fMainView;
    private SingleModelFormView fFormEditorView;
    private SingleModelTextView fTextEditorView;
    private SingleModelVisualView fVisualEditorView;
    private ULCTabbedPane fDataSetSheets;

    public SingleModelMultiEditView(ApplicationContext ctx, AbstractGraphModel model) {
        super();
        fMainView = new ULCBoxPane(true, 1);
        fApplicationContext = ctx;
        fGraphModel = model;
        fIsModel = model instanceof ModelGraphModel;
        createView();
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
        // toolbar
        //ULCToolBar toolBar = createToolBar();
        //toolBar.setBorderPainted(true);

        // button to select the view
        ULCBoxPane viewSelector = new ULCBoxPane(false);
        ULCRadioButton formSelectButton = new ULCRadioButton("Forms", true);
        formSelectButton.setToolTipText("Edit (model | component) by filling in forms with nodes and connections.");
        ULCRadioButton textSelectButton = new ULCRadioButton("Code");
        textSelectButton.setToolTipText("Inspect (model | component) by looking at its groovy code.");
        ULCRadioButton visualSelectButton = new ULCRadioButton("Visual");
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
        //toolBarPane.add(ULCBoxPane.BOX_LEFT_CENTER, toolBar);
        toolBarPane.add(ULCBoxPane.BOX_EXPAND_CENTER, ULCFiller.createHorizontalGlue());
        toolBarPane.add(ULCBoxPane.BOX_RIGHT_CENTER, viewSelector);
        toolBarPane.setBorder(BorderFactory.createEtchedBorder());

        // content pane - initialize the different views
        final ULCCardPane cardPane = new ULCCardPane();
        fFormEditorView = new SingleModelFormView(fApplicationContext, fGraphModel);
        final ULCComponent formView =  fFormEditorView.getView();
        cardPane.addCard("Form", formView);
        fTextEditorView = new SingleModelTextView(fApplicationContext, fGraphModel);
        final ULCComponent textView =  fTextEditorView.getView();
        cardPane.addCard("Text", textView);
        fVisualEditorView = new SingleModelVisualView(fApplicationContext, fGraphModel);
        final ULCComponent visualView =  fVisualEditorView.getView();
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
        ULCSplitPane splitPane = new ULCSplitPane(ULCSplitPane.VERTICAL_SPLIT);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(0.65);
        splitPane.setDividerSize(10);
        // the model editing area
        ULCBoxPane modelPane = new ULCBoxPane(true,2);
        modelPane.add(ULCBoxPane.BOX_EXPAND_TOP, toolBarPane);
        modelPane.add(ULCBoxPane.BOX_EXPAND_EXPAND, cardPane);
        splitPane.setTopComponent(modelPane);

        // lower pane - consisting of a property pane and a model filter pane
        ULCBoxPane lower = new ULCBoxPane(true,1);
        ULCSplitPane splitPane2 = new ULCSplitPane();
        lower.add(ULCBoxPane.BOX_EXPAND_EXPAND, splitPane2);

        // the property editing area --> comments, help, ...etc.
        ULCBoxPane propertyPane = new ULCBoxPane(true,1);
        ULCTabbedPane tabbedPane = new ULCTabbedPane();
        ULCTextArea comments = new ULCTextArea();
        comments.setEditable(true);
        tabbedPane.addTab("Comments", comments);
        tabbedPane.setEnabledAt(0,false);
        ULCBoxPane data = new ULCBoxPane();
        fDataSetSheets = new ULCTabbedPane();
        data.add(ULCBoxPane.BOX_EXPAND_EXPAND, fDataSetSheets);
        tabbedPane.addTab("Parameters", data);
        tabbedPane.setEnabledAt(1,true);
        ULCBoxPane results = new ULCBoxPane();
        tabbedPane.addTab("Results", results);
        tabbedPane.setEnabledAt(2,false);
        tabbedPane.setSelectedIndex(1);
        propertyPane.add(ULCBoxPane.BOX_EXPAND_EXPAND, tabbedPane);
        splitPane2.setLeftComponent(propertyPane);

        // filter pane
        ModelFilterPane filterPane = new ModelFilterPane(fGraphModel);
        splitPane2.setRightComponent(filterPane);
        splitPane2.setDividerSize(10);
        splitPane2.setOneTouchExpandable(true);
        splitPane2.setDividerLocation(0.7);

        splitPane.setBottomComponent(lower);
        fMainView.add(ULCBoxPane.BOX_EXPAND_EXPAND, splitPane);
    }

    public ULCBoxPane getView() {
        return fMainView;
    }

    public AbstractGraphModel getGraphModel() {
        return fGraphModel;
    }
    
    public void addNewDataSet(String name) {
        if (fGraphModel instanceof ModelGraphModel) {
            fDataSetSheets.addTab(name, new DataTable((ModelGraphModel)fGraphModel, 1, name));
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
