
package swing;

import javax.swing.JPanel;
import java.awt.*;
public class PanelOval extends JPanel{
    public PanelOval() {
        super();
        setOpaque(false); // Make sure the panel is transparent
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
        super.paintComponent(g);
    }
}
