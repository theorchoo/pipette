import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ColoringBook {

    public void init() {
        JLabel label = new JLabel("Hello Swing!", SwingConstants.CENTER);
        JFrame frame = new JFrame("HelloSwing Application");
        frame.getContentPane().add(label, BorderLayout.CENTER);

        //Finish setting up the frame, and show it.
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) throws Exception {
        ColoringBook app = new ColoringBook();
        app.init();
    }
}
