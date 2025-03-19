package com.example.chat.controller;

import com.example.chat.model.User;
import com.example.chat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;


@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; 


    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; // This should match a Thymeleaf template or an HTML file
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register"; // Resolves to register.html
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user, Model model) {
        if(userRepository.findByUsername(user.getUsername()) != null) {
            // Optionally add an error attribute and return to registration page
            model.addAttribute("error", "Username already exists!");
            return "register";
        }
        // Hash the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        // Redirect to login page after successful registration
        return "redirect:/auth/login";
    }
}
