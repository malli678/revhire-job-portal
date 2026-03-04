package com.revhire.controller;

import com.revhire.model.Experience;
import com.revhire.model.JobSeeker;
import com.revhire.model.User;
import com.revhire.repository.ExperienceRepository;
import com.revhire.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/jobseeker/experience")
public class ExperienceController {

    private final ExperienceRepository experienceRepository;
    private final UserRepository userRepository;

    public ExperienceController(ExperienceRepository experienceRepository,
                                UserRepository userRepository) {
        this.experienceRepository = experienceRepository;
        this.userRepository = userRepository;
    }

    // ✅ OPEN ADD EXPERIENCE PAGE
    @GetMapping("/add")
    public String addExperienceForm(Model model) {

        model.addAttribute("experience", new Experience());

        return "jobseeker/add-experience";
    }

    // ✅ SAVE EXPERIENCE
    @PostMapping("/save")
    public String saveExperience(@ModelAttribute Experience experience,
                                 HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            throw new RuntimeException("Session expired");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        JobSeeker jobSeeker = (JobSeeker) user;

        experience.setJobSeeker(jobSeeker);

        experienceRepository.save(experience);

        return "redirect:/jobseeker/profile";
    }
}