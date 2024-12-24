package com.profile.candidate.dto;

import com.profile.candidate.model.CandidateDetails;
import java.time.LocalDateTime;

public class InterviewDto {

    private LocalDateTime interviewDateTime;
    private Integer duration;
    private String zoomLink;
    private LocalDateTime scheduledTimeStamp;
    private String userId;
    private String jobId;
    private String clientName;
    private String candidateId;
    private String fullName;

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setCandidateId(String candidateId) {
        this.candidateId = candidateId;
    }

    private String contactNumber;// Use CandidateDetails object instead of individual fields
    private String userEmail;
    private String interviewLevel;
    private String clientEmail;

    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }

    // Constructor
    public InterviewDto() {
        // Empty constructor
    }

    // Getters and Setters


    public LocalDateTime getInterviewDateTime() {
        return interviewDateTime;
    }

    public void setInterviewDateTime(LocalDateTime interviewDateTime) {
        this.interviewDateTime = interviewDateTime;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public LocalDateTime getScheduledTimeStamp() {
        return scheduledTimeStamp;
    }

    public void setScheduledTimeStamp(LocalDateTime scheduledTimeStamp) {
        this.scheduledTimeStamp = scheduledTimeStamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getInterviewLevel() {
        return interviewLevel;
    }

    public void setInterviewLevel(String interviewLevel) {
        this.interviewLevel = interviewLevel;
    }

    public String getZoomLink() {
        return zoomLink;
    }

    public void setZoomLink(String zoomLink) {
        this.zoomLink = zoomLink;
    }

    @Override
    public String toString() {
        return "InterviewDto{" +
                "interviewDateTime=" + interviewDateTime +
                ", duration=" + duration +
                ", zoomLink='" + zoomLink + '\'' +
                ", scheduledTimeStamp=" + scheduledTimeStamp +
                ", userId='" + userId + '\'' +
                ", jobId='" + jobId + '\'' +
                ", candidateId='" + candidateId + '\'' +
                ", fullName='" + fullName + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                ", clientName='" + clientName + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", interviewLevel='" + interviewLevel + '\'' +
                '}';
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public String getCandidateId() {
        return candidateId;
    }
}
