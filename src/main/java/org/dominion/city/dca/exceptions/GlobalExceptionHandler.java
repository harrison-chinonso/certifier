package org.dominion.city.dca.exceptions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Objects;
import java.util.UUID;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {
	private ResponseEntity<ExceptionResponse<?>> getExceptionResponseResponseEntity(Throwable e, String errorMessage, String code, HttpStatus status, ContentCachingRequestWrapper request) {
		ExceptionResponse<?> exceptionResponse = ExceptionResponse.builder()
				.respBody(String.valueOf(UUID.randomUUID()))
				.respDescription(errorMessage)
				.respCode(code)
				.build();

		return new ResponseEntity<>(exceptionResponse, status);
	}

	@ExceptionHandler(DuplicateEntityException.class)
	public ResponseEntity<ExceptionResponse<?>> handleDuplicateEntityException(DuplicateEntityException ex, ContentCachingRequestWrapper request) {
		return getExceptionResponseResponseEntity(ex, ex.getMessage(), ex.getResponseCode(), ex.getStatus(), request);
	}


	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ExceptionResponse<?>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex, ContentCachingRequestWrapper request) {
		final String supportedMethods = String.join(", ", Objects.requireNonNullElse(ex.getSupportedMethods(), new String[]{}));
		final String message = String.format("%s, supported method(s) are : %s", ex.getMessage(), supportedMethods );
		return getExceptionResponseResponseEntity(ex, message, "96", HttpStatus.METHOD_NOT_ALLOWED, request);
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ExceptionResponse<?>> handleNoRecordException(ResourceNotFoundException ex, ContentCachingRequestWrapper request) {
		return getExceptionResponseResponseEntity(ex, ex.getMessage(), ex.getResponseCode(), ex.getStatus(), request);
	}

	@ExceptionHandler(PermissionException.class)
	public ResponseEntity<ExceptionResponse<?>> handlePermissionException(PermissionException ex, ContentCachingRequestWrapper request) {
		return getExceptionResponseResponseEntity(ex, ex.getMessage(), ex.getResponseCode(), ex.getStatus(), request);
	}

	@ExceptionHandler(UnauthorisedException.class)
	public ResponseEntity<ExceptionResponse<?>> handleUnauthorisedException(UnauthorisedException ex, ContentCachingRequestWrapper request) {
		return getExceptionResponseResponseEntity(ex, ex.getMessage(), ex.getResponseCode(), ex.getStatus(), request);
	}

	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<ExceptionResponse<?>> handleBadRequestExceptions(BadRequestException ex, ContentCachingRequestWrapper request) {
		return getExceptionResponseResponseEntity(ex, ex.getMessage(), ex.getResponseCode(), ex.getStatus(), request);
	}

	@ExceptionHandler(GeneralException.class)
	public ResponseEntity<ExceptionResponse<?>> handleGeneralException(GeneralException ex, ContentCachingRequestWrapper request) {
		return getExceptionResponseResponseEntity(ex, NestedExceptionUtils.getMostSpecificCause(ex).getMessage(), ex.getResponseCode(), ex.getStatus(), request);
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
	public ResponseEntity<ExceptionResponse<?>> handleException(final Exception ex, ContentCachingRequestWrapper request) {
		return getExceptionResponseResponseEntity(ex, NestedExceptionUtils.getMostSpecificCause(ex).getMessage(), "01", HttpStatus.INTERNAL_SERVER_ERROR, request);
	}

	@ExceptionHandler(SQLIntegrityConstraintViolationException.class)
	public ResponseEntity<?> handleSQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException ex, ContentCachingRequestWrapper request) {
		return getExceptionResponseResponseEntity(ex, NestedExceptionUtils.getMostSpecificCause(ex).getMessage(), "06", HttpStatus.BAD_REQUEST, request);

	}

	@ExceptionHandler(NumberFormatException.class)
	public ResponseEntity<?> handleNumberFormatExceptions(NumberFormatException ex, ContentCachingRequestWrapper request) {
		return getExceptionResponseResponseEntity(ex, NestedExceptionUtils.getMostSpecificCause(ex).getMessage(), "06", HttpStatus.BAD_REQUEST, request);
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<?> maxUploadSizeExceeded(MaxUploadSizeExceededException ex, ContentCachingRequestWrapper request) {
		return getExceptionResponseResponseEntity(ex, NestedExceptionUtils.getMostSpecificCause(ex).getMessage(), "06", HttpStatus.BAD_REQUEST, request);
	}
}
