package com.entreprise.gestion.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.entreprise.gestion.dto.EmployeeForm;
import com.entreprise.gestion.model.Employee;
import com.entreprise.gestion.service.EmployeeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final EmployeeService employeeService;

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(value = "search", required = false) String search,
            Model model) {

        List<Employee> employees = employeeService.search(search);
        model.addAttribute("employees", employees);
        model.addAttribute("search", search);
        model.addAttribute("totalEmployes", employees.size());
        return "admin/dashboard";
    }

    @GetMapping("/employees/new")
    public String showCreateForm(Model model) {
        model.addAttribute("employeeForm", new EmployeeForm());
        return "admin/create-employee";
    }

    @PostMapping("/employees/new")
    public String createEmployee(
            @Valid @ModelAttribute("employeeForm") EmployeeForm form,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            return "admin/create-employee";
        }

        try {
            employeeService.createFromForm(form);
            redirectAttributes.addFlashAttribute("successMsg",
                    "Employé " + form.getPrenom() + " " + form.getNom() + " créé avec succès !");
            return "redirect:/admin/dashboard";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMsg", e.getMessage());
            return "admin/create-employee";
        }
    }

    @GetMapping("/employees/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Employee employee = employeeService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employé introuvable : " + id));

        // Mapper Employee → EmployeeForm
        EmployeeForm form = new EmployeeForm();
        form.setId(employee.getId());
        form.setNom(employee.getNom());
        form.setPrenom(employee.getPrenom());
        form.setAge(employee.getAge());
        form.setEmail(employee.getEmail());
        form.setPoste(employee.getPoste());
        form.setDepartement(employee.getDepartement());
        form.setTelephone(employee.getTelephone());
        form.setDateEmbauche(employee.getDateEmbauche());
        form.setAdresse(employee.getAdresse());

        employee.getDiplomes().forEach(d -> {
            form.getDiplomeIntitules().add(d.getIntitule());
            form.getDiplomeEtablissements().add(d.getEtablissement());
            form.getDiplomeAnnees().add(d.getAnneeObtention());
        });

        model.addAttribute("employeeForm", form);
        model.addAttribute("employee", employee);
        return "admin/edit-employee";
    }

    @PostMapping("/employees/{id}/edit")
    public String updateEmployee(
            @PathVariable Long id,
            @Valid @ModelAttribute("employeeForm") EmployeeForm form,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            return "admin/edit-employee";
        }

        try {
            employeeService.updateFromForm(id, form);
            redirectAttributes.addFlashAttribute("successMsg", "Employé mis à jour avec succès !");
            return "redirect:/admin/dashboard";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMsg", e.getMessage());
            return "admin/edit-employee";
        }
    }

    @PostMapping("/employees/{id}/delete")
    public String deleteEmployee(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            employeeService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMsg", "Employé supprimé avec succès.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/employees/{id}")
    public String viewEmployee(@PathVariable Long id, Model model) {
        Employee employee = employeeService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employé introuvable : " + id));
        model.addAttribute("employee", employee);
        return "admin/view-employee";
    }
}
