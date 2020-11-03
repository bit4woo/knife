package test;

import java.util.HashSet;
import java.util.Set;

public class SetTest {
    public static void main(String[] args) {
        Set<String> tmp = new HashSet<>();
        System.out.println(tmp);
        System.out.println(tmp.size());
        for(String item:tmp){
            System.out.println(item+"111");
        }
    }
}
