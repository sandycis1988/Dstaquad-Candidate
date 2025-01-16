package com.profile.candidate.exceptions;

public class InterviewAlreadyScheduledException extends RuntimeException {
    public InterviewAlreadyScheduledException(String message) {
        super(message);
    }
}
