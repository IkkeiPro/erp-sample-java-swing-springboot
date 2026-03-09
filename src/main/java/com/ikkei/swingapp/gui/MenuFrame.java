package com.ikkei.swingapp.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class MenuFrame extends JFrame {

    public MenuFrame(ApplicationContext applicationContext) {
        super("メニュー画面");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 320);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        JLabel title = new JLabel("使えるメニュー", JLabel.CENTER);
        root.add(title, BorderLayout.NORTH);

        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.add(Box.createVerticalStrut(16));

        JButton mainFrameButton = new JButton("MainFrame");
        mainFrameButton.setAlignmentX(CENTER_ALIGNMENT);
        mainFrameButton.setMaximumSize(new Dimension(240, 36));
        mainFrameButton.addActionListener(e -> {
            MainFrame mainFrame = applicationContext.getBean(MainFrame.class);
            mainFrame.setVisible(true);
        });

        JButton compositionFrameButton = new JButton("構成画面");
        JButton specialSpecFrameButton = new JButton("特別仕様画面");
        specialSpecFrameButton.setAlignmentX(CENTER_ALIGNMENT);
        specialSpecFrameButton.setMaximumSize(new Dimension(240, 36));
        specialSpecFrameButton.addActionListener(e -> {
            SpecialSpecFrame specialSpecFrame = applicationContext.getBean(SpecialSpecFrame.class);
            specialSpecFrame.reloadTable();
            specialSpecFrame.setVisible(true);
        });

        compositionFrameButton.setAlignmentX(CENTER_ALIGNMENT);
        compositionFrameButton.setMaximumSize(new Dimension(240, 36));
        compositionFrameButton.addActionListener(e -> {
            CompositionFrame compositionFrame = applicationContext.getBean(CompositionFrame.class);
            compositionFrame.reloadTable();
            compositionFrame.setVisible(true);
        });

        menuPanel.add(mainFrameButton);
        menuPanel.add(Box.createVerticalStrut(12));
        menuPanel.add(compositionFrameButton);
        menuPanel.add(Box.createVerticalStrut(12));
        menuPanel.add(specialSpecFrameButton);
        menuPanel.add(Box.createVerticalGlue());

        root.add(menuPanel, BorderLayout.CENTER);
        add(root);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                // メニュー再表示時に中央へ寄せる
                setLocationRelativeTo(null);
            }
        });
    }
}
