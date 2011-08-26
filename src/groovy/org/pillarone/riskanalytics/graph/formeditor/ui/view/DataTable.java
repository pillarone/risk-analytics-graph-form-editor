package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.applicationframework.application.ApplicationActionMap;
import com.ulcjava.base.application.*;
import com.ulcjava.base.application.datatype.IDataType;
import com.ulcjava.base.application.datatype.ULCNumberDataType;
import com.ulcjava.base.application.event.ActionEvent;
import com.ulcjava.base.application.event.FocusEvent;
import com.ulcjava.base.application.event.IActionListener;
import com.ulcjava.base.application.event.IFocusListener;
import com.ulcjava.base.application.tabletree.*;
import com.ulcjava.base.application.tree.TreePath;
import com.ulcjava.base.application.util.Color;
import com.ulcjava.base.application.util.Dimension;
import com.ulcjava.base.shared.UlcEventConstants;
import org.pillarone.riskanalytics.core.simulation.item.Parameterization;
import org.pillarone.riskanalytics.graph.core.graph.model.ComposedComponentGraphModel;
import org.pillarone.riskanalytics.graph.core.graph.model.ModelGraphModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.DataTableTreeModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.IDataTreeNode;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class DataTable extends ULCBoxPane {

    ModelGraphModel fGraphModel;
    DataTableTreeModel fTableModel;
    ULCTableTree fTableTree;


    public DataTable(ModelGraphModel model, int periodCount, String dataObjectName) {
        super(true);
        fTableModel = new DataTableTreeModel(model, periodCount, dataObjectName);
        fGraphModel = model;
        fGraphModel.addGraphModelChangeListener(fTableModel);
        createView();
        addCellEditorsAndRenderers();
    }

    public DataTable(ModelGraphModel model, Parameterization parametrization) {
        super(true);
        fTableModel = new DataTableTreeModel(model, parametrization);
        fGraphModel = model;
        fGraphModel.addGraphModelChangeListener(fTableModel);
        createView();
        addCellEditorsAndRenderers();
    }

    public void createView() {
        ClientContext.setModelUpdateMode(fTableModel, UlcEventConstants.SYNCHRONOUS_MODE);
        fTableTree = new ULCTableTree(fTableModel);
        //fTableTree.createDefaultColumnsFromModel();
        fTableTree.setShowGrid(true);

        int width = ClientContext.getScreenWidth();
        int height = ClientContext.getScreenHeight();
        int preferredWidth = width / 2;
        int preferredHeight = preferredWidth * height * 10 / (width * 11 * 4);
        fTableTree.setPreferredScrollableViewportSize(new Dimension(preferredWidth, preferredHeight));

        ULCScrollPane scrollPane = new ULCScrollPane(fTableTree);
        this.add(ULCBoxPane.BOX_EXPAND_EXPAND, scrollPane);
        this.setBorder(BorderFactory.createEmptyBorder());

        createContextMenu();
        new SelectionTracker(fTableTree);
    }

    public DataTableTreeModel getModel() {
        return fTableModel;
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
                fTableTree.collapsePaths(selectedPaths, true);
            }
        });
        menu.add(collapseItem);
        ULCMenuItem collapseAllItem = new ULCMenuItem("collapse all");
        collapseAllItem.addActionListener(new IActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                fTableTree.collapseAll();
            }
        });
        menu.add(collapseAllItem);

        fTableTree.setComponentPopupMenu(menu);
    }


    private void addCellEditorsAndRenderers() {
        Map<Class, ITableTreeCellEditor> editors = createEditors();
        DefaultTableTreeCellRenderer defaultTableTreeCellRenderer = new DefaultTableTreeCellRenderer();
        for (int i = 0; i < fTableTree.getColumnModel().getColumnCount(); i++) {
            ULCTableTreeColumn column = fTableTree.getColumnModel().getColumn(i);
            column.setMaxWidth(200);
            if (i == 0) {
                column.setCellRenderer(defaultTableTreeCellRenderer);
            } else {
                column.setCellEditor(new DelegatingCellEditor(editors));
                IDataType dataType = new ULCNumberDataType<Double>(ClientContext.getLocale());
                column.setCellRenderer(new BasicCellRenderer(i, dataType));
            }


        }
    }

    private class DelegatingCellEditor extends DefaultCellEditor implements ITableTreeCellEditor {

        private Map<Class, ITableTreeCellEditor> editors;

        public DelegatingCellEditor(Map<Class, ITableTreeCellEditor> editors) {
            super(new ULCTextField());
            this.editors = editors;
        }

        public IEditorComponent getTableTreeCellEditorComponent(ULCTableTree ulcTableTree, Object value,
                                                                boolean selected, boolean expanded,
                                                                boolean leaf, Object node) {
            ITableTreeCellEditor editor = null;
            if (node instanceof IDataTreeNode) {
                editor = editors.get(((IDataTreeNode) node).getType());
            }
            if (editor != null) {
                return editor.getTableTreeCellEditorComponent(ulcTableTree, value, selected, expanded, leaf, node);
            } else {
                return super.getTableTreeCellEditorComponent(ulcTableTree, value, selected, expanded, leaf, node);
            }
        }
    }

    private Map<Class, ITableTreeCellEditor> createEditors() {
        Map<Class, ITableTreeCellEditor> editors = new HashMap<Class, ITableTreeCellEditor>();
        // double
        BaseCellEditor doubleEditor = new BaseCellEditor(Double.class) {
            @Override
            public IEditorComponent getTableTreeCellEditorComponent(ULCTableTree tableTree, Object value, boolean selected, boolean expanded, boolean leaf, Object node) {
                ULCTextField editor = (ULCTextField) super.getTableTreeCellEditorComponent(tableTree, value, selected, expanded, leaf, node);
                editor.setDataType(this.dataType);
                editor.setHorizontalAlignment(ULCTextField.RIGHT);
                return editor;
            }
        };
        editors.put(Double.class, doubleEditor);

        // integer
        BaseCellEditor integerEditor = new BaseCellEditor(Integer.class) {
            @Override
            public IEditorComponent getTableTreeCellEditorComponent(ULCTableTree tableTree, Object value, boolean selected, boolean expanded, boolean leaf, Object node) {
                ULCTextField editor = (ULCTextField) super.getTableTreeCellEditorComponent(tableTree, value, selected, expanded, leaf, node);
                editor.setDataType(this.dataType);
                editor.setHorizontalAlignment(ULCTextField.RIGHT);
                return editor;
            }
        };
        editors.put(Integer.class, integerEditor);

        // boolean
        ITableTreeCellEditor booleanEditor = new CheckBoxCellComponent();
        editors.put(Boolean.class, booleanEditor);

        return editors;
    }

    private class BaseCellEditor extends DefaultCellEditor {
        protected IDataType dataType;

        BaseCellEditor(Class type) {
            super(new ULCTextField());
            if (type.equals(Double.class)) {
                dataType = new ULCNumberDataType<Double>(ClientContext.getLocale());
            } else if (type.equals(Integer.class)) {
                dataType = new ULCNumberDataType<Integer>(ClientContext.getLocale());
            }
        }
    }

    private class CheckBoxCellComponent extends ULCCheckBox implements ITableTreeCellRenderer, ITableTreeCellEditor {

        CheckBoxCellComponent() {
            setHorizontalAlignment(ULCCheckBox.RIGHT);
        }

        public IRendererComponent getTableTreeCellRendererComponent(ULCTableTree ulcTableTree, Object value, boolean selected, boolean hasFocus, boolean expanded, boolean leaf, Object node) {
            selected = (Boolean) value;
            return this;
        }

        public IEditorComponent getTableTreeCellEditorComponent(ULCTableTree tableTree, Object value, boolean selected, boolean expanded, boolean leaf, Object node) {
            selected = (Boolean) value;
            return this;
        }
    }



    public class DelegatingCellRenderer extends DefaultTableTreeCellRenderer {

        private HashMap<Class, ITableTreeCellRenderer> renderers;

        public DelegatingCellRenderer(HashMap<Class, ITableTreeCellRenderer> renderers) {
            this.renderers = renderers;
        }

        @Override
        public IRendererComponent getTableTreeCellRendererComponent(ULCTableTree ulcTableTree, Object value,
                                                                    boolean selected, boolean hasFocus, boolean expanded,
                                                                    boolean leaf, Object node) {
            ITableTreeCellRenderer renderer = renderers.get(node.getClass());
            if (renderer != null) {
                return renderer.getTableTreeCellRendererComponent(ulcTableTree, value, selected, hasFocus, expanded, leaf, node);
            } else {
                return super.getTableTreeCellRendererComponent(ulcTableTree, value, selected, hasFocus, expanded, leaf, node);
            }
        }
    }
}

class BasicCellRenderer extends DefaultTableTreeCellRenderer implements ITableTreeCellRenderer {

        protected int columnIndex;
        private IDataType dataType;

        public BasicCellRenderer(int columnIndex, IDataType dataType) {
            this.columnIndex = columnIndex;
            this.dataType = dataType;
        }


        public IRendererComponent getTableTreeCellRendererComponent(ULCTableTree ulcTableTree, Object value,
                                                                    boolean selected, boolean hasFocus, boolean expanded,
                                                                    boolean leaf, Object node) {
            if (!selected) {
                if (value != null || ((IMutableTableTreeNode) node).isCellEditable(columnIndex)) {
                    setBackground(Color.white);
                    // setComponentPopupMenu(menu);
                } else {
                    setBackground(Color.lightGray);
                    setComponentPopupMenu(null);
                }
                setHorizontalAlignment(ULCLabel.RIGHT);
            }
            setDataType(dataType);
            return super.getTableTreeCellRendererComponent(ulcTableTree, value, selected, hasFocus, expanded, leaf, node);
        }
    }