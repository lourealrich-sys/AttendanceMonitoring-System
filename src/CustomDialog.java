import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CustomDialog extends JDialog {
    private Point mouseClickPoint; // Used for dragging the window

    public CustomDialog() {
        // 1. Remove the default OS frame
        setUndecorated(true);
        setSize(450, 300);
        setLocationRelativeTo(null);

        // 2. Create the main container with a thin border
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 1));

        // 3. Create the Custom Top Frame (Title Bar)
        JPanel topFrame = new JPanel(new BorderLayout());
        topFrame.setBackground(new Color(85, 85, 21)); // YOUR CUSTOM COLOR
        topFrame.setPreferredSize(new Dimension(0, 40));

        // Add Title Text
        JLabel titleLabel = new JLabel("  Custom Application Dialog");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        topFrame.add(titleLabel, BorderLayout.WEST);

        // Add a simple Close Button
        JButton closeButton = new JButton("X");
        closeButton.setForeground(Color.WHITE);
        closeButton.setContentAreaFilled(false);
        closeButton.setBorderPainted(false);
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(e -> dispose());
        topFrame.add(closeButton, BorderLayout.EAST);

        // 4. Make the window draggable
        topFrame.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                mouseClickPoint = e.getPoint();
            }
        });
        topFrame.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point currCoords = e.getLocationOnScreen();
                setLocation(currCoords.x - mouseClickPoint.x, currCoords.y - mouseClickPoint.y);
            }
        });

        // 5. Add Content Area
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(new Color(240, 240, 240));
        contentPanel.add(new JLabel("This is the main dialog content."));

        // Assemble
        mainPanel.add(topFrame, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        add(mainPanel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new CustomDialog().setVisible(true);
        });
    }
}