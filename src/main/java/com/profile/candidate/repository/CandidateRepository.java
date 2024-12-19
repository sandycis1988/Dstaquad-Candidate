package com.profile.candidate.repository;

import com.profile.candidate.model.CandidateDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<CandidateDetails, String> {
    // Additional custom queries if needed
    // Find candidate by email

    Optional<CandidateDetails> findByFullName(String fullName);

    Optional<CandidateDetails> findByEmailId(String emailId);

    Optional<CandidateDetails> findByContactNumber(String contactNumber);

    // Find all candidates with specific total experience
    List<CandidateDetails> findByTotalExperience(Integer totalExperience);

    // Find all candidates with specific skills (if skills are a list)
    List<CandidateDetails> findBySkillsContaining(String skill);

    // Find candidates by notice period
    List<CandidateDetails> findByNoticePeriod(String noticePeriod);

    Optional<CandidateDetails> findByFullNameAndEmailIdAndContactNumber(String fullName, String emailId, String contactNumber);
}