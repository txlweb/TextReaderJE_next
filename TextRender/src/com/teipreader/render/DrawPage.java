package com.teipreader.render;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;

public class DrawPage extends JPanel {

    int panelWidth = 150; // 面板宽度
    int panelHeight = 150; // 面板高度
    private Font font; // 设置字体
    private int angle = 0; // 旋转角度
    boolean enable_up_down = false;
    boolean up_out = false;
    boolean chapter_mode = false;
    boolean use_topbar = false;
    boolean down_out = false;
    boolean tts_on = false;
    int top = 0;
    int sum_top = 0;
    private static final int MENU_HEIGHT = 60;
    private final String[] menuItems = {"目录", "黑夜模式", "设置"};
    private int hoveredIndex = -1;
    int inline_top = 0;
    private long lastFrameTime = System.nanoTime();
    int select_line = 0;
    public FontSet DS() {
        return ds;
    }
    private float transitionProgress = 0f; // 渐变进度
    private Timer transitionTimer; // 用于处理渐变动画
    private boolean dragging = false;
    FontSet ds;
    int font_h  = 0;
    public void push_data(FontSet fs){
        setting.loadSettings();
        this.ds = fs;
        this.font = new Font(setting.fontID, Font.PLAIN, this.ds.getFont_size());
        this.panelWidth = this.ds.getW();
        this.panelHeight = this.ds.getH();
        transitionTimer = new Timer(20, ee -> {
            transitionProgress += 0.05f;
            if (transitionProgress >= 1f) {
                transitionProgress = 1f;
                transitionTimer.stop();
            }
        });
        startTransition();
    }
    public void reprocess(int _font_size,int _w,int _h){
        this.ds.reprocess(_font_size,_w,_h);
        this.font = new Font(setting.fontID, Font.PLAIN, this.ds.getFont_size());
        this.panelWidth = this.ds.getW();
        this.panelHeight = this.ds.getH();
    }
    public void reload(){
       this.reprocess(setting.fontSize,this.panelWidth,this.panelHeight);
    }
    public void SetTopPx(int px){
        //if(font_h==0) SetTopPx(px);
        top = px;
        sum_px();
    }
    public void SetTopLine(int line){
        top = (line-1)*(font_h);
        sum_px();
    }
    public void addTopPx(int px){
        top = top+px;
        sum_px();
        //System.out.println("top="+top+";sum_top="+sum_top+";inline_top="+inline_top);
    }

    private void sum_px() {
        if(!enable_up_down) {
            if (top < -font_h + 1) top = -font_h + 1;
            sum_top = top / font_h;
            if (top > ds.getSize() * font_h) top = ds.getSize() * font_h - 1;
            sum_top = top / font_h;
            inline_top = sum_top * font_h - top;
        } else {
            if (top < -font_h + 1){
                up_out = true;
            }
            if (top > ds.getSize() * font_h){
                down_out = true;
            }
        }
    }
    //当执行这个方法时，会启动带上下文模式，上超界返回“up_out”，下超界返回"down_out"，不超界返回"normal"
    public String getReactOut(){
        enable_up_down = true;
        if(up_out) return "up_out";
        if(down_out) return "down_out";
        return "normal";
    }
    //在这里创建事件回调，如果不需要可以不初始化它（只是显示一页文本）
    public void event() {
        setFocusable(true);
        // 添加鼠标滚轮监听器
        addMouseWheelListener(e -> {
            int rotation = e.getWheelRotation(); // 滚轮方向
            if(!chapter_mode){
                addTopPx(rotation*setting.rowRatio);
            }else {
                top_chapter += rotation*(getHeight()/(ds.getFont_size()+5)/2);
                if(top_chapter<0) top_chapter = 0;
                if(top_chapter>=ds.getLineMap_title().size()-getHeight()/(ds.getFont_size()+5)) top_chapter = ds.getLineMap_title().size()-getHeight()/(ds.getFont_size()+5)+1;
            }

            //repaint(); // 触发重绘
        });
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // 显示按下的键
                //System.out.println("Key Pressed: " + KeyEvent.getKeyText(e.getKeyCode())+"["+e.getKeyCode()+"]");
                //addTopPx(10000);

            }

            @Override
            public void keyReleased(KeyEvent e) {
                // 显示释放的键
                //System.out.println("Key Released: " + KeyEvent.getKeyText(e.getKeyCode()));
                // I 键打开设置
                if(e.getKeyCode() == 73){
                    SwingUtilities.invokeLater(setting::new);
                }
                // R 键刷新显示
                if(e.getKeyCode() == 82){
                    reload();
                }
                // C 键章节
                if(e.getKeyCode() == 67){
                    chapter_mode = !chapter_mode;
                }
            }
        });
        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    //System.out.println("Left mouse button pressed at: " + e.getPoint());
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    //System.out.println("Right mouse button pressed at: " + e.getPoint());
                }

                // 点击位置
                if (e.getX() >= panelWidth-30) {
                    dragging = true; // 启动拖动模式
                    SetTopPx(e.getY()/panelHeight*ds.getSize()*font_h);
                }
                if(chapter_mode){
                    chapter_mode = false;
                    SetTopLine(ds.getLineMap_line().get(select_line+top_chapter));
                    System.out.println(ds.getLineMap_title().get(select_line+top_chapter)+ds.getLineMap_line().get(select_line+top_chapter));
                }
                if(use_topbar){
                    System.out.println(hoveredIndex);
                    switch (hoveredIndex){
                        case 0:
                            chapter_mode = !chapter_mode;
                            break;
                        case 1:
                            if(Objects.equals(menuItems[1], "白日模式")){
                                menuItems[1] = "黑夜模式";
                            }else {
                                menuItems[1] = "白日模式";
                            }
                            startTransition();

                            break;
                        case 2:
                            SwingUtilities.invokeLater(setting::new);
                            break;
                    }

                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (dragging) {
                    //System.out.println("Mouse released, stopping drag at: " + e.getPoint());
                }
                dragging = false; // 停止拖动
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if(chapter_mode) {
                    use_topbar = false;
                    select_line = e.getY() / (ds.getFont_size() + 5);
                }else {
                    use_topbar = e.getY() < 60;
                    int widthPerItem = getWidth() / menuItems.length;
                    hoveredIndex = e.getX() / widthPerItem;
                }
            }
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragging) {
                    if(!chapter_mode) {
                        SetTopPx((int) ((e.getY() / (float) panelHeight) * ds.getSize() * font_h));
                    }
                    //System.out.println((e.getY() / (float) panelHeight) * ds.getSize() * font_h);
                }

            }


        });
        new Timer(16, e -> {
            angle += 5; // 每次增加5度
            if (angle >= 360) {
                angle = 0; // 复位角度
            }
            repaint(); // 触发重绘
            font = new Font(setting.fontID, Font.PLAIN, ds.getFont_size());
        }).start();
    }

    public void setChangeListener(JFrame frame){
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // 调整面板大小和位置
                int width = frame.getWidth();
                int height = frame.getHeight();
                //适应大小
                reprocess(setting.fontSize,width ,height);
                setBounds(0, 0, width, height);

            }
        });
    }
    public int getPageLine(){return (int) Math.ceil(panelHeight/(float)font_h);}
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if(chapter_mode){
            draw_chapters(g2d);
        }else {
            draw_reader(g2d);
        }


    }
    private int top_chapter = 0;
    void draw_chapters(Graphics2D g2d){
        if(!Objects.equals(menuItems[1], "白日模式")) {
            setBackground(Color.BLACK);
            setForeground(Color.WHITE);
        }else {
            setBackground(Color.WHITE);
            setForeground(Color.DARK_GRAY);
        }
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setFont(font);
        int sum_lineHeight = ds.getFont_size()+5;
        if(ds.getLineMap_title().size()<1){
            g2d.drawString("目录建立失败！",5,50);
            return;
        }
        for (int i = 0; i < getHeight()/sum_lineHeight-1; i++) {
            if(select_line == i){
                g2d.setColor(Color.red);
            }
            g2d.drawString(ds.getLineMap_title().get(i+top_chapter),5,(i+1)*sum_lineHeight-5);
            if(!Objects.equals(menuItems[1], "白日模式")) {
                g2d.setColor(Color.white);
            }else {
                g2d.setColor(Color.DARK_GRAY);
            }

            g2d.drawLine(0,i*sum_lineHeight,getWidth(),i*sum_lineHeight);
        }
        g2d.setColor(Color.GRAY);
        g2d.fillRect(getWidth()-20,(int)((top_chapter/(float)ds.getLineMap_title().size())*getHeight())-8,10,16);
    }
    private Color blendColors(Color c1, Color c2, float ratio) {
        int red = (int) (c1.getRed() * (1 - ratio) + c2.getRed() * ratio);
        int green = (int) (c1.getGreen() * (1 - ratio) + c2.getGreen() * ratio);
        int blue = (int) (c1.getBlue() * (1 - ratio) + c2.getBlue() * ratio);
        return new Color(red, green, blue);
    }
    private void startTransition() {
        transitionProgress = 0f;
        transitionTimer.start();
    }
    void draw_reader(Graphics2D g2d){
        Color startColor = Objects.equals(menuItems[1], "白日模式") ? Color.DARK_GRAY : Color.WHITE;
        Color endColor = Objects.equals(menuItems[1], "白日模式") ? Color.WHITE : Color.DARK_GRAY;
        Color currentBackground = blendColors(startColor, endColor, transitionProgress);
        setBackground(currentBackground);

        long currentFrameTime = System.nanoTime();
        double deltaTime = (currentFrameTime - lastFrameTime) / 1_000_000_000.0; // 转换为秒
        lastFrameTime = currentFrameTime;
        double fps = 1.0 / deltaTime;
        if(Objects.equals(menuItems[1], "白日模式")) {
            setForeground(Color.BLACK);
            g2d.setColor(Color.BLACK);
        }else {
            setForeground(Color.WHITE);
            g2d.setColor(Color.WHITE);
        }

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setFont(font);

        int h_ix = g2d.getFontMetrics().getHeight();
        font_h = h_ix;
        int y = inline_top;
        if(!this.ds.SetI(sum_top)) {
            g2d.drawString("[IEX0x08:INDEX_OUT] 所选取的区段超出文章长度极限!（数组超下标）", 50, 50);
            return;
        }
        if(setting.enableAutoSync) {
            String fpsText = String.format("FPS: %.2f", fps);
            g2d.drawString(fpsText, 10, 30);
        }
        for (int i = 0; i < panelHeight/font_h+2; i++) {
            String s = this.ds.page();
            if(!Objects.equals(s, "[IEX0x08:INDEX_OUT]")) {
                g2d.drawString(s, 5, y);
            }else {
                return;
            }
            y += h_ix; // 行间距
        }
        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        //如果正在计算字体，播放加载动画
        if(!this.ds.isProcessed()){
            // 计算圆环的大小
            int radius = Math.min(width, height) / 4;
            int thickness = 10; // 圆环厚度
            // 设置绘图颜色
            g2d.setColor(Color.BLUE);
            // 绘制旋转的扇形（等待效果）
            g2d.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawArc(centerX - radius, centerY - radius, radius * 2, radius * 2, angle, 90); // 画一个扇形
        }
        //绘制进度条
        g2d.setColor(Color.GRAY);
        g2d.fillRect(width-20,(int)((sum_top/(float)ds.getSize())*height)-8,10,16);
        //System.out.println(sum_top+","+ds.getSize()+","+(int)(sum_top/(float)ds.getSize())*height);
        if(use_topbar){
            g2d.fillRect(0,0,getWidth(),60);

            int widthPerItem = getWidth() / menuItems.length;

            for (int i = 0; i < menuItems.length; i++) {
                int x = i * widthPerItem;

                if (i == hoveredIndex) {
                    g2d.setColor(Color.LIGHT_GRAY);
                    g2d.fillRect(x, 0, widthPerItem, MENU_HEIGHT);
                }

                g2d.setColor(i == hoveredIndex ? Color.BLACK : Color.DARK_GRAY);
                g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(menuItems[i]);
                int textX = x + (widthPerItem - textWidth) / 2;
                int textY = (MENU_HEIGHT + fm.getAscent() + 20) / 2;
                g2d.drawString(menuItems[i], textX, textY);

                drawIcon(g2d, i, x + (widthPerItem / 2 - 10), 10, i == hoveredIndex);
            }
        }
    }

    private void drawIcon(Graphics2D g2d, int index, int x, int y, boolean hovered) {
        Color color = hovered ? Color.BLACK : Color.DARK_GRAY;
        g2d.setColor(color);
        switch (index) {
            case 0: // Directory icon
                g2d.fillRect(x, y, 20, 3);
                g2d.fillRect(x, y + 7, 20, 3);
                g2d.fillRect(x, y + 14, 20, 3);
                break;
            case 1: // Night mode icon (crescent moon)
                g2d.fillArc(x, y, 20, 20, 45, 360);
                if(Objects.equals(menuItems[1], "白日模式")) {
                    g2d.setColor(Color.red);
                }else {
                    g2d.setColor(Color.yellow);

                }
                g2d.fillArc(x+2, y+2, 16, 16, 95, 180);
                break;
            case 2: // Settings icon (gear)
                g2d.drawOval(x, y, 20, 20);
                for (int i = 0; i < 8; i++) {
                    double angle = i * Math.PI / 4;
                    int gearX = x + 10 + (int) (Math.cos(angle) * 12);
                    int gearY = y + 10 + (int) (Math.sin(angle) * 12);
                    g2d.fillRect(gearX - 2, gearY - 2, 4, 4);
                }
                g2d.fillOval(x + 6, y + 6, 8, 8);
                break;
        }
    }

}