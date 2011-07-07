package org.pillarone.riskanalytics.graph.formeditor.ui.view;

import com.ulcjava.base.application.*;
import com.ulcjava.base.application.datatype.IDataType;
import com.ulcjava.base.application.datatype.ULCNumberDataType;
import com.ulcjava.base.application.tabletree.*;
import com.ulcjava.base.application.util.Color;
import com.ulcjava.base.application.util.Dimension;
import org.pillarone.riskanalytics.core.simulation.item.Parameterization;
import org.pillarone.riskanalytics.graph.core.graph.model.ModelGraphModel;
import org.pillarone.riskanalytics.graph.formeditor.ui.model.DataTableTreeModel;

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
    }

    public DataTableTreeModel getModel() {
        return fTableModel;
    }

    private void addCellEditorsAndRenderers() {
        Map<Class, ITableTreeCellEditor> editors = createEditors();
        for (int i = 1; i < fTableTree.getColumnModel().getColumnCount(); i++) {
            ULCTableTreeColumn column = fTableTree.getColumnModel().getColumn(i);
            column.setCellEditor(new DelegatingCellEditor(editors));
            IDataType dataType = new ULCNumberDataType<Double>(ClientContext.getLocale());
            column.setCellRenderer(new BasicCellRenderer(i, dataType));
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
            if (node instanceof DataTableTreeModel.IDataTreeNode) {
                editor = editors.get(((DataTableTreeModel.IDataTreeNode)node).getType());
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

    public class BasicCellRenderer extends DefaultTableTreeCellRenderer implements ITableTreeCellRenderer {

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
                    setHorizontalAlignment(ULCLabel.RIGHT);
                    // setComponentPopupMenu(menu);
                } else {
                    setBackground(Color.lightGray);
                    setComponentPopupMenu(null);
                }
            }
            setDataType(dataType);
            return super.getTableTreeCellRendererComponent(ulcTableTree, value, selected, hasFocus, expanded, leaf, node);
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