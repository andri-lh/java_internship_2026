package al.lhind.eventbooking.exception;

import al.lhind.eventbooking.dto.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<Object> invalidRequest(
            InvalidRequestException exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, exception.getMessage(), request, Map.of(), new HttpHeaders());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> notFound(
            ResourceNotFoundException exception, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage(), request, Map.of(), new HttpHeaders());
    }

    @ExceptionHandler(BusinessConflictException.class)
    public ResponseEntity<Object> conflict(
            BusinessConflictException exception, HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, exception.getMessage(), request, Map.of(), new HttpHeaders());
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<Object> forbidden(
            ForbiddenOperationException exception, HttpServletRequest request) {
        return response(HttpStatus.FORBIDDEN, exception.getMessage(), request, Map.of(), new HttpHeaders());
    }

    @ExceptionHandler(AuthenticationFailureException.class)
    public ResponseEntity<Object> authenticationFailure(
            AuthenticationFailureException exception, HttpServletRequest request) {
        return response(HttpStatus.UNAUTHORIZED, exception.getMessage(), request, Map.of(), new HttpHeaders());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> authenticationRequired(
            AuthenticationException exception, HttpServletRequest request) {
        return response(HttpStatus.UNAUTHORIZED, "Authentication required", request, Map.of(), new HttpHeaders());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> accessDenied(
            AccessDeniedException exception, HttpServletRequest request) {
        return response(HttpStatus.FORBIDDEN, "Access denied", request, Map.of(), new HttpHeaders());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> constraintViolation(
            ConstraintViolationException exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "Validation failed", request, Map.of(), new HttpHeaders());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.putIfAbsent(
                        error.getField(),
                        error.getDefaultMessage() == null ? "Invalid value" : error.getDefaultMessage()));

        return response(status, "Validation failed", servletRequest(request), fieldErrors, headers);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        if (status.is5xxServerError()) {
            return handleExceptionInternal(exception, null, headers, status, request);
        }
        return response(status, "Validation failed", servletRequest(request), Map.of(), headers);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        return response(status, "Malformed or missing request body", servletRequest(request), Map.of(), headers);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        return response(status, "Required parameter '" + exception.getParameterName() + "' is missing",
                servletRequest(request), Map.of(), headers);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        if (status.is5xxServerError()) {
            return handleExceptionInternal(exception, null, headers, status, request);
        }
        String message = exception instanceof MethodArgumentTypeMismatchException argument
                ? "Invalid value for '" + argument.getName() + "'"
                : "Invalid request value";
        return response(status, message, servletRequest(request), Map.of(), headers);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception exception,
            Object body,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        if (status.is5xxServerError()) {
            return unexpected(exception, servletRequest(request));
        }
        return response(status, clientMessage(status), servletRequest(request), Map.of(), headers);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> unexpectedException(
            Exception exception, HttpServletRequest request) {
        return unexpected(exception, request);
    }

    private ResponseEntity<Object> unexpected(Exception exception, HttpServletRequest request) {
        String path = request.getRequestURI().replace('\r', '_').replace('\n', '_');
        log.error("Unhandled request failure: method={}, path={}", request.getMethod(), path, exception);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred",
                request, Map.of(), new HttpHeaders());
    }

    private ResponseEntity<Object> response(
            HttpStatusCode status,
            String message,
            HttpServletRequest request,
            Map<String, String> validationErrors,
            HttpHeaders headers) {
        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                reasonPhrase(status),
                message,
                request.getRequestURI(),
                validationErrors);
        return new ResponseEntity<>(body, headers, status);
    }

    private String clientMessage(HttpStatusCode status) {
        return switch (status.value()) {
            case 400 -> "Invalid request";
            case 404 -> "Resource not found";
            case 405 -> "Method not allowed";
            case 415 -> "Unsupported media type";
            default -> reasonPhrase(status);
        };
    }

    private String reasonPhrase(HttpStatusCode status) {
        HttpStatus knownStatus = HttpStatus.resolve(status.value());
        return knownStatus == null ? "HTTP " + status.value() : knownStatus.getReasonPhrase();
    }

    private HttpServletRequest servletRequest(WebRequest request) {
        return ((ServletWebRequest) request).getRequest();
    }
}
