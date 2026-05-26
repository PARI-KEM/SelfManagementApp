package com.example.Bean.controller;

import com.example.Bean.dto.RegisterRequest;
import com.example.Bean.model.User;
import com.example.Bean.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // GET /auth/login — Spring Security handles the POST via formLogin.
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute RegisterRequest request, Model model) {

        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            model.addAttribute("error", "Password is required");
            return "register";
        }
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            model.addAttribute("error", "Email is required");
            return "register";
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            model.addAttribute("error", "Email already registered");
            return "register";
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        return "redirect:/auth/login?registered";
    }

    // Logout is handled by Spring Security's logout filter via /auth/logout.
}
