package com.profile.candidate.service;

import com.profile.candidate.dto.CandidateResponseDto;
import com.profile.candidate.exceptions.CandidateAlreadyExistsException;
import com.profile.candidate.model.CandidateDetails;
import com.profile.candidate.repository.CandidateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CandidateService {

    @Autowired
    private CandidateRepository candidateRepository;

    public CandidateResponseDto submitCandidate(CandidateDetails candidateDetails) {
        // Validate input fields
        validateCandidateDetails(candidateDetails);

        // Check for duplicates individually
        checkForDuplicates(candidateDetails);

        // Save the candidate details to the database
        CandidateDetails savedCandidate = candidateRepository.save(candidateDetails);

        // Create and return the response DTO
        return new CandidateResponseDto(
                "Candidate profile submitted successfully.",
                savedCandidate.getCandidateId(),
                savedCandidate.getUserId(),
                savedCandidate.getJobId()
        );
    }

    private void validateCandidateDetails(CandidateDetails candidateDetails) {
        if (candidateDetails.getFullName() == null || candidateDetails.getFullName().trim().isEmpty()) {
            throw new CandidateAlreadyExistsException("Full Name is required and cannot be empty.");
        }

        if (candidateDetails.getEmailId() == null || !candidateDetails.getEmailId().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new CandidateAlreadyExistsException("Invalid email format.");
        }

        if (candidateDetails.getContactNumber() == null || !candidateDetails.getContactNumber().matches("\\d{10}")) {
            throw new CandidateAlreadyExistsException("Contact number must be 10 digits.");
        }
    }

    private void checkForDuplicates(CandidateDetails candidateDetails) {
        // Check for duplicate Full Name
        Optional<CandidateDetails> existingFullName = candidateRepository.findByFullName(candidateDetails.getFullName());
        if (existingFullName.isPresent()) {
            throw new CandidateAlreadyExistsException("Candidate with the same full name already exists: " + existingFullName.get().getFullName());
        }

        // Check for duplicate Email ID
        Optional<CandidateDetails> existingEmailId = candidateRepository.findByEmailId(candidateDetails.getEmailId());
        if (existingEmailId.isPresent()) {
            throw new CandidateAlreadyExistsException("Candidate with the same email ID already exists: " + existingEmailId.get().getEmailId());
        }

        // Check for duplicate Contact Number
        Optional<CandidateDetails> existingContactNumber = candidateRepository.findByContactNumber(candidateDetails.getContactNumber());
        if (existingContactNumber.isPresent()) {
            throw new CandidateAlreadyExistsException("Candidate with the same contact number already exists: " + existingContactNumber.get().getContactNumber());
        }
    }
    public List<CandidateResponseDto> getSubmissions() {
        // Retrieve all candidates from the repository
        List<CandidateDetails> candidates = candidateRepository.findAll();

        // Convert the candidates to a list of CandidateResponseDto objects
        return candidates.stream().map(candidate -> new CandidateResponseDto(
                "Candidate retrieved successfully.",
                candidate.getCandidateId(),
                candidate.getUserId() != null ? candidate.getUserId() : "Not assigned",
                candidate.getJobId() != null ? candidate.getJobId() : "Not assigned"
        )).collect(Collectors.toList());
    }
}
