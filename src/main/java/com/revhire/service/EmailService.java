package com.revhire.service;

import com.revhire.model.Application;
import com.revhire.model.Job;
import com.revhire.model.JobSeeker;
import com.revhire.model.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {
    
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.base.url}")
    private String baseUrl;
    
    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }
    
    @Async
    public void sendApplicationConfirmation(JobSeeker jobSeeker, Job job) {
        try {
            Context context = new Context();
            context.setVariable("name", jobSeeker.getFullName());
            context.setVariable("jobTitle", job.getTitle());
            context.setVariable("companyName", job.getEmployer().getCompanyName());
            context.setVariable("dashboardUrl", baseUrl + "/jobseeker/applications");
            
            String htmlContent = templateEngine.process("email/application-confirmation", context);
            
            sendEmail(
                jobSeeker.getEmail(),
                "Application Submitted Successfully - RevHire",
                htmlContent
            );
            
            log.info("Application confirmation email sent to: {}", jobSeeker.getEmail());
        } catch (Exception e) {
            log.error("Failed to send application confirmation email: {}", e.getMessage());
        }
    }
    
    @Async
    public void sendApplicationStatusUpdate(Application application, String oldStatus) {
        try {
            Context context = new Context();
            context.setVariable("name", application.getJobSeeker().getFullName());
            context.setVariable("jobTitle", application.getJob().getTitle());
            context.setVariable("companyName", application.getJob().getEmployer().getCompanyName());
            context.setVariable("oldStatus", oldStatus);
            context.setVariable("newStatus", application.getStatus().toString());
            context.setVariable("notes", application.getNotes());
            context.setVariable("dashboardUrl", baseUrl + "/jobseeker/applications");
            
            String htmlContent = templateEngine.process("email/status-update", context);
            
            sendEmail(
                application.getJobSeeker().getEmail(),
                "Application Status Updated - RevHire",
                htmlContent
            );
            
            log.info("Status update email sent to: {}", application.getJobSeeker().getEmail());
        } catch (Exception e) {
            log.error("Failed to send status update email: {}", e.getMessage());
        }
    }
    
    @Async
    public void sendNewApplicationAlert(Application application) {
        try {
            User employer = application.getJob().getEmployer();
            
            Context context = new Context();
            context.setVariable("employerName", employer.getFullName());
            context.setVariable("candidateName", application.getJobSeeker().getFullName());
            context.setVariable("jobTitle", application.getJob().getTitle());
            context.setVariable("dashboardUrl", baseUrl + "/employer/dashboard");
            
            String htmlContent = templateEngine.process("email/new-application-alert", context);
            
            sendEmail(
                employer.getEmail(),
                "New Application Received - RevHire",
                htmlContent
            );
            
            log.info("New application alert email sent to: {}", employer.getEmail());
        } catch (Exception e) {
            log.error("Failed to send new application alert email: {}", e.getMessage());
        }
    }
    
    @Async
    public void sendPasswordResetEmail(User user, String token) {
        try {
            Context context = new Context();
            context.setVariable("name", user.getFullName());
            context.setVariable("resetLink", baseUrl + "/auth/reset-password?token=" + token);
            
            String htmlContent = templateEngine.process("email/password-reset", context);
            
            sendEmail(
                user.getEmail(),
                "Password Reset Request - RevHire",
                htmlContent
            );
            
            log.info("Password reset email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email: {}", e.getMessage());
        }
    }
    
    @Async
    public void sendJobAlert(JobSeeker jobSeeker, Job job) {
        try {
            Context context = new Context();
            context.setVariable("name", jobSeeker.getFullName());
            context.setVariable("jobTitle", job.getTitle());
            context.setVariable("companyName", job.getEmployer().getCompanyName());
            context.setVariable("location", job.getLocation());
            context.setVariable("salary", formatSalary(job));
            context.setVariable("jobUrl", baseUrl + "/jobseeker/job/" + job.getJobId());
            
            String htmlContent = templateEngine.process("email/job-alert", context);
            
            sendEmail(
                jobSeeker.getEmail(),
                "New Job Match: " + job.getTitle() + " - RevHire",
                htmlContent
            );
            
            log.info("Job alert email sent to: {}", jobSeeker.getEmail());
        } catch (Exception e) {
            log.error("Failed to send job alert email: {}", e.getMessage());
        }
    }
    
    private void sendEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        
        mailSender.send(message);
    }
    
    private String formatSalary(Job job) {
        if (job.getSalaryMin() == null && job.getSalaryMax() == null) {
            return "Not specified";
        }
        if (job.getSalaryMin() == null) {
            return "Up to ₹" + String.format("%,.0f", job.getSalaryMax());
        }
        if (job.getSalaryMax() == null) {
            return "From ₹" + String.format("%,.0f", job.getSalaryMin());
        }
        return "₹" + String.format("%,.0f", job.getSalaryMin()) + 
               " - ₹" + String.format("%,.0f", job.getSalaryMax());
    }
}