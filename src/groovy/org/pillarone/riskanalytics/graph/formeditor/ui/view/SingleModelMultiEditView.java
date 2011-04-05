package org.pillarone.riskanalytics.graph.formeditor.ui.view;


import com.ulcjava.applicationframework.application.*;
import com.ulcjava.applicationframework.application.ApplicationContext;
import com.ulcjava.base.application.*;
import com.ulcjava.base.application.border.ULCAbstractBorder;
import com.ulcjava.base.application.event.*;
import com.ulcjava.base.application.util.Dimension;
import com.ulcjava.base.application.util.IFileChooseHandler;
import com.ulcjava.base.application.util.IFileStoreHandler;
import com.ulcjava.base.application.util.Insets;
import com.ulcjava.base.shared.FileChooserConfig;
import org.pillarone.riskanalytics.graph.core.graph.model.*;
import org.pillarone.riskanalytics.graph.formeditor.ui.handlers.TypeTransferHandler;
import org.pillarone.riskanalytics.graph.formeditor.util.GraphModelUtilities;

public class SingleModelMultiEditView extends AbstractBean {
    private ApplicationContext fApplicationContext;

    private AbstractGraphModel fGraphModel;
    private boolean fIsModel;

    private ULCBoxPane fMainView;
    private SingleModelFormView fFormEditorView;
    private SingleModelTextView fTextEditorView;
    private SingleModelVisualView fVisualEditorView;

    public SingleModelMultiEditView(ApplicationContext ctx, AbstractGraphModel model) {
        super();
        fMainView = new ULCBoxPane(true, 2);
        fApplicationContext = ctx;
        fGraphModel = model;
        fIsModel = model instanceof ModelGraphModel;
        createView();
    }

    public void setTransferHandler(TypeTransferHandler transferHandler) {
        fFormEditorView.setTransferHandler(transferHandler);
    }

    private ULCToolBar createToolBar() {
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
    }

    public void createView() {
        // toolbar
        ULCToolBar toolBar = createToolBar();
        toolBar.setBorderPainted(true);

        // button to select the view
        ULCBoxPane viewSelector = new ULCBoxPane(false);
        ULCRadioButton formSelectButton = new ULCRadioButton("Forms", true);
        ULCRadioButton textSelectButton = new ULCRadioButton("Groovy");
        ULCRadioButton visualSelectButton = new ULCRadioButton("Visual");
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
        toolBarPane.add(ULCBoxPane.BOX_LEFT_CENTER, toolBar);
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
                cardPane.setSelectedComponent(formView);
            }
        });
        textSelectButton.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent event) {
                fTextEditorView.injectGraphModel(fGraphModel);
                cardPane.setSelectedComponent(textView);
            }
        });
        visualSelectButton.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent event) {
                fVisualEditorView.injectGraphModel(fGraphModel);
                cardPane.setSelectedComponent(visualView);
            }
        });
        fMainView.add(ULCBoxPane.BOX_EXPAND_TOP, toolBarPane);
        fMainView.add(ULCBoxPane.BOX_EXPAND_EXPAND, cardPane);
    }

    public ULCBoxPane getView() {
        return fMainView;
    }

    @Action
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
    }

    /**
     * Deploys the model in RA application.
     */
    @Action
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
    }
}
