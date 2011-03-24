package org.pillarone.riskanalytics.graph.formeditor.ui.view;


import com.canoo.ulc.detachabletabbedpane.server.ITabListener;
import com.canoo.ulc.detachabletabbedpane.server.TabEvent;
import com.canoo.ulc.detachabletabbedpane.server.ULCCloseableTabbedPane;
import com.ulcjava.applicationframework.application.*;
import com.ulcjava.applicationframework.application.ApplicationContext;
import com.ulcjava.base.application.*;
import com.ulcjava.base.application.event.ActionEvent;
import com.ulcjava.base.application.event.IActionListener;
import com.ulcjava.base.application.tree.DefaultMutableTreeNode;
import com.ulcjava.base.application.tree.TreePath;
import com.ulcjava.base.application.util.IFileChooseHandler;
import com.ulcjava.base.application.util.IFileStoreHandler;
import com.ulcjava.base.shared.FileChooserConfig;
import org.pillarone.riskanalytics.core.components.ComposedComponent;
import org.pillarone.riskanalytics.core.model.Model;
import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.model.ComposedComponentGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.model.ModelGraphModel;
import org.pillarone.riskanalytics.graph.core.graphimport.AbstractGraphImport;
import org.pillarone.riskanalytics.graph.core.graphimport.ComposedComponentGraphImport;
import org.pillarone.riskanalytics.graph.core.graphimport.ModelGraphImport;
import org.pillarone.riskanalytics.graph.formeditor.ui.handlers.TypeTransferHandler;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.TypeDefinitionFormModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.beans.TypeDefinitionBean;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.beans.TypeImportBean;
import org.pillarone.riskanalytics.graph.formeditor.util.ComponentTypeTreeUtilities;
import org.pillarone.riskanalytics.graph.formeditor.util.GraphModelUtilities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The main window with the form editor view.
 * It inherits from {@link AbstractBean} to benefit from its property change support. 
 * 
 * The window is a tabbed pane which contains for each model or component to be edited a tab.
 * 
 * @author martin.melchior
 */
public class FormEditorModelsView extends AbstractBean {
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
    /* The type tree view.*/
	private ComponentTypeTree fComponentTypeTree;

    /**
     * @param context Application context is used for accessing and using resources (such as icons, etc.).
     */
    public FormEditorModelsView(ApplicationContext context) {
        fContext = context;
        fTypeDefinitions = new HashSet<TypeDefinitionBean>();
    	fModelTabs = new HashMap<ULCComponent, AbstractGraphModel>();
    }

    /**
     * Returns the tabbed pane as content view.
     * @return
     */
    public ULCComponent getContentView() {
        ULCBoxPane modelEdit = new ULCBoxPane(true);
        ULCSeparator separator = new ULCSeparator();
        separator.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        modelEdit.add(ULCBoxPane.BOX_EXPAND_BOTTOM, ULCFiller.createVerticalStrut(3));
        modelEdit.add(ULCBoxPane.BOX_EXPAND_BOTTOM, separator);

        fEditorArea = new ULCCloseableTabbedPane();
        fEditorArea.addTabListener(new ITabListener() {
        	public void tabClosing(TabEvent event) {
        		event.getClosableTabbedPane().closeCloseableTab(event.getTabClosingIndex());
        		if (fEditorArea.getTabCount()>0) {
        			event.getClosableTabbedPane().setSelectedIndex(0);
        		}
        	}
        });
        modelEdit.add(ULCBoxPane.BOX_EXPAND_EXPAND, fEditorArea);

        fComponentTypeTree = new ComponentTypeTree(this);

        ULCSplitPane splitPane = new ULCSplitPane(ULCSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(200);
        splitPane.setDividerSize(5);
        splitPane.setLeftComponent(fComponentTypeTree);
        splitPane.setRightComponent(modelEdit);

        return splitPane;
    }
    
    /**
     * Creates a new tab for a given model and type definition.
     * @param model
     * @param typeDef
     */
    private void addModelView(AbstractGraphModel model, TypeDefinitionBean typeDef) {
    	SingleModelEditView modelView = new SingleModelEditView(fContext, model);
		fModelTabs.put(modelView.getView(), model);
        fEditorArea.addTab(typeDef.getName(), modelView.getView());
        modelView.setTransferHandler(new TypeTransferHandler());
		fEditorArea.setSelectedIndex(fEditorArea.getComponentCount()-1);
		fTypeDefinitions.add(typeDef);
    }
    
	private void createTypeDefinitionDialog() {
    	fTypeDefView = new TypeDefinitionDialog(UlcUtilities.getWindowAncestor(fEditorArea), fTypeDefinitions);
        IActionListener newModelListener = new IActionListener() {

			public void actionPerformed(ActionEvent event) {
				TypeDefinitionBean typeDef = fTypeDefView.getBeanForm().getModel().getBean();
				AbstractGraphModel model = typeDef.isModel() ? new ModelGraphModel() : new ComposedComponentGraphModel();
				model.setPackageName(typeDef.getPackageName());
				model.setName(typeDef.getName());
				addModelView(model, typeDef);
				fTypeDefView.setVisible(false);
			}
		};
		fTypeDefView.getBeanForm().addSaveActionListener(newModelListener); 	
    }

	private void showTypeDefinitionDialog() {
        if (fTypeDefView == null) {
        	createTypeDefinitionDialog();
//            getResourceMap().injectComponents(fTypeDefView);
        } else {
            TypeDefinitionBean newBean = new TypeDefinitionBean();
            fTypeDefView.getBeanForm().setModel(new TypeDefinitionFormModel(newBean));
        }
        fTypeDefView.setVisible(true);        
    }

    @SuppressWarnings("serial")
	private void createTypeImportDialog() {
    	fTypeImportView = new TypeImportDialog(UlcUtilities.getWindowAncestor(fEditorArea));
        IActionListener newModelListener = new IActionListener() {			
			public void actionPerformed(ActionEvent event) {
                TypeImportBean bean = (TypeImportBean)fTypeImportView.getBeanForm().getModel().getBean();
                Class clazz = null;
                try {
                    boolean success = importComponentType(bean.getClazzName());
                    if (success) {
                        fTypeImportView.setVisible(false);
                	} else {
                		ULCAlert alert = new ULCAlert("No class loaded",
    							"No class with name " + bean.getClazzName() + " could be loaded as graph model.", "ok");
                		alert.show();
                        bean.reset();
                	}
                } catch (Exception ex) {
            		ULCAlert alert = new ULCAlert("No class loaded", 
							"No class with name " + bean.getClazzName() + " could be loaded as graph model.", "ok");
            		alert.show();
                    bean.reset();
                }
			}
		};
//		fTypeImportView.getBeanForm().addSaveActionListener(newModelListener);
    }

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
            typeDef.setModel(model instanceof ModelGraphModel);
            typeDef.setName(model.getName());
            typeDef.setPackageName(model.getPackageName());
            addModelView(model, typeDef);
            return true;
         }
        return false;
    }

	private void showTypeImportDialog() {
        if (fTypeImportView == null) {
        	createTypeImportDialog();
        } else {
            fTypeImportView.getBeanForm().getModel().getBean().reset();        	
        }
        fTypeImportView.setVisible(true);        
    }

    @Action
    public void showComponentAction(String componentName) {
    }

	@Action
    public void newAction() {
        if (fTypeDefView == null || !fTypeDefView.isVisible()) {
            showTypeDefinitionDialog();
        }
    }

	@Action
    public void exportAction() {
        if (fModelTabs==null || fModelTabs.size()==0) {
        	ULCAlert alert = new ULCAlert("No Model found", "No model or component found that could be exported", "ok");
        	alert.show();
        } else {
        	int i = fEditorArea.getSelectedIndex();
        	ULCComponent component = fEditorArea.getComponentAt(i);
        	if (fModelTabs.containsKey(component)) {
        		AbstractGraphModel model = fModelTabs.get(component);
                try {
        		    CodeView codeView = new CodeView();
        		    codeView.setText(GraphModelUtilities.getGroovyModelCode(model));
        		    fEditorArea.addTab(model.getName()+".groovy", codeView);
                } catch (Exception ex) {
                    ULCAlert alert = new ULCAlert("GraphModel not exported.", "Exception occurred: " + ex.getMessage(), "ok");
                    alert.show();
                }
        	} else {
            	ULCAlert alert = new ULCAlert("No Model found", "Current page does not contain a model or component specification.", "ok");
            	alert.show();        		
        	}
        	
        }
    }

	@Action
    public void saveAction() {
        if (fEditorArea.getComponentCount()==0) {
        	ULCAlert alert = new ULCAlert("No Model found", "No model or component to be saved", "ok");
        	alert.show();
        } else {
        	int i = fEditorArea.getSelectedIndex();
        	ULCComponent component = fEditorArea.getComponentAt(i);
        	if (fModelTabs.containsKey(component)) {
        		AbstractGraphModel model = fModelTabs.get(component);
        		String text = GraphModelUtilities.getGroovyModelCode(model);
        		saveOutput(model.getName()+".groovy", text, UlcUtilities.getWindowAncestor(fEditorArea));
        	} else if (component instanceof CodeView){
        		String text = ((CodeView)component).getText();
        		String name = "dummy"; // TODO: get the name of the class
        		saveOutput(name+".groovy", text, UlcUtilities.getWindowAncestor(fEditorArea));
        	} else {
            	ULCAlert alert = new ULCAlert("No Model found", "Current page does not contain a model or component specification.", "ok");
            	alert.show();        		
        	}
        	
        }
    }
	
	@SuppressWarnings("serial")
	@Action
    public void importAction() {
        if (fTypeImportView == null || !fTypeImportView.isVisible()) {
            showTypeImportDialog();
        }
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
	
    protected ApplicationActionMap getActionMap() {
        return fContext.getActionMap(this);
    }

    public ULCToolBar getToolBar() {
        return new ToolBarFactory(getActionMap()).createToolBar("newAction", "importAction", "exportAction", "saveAction");
    }
}
