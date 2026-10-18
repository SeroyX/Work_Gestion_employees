package com.entreprise.gestion.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.entreprise.gestion.dto.ChangePasswordForm;
import com.entreprise.gestion.model.Employee;
import com.entreprise.gestion.service.EmployeeService;
import com.entreprise.gestion.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/employee")
@PreAuthorize("hasRole('EMPLOYEE')")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final UserService userService;

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        String email = authentication.getName(); // l'email est le username

        Employee employee = employeeService.findByEmail(email)
                .orElse(null);

        model.addAttribute("employee", employee);
        model.addAttribute("changePasswordForm", new ChangePasswordForm());
        return "employee/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(
            Authentication authentication,
            @ModelAttribute ChangePasswordForm form,
            RedirectAttributes redirectAttributes) {

        String email = authentication.getName();

        if (!form.getNouveauMotDePasse().equals(form.getConfirmationMotDePasse())) {
            redirectAttributes.addFlashAttribute("errorMsg",
                    "Le nouveau mot de passe et la confirmation ne correspondent pas.");
            return "redirect:/employee/profile";
        }

        try {
            userService.changePassword(email, form.getAncienMotDePasse(), form.getNouveauMotDePasse());
            redirectAttributes.addFlashAttribute("successMsg",
                    "Mot de passe modifié avec succès !");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/employee/profile";
    }
}
