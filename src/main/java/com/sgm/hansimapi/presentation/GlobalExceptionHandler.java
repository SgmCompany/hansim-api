package com.sgm.hansimapi.presentation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(new ErrorResponse(message));
    }

    // CompletableFuture 내부 예외 언래핑
    @ExceptionHandler(CompletionException.class)
    public ResponseEntity<ErrorResponse> handleCompletion(CompletionException e) {
        Throwable cause = e.getCause() != null ? e.getCause() : e;

        if (cause instanceof IllegalArgumentException) {
            return ResponseEntity.badRequest().body(new ErrorResponse(cause.getMessage()));
        }
        if (cause instanceof HttpClientErrorException.TooManyRequests) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new ErrorResponse("Riot API 호출 한도를 초과했습니다. 잠시 후 다시 시도해주세요."));
        }
        if (cause instanceof HttpClientErrorException http) {
            return ResponseEntity.status(http.getStatusCode())
                    .body(new ErrorResponse("Riot API 오류: " + http.getStatusText()));
        }

        log.error("배치 처리 중 예외 발생", cause);
        return ResponseEntity.internalServerError()
                .body(new ErrorResponse("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
    }

    public record ErrorResponse(String message) {}
}
