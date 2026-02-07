package com.alertify.common.rest;

import com.alertify.common.exception.AlertifyException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AlertifyException.class)
    public ResponseEntity<ErrorResponse> handleAlertifyException(
            AlertifyException ex,
            WebRequest request
    ) {
        HttpStatus status = ex.getStatus();
        String path = request.getDescription(false).replace("uri=", "");

        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                path,
                LocalDateTime.now()
        );
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            WebRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String path = request.getDescription(false).replace("uri=", "");
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                "Validation Error",
                errorMessage,
                path,
                LocalDateTime.now()
        );
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler({
            ExpiredJwtException.class,
            SignatureException.class,
            MalformedJwtException.class
    })
    public ResponseEntity<ErrorResponse> handleJwtExceptions(Exception ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Authentication Failed",
                ex.getMessage(),
                null,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(
            WebRequest request
    ) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String path = request.getDescription(false).replace("uri=", "");

        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                "An unexpected error occurred.",
                path,
                LocalDateTime.now()
        );
        return ResponseEntity.status(status).body(errorResponse);
    }

}
