package com.revhire.service;

import com.revhire.model.JobSeeker;
import com.revhire.model.Skill;
import com.revhire.repository.JobSeekerRepository;
import com.revhire.repository.SkillRepository;

import jakarta.transaction.Transactional;

import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ResumeParserService {
    
    private static final Logger log = LoggerFactory.getLogger(ResumeParserService.class);
    private final Tika tika = new Tika();
    
    private final JobSeekerRepository jobSeekerRepository;
    private final SkillRepository skillRepository;
    private final FileStorageService fileStorageService;
    
    // Common skills for extraction
    private static final Set<String> COMMON_SKILLS = Set.of(
        "Java", "Python", "JavaScript", "TypeScript", "C++", "C#", "Ruby", "PHP",
        "Spring", "Spring Boot", "Hibernate", "JPA", "React", "Angular", "Vue", 
        "Node.js", "Express", "Django", "Flask",
        "SQL", "MySQL", "PostgreSQL", "MongoDB", "Oracle", "Redis", "Cassandra",
        "AWS", "Azure", "GCP", "Docker", "Kubernetes", "Jenkins", "GitHub Actions",
        "Git", "Maven", "Gradle", "JUnit", "Mockito", "Selenium",
        "HTML", "CSS", "Bootstrap", "Tailwind", "SASS",
        "REST", "GraphQL", "Microservices", "SOAP", "gRPC",
        "Agile", "Scrum", "JIRA", "Confluence"
    );
    
    // Email pattern
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    
    // Phone pattern (international format)
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("(\\+\\d{1,3}[-.]?)?\\(?\\d{3}\\)?[-.]?\\d{3}[-.]?\\d{4}");
    
    // Experience pattern
    private static final Pattern EXP_PATTERN = 
        Pattern.compile("(\\d+)[+]?\\s*(?:years?|yrs?)\\s+(?:of\\s+)?experience", Pattern.CASE_INSENSITIVE);
    
    // Education patterns
    private static final List<Pattern> DEGREE_PATTERNS = Arrays.asList(
        Pattern.compile("(B\\.?Tech|Bachelor of Technology)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(B\\.?E|Bachelor of Engineering)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(B\\.?Sc|Bachelor of Science)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(B\\.?Com|Bachelor of Commerce)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(B\\.?A|Bachelor of Arts)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(M\\.?Tech|Master of Technology)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(M\\.?E|Master of Engineering)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(M\\.?Sc|Master of Science)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(MBA|Master of Business Administration)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(PhD|Doctor of Philosophy)", Pattern.CASE_INSENSITIVE)
    );
    
    public ResumeParserService(JobSeekerRepository jobSeekerRepository,
                               SkillRepository skillRepository,
                               FileStorageService fileStorageService) {
        this.jobSeekerRepository = jobSeekerRepository;
        this.skillRepository = skillRepository;
        this.fileStorageService = fileStorageService;
    }
    

    @Async
    @Transactional
    public void parseAndUpdateResume(Long jobSeekerId, MultipartFile file) {
        try {
            log.info("Starting resume parsing for job seeker ID: {}", jobSeekerId);
            
            // Save file first
            String fileName = fileStorageService.storeFile(file);
            
            // Extract text from resume
            String extractedText = extractText(file);
            log.debug("Extracted text length: {} characters", extractedText.length());
            
            // Parse the extracted text
            ParsedResumeData parsedData = parseResumeData(extractedText);
            
            // Get job seeker
            JobSeeker jobSeeker = jobSeekerRepository.findById(jobSeekerId)
                .orElseThrow(() -> new RuntimeException("JobSeeker not found"));
            
            // Always update resume file
            jobSeeker.setResumeFile(fileName);
            jobSeeker.setResumeText(extractedText.substring(0, Math.min(extractedText.length(), 4000)));
            
            // Update fields (don't overwrite if user already entered them)
            if ((jobSeeker.getPhoneNumber() == null || jobSeeker.getPhoneNumber().isEmpty()) 
                && parsedData.getPhone() != null) {
                jobSeeker.setPhoneNumber(parsedData.getPhone());
            }
            
            if (parsedData.getExperienceYears() != null) {
                jobSeeker.setTotalExperienceYears(parsedData.getExperienceYears());
            }
            
            // Update skills (merge with existing)
            Set<String> existingSkills = jobSeeker.getSkills();
            if (existingSkills == null) {
                existingSkills = new HashSet<>();
            }
            
            if (parsedData.getSkills() != null && !parsedData.getSkills().isEmpty()) {
                existingSkills.addAll(parsedData.getSkills());
                jobSeeker.setSkills(existingSkills);
            }
            
            // Save the updated job seeker
            jobSeekerRepository.save(jobSeeker);
            
            log.info("Resume parsing completed for job seeker: {}. Extracted {} skills", 
                     jobSeekerId, parsedData.getSkills() != null ? parsedData.getSkills().size() : 0);
            
        } catch (Exception e) {
            log.error("Failed to parse resume: {}", e.getMessage(), e);
        }
    }
    
    public String extractText(MultipartFile file) throws IOException {
        try {
            return tika.parseToString(file.getInputStream());
        } catch (Exception e) {
            throw new IOException("Failed to extract text from resume: " + e.getMessage());
        }
    }
    
    public ParsedResumeData parseResumeData(String text) {
        ParsedResumeData data = new ParsedResumeData();
        
        // Extract email
        Matcher emailMatcher = EMAIL_PATTERN.matcher(text);
        if (emailMatcher.find()) {
            data.setEmail(emailMatcher.group());
        }
        
        // Extract phone
        Matcher phoneMatcher = PHONE_PATTERN.matcher(text);
        if (phoneMatcher.find()) {
            data.setPhone(phoneMatcher.group());
        }
        
        // Extract experience years
        Matcher expMatcher = EXP_PATTERN.matcher(text);
        if (expMatcher.find()) {
            try {
                data.setExperienceYears(Integer.parseInt(expMatcher.group(1)));
            } catch (NumberFormatException e) {
                log.debug("Failed to parse experience number: {}", expMatcher.group(1));
            }
        }
        
        // Extract skills
        Set<String> foundSkills = new HashSet<>();
        for (String skill : COMMON_SKILLS) {
            // Look for whole word matches
            Pattern skillPattern = Pattern.compile("\\b" + Pattern.quote(skill) + "\\b", Pattern.CASE_INSENSITIVE);
            if (skillPattern.matcher(text).find()) {
                foundSkills.add(skill);
            }
        }
        data.setSkills(foundSkills);
        
        // Extract degree
        for (Pattern pattern : DEGREE_PATTERNS) {
            Matcher degreeMatcher = pattern.matcher(text);
            if (degreeMatcher.find()) {
                data.setDegree(degreeMatcher.group());
                break;
            }
        }
        
        log.debug("Parsed resume data: {}", data);
        return data;
    }
    
    // Inner class for parsed data
    public static class ParsedResumeData {
        private String email;
        private String phone;
        private Set<String> skills;
        private String degree;
        private Integer experienceYears;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        
        public Set<String> getSkills() { return skills; }
        public void setSkills(Set<String> skills) { this.skills = skills; }
        
        public String getDegree() { return degree; }
        public void setDegree(String degree) { this.degree = degree; }
        
        public Integer getExperienceYears() { return experienceYears; }
        public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }
        
        @Override
        public String toString() {
            return String.format("ParsedResumeData{email=%s, phone=%s, skills=%d, degree=%s, experience=%d}",
                email, phone, skills != null ? skills.size() : 0, degree, experienceYears);
        }
    }
}