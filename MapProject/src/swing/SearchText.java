
package swing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import model.Model_SearchText;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SearchText extends JTextField{

    private Model_SearchText model;

    public SearchText(){
        setBorder(new EmptyBorder(5, 5, 5, 5));
        setSelectionColor(new Color(137, 207, 240));
        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateModel(); 
            }
        });
    }
    

    private final String hint = "Search here ...";

    private void updateModel() {
        String text = getText();
        model.setLocation(text); // Update the location in the model
    }
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (getText().length() == 0) {
            int h = getHeight();
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Insets ins = getInsets();
            FontMetrics fm = g.getFontMetrics();
            int c0 = getBackground().getRGB();
            int c1 = getForeground().getRGB();
            int m = 0xfefefefe;
            int c2 = ((c0 & m) >>> 1) + ((c1 & m) >>> 1);
            g.setColor(new Color(c2, true));
            g.drawString(hint, ins.left, h / 2 + fm.getAscent() / 2 - 2);
        }
    }
    
}
