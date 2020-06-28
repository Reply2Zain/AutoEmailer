import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;

public class ViewImpl extends JFrame {

  private final JLabel recipientL;
  private String name;
  private final JTextArea textBox;
  private final JTextField recipientTextField;
  private final JTextField headerTextField;
  private final JButton sendButton;
  private final JButton resetButton;
  private final JButton stopButton;
  private final JTextField countTextField;
  private boolean isStopped;
  private final JFrame frame;
  private boolean taskMode;
  private boolean showCounter;
  private boolean emailAnyone;
  JMenuItem counterMenuItem;
  JMenuItem taskModeMenu;
  JMenuItem emailToMenuItem;

  ViewImpl() {
    if (System.getProperty("os.name").contains("Mac")) {
      System.setProperty("apple.laf.useScreenMenuBar", "true");
      System.setProperty(
          "com.apple.mrj.application.apple.menu.about.name", "Stack");

    }
    // sets the name of the person to email - leave blank unless dedicated
    this.name = "";

    frame = new JFrame(name.concat(" Auto Emailer"));
    frame.setDefaultCloseOperation(this.EXIT_ON_CLOSE);
    frame.setSize(480, 390);

    countTextField = new JTextField();
    countTextField.setText("1");
    countTextField.setColumns(3);
    stopButton = new JButton("Stop");
    this.isStopped = false;

    taskMode = false;

    //Creating the MenuBar and adding components
    JMenuBar mb = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    JMenuItem advancedMenu = new JMenu("Advanced");
    JMenu helpMenu = new JMenu("Help");
    mb.add(fileMenu);
    mb.add(advancedMenu);
    mb.add(helpMenu);
    JMenuItem m11 = new JMenuItem("Open");
    JMenuItem m12 = new JMenuItem("Save as");
    emailToMenuItem = new JMenuItem("Email me");
    counterMenuItem = new JMenuItem("Show counter");
    taskModeMenu = new JMenuItem("Enable task mode");
    fileMenu.add(m11);
    fileMenu.add(m12);
    advancedMenu.add(counterMenuItem);
    advancedMenu.add(emailToMenuItem);
    advancedMenu.add(taskModeMenu);
    showCounter = false;
    emailAnyone = true;

    stopButton.setVisible(false);
    countTextField.setVisible(false);

    //Creating the components
    JPanel bottomPanel = new JPanel(); // the panel is not visible in output
    JLabel headingL = new JLabel("Enter heading");
    recipientTextField = new JTextField(16); // accepts upto 10 characters
    headerTextField = new JTextField(20); // accepts upto 10 characters
    sendButton = new JButton("Send");
    resetButton = new JButton("Reset");
    recipientL = new JLabel();

    // Text Area at the Center
    JPanel centerPanel = new JPanel();
    centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
    textBox = new JTextArea();
    textBox.setLineWrap(true);
    textBox.setWrapStyleWord(true);
    TitledBorder b = BorderFactory.createTitledBorder("<HTML>Body of the email:</HTML>");
    b.setTitleColor(Color.DARK_GRAY);
    centerPanel.setBorder(b);
    JScrollPane scrollPane = new JScrollPane(textBox);

    JPanel headingBar = new JPanel();
    headingBar.setLayout(new FlowLayout());

    textBox.setTabSize(2);
    updateRecipientComponents();

    centerPanel.add(scrollPane);
    headingBar.add(headingL);
    headingBar.add(headerTextField);
    bottomPanel.add(recipientL);
    bottomPanel.add(recipientTextField);
    bottomPanel.add(sendButton);
    bottomPanel.add(resetButton);
    bottomPanel.add(countTextField);
    bottomPanel.add(stopButton);

    initializeActionListeners();
    initializeKeyListeners();

    //Adding Components to the frame.
    frame.getContentPane().add(BorderLayout.SOUTH, bottomPanel);
    frame.getContentPane().add(BorderLayout.CENTER, centerPanel);
    frame.setJMenuBar(mb);
    frame.getContentPane().add(BorderLayout.NORTH, headingBar);
    frame.setVisible(true);

  }

  private void updateRecipientComponents() {
    // changes the recipient label
    if (name.isEmpty()) {
      recipientL.setText("Enter recipient:");
      recipientTextField.setText("");
      recipientTextField.setVisible(true);
      frame.setName(name.concat(" Auto Emailer"));

    } else {
      recipientL.setText("Send to ".concat(name).concat(":"));
      recipientTextField.setText(personToEmailD(name));
      recipientTextField.setVisible(false);
      frame.setName(name.concat(" Auto Emailer"));
    }
  }


  private void initializeActionListeners() {
    sendButton.addActionListener(e -> {
      String r = recipientTextField.getText(); //recipient
      String t = textBox.getText();
      String h = headerTextField.getText();
      if (h.isEmpty() || t.isEmpty()) {
        error("Empty fields!");
        return;
      }

      if (r.length() < 7 || !r.contains("@")) {
        //error("Check recipient!");
        try {
          String newH;
          newH = h.replace(":", "");
          FileWriter fw = new FileWriter(newH);
          fw.write(t);
          fw.close();
          recipientL.setForeground(new Color(0, 134, 62));
          recipientL.setText("Saved!");
        } catch (IOException ioException) {
          ioException.printStackTrace();
        }
        return;
      }

      recipientL.setForeground(Color.BLUE);
      recipientL.setText("Sending...");

      SwingWorker<?, ?> sw = new SwingWorker<>() {
        @Override
        protected Object doInBackground() {
          try {
            sendMessage(r, h, t);
          } catch (Exception exception) {
            error();
          }
          return null;
        }
      };
      sw.execute();
    });

    emailToMenuItem.addActionListener(e -> {
      emailAnyone = !emailAnyone;
      if (emailAnyone) {
        emailToMenuItem.setText("Email anyone");
        name = "";
        updateRecipientComponents();
        frame.setTitle(" Auto Emailer");
      } else {
        emailToMenuItem.setText("Email me");
        name = "Zain";
        updateRecipientComponents();
        frame.setTitle("Email Myself");
      }
    });

    stopButton.addActionListener(e -> {
      this.isStopped = true;
    });

    counterMenuItem.addActionListener(e -> {
      showCounter = !showCounter;
      if (showCounter) {
        counterMenuItem.setText("Hide counter");
        stopButton.setVisible(true);
        countTextField.setVisible(true);
        if (frame.getWidth() < 610) {
          frame.setSize(610, frame.getHeight());
        }
      } else {
        counterMenuItem.setText("Show counter");
        stopButton.setVisible(false);
        countTextField.setVisible(false);
        frame.setSize(480, frame.getHeight());
      }
    });

    taskModeMenu.addActionListener(e -> {
      taskMode = !taskMode;

      Date time = new Date();
      SimpleDateFormat sdf = new SimpleDateFormat("EEEE");

      if (taskMode) {
        taskModeMenu.setText("Disable Task Mode");
        if (textBox.getText().isEmpty()) {
          textBox.append("-");
        }
        if (headerTextField.getText().isBlank()) {
          headerTextField.setText(sdf.format(time) + "'s Plan: ");
        }
      } else {
        taskModeMenu.setText("Enable Task Mode");
        if (textBox.getText().equals("-")) {
          textBox.setText("");
        }
      }
    });

    resetButton.addActionListener(e -> resetFields());
  }

  /**
   * Initializes all of the key listeners
   */
  private void initializeKeyListeners() {
    final Set<Integer> pressedKeys = new HashSet<>();

    KeyListener quit = new KeyListener() {
      @Override
      public void keyTyped(KeyEvent e) {

      }

      @Override
      public void keyPressed(KeyEvent e) {
        pressedKeys.add(e.getKeyCode());

        if (pressedKeys.size() < 3 && pressedKeys.contains(157) && pressedKeys.contains(87)) {
          System.exit(0);
        }
      }

      @Override
      public void keyReleased(KeyEvent e) {
        pressedKeys.remove(e.getKeyCode());
      }
    };

    KeyListener resetOnType = new KeyListener() {
      @Override
      public void keyTyped(KeyEvent e) {
        resetLabel();
      }

      @Override
      public void keyPressed(KeyEvent e) {
        //left blank
      }

      @Override
      public void keyReleased(KeyEvent e) {
        //left blank
      }
    };

    KeyListener textBoxKeySpecialities = new KeyListener() {
      @Override
      public void keyTyped(KeyEvent e) {
        // if pressing tab
        if (e.getKeyChar() == (KeyEvent.VK_TAB)) {
          if (taskMode) {
            // code to remove a tab from the middle of text
            String oTextBoxText = textBox.getText();
            String text;
            int carrotPositioning = textBox.getCaretPosition();
            //removing the inputted 'tab'
            if (oTextBoxText.length() == textBox.getCaretPosition()) {
              textBox.setText(oTextBoxText.substring(0, oTextBoxText.length() - 1));
              text = textBox.getText();
            } else {
              text = oTextBoxText.substring(0, textBox.getCaretPosition() - 1)
                  + oTextBoxText.substring(textBox.getCaretPosition());
            }

            String[] textArray = text.split("\n");
            if (textArray.length == 0) {
              return;
            }
            int totalSoFar = 0;
            int arrayIndex = 0;
            int carrotToEnd = 0;
            for (String s : textArray) {
              totalSoFar += s.length() + 1;
              if (textBox.getCaretPosition() <= totalSoFar) {
                //carrotToEnd = totalSoFar - textBox.getCaretPosition();
                break;
              }
              arrayIndex++;
            }

            StringBuilder newText = new StringBuilder();
            int moveCursorBack = 0;

/*            if(text.length() > textBox.getColumns()) {
              StringBuilder newString = new StringBuilder();
              int j = 0;
              for (int i = text.length()/ textBox.getWidth() - 5 ; i > 0; i--) {
                int newLength = Math.min(textBox.getWidth()*j - 5 * (j + 1), text.length());
                newString.append(text.substring(textBox.getWidth() - 5 * j, newLength));
                newString.append("\t");
                j++;
              }
              text = newString.toString();
            }*/

            if (arrayIndex < textArray.length) {
              moveCursorBack += (textArray[arrayIndex].length() + 1);
            } else {
              moveCursorBack++;
            }
            int beginningCutOff = totalSoFar - moveCursorBack;

            newText.append(text, 0, beginningCutOff);
            newText.append("\t");
            newText.append(text.substring(beginningCutOff));
            textBox.setText(newText.toString());
            textBox.setCaretPosition(carrotPositioning);
          } else if (textBox.getCaretPosition() == textBox.getText().length()) {
            textBox.setText(textBox.getText().substring(0, textBox.getText().length() - 1));
            if (recipientTextField.getText().isBlank()) {
              recipientTextField.grabFocus();
            } else {
              sendButton.grabFocus();
            }
          }
        } else if (taskMode && e.getKeyChar() == KeyEvent.VK_ENTER) {
          StringBuilder sb = new StringBuilder();
          String textBoxText = textBox.getText();
          int cp = textBox.getCaretPosition();
          sb.append(textBoxText, 0, cp).append("-");
          sb.append(textBoxText.substring(cp));
          textBox.setText(sb.toString());
          textBox.setCaretPosition(cp + 1);
        }
      }

      @Override
      public void keyPressed(KeyEvent e) {
        //left blank
      }

      @Override
      public void keyReleased(KeyEvent e) {
        //left blank
      }
    };

    textBox.addKeyListener(resetOnType);
    recipientTextField.addKeyListener(resetOnType);
    headerTextField.addKeyListener(resetOnType);
    textBox.addKeyListener(quit);
    textBox.addKeyListener(textBoxKeySpecialities);
    recipientTextField.addKeyListener(quit);
    headerTextField.addKeyListener(quit);
    resetButton.addKeyListener(quit);
    sendButton.addKeyListener(quit);
  }


  private void sendMessage(String recipient, String htfText, String taText) throws Exception {
    int times;
    try {
      times = Integer.parseInt(countTextField.getText());
    } catch (NumberFormatException nfe) {
      recipientL.setText("Count failed!");
      return;
    }
    if (times > 1) {

      recipientL.setText("Sent 0/" + times);
      for (int i = 1; i <= times; i++) {
        if (isStopped) {
          isStopped = false;
          error("Stopped (" + (i - 1) + ")");
          return;
        }
        if (i > 1) {
          JavaMailUtil.sendMail(recipient, htfText + " " + i, taText);
        } else {
          JavaMailUtil.sendMail(recipient, htfText, taText);
        }
        recipientL.setText("Sent " + i + "/" + times);
        if (i != times) {
          Thread.sleep(5700);
        }
      }
    } else {
      JavaMailUtil.sendMail(recipient, htfText, taText);
    }

    //JavaMailUtil.sendMail("sendreplyza@gmail.com", htfText + " - " + name, taText);
    recipientL.setForeground(new Color(0, 134, 62));
    StringBuilder rsb = new StringBuilder();
    if (times == 1) {
      rsb.append("Sent!");
    } else {
      rsb.append("Sent! (").append(times).append(")");
    }
    if (!this.name.isEmpty()) {
      rsb.append(" to ").append(name).append("!");
    }
    recipientL.setText(rsb.toString());
  }

  /**
   * The database of people who can be emailed.
   *
   * @param s the name of the person
   * @return the email address
   */
  private String personToEmailD(String s) {
    if (s.substring(0, 1).equalsIgnoreCase("k")) {
      return "keith.kondapi@gmail.com";
    }
    if (s.substring(0, 1).equalsIgnoreCase("a")) {
      return "replyali10@gmail.com";
    }
    if (s.substring(0, 1).equalsIgnoreCase("z")) {
      return "reply2zain@gmail.com";
    } else {
      throw new IllegalArgumentException("Cannot find person in database.");
    }
  }


  private void resetFields() {
    resetLabel();
    recipientTextField.setText("");
    headerTextField.setText("");
    textBox.setText("");
  }

  private void resetLabel() {
    recipientL.setForeground(Color.BLACK);
    if (name.isEmpty()) {
      recipientL.setText("Enter recipient:");
    } else {
      recipientL.setText("Send to " + name);
    }
  }

  private void error() {
    recipientL.setText("Failed!");
    recipientL.setForeground(new Color(149, 0, 0));
  }

  private void error(String s) {
    recipientL.setText(s);
    recipientL.setForeground(new Color(149, 0, 0));
  }

}

