package com.example.utils;

import java.util.*;

import com.alibaba.fastjson.JSON;
import org.joda.time.DateTime;
import org.springframework.util.CollectionUtils;

public class Test {
    public static void main(String[] args) {
        int dayValue = 1;
        Date date = new DateTime(new Date()).plusDays(dayValue + 1).withTimeAtStartOfDay().minusMillis(2000).toDate();
        System.out.println(date);

        List<String> phoneList = new ArrayList<>();
//        phoneList.add("111");
//        phoneList.add("222");
//        phoneList.add("333");

        if (!CollectionUtils.isEmpty(phoneList)) {
            Random random = new Random();
            String phone = phoneList.get(random.nextInt(phoneList.size()));
            System.out.println(phone);
        }

         Map<String, List<String>> businessVirtualLoginDefaultPhone=new HashMap<>();
        businessVirtualLoginDefaultPhone.put("84258", Arrays.asList("18730781100"));
        businessVirtualLoginDefaultPhone.put("99999", Arrays.asList("15810386763"));
        System.out.println(JSON.toJSONString(businessVirtualLoginDefaultPhone));
    }
}
