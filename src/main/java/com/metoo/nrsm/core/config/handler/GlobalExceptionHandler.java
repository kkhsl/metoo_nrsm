package com.metoo.nrsm.core.config.handler;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.metoo.nrsm.core.config.aop.idempotent.MyIllegalArgumentException;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.utils.exception.CustomRuntimeException;
import com.metoo.nrsm.core.utils.string.StringUtils;
import com.metoo.nrsm.core.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.apache.shiro.session.ExpiredSessionException;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;



/**
 * <p>
 *     Title: GlobalExceptionHandler.java
 * </p>
 *
 * <p>
 *     Description: 全局异常处理类, 增强的 Controller
 *     全局异常处理
 *     全局数据绑定
*      全局数据预处理
 * </p>
 *
 * <author>
 *     Hkk
 * </author>
 */
@Slf4j
@ControllerAdvice
@Order(2)
public class GlobalExceptionHandler {

//    private log log = logFactory.getlog(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public Object badArgumentHandler(IllegalArgumentException e){
        log.error(e.getMessage(),e);
        return ResponseUtil.badArgument();
    }



    @ExceptionHandler(MyIllegalArgumentException.class)
    @ResponseBody
    public Object badArgumentHandler2(MyIllegalArgumentException e){
        log.error(e.getMessage(),e);
        return ResponseUtil.badArgument(e.getMessage());
    }

    @ExceptionHandler(NullPointerException.class)
    @ResponseBody
    public Object badArgumentHandler(NullPointerException e){
        log.error(e.getMessage(), e);
        return ResponseUtil.nullPointException();
    }

    @ExceptionHandler(ArithmeticException.class)
    @ResponseBody
    public Object ArithmeticException(ArithmeticException e){
        log.error(e.getMessage(), e);
        return ResponseUtil.arithmeticException();
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Object seriousHandler(Exception e) {
        log.error(e.getMessage(), e);
        return ResponseUtil.serious();
    }

//    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
//    public Object HttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e){
//        log.error(e.getMessage(), e);
//        return ResponseUtil.httpRequestMethodNotSupportedException();
//    }

    @ExceptionHandler(FileNotFoundException.class)
    public Object fileNotFoundException(FileNotFoundException e){
        log.error(e.getMessage());
        return ResponseUtil.fileNotFoundException();
    }

    @ExceptionHandler(value = ExpiredSessionException.class)
    public Object handleExpiredSessionException(ExpiredSessionException e) {
        log.debug("ExpiredSessionException", e);
        return ResponseUtil.expired();
    }

    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    public Object handleException(HttpRequestMethodNotSupportedException e) {
        return ResponseUtil.error("不支持' " + e.getMethod() + "'请求");
    }

    @ExceptionHandler(value = HttpMediaTypeNotSupportedException.class)
    @ResponseBody
    public Object handleException(HttpMediaTypeNotSupportedException e) {
        MediaType contentType = e.getContentType();

        return ResponseUtil.error("不支持' " + contentType.getType() + "/" + contentType.getSubtype() + " 媒体类型");
    }

    @ExceptionHandler(value = HttpServerErrorException.class)
    @ResponseBody
    public Object httpServletErrorException(HttpServerErrorException e){
        System.out.println(e.getMessage());
        return ResponseUtil.badArgument("远程调用失败，检查参数是否正确");
    }

    @ExceptionHandler(value = NoSuchMethodError.class)
    @ResponseBody
    public Object NoSuchMethodError(NoSuchMethodError e){
        System.out.println(e.getMessage());
        return ResponseUtil.notFound();
    }

    @ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
    @ResponseBody
    public Object MethodArgumentTypeMismatchException(NoSuchMethodError e){
        return ResponseUtil.badArgument();
    }

//    @ExceptionHandler(value = NumberFormatException.class)
//    @ResponseBody
//    public Object NumberFormatException(NumberFormatException e){
//        return ResponseUtil.badArgument("数据类型错误");
//    }
//

    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    @ResponseBody
    public Object HttpMessageNotReadableException(HttpMessageNotReadableException e){
        System.out.println(e.getMessage());
        return ResponseUtil.notFound();
    }

//    @ExceptionHandler(value = MissingServletRequestParameterException.class)
//    @ResponseBody
//    public Object MissingServletRequestParameterException(MissingServletRequestParameterException e){
//        System.out.println(e.getMessage());
//        return ResponseUtil.missingParameter();
//    }
//
//    @ExceptionHandler(value = InvalidFormatException.class)
//    @ResponseBody
//    public Object InvalidFormatException(InvalidFormatException e){
//        System.out.println(e.getMessage());
//        return ResponseUtil.missingParameter();
//    }

//    @ExceptionHandler(value = ClientAbortException.class)
//    public void ClientAbortException(ClientAbortException e){
//        System.out.println(e.getMessage());
//        return ResponseUtil.missingparameter();
//    }



//    @ExceptionHandler(value = DataIntegrityViolationException.class)
//    @ResponseBody
//    public Object DataIntegrityViolationException(DataIntegrityViolationException e){
//        System.out.println(e.getMessage());
//        return ResponseUtil.badArgument("检查日期格式");
//    }







    // 捕捉shiro的异常
//    @ExceptionHandler(ShiroException.class)
//    public Object handleShiroException(ShiroException e) {
//        return ResponseUtil.badArgument(401, e.getMessage());
//    }
//
//    // 捕捉其他所有异常
//    @ExceptionHandler(Exception.class)
//    public Object globalException(HttpServletRequest request, Throwable ex) {
//        return ResponseUtil.badArgument(401, ex.getMessage());
//    }


    /**
     * 业务异常
     */
    @ExceptionHandler(CustomRuntimeException.class)
    @ResponseBody
    public Result handleServiceException(CustomRuntimeException e, HttpServletRequest request) {
        log.error(e.getMessage(), e);
        Integer code = e.getCode();
        return ResponseUtil.badArgument(code, e.getMessage());
    }
}
