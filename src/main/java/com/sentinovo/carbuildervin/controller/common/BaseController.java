package com.sentinovo.carbuildervin.controller.common;

import com.sentinovo.carbuildervin.dto.common.PageResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class BaseController {

    protected <T> ResponseEntity<StandardApiResponse<T>> success(T data) {
        return ResponseEntity.ok(StandardApiResponse.success(data));
    }

    protected <T> ResponseEntity<StandardApiResponse<T>> success(T data, String message) {
        return ResponseEntity.ok(StandardApiResponse.success(data, message));
    }

    protected <T> ResponseEntity<StandardApiResponse<T>> created(T data) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(StandardApiResponse.success(data, "Resource created successfully"));
    }

    protected <T> ResponseEntity<StandardApiResponse<T>> created(T data, String message) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(StandardApiResponse.success(data, message));
    }

    protected ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }

    protected <T> ResponseEntity<StandardApiResponse<PageResponseDto<T>>> successPage(PageResponseDto<T> page) {
        return ResponseEntity.ok(StandardApiResponse.success(page));
    }

    protected <T> ResponseEntity<StandardApiResponse<PageResponseDto<T>>> successPage(PageResponseDto<T> page, String message) {
        return ResponseEntity.ok(StandardApiResponse.success(page, message));
    }

    protected <T> ResponseEntity<StandardApiResponse<Object>> error(String message, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(StandardApiResponse.error(message));
    }

    protected <T> ResponseEntity<StandardApiResponse<Object>> badRequest(String message) {
        return error(message, HttpStatus.BAD_REQUEST);
    }

    protected <T> ResponseEntity<StandardApiResponse<Object>> notFound(String message) {
        return error(message, HttpStatus.NOT_FOUND);
    }

    protected <T> ResponseEntity<StandardApiResponse<Object>> forbidden(String message) {
        return error(message, HttpStatus.FORBIDDEN);
    }

    protected <T> ResponseEntity<StandardApiResponse<Object>> conflict(String message) {
        return error(message, HttpStatus.CONFLICT);
    }
}