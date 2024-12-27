package com.profile.candidate.service;

import com.profile.candidate.dto.CandidateGetResponseDto;
import com.profile.candidate.dto.CandidateResponseDto;
import com.profile.candidate.dto.GetInterviewResponseDto;
import com.profile.candidate.dto.InterviewResponseDto;
import com.profile.candidate.exceptions.CandidateAlreadyExistsException;
import com.profile.candidate.exceptions.CandidateNotFoundException;
import com.profile.candidate.model.CandidateDetails;
import com.profile.candidate.repository.CandidateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CandidateService {

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private InterviewEmailService emailService;

    // Method to submit a candidate profile
    public CandidateResponseDto submitCandidate(CandidateDetails candidateDetails) {
        // Validate input fields
        validateCandidateDetails(candidateDetails);

        // Check for duplicates
        checkForDuplicates(candidateDetails);

        // Optionally set userEmail and clientEmail if not already set
        setDefaultEmailsIfMissing(candidateDetails);

        // Save the candidate details to the database
        CandidateDetails savedCandidate = candidateRepository.save(candidateDetails);

        // Return response DTO
        return new CandidateResponseDto(
                "Candidate profile submitted successfully.",
                savedCandidate.getCandidateId(),
                savedCandidate.getUserId(),
                savedCandidate.getJobId()
        );
    }

    // Validate required candidate fields
    private void validateCandidateDetails(CandidateDetails candidateDetails) {
        if (candidateDetails.getFullName() == null || candidateDetails.getFullName().trim().isEmpty()) {
            throw new CandidateAlreadyExistsException("Full Name is required and cannot be empty.");
        }

        if (candidateDetails.getCandidateEmailId() == null || !candidateDetails.getCandidateEmailId().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new CandidateAlreadyExistsException("Invalid email format.");
        }

        if (candidateDetails.getContactNumber() == null || !candidateDetails.getContactNumber().matches("\\d{10}")) {
            throw new CandidateAlreadyExistsException("Contact number must be 10 digits.");
        }


    }

    // Check for duplicate candidate based on Full Name, Email ID, and Contact Number
    private void checkForDuplicates(CandidateDetails candidateDetails) {
        // Check for duplicate Full Name

        // Check for duplicate Email ID
        Optional<CandidateDetails> existingEmailId = candidateRepository.findByCandidateEmailId(candidateDetails.getCandidateEmailId());
        if (existingEmailId.isPresent()) {
            throw new CandidateAlreadyExistsException("Candidate with the same email ID already exists: " + existingEmailId.get().getCandidateEmailId());
        }

        // Check for duplicate Contact Number
        Optional<CandidateDetails> existingContactNumber = candidateRepository.findByContactNumber(candidateDetails.getContactNumber());
        if (existingContactNumber.isPresent()) {
            throw new CandidateAlreadyExistsException("Candidate with the same contact number already exists: " + existingContactNumber.get().getContactNumber());
        }
    }

    // Set default values for userEmail and clientEmail if not provided
    private void setDefaultEmailsIfMissing(CandidateDetails candidateDetails) {
        if (candidateDetails.getUserEmail() == null) {
            candidateDetails.setUserEmail(candidateDetails.getUserEmail());  // Set to default or handle differently
        }

        if (candidateDetails.getClientEmail() == null) {
            candidateDetails.setClientEmail(candidateDetails.getClientEmail());  // Set to default or handle differently
        }
    }

    // Method to get all candidate submissions
    public List<CandidateDetails> getSubmissions() {
        // Retrieve all candidates from the repository
        return candidateRepository.findAll();
    }

    // Method to get candidate submissions by userId
    public List<CandidateGetResponseDto> getSubmissionsByUserId(String userId) {
        // Retrieve candidates by userId from the repository
        List<CandidateDetails> candidates = candidateRepository.findByUserId(userId);

        // If no candidates are found, throw a CandidateNotFoundException
        if (candidates.isEmpty()) {
            throw new CandidateNotFoundException("No submissions found for userId: " + userId);
        }

        // Map the list of CandidateDetails to List<CandidateGetResponseDto>
        List<CandidateGetResponseDto> candidateDtos = candidates.stream()
                .map(CandidateGetResponseDto::new)  // Convert each CandidateDetails to CandidateGetResponseDto
                .collect(Collectors.toList());

        // Return the list of CandidateGetResponseDto
        return candidateDtos;
    }

    public boolean isCandidateValidForUser(String userId, String candidateId) {
        // Fetch the candidate by candidateId
        CandidateDetails candidateDetails = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new CandidateNotFoundException("Candidate not found"));

        // Check if the userId associated with the candidate matches the provided userId
        if (!candidateDetails.getUserId().equals(userId)) {
            return false; // Candidate does not belong to the provided userId
        }

        return true; // Candidate is valid for the user
    }

    // Method to schedule an interview for a candidate

    public InterviewResponseDto scheduleInterview(String userId, String candidateId, LocalDateTime interviewDateTime, Integer duration,
                                                  String zoomLink, String userEmail, String clientEmail,
                                                  String clientName, String interviewLevel) {

        System.out.println("Starting to schedule interview for userId: " + userId + " and candidateId: " + candidateId);

        // Validate if candidateId is null
        if (candidateId == null) {
            System.err.println("Candidate ID cannot be null for userId: " + userId);
            throw new CandidateNotFoundException("Candidate ID cannot be null for userId: " + userId);
        }

        System.out.println("Candidate ID passed: " + candidateId);


        // Find candidate by userId and candidateId
        Optional<CandidateDetails> optionalCandidate = candidateRepository.findByCandidateIdAndUserId(candidateId, userId);

        if (optionalCandidate.isEmpty()) {
            System.err.println("No candidate found for userId: " + userId + " and candidateId: " + candidateId);
            throw new CandidateNotFoundException("Candidate not found for userId: " + userId + " and candidateId: " + candidateId);
        }

        CandidateDetails candidate = optionalCandidate.get();

        System.out.println("Found candidate: " + candidate);

        // Set userEmail and clientEmail from interview request if provided
        if (userEmail != null && !userEmail.isEmpty()) {
            candidate.setUserEmail(userEmail);
        }
        if (clientEmail != null && !clientEmail.isEmpty()) {
            candidate.setClientEmail(clientEmail);
        }

        // Ensure userEmail and clientEmail are set (default them if missing)
        setDefaultEmailsIfMissing(candidate);

        // Update interview fields
        candidate.setInterviewDateTime(interviewDateTime);
        candidate.setDuration(duration);
        candidate.setTimestamp(LocalDateTime.now());
        candidate.setZoomLink(zoomLink);

        // Set the new fields (clientName and interviewLevel)
        if (clientName != null && !clientName.isEmpty()) {
            candidate.setClientName(clientName);
        }
        if (interviewLevel != null && !interviewLevel.isEmpty()) {
            candidate.setInterviewLevel(interviewLevel);
        }

        // Save the updated candidate details to the database
        try {
            candidateRepository.save(candidate);
            System.out.println("Candidate saved successfully.");
        } catch (Exception e) {
            System.err.println("Error while saving candidate: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error while saving candidate data.");
        }

        // Create the email subject and body
        String subject = "Interview Scheduled for " + candidate.getFullName();
        String body = "<p>Dear " + candidate.getFullName() + ",</p>"
                + "<p>We are pleased to inform you that your interview has been scheduled on <b>" + interviewDateTime + "</b>.</p>"
                + "<p>Please find the details of your interview below:</p>"
                + "<ul>"
                + "<li><b>Zoom Link:</b> <a href='" + zoomLink + "'>Join the Interview</a></li>"
                + "<li><b>Duration:</b> " + duration + " minutes</li>"
                + "<li><b>Client:</b> " + clientName + "</li>"
                + "<li><b>Interview Level:</b> " + interviewLevel + "</li>"
                + "</ul>"
                + "<p>We look forward to your participation. Please let us know if you have any questions or need further assistance.</p>"
                + "<p>Best regards,</p>"
                + "<p>The Interview Team</p>";

        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body);

        // Send email to Candidate, Client, and User
        emailService.sendInterviewNotification(candidate.getCandidateEmailId(), subject, body);
        emailService.sendInterviewNotification(candidate.getClientEmail(), subject, body);
        emailService.sendInterviewNotification(candidate.getUserEmail(), subject, body);

        // Create the payload for the response
        InterviewResponseDto.InterviewPayload payload = new InterviewResponseDto.InterviewPayload(
                candidate.getCandidateId(),
                candidate.getUserEmail(),
                candidate.getCandidateEmailId(),
                candidate.getClientEmail()
        );

        // Return response DTO with success = true
        return new InterviewResponseDto(true,
                "Interview scheduled successfully and email notifications sent.",
                payload,
                null);  // No errors
    }

    public List<GetInterviewResponseDto> getAllScheduledInterviews(String userId) {
        // Assume you fetch interview records from your repository
        List<CandidateDetails> candidates = candidateRepository.findByUserId(userId);

        // List to store mapped response DTOs
        List<GetInterviewResponseDto> response = new ArrayList<>();

        // Map each Interview entity to GetInterviewResponseDto
        for (CandidateDetails interview : candidates) {
            GetInterviewResponseDto dto = new GetInterviewResponseDto(
                    interview.getJobId(),
                    interview.getCandidateId(),
                    interview.getFullName(),
                    interview.getContactNumber(),
                    interview.getCandidateEmailId(),
                    interview.getUserEmail(),
                    interview.getUserId(),
                    interview.getInterviewDateTime(),
                    interview.getDuration(),
                    interview.getZoomLink(),
                    interview.getTimestamp(),
                    interview.getClientEmail(),
                    interview.getClientName(),
                    interview.getInterviewLevel()
            );
            response.add(dto);
        }

        return response;
    }

}


