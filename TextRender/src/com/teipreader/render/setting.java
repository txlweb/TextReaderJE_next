package com.teipreader.render;

import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class setting {
    private JFrame frame;
    private JSpinner rowRatioSpinner;
    private JSpinner fontSizeSpinner;
    private JComboBox<String> fontIDComboBox;
    private JCheckBox enableAutoSyncCheckBox;
    // 设置变量
    public static int rowRatio = 15;
    public static int fontSize = 30;
    public static String fontID = "Serif";
    public static boolean enableAutoSync = true;

    public setting() {
        //获取字体
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fonts = ge.getAvailableFontFamilyNames();
        // 创建窗口
        frame = new JFrame("配置TextReader Render Core");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new GridLayout(5, 2, 10, 10));
        frame.setSize(300, 200);
//        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setResizable(false);
        // 创建控件
        rowRatioSpinner = new JSpinner(new SpinnerNumberModel(rowRatio, 1, 1000, 10));
        fontSizeSpinner = new JSpinner(new SpinnerNumberModel(fontSize, 6, 72, 1));
        fontIDComboBox = new JComboBox<>(fonts);//new String[]{"Serif", "Msyh"}
        enableAutoSyncCheckBox = new JCheckBox("启用", enableAutoSync);

        // 添加组件到窗口
        frame.add(new JLabel("滚轮系数:"));
        frame.add(rowRatioSpinner);

        frame.add(new JLabel("字 号  :"));
        frame.add(fontSizeSpinner);

        frame.add(new JLabel("字 体  :"));
        frame.add(fontIDComboBox);
        fontIDComboBox.setSelectedItem(fontID);
        frame.add(new JLabel("显示FPS:"));
        frame.add(enableAutoSyncCheckBox);

        // 确定和取消按钮
        JButton applyButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");

        frame.add(applyButton);
        frame.add(cancelButton);

        // 按钮事件
        applyButton.addActionListener(e -> applySettings());

        cancelButton.addActionListener(e -> frame.dispose());

        // 显示窗口
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void applySettings() {
        // 获取输入框的值
        rowRatio = (Integer) rowRatioSpinner.getValue();
        fontSize = (Integer) fontSizeSpinner.getValue();
        fontID = (String) fontIDComboBox.getSelectedItem();
        enableAutoSync = enableAutoSyncCheckBox.isSelected();

        // 打印测试
        System.out.println("Row Ratio: " + rowRatio);
        System.out.println("Font Size: " + fontSize);
        System.out.println("Font ID: " + fontID);
        System.out.println("Enable Auto Sync: " + enableAutoSync);
        // 关闭窗口
        frame.dispose();
        saveSettings();
    }
    private static final String SETTINGS_FILE = "render_settings.properties";

    public static void saveSettings() {
        Properties props = new Properties();
        props.setProperty("rowRatio", String.valueOf(rowRatio));
        props.setProperty("fontSize", String.valueOf(fontSize));
        props.setProperty("fontID", fontID);
        props.setProperty("enableAutoSync", String.valueOf(enableAutoSync));

        try (FileOutputStream out = new FileOutputStream(SETTINGS_FILE)) {
            props.store(out, "TextReader Settings");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadSettings() {
        Properties props = new Properties();

        try (FileInputStream in = new FileInputStream(SETTINGS_FILE)) {
            props.load(in);
            setting.rowRatio = Integer.parseInt(props.getProperty("rowRatio", "15"));
            setting.fontSize = Integer.parseInt(props.getProperty("fontSize", "30"));
            setting.fontID = props.getProperty("fontID", "Serif");
            setting.enableAutoSync = Boolean.parseBoolean(props.getProperty("enableAutoSync", "true"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
