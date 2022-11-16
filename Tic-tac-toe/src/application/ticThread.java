package application;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ticThread extends Thread{
    Socket play1;
    Socket play2;
    String str_board = "000000000";
    String LF = "\n";
    int step = 0;

    public ticThread(Socket p1, Socket p2){
        play1 = p1;
        play2 = p2;
    }

    @Override
    public void run() {
        super.run();
        Scanner sc;
        try {
            while (step <= 10){
                int mark = jdgWin();
                if (step == 10 || mark != 0){
                    System.out.println("END");
                    if (mark == 1){
                        send(play1, "win");
                        send(play2, "lose");
                    }else if (mark == 2){
                        send(play2, "win");
                        send(play1, "lose");
                    }else {
                        send(play1, "draw");
                        send(play2, "draw");
                    }
                    step+=10;
                }else if ((step % 2) == 1){
                    System.out.println("Going 1");
                    sc = new Scanner(play1.getInputStream());
                    if (!sc.hasNext()) break;
                    str_board = sc.next();
                    System.out.println(str_board + " 1");
                    StringBuilder sb = new StringBuilder();
                    sb.append(str_board).append(LF).append("id1").append(LF).append("Waiting");
                    send(play1, sb.toString());
                    sb = new StringBuilder();
                    sb.append(str_board).append(LF).append("id2").append(LF).append("YouGo");
                    send(play2, sb.toString());
                    step++;
                    System.out.println("Ending 1");
                }else {
                    System.out.println("Going 2");
                    if (step == 0){
                        sc = new Scanner(play1.getInputStream());
                        String temp = sc.next();
                    }
                    sc = new Scanner(play2.getInputStream());
                    if (!sc.hasNext()) break;
                    str_board = sc.next();
                    System.out.println(str_board + " 2");
                    StringBuilder sb = new StringBuilder();
                    sb.append(str_board).append(LF).append("id2").append(LF).append("Waiting");
                    send(play2, sb.toString());
                    sb = new StringBuilder();
                    sb.append(str_board).append(LF).append("id1").append(LF).append("YouGo");
                    send(play1, sb.toString());
                    step++;
                    System.out.println("Ending 2");
                }
            }
            Thread.sleep(1000);
            System.exit(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public int jdgWin() {
        char[] ca = str_board.toCharArray();
        int[][] c_bd = new int[3][3];
        int ct = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                c_bd[i][j] = ca[ct] - '0';
                ct++;
            }
        }
        if (ifWin(c_bd, 1)) return 1;
        if (ifWin(c_bd, 2)) return 2;
        return 0;
    }

    public boolean ifWin(int[][] board, int id){
        for (int i = 0; i < 3; i++) {
            if (id == board[i][0] && board[i][0] == board[i][1] && board[i][1] == board[i][2]) return true;
            if (id == board[0][i] && board[0][i] == board[1][i] && board[1][i] == board[2][i]) return true;
        }
        if (id == board[0][0] && board[0][0] == board[1][1] && board[1][1] == board[2][2]) return true;
        return id == board[2][0] && board[2][0] == board[1][1] && board[1][1] == board[0][2];
    }

    public void send(Socket sck, String msg){
        PrintWriter toward;
        try {
            toward = new PrintWriter(sck.getOutputStream());
            toward.println(msg);
            toward.flush();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
