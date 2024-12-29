package com.teipreader.render;

import javax.swing.*;
import java.io.File;

public class Main {

    public static void main(String[] args) {
        String filePath = null;
        System.out.println("[TextReader Render core] x86_64 version1.0");
        String encode = "UTF-8";
        // 遍历命令行参数
        for (int i = 0; i < args.length; i++) {
            if ("-f".equals(args[i]) || "-file".equals(args[i])) {
                if (i + 1 < args.length) { // 确保有下一个参数作为文件路径
                    filePath = args[i + 1];
                    System.out.println("File path specified: " + filePath);
                    break; // 找到后跳出循环
                } else {
                    System.out.println("Error: Missing file path after '-f' or '-file' parameter.");
                    return;
                }
            }
            if ("-e".equals(args[i]) || "-encode".equals(args[i])) {
                if (i + 1 < args.length) { // 确保有下一个参数作为文件路径
                    encode = args[i + 1];
                    System.out.println("Encode: " + encode);
                    break; // 找到后跳出循环
                } else {
                    System.out.println("Error: Missing encode after '-e' or '-encode' parameter.");
                    return;
                }
            }
        }
        JFrame frame = new JFrame("TextReader Render Core");
        DrawPage main_drawer = new DrawPage();
        if (filePath == null) {
            System.out.println("No file path provided.");

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setDialogTitle("选择一个TXT小说文件");
            int result = fileChooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                //JOptionPane.showMessageDialog(frame, "选择的文件: " + selectedFile.getAbsolutePath());
                main_drawer.push_data(new FontSet().init(selectedFile.getAbsolutePath(),encode, 30, 960, 540));//传入章节信息
            } else {
                frame.dispose();
                return;
            }
        }else {
            main_drawer.push_data(new FontSet().init(filePath,encode ,30, 960, 540));//传入章节信息
        }
        main_drawer.event();//激活时钟事件，按键监听
        main_drawer.setChangeListener(frame);
        frame.setSize(150,150);
        frame.add(main_drawer);
        frame.setSize(main_drawer.panelWidth, main_drawer.panelHeight);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}