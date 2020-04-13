package test;

public class NewClass { 
    public static void main(String[] args) 
    { 
        try
        {  
         // We are running "dir" and "ping" command on cmd 
         Runtime.getRuntime().exec("cmd /c start cmd.exe /K \"dir && ping localhost\""); 
        } 
        catch (Exception e) 
        { 
            System.out.println("HEY Buddy ! U r Doing Something Wrong "); 
            e.printStackTrace(); 
        } 
    } 
} 