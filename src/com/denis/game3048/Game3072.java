package com.denis.game3048;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Game3072 extends JPanel {
  private static final Color BG_COLOR = new Color(0xFFFFFF);
  private static final String FONT_NAME = "Arial";
  private static final int TILE_SIZE = 64;
  private static final int TILES_MARGIN = 16;

  private Tile[] myTiles;
  boolean myWin = false;
  boolean myLose = false;
  int myScore = 0;

  public Game3072() {
    setPreferredSize(new Dimension(400, 600));
    setFocusable(true);
    addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          resetGame();
        }
        if (!canMove()) {
          myLose = true;
        }

        if (!myWin && !myLose) {
          switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
              left();
              break;
            case KeyEvent.VK_RIGHT:
              right();
              break;
            case KeyEvent.VK_DOWN:
              down();
              break;
            case KeyEvent.VK_UP:
              up();
              break;
          }
        }

        if (!myWin && !canMove()) {
          myLose = true;
        }

        repaint();
      }
    });
    resetGame();
  }

  // сбрасываем игру в начальное состояние
  public void resetGame() {
    myScore = 0; // обнуляем текущий счет
    myWin = false;
    myLose = false;
    myTiles = new Tile[4 * 4]; // Создаем массив myTiles с 16 пустыми плитками.
    for (int i = 0; i < myTiles.length; i++) {
      // В цикле проходимся по каждому элементу массива myTiles
      // и создаем новый объект типа Tile для каждой ячейки.
      myTiles[i] = new Tile();
    }
    // Вызываем метод addTile() дважды,
    // чтобы добавить две новые плитки в случайные пустые ячейки на игровом поле.
    addTile();
    addTile();
  }

  public void left() {
    /*
     * отвечает за сдвиг всех плиток влево на игровом поле,
     * слияние плиток одного номинала и добавление новой плитки в случае,
     * если было произведено слияние
     */
    boolean needAddTile = false; // определяет необход. добавл. новой плитки на поле
    for (int i = 0; i < 4; i++) {
      Tile[] line = getLine(i);
      // В цикле проходится по каждой строке игрового поля и
      // получает массив плиток для этой строки с помощью метода getLine(i).
      Tile[] merged = mergeLine(moveLine(line));
      // Полученный массив плиток передается в метод moveLine(),
      // который производит сдвиг плиток влево и возвращает новый массив.
      // Полученный массив сдвинутых плиток передается в метод mergeLine(),
      // который объединяет плитки одного номинала и возвращает новый массив.
      setLine(i, merged);
      // Полученный массив объединенных плиток передается в метод setLine(i),
      // который записывает новый массив в соответствующую строку игрового поля.
      if (!needAddTile && !compare(line, merged)) {
        needAddTile = true;
        // Если не было произведено слияние плиток и переменная needAddTile равна false,
        // то она устанавливается в true.
      }
    }
    if (needAddTile) {
      addTile();
      // Если переменная needAddTile равна true, то вызывается метод addTile(),
      // который добавляет новую плитку в случайную пустую ячейку на игровом поле.
    }
  }

  public void right() {
    myTiles = rotate(180);
    left();
    myTiles = rotate(180);
  }

  public void up() {
    myTiles = rotate(270);
    left();
    myTiles = rotate(90);
  }

  public void down() {
    myTiles = rotate(90);
    left();
    myTiles = rotate(270);
  }

  private Tile tileAt(int x, int y) {
    return myTiles[x + y * 4];
  }

  private void addTile() {
    /*
     * отвечает за добавление новой плитки на игровое поле в случае,
     * если было произведено слияние плиток и на игровом поле есть пустые ячейки.
     */
    List<Tile> list = availableSpace();
    // Создаем список доступных для добавления плиток с помощью метода availableSpace().
    if (!availableSpace().isEmpty()) {
      // Если список не пустой, то генерируется случайный индекс из списка доступных ячеек.
      int index = (int) (Math.random() * list.size()) % list.size();
      Tile emptyTime = list.get(index);
      emptyTime.value = Math.random() < 0.9 ? 3 : 6;
      // Полученная пустая ячейка заполняется новой плиткой с номиналом 2 или 4
      // с вероятностью 90 % и 10 % соответственно.
    }
  }

  private List<Tile> availableSpace() {
    /*
     * возвращает список пустых ячеек на игровом поле
     */
    final List<Tile> list = new ArrayList<Tile>(16);
    // Создается новый список объектов класса Tile с именем list,
    // который будет хранить все пустые ячейки на игровом поле.
    for (Tile t : myTiles) {
      // Цикл for-each проходит по всем объектам класса Tile в массиве myTiles.
      if (t.isEmpty()) {
        list.add(t);
        // Если текущая ячейка (объект класса Tile) пустая (метод isEmpty() возвращает true),
        // то она добавляется в список list с помощью метода add().
      }
    }
    return list;
  }

  private boolean isFull() {
    /*
     * использует метод availableSpace()
     * для получения списка всех пустых ячеек на игровом поле.
     */
    return availableSpace().size() == 0;
    // Если размер списка равен нулю, то метод возвращает true, что означает,
    // что все ячейки на игровом поле заполнены.
    // В противном случае метод возвращает false, что означает,
    // что есть хотя бы одна пустая ячейка на игровом поле.
  }

  boolean canMove() {
    /*
     * проверка хода
     */
    if (!isFull()) {
      // проверяет, не заполнено ли игровое поле, используя метод "isFull()".
      // Если игровое поле не заполнено, возвращается true.
      return true;
    }
    for (int x = 0; x < 4; x++) {
      for (int y = 0; y < 4; y++) {
        // перебирает все плитки на поле.
        Tile t = tileAt(x, y);
        if ((x < 3 && t.value == tileAt(x + 1, y).value)
          || ((y < 3) && t.value == tileAt(x, y + 1).value)) {
          return true;
          // Если найдены соседние плитки с одинаковым значением, возвращается true.
        }
      }
    }
    // Если не найдено ни одной соседней плитки с одинаковым значением, возвращается false.
    return false;
  }

  private boolean compare(Tile[] line1, Tile[] line2) {
    if (line1 == line2) {
      return true;
    } else if (line1.length != line2.length) {
      return false;
    }

    for (int i = 0; i < line1.length; i++) {
      if (line1[i].value != line2[i].value) {
        return false;
      }
    }
    return true;
  }

  private Tile[] rotate(int angle) {
    Tile[] newTiles = new Tile[4 * 4];
    int offsetX = 3, offsetY = 3;
    if (angle == 90) {
      offsetY = 0;
    } else if (angle == 270) {
      offsetX = 0;
    }

    double rad = Math.toRadians(angle);
    int cos = (int) Math.cos(rad);
    int sin = (int) Math.sin(rad);
    for (int x = 0; x < 4; x++) {
      for (int y = 0; y < 4; y++) {
        int newX = (x * cos) - (y * sin) + offsetX;
        int newY = (x * sin) + (y * cos) + offsetY;
        newTiles[(newX) + (newY) * 4] = tileAt(x, y);
      }
    }
    return newTiles;
  }

  private Tile[] moveLine(Tile[] oldLine) {
    LinkedList<Tile> l = new LinkedList<Tile>();
    for (int i = 0; i < 4; i++) {
      if (!oldLine[i].isEmpty())
        l.addLast(oldLine[i]);
    }
    if (l.size() == 0) {
      return oldLine;
    } else {
      Tile[] newLine = new Tile[4];
      ensureSize(l, 4);
      for (int i = 0; i < 4; i++) {
        newLine[i] = l.removeFirst();
      }
      return newLine;
    }
  }

  private Tile[] mergeLine(Tile[] oldLine) {
    LinkedList<Tile> list = new LinkedList<Tile>();
    for (int i = 0; i < 4 && !oldLine[i].isEmpty(); i++) {
      int num = oldLine[i].value;
      if (i < 3 && oldLine[i].value == oldLine[i + 1].value) {
        num *= 2;
        myScore += num;
        int ourTarget = 3072;
        if (num == ourTarget) {
          myWin = true;
        }
        i++;
      }
      list.add(new Tile(num));
    }
    if (list.size() == 0) {
      return oldLine;
    } else {
      ensureSize(list, 4);
      return list.toArray(new Tile[4]);
    }
  }

  private static void ensureSize(java.util.List<Tile> l, int s) {
    while (l.size() != s) {
      l.add(new Tile());
    }
  }

  private Tile[] getLine(int index) {
    Tile[] result = new Tile[4];
    for (int i = 0; i < 4; i++) {
      result[i] = tileAt(i, index);
    }
    return result;
  }

  private void setLine(int index, Tile[] re) {
    System.arraycopy(re, 0, myTiles, index * 4, 4);
  }

  @Override
  public void paint(Graphics g) {
    super.paint(g);
    g.setColor(BG_COLOR);
    g.fillRect(0, 0, this.getSize().width, this.getSize().height);
    for (int y = 0; y < 4; y++) {
      for (int x = 0; x < 4; x++) {
        drawTile(g, myTiles[x + y * 4], x, y);
      }
    }
  }

  private void drawTile(Graphics g2, Tile tile, int x, int y) {
    Graphics2D g = ((Graphics2D) g2);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
    int value = tile.value;
    int xOffset = offsetCoors(x);
    int yOffset = offsetCoors(y);
    g.setColor(tile.getBackground());
    g.fillRoundRect(xOffset, yOffset, TILE_SIZE, TILE_SIZE, 14, 14);
    g.setColor(tile.getForeground());
    final int size = value < 100 ? 36 : value < 1000 ? 32 : 24;
    final Font font = new Font(FONT_NAME, Font.BOLD, size);
    g.setFont(font);

    String s = String.valueOf(value);
    final FontMetrics fm = getFontMetrics(font);

    final int w = fm.stringWidth(s);
    final int h = -(int) fm.getLineMetrics(s, g).getBaselineOffsets()[2];

    if (value != 0)
      g.drawString(s, xOffset + (TILE_SIZE - w) / 2, yOffset + TILE_SIZE - (TILE_SIZE - h) / 2 - 2);

    if (myWin || myLose) {
      g.setColor(new Color(255, 255, 255, 30));
      g.fillRect(0, 0, getWidth(), getHeight());
      g.setColor(new Color(78, 139, 202));
      g.setFont(new Font(FONT_NAME, Font.BOLD, 48));
      if (myWin) {
        g.drawString("Вы выиграли!", 68, 150);
      }
      if (myLose) {
        g.drawString("Игра окончена!", 50, 130);
        g.drawString("Вы проиграли!", 64, 200);
      }
      if (myWin || myLose) {
        g.setFont(new Font(FONT_NAME, Font.PLAIN, 16));
        g.setColor(new Color(128, 128, 128, 128));
        g.drawString("Нажмите ENTER, чтобы начать заново", 80, getHeight() - 40);
      }
    }
    g.setFont(new Font(FONT_NAME, Font.PLAIN, 18));
    g.drawString("Счет: " + myScore, 200, 365);

  }

  private static int offsetCoors(int arg) {
    return arg * (TILES_MARGIN + TILE_SIZE) + TILES_MARGIN;
  }

  static class Tile {
    int value;

    public Tile() {
      this(0);
    }

    public Tile(int num) {
      value = num;
    }

    public boolean isEmpty() {
      return value == 0;
    }

    public Color getForeground() {
      return value < 16 ? new Color(0x776e65) :  new Color(0xf9f6f2);
    }

    public Color getBackground() {
      switch (value) {
        case 3:    return new Color(0xDBEEDA);
        case 6:    return new Color(0xC8EDE4);
        case 12:   return new Color(0x79A9F2);
        case 24:   return new Color(0x6391F5);
        case 48:   return new Color(0xA35FF6);
        case 96:   return new Color(0xF63BF3);
        case 192:  return new Color(0xED72B4);
        case 384:  return new Color(0xED6182);
        case 768:  return new Color(0xED5050);
        case 1536: return new Color(0xED9C3F);
        case 3072: return new Color(0xedc22e);
      }
      return new Color(0xCDCCB4);
    }
  }

  public static void main(String[] args) {
    JFrame game = new JFrame();
    game.setTitle("3072 Game");
    game.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    game.setSize(450, 500);
    game.setResizable(false);

    game.add(new Game3072());

    game.setLocationRelativeTo(null);
    game.setVisible(true);
  }
}
