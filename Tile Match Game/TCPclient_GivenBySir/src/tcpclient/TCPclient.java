/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tcpclient;


import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;

public class TCPclient  implements Runnable 
{  private Socket socket              = null;
   private Thread thread              = null;
   private DataInputStream  console   = null;
   private DataOutputStream streamOut = null;
   private ChatClientThread client    = null;
   
    JFrame window=new JFrame(); // we open a new Jframe window
    int n=6; // initially the number of the total row and column of the tiles is supposed to be 6
    JButton[][] btn=new JButton[n][n]; //we take button arrays 
    int[][] mat=new int[n][n]; // we create matrices for the arrays
    StringBuilder msg=new StringBuilder(""); // a new string builder object is created

   public TCPclient(String serverName, int serverPort)
   {  
       init();
       System.out.println("Establishing connection. Please wait ...");
      try
      {  socket = new Socket(serverName, serverPort);
         System.out.println("Connected: " + socket);
         start();
      }
      catch(UnknownHostException uhe)
      {  System.out.println("Host unknown: " + uhe.getMessage()); }
      catch(IOException ioe)
      {  System.out.println("Unexpected exception: " + ioe.getMessage()); }
   }
   public void run()
   {  while (thread != null)
      {  try
         {  streamOut.writeUTF(console.readLine());
            streamOut.flush();
         }
         catch(IOException ioe)
         {  System.out.println("Sending error: " + ioe.getMessage());
            stop();
         }
      }
   }
   public void handle(String msg)
   {  if (msg.equals(".bye"))
      {  System.out.println("Good bye. Press RETURN to exit ...");
         stop();
      }else if(msg.startsWith("clientnumber"))
      {
          String[] parse=msg.split(":");
          window.setTitle(parse[1]);
      }
      else
   {
       String[] rows = msg.split(";"); //we split the rows by ; of the 1 and 0 matrix formed in the console at any point of the game
            for (int i = 0; i < rows.length; i++) {
                String[] cols = rows[i].split(","); //we split the columns by , of the 1 and 0 matrix formed in the console at any point of the game
                for (int j = 0; j < cols.length; j++) {
                    if (cols[j].equals("1")) {
                        btn[i][j].setBackground(Color.red); // when the value of a column in the matrix is 1 we color the button red
                        
                    }
                    else if(cols[j].equals("2"))
                    {
                        btn[i][j].setBackground(Color.green); // when the value of a column in the matrix is 2 we color the button green
                    }

                    System.out.print(mat[i][j] + " ");
                    window.setEnabled(true);

                }
                System.out.println("");
            }
   }
         System.out.println(msg);
   }
   public void start() throws IOException
   {  console   = new DataInputStream(System.in);
      streamOut = new DataOutputStream(socket.getOutputStream());
      if (thread == null)
      {  client = new ChatClientThread(this, socket);
         thread = new Thread(this);                   
         thread.start();
      }
   }
   public void stop()
   {  if (thread != null)
      {  thread.stop();  
         thread = null;
      }
      try
      {  if (console   != null)  console.close();
         if (streamOut != null)  streamOut.close();
         if (socket    != null)  socket.close();
      }
      catch(IOException ioe)
      {  System.out.println("Error closing ..."); }
      client.close();  
      client.stop();
   }
   public void init()
   {
       window.setLayout(null);
       window.setSize(360,300); //JFrame window size
       window.setDefaultCloseOperation(window.EXIT_ON_CLOSE);
       
       int xpos=10,ypos=10; // we take initial pixel values for arranging the buttons
       for(int i=0;i<n;i++)
       {
           for(int j=0;j<n;j++)
           {
               final int a=i,b=j;
               mat[i][j]=0;
               btn[i][j]=new JButton();
               btn[i][j].setBounds(xpos,ypos,50,30); 
               btn[i][j].addActionListener(new ActionListener() {
                   @Override
                   public void actionPerformed(ActionEvent e) {
                       try {
                           if(window.getTitle().equals("0"))
                           {
                               btn[a][b].setBackground(Color.red); // player 0 is assigned the red color button 
                           }
                           else 
                               btn[a][b].setBackground(Color.green); // player 1 is assigned the green color button
                           mat[a][b]=1;
                           String msgtosnd=mattostr(); ///the correspondind matrix in the console is converted to string
                           streamOut.writeUTF(msgtosnd); // the matrix is sent
                           streamOut.flush();
                           btn[a][b].setEnabled(false); // once a player clicks a button the button is disabled
                           window.setEnabled(false); // once a player clicks a button the window is disabled
                       } catch (IOException ex) {
                           Logger.getLogger(TCPclient.class.getName()).log(Level.SEVERE, null, ex);
                       }
                   }
               });
               window.add(btn[i][j]);
               xpos+=55; // increment the position of the next tile by 5 along x axis
           }
           ypos+=35; // increment the position of the next tile by 5 along y axis
           xpos=10;
       }
       window.show();
   }
   
   public String mattostr()
   {
       msg.delete(0, msg.length());
       for(int i=0;i<n;i++)
       {
           for(int j=0;j<n;j++)
           {
               msg.append(mat[i][j]);
               if(j<n-1)
                   msg.append(",");
           }
           msg.append(";");
       }
       return msg.toString();
   }
   
   public static void main(String args[])
   {  TCPclient client = null;
         client = new TCPclient ("localhost", 2000);
   }
}