package lanjutan;

import java.sql.*;
import java.util.*;
import java.io.*;
import java.net.*;

/**
 * JdbcInsert1.java - Demonstrates how to INSERT data into an SQL
 *                    database using Java JDBC.
 */
class JdbcInsert1 { 
  
    public static void main (String[] args) 
    { 
        try 
        {
            Scanner sc = new Scanner(System.in); 
            System.out.println("Enter your name: ");
            String username = sc.next(); 
//          System.out.println("you entered : "+no);
      
            System.out.println("Enter your Password: ");
            String pass = sc.next();
            String url = "jdbc:mysql://localhost/progjardb"; 
            Connection conn = DriverManager.getConnection(url,"root",""); 
            Statement st = conn.createStatement(); 
            st.executeUpdate("INSERT INTO admin " + "VALUES ('"+username+"','"+pass+"')");
            
            
            System.out.println("Success Insert New User");
            conn.close(); 
        }
        catch (Exception e) { 
            System.err.println("Got an exception! "); 
            System.err.println(e.getMessage()); 
        } 
  
    }
} 