package org.trc.exception;

import org.trc.enums.ExceptionEnum;

/**
 * 品牌管理异常
 */
public class BrandException extends RuntimeException {

    /**
     * 异常枚举
     */
    private ExceptionEnum exceptionEnum;
    /**
     * 错误信息
     */
    private String message;

    public BrandException(ExceptionEnum exceptionEnum, String message) {
        super(message);
        this.exceptionEnum = exceptionEnum;
        this.message = message;
    }

    public ExceptionEnum getExceptionEnum() {
        return exceptionEnum;
    }

    public void setExceptionEnum(ExceptionEnum exceptionEnum) {
        this.exceptionEnum = exceptionEnum;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
