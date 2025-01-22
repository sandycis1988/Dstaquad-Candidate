package com.profile.candidate.service;

import com.profile.candidate.dto.CandidateGetResponseDto;
import com.profile.candidate.dto.CandidateResponseDto;
import com.profile.candidate.dto.GetInterviewResponseDto;
import com.profile.candidate.dto.InterviewResponseDto;
import com.profile.candidate.exceptions.CandidateAlreadyExistsException;
import com.profile.candidate.exceptions.CandidateNotFoundException;
import com.profile.candidate.exceptions.InterviewAlreadyScheduledException;
import com.profile.candidate.exceptions.InvalidFileTypeException;
import com.profile.candidate.model.CandidateDetails;
import com.profile.candidate.repository.CandidateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CandidateService {

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private InterviewEmailService emailService;

    // Method to submit a candidate profile
    public CandidateResponseDto submitCandidate(CandidateDetails candidateDetails, MultipartFile resumeFile) throws IOException {
        // Validate input fields
        validateCandidateDetails(candidateDetails);

        // Check for duplicates
        checkForDuplicates(candidateDetails);

        // Optionally set userEmail and clientEmail if not already set
        setDefaultEmailsIfMissing(candidateDetails);

        // Process the resume file and set it as a BLOB
        if (resumeFile != null && !resumeFile.isEmpty()) {
            // Convert the resume file to byte[] and set it in the candidateDetails object
            byte[] resumeData = resumeFile.getBytes();
            candidateDetails.setResume(resumeData);  // Store the resume as binary data in DB

            // Save the resume to the file system and store the file path in DB
            String resumeFilePath = saveResumeToFileSystem(resumeFile);
            candidateDetails.setResumeFilePath(resumeFilePath);  // Store the file path in DB
        }
        if (!isValidFileType(resumeFile)) {
            throw new InvalidFileTypeException("Invalid file type. Only PDF and DOCX are allowed.");
        }

        // Save the candidate details to the database
        CandidateDetails savedCandidate = candidateRepository.save(candidateDetails);

        // Return response DTO with success message and candidate details
        return new CandidateResponseDto(
                "Candidate profile submitted successfully.",
                savedCandidate.getCandidateId(),
                savedCandidate.getUserId(),
                savedCandidate.getJobId()
        );
    }
    private boolean isValidFileType(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName != null) {
            String fileExtension = getFileExtension(fileName).toLowerCase();
            return fileExtension.equals("pdf") || fileExtension.equals("docx");
        }
        return false;
    }

    private String getFileExtension(String fileName) {
        int index = fileName.lastIndexOf(".");
        if (index > 0) {
            return fileName.substring(index + 1);
        }
        return "";
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

    // Check for duplicate candidate based on Email ID, Job ID, and Client Name
    private void checkForDuplicates(CandidateDetails candidateDetails) {
        Optional<CandidateDetails> existingCandidate =
                candidateRepository.findByCandidateEmailIdAndJobIdAndClientName(
                        candidateDetails.getCandidateEmailId(),
                        candidateDetails.getJobId(),
                        candidateDetails.getClientName());

        if (existingCandidate.isPresent()) {
            throw new CandidateAlreadyExistsException(
                    "Candidate with email ID " + existingCandidate.get().getCandidateEmailId() +
                            " has already been submitted for job " + existingCandidate.get().getJobId() +
                            " by client " + existingCandidate.get().getClientName()
            );
        }
        Optional<CandidateDetails> existingContactNumber =
                candidateRepository.findByContactNumberAndJobIdAndClientName(
                        candidateDetails.getContactNumber(),
                        candidateDetails.getJobId(),
                        candidateDetails.getClientName());

        if (existingContactNumber.isPresent()) {
            throw new CandidateAlreadyExistsException(
                    "Candidate with contact number " + existingContactNumber.get().getContactNumber() +
                            " has already been submitted for job " + existingContactNumber.get().getJobId() +
                            " by client " + existingContactNumber.get().getClientName()
            );
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

    private String saveResumeToFileSystem(MultipartFile resumeFile) throws IOException {
        // Set the directory where resumes will be stored
        String resumeDirectory = "C:\\Users\\User\\Downloads"; // Ensure the directory path is correct and does not have extra quotes

        // Generate a unique file name using UUID to avoid conflicts
        String fileName = UUID.randomUUID().toString() + "-" + resumeFile.getOriginalFilename();
        Path filePath = Paths.get(resumeDirectory, fileName);

        // Create the directories if they don't exist
        Files.createDirectories(filePath.getParent());

        // Save the file to the disk
        Files.write(filePath, resumeFile.getBytes());

        // Return the path where the file is saved
        return filePath.toString();
    }

    private static final Logger logger = LoggerFactory.getLogger(CandidateService.class);

//    public String uploadResume(String candidateId, MultipartFile file) throws IOException {
//        // Validate file type (only .pdf and .docx)
//        try {
//            validateFile(file);
//        } catch (InvalidFileTypeException ex) {
//            logger.error("Invalid file type for candidateId: {}", candidateId);
//            throw ex; // rethrow to be caught by controller
//        }
//
//        // Check file size (optional: 10 MB max)
//        try {
//            validateFileSize(file);
//        } catch (RuntimeException ex) {
//            logger.error("File size exceeds limit for candidateId: {}", candidateId);
//            throw ex; // rethrow to be caught by controller
//        }
//
//        // Find the candidate by ID
//        CandidateDetails candidate = candidateRepository.findById(candidateId)
//                .orElseThrow(() -> new CandidateNotFoundException("Candidate not found"));
//
//        // Save the file (either on disk or as a byte array)
//        try {
//            saveFile(candidate, file);
//        } catch (IOException ex) {
//            logger.error("Error saving the file for candidateId: {}", candidateId, ex);
//            throw ex; // rethrow to be caught by controller
//        }
//
//        return "Resume uploaded successfully for candidate: " + candidateId;
//    }

    private void validateFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || !(filename.endsWith(".pdf") || filename.endsWith(".docx"))) {
            throw new InvalidFileTypeException("Only .pdf and .docx files are allowed.");
        }
    }

    private void validateFileSize(MultipartFile file) {
        long maxSize = 10 * 1024 * 1024; // 10 MB
        if (file.getSize() > maxSize) {
            throw new RuntimeException("File size exceeds the maximum limit of 10 MB.");
        }
    }

    private void saveFile(CandidateDetails candidate, MultipartFile file) throws IOException {
        // Define the path where files will be stored
        Path uploadsDirectory = Paths.get("uploads");

        // Check if the directory exists, if not, create it
        if (Files.notExists(uploadsDirectory)) {
            Files.createDirectories(uploadsDirectory);
            logger.info("Created directory: {}", uploadsDirectory.toString());
        }

        // Generate a filename that combines the candidateId and timestamp
        String filename = candidate.getCandidateId() + "-" + System.currentTimeMillis() + "-" + file.getOriginalFilename();
        Path targetPath = uploadsDirectory.resolve(filename);  // Save the file inside the "uploads" directory

        // Log the file saving action
        logger.info("Saving file to path: {}", targetPath);

        // Save the file to the directory
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // Optionally save the file path in the database (for example, updating the candidate)
        candidate.setResumeFilePath(targetPath.toString());
        candidateRepository.save(candidate);
    }



    public Resource fetchResume(String candidateId) throws IOException {
        // Find the candidate by ID to get the resume file path
        CandidateDetails candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new CandidateNotFoundException("Candidate not found"));

        // Get the file path from the candidate entity
        String resumeFilePath = candidate.getResumeFilePath();
        Path path = Paths.get(resumeFilePath);

        // Check if the file exists
        if (Files.exists(path)) {
            // Return the file as a resource
            return new UrlResource(path.toUri());
        } else {
            // If the file doesn't exist, throw an exception
            throw new IOException("File not found: " + resumeFilePath);
        }
    }
//    // Method to get all candidate submissions
//    public List<CandidateDetails> getSubmissions() {
//        // Retrieve all candidates from the repository
//        return candidateRepository.findAll();
//    }
// Service method to get all candidate submissions
public List<CandidateGetResponseDto> getAllSubmissions() {
    // Retrieve all candidates from the repository
    List<CandidateDetails> candidates = candidateRepository.findAll();

    // Check if there are no submissions
    if (candidates.isEmpty()) {
        throw new CandidateNotFoundException("No candidate submissions found.");
    }

    // Map CandidateDetails to CandidateGetResponseDto
    return candidates.stream()
            .map(CandidateGetResponseDto::new)  // Use the DTO constructor for mapping
            .collect(Collectors.toList());
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
    public boolean isInterviewScheduled(String candidateId, OffsetDateTime interviewDateTime) {
        // Query the repository to check if there's already an interview scheduled at that time
        Optional<CandidateDetails> existingInterview = candidateRepository.findByCandidateIdAndInterviewDateTime(candidateId, interviewDateTime);

        // Return true if an interview already exists, otherwise false
        return existingInterview.isPresent();
    }


    // Method to schedule an interview for a candidate

    public InterviewResponseDto scheduleInterview(String userId, String candidateId, OffsetDateTime interviewDateTime, Integer duration,
                                                  String zoomLink, String userEmail, String clientEmail,
                                                  String clientName, String interviewLevel, String externalInterviewDetails) {

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


        // Check if an interview is already scheduled for the candidate
        if (candidate.getInterviewDateTime() != null) {
            throw new InterviewAlreadyScheduledException("An interview is already scheduled for candidate ID: " + candidateId);
        }

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
        candidate.setClientName(clientName);
        candidate.setInterviewLevel(interviewLevel);
        candidate.setExternalInterviewDetails(externalInterviewDetails);

        // Set the new fields (clientName and interviewLevel)
        if (clientName != null && !clientName.isEmpty()) {
            candidate.setClientName(clientName);
        }
        if (interviewLevel != null && !interviewLevel.isEmpty()) {
            candidate.setInterviewLevel(interviewLevel);
        }

        // **Update interview status to "Scheduled"**
        candidate.setInterviewStatus("Scheduled");
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

        // Build the email body using the provided format
        // Build the email body using the provided format
        String body = "Hi " + candidate.getFullName() + ",\n\n"
                + "Hope you are doing well!\n\n"
                + "Thank you for your interest in the position <b>" + interviewLevel + "</b> for our client <b>" + clientName + "</b>.\n"
                + "We're pleased to inform you that your profile has been shortlisted for screening.\n\n"
                + "As per our discussion, I am scheduling your screening and below are the details:\n\n"
                + "<b>Date:</b> " + interviewDateTime.format(DateTimeFormatter.BASIC_ISO_DATE) + "\n"
                + "<b>Time:</b> " + interviewDateTime.format(DateTimeFormatter.ISO_TIME) + "\n"
                + "<b>Duration:</b> Approx. " + duration + " minutes\n\n"
                + "<b>Join Zoom Meeting:</b> <a href='" + zoomLink + "'>Click here to join the interview</a>\n\n"
                + "Kindly confirm your availability by replying to this email. "
                + "Please let us know if this needs to be rescheduled or if you need further details.\n\n"
                + "We look forward to speaking with you.\n\n"
                + "Best regards,\n"
                + "The Interview Team";


        // Create the subject for the email
        String subject = "Interview Scheduled for " + candidate.getFullName();

        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body);
        // Send email to Candidate, Client, and User
        emailService.sendInterviewNotification(candidate.getCandidateEmailId(), subject, body);
        emailService.sendInterviewNotification(candidate.getClientEmail(), subject, body);
        emailService.sendInterviewNotification(candidate.getUserEmail(), subject, body);

        try {
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
        }catch (Exception e) {
            // If email sending fails, log the error and return a failure response
            e.printStackTrace();

            InterviewResponseDto.InterviewPayload payload = new InterviewResponseDto.InterviewPayload(
                    candidate.getCandidateId(),
                    candidate.getUserEmail(),
                    candidate.getCandidateEmailId(),
                    candidate.getClientEmail()
            );

            return new InterviewResponseDto(false,
                    "Interview scheduled, but an error occurred while sending email notifications.",
                    payload,
                    e.getMessage());  // Return the exception message in case of failure
        }
    }

    public List<GetInterviewResponseDto> getAllScheduledInterviews(String userId) {
        List<CandidateDetails> candidates = candidateRepository.findByUserId(userId);
        List<GetInterviewResponseDto> response = new ArrayList<>();

        for (CandidateDetails interview : candidates) {
            // Determine interview status dynamically
            String interviewStatus = (interview.getInterviewDateTime() != null) ? "Scheduled" : "Not Scheduled";

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
                    interview.getInterviewLevel(),
                    interviewStatus  // Dynamically assign status
            );
            response.add(dto);
        }

        return response;
    }
    public CandidateResponseDto resubmitCandidate(String candidateId, CandidateDetails updatedCandidateDetails, MultipartFile resumeFile) {
        try {
            // Fetch the existing candidate from the database
            Optional<CandidateDetails> existingCandidateOpt = candidateRepository.findById(candidateId);
            if (!existingCandidateOpt.isPresent()) {
                return new CandidateResponseDto("Candidate not found", null, null, null);
            }

            CandidateDetails existingCandidate = existingCandidateOpt.get();

            // Update the fields in the existing candidate with new data
            updateCandidateFields(existingCandidate, updatedCandidateDetails);

            // Handle file upload if a new resume is provided
            if (resumeFile != null && !resumeFile.isEmpty()) {
                if (!isValidFileType(resumeFile)) {
                    return new CandidateResponseDto("Invalid file type. Only PDF and DOCX are allowed.", null, null, null);
                }
                // Save the new resume file and update the file path
                String newFilePath = saveResumeToFileSystem(resumeFile);
                existingCandidate.setResumeFilePath(newFilePath);
            }

            // Save the updated candidate details
            candidateRepository.save(existingCandidate);

            // Return a success response with the updated candidate details
            return new CandidateResponseDto(
                    "Candidate successfully updated", existingCandidate.getCandidateId(),
                    existingCandidate.getJobId(), existingCandidate.getResumeFilePath()
            );

        } catch (Exception ex) {
            logger.error("An error occurred while resubmitting the candidate: {}", ex.getMessage());
            return new CandidateResponseDto("An error occurred while resubmitting the candidate", null, null, null);
        }
    }
    // Method to update the candidate fields with new values
    private void updateCandidateFields(CandidateDetails existingCandidate, CandidateDetails updatedCandidateDetails) {
        if (updatedCandidateDetails.getJobId() != null) existingCandidate.setJobId(updatedCandidateDetails.getJobId());
        if (updatedCandidateDetails.getUserId() != null) existingCandidate.setUserId(updatedCandidateDetails.getUserId());
        if (updatedCandidateDetails.getFullName() != null) existingCandidate.setFullName(updatedCandidateDetails.getFullName());
        if (updatedCandidateDetails.getCandidateEmailId() != null)
            existingCandidate.setCandidateEmailId(updatedCandidateDetails.getCandidateEmailId());
        if (updatedCandidateDetails.getContactNumber() != null) existingCandidate.setContactNumber(updatedCandidateDetails.getContactNumber());
        if (updatedCandidateDetails.getQualification() != null) existingCandidate.setQualification(updatedCandidateDetails.getQualification());
        if (updatedCandidateDetails.getTotalExperience() != 0)
            existingCandidate.setTotalExperience(updatedCandidateDetails.getTotalExperience());
        if (updatedCandidateDetails.getCurrentCTC() != null) existingCandidate.setCurrentCTC(updatedCandidateDetails.getCurrentCTC());
        if (updatedCandidateDetails.getExpectedCTC() != null)
            existingCandidate.setExpectedCTC(updatedCandidateDetails.getExpectedCTC());
        if (updatedCandidateDetails.getNoticePeriod() != null)
            existingCandidate.setNoticePeriod(updatedCandidateDetails.getNoticePeriod());
        if (updatedCandidateDetails.getCurrentLocation() != null)
            existingCandidate.setCurrentLocation(updatedCandidateDetails.getCurrentLocation());
        if (updatedCandidateDetails.getPreferredLocation() != null)
            existingCandidate.setPreferredLocation(updatedCandidateDetails.getPreferredLocation());
        if (updatedCandidateDetails.getSkills() != null) existingCandidate.setSkills(updatedCandidateDetails.getSkills());
        if (updatedCandidateDetails.getCommunicationSkills() != null)
            existingCandidate.setCommunicationSkills(updatedCandidateDetails.getCommunicationSkills());
        if (updatedCandidateDetails.getRequiredTechnologiesRating() != null)
            existingCandidate.setRequiredTechnologiesRating(updatedCandidateDetails.getRequiredTechnologiesRating());
        if (updatedCandidateDetails.getOverallFeedback() != null)
            existingCandidate.setOverallFeedback(updatedCandidateDetails.getOverallFeedback());
        if (updatedCandidateDetails.getRelevantExperience() != 0)
            existingCandidate.setRelevantExperience(updatedCandidateDetails.getRelevantExperience());
        if (updatedCandidateDetails.getCurrentOrganization() != null)
            existingCandidate.setCurrentOrganization(updatedCandidateDetails.getCurrentOrganization());
    }
    // File type validation moved above file processing


}


