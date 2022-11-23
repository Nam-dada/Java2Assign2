package application.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class Controller implements Initializable {

  private static final int PLAY_1 = 1;
  private static final int PLAY_2 = 2;
  private static final int EMPTY = 0;
  private static final int BOUND = 90;
  private static final int OFFSET = 15;
  private static int id = 1;

  @FXML
  private Text text = new Text();

  @FXML
  private Pane base_square;

  @FXML
  private Rectangle game_panel;

  private static boolean TURN = false;

  private static final int[][] chessBoard = new int[3][3];
  public String str_board = "000000000";
  private static final boolean[][] flag = new boolean[3][3];

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    Socket socket = null;
    Scanner res;
    PrintWriter req;
    text.setText("Hello, matching new players");
    try {
      socket = new Socket("127.0.0.1", 8848);
    } catch (Exception e) {
      System.out.println("Connect failed, please retry.");
      System.exit(0);
    }
    try {
      res = new Scanner(socket.getInputStream());
      req = new PrintWriter(socket.getOutputStream());

      Timer ti = new Timer();
      TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
          Platform.runLater(() -> {
            refreshAll(str_board);
          });
        }
      };
      ti.schedule(timerTask, 100, 100);

      new Thread(() -> {
        while (res.hasNext()) {
          String temp = res.next();
          System.out.println(temp);
          if (temp.matches("^id[1-2]$")) {
            temp = temp.replace("id", "");
            id = Integer.parseInt(temp);
          } else if (temp.equals("win")) {
            text.setText(temp);
            TURN = false;
            System.out.println("You win");
          } else if (temp.equals("lose")) {
            text.setText(temp);
            TURN = false;
            System.out.println("You lose");
          } else if (temp.equals("draw")) {
            text.setText(temp);
            TURN = false;
            System.out.println("Draw!");
          } else if (temp.matches("^[0-2]+$")) {
            str_board = temp;
          } else {
            System.out.println("");
            text.setText(temp);
            TURN = temp.length() != 7;
          }
        }
        if (jdgWin() == 0) {
          System.out.println("Server disconnect");
        }
        System.exit(0);
      }).start();
      req.println(str_board);
      req.flush();

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    game_panel.setOnMouseClicked(event -> {
      int x = (int) (event.getX() / BOUND);
      int y = (int) (event.getY() / BOUND);
      if (refreshBoard(x, y)) {
        TURN = !TURN;
        req.println(str_board);
        req.flush();
      }
    });
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
    if (ifWin(c_bd, 1)) {
      return 1;
    }
    if (ifWin(c_bd, 2)) {
      return 2;
    }
    return 0;
  }

  public boolean ifWin(int[][] board, int id) {
    for (int i = 0; i < 3; i++) {
      if (id == board[i][0] && board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
        return true;
      }
      if (id == board[0][i] && board[0][i] == board[1][i] && board[1][i] == board[2][i]) {
        return true;
      }
    }
    if (id == board[0][0] && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
      return true;
    }
    return id == board[2][0] && board[2][0] == board[1][1] && board[1][1] == board[0][2];
  }

  private void refreshAll(String str_brd) {
    char[] bd = str_brd.toCharArray();
    int idx = 0;
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        chessBoard[i][j] = bd[idx] - '0';
        idx++;
      }
    }
    drawChess();
  }

  private boolean refreshBoard(int x, int y) {
    if (chessBoard[x][y] == EMPTY && TURN) {
      chessBoard[x][y] = id;
      StringBuilder sb = new StringBuilder(str_board);
      int idx = x * 3 + y;
      sb.replace(idx, idx + 1, String.valueOf(id));
      str_board = sb.toString();
      drawChess();
      return true;
    }
    return false;
  }

  private void drawChess() {
    for (int i = 0; i < chessBoard.length; i++) {
      for (int j = 0; j < chessBoard[0].length; j++) {
        if (flag[i][j]) {
          // This square has been drawing, ignore.
          continue;
        }
        switch (chessBoard[i][j]) {
          case PLAY_1:
            drawCircle(i, j);
            break;
          case PLAY_2:
            drawLine(i, j);
            break;
          case EMPTY:
            // do nothing
            break;
          default:
            System.err.println("Invalid value!");
        }
      }
    }
  }

  private void drawCircle(int i, int j) {
    Circle circle = new Circle();
    base_square.getChildren().add(circle);
    circle.setCenterX(i * BOUND + BOUND / 2.0 + OFFSET);
    circle.setCenterY(j * BOUND + BOUND / 2.0 + OFFSET);
    circle.setRadius(BOUND / 2.0 - OFFSET / 2.0);
    circle.setStroke(Color.RED);
    circle.setFill(Color.TRANSPARENT);
    flag[i][j] = true;
  }

  private void drawLine(int i, int j) {
    Line line_a = new Line();
    Line line_b = new Line();
    base_square.getChildren().add(line_a);
    base_square.getChildren().add(line_b);
    line_a.setStartX(i * BOUND + OFFSET * 1.5);
    line_a.setStartY(j * BOUND + OFFSET * 1.5);
    line_a.setEndX((i + 1) * BOUND + OFFSET * 0.5);
    line_a.setEndY((j + 1) * BOUND + OFFSET * 0.5);
    line_a.setStroke(Color.BLUE);

    line_b.setStartX((i + 1) * BOUND + OFFSET * 0.5);
    line_b.setStartY(j * BOUND + OFFSET * 1.5);
    line_b.setEndX(i * BOUND + OFFSET * 1.5);
    line_b.setEndY((j + 1) * BOUND + OFFSET * 0.5);
    line_b.setStroke(Color.BLUE);
    flag[i][j] = true;
  }
}
