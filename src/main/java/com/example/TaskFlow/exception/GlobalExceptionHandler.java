package com.example.TaskFlow.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Central place for translating server side exceptions into consistent JSON responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                 HttpServletRequest request) {
        Map<String, List<String>> validationErrors = collectFieldErrors(ex.getBindingResult().getFieldErrors());
        ex.getBindingResult().getGlobalErrors().forEach(error ->
                validationErrors.computeIfAbsent(error.getObjectName(), key -> new ArrayList<>())
                        .add(error.getDefaultMessage()));

        ApiError apiError = ApiError.of(HttpStatus.BAD_REQUEST, "Validation failed", request.getRequestURI(), validationErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiError> handleBindException(BindException ex, HttpServletRequest request) {
        Map<String, List<String>> validationErrors = collectFieldErrors(ex.getBindingResult().getFieldErrors());
        ex.getBindingResult().getGlobalErrors().forEach(error ->
                validationErrors.computeIfAbsent(error.getObjectName(), key -> new ArrayList<>())
                        .add(error.getDefaultMessage()));

        ApiError apiError = ApiError.of(HttpStatus.BAD_REQUEST, "Validation failed", request.getRequestURI(), validationErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex,
                                                              HttpServletRequest request) {
        Map<String, List<String>> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.groupingBy(this::resolveViolationPath, LinkedHashMap::new,
                        Collectors.mapping(ConstraintViolation::getMessage, Collectors.toList())));

        ApiError apiError = ApiError.of(HttpStatus.BAD_REQUEST, "Validation failed", request.getRequestURI(), errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                       HttpServletRequest request) {
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "required type";
        String message = "Parameter '" + ex.getName() + "' should be of type " + requiredType;
        ApiError apiError = ApiError.of(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParameter(MissingServletRequestParameterException ex,
                                                           HttpServletRequest request) {
        String message = "Missing required parameter '" + ex.getParameterName() + "'";
        ApiError apiError = ApiError.of(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                 HttpServletRequest request) {
        log.debug("Malformed request payload for {}", request.getRequestURI(), ex);
        ApiError apiError = ApiError.of(HttpStatus.BAD_REQUEST, "Malformed JSON request", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                             HttpServletRequest request) {
        String message = "Request method '" + ex.getMethod() + "' not supported";
        Map<String, Object> details = new LinkedHashMap<>();
        Set<?> supported = ex.getSupportedHttpMethods();
        if (supported != null && !supported.isEmpty()) {
            details.put("supportedMethods", supported);
        }
        ApiError apiError = ApiError.of(HttpStatus.METHOD_NOT_ALLOWED, message, request.getRequestURI(), details);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(apiError);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
                                                                HttpServletRequest request) {
        String message = "Media type '" + ex.getContentType() + "' not supported";
        Map<String, Object> details = new LinkedHashMap<>();
        if (!ex.getSupportedMediaTypes().isEmpty()) {
            details.put("supportedMediaTypes", ex.getSupportedMediaTypes());
        }
        ApiError apiError = ApiError.of(HttpStatus.UNSUPPORTED_MEDIA_TYPE, message, request.getRequestURI(), details);
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(apiError);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatus(ResponseStatusException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String message = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        ApiError apiError = ApiError.of(status, message, request.getRequestURI());
        return ResponseEntity.status(status).body(apiError);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        ApiError apiError = ApiError.of(HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiError);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        ApiError apiError = ApiError.of(HttpStatus.FORBIDDEN, "Access is denied", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiError);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(DataIntegrityViolationException ex,
                                                                 HttpServletRequest request) {
        Throwable mostSpecificCause = ex.getMostSpecificCause();
        String logMessage = mostSpecificCause != null && mostSpecificCause.getMessage() != null
                ? mostSpecificCause.getMessage()
                : ex.getMessage();
        log.warn("Data integrity violation at {}: {}", request.getRequestURI(), logMessage);
        ApiError apiError = ApiError.of(HttpStatus.CONFLICT, "Database constraint violated", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class, NoSuchElementException.class})
    public ResponseEntity<ApiError> handleClientErrors(RuntimeException ex, HttpServletRequest request) {
        ApiError apiError = ApiError.of(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error processing request {}", request.getRequestURI(), ex);
        ApiError apiError = ApiError.of(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }

    private Map<String, List<String>> collectFieldErrors(List<FieldError> fieldErrors) {
        Map<String, List<String>> errors = new LinkedHashMap<>();
        for (FieldError fieldError : fieldErrors) {
            errors.computeIfAbsent(fieldError.getField(), key -> new ArrayList<>())
                    .add(fieldError.getDefaultMessage());
        }
        return errors;
    }

    private String resolveViolationPath(ConstraintViolation<?> violation) {
        if (violation.getPropertyPath() == null) {
            return "";
        }
        return violation.getPropertyPath().toString();
    }
}
