package config;

import java.awt.Component;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
 
public class render extends JComboBox implements TableCellRenderer{
         public render(){
                   super();
                   addItem("男");
                   addItem("女");
         }
         public Component getTableCellRendererComponent(JTable table, Object value,
                            boolean isSelected, boolean hasFocus, int row, int column) {
                   if(isSelected){
                            setForeground(table.getForeground());
                            super.setBackground(table.getBackground());
                   }else{
                            setForeground(table.getForeground());
                            setBackground(table.getBackground());
                   }
                   boolean isMale = ((Boolean)value).booleanValue();
                   setSelectedIndex(isMale? 0 : 1);
                   return this;
         }
 
} 