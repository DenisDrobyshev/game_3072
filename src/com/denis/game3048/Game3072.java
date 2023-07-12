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
  private static final String FONT_NAME = "MonoSpace";
  private static final int TILE_SIZE = 80;
  private static final int TILES_MARGIN = 16;

  private Tile[] myTiles;
  boolean myWin = false;
  boolean myLose = false;
  int myScore = 0;

  public Game3072() {
    // размер окна игры
    setPreferredSize(new Dimension(450, 500));
    // позволяет игроку фокусироваться на окне игры
    setFocusable(true);
    addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          resetGame();
        }
        // Если игрок не может сделать ход, игра проиграна
        if (!canMove()) {
          myLose = true;
        }

        if (!myWin && !myLose) {
          // Если игрок еще не выиграл и не проиграл,
          // то в зависимости от нажатой клавиши вызывается соответствующий метод
          // для перемещения плиток в игре.
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
        // Если игрок не выиграл и не может сделать ход, игра проиграна
        if (!myWin && !canMove()) {
          myLose = true;
        }
        // перерисовка окна игры
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
     * сдвигает все плитки влево на игровом поле,
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
    /*
     * сдвигает все плитки вправо
     */
    myTiles = rotate(180);
    left();
    myTiles = rotate(180);
  }

  public void up() {
    /*
     * сдвигает все плитки вверх
     */
    myTiles = rotate(270);
    left();
    myTiles = rotate(90);
  }

  public void down() {
    /*
     * сдвигает все плитки вниз
     */
    myTiles = rotate(90);
    left();
    myTiles = rotate(270);
  }

  private Tile tileAt(int x, int y) {
    /*
     * возвращает плитку на заданных координатах
     */
    return myTiles[x + y * 4];
    // x + y * 4 используется для того, чтобы преобразовать двумерные координаты
    // x и y в одномерный индекс элемента массива.

    // координаты задаются в виде x и y,
    // где x - номер столбца (от 0 до 3), а y - номер строки (от 0 до 3).
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
      // Полученная пустая ячейка заполняется новой плиткой с номиналом 3 или 6
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
    /*
     * сравнивает две линии, представленные в виде массивов объектов Tile.
     * Если массивы имеют одинаковую длину и значения всех элементов в них равны,
     * то функция возвращает true, иначе false.
     */
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
    /*
     * принимает на вход угол поворота angle и возвращает новый массив объектов Tile,
     * содержащий новые позиции плиток после поворота.
     */
    Tile[] newTiles = new Tile[4 * 4];
    int offsetX = 3, offsetY = 3;
    // Устанавливаются смещения offsetX и offsetY в зависимости от угла поворота.
    // Если угол равен 90, то offsetY устанавливается в 0,
    // а если угол равен 270, то offsetX устанавливается в 0.
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
        // Новые координаты используются для записи плитки в новый массив newTiles
        // с помощью индексации по формуле (newX) + (newY) * 4.
        newTiles[(newX) + (newY) * 4] = tileAt(x, y);
      }
    }
    return newTiles;
  }

  private Tile[] moveLine(Tile[] oldLine) {
    /*
     * принимает на вход массив объектов Tile oldLine и возвращает новый массив объектов Tile,
     * содержащий новые позиции плиток после перемещения.
     */
    LinkedList<Tile> l = new LinkedList<Tile>();
    for (int i = 0; i < 4; i++) {
      // Если плитка не пуста, то она добавляется в конец списка l с помощью метода addLast().
      if (!oldLine[i].isEmpty())
        l.addLast(oldLine[i]);
    } // Если список l пуст, то возвращается исходный массив oldLine.
    if (l.size() == 0) {
      return oldLine;
    } else { // В противном случае создается новый массив объектов Tile - newLine размером 4.
      Tile[] newLine = new Tile[4];
      ensureSize(l, 4);
      for (int i = 0; i < 4; i++) {
        newLine[i] = l.removeFirst();
        // Каждый элемент списка l удаляется из начала списка и записывается
        // в соответствующую позицию нового массива newLine с помощью индексации по i.
      }
      return newLine;
    }
  }

  private Tile[] mergeLine(Tile[] oldLine) {
    /*
     * принимает на вход массив объектов Tile oldLine и возвращает
     * новый массив объектов Tile, содержащий объединенные плитки.
     */

    // Создается новый связанный список объектов Tile - list.
    LinkedList<Tile> list = new LinkedList<Tile>();
    // перебор всех плиток в oldLine с помощью цикла for.
    for (int i = 0; i < 4 && !oldLine[i].isEmpty(); i++) {
      // Если плитка не пуста, то ее значение записывается в переменную num.
      int num = oldLine[i].value;
      if (i < 3 && oldLine[i].value == oldLine[i + 1].value) {
        num *= 2;
        myScore += num;
        int ourTarget = 3072;
        if (num == ourTarget) {
          myWin = true;
        }
        // индекс i увеличивается на 1, чтобы пропустить следующую плитку.
        i++;
      }// Объект Tile с новым значением num добавляется в конец списка list с помощью метода add().
      list.add(new Tile(num));
    } // Если список list пуст, то возвращается исходный массив oldLine.
    if (list.size() == 0) {
      return oldLine;
    } else { // иначе список list расширяется до размера 4 с помощью метода ensureSize().
      ensureSize(list, 4);
      return list.toArray(new Tile[4]);
    }
  }

  private static void ensureSize(java.util.List<Tile> l, int s) {
    /*
     * принимает на вход связанный список объектов Tile l и целочисленное значение s.
     * Она увеличивает размер списка l до значения s,
     * добавляя пустые плитки (объекты Tile) в конец списка с помощью метода add().
     */
    while (l.size() != s) {
      l.add(new Tile());
    }
  }

  private Tile[] getLine(int index) {
    Tile[] result = new Tile[4];
    // В каждой итерации цикла создается объект Tile
    // с помощью метода tileAt, который принимает на вход два целочисленных
    // значения (координаты плитки на игровом поле) и возвращает объект Tile
    // на этой позиции.
    for (int i = 0; i < 4; i++) {
      result[i] = tileAt(i, index);
      // Полученный объект Tile добавляется в массив result на соответствующую позицию i.
    }
    return result;
  }

  private void setLine(int index, Tile[] re) {
    System.arraycopy(re, 0, myTiles, index * 4, 4);
  }

  @Override
  // Создается метод paint, который принимает на вход объект Graphics.
  public void paint(Graphics g) {
    // Вызывается метод paint родительского класса, чтобы нарисовать фон.
    super.paint(g);
    // Устанавливается цвет фона.
    g.setColor(BG_COLOR);
    // Рисуется прямоугольник, заполняющий всю область игрового поля.
    g.fillRect(0, 0, this.getSize().width, this.getSize().height);
    // В каждой итерации цикла получается объект Tile с помощью индексации массива myTiles.
    for (int y = 0; y < 4; y++) {
      for (int x = 0; x < 4; x++) {
        // принимает на вход объект Graphics, объект Tile и координаты x и y.
        drawTile(g, myTiles[x + y * 4], x, y);
      }
    }
  }

  private void drawTile(Graphics g2, Tile tile, int x, int y) {
    Graphics2D g = ((Graphics2D) g2);
    // параметры рендеринга для сглаживания границ и нормализации контуров.
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
    // значение плитки из объекта Tile.
    int value = tile.value;
    // смещения для координат x и y плитки.
    int xOffset = offsetCoors(x);
    int yOffset = offsetCoors(y);
    // Устанавливается цвет фона плитки и рисуется закругленный прямоугольник с размерами TILE_SIZE.
    g.setColor(tile.getBackground());
    // 15 15 - параметры закругления
    g.fillRoundRect(xOffset, yOffset, TILE_SIZE, TILE_SIZE, 15, 15);
    // Устанавливается цвет текста плитки.
    g.setColor(tile.getForeground());
    // размер шрифта в зависимости от значения плитки.
    final int size = value < 100 ? 36 : value < 1000 ? 32 : 24;
    // объект Font с выбранным размером шрифта и именем шрифта FONT_NAME.
    final Font font = new Font(FONT_NAME, Font.BOLD, size);
    g.setFont(font);
    // Получается строковое представление значения плитки.
    String s = String.valueOf(value);
    // Получаются метрики шрифта для вычисления размеров текста.
    final FontMetrics fm = getFontMetrics(font);
    // Вычисляются ширина и высота текста.
    final int w = fm.stringWidth(s);
    final int h = -(int) fm.getLineMetrics(s, g).getBaselineOffsets()[2];

    if (value != 0)
      // рисуется строка с текстом значения плитки по центру плитки.
      g.drawString(s, xOffset + (TILE_SIZE - w) / 2, yOffset + TILE_SIZE - (TILE_SIZE - h) / 2 - 2);

    if (myWin || myLose) {
      // устанавливается цвет фона
      g.setColor(new Color(255, 255, 255, 255));
      // рисуется прямоугольник
      g.fillRect(0, 0, getWidth(), getHeight());
      // устанавливается цвет текста
      g.setColor(new Color(190, 78, 202));
      // рисуется строка с сообщением о выигрыше или проигрыше
      g.setFont(new Font(FONT_NAME, Font.BOLD, 48));
      if (myWin) {
        g.drawString("Вы выиграли!", 68, 150);
      }
      if (myLose) {
        g.drawString("Игра окончена!", 50, 130);
        g.drawString("Вы проиграли!", 50, 200);
      }
      if (myWin || myLose) {
        g.setFont(new Font(FONT_NAME, Font.PLAIN, 16));
        g.setColor(new Color(128, 128, 128, 128));
      }
    }
    g.setFont(new Font(FONT_NAME, Font.PLAIN, 18));
    g.drawString("Счет: " + myScore, 150, 440);
    g.drawString("Нажмите Enter для перезапуска", 52, 410);

  }

  private static int offsetCoors(int arg) {
    /*
     * Используется для вычисления координат плитки на игровом поле.
     * принимает аргумент "arg", который является индексом строки или столбца,
     * вычисляет координату с учетом размера плитки и отступов между ними.
     */
    return arg * (TILES_MARGIN + TILE_SIZE) + TILES_MARGIN;
  }

  static class Tile {
    int value;

    public Tile() {
      /*
       * представляет собой отдельную плитку на игровом поле.
       */
      this(0);
    }

    public Tile(int num) {
      value = num;
    }

    public boolean isEmpty() {
      /*
       * возвращает true, если значение плитки равно 0, т.е. плитка пустая.
       */
      return value == 0;
    }

    public Color getForeground() {
      // возвращает цвет текста на плитке в зависимости от ее значения.
      // Если значение меньше 47, то цвет текста будет темным, иначе - светлым.
      return value < 47 ? new Color(0x776e65) :  new Color(0xf9f6f2);
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
    // создаем новое окно игры "3072" с помощью класса JFrame
    JFrame game = new JFrame();
    game.setTitle("3072 Game");
    // завершение программы при нажатии на крестик
    game.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    game.setSize(400, 500);
    game.setResizable(true);
    // добавляем на экран экз класса Game3072
    game.add(new Game3072());
    // по центру
    game.setLocationRelativeTo(null);
    // делаем видимым
    game.setVisible(true);
  }
}
