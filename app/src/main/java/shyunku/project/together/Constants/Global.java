package shyunku.project.together.Constants;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Global {
    public static final int CONSOLE_DEBUG_MODE = 0;
    public static String version = "1.2.25";

    //functions
    public static String getOppKey(){
        if(CONSOLE_DEBUG_MODE == 1)
            return DEVICE_KEY[2]; //조영훈 -> 조재훈
        return DEVICE_KEY[1]; //조재훈 -> 조영훈
    }

    public static String getOwner(){
        return userList[CONSOLE_DEBUG_MODE];
    }

    public static String getOpper(){
        return userList[1-CONSOLE_DEBUG_MODE];
    }

    //never changes
    public static final String[] userList = {
            "조재훈", "조영훈"
    };
    public static final String rootName = "party-01482";
    public static final SimpleDateFormat sdf = new SimpleDateFormat("M월 d일 a h:mm", Locale.KOREA);
    public static final String FMC_SERVER_KEY = "AAAARb8XDHU:APA91bFj6ysDKxywfmeQDRL4kMPAZj2jgWAGlKtjL7cpXkRhpiyjaWPo2ENO_8sdK8KajOFCoYFh7quvmu2q6KF9BqN4Irf_j1ihEPts51cGOzFVf0kJfkf0FtVOjPcQ6XYjIbLz9PQS";
    public static final String[] DEVICE_KEY = {
            "erWHbNMNDtI:APA91bEm6zOSRZf45zP63d8emb_gl-BMTvxSkaYSl-tHKsBwNqwrb8TxGVjbjVcP7h__J991e8zi5Cn2MCG94hZSa2W4UlntAeq26rTLb5KSvLPCqE3m3SM_QVit1u33iH1d4J85hzxH",
            "eLeC5YbmWgQ:APA91bG8_ADoIvTj2HXu40iNkHq83XC_O9XcBIUmjTQDEJukSSqSHGGU06ytaiQAm4OuAKihr5eAqPOLWc55NGBNiRh5XrlRqppSkGtMis0Q3tKJ8NdwemvkN_mg0ixrvzdG9q-eYDkC",
            "c2ozCHqixb0:APA91bFQsvm07VuuV8Ca-u_qOaT7hm3JACfaV0lZ-cjgelSYa59xsIq7uZli_oMdTChYXIfwQh3ey4RqeSNpfxYh0b1vcr4qWTH80dQzGR6WfBp6N1gs2VPRP_r_tyGkpQJSUqcS0wKA"
    };
}

// 조영훈 : erWHbNMNDtI:APA91bEm6zOSRZf45zP63d8emb_gl-BMTvxSkaYSl-tHKsBwNqwrb8TxGVjbjVcP7h__J991e8zi5Cn2MCG94hZSa2W4UlntAeq26rTLb5KSvLPCqE3m3SM_QVit1u33iH1d4J85hzxH
// 조재훈 : eFcVFb6v9qc:APA91bGJcNSU1x2H3zbcM3XmcO4voGdv3VCdTvvh1DEITpZ3T7LGIi1RJCk0Xizwxd0oDmd-oghZGCqy53uoOQ9xy1zKlQMJr8YvtTt2f11p4T-EC09FkPDFuxHFXx89cwp_scCxQBsG