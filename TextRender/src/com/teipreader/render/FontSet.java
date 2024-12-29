package com.teipreader.render;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class FontSet extends Thread{
    private List<String> pages = new ArrayList<>();

    public List<String> getLineMap_title() {
        return lineMap_title;
    }

    public List<Integer> getLineMap_line() {
        return lineMap_line;
    }

    private List<String> lineMap_title = new ArrayList<>();
    private List<Integer> lineMap_line = new ArrayList<>();
    private String text_path = "[unknow file]";
    private boolean processed = false;
    private boolean need_run = false;
    private final Pattern ChapterPattern = Pattern.compile(".*第.{0,5}章.*|.*Chapter.*");
    private int font_size = 10;
    private int w = 150;
    private int _wf = 0;
    private int h = 150;
    private int i = 0;
    private String encode = "UTF-8";
    public int getFont_size() {
        return font_size;
    }

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }

    public FontSet init(String _text_path,String _encode, int _font_size,int _w,int _h){
        this.processed = false;
        this.need_run = true;
        this.text_path = _text_path;
        this.encode = _encode;
        this.w = _w;
        this.h = _h;
        this.font_size = _font_size;

        this.start();
        return this;
    }
    public void reprocess(int _font_size,int _w,int _h){


        this.w = _w;
        this.h = _h;
        this.font_size = _font_size;
        System.gc();
        int wf = w / (font_size) + 1;//横向文本数（左右各10编剧）
        if(!processed && _wf == wf){
            //System.out.println("[Style SYNC] [-]");
            return;//防止多次执行
        }
        this.processed = false;
        this.need_run = true;
    }
    public void run(){
        while (true) {
            while (!need_run){
                this.processed = true;
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            need_run = false;
            if (Objects.equals(this.text_path, "[unknow file]")) return;
            List<String> t_pages = new ArrayList<>();
            lineMap_title.clear();
            lineMap_line.clear();
            int wf = this.w / (this.font_size) ; // 每行最大字符数
            int ii = 0;
            this._wf=wf;

            try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(this.text_path)), "UTF-8"))) {

                StringBuilder currentLine = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) { // 按行读取文件内容

                    java.util.regex.Matcher matcher = ChapterPattern.matcher(line);
                    if (matcher.matches()) {
                        lineMap_title.add(line);
                        lineMap_line.add(ii);//t_pages.size()
                    }
                    for (char c : line.toCharArray()) {
                        currentLine.append(c);

                        // 如果当前行达到最大宽度
                        if (currentLine.length() >= wf) {
                            t_pages.add(currentLine.toString());
                            ii++;
                            currentLine = new StringBuilder(); // 清空当前行
                        }
                    }

                    // 每行末尾加换行处理
                    if (!(currentLine.toString().isEmpty())) {
                        t_pages.add(currentLine.toString());
                        ii++;
                        currentLine = new StringBuilder(); // 清空当前行
                    }
                }

                // 添加文件结束时的剩余内容
                if (!(currentLine.toString().isEmpty())) {
                    t_pages.add(currentLine.toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.pages.clear();
            this.pages = new ArrayList<>(t_pages);
            t_pages.clear();
            this.processed = true;
            //System.out.println("[Style SYNC] wf=" + wf + " [OK]");
        }
    }
    public boolean SetI(int index){
        if(index >= 0 && index < pages.size()) {
            i = index;
        }else{
            return false;
        }
        return true;
    }


    public String page(){
        //校验是否超下标，自增，返回当前
        i++;
        if(i > pages.size()) return "[IEX0x08:INDEX_OUT]";
        if(i-1 < 0 ) return pages.get(0);
        return pages.get(i-1);
    }
    public int getSize(){
        return pages.size();
    }
    public boolean isProcessed() {
        return processed;
    }
}
