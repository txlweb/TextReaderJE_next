package com.teipreader.window;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.Base64;
import java.util.Objects;
import java.util.Properties;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class Main {
    static String noimg_base64 = "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";
    public static void main(String[] args) {
        // 创建主窗口
        JFrame frame = new JFrame("列表查看器");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        // 创建主面板
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));  // 纵向排列

        // 遍历指定文件夹
        File rootDir = new File("./rom"); // 替换为实际路径
        if (rootDir.exists() && rootDir.isDirectory()) {
            for (File subDir : Objects.requireNonNull(rootDir.listFiles(File::isDirectory))) {
                File iniFile = new File(subDir, "resource.ini");
                System.out.println(iniFile.getPath());
                if (iniFile.exists()) {
                    try {
                        // 读取配置文件
                        Properties properties = new Properties();
                        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(iniFile), EncodingDetect.getJavaEncode(iniFile.getPath()))) {
                            properties.load(reader);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                        String iconPath = properties.getProperty("icon", "icon.jpg");
                        String title = properties.getProperty("title", "无标题");
                        String author = properties.getProperty("by", "未知作者");
                        String description = properties.getProperty("ot", "无简介");

                        BufferedImage image = decodeBase64ToImage(noimg_base64);
                        File iconFile = new File(subDir, iconPath);
                        if (iconFile.exists()) {
                            image = ImageIO.read(iconFile);
                        }
                        image = resizeImage(image, 180, 240);
                        // 创建子面板
                        JPanel itemPanel = new JPanel();
                        itemPanel.setLayout(new BorderLayout());
                        itemPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                        // 添加图片
                        JLabel imageLabel = new JLabel(new ImageIcon(image));
                        itemPanel.add(imageLabel, BorderLayout.WEST);

                        // 添加文本信息面板
                        JPanel textPanel = new JPanel();
                        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
                        textPanel.setAlignmentX(Component.LEFT_ALIGNMENT); // 确保文本左对齐

                        // 添加标题
                        JLabel titleLabel = new JLabel("<html>" + title.replaceAll("\n", "<br>") + "</html>");
                        titleLabel.setFont(new Font("Serif", Font.BOLD, 20));
                        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);  // 左对齐
                        textPanel.add(titleLabel);

                        // 添加作者
                        JLabel authorLabel = new JLabel("作者: " + author);
                        authorLabel.setFont(new Font("Serif", Font.PLAIN, 14));
                        authorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);  // 左对齐
                        textPanel.add(authorLabel);

                        // 添加简介
                        JTextArea descriptionArea = new JTextArea("简介: " + description);
                        descriptionArea.setFont(new Font("Serif", Font.ITALIC, 12));
                        descriptionArea.setLineWrap(true);
                        descriptionArea.setWrapStyleWord(true);
                        descriptionArea.setEditable(false);
                        descriptionArea.setOpaque(false);
                        descriptionArea.setAlignmentX(Component.LEFT_ALIGNMENT);  // 左对齐
                        textPanel.add(descriptionArea);
                        itemPanel.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                start_read.make_offer_file(subDir.getPath());
                            }
                        });
                        itemPanel.add(textPanel, BorderLayout.CENTER);
                        panel.add(itemPanel);  // 将子面板添加到主面板

                        // 强制刷新布局
                        panel.revalidate();
                        panel.repaint();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // 将 panel 添加到滚动窗格
        JScrollPane scrollPane = new JScrollPane(panel);  // 使 panel 可滚动
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);  // 始终显示垂直滚动条
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);  // 不显示水平滚动条
        frame.add(scrollPane);  // 将滚动窗格添加到主窗口中

        // 显示窗口
        frame.setVisible(true);
    }
    public static BufferedImage decodeBase64ToImage(String base64String) {
        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64String);
            return ImageIO.read(new java.io.ByteArrayInputStream(imageBytes));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    // 缩放图片至指定的宽度和高度
    public static BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        Image tmp = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        // 创建一个新的 BufferedImage，将缩放后的图像绘制到新的 BufferedImage 中
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return resizedImage;
    }
}
