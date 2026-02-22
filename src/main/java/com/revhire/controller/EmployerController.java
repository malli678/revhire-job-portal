package com.revhire.controller;

import com.revhire.model.Employer;
import com.revhire.model.Job;
import com.revhire.model.User;
import com.revhire.service.EmployerService;
import com.revhire.service.UserService;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/employer")
public class EmployerController {

    private final UserService userService;
    private final EmployerService employerService;

    public EmployerController(UserService userService,
                              EmployerService employerService) {
        this.userService = userService;
        this.employerService = employerService;
    }

    // =========================
    // DASHBOARD (KEEP SAME)
    // =========================
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session,
                            Authentication authentication) {

        if (authentication == null) {
            return "redirect:/auth/login";
        }

        User user = userService.findByEmail(authentication.getName());

        if (user == null) {
            return "redirect:/auth/login";
        }

        session.setAttribute("userId", user.getUserId());
        session.setAttribute("userName", user.getFullName());

        return "employer/dashboard";
    }

    // =========================
    // POST JOB
    // =========================
    @PostMapping("/post-job")
    @ResponseBody
    public Job postJob(@RequestBody Job job,
                       Authentication auth) {

        Employer emp =
                employerService.getEmployerByEmail(auth.getName());

        return employerService.postJob(job, emp);
    }

    // =========================
    // EDIT JOB
    // =========================
    @PutMapping("/job/{id}")
    @ResponseBody
    public Job editJob(@PathVariable Long id,
                       @RequestBody Job job) {
        return employerService.updateJob(id, job);
    }

    // =========================
    // DELETE JOB
    // =========================
    @DeleteMapping("/job/{id}")
    @ResponseBody
    public String delete(@PathVariable Long id) {
        employerService.deleteJob(id);
        return "Deleted Successfully";
    }

    // =========================
    // CLOSE JOB
    // =========================
    @PutMapping("/job/{id}/close")
    @ResponseBody
    public Job close(@PathVariable Long id) {
        return employerService.closeJob(id);
    }

    // =========================
    // REOPEN JOB
    // =========================
    @PutMapping("/job/{id}/reopen")
    @ResponseBody
    public Job reopen(@PathVariable Long id) {
        return employerService.reopenJob(id);
    }

    // =========================
    // MARK FILLED
    // =========================
    @PutMapping("/job/{id}/filled")
    @ResponseBody
    public Job filled(@PathVariable Long id) {
        return employerService.markFilled(id);
    }
}