package cqu.edu.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by fxy on 2016/9/15.
 */

//工具类，检测用户登录注册时输入手机号是否合法
public class checkInputType {
    public static String checkInputType(String input){
        //电话号码的正则表达式
        //  String teleType="^[1]([3][0-9]{1}|59|58|88|89)[0-9]{8}$";
        String teleType="[1][358]\\d{9}";

        Pattern teleRegex= Pattern.compile(teleType);
        Matcher teleMatcher=teleRegex.matcher(input);

        if(teleMatcher.matches()){
            return "valid";
        }else {
            return "请输入有效的电话号码！";
        }
    }
}
