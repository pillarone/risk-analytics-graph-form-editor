package org.pillarone.riskanalytics.graph.formeditor.ui.view;


import com.canoo.ulc.detachabletabbedpane.server.ITabListener;
import com.canoo.ulc.detachabletabbedpane.server.TabEvent;
import com.canoo.ulc.detachabletabbedpane.server.ULCCloseableTabbedPane;
import com.canoo.ulc.graph.ULCGraphPalette;
import com.canoo.ulc.graph.shared.ShapeTemplate;
import com.ulcjava.applicationframework.application.*;
import com.ulcjava.applicationframework.application.ApplicationContext;
import com.ulcjava.base.application.*;
import com.ulcjava.base.application.event.ActionEvent;
import com.ulcjava.base.application.event.IActionListener;
import com.ulcjava.base.application.util.IFileChooseHandler;
import com.ulcjava.base.application.util.IFileLoadHandler;
import com.ulcjava.base.application.util.IFileStoreHandler;
import com.ulcjava.base.shared.FileChooserConfig;
import org.pillarone.riskanalytics.core.components.ComposedComponent;
import org.pillarone.riskanalytics.core.model.Model;
import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.model.ComposedComponentGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.model.ModelGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.persistence.GraphPersistenceService;
import org.pillarone.riskanalytics.graph.core.graphimport.AbstractGraphImport;
import org.pillarone.riskanalytics.graph.core.graphimport.ComposedComponentGraphImport;
import org.pillarone.riskanalytics.graph.core.graphimport.GraphImportService;
import org.pillarone.riskanalytics.graph.core.graphimport.ModelGraphImport;
import org.pillarone.riskanalytics.graph.core.palette.model.ComponentDefinition;
import org.pillarone.riskanalytics.graph.core.palette.service.PaletteService;
import org.pillarone.riskanalytics.graph.formeditor.ui.handlers.TypeTransferHandler;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.TypeDefinitionFormModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.beans.TypeDefinitionBean;
import org.pillarone.riskanalytics.graph.formeditor.util.GraphModelUtilities;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The main window with the form editor view.
 * It inherits from {@link AbstractBean} to benefit from its property change support.
 * <p/>
 * The window is a tabbed pane which contains for each model or component to be edited a tab.
 *
 * @author martin.melchior
 */
public class GraphModelEditor extends AbstractBean {
    /* Context is needed to load resources (such as icons, etc).*/
    private ApplicationContext fContext;
    /* The editor view.*/
    private ULCCloseableTabbedPane fEditorArea;
    /* Set of currently opened type defs - check that type defs does not already exist. */
    private Set<TypeDefinitionBean> fTypeDefinitions;
    /* Is used for remembering what types have already been declared */
    private Map<ULCComponent, AbstractGraphModel> fModelTabs;
    /* A dialog for new models or composed components to be created.*/
    private TypeDefinitionDialog fTypeDefView;
    /* A dialog for models or composed components to be imported.*/
    private TypeImportDialog fTypeImportView;
    /* Palette views.*/
    private ULCBoxPane fPaletteArea;

    /**
     * @param context Application context is used for accessing and using resources (such as icons, etc.).
     */
    public GraphModelEditor(ApplicationContext context) {
        fContext = context;
        fTypeDefinitions = new HashSet<TypeDefinitionBean>();
        fModelTabs = new HashMap<ULCComponent, AbstractGraphModel>();
    }

    /**
     * Returns the split pane - consisting of the palette view and the model edit view in tabbed pane -
     * as content view.
     *
     * @return model edit and palette view
     */
    public ULCComponent getContentView() {
        ULCBoxPane modelEdit = new ULCBoxPane(true);
        modelEdit.add(ULCBoxPane.BOX_EXPAND_BOTTOM, ULCFiller.createVerticalStrut(3));
        ULCSeparator separator = new ULCSeparator();
        separator.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        modelEdit.add(ULCBoxPane.BOX_EXPAND_BOTTOM, separator);

        fEditorArea = new ULCCloseableTabbedPane();
        fEditorArea.addTabListener(new ITabListener() {
            public void tabClosing(TabEvent event) {
                event.getClosableTabbedPane().closeCloseableTab(event.getTabClosingIndex());
                if (fEditorArea.getTabCount() > 0) {
                    event.getClosableTabbedPane().setSelectedIndex(0);
                }
            }
        });
        modelEdit.add(ULCBoxPane.BOX_EXPAND_EXPAND, fEditorArea);


        ULCBoxPane viewSelector = new ULCBoxPane(false);
        ULCRadioButton typeTreeSelectButton = new ULCRadioButton("Type Tree", true);
        ULCRadioButton categoryTreeSelectButton = new ULCRadioButton("Categories");
        ULCRadioButton paletteSelectButton = new ULCRadioButton("Palette");
        ULCButtonGroup buttonGroup = new ULCButtonGroup();
        typeTreeSelectButton.setGroup(buttonGroup);
        categoryTreeSelectButton.setGroup(buttonGroup);
        paletteSelectButton.setGroup(buttonGroup);
        viewSelector.add(typeTreeSelectButton);
        viewSelector.add(categoryTreeSelectButton);
        viewSelector.add(paletteSelectButton);
        //viewSelector.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));

        final ULCCardPane views = new ULCCardPane();
        final ComponentTypeTree typeTree = new ComponentTypeTree(this);
        views.addCard("TypeTree", typeTree);
        final ComponentCategoryTree categoryTree = new ComponentCategoryTree(this);
        views.addCard("CategoryTree", categoryTree);
        final ULCGraphPalette palette = new ULCGraphPalette();

        for (ComponentDefinition definition : PaletteService.getInstance().getAllComponentDefinitions()) {
            palette.addShapeTemplate(new ShapeTemplate(ShapeTemplate.ShapeType.Container, definition.getTypeClass().getName(), definition.getTypeClass().getSimpleName()));
        }
        views.addCard("PaletteView", palette);
        typeTreeSelectButton.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent event) {
                views.setSelectedComponent(typeTree);
            }
        });
        categoryTreeSelectButton.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent event) {
                views.setSelectedComponent(categoryTree);
            }
        });
        paletteSelectButton.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent event) {
                views.setSelectedComponent(palette);
            }
        });
        fPaletteArea = new ULCBoxPane(true);
        fPaletteArea.add(ULCBoxPane.BOX_EXPAND_TOP, viewSelector);
        fPaletteArea.add(ULCBoxPane.BOX_EXPAND_EXPAND, views);


        ULCSplitPane splitPane = new ULCSplitPane(ULCSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(fPaletteArea);
        splitPane.setRightComponent(modelEdit);
        splitPane.setDividerLocationAnimationEnabled(true);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(0.3);
        splitPane.setDividerSize(10);

        return splitPane;
    }

    /**
     * Creates a new tab for a given or a new model with given type definition
     *
     * @param model
     * @param typeDef
     */
    private void addModelToView(AbstractGraphModel model, TypeDefinitionBean typeDef) {
        SingleModelMultiEditView modelView = new SingleModelMultiEditView(fContext, model);
        fModelTabs.put(modelView.getView(), model);
        fEditorArea.addTab(typeDef.getName(), modelView.getView());
        modelView.setTransferHandler(new TypeTransferHandler());
        fEditorArea.setSelectedIndex(fEditorArea.getComponentCount() - 1);
        fEditorArea.setToolTipTextAt(fEditorArea.getComponentCount() - 1, model.getPackageName() + "." + model.getName());
        fTypeDefinitions.add(typeDef);
    }

    /**
     * Show the type definition dialog - create the dialog if not yet instantiated
     */
    private void showTypeDefinitionDialog() {
        if (fTypeDefView == null) {
            fTypeDefView = new TypeDefinitionDialog(UlcUtilities.getWindowAncestor(fEditorArea), fTypeDefinitions);
            IActionListener newModelListener = new IActionListener() {

                public void actionPerformed(ActionEvent event) {
                    TypeDefinitionBean typeDef = fTypeDefView.getBeanForm().getModel().getBean();
                    AbstractGraphModel model = typeDef.getBaseType().equals("Model") ? new ModelGraphModel() : new ComposedComponentGraphModel();
                    model.setPackageName(typeDef.getPackageName());
                    model.setName(typeDef.getName());
                    addModelToView(model, typeDef);
                    fTypeDefView.setVisible(false);
                }
            };
            fTypeDefView.getBeanForm().addSaveActionListener(newModelListener);
        } else {
            TypeDefinitionBean newBean = new TypeDefinitionBean();
            fTypeDefView.getBeanForm().setModel(new TypeDefinitionFormModel(newBean));
        }
        fTypeDefView.setVisible(true);
    }

    /**
     * Import the component type specified by <code>clazzName</code>.
     * It means that for a given class (of type <code>ComposedComponent</code> or <code>AbstractModel</code>
     * a corresponding graph model is created and added to the model edit pane (as a new tab).
     *
     * @param clazzName name of the class to be imported
     * @return boolean to indicate whether the import was successful.
     * @throws ClassNotFoundException
     */
    public boolean importComponentType(String clazzName) throws ClassNotFoundException {
        Class clazz = getClass().getClassLoader().loadClass(clazzName);
        AbstractGraphImport importer = null;
        if (ComposedComponent.class.isAssignableFrom(clazz)) {
            importer = new ComposedComponentGraphImport();
        } else if (Model.class.isAssignableFrom(clazz)) {
            importer = new ModelGraphImport();
        }
        if (importer != null) {
            AbstractGraphModel model = importer.importGraph(clazz, null);
            TypeDefinitionBean typeDef = new TypeDefinitionBean();
            typeDef.setBaseType(model instanceof ModelGraphModel ? "Model" : "ComposedComponent");
            typeDef.setName(model.getName());
            typeDef.setPackageName(model.getPackageName());
            addModelToView(model, typeDef);
            return true;
        }
        return false;
    }

    /**
     * Action for creating a new graph model and preparing a new tab in the editor pane.
     * Delegates to showTypDefinitionDialog.
     */
    @Action
    public void newModelAction() {
        if (fTypeDefView == null || !fTypeDefView.isVisible()) {
            showTypeDefinitionDialog();
        }
    }

    /**
     * Deploys the model in RA application.
     */
    @Action
    public void exportToApplication() {
        if (fEditorArea.getComponentCount() == 0) {
            ULCAlert alert = new ULCAlert("No Model found", "No model or component to be saved", "ok");
            alert.show();
        } else {
            int i = fEditorArea.getSelectedIndex();
            ULCComponent component = fEditorArea.getComponentAt(i);
            if (fModelTabs.containsKey(component)) {
                AbstractGraphModel model = fModelTabs.get(component);
                GraphModelUtilities.exportToApplication((ModelGraphModel) model);
            } else {
                ULCAlert alert = new ULCAlert("No Model found", "Current page does not contain a model or component specification.", "ok");
                alert.show();
            }

        }
    }

    @SuppressWarnings("serial")
    @Action
    public void importModelAction() {
        FileChooserConfig config = new FileChooserConfig();
        config.setDialogTitle("Open file");
        config.setDialogType(FileChooserConfig.FILES_ONLY);
        config.addFileFilterConfig(new FileChooserConfig.FileFilterConfig(
                new String[]{"groovy"}, "groovy files (*.groovy)")
        );

        IFileLoadHandler handler = new IFileLoadHandler() {
            public void onSuccess(InputStream[] inputStreams, String[] filePaths, String[] fileNames) {
                try {
                    InputStream in = inputStreams[0];
                    Writer writer = new StringWriter();
                    char[] buffer = new char[1024];
                    try {
                        Reader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                        int n;
                        while ((n = reader.read(buffer)) != -1) {
                            writer.write(buffer, 0, n);
                        }
                    } finally {
                        in.close();
                    }
                    String content = writer.toString();
                    GraphImportService importService = new GraphImportService();
                    AbstractGraphModel model = importService.importGraph(content);
                    TypeDefinitionBean typeDef = new TypeDefinitionBean();
                    typeDef.setBaseType(model instanceof ModelGraphModel ? "Model" : "Composed Component");
                    typeDef.setName(model.getName());
                    typeDef.setPackageName(model.getPackageName());
                    addModelToView(model, typeDef);
                } catch (Exception ex) {
                    new ULCAlert(UlcUtilities.getWindowAncestor(fEditorArea), "Import failed", "The specified file could not be imported. Reason: " + ex.getMessage(), "Ok").show();
                }
            }

            public void onFailure(int reason, String description) {
                new ULCAlert(UlcUtilities.getWindowAncestor(fEditorArea), "Import failed", "The specified file could not be imported.", "Ok").show();
            }
        };

        ClientContext.loadFile(handler, config, fEditorArea);
    }

    @Action
    public void loadModelAction() {
        GraphPersistenceService service = new GraphPersistenceService();
       // AbstractGraphModel model = service.load(name,type);

        
    }

    @Action
    public void saveModelAction() {
        AbstractGraphModel model = getSelectedModel();
        GraphPersistenceService service = new GraphPersistenceService();
        service.save(model);
    }

    @Action
    public void exportModelToGroovyAction() {
        AbstractGraphModel model = getSelectedModel();
        String text = GraphModelUtilities.getGroovyModelCode(model);
        saveOutput(model.getName() + ".groovy", text, UlcUtilities.getWindowAncestor(this.getContentView()));
    }

    private AbstractGraphModel getSelectedModel() {
        ULCComponent comp = fEditorArea.getSelectedComponent();
        AbstractGraphModel model = fModelTabs.get(comp);
        return model;
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

    @Action
    public void exportModelToApplicationAction() {
        AbstractGraphModel model = getSelectedModel();
        if (model instanceof ModelGraphModel) {
            try {
                GraphModelUtilities.exportToApplication((ModelGraphModel) model);
            } catch (Exception ex) {
                ULCAlert alert = new ULCAlert("Model not deployed.", "Model could not be deployed. Reason: " + ex.getMessage(), "ok");
                alert.show();
            }
        } else {
            ULCAlert alert = new ULCAlert("Graph Model cannot be deployed.", "Graph model is a ComposedComponent - these cannot be deployed and run.", "ok");
            alert.show();
        }
    }

    protected ApplicationActionMap getActionMap() {
        return fContext.getActionMap(this);
    }

    public ULCToolBar getToolBar() {
        return new ToolBarFactory(getActionMap()).createToolBar("newModelAction", "loadModelAction", "importModelAction", "saveModelAction", "exportModelToGroovyAction", "exportModelToApplicationAction");
    }
}
