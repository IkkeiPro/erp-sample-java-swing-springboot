package com.ikkei.swingapp;

import javax.swing.SwingUtilities;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class SwingappApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context =
                new SpringApplicationBuilder(SwingappApplication.class)
                        .headless(false)
                        .run(args);

        SwingUtilities.invokeLater(() -> {
            var frame = context.getBean(com.ikkei.swingapp.gui.MainFrame.class);
            frame.setVisible(true);
        });
    }
}