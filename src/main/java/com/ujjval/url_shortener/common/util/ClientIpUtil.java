package com.ujjval.url_shortener.common.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class ClientIpUtil {
    public static String getClientIp() {
        try{
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if(attributes!=null){
                HttpServletRequest request = attributes.getRequest();
                String xfHeader = request.getHeader("X-Forwarded-For");
                if (xfHeader == null || xfHeader.isEmpty()) {
                    return request.getRemoteAddr();
                }
                return xfHeader.split(",")[0];
            }
        }catch (Exception e){

        }
        return "UNKNOWN_IP";
    }

    public static String getUserAgent() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if(attributes != null){
                HttpServletRequest request = attributes.getRequest();
                // Safely extract the User-Agent header
                String userAgent = request.getHeader(HttpHeaders.USER_AGENT);

                if (userAgent != null && !userAgent.isEmpty()) {
                    return userAgent;
                }
            }
        } catch (Exception e) {
            // Silently catch
        }
        return "UNKNOWN_USER_AGENT";
    }
}
