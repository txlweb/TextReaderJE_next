package com.teipreader.window;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class start_read {
    private static int extractNumber(String filename) {
        String number = filename.replaceAll("\\D", "");  // 去除非数字字符
        return number.isEmpty() ? 0 : Integer.parseInt(number);
    }
    public static String[] extractLastDirectoryAndParent(String path) {
        // 使用 File.separator 确保路径分隔符的兼容性
        String separator = File.separator;

        // 去除路径末尾可能的分隔符
        if (path.endsWith(separator)) {
            path = path.substring(0, path.length() - separator.length());
        }

        // 获取父目录路径
        int lastSeparatorIndex = path.lastIndexOf(separator);
        String parentPath = (lastSeparatorIndex == -1) ? "" : path.substring(0, lastSeparatorIndex);

        // 获取最后一级目录名称
        String lastDirectoryName = (lastSeparatorIndex == -1) ? path : path.substring(lastSeparatorIndex + 1);

        return new String[]{parentPath, lastDirectoryName};
    }
    public static void make_offer_file(String in){
        String[] result = extractLastDirectoryAndParent(in);
        String outputFilePath = result[0]+"/"+result[1]+".txt";
        if(new File(outputFilePath).isFile()){
            run_reader(outputFilePath);
            return;
        }
        if(new File(in+"/main.txt").isFile()){
            run_reader(in+"/main.txt");
            return;
        }
        System.out.println(in);
        // 获取目录中的所有txt文件
        File dir = new File(in);
        File[] txtFiles = dir.listFiles((d, name) -> name.endsWith(".txt"));

        if (txtFiles != null) {
            // 根据文件名中的数字部分进行排序
            Arrays.sort(txtFiles, (f1, f2) -> {
                // 提取文件名中的数字进行比较
                int num1 = extractNumber(f1.getName());
                int num2 = extractNumber(f2.getName());
                return Integer.compare(num1, num2);
            });

            // 拼接所有文件内容
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFilePath), Charset.forName("UTF-8")))) {
                for (File file : txtFiles) {
                    //System.out.println("Processing file: " + file.getName());
                    try {
                        // 获取当前文件的编码方式
                        String fileCharset = EncodingDetect.getJavaEncode(String.valueOf(file.toPath()));

                        // 使用正确的编码打开文件
                        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), Charset.forName(fileCharset))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                writer.write(line);
                                writer.newLine();  // 每行之间换行
                            }
                        }
                    } catch (IOException e) {
                        System.err.println("Error reading file: " + file.getName());
                    }
                }
                //System.out.println("Files merged successfully into: " + outputFilePath);
            } catch (IOException e) {
                System.err.println("Error writing to output file: " + outputFilePath);
            }
        } else {
            System.err.println("No .txt files found in the directory.");
        }
        run_reader(outputFilePath);
    }
    public static void run_reader(String file){
        // 定义 JAR 文件路径和参数
        String jarPath = "./render.jar";  // 替换为你的 JAR 文件路径
        List<String> command = new ArrayList<>();

        // Java 执行 JAR 的命令
        command.add("java");
        command.add("-jar");
        command.add(jarPath);

        // 添加额外的参数
        // 假设传入的参数是从 main 方法传递的
        command.add("-f");
        command.add(file);
        command.add("-e");
        command.add(EncodingDetect.getJavaEncode(file));

        // 使用 ProcessBuilder 执行命令
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        //processBuilder.inheritIO();  // 让当前进程继承 I/O 输出
        try {
            Process process = processBuilder.start();  // 启动进程
            //int exitCode = process.waitFor();  // 等待执行结束
            //System.out.println("execed，退出代码: " + exitCode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
