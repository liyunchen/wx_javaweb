package com.xingshang.servlet;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xingshang.util.AuthUtil;

/**
 * 入口地址
 * @author Administrator
 *
 */
@WebServlet("/wxLogin")
public class LoginServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        //第一步：引导用户进入授权页面同意授权，获取code
        
        //回调地址
//        String backUrl = "http://suliu.free.ngrok.cc/WxAuth/callBack";    //第1种情况使用
        String backUrl = "http://aaa.chenlove.cn/WxAuth/wxCallBack";//第2种情况使用，这里是web.xml中的路径
        
        //授权页面地址
        String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+AuthUtil.APPID
                + "&redirect_uri="+URLEncoder.encode(backUrl)
                + "&response_type=code"
                + "&scope=snsapi_userinfo"
                + "&state=STATE#wechat_redirect";
        
        //重定向到授权页面
        response.sendRedirect(url);
    }
}