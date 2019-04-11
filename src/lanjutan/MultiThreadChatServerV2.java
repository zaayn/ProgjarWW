/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lanjutan;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.Scanner;

/**
 *
 * @author ayya
 */
public class MultiThreadChatServerV2 
{

  // The server socket.
  private static ServerSocket serverSocket = null;
  // The client socket.
  private static Socket clientSocket = null;
  // This chat server can accept up to maxClientsCount clients' connections.
  private static final int maxClientsCount = 10;
  //Array untuk menyimpang sekumpulan running thread
  private static final clientThread[] ArrayRunningSocket = new clientThread[maxClientsCount];

  public static void main(String args[]) 
  {
    // The default port number.
    int portNumber = 2222;
    if (args.length < 1) 
    {
      System.out.println("Usage: java MultiThreadChatServer <portNumber>\n" + "Now using port number=" + portNumber);
    } 
    else 
    {
      portNumber = Integer.valueOf(args[0]).intValue();
    }

    /*
     * Open a server socket on the portNumber (default 2222). Note that we can
     * not choose a port less than 1023 if we are not privileged users (root).
     */
    try {
      serverSocket = new ServerSocket(portNumber);
    } catch (IOException e) {
      System.out.println(e);
    }

    /*
     * Create a client socket for each connection and pass it to a new client
     * thread.
     */
    while (true) {
      try {
            // blocking menunggu ada client konek
             clientSocket = serverSocket.accept();
            int i = 0;
            for (i = 0; i < maxClientsCount; i++) {
                if (ArrayRunningSocket[i] == null) {
                   ArrayRunningSocket[i] = new clientThread(clientSocket, ArrayRunningSocket);
                   ArrayRunningSocket[i].start();
                   break;
                 //  (ArrayRunningSocket[i] = new clientThread(clientSocket, ArrayRunningSocket)).start();
                 }
             }

        // jika array sudah penuh kirim pesan bahwa jumlah client sudah maksimal
        if (i == maxClientsCount) {
          PrintStream os = new PrintStream(clientSocket.getOutputStream());
          os.println("Server too busy. Try later.");
          os.close();
          clientSocket.close();
        }
      } catch (IOException e) {
        System.out.println(e);
      }
    }
  }
}
/*
 * The chat client thread. This client thread opens the input and the output
 * streams for a particular client, ask the client's name, informs all the
 * clients connected to the server about the fact that a new client has joined
 * the chat room, and as long as it receive data, echos that data back to all
 * other clients. When a client leaves the chat room this thread informs also
 * all the clients about that and terminates.
 */

class clientThread extends Thread 
{

  private DataInputStream is = null;
  private PrintStream os = null;
  private Socket clientSocket = null;
  private final clientThread[] threads;
  private int maxClientsCount;
  private String nama;
  private String pass;
  private String key;

  public static Connection getConnection() throws Exception
  {
    try 
    {
        String url = "jdbc:mysql://localhost/progjardb"; 
        Connection conn = DriverManager.getConnection(url,"root",""); 
        System.out.println("");
        return conn;
    }
    catch (Exception e) 
    { 
        System.err.println("Got an exception! "); 
        System.err.println(e.getMessage()); 
        return null;
    }         
 }
 
  
  public static boolean CheckUser(String Username,String Password)
  {
      try
      {
          Connection conn = getConnection();
          Statement st = conn.createStatement();
          ResultSet rs = st.executeQuery("SELECT * from admin where username='"+Username+"'AND Password='"+Password+"';");
          if (!rs.next())
          {
              return false;
          }
          else
          {
              return true;
          }
      }
      catch(Exception e)
      {
          System.err.println("cek gagal"+e.getMessage());
          e.printStackTrace();
          return false;
      }
  }
  
  public clientThread(Socket clientSocket, clientThread[] threads) 
  {
    this.clientSocket = clientSocket;
    this.threads = threads;
    maxClientsCount = threads.length;
  }
  
  public void run() {
      
    
    int maxClientsCount = this.maxClientsCount;
    clientThread[] threads = this.threads;
    try {
     
//       * Create input and output streams for this client.
      is = new DataInputStream(clientSocket.getInputStream());
      os = new PrintStream(clientSocket.getOutputStream());
      os.println("Masukkan namamu .");
      String name = is.readLine().trim();
      this.nama= name;
      
      os.println("masukkan password");
      String password = is.readLine().trim();
      this.pass= password;
      
     if(!CheckUser(name,password))
     {
         os.println("Searching data ....");
         os.println("Soryy Data Not Found");
         try { 
             os.println("Lanjut ke Register ? press Y to Continue , N to Close");
             String key = is.readLine().trim();
             this.nama= key;
             
             if(key.equals("Y") || key.equals("y"))
             {
                os.println("try to Insert New Data");
                String url = "jdbc:mysql://localhost/progjardb"; 
                Connection conn = DriverManager.getConnection(url,"root",""); 
                Statement st = conn.createStatement(); 
                st.executeUpdate("INSERT INTO admin " + "VALUES ('"+name+"','"+password+"')");

                os.println("Success Insert New User");
                conn.close(); 
             }
             else 
             {
                clientSocket.close();
             }
             
        } 
        catch (Exception e) { 
            System.err.println("Got an exception! "); 
            System.err.println(e.getMessage()); 
        }
     }
      
      for (int i = 0; i < maxClientsCount; i++) 
      {
          
        if( threads[i]!= null && threads[i].getName().equals(name))
        {
            threads[i].os.println("Anda telah login di Device lain"); 
            threads[i].clientSocket.close();
            threads[i]=null;
        } 
      }
      this.setName(name);
      
      
      
      
      
      os.println("Hello " + name + " to our chat room. enter /quit for exit room");
      for (int i = 0; i < maxClientsCount; i++) 
      { 
        if (threads[i] != null && threads[i] != this) 
        {
          threads[i].os.println("*** A new user " + name + " joined the chat room !!! ***");
        } 
      }
      while (true) 
      {
        String line = is.readLine();
        if (line.startsWith("/quit")) {
          break;
        }
        
        //########## private chat ##########
        else if (line.contains("@"))
        {
            String[] txt = line.split("@");
            for (int i = 0; i < maxClientsCount; i++)
            {
                if(threads[i] != null && threads[i].getName().equals(txt[0]))
                {
                    threads[i].os.println("<private from " + name + " --> " +txt[1]);
                    os.println("<private to " +threads[i].nama + " --> " +txt[1]);
                }
            }
        }
        
        //########## kick someone ##########
        else if (line.contains("%"))
        {
            String[] txt = line.split("%");
            for (int i = 0; i < maxClientsCount; i++)
            {
                if(threads[i] != null && threads[i].getName().equals(txt[0]))
                {
                    threads[i].os.print("kamu telah disconnect");
                    threads[i].clientSocket.close();
                    threads[i]=null;
                }
            }
        }
        
        // menampilkan orang online
        else if (line.equals("##"))
        {
            for (int i = 0; i < maxClientsCount; i++)
            {
                if (threads[i] != null) 
                {
                    os.println(threads[i].getName());
                }
            }
        }
        
        else
        {
            for (int i = 0; i < maxClientsCount; i++) 
            {
                if (threads[i] != null) 
                {
                    threads[i].os.println("<" + name + "--> " + line);
                }
            }
        }
 
      }
      for (int i = 0; i < maxClientsCount; i++) 
      {
        if (threads[i] != null && threads[i] != this) 
        {
            threads[i].os.println("*** The user " + name+ " is leaving the chat room !!! ***");
        }
      }
      os.println("*** Bye " + name + " ***");

      /*
       * Clean up. Set the current thread variable to null so that a new client
       * could be accepted by the server.
       */
      for (int i = 0; i < maxClientsCount; i++) 
      {
        if (threads[i] == this) 
        {
          threads[i] = null;
        }
      }

      /*
       * Close the output stream, close the input stream, close the socket.
       */
      is.close();
      os.close();
      clientSocket.close();
    } 
    catch (IOException e) {
    }
  }
}

