package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.canoo.ulc.community.fixedcolumntabletree.server.ULCFixedColumnTableTree;
import com.canoo.ulc.detachabletabbedpane.server.ITabListener;
import com.canoo.ulc.detachabletabbedpane.server.TabEvent;
import com.canoo.ulc.detachabletabbedpane.server.ULCCloseableTabbedPane;
import com.ulcjava.base.application.*;
import com.ulcjava.base.application.event.ActionEvent;
import com.ulcjava.base.application.event.IActionListener;
import com.ulcjava.base.application.event.ITreeSelectionListener;
import com.ulcjava.base.application.event.KeyEvent;
import com.ulcjava.base.application.tabletree.*;
import com.ulcjava.base.application.tree.TreePath;
import com.ulcjava.base.application.util.Dimension;
import com.ulcjava.base.application.util.KeyStroke;
import com.ulcjava.base.shared.UlcEventConstants;
import groovy.util.ConfigObject;
import org.pillarone.riskanalytics.application.ui.base.action.OpenMDPAction;
import org.pillarone.riskanalytics.application.ui.base.action.TableTreeCopier;
import org.pillarone.riskanalytics.application.ui.base.action.TreeNodePaster;
import org.pillarone.riskanalytics.application.ui.base.model.ComponentTableTreeNode;
import org.pillarone.riskanalytics.application.ui.base.model.SimpleTableTreeNode;
import org.pillarone.riskanalytics.application.ui.base.view.DelegatingCellEditor;
import org.pillarone.riskanalytics.application.ui.base.view.DelegatingCellRenderer;
import org.pillarone.riskanalytics.application.ui.parameterization.model.*;
import org.pillarone.riskanalytics.application.ui.parameterization.view.*;
import org.pillarone.riskanalytics.application.ui.util.DataTypeFactory;
import org.pillarone.riskanalytics.application.ui.util.UIUtils;
import org.pillarone.riskanalytics.core.model.Model;
import org.pillarone.riskanalytics.core.parameterization.ParameterizationHelper;
import org.pillarone.riskanalytics.core.simulation.item.ModelStructure;
import org.pillarone.riskanalytics.core.simulation.item.Parameterization;
import org.pillarone.riskanalytics.graph.core.graph.model.AbstractGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.model.ComponentNode;
import org.pillarone.riskanalytics.graph.core.graph.model.Connection;
import org.pillarone.riskanalytics.graph.core.graph.model.filters.IComponentNodeFilter;
import org.pillarone.riskanalytics.graph.formeditor.ui.ISaveListener;
import org.pillarone.riskanalytics.graph.formeditor.ui.ISelectionListener;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.treetable.NodeNameFilter;
import org.pillarone.riskanalytics.graph.formeditor.ui.view.helpers.ParameterizationTableTreeGraphModelListener;
import org.pillarone.riskanalytics.graph.formeditor.ui.view.helpers.SelectionTracker;
import org.pillarone.riskanalytics.graph.formeditor.util.GraphModelAdapter;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class DataTable extends ULCBoxPane implements ISelectionListener, ISaveListener {

    AbstractGraphModel fGraphModel;
    AbstractTableTreeModel fTableModel;
    ULCFixedColumnTableTree fTableTree;
    Parameterization parameterization;


    public DataTable(AbstractGraphModel model, int periodCount, String dataObjectName) {
        super(true);
        fGraphModel = model;
        final GraphModelAdapter modelAdapter = new GraphModelAdapter(fGraphModel);
        final Parameterization parameterization = ParameterizationHelper.createDefaultParameterization(modelAdapter, periodCount);
        parameterization.setName(dataObjectName);
        createTableTreeModel(modelAdapter, parameterization);
        fGraphModel.addGraphModelChangeListener(new ParameterizationTableTreeGraphModelListener((ParameterizationTableTreeModel) fTableModel));
        createView();
        addCellEditorsAndRenderers();
        ClientContext.setModelUpdateMode(fTableModel, UlcEventConstants.SYNCHRONOUS_MODE);
    }

    public DataTable(AbstractGraphModel model, Parameterization parametrization) {
        super(true);
        fGraphModel = model;
        createTableTreeModel(new GraphModelAdapter(model), parametrization);
        fGraphModel.addGraphModelChangeListener(new ParameterizationTableTreeGraphModelListener((ParameterizationTableTreeModel) fTableModel));
        createView();
        addCellEditorsAndRenderers();
        ClientContext.setModelUpdateMode(fTableModel, UlcEventConstants.SYNCHRONOUS_MODE);
    }

    private void createTableTreeModel(Model model, Parameterization parameterization) {
        this.parameterization = parameterization;
        ModelStructure structure = new ModelStructure(model.getName() + "Structure");
        structure.setData(new ConfigObject());
        ParameterizationTreeBuilder treeBuilder = new ParameterizationTreeBuilder(model, structure, parameterization);
        fTableModel = new ParameterizationTableTreeModel(treeBuilder);
    }

    public void createView() {
        ClientContext.setModelUpdateMode(fTableModel, UlcEventConstants.SYNCHRONOUS_MODE);
        int treeWidth = UIUtils.calculateTreeWidth(fTableModel.getRoot());
        int columnsWidths = Math.max(UIUtils.calculateColumnWidth(fTableModel.getRoot(), 1) + 10, 150);

        int[] columnWidths = new int[1 + parameterization.getPeriodCount()];
        Arrays.fill(columnWidths, columnsWidths);
        columnWidths[0] = treeWidth;

        fTableTree = new ULCFixedColumnTableTree(fTableModel, 1, columnWidths);
        //fTableTree.createDefaultColumnsFromModel();
        fTableTree.getViewPortTableTree().setShowGrid(true);

        int width = ClientContext.getScreenWidth();
        int height = ClientContext.getScreenHeight();
        int preferredWidth = width / 2;
        int preferredHeight = preferredWidth * height * 10 / (width * 11 * 4);
        fTableTree.getViewPortTableTree().setPreferredScrollableViewportSize(new Dimension(preferredWidth, preferredHeight));

        ULCScrollPane scrollPane = new ULCScrollPane(fTableTree);
        this.add(ULCBoxPane.BOX_EXPAND_EXPAND, scrollPane);
        this.setBorder(BorderFactory.createEmptyBorder());

        createContextMenu();
        new SelectionTracker(fTableTree.getRowHeaderTableTree());
    }

    public AbstractTableTreeModel getModel() {
        return fTableModel;
    }

    public Parameterization getParameterization() {
        return parameterization;
    }

    public ULCFixedColumnTableTree getTableTree() {
        return fTableTree;
    }

    private void createContextMenu() {
        ULCPopupMenu menu = new ULCPopupMenu();

        ULCMenuItem expandItem = new ULCMenuItem("expand");
        expandItem.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                TreePath[] selectedPaths = fTableTree.getSelectedPaths();
                fTableTree.expandPaths(selectedPaths, true);
            }
        });
        menu.add(expandItem);
        ULCMenuItem expandAllItem = new ULCMenuItem("expand all");
        expandAllItem.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                fTableTree.expandAll();
            }
        });
        menu.add(expandAllItem);
        ULCMenuItem collapseItem = new ULCMenuItem("collapse");
        collapseItem.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                TreePath[] selectedPaths = fTableTree.getSelectedPaths();
                fTableTree.getRowHeaderTableTree().collapsePaths(selectedPaths, true);
            }
        });
        menu.add(collapseItem);
        ULCMenuItem collapseAllItem = new ULCMenuItem("collapse all");
        collapseAllItem.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                fTableTree.getRowHeaderTableTree().collapseAll();
            }
        });
        menu.add(collapseAllItem);

        fTableTree.setComponentPopupMenu(menu);
    }


    private void addCellEditorsAndRenderers() {

        int index = 0;
        final ULCTableTree viewPortTableTree = fTableTree.getViewPortTableTree();
        final Enumeration<AbstractColumn> columns = viewPortTableTree.getColumnModel().getColumns();
        while (columns.hasMoreElements()) {
            ULCTableTreeColumn it = (ULCTableTreeColumn) columns.nextElement();
            it.setCellEditor(new DelegatingCellEditor(createEditorConfiguration()));
            it.setCellRenderer(new DelegatingCellRenderer(createRendererConfiguration(++index, viewPortTableTree)));
            it.setHeaderRenderer(new CenteredHeaderRenderer());
        }
    }

    private HashMap<Class, ITableTreeCellEditor> createEditorConfiguration() {
        DefaultCellEditor defaultEditor = new DefaultCellEditor(new ULCTextField());
        DefaultCellEditor doubleEditor = new BasicCellEditor(DataTypeFactory.getDoubleDataTypeForEdit());
        DefaultCellEditor integerEditor = new BasicCellEditor(DataTypeFactory.getIntegerDataTypeForEdit());
        DefaultCellEditor dateEditor = new BasicCellEditor(DataTypeFactory.getDateDataType());

        ComboBoxCellComponent comboBoxEditor = new ComboBoxCellComponent();
        CheckBoxCellComponent checkBoxEditor = new CheckBoxCellComponent();

        HashMap<Class, ITableTreeCellEditor> editors = new HashMap<Class, ITableTreeCellEditor>();
        editors.put(SimpleValueParameterizationTableTreeNode.class,
                defaultEditor);
        editors.put(DoubleTableTreeNode.class,
                doubleEditor);
        editors.put(BooleanTableTreeNode.class, checkBoxEditor);
        editors.put(IntegerTableTreeNode.class,
                integerEditor);
        editors.put(DateParameterizationTableTreeNode.class,
                dateEditor);
        editors.put(EnumParameterizationTableTreeNode.class,
                comboBoxEditor);
        editors.put(ParameterizationClassifierTableTreeNode.class,
                comboBoxEditor);
        editors.put(ConstrainedStringParameterizationTableTreeNode.class,
                comboBoxEditor);

        return editors;
    }

    private HashMap<Class, ITableTreeCellRenderer> createRendererConfiguration(int columnIndex, ULCTableTree tree) {
        BasicCellRenderer defaultRenderer = new BasicCellRenderer(columnIndex, null);
        MultiDimensionalCellRenderer mdpRenderer = new MultiDimensionalCellRenderer(columnIndex);
        BasicCellRenderer doubleRenderer = new BasicCellRenderer(columnIndex, DataTypeFactory.getDoubleDataTypeForNonEdit());
        BasicCellRenderer integerRenderer = new BasicCellRenderer(columnIndex, DataTypeFactory.getIntegerDataTypeForNonEdit());
        BasicCellRenderer dateRenderer = new BasicCellRenderer(columnIndex, DataTypeFactory.getDateDataType());
        ComboBoxCellComponent comboBoxRenderer = new ComboBoxCellComponent();
        CheckBoxCellComponent checkBoxRenderer = new CheckBoxCellComponent();

        ULCPopupMenu menu = new ULCPopupMenu();
        ULCPopupMenu mdpMenu = new ULCPopupMenu();
        mdpMenu.add(new ULCMenuItem(new OpenMDPAction(tree)));

        TableTreeCopier copier = new TableTreeCopier();
        copier.setTable(tree);
        menu.add(new ULCMenuItem(copier));
        mdpMenu.add(new ULCMenuItem(copier));
        TreeNodePaster paster = new TreeNodePaster();
        paster.setTree(tree);
        menu.add(new ULCMenuItem(paster));
        mdpMenu.add(new ULCMenuItem(paster));

        defaultRenderer.setMenu(menu);
        doubleRenderer.setMenu(menu);
        integerRenderer.setMenu(menu);
        dateRenderer.setMenu(menu);
        initComboBox(comboBoxRenderer, menu);
        initCheckBox(checkBoxRenderer, menu);
        mdpRenderer.setMenu(mdpMenu);

        HashMap<Class, ITableTreeCellRenderer> renderers = new HashMap<Class, ITableTreeCellRenderer>();
        renderers.put(SimpleValueParameterizationTableTreeNode.class,
                defaultRenderer);
        renderers.put(DoubleTableTreeNode.class,
                doubleRenderer);
        renderers.put(BooleanTableTreeNode.class, checkBoxRenderer);
        renderers.put(IntegerTableTreeNode.class,
                integerRenderer);
        renderers.put(DateParameterizationTableTreeNode.class,
                dateRenderer);
        renderers.put(EnumParameterizationTableTreeNode.class,
                comboBoxRenderer);
        renderers.put(ParameterizationClassifierTableTreeNode.class,
                comboBoxRenderer);
        renderers.put(ConstrainedStringParameterizationTableTreeNode.class,
                comboBoxRenderer);
        renderers.put(MultiDimensionalParameterizationTableTreeNode.class,
                mdpRenderer);

        return renderers;
    }

    private void initComboBox(ULCComboBox renderer, ULCPopupMenu menu) {
        renderer.setComponentPopupMenu(menu);
    }

    private void initCheckBox(ULCCheckBox renderer, ULCPopupMenu menu) {
        renderer.setComponentPopupMenu(menu);
    }

    public void applyFilter(IComponentNodeFilter filter) {
    }

    public void applyFilter(NodeNameFilter filter) {
    }

    public void setSelectedComponents(List<ComponentNode> selection) {
        ULCTableTree tree = fTableTree.getRowHeaderTableTree();
        for (ComponentNode cn : selection) {
            ComponentTableTreeNode node = findTreeNode(cn);
            if (node != null) {
                TreePath treePath = new TreePath(DefaultTableTreeModel.getPathToRoot(node));
                tree.makeVisible(treePath);
                tree.scrollCellToVisible(treePath, 0);
                if (!tree.getSelectionModel().isPathSelected(treePath))
                    tree.getSelectionModel().setSelectionPath(treePath);
            }
        }
    }

    protected ComponentTableTreeNode findTreeNode(ComponentNode node) {
        SimpleTableTreeNode root = (SimpleTableTreeNode) fTableModel.getRoot();
        for (Object child : root.getChildren()) {
            if(child instanceof ComponentTableTreeNode) {
                ComponentTableTreeNode componentTableTreeNode = (ComponentTableTreeNode) child;
                if(componentTableTreeNode.getComponent().getName().equals(node.getName())) {
                    return componentTableTreeNode;
                }
            }
        }
        return null;
    }

    public void nodeSelected(String path) { }

    public void setSelectedConnections(List<Connection> selection) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void clearSelection() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addTreeSelectionListener(ITreeSelectionListener treeSelectionListener) {
        fTableTree.addTreeSelectionListener(treeSelectionListener);
    }

    public void save() {
//       fTableModel.save();
    }


    public static class MDPTabStarter implements IActionListener {

        private ULCCloseableTabbedPane tabbedPane;
        private Map<TabIdentifier, AtomicInteger> openTabs = new HashMap<TabIdentifier, AtomicInteger>();

        public MDPTabStarter(ULCCloseableTabbedPane tabbedPane) {
            this.tabbedPane = tabbedPane;
            attachListeners();
        }

        public void actionPerformed(ActionEvent event) {
            ULCTableTree tree = (ULCTableTree) event.getSource();
            Object lastComponent = tree.getSelectedPath().getLastPathComponent();

            if (lastComponent instanceof MultiDimensionalParameterizationTableTreeNode) {
                TabIdentifier identifier = new TabIdentifier();
                identifier.setPath(tree.getSelectedPath());
                identifier.setColumnIndex(tree.getSelectedColumn());

                AtomicInteger index = openTabs.get(identifier);

                if (index == null) {
                    MultiDimensionalParameterModel model = new MultiDimensionalParameterModel(tree.getModel(), (MultiDimensionalParameterizationTableTreeNode) lastComponent, tree.getSelectedColumn() + 1);
                    ClientContext.setModelUpdateMode(model.getTableModel(), UlcEventConstants.SYNCHRONOUS_MODE);
                    tabbedPane.addTab(((MultiDimensionalParameterizationTableTreeNode) lastComponent).getDisplayName() + " " + tree.getColumnModel().getColumn(tree.getSelectedColumn()).getHeaderValue(), UIUtils.getIcon(UIUtils.getText(this.getClass(), "MDP.icon", null)), new MultiDimensionalParameterView(model).getContent());
                    int currentTab = tabbedPane.getTabCount() - 1;
                    tabbedPane.setSelectedIndex(currentTab);
                    tabbedPane.setToolTipTextAt(currentTab, model.getPathAsString());
                    final TabIdentifier tabIdentifier = new TabIdentifier();
                    tabIdentifier.setPath(tree.getSelectedPath());
                    tabIdentifier.setColumnIndex(tree.getSelectedColumn());
                    openTabs.put(tabIdentifier, new AtomicInteger(currentTab));
                } else {
                    tabbedPane.setSelectedIndex(index.get());
                }
            } else {
                if (tree.getSelectedRow() >= 0) {
                    int selectedRow = tree.getSelectedRow();
                    if (selectedRow + 1 <= tree.getRowCount()) {
                        tree.getSelectionModel().setSelectionPath(tree.getPathForRow(selectedRow + 1));
                    }
                }
            }
        }

        protected void attachListeners() {
            tabbedPane.addTabListener(new ITabListener() {
                public void tabClosing(TabEvent event) {
                    removeTab(event.getTabClosingIndex());
                }
            });
            tabbedPane.registerKeyboardAction(new IActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    int index = tabbedPane.getSelectedIndex();
                    if (tabbedPane.isCloseable(index)) {
                        tabbedPane.closeCloseableTab(index);
                        removeTab(index);
                    }
                }
            }, KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK, false), ULCComponent.WHEN_IN_FOCUSED_WINDOW);

        }

        private void removeTab(int index) {
            for (Iterator<Map.Entry<TabIdentifier, AtomicInteger>> it = openTabs.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<TabIdentifier, AtomicInteger> entry = it.next();
                if (entry.getValue().get() > index) {
                    entry.getValue().decrementAndGet();
                } else if (entry.getValue().get() == index) {
                    it.remove();
                }
            }
        }

    }

}


