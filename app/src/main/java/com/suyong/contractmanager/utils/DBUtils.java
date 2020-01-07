package com.suyong.contractmanager.utils;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBUtils {
    private static String driver = "com.mysql.jdbc.Driver";//mysql驱动
    private static String url = "jdbc:mysql://49.234.92.110:3306/MyDatabases"+"?useUnicode=true&characterEncoding=UTF-8";//mysql数据库连接url
    private static String user = "root";
    private static String password = "19980713";

    public static Connection getConnection(){
        Connection conn = null;
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url,user,password);

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }
    //插入数据
    public static void insertStudent(String number,String name,int password) {

        Connection conn = getConnection();
        Statement st;
        st = null;
        try {
            st = conn.createStatement();
            String sql = "insert into student(number,name,password) values(?,?,?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, number);
            ps.setString(2, name);
            ps.setInt(3, password);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (st != null) {
                    st.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public static void close(Connection con, ResultSet rs, PreparedStatement ps) {
       try {
           if (con != null) {
               con.close();
           }
           if (rs != null) {
               rs.close();
           }
           if (ps != null) {
               ps.close();
           }
       }catch(Exception e){
           e.printStackTrace();
       }
    }
}
