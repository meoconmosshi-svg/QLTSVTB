package com.qlts.common;

/** Lỗi nghiệp vụ / dữ liệu nhập sai: message có thể hiển thị thẳng cho người dùng. */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
