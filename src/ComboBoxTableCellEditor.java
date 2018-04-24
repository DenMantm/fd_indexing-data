import java.awt.Component;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

    public class ComboBoxTableCellEditor extends AbstractCellEditor implements TableCellEditor {

        private JComboBox editor;
        private List<String> masterValues;

        public ComboBoxTableCellEditor(List<String> masterValues) {
            this.editor = new JComboBox();
            this.masterValues = masterValues;
        }

        @Override
        public Object getCellEditorValue() {
            return editor.getSelectedItem();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

            DefaultComboBoxModel model = new DefaultComboBoxModel(masterValues.toArray(new String[masterValues.size()]));
            for (int index = 0; index < table.getRowCount(); index++) {
                if (index != row) {
                    String cellValue = (String) table.getValueAt(index, 0);
                    model.removeElement(cellValue);
                }
            }

            editor.setModel(model);
            editor.setSelectedItem(value);

            return editor;

        }
    }