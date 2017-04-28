import javax.swing.*;
import java.awt.image.BufferedImage;

public class CustomJFrame extends JFrame {

    public CustomJFrame(int width, int height) {
        super.setTitle("Test");
        super.setSize(width, height);
        super.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public void addImage(BufferedImage image) {
        super.add(new ImagePanel(image));
        super.setVisible(true);
    }
}
