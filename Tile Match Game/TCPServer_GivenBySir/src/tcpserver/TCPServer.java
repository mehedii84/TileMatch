package tcpserver;

import java.net.*;
import java.io.*;
import javax.swing.JOptionPane;

public class TCPServer implements Runnable {

    private ChatServerThread clients[] = new ChatServerThread[2];
    private ServerSocket server = null;
    private Thread thread = null;
    private int clientCount = 0;

    //(Md.Mehedi Islam Khandaker . ID - 13.01.04.145)
    int n=6; // initially the number of the total row and column of the tiles is supposed to be 6
    int[][] mat = new int[n][n]; // we create matrices for the arrays
    StringBuilder msg = new StringBuilder(""); // a new string builder object is created

    public TCPServer(int port) {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                mat[i][j] = 0;
            }
        }
        try {
            System.out.println("Binding to port " + port + ", please wait  ...");
            server = new ServerSocket(port);
            System.out.println("Server started: " + server);
            start();
        } catch (IOException ioe) {
            System.out.println("Can not bind to port " + port + ": " + ioe.getMessage());
        }
    }

    @Override
    public void run() {
        while (thread != null) {
            try {
                System.out.println("Waiting for a client ...");
                addThread(server.accept());
            } catch (IOException ioe) {
                System.out.println("Server accept error: " + ioe);
                stop();
            }
        }
    }

    public void start() {
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    public void stop() {
        if (thread != null) {
            thread.stop();
            thread = null;
        }
    }

    private int findClient(int ID) { // finds the client by the id 0 or 1
        for (int i = 0; i < clientCount; i++) {
            if (clients[i].getID() == ID) { 
                return i;
            }
        }
        return -1;
    }

    public synchronized void handle(int ID, String input) {
        if (input.equals(".bye")) {
            clients[findClient(ID)].send(".bye");
            remove(ID);
        } else if (findClient(ID) == 0) {
            String[] rows = input.split(";");
            for (int i = 0; i < rows.length; i++) {
                String[] cols = rows[i].split(",");
                for (int j = 0; j < cols.length; j++) {
                    if (cols[j].equals("1")) {
                        mat[i][j] = 1; //
                    }

                    System.out.print(mat[i][j] + " ");

                }
                System.out.println("");
            }
            //(Md.Mehedi Islam Khandaker . ID - 13.01.04.145)
            String msgtosnd = mattostr(); //the correspondind matrix in the console is converted to string
            int next = findClient(ID) + 1;  // the next client is found
            if (next >= clientCount) {
                next = 0; // if the client exceeds 2 the client is initialized 0
            }
            clients[next].send(msgtosnd);
            if(rowchek1() || colchek1())
            {
                System.out.println("over");
                JOptionPane.showMessageDialog(null, "Player 0 wins"); // the flag is set true when a player wins by
                                                                      // rowcheck or columncheck
            }
            else if(rowchek2() || colchek2())
            {
                                System.out.println("over");
                JOptionPane.showMessageDialog(null, "Player 1 wins"); // the flag is set true when a player wins by
                                                                      // rowcheck or columncheck
            }
//            System.out.println(input);
        } 
        
        //(Md.Mehedi Islam Khandaker . ID - 13.01.04.145)
        
        else if (findClient(ID) == 1) {
            String[] rows = input.split(";");
            for (int i = 0; i < rows.length; i++) {
                String[] cols = rows[i].split(",");
                for (int j = 0; j < cols.length; j++) {
                    if (cols[j].equals("1")) {
                        mat[i][j] = 2;
                    }
                    System.out.print(mat[i][j] + " ");

                }
                System.out.println("");
            }
            String msgtosnd = mattostr(); //the correspondind matrix in the console is converted to string
            int next = findClient(ID) + 1; // the next client is found
            if (next >= clientCount) {
                next = 0; // if the client exceeds 2 the client is initialized 0
            }
            clients[next].send(msgtosnd);
            
            if(rowchek1() || colchek1())
            {
                JOptionPane.showMessageDialog(null, "Player 0 wins"); // the flag is set true when a player wins by
                                                                      // rowcheck or columncheck
            }
            else if(rowchek2() || colchek2())
            {
                JOptionPane.showMessageDialog(null, "Player 1 wins"); // the flag is set true when a player wins by
                                                                      // rowcheck or columncheck
            }
//            System.out.println(input);
        }

    }

    public synchronized void remove(int ID) {
        int pos = findClient(ID);
        if (pos >= 0) {
            ChatServerThread toTerminate = clients[pos];
            System.out.println("Removing client thread " + ID + " at " + pos);
            if (pos < clientCount - 1) {
                for (int i = pos + 1; i < clientCount; i++) {
                    clients[i - 1] = clients[i];
                }
            }
            clientCount--;
            try {
                toTerminate.close();
            } catch (IOException ioe) {
                System.out.println("Error closing thread: " + ioe);
            }
            toTerminate.stop();
        }
    }

    private void addThread(Socket socket) {
        if (clientCount < clients.length) {
            System.out.println("Client accepted: " + socket);
            clients[clientCount] = new ChatServerThread(this, socket);
            try {
                clients[clientCount].open();
                clients[clientCount].start();
                clients[clientCount].send("clientnumber:"+clientCount);
                clientCount++;
            } catch (IOException ioe) {
                System.out.println("Error opening thread: " + ioe);
            }
        } else {
            System.out.println("Client refused: maximum " + clients.length + " reached.");
        }
    }

    public String mattostr() {
        msg.delete(0, msg.length());
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                msg.append(mat[i][j]);
                if (j < n - 1) {
                    msg.append(",");
                }
            }
            msg.append(";");
        }
        return msg.toString();
    }
//(Md.Mehedi Islam Khandaker . ID - 13.01.04.145)
    public boolean rowchek1() {
        //since 4 tiles either horizontally or vertically are to be matched for each player 0 or 1 to win the game and
        //there are 6 rows and columns so to win either horizontally or vertically we can have 3 win patterns in a 
        //single row or column . Hence we use 3 if else conditions within the loop to check whether we have a 
        //winner in a row or column. Once we find a match er set the flag true
        int count = 0;
        boolean flag = false;
        for (int i = 0; i < n; i++) {
            if (mat[i][0] == 1 && mat[i][1] == 1 && mat[i][2] == 1 && mat[i][3] == 1) {
                flag = true;
                break;
            } else if (mat[i][1] == 1 && mat[i][2] == 1 && mat[i][3] == 1 && mat[i][4] == 1) {
                flag = true;
                break;
            } else if (mat[i][2] == 1 && mat[i][3] == 1 && mat[i][4] == 1 && mat[i][5] == 1) {
                flag = true;
                break;
            } 
        }
        return flag;
    }
    
//(Md.Mehedi Islam Khandaker . ID - 13.01.04.145)
        public boolean rowchek2() {
        //since 4 tiles either horizontally or vertically are to be matched for each player 0 or 1 to win the game and
        //there are 6 rows and columns so to win either horizontally or vertically we can have 3 win patterns in a 
        //single row or column . Hence we use 3 if else conditions within the loop to check whether we have a 
        //winner in a row or column. Once we find a match er set the flag true
        int count = 0;
        boolean flag = false;
        for (int i = 0; i < n; i++) {
            if (mat[i][0] == 2 && mat[i][1] == 2 && mat[i][2] == 2 && mat[i][3] == 2) {
                flag = true;
                break;
            } else if (mat[i][1] == 2 && mat[i][2] == 2 && mat[i][3] == 2 && mat[i][4] == 2) {
                flag = true;
                break;
            } else if (mat[i][2] == 2 && mat[i][3] == 2 && mat[i][4] == 2 && mat[i][5] == 2) {
                flag = true;
                break;
            } 
        }
        return flag;
    }
        
 //(Md.Mehedi Islam Khandaker . ID - 13.01.04.145)
        
                public boolean colchek2() {
        //since 4 tiles either horizontally or vertically are to be matched for each player 0 or 1 to win the game and
        //there are 6 rows and columns so to win either horizontally or vertically we can have 3 win patterns in a 
        //single row or column . Hence we use 3 if else conditions within the loop to check whether we have a 
        //winner in a row or column. Once we find a match er set the flag true
        int count = 0;
        boolean flag = false;
        for (int i = 0; i < n; i++) {
            if (mat[0][i] == 2 && mat[1][i] == 2 && mat[2][i] == 2 && mat[3][i] == 2) {
                flag = true;
                break;
            } else if (mat[1][i] == 2 && mat[2][i] == 2 && mat[3][i] == 2 && mat[4][i] == 2) {
                flag = true;
                break;
            } else if (mat[2][i] == 2 && mat[3][i] == 2 && mat[4][i] == 2 && mat[5][i] == 2) {
                flag = true;
                break;
            } 
        }
        return flag;
    }
                
 //(Md.Mehedi Islam Khandaker . ID - 13.01.04.145)
                  public boolean colchek1() {
                      
        //since 4 tiles either horizontally or vertically are to be matched for each player 0 or 1 to win the game and
        //there are 6 rows and columns so to win either horizontally or vertically we can have 3 win patterns in a 
        //single row or column . Hence we use 3 if else conditions within the loop to check whether we have a 
        //winner in a row or column. Once we find a match er set the flag true              
        int count = 0;
        boolean flag = false;
        for (int i = 0; i < n; i++) {
            if (mat[0][i] == 1 && mat[1][i] == 1 && mat[2][i] == 1 && mat[3][i] == 1) {
                flag = true;
                break;
            } else if (mat[1][i] == 1 && mat[2][i] == 1 && mat[3][i] == 1 && mat[4][i] == 1) {
                flag = true;
                break;
            } else if (mat[2][i] == 1 && mat[3][i] == 1 && mat[4][i] == 1 && mat[5][i] == 1) {
                flag = true;
                break;
            } 
        }
        return flag;
    }


    public static void main(String args[]) {
        TCPServer server = null;
        server = new TCPServer(2000);
    }
}
