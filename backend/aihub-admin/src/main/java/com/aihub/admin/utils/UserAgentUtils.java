package com.aihub.admin.utils;

/**
 * User-Agent解析工具类
 */
public class UserAgentUtils {
    
    /**
     * 解析操作系统
     */
    public static String parseOS(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "未知";
        }
        
        String ua = userAgent.toLowerCase();
        
        if (ua.contains("windows nt 10.0") || ua.contains("windows 10")) {
            return "Windows 10";
        } else if (ua.contains("windows nt 6.3") || ua.contains("windows 8.1")) {
            return "Windows 8.1";
        } else if (ua.contains("windows nt 6.2") || ua.contains("windows 8")) {
            return "Windows 8";
        } else if (ua.contains("windows nt 6.1") || ua.contains("windows 7")) {
            return "Windows 7";
        } else if (ua.contains("windows nt 6.0")) {
            return "Windows Vista";
        } else if (ua.contains("windows nt 5.1") || ua.contains("windows xp")) {
            return "Windows XP";
        } else if (ua.contains("mac os x") || ua.contains("macintosh")) {
            if (ua.contains("mac os x 10_15") || ua.contains("mac os x 10.15")) {
                return "Mac OS >=10.15.7";
            } else if (ua.contains("mac os x 10_14") || ua.contains("mac os x 10.14")) {
                return "Mac OS 10.14";
            } else if (ua.contains("mac os x 10_13") || ua.contains("mac os x 10.13")) {
                return "Mac OS 10.13";
            } else {
                return "Mac OS";
            }
        } else if (ua.contains("linux")) {
            return "Linux";
        } else if (ua.contains("android")) {
            return "Android";
        } else if (ua.contains("iphone") || ua.contains("ipad") || ua.contains("ipod")) {
            return "iOS";
        }
        
        return "未知";
    }
    
    /**
     * 解析浏览器
     */
    public static String parseBrowser(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "未知";
        }
        
        String ua = userAgent.toLowerCase();
        
        if (ua.contains("edg/")) {
            return "Edge " + extractVersion(ua, "edg/");
        } else if (ua.contains("chrome/") && !ua.contains("edg/")) {
            return "Chrome " + extractVersion(ua, "chrome/");
        } else if (ua.contains("safari/") && !ua.contains("chrome/")) {
            return "Safari " + extractVersion(ua, "safari/");
        } else if (ua.contains("firefox/")) {
            return "Firefox " + extractVersion(ua, "firefox/");
        } else if (ua.contains("msie") || ua.contains("trident/")) {
            return "IE " + extractVersion(ua, "msie");
        } else if (ua.contains("qqbrowser/")) {
            return "QQ Browser " + extractVersion(ua, "qqbrowser/");
        } else if (ua.contains("micromessenger")) {
            return "微信浏览器";
        }
        
        return "未知";
    }
    
    /**
     * 提取版本号
     */
    private static String extractVersion(String userAgent, String key) {
        int index = userAgent.indexOf(key);
        if (index == -1) {
            return "";
        }
        
        int start = index + key.length();
        int end = start;
        while (end < userAgent.length() && 
               (Character.isDigit(userAgent.charAt(end)) || userAgent.charAt(end) == '.')) {
            end++;
        }
        
        if (end > start) {
            return userAgent.substring(start, end);
        }
        
        return "";
    }
}
