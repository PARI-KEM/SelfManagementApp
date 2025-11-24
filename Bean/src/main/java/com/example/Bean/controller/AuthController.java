package com.example.Bean.controller;


import com.example.Bean.model.User;
import com.example.Bean.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/")
    public String home() {
        return "login"; // or homepage
    }

    @GetMapping("/login")
    public String loginPage(){
        return "login";
    }



    @PostMapping("/register")
    public  String register(@RequestParam String name,
                            @RequestParam String email,
                            @RequestParam String  password,
                            Model model){
        if(userRepository.existsByEmail(email)){
            model.addAttribute("error","Email already Registered");
            return "register";

        }
        User user=new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setTimezone("Asia/Kolkata");

        userRepository.save(user);
        return "redirect:/login?success";
    }
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,@RequestParam String
                        password,Model model){
        return userRepository.findByEmail(email).
                filter(user-> passwordEncoder.matches(password, user.getPasswordHash())).
                map(user->"redirect:/dashboard").
                orElseGet(()-> {
                    model.addAttribute("error", "Invalid Email or Password");
                    return "login";
                });

    }
}
