package com.metoo.nrsm.core.utils.freemarker;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
public class FreemarkerUtil {

    public static void main(String[] args) {
        String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        System.out.println(path);
    }

    private static Configuration config;
    private static String serverPath;

    @Value("${spring.servlet.multipart.location:D:\\java\\project\\metoo\\freeMarker\\}")
    public void setServerPath(String serverPath) {
        FreemarkerUtil.serverPath = serverPath;
    }


    public void createHtmlToBrowser(String templateName, Object object, Writer writer){
        //创建fm的配置
        config = new Configuration();
        //指定默认编码格式
        config.setDefaultEncoding("UTF-8");
        //设置模版文件的路径
        try {
            config.setClassForTemplateLoading(PDFTemplateUtil.class, "/templates");

            //获得模版包
            Template template = config.getTemplate(templateName);

            //输出到浏览器
            //定义输出流，注意必须指定编码
//            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(path + "/" + targetFileName))));
//            Writer writer = new StringWriter();
            template.process(object, writer);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TemplateException e) {
            e.printStackTrace();
        }
    }
}
