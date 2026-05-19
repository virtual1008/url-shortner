package com.ujjval.url_shortener.common.util;

public class Base62Encoder {
    private static final String BASE62 =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final int BASE = BASE62.length();

    public static String encode(long value){
        if(value==0){
            return String.valueOf(BASE62.charAt(0));
        }
        StringBuilder sb = new StringBuilder();

        while (value>0){
            int remainder = (int) (value%BASE);
            sb.append(BASE62.charAt(remainder));
            value = value/BASE;
        }
        return sb.reverse().toString();
    }

}
