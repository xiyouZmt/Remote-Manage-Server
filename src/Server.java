/**
 * Created by Dangelo on 2016/5/17.
 */

import Utils.Mouse;
import Utils.NetWorkIP;
import Utils.Storage;
import json.JSONException;
import json.JSONObject;
import lib.Qr;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Server {

    private static JButton beginButton;
    private static JButton stopButton;
    private static Color  mColor;
    private static NetWorkIP netWorkIP;
    public static void main(String[] args) {
        JFrame serverJFrame = new JFrame();
        init();
        JPanel panel2 = new JPanel(new BorderLayout(3,0));
        JPanel panel3 = new JPanel(new FlowLayout());
        beginButton.addActionListener(arg0 -> {
            new Thread(new ServerReceiverThread()).start();
            beginButton.setBackground(Color.WHITE);
            stopButton.setBackground(mColor);
            beginButton.setEnabled(false);
            stopButton.setEnabled(true);
        });
        stopButton.addActionListener(arg0 -> {
            try {
                ServerReceiverThread.ss.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            beginButton.setBackground(mColor);
            stopButton.setBackground(Color.WHITE);
            beginButton.setEnabled(true);
            stopButton.setEnabled(false);

        });
        panel3.add(beginButton);
        panel3.add(stopButton);

        /**
         * 手机IP与端口号JPanel
         */
        JPanel mJPanel = new JPanel();
        mJPanel.setLayout(new GridLayout(1,0));
        mJPanel.setSize(300, 50);

        JPanel panel1 = new JPanel(new FlowLayout());

        netWorkIP = new NetWorkIP();
        JLabel IPLabel;
        if(netWorkIP.getIP().equals("暂无网络连接")) {
            IPLabel = new JLabel(netWorkIP.getIP());
            beginButton.setEnabled(false);
        } else {
            IPLabel = new JLabel("网络IP: " + netWorkIP.getIP());
        }
        panel1.add(IPLabel);
        mJPanel.add(panel1);
        JPanel panel4 = new JPanel(new FlowLayout());
        JLabel label4 = new JLabel("请扫描二维码");
        panel4.add(label4);

        /**
         * 生成二维码面板
         */
        JPanel qrPanel = new JPanel(){
            @Override
            protected void paintComponent(Graphics g) {
                if(!netWorkIP.getIP().equals("暂无网络连接")) {
                    try {
                        System.out.println(netWorkIP.getIP());
                        Qr.getQrCode("image/qr",Server.netWorkIP.getIP());
                    } catch (IOException e) {
                        System.out.println(e.toString());
                    }
                    ImageIcon icon = new ImageIcon("image/qr");
                    Image image = icon.getImage().getScaledInstance(100,100, icon.getImage().SCALE_DEFAULT);
                    ImageIcon icon1 = new ImageIcon(image);
                    g.drawImage(icon1.getImage(),getSize().width/2 - 50,getSize().height/2 - 50, this);
                }
            }
        };
        qrPanel.setPreferredSize(new Dimension(100,100));
        JPanel panel5 = new JPanel(new BorderLayout(2,0));
        panel5.setPreferredSize(new Dimension(300,100));
        panel5.add(qrPanel,BorderLayout.CENTER);
        panel5.add(panel4,BorderLayout.SOUTH);

        /**
         * 使用说明
         */
        JPanel mUserJPanel = new JPanel(new GridLayout(6,0));
        JLabel mLabel1 = new JLabel("1. 在客户端输入网络IP地址;");
        JLabel mLabel2 = new JLabel("2. 点击开启，开启该服务端，等待客户端连接；");
        JLabel mLabel3 = new JLabel("3. 点击停止，关闭该服务端。");

        mUserJPanel.add(new JLabel());
        mUserJPanel.add(mLabel1);
        mUserJPanel.add(new JLabel());
        mUserJPanel.add(mLabel2);
        mUserJPanel.add(new JLabel());
        mUserJPanel.add(mLabel3);

        panel2.add(mJPanel,BorderLayout.NORTH);
        panel2.add(panel5,BorderLayout.CENTER);
        panel2.add(panel3,BorderLayout.SOUTH);

        serverJFrame.setLayout(new BorderLayout(2,0));
        serverJFrame.add(panel2,BorderLayout.CENTER);
        serverJFrame.add(mUserJPanel,BorderLayout.SOUTH);
        serverJFrame.setSize(300, 350);
        serverJFrame.setLocationRelativeTo(null);
        serverJFrame.setResizable(false);
        serverJFrame.setVisible(true);
    }

    private static void init() {
        beginButton = new JButton("开启");
        stopButton = new JButton("停止");
        stopButton.setEnabled(false);
        mColor = stopButton.getBackground();
    }
}

class ServerReceiverThread implements Runnable{

    static ServerSocket ss = null;

    public void run() {
        try {
            ss = new ServerSocket(30000);
            System.out.println("服务端创建成功!");
            while(true) {
                Socket socket = ss.accept();
                System.out.println("客户端连接成功!");
                System.out.println(socket.getLocalSocketAddress());
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                StringBuilder str = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    str.append(line);
                }
                if(str.toString().equals("")){
                    continue;
                }
                JSONObject jsonObject = new JSONObject(str.toString());
                if(jsonObject.has("command")) {                     //传输命令
                    switch (jsonObject.get("command").toString()){
                        case "power" :
                        case "speech" :
                            if(jsonObject.get("type").toString().equals("music")){
                                Runtime.getRuntime().exec("program/Dan Gibson - Nature's Path 自然小径.mp3");
                            } else {
                                Runtime.getRuntime().exec(jsonObject.get("type").toString());
                            }
                            break;
                        case "volume" :
                            volumeOperation(jsonObject.get("type").toString());
                            break;
                        case "brightness" :
                            brightnessOperation(jsonObject.get("type").toString());
                            break;
                        case "mouse" :
                            mouseOperation(str.toString());
                            break;
                        case "driver" :
                            if(jsonObject.get("operation").equals("execute")){
                                /**
                                 * 打开文件
                                 */
                                String path = jsonObject.get("path").toString();
                                Runtime.getRuntime().exec("cmd /c start \"\" \"" + path + "\"");
                            } else {
                                Socket driverSocket = ss.accept();
                                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(driverSocket.getOutputStream()));
                                OutputStream os;
                                StringBuilder builder = new StringBuilder("{\"result\":\"success\",");
                                Storage storage = new Storage();
                                switch (jsonObject.get("operation").toString()){
                                    case "getDisk" :
                                        List<Map<String, String>> list = storage.getDriver();
                                        builder.append("\"driver\":[");
                                        for (int i = 0; i < list.size(); i++) {
                                            builder.append("{")
                                                    .append("\"diskName\":\"")
                                                    .append(list.get(i).get("diskName"))
                                                    .append("\",\"totalSize\":\"")
                                                    .append(list.get(i).get("totalSize"))
                                                    .append("\",\"availableSize\":\"")
                                                    .append(list.get(i).get("availableSize"));
                                            if (i == list.size() - 1) {
                                                builder.append("\"}]}");
                                            } else {
                                                builder.append("\"},");
                                            }
                                        }
                                        writer.write(builder.toString());
                                        writer.close();
                                        break;
                                    case "getFile" :
                                        File [] files = new File(jsonObject.get("path").toString()).listFiles();
                                        if(files != null && files.length != 0){
                                            System.out.println(files.length);
                                            builder.append("\"file\":[");
                                            for (int i = 0; i < files.length; i++) {
                                                System.out.println(files[i].getPath());
                                                if(files[i].canRead()) {
                                                    builder.append("{")
                                                            .append("\"fileName\":\"")
                                                            .append(files[i].getName())
                                                            .append("\",\"filePath\":\"")
                                                            .append(files[i].getAbsolutePath());
                                                    if (files[i].isFile() && files[i].canWrite()) {
                                                        FileInputStream fis = new FileInputStream(files[i]);
                                                        builder.append("\",\"fileStyle\":\"")
                                                                .append("file")
                                                                .append("\",\"fileLength\":\"")
                                                                .append(storage.formatFileSize(fis.available()));
                                                    } else {
                                                        builder.append("\",\"fileStyle\":\"")
                                                                .append("directory")
                                                                .append("\",\"fileLength\":\"");
                                                        File [] childFiles = files[i].listFiles();
                                                        if (childFiles != null) {
                                                            builder.append(childFiles.length);
                                                        } else {
                                                            builder.append("0");
                                                        }
                                                    }
                                                    if (i == files.length - 1) {
                                                        builder.append("\"}]}");
                                                    } else {
                                                        builder.append("\"},");
                                                    }
                                                }
                                            }
                                        } else {
                                            builder.append("\"file\":\"null\"}");
                                        }
                                        writer.write(builder.toString());
                                        writer.close();
                                        break;
                                    case "download" :
                                        /**
                                         * 下载文件
                                         */
                                        os = driverSocket.getOutputStream();
                                        File file = new File(jsonObject.get("path").toString());
                                        InputStream is = new FileInputStream(file.getPath());
                                        byte [] b = new byte[1024 * 10];
                                        int c;
                                        while ((c = is.read(b)) > 0){
                                            os.write(b, 0, c);
                                        }
                                        os.close();
                                        is.close();
                                        break;
                                }
                                driverSocket.close();
                            }
                            break;
                        case "tools" :
                            Runtime.getRuntime().exec(jsonObject.get("type").toString());
                            break;
                        case "screenShot":
                            Mouse mouse = new Mouse();
                            BufferedImage image = mouse.screenShot();
                            File file = new File("screenshot");
                            if(!file.exists()){
                                file.mkdirs();
                            }
                            System.out.println(file.getPath());
                            mouse.saveScreenShot(image, file.getAbsolutePath() + "\\screenshot-" + getTime() + ".png");
                            break;
                    }
                } else {                                            //传输文件
                    String fileName = jsonObject.get("fileName").toString();
                    File file = new File("download");
                    if(!file.exists()){
                        file.mkdir();                               //新建文件夹
                    }
                    /**
                     * 字节流读取文件
                     */
                    Socket fileSocket = ss.accept();
                    InputStream is = fileSocket.getInputStream();
                    FileOutputStream os = new FileOutputStream(file.getPath() + '/' + fileName);
                    byte[] c = new byte[1024 * 100];
                    int b;
                    while ((b = is.read(c)) > 0) {
                        os.write(c, 0, b);
                    }
                    is.close();
                    os.close();
                    fileSocket.close();
                }
                reader.close();
                socket.close();
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    private void mouseOperation(String data){
        Mouse mouse = new Mouse();
        try {
            JSONObject object = new JSONObject(data);
            switch (object.get("type").toString()){
                case "move" :
                    mouse.move(Integer.parseInt(object.get("width").toString()),
                            Integer.parseInt(object.get("height").toString()));
                    break;
                case "singleClick" :
                    mouse.singleClick();
                    break;
                case "doubleClick" :
                    mouse.doubleClick();
                    break;
                case "rightClick" :
                    mouse.rightClick();
                    break;
                case "wheel" :
                    mouse.wheel(object.get("direction").toString());
                    break;
                case "up" :
                    mouse.direction("up");
                    break;
                case "down" :
                    mouse.direction("down");
                    break;
                case "left" :
                    mouse.direction("left");
                    break;
                case "right" :
                    mouse.direction("right");
                    break;
                case "home" :
                    mouse.direction("home");
                    break;
                case "end" :
                    mouse.direction("end");
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void volumeOperation(String type){
        try {
            System.out.println("cmd /c start program/ClickMonitorDDC.exe volume " + type);
            Runtime.getRuntime().exec("cmd /c start  program/ClickMonitorDDC.exe volume " + type);
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }

    private void brightnessOperation(String type){
        try {
            System.out.println("cmd /c start program/ClickMonitorDDC.exe volume " + type);
            Runtime.getRuntime().exec("cmd /c start program/ClickMonitorDDC.exe brightness " + type);
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }

    private String getTime(){
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        return simpleDateFormat.format(date);
    }

}