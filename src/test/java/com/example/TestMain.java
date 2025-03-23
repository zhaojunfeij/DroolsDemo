package com.example;

import java.util.HashMap;
import java.util.Map;

public class TestMain {
    public static void main(String[] args) {
        Object flag=true;
        boolean flag1=(boolean) flag;
        Map map=new HashMap<>();
        map.put("flag",flag1);
        if ((boolean)map.get("flag")){
            System.out.println("..........");
        }
    }
}
