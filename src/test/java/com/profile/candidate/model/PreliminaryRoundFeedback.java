package com.profile.candidate.model;

import jakarta.persistence.Embeddable;

import javax.validation.constraints.*;

@Embeddable
public class PreliminaryRoundFeedback {

    @NotBlank(message = "Communication skills feedback is required")
    private String communicationSkills;

    @NotNull (message = "Required technologies rating is required")
    @Min(value = 0, message = "Rating must be 0 or higher")
    @Max(value = 5, message = "Rating must be 5 or lower")
    private Double requiredTechnologiesRating;

    @NotBlank(message = "Overall feedback is required")
    @Size(max = 500, message = "Overall feedback must not exceed 500 characters")
    private String overallFeedback;

    private String zoomLink;

    // Getters and Setters

    public String getCommunicationSkills() {
        return communicationSkills;
    }

    public void setCommunicationSkills(String communicationSkills) {
        this.communicationSkills = communicationSkills;
    }

    public Double getRequiredTechnologiesRating() {
        return requiredTechnologiesRating;
    }

    public void setRequiredTechnologiesRating(Double requiredTechnologiesRating) {
        this.requiredTechnologiesRating = requiredTechnologiesRating;
    }

    public String getOverallFeedback() {
        return overallFeedback;
    }

    public void setOverallFeedback(String overallFeedback) {
        this.overallFeedback = overallFeedback;
    }

    public String getZoomLink() {
        return zoomLink;
    }

    public void setZoomLink(String zoomLink) {
        this.zoomLink = zoomLink;
    }
}

