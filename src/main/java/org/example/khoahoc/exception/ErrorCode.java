package org.example.khoahoc.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Lỗi không xác định"),
    USER_NOT_FOUND(1001, "Người dùng không tồn tại"),
    USER_EXISTED(1002, "Người dùng đã tồn tại"),
    COURSE_NOT_FOUND(1003, "Khóa học không tồn tại"),
    CATEGORY_NOT_FOUND(1004, "Danh mục không tồn tại"),
    CHAPTER_NOT_FOUND(1005, "Chương không tồn tại"),
    LESSON_NOT_FOUND(1006, "Bài học không tồn tại"),
    ORDER_NOT_FOUND(1007, "Đơn hàng không tồn tại"),
    RESOURCE_NOT_FOUND(1008, "Tài liệu không tồn tại"),
    PAYMENT_TRANSACTION_NOT_FOUND(1009, "Giao dịch thanh toán không tồn tại"),
    TRANSACTION_ITEM_NOT_FOUND(1010, "Mục giao dịch không tồn tại"),
    ENROLLMENT_NOT_FOUND(1011, "Đăng ký khóa học không tồn tại"),
    LEARNING_PROGRESS_NOT_FOUND(1012, "Tiến độ học tập không tồn tại"),
    INVALID_CREDENTIALS(1013, "Tên đăng nhập hoặc mật khẩu không đúng"),
    INVALID_API_KEY(1014, "Api key không hợp lệ"),
    INVALID_SIGNATURE(1015, "Chữ ký không hợp lệ"),
    PAYMENT_ALREADY_PROCESSED(1016, "Giao dịch đã được xử lý"),
    GATEWAY_UNAVAILABLE(1017, "Cổng thanh toán không khả dụng"),
    ENROLLMENT_EXISTED(1018, "Người dùng đã đăng ký khóa học này"),
    INVALID_PAYMENT_STATUS(1019, "Trạng thái thanh toán không hợp lệ");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
