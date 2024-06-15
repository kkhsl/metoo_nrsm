package com.metoo.nrsm.core.utils.file;

import com.metoo.nrsm.core.service.IAccessoryService;
import com.metoo.nrsm.core.service.IBackupSqlService;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.entity.Accessory;
import com.metoo.nrsm.entity.BackupSql;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLDecoder;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.*;

/**
 * <p>
 *     Title: FileUtil.java
 * </p>
 *
 * <p>
 *     Desciption: 文件工具管理类；
 *        负责创建文件
 *       文件夹权限修改等;
 * </p>
 *
 * <author>
 *     HKK
 * </author>
 */

@Component
public class FileUtil {


    @Autowired
    private IAccessoryService accessoryService;
    @Autowired
    private IBackupSqlService backupSqlService;

    /**
     * m3u8转mp4
     *
     * @param path
     * @return
     */
    public static boolean merge(String path, String playBack) throws FileNotFoundException {
        boolean flag = true;
        File file = new File(path);
        if (!file.exists()) {
            return false;
        }
        LinkedList<Integer> tsList = getTsNumber(file);
        if (tsList != null && tsList.size() > 0) {
            File playBackFile = new File(playBack);
            if (!playBackFile.exists() && !playBackFile.isDirectory()) {
                playBackFile.mkdirs();
            }
            String mergeFile = playBack + File.separator + "merge.ts";
            BufferedInputStream bis = null;
            Collections.sort(tsList);
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(mergeFile));

            for (Integer ts : tsList) {
                try {
                    bis = new BufferedInputStream(new FileInputStream(path + File.separator + ts + ".ts"));
                    byte[] bytes = new byte[1024];
                    int len = 0;
                    while ((len = bis.read(bytes)) > 0) {
                        bos.write(bytes, 0, len);
                        bos.flush();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException("第" + ts + "次循环IO流异常");
                }
            }
            // 删除原文件所有.ts文件、m3u8文件
            try {
                boolean delFlag = delFileTs(path);
                String m3u8 = path + File.separator + "index.m3u8";
                delFile(m3u8);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            return false;
        }
        return flag;
    }

    /**
     * 遍历目录下所有.ts文件
     *
     * @param file
     * @return
     */
    public static LinkedList<Integer> getTsNumber(File file) {
        File[] listFiles = file.listFiles();
        LinkedList<Integer> list = new LinkedList<>();
        for (File f : listFiles) {
            if (f.getName().endsWith(".ts")) {
                //fileName.substring(0, fileName.lastIndexOf("."));
                list.add(Integer.parseInt(f.getName().substring(0, f.getName().lastIndexOf("."))));
            }
        }
        return list;
    }

    /**
     * 删除文件
     * 删除当前目录下所有.ts文件 文件分片
     *
     * @param path
     * @return
     */
    public static boolean delFileTs(String path) {
        File file = new File(path);
        if (!file.exists() && !file.isDirectory()) {
            return false;
        }
        File[] files = file.listFiles();
        for (File f : files) {
            if (f.getName().endsWith(".ts")) {
                if (f.isFile() && file.exists()) {
                    f.delete();
                }
            }
        }
        return true;
    }

    // 删除垃圾文件
    public static void delFile(String path) {
        File file = new File(path);
        try {
            if (file.exists() && file.isFile()) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteAll(File file) {
        if (file.isFile() || file.list().length == 0) {
            file.delete();
        }else{
            for (File f : file.listFiles()) {
                deleteAll(f); // 递归删除每一个文件
            }
            file.delete(); // 删除文件夹
        }



        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 修改目录权限 off
     *
     * @param filePath
     * @throws IllegalStateException
     * @throws IOException
     */
    public static void storeFile(String filePath) throws IllegalStateException, IOException {
        File file = new File(filePath);
        if(!file.isDirectory() ){
            file.mkdirs();
        }

        //设置权限
        Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
        perms.add(PosixFilePermission.OWNER_READ);//设置所有者的读取权限
      /*  perms.add(PosixFilePermission.OWNER_WRITE);//设置所有者的写权限
        perms.add(PosixFilePermission.OWNER_EXECUTE);//设置所有者的执行权限*/
/*        perms.add(PosixFilePermission.GROUP_READ);//设置组的读取权限
        perms.add(PosixFilePermission.GROUP_EXECUTE);//设置组的读取权限
        perms.add(PosixFilePermission.OTHERS_READ);//设置其他的读取权限
        perms.add(PosixFilePermission.OTHERS_EXECUTE);//设置其他的读取权限*/
        try {
            //设置文件和文件夹的权限
            Path pathParent = Paths.get(file.getAbsolutePath());
            Files.setPosixFilePermissions(pathParent, perms);//修改文件夹路径的权限

            //Runtime.getRuntime().exec("chown -R www:www " + filePath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void storeFileOpen(String filePath) throws IllegalStateException, IOException {
        File file = new File(filePath);
        //设置权限
        Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
        perms.add(PosixFilePermission.OWNER_READ);//设置所有者的读取权限
        perms.add(PosixFilePermission.OWNER_WRITE);//设置所有者的写权限
        perms.add(PosixFilePermission.OWNER_EXECUTE);
        try {
            /*perms.add(PosixFilePermission.OWNER_EXECUTE);//设置所有者的执行权限*/
            perms.add(PosixFilePermission.GROUP_READ);//设置组的读取权限
            /*perms.add(PosixFilePermission.GROUP_EXECUTE);//设置组的读取权限*/
            perms.add(PosixFilePermission.OTHERS_READ);//设置其他的读取权限
            /*perms.add(PosixFilePermission.OTHERS_EXECUTE);//设置其他的执行权限*/
            //设置文件和文件夹的权限
            Path pathParent = Paths.get(file.getAbsolutePath());
            Files.setPosixFilePermissions(pathParent, perms);//修改文件夹路径的权限
            //Runtime.getRuntime().exec("chown -R www:www " + filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 修改文件/目录所有者
     */
    public static void possessor(String object){
        Path path = Paths.get(object);
        FileOwnerAttributeView foav = Files.getFileAttributeView(path,
                FileOwnerAttributeView.class);
        try {
            UserPrincipal owner = foav.getOwner();
            UserPrincipalLookupService upls = FileSystems.getDefault().getUserPrincipalLookupService();

            UserPrincipal newOwner = upls.lookupPrincipalByName("www");
            foav.setOwner(newOwner);

            UserPrincipal changedOwner = foav.getOwner();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    /**
     * 修改文件/目录所有组
     */
    public static void groups(){

    }

    /**
     * 上传路径， 上传文件
     *
     * @param path          文件保存路径
     * @param multipartFile 上传文件
     */

    public static boolean imageUpload(String path, MultipartFile multipartFile) {
        path = System.getProperty("user.dir");
        if (multipartFile == null && multipartFile.getSize() <= 0) {
            return false;
        }
        //文件名
        String originalName = multipartFile.getOriginalFilename();
        String fileName = UUID.randomUUID().toString().replace("-", "");
        String picNewName = fileName + originalName.substring(originalName.lastIndexOf("."));
        String imgRealPath = path + picNewName;

        try {
            //保存图片-将multipartFile对象装入image文件中
            File imageFile = new File(imgRealPath);
            multipartFile.transferTo(imageFile);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean uploadFile(MultipartFile file, String fileName,
                                String ext, String path){
//        boolean flag = this.createFile(file, fileName, path);
        boolean flag = false;
        File fil = new File(path + File.separator + fileName + File.separator + Global.DBNAME  + ext);
        if (!fil.getParentFile().exists()) {
            fil.getParentFile().mkdirs();
        }
        try {
            file.transferTo(fil);
            flag = true;
        } catch (IOException e) {
            e.printStackTrace();
            flag = false;
        }
        if(flag){
            String size = null;
            try {
                size = this.getSize(path + File.separator + fileName + File.separator + Global.DBNAME  + ext);
            } catch (Exception e) {
                e.printStackTrace();
            }
            boolean accessory = this.createBackupSql(fileName,size);
            if(accessory){
                return true;
            }else{
                // 删除文件
            }
        }
        return false;
    }

    public boolean createFile(MultipartFile file, String fileName, String path){
        File fil = new File(path + fileName);
        if (!fil.getParentFile().exists()) {
            fil.getParentFile().mkdirs();
        }
        try {
            file.transferTo(fil);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean createBackupSql(String fileName, String size){
        // 创建记录
//        BackupSql backupSql = this.backupSqlService.selectObjByName(fileName);
//        if(backupSql == null){
//            backupSql = new BackupSql();
//        }
        BackupSql backupSql = new BackupSql();
        backupSql.setName(fileName);
        backupSql.setSize(size);
        int i = this.backupSqlService.save(backupSql);
        if(i > 0){
            return true;
        }else{
            return false;
        }
    }

    public String getSize(String path){
        try {
            Process p = Runtime.getRuntime().exec("du -sh " + path);

            if (p.waitFor() == 0) {// 0 表示线程正常终止

                InputStream is = p.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String line;

                StringBuilder builder = new StringBuilder();

                while((line = reader.readLine())!= null){

                    builder.append(line);

                }

                try {
                    p.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                is.close();

                reader.close();

                p.destroy();

                if (builder.length()==0) {
                    return "";
                } else {
                    String str = builder.substring(0, builder.length() - System.lineSeparator().length());
                    if(str.indexOf("/") > -1){
                        return str.substring(0, str.indexOf("/")).trim();
                    }
                    return builder.substring(0, builder.length() - System.lineSeparator().length());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "";
    }

    public boolean createAccessory(String fileName, String ext, String path, int fileSize){
        Accessory accessory = new Accessory();
        accessory.setAddTime(new Date());
        accessory.setA_name(fileName);
        try {
            accessory.setA_path(URLDecoder.decode(path, "utf-8"));
            accessory.setA_ext(ext);
            accessory.setA_size(fileSize);
            int i = this.accessoryService.save(accessory);
            if(i > 0){
                return true;
            }
            return false;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }
    }
}
