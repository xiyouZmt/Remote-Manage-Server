package lib;

import com.swetake.util.Qrcode;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Qr {

	public static Graphics getQrCode(String path, String content) throws IOException {
		
		Qrcode qrcode = new Qrcode();																					//创建一个Qrcode对象
		qrcode.setQrcodeEncodeMode('M');																				//设置二维码的纠错能力
		qrcode.setQrcodeErrorCorrect('B');																				//以二进制存储
		qrcode.setQrcodeVersion(7);																						//设置二进制版本
		byte[] bt = content.getBytes("utf-8");																//字符编码
		BufferedImage image = new BufferedImage(140,140,BufferedImage.TYPE_INT_RGB);						//创建一个图像数据缓存区(创建一张纸出来)
		Graphics2D g = image.createGraphics();																			//创建一支笔
		g.setBackground(Color.WHITE);																					//设置二维码背景
		g.fillRect(0,0,140,140);																		//填充颜色
		g.setColor(Color.BLACK);																						//设置二维码的前景色
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
