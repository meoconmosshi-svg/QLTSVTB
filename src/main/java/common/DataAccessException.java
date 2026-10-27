package com.qlts.common;

/** Lỗi kỹ thuật khi làm việc với CSDL (bọc SQLException). */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
