package Utils;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Dangelo on 2016/5/23.
 */
public class Storage {

    private List< Map<String, String> > list;

    /**
     * 获取硬盘的每个盘符
     */
    public List< Map<String, String> > getDriver(){
        // 当前文件系统类
        FileSystemView fsv = FileSystemView.getFileSystemView();
        // 列出所有windows 磁盘
        File [] files = File.listRoots();
        System.out.println(files.length);
        list = new ArrayList<>();
        // 显示磁盘卷标
        for (File file : files) {
            if(!fsv.getSystemDisplayName(file).equals("")){
                Map<String, String> map = new HashMap<>();
                map.put("diskName", fsv.getSystemDisplayName(file));
                map.put("totalSize", formatFileSize(file.getTotalSpace()));
                map.put("availableSize", formatFileSize(file.getFreeSpace()));
                list.add(map);
                System.out.print(fsv.getSystemDisplayName(file));
                System.out.print("总大小" + formatFileSize(file.getTotalSpace()) + " ");
                System.out.println("剩余" + formatFileSize(file.getFreeSpace()));
            }
        }
        return list;
    }

    public String formatFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString ;
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }
}
