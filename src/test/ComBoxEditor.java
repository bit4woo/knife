package test;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

class ComBoxEditor extends AbstractCellEditor implements TableCellEditor
{
	/*
	*	ReadMe: 这个 ComboBox下拉列表的编辑器 使用一个 JLable 和一个 JComboBox组合的
	*	将JComboBox放到JLable里，所以只需要将 JLable 作为编辑器组件返回就行了
	*/
	private JComboBox m_ComboBox;
	//获取 下拉列表的 选择的值
	private String m_SelStr;
	private JLabel m_OutLable;
	//这里我们设置 鼠标点击 1 次就响应编辑器
	private static final int clickCountToStart = 1;
	//初始化编辑器包含的控件信息
	public ComBoxEditor()
	{
		m_ComboBox = new JComboBox();
		m_ComboBox.addItem("选项A");
		m_ComboBox.addItem("选项B");
		m_ComboBox.addItem("选项C");
		
		m_ComboBox.setSize(100,30);
		
		m_OutLable= new JLabel();
		m_OutLable.setLayout(null);
		m_OutLable.setBounds(0, 0, 120, 40);
		m_OutLable.add(m_ComboBox);
		m_ComboBox.setLocation(50, 50);
		
		//响应下拉列表的事件
		m_ComboBox.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				System.out.println("下拉列表的选中事件");
				if(e.getStateChange() == e.SELECTED)
				{
					//获取选择的值
					m_SelStr = (String)m_ComboBox.getSelectedItem();
					//结束选择
					fireEditingStopped();
				}
			}
		});
	}
	//检测鼠标的点击次数，判断编辑器是否起作用
	public boolean isCellEditable(EventObject anEvent) 
    {
		//如果事件 是 鼠标的事件，大于设定的次数就true,否则false
	    if (anEvent instanceof MouseEvent) 
	    {
			System.out.println("检测鼠标的点击次数，设置编辑器是否响应");
			return ((MouseEvent)anEvent).getClickCount() >= clickCountToStart;
		}
	    return false;
    }
 
	//获取编辑器的组件
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column)
	{
		System.out.println("获取编辑器的组件");
		//将下拉列表设置为之前的选项
		m_SelStr = (String)value;
		m_ComboBox.setSelectedItem(m_SelStr);
		//返回值为 null的时候 是空的编辑器，就是说 = =不允许 编辑的
		return m_OutLable;
	}
	//获取编辑器的 值
	@Override
	public Object getCellEditorValue()
	{return m_SelStr;}	
}
