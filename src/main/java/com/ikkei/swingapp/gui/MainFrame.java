package com.ikkei.swingapp.gui;
        
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.springframework.stereotype.Component;

@Component
public class MainFrame extends JFrame {

    public MainFrame() {
        super("Swing + Spring Boot");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);

        JLabel label = new JLabel("起動できました", JLabel.CENTER);

        JButton button = new JButton("クリック");
        button.addActionListener(e -> label.setText("押された！"));

        JPanel south = new JPanel();
        south.add(button);

        add(label, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
    }
}