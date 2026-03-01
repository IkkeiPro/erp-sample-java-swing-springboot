package com.ikkei.swingapp.gui;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.springframework.stereotype.Component;

@Component
public class MainFrame extends JFrame {

    public MainFrame(MenuFrame menuFrame) {
        super("Swing + Spring Boot");

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);

        JLabel label = new JLabel("起動できました", JLabel.CENTER);

        JButton button = new JButton("クリック");
        button.addActionListener(e -> label.setText("押された！"));

        JButton backButton = new JButton("メニューに戻る");
        backButton.addActionListener(e -> {
            menuFrame.setVisible(true);
            dispose();
        });

        JPanel south = new JPanel();
        south.add(button);
        south.add(backButton);

        add(label, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
    }
}
