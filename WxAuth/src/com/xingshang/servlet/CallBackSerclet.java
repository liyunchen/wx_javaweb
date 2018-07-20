package com.xingshang.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xingshang.util.AuthUtil;

import net.sf.json.JSONObject;

/**
 * 回调地址
 * @author Administrator
 *
 */
//@WebServlet("/callBack")
public class CallBackSerclet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private String dbUrl;
    private String driverClassName;
    private String userName;
    private String passWord;
    
    private Connection conn =null;
    private PreparedStatement ps =null;
    private ResultSet rs = null;
    
    //初始化数据库
    @Override
    public void init(ServletConfig config) throws ServletException {
        
        //加载驱动
        try {
            this.dbUrl = config.getInitParameter("dbUrl");
            this.driverClassName = config.getInitParameter("driverClassName");
            this.userName = config.getInitParameter("userName");
            this.passWord = config.getInitParameter("passWord");
            Class.forName(driverClassName);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        //第二步：通过code换取网页授权access_token
        
        //从request里面获取code参数(当微信服务器访问回调地址的时候，会把code参数传递过来)
        String code = request.getParameter("code");
        
        System.out.println("code:"+code);
        
        //获取code后，请求以下链接获取access_token
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + AuthUtil.APPID
                + "&secret=" + AuthUtil.APPSECRET
                + "&code=" + code
                + "&grant_type=authorization_code";
        
        //通过网络请求方法来请求上面这个接口
        JSONObject jsonObject = AuthUtil.doGetJson(url);
        
        System.out.println("==========================jsonObject"+jsonObject);
        
        //从返回的JSON数据中取出access_token和openid，拉取用户信息时用
        String token =  jsonObject.getString("access_token");
        String openid = jsonObject.getString("openid");
        
        // 第三步：刷新access_token（如果需要）

        // 第四步：拉取用户信息(需scope为 snsapi_userinfo)
        String infoUrl ="https://api.weixin.qq.com/sns/userinfo?access_token=" + token
                + "&openid=" + openid
                + "&lang=zh_CN";
        //通过网络请求方法来请求上面这个接口
        JSONObject userInfo = AuthUtil.doGetJson(infoUrl);
        
        System.out.println(userInfo);
        
        
        
        //第1种情况：使用微信用户信息直接登录，无需注册和绑定
//        request.setAttribute("info", userInfo);
        //直接跳转
//        request.getRequestDispatcher("/index1.jsp").forward(request, response);
        
        
        //第2种情况： 将微信与当前系统的账号进行绑定(需将第1种情况和@WebServlet("/callBack")注释掉)
        //第一步，根据当前openid查询数据库，看是否该账号已经进行绑定
        try {
            String nickname = getNickName(openid);
            if(!"".equals(nickname)){
                //已绑定
                request.setAttribute("nickname", nickname);
                request.getRequestDispatcher("/index2.jsp").forward(request, response);
            }else{
                //未绑定
                request.setAttribute("openid", openid);
                request.getRequestDispatcher("/login.jsp").forward(request, response);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

    //数据库的查询
    public String getNickName(String openid) throws SQLException{
        String nickName = "";
        //创建数据库链接
        conn = DriverManager.getConnection(dbUrl, userName, passWord);
        String sql = "select nickname from user where openid = ?";
        ps = conn.prepareStatement(sql);
        ps.setString(1, openid);
        rs = ps.executeQuery();
        while (rs.next()) {
            nickName = rs.getString("nickname");
        }
        
        //关闭链接
        rs.close();
        ps.close();
        conn.close();
        
        return nickName;
    }
    
    //数据库的修改(openid的綁定)
    public int updateUser(String account,String password,String openid) throws SQLException{
        
        //创建数据库链接
        conn = DriverManager.getConnection(dbUrl, userName, passWord);
        String sql = "update user set openid = ? where account = ? and password = ?";
        ps = conn.prepareStatement(sql);
        ps.setString(1, openid);
        ps.setString(2, account);
        ps.setString(3, password);
        int temp = ps.executeUpdate();
        
        //关闭链接
        rs.close();
        ps.close();
        conn.close();
        
        return temp;
    }
    
    //post方法，用来接受登录请求
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	request.setCharacterEncoding("utf-8");
        String account = request.getParameter("account");
        String password = request.getParameter("password");
        String openid = request.getParameter("openid");
        
       
        try {
            int temp = updateUser(account, password, openid); 
            request.setAttribute("temp", temp);
            request.setAttribute("account", account);
            request.setAttribute("password", password);
            request.setAttribute("openid", openid);
            if(temp > 0){
                String nickname = getNickName(openid);
                request.setAttribute("nickname", nickname);
                request.getRequestDispatcher("/index2.jsp").forward(request, response);
                System.out.println("账号绑定成功");
            }else{
            	request.getRequestDispatcher("/error.jsp").forward(request, response);
            }
            
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    
}