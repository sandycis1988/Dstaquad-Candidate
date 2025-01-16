package com.profile.candidate.exceptions;

import com.profile.candidate.dto.CandidateResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handle CandidateNotFoundException
    @ExceptionHandler(CandidateNotFoundException.class)
    public ResponseEntity<CandidateResponseDto> handleCandidateNotFoundException(CandidateNotFoundException ex) {
        // Assuming candidateId, employeeId, and jobId are null by default, as per the previous DTO definition
        CandidateResponseDto response = new CandidateResponseDto(
                ex.getMessage(),  // Custom exception message
                null,  // candidateId = null
                null,  // employeeId = null
                null   // jobId = null
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND); // HTTP 404
    }
    // Handle InterviewAlreadyScheduledException
    @ExceptionHandler(InterviewAlreadyScheduledException.class)
    public ResponseEntity<CandidateResponseDto> handleInterviewAlreadyScheduledException(InterviewAlreadyScheduledException ex) {
        CandidateResponseDto response = new CandidateResponseDto(
                ex.getMessage(),  // Custom exception message
                null,  // candidateId = null
                null,  // employeeId = null
                null   // jobId = null
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); // HTTP 400 for invalid request
    }

    // Handle CandidateAlreadyExistsException
    @ExceptionHandler(CandidateAlreadyExistsException.class)
    public ResponseEntity<CandidateResponseDto> handleCandidateAlreadyExistsException(CandidateAlreadyExistsException ex) {
        // Assuming candidateId, employeeId, and jobId are null by default
        CandidateResponseDto response = new CandidateResponseDto(
                ex.getMessage(),  // Custom exception message
                null,  // candidateId = null
                null,  // employeeId = null
                null   // jobId = null
        );
        return new ResponseEntity<>(response, HttpStatus.CONFLICT); // HTTP 409
    }

    // Handle all other unchecked exceptions (generic fallback)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<CandidateResponseDto> handleRuntimeException(RuntimeException ex) {
        // Assuming candidateId, employeeId, and jobId are null by default
        CandidateResponseDto response = new CandidateResponseDto(
                "Internal server error occurred",  // Generic error message
                null,  // candidateId = null
                null,  // employeeId = null
                null   // jobId = null
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR); // HTTP 500
    }
}
