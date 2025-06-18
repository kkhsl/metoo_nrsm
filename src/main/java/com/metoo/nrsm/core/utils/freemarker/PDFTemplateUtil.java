package com.metoo.nrsm.core.utils.freemarker;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;

/**
 * Freemarker转PDF
 */
@Component
public class PDFTemplateUtil {

    private static String serverPath;

    @Value("${spring.servlet.multipart.location:D:\\java\\project\\metoo\\freeMarker\\}")
    public void setServerPath(String serverPath) {
        PDFTemplateUtil.serverPath = serverPath;
    }

}
