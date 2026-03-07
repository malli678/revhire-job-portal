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

/**
 * ExperienceController manages operations related to
 * job seeker work experience.
 *
 * Responsibilities:
 * - Display the add experience form.
 * - Save experience details submitted by the job seeker.
 */
@Controller
@RequestMapping("/jobseeker/experience")
public class ExperienceController {

    /**
     * Repository used to perform database operations for Experience entities.
     */
    private final ExperienceRepository experienceRepository;

    /**
     * Repository used to retrieve user details from the database.
     */
    private final UserRepository userRepository;

    /**
     * Constructor for dependency injection.
     *
     * @param experienceRepository repository used to store and retrieve experience records
     * @param userRepository repository used to access user data
     */
    public ExperienceController(ExperienceRepository experienceRepository,
                                UserRepository userRepository) {
        this.experienceRepository = experienceRepository;
        this.userRepository = userRepository;
    }

    //  OPEN ADD EXPERIENCE PAGE

    /**
     * Displays the add experience form for job seekers.
     *
     * A new Experience object is added to the model
     * so that the form can bind user input.
     *
     * @param model Spring model used to pass data to the view
     * @return add experience page
     */
    @GetMapping("/add")
    public String addExperienceForm(Model model) {

        model.addAttribute("experience", new Experience());

        return "jobseeker/add-experience";
    }

    //  SAVE EXPERIENCE

    /**
     * Saves the experience details submitted by the job seeker.
     *
     * Steps performed:
     * - Retrieve the logged-in user's ID from the HTTP session.
     * - Fetch the user from the database.
     * - Cast the user to a JobSeeker entity.
     * - Associate the experience with the job seeker.
     * - Save the experience in the database.
     *
     * @param experience experience details submitted from the form
     * @param session HTTP session containing logged-in user information
     * @return redirect to the job seeker profile page
     */
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