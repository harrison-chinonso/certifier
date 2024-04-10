/*
 * *
 *  * Created by Kolawole Omirin
 *  * Copyright (c) 2023 . All rights reserved.
 *  * Last modified 11/15/23, 3:23 PM
 *
 */

package com.line.medusa_merchant.exceptions;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.line.medusa_merchant.generic.dto.ExceptionResponse;
import com.line.medusa_merchant.generic.dto.MedusaMerchantResponse;
import com.line.medusa_merchant.generic.dto.ResponseConstants;
import com.line.medusa_merchant.utils.utilities.SlackService;
import com.line.medusa_merchant.utils.utilities.UserUtils;
import liquibase.pro.packaged.N;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.*;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {
	private final SlackService slackService;
	private static final String NEW_LINE = "\n";

	private ResponseEntity<ExceptionResponse<?>> getExceptionResponseResponseEntity(Throwable e, String errorMessage, String code, HttpStatus status, ContentCachingRequestWrapper request) {
		ExceptionResponse<?> exceptionResponse = ExceptionResponse.builder()
				.respBody(String.valueOf(UUID.randomUUID()))
				.respDescription(errorMessage)
				.respCode(code)
				.build();

		String reqBody = new String(request.getContentAsByteArray());
		String [] error = setErrorDetails(e);

		String message = "SOURCE CLASS :: "+error[1]+NEW_LINE+
				"SOURCE METHOD :: "+error[0]+NEW_LINE+
				"REQUEST BODY :: "+reqBody+NEW_LINE+
				"INITIATOR :: "+UserUtils.loggedInUser().getUsername()+NEW_LINE+
				"ERROR MESSAGE ::"+errorMessage;
		slackService.sendMessageToSlack( message, "#app-error-notification", "EXCEPTION");
		return new ResponseEntity<>(exceptionResponse, status);
	}

	private String[] setErrorDetails(final Throwable throwable) {
		Throwable rootCause = throwable;
		while (rootCause.getCause() != null && rootCause.getCause() != rootCause)
			rootCause = rootCause.getCause();

		return new String[]{rootCause.getStackTrace()[0].getMethodName(), rootCause.getStackTrace()[0].getClassName()};
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<MedusaMerchantResponse<?>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, ContentCachingRequestWrapper request) {
		
		List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
		List<ObjectError> globalErrors = ex.getBindingResult().getGlobalErrors();
		List<String> errors = new ArrayList<>(fieldErrors.size() + globalErrors.size());
		String error;
		for (FieldError fieldError : fieldErrors) {
			error = fieldError.getField() + ", " + fieldError.getDefaultMessage();
			errors.add(error);
		}
		for (ObjectError objectError : globalErrors) {
			error = objectError.getObjectName() + ", " + objectError.getDefaultMessage();
			errors.add(error);
		}

		return ResponseEntity
				.badRequest()
				.body(new MedusaMerchantResponse<>(
					"96",
					"arguement violations",
					errors,
					HttpStatus.BAD_REQUEST)
				);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<MedusaMerchantResponse<?>> handleConstraintViolationException(final ConstraintViolationException ex, ContentCachingRequestWrapper request) {
		List<Map<String, Object>> violations = new ArrayList<>();
		for (var v : ex.getConstraintViolations()) {
			Map<String, Object> map = new HashMap<>();

			map.put("object", v.getRootBeanClass().getSimpleName());
			map.put("field", v.getPropertyPath().toString());
			map.put("rejectedValue", v.getInvalidValue());
			map.put("message", v.getMessage());
			violations.add(map);
		}

		return ResponseEntity
				.badRequest()
				.body(new MedusaMerchantResponse<>(
								"96",
								"Constraint violations",
								violations,
								HttpStatus.BAD_REQUEST
						)
				);
	}

	@ExceptionHandler(DuplicateEntityException.class)
	public ResponseEntity<ExceptionResponse<?>> handleDuplicateEntityException(DuplicateEntityException ex, ContentCachingRequestWrapper request) {
		return getExceptionResponseResponseEntity(ex, ex.getMessage(), ex.getResponseCode(), ex.getStatus(), request);
	}

	@ExceptionHandler(MakerCheckerException.class)
	public ResponseEntity<ExceptionResponse<?>> handleMakerCheckerException(MakerCheckerException ex, ContentCachingRequestWrapper request) {
		return getExceptionResponseResponseEntity(ex, ex.getMessage(), ex.getResponseCode(), ex.getStatus(), request);
	}

	@ExceptionHandler(WalletBalanceErrorExceptions.class)
	public ResponseEntity<ExceptionResponse<?>> handleWalletBalanceErrorException(WalletBalanceErrorExceptions ex, ContentCachingRequestWrapper request) {
		return getExceptionResponseResponseEntity(ex, ex.getMessage(), ex.getResponseCode(), ex.getStatus(), request);
	}

	@ExceptionHandler(MerchantDirectorDetailsAlreadyCaptured.class)
	public ResponseEntity<ExceptionResponse<?>> handleMerchantDirectorDetailsAlreadyCapturedException(MerchantDirectorDetailsAlreadyCaptured ex, ContentCachingRequestWrapper request) {
		return getExceptionResponseResponseEntity(ex, ex.getMessage(), ex.getResponseCode(), ex.getStatus(), request);
	}

	@ExceptionHandler(MerchantPinAlreadyCaptured.class)
	public ResponseEntity<ExceptionResponse<?>> handleMerchantPinAlreadyCapturedException(MerchantPinAlreadyCaptured ex, ContentCachingRequestWrapper request) {
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

	@ExceptionHandler(OTPExceptions.class)
	public ResponseEntity<ExceptionResponse<?>> handleOTPExceptions(OTPExceptions ex, ContentCachingRequestWrapper request) {
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

	@ExceptionHandler(PinIncorrectExceptions.class)
	public ResponseEntity<ExceptionResponse<?>> handlePinIncorrectExceptions(PinIncorrectExceptions ex, ContentCachingRequestWrapper request) {
		return getExceptionResponseResponseEntity(ex, ex.getMessage(), ex.getResponseCode(), ex.getStatus(), request);
	}

	@ExceptionHandler(TokenExpiredException.class)
	public ResponseEntity<ExceptionResponse<?>> handlePinIncorrectExceptions(TokenExpiredException ex, ContentCachingRequestWrapper request) {
		return getExceptionResponseResponseEntity(ex,ex.getMessage(), String.valueOf(HttpStatus.UNAUTHORIZED.value()), HttpStatus.UNAUTHORIZED, request);
	}

	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<ExceptionResponse<?>> handleBadRequestExceptions(BadRequestException ex, ContentCachingRequestWrapper request) {
		return getExceptionResponseResponseEntity(ex, ex.getMessage(), ex.getResponseCode(), ex.getStatus(), request);
	}

	@ExceptionHandler(RemoteServiceException.class)
	public ResponseEntity<ExceptionResponse<?>> handleRemoteServiceException(RemoteServiceException ex, ContentCachingRequestWrapper request) {
		return getExceptionResponseResponseEntity(ex, ex.getMessage(), ResponseConstants.GENERAL, HttpStatus.BAD_GATEWAY, request);
	}

	@ExceptionHandler(UpdateExceptions.class)
	public ResponseEntity<ExceptionResponse<?>> handleUpdateExceptions(UpdateExceptions ex, ContentCachingRequestWrapper request) {
		return getExceptionResponseResponseEntity(ex, ex.getMessage(), ex.getResponseCode(), ex.getStatus(), request);
	}

	@ExceptionHandler(WalletCreationException.class)
	public ResponseEntity<ExceptionResponse<?>> handleWalletCreationException(WalletCreationException ex, ContentCachingRequestWrapper request) {
		return getExceptionResponseResponseEntity(ex, ex.getMessage(), ex.getResponseCode(), ex.getStatus(), request);
	}

	@ExceptionHandler(EntityExistsException.class)
	public ResponseEntity<ExceptionResponse<?>> handleEntityExistException(final EntityExistsException e, ContentCachingRequestWrapper request) {
		return getExceptionResponseResponseEntity(e, e.getMessage(), ResponseConstants.FAILED_CODE, HttpStatus.BAD_REQUEST, request);
	}

	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<ExceptionResponse<?>> handleEntityNotFoundException(final EntityNotFoundException e, ContentCachingRequestWrapper request) {
		return getExceptionResponseResponseEntity(e, e.getMessage(), ResponseConstants.FAILED_CODE, HttpStatus.BAD_REQUEST, request);
	}

	@ExceptionHandler(PermissionDeniedDataAccessException.class)
	public ResponseEntity<ExceptionResponse<?>> handlePermissionDeniedDataAccessException(final PermissionDeniedDataAccessException ex, ContentCachingRequestWrapper request) {
		return getExceptionResponseResponseEntity(ex, ex.getMessage(), ResponseConstants.PERMISSION, HttpStatus.FORBIDDEN, request);
	}

	@ExceptionHandler(SpringSecurityException.class)
	public ResponseEntity<ExceptionResponse<?>> handleSpringSecurityException(final SpringSecurityException ex, ContentCachingRequestWrapper request) {
		return getExceptionResponseResponseEntity(ex, NestedExceptionUtils.getMostSpecificCause(ex).getMessage(), ResponseConstants.PERMISSION, HttpStatus.UNAUTHORIZED, request);
	}

	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<ExceptionResponse<?>> handleValidationException(final ValidationException ex,  ContentCachingRequestWrapper request) {
		return getExceptionResponseResponseEntity(ex, NestedExceptionUtils.getMostSpecificCause(ex).getMessage(), ResponseConstants.FAILED_CODE, HttpStatus.UNAUTHORIZED, request);
	}

	@ExceptionHandler(GeneralException.class)
	public ResponseEntity<ExceptionResponse<?>> handleGeneralException(GeneralException ex, ContentCachingRequestWrapper request) {
		return getExceptionResponseResponseEntity(ex, NestedExceptionUtils.getMostSpecificCause(ex).getMessage(), ex.getResponseCode(), ex.getStatus(), request);
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
	public ResponseEntity<ExceptionResponse<?>> handleException(final Exception ex, ContentCachingRequestWrapper request) {
		return getExceptionResponseResponseEntity(ex, NestedExceptionUtils.getMostSpecificCause(ex).getMessage(), ResponseConstants.FAILED_CODE, HttpStatus.INTERNAL_SERVER_ERROR, request);
	}

	@ExceptionHandler(SQLIntegrityConstraintViolationException.class)
	public ResponseEntity<?> handleSQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException ex, ContentCachingRequestWrapper request) {
		return getExceptionResponseResponseEntity(ex, NestedExceptionUtils.getMostSpecificCause(ex).getMessage(), ResponseConstants.BAD_REQUEST, HttpStatus.BAD_REQUEST, request);

	}
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<?> handleDataIntegrityViolationException(DataIntegrityViolationException ex, ContentCachingRequestWrapper request) {
		return getExceptionResponseResponseEntity(ex, NestedExceptionUtils.getMostSpecificCause(ex).getMessage(), ResponseConstants.BAD_REQUEST, HttpStatus.BAD_REQUEST, request);
	}
	@ExceptionHandler(NumberFormatException.class)
	public ResponseEntity<?> handleNumberFormatExceptions(NumberFormatException ex, ContentCachingRequestWrapper request) {
		return getExceptionResponseResponseEntity(ex, NestedExceptionUtils.getMostSpecificCause(ex).getMessage(), ResponseConstants.BAD_REQUEST, HttpStatus.BAD_REQUEST, request);
	}
	@ExceptionHandler(JWTVerificationException.class)
	public ResponseEntity<?> handleNumberFormatExceptions(JWTVerificationException ex, ContentCachingRequestWrapper request) {
		return getExceptionResponseResponseEntity(ex, NestedExceptionUtils.getMostSpecificCause(ex).getMessage(), ResponseConstants.PERMISSION, HttpStatus.UNAUTHORIZED, request);
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<?> maxUploadSizeExceeded(MaxUploadSizeExceededException ex, ContentCachingRequestWrapper request) {
		return getExceptionResponseResponseEntity(ex, NestedExceptionUtils.getMostSpecificCause(ex).getMessage(), ResponseConstants.BAD_REQUEST, HttpStatus.BAD_REQUEST, request);
	}
}
