package lib;

import com.swetake.util.Qrcode;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Qr {
	public static Graphics getQrCode(String path,String content) throws IOException {
		
		//创建一个Qrcode对象
		Qrcode qrcode = new Qrcode();
		
		//设置二维码的纠错能力
		qrcode.setQrcodeEncodeMode('M');
		//以二进制存储
		qrcode.setQrcodeErrorCorrect('B');
		//设置二进制版本
		qrcode.setQrcodeVersion(7);
		//字符编码
//		byte[] bt = new String(content.getBytes("ISO-8859-1"),"utf-8").getBytes();
		byte[] bt = content.getBytes("utf-8");
		//创建一个图像数据缓存区(创建一张纸出来)
		BufferedImage image = new BufferedImage(140,140,BufferedImage.TYPE_INT_RGB);
		//创建一支笔
		Graphics2D g = image.createGraphics();
		//设置二维码背景
		g.setBackground(Color.WHITE);
		//填充颜色
		g.fillRect(0,0,140,140);
		//设置二维码的前景色
		g.setColor(Color.BLACK);
		if(bt.length > 0) {
			boolean[][] b = qrcode.calQrcode(bt);
			for(int i = 0; i < b.length;i++) {
				for(int j = 0;j < b.length;j++) {
					if(b[j][i]) {
						g.fillRect(j * 3 + 2,i * 3 + 2, 3,3);
					}
				}
			}
		}
		File file = new File(path);
		ImageIO.write(image,"png", file);
		return g;
	}
	
	public static void main(String[] args) throws IOException {
		getQrCode("D:\\qrcode.png","nihao");
	}
}
