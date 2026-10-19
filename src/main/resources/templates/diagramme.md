// Controller folder contiens 3 files

// adminController.java file
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

// authController.java file
package com.entreprise.gestion.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        if (error != null) {
            model.addAttribute("errorMsg", "Email ou mot de passe incorrect.");
        }
        if (logout != null) {
            model.addAttribute("logoutMsg", "Vous avez été déconnecté avec succès.");
        }
        return "auth/login";
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }

}

// employeeController.java file
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

// dto folder contiens 2 files

// ChangePasswordForm.java
package com.entreprise.gestion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordForm {

    private String ancienMotDePasse;

    private String nouveauMotDePasse;

    private String confirmationMotDePasse;

}

// EmployeeForm.java
package com.entreprise.gestion.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeForm {

    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @Min(value = 18, message = "L'âge minimum est 18 ans")
    @Max(value = 70, message = "L'âge maximum est 70 ans")
    private int age;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format email invalide")
    private String email;

    private String poste;

    private String departement;

    private String telephone;

    private LocalDate dateEmbauche;

    private String adresse;

    private List<String> diplomeIntitules = new ArrayList<>();
    private List<String> diplomeEtablissements = new ArrayList<>();
    private List<Integer> diplomeAnnees = new ArrayList<>();

    private String motDePasse;

}

// model folder contiens 3 files

// diplome.java

package com.entreprise.gestion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "diplomes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Diplome {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "L'intitulé du diplôme est obligatoire")
    @Column(nullable = false)
    private String intitule;

    @Column
    private String etablissement;

    @Column
    private Integer anneeObtention;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

}

// emplpyee.java
package com.entreprise.gestion.model;

import jakarta.persistence._;
import jakarta.validation.constraints._;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    @Column(nullable = false)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Column(nullable = false)
    private String prenom;

    @Min(value = 18, message = "L'âge minimum est 18 ans")
    @Max(value = 70, message = "L'âge maximum est 70 ans")
    @Column(nullable = false)
    private int age;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format email invalide")
    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String poste;

    @Column
    private String departement;

    @Column
    private String telephone;

    @Column
    private LocalDate dateEmbauche;

    @Column(length = 1000)
    private String adresse;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Diplome> diplomes = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    // Méthode utilitaire pour ajouter un diplôme
    public void addDiplome(Diplome diplome) {
        diplomes.add(diplome);
        diplome.setEmployee(this);
    }

    // Méthode utilitaire pour supprimer un diplôme
    public void removeDiplome(Diplome diplome) {
        diplomes.remove(diplome);
        diplome.setEmployee(null);
    }

}

// user.java
package com.entreprise.gestion.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private boolean enabled = true;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Employee employee;

}

// repository folder contiens 3 files

// DiplomeRepository.java
package com.entreprise.gestion.repository;

import com.entreprise.gestion.model.Diplome;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiplomeRepository extends JpaRepository<Diplome, Long> {

    List<Diplome> findByEmployeeId(Long employeeId);

    void deleteByEmployeeId(Long employeeId);

}

// EmployeeRepository.java
package com.entreprise.gestion.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.entreprise.gestion.model.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmail(String email);

    boolean existsByEmail(String email);

    // Recherche par nom ou prénom
    List<Employee> findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(String nom, String prenom);

    // Recherche par département
    List<Employee> findByDepartement(String departement);

    // Liste avec diplômes
    @Query("SELECT DISTINCT e FROM Employee e LEFT JOIN FETCH e.diplomes WHERE e.id = :id")
    Optional<Employee> findByIdWithDiplomes(Long id);

    // Tous les employés avec leurs diplômes
    @Query("SELECT DISTINCT e FROM Employee e LEFT JOIN FETCH e.diplomes")
    List<Employee> findAllWithDiplomes();

}

// UserRepository.java
package com.entreprise.gestion.repository;

import com.entreprise.gestion.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

}

// service folder contiens 2 files

// EmployeeService.java
package com.entreprise.gestion.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.entreprise.gestion.dto.EmployeeForm;
import com.entreprise.gestion.model.Diplome;
import com.entreprise.gestion.model.Employee;
import com.entreprise.gestion.model.User;
import com.entreprise.gestion.repository.DiplomeRepository;
import com.entreprise.gestion.repository.EmployeeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DiplomeRepository diplomeRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Employee> findById(Long id) {
        return employeeRepository.findByIdWithDiplomes(id);
    }

    @Transactional(readOnly = true)
    public Optional<Employee> findByEmail(String email) {
        return employeeRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public List<Employee> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return employeeRepository.findAll();
        }
        return employeeRepository
                .findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCase(keyword, keyword);
    }

    public Employee createFromForm(EmployeeForm form) {
        if (employeeRepository.existsByEmail(form.getEmail())) {
            throw new IllegalArgumentException("L'email '" + form.getEmail() + "' est déjà utilisé.");
        }

        // Mot de passe ou l'email par défaut
        String rawPassword = (form.getMotDePasse() != null && !form.getMotDePasse().isBlank())
                ? form.getMotDePasse()
                : form.getEmail();

        User user = userService.createUser(form.getEmail(), rawPassword, "ROLE_EMPLOYEE");

        Employee employee = new Employee();
        mapFormToEmployee(form, employee);
        employee.setUser(user);

        Employee saved = employeeRepository.save(employee);

        saveDiplomes(form, saved);

        return saved;
    }

    // Modification

    public Employee updateFromForm(Long id, EmployeeForm form) {
        Employee employee = employeeRepository.findByIdWithDiplomes(id)
                .orElseThrow(() -> new IllegalArgumentException("Employé introuvable avec l'ID : " + id));

        // Si l'email a changé, mettre à jour le username aussi
        if (!employee.getEmail().equals(form.getEmail())) {
            if (employeeRepository.existsByEmail(form.getEmail())) {
                throw new IllegalArgumentException("L'email '" + form.getEmail() + "' est déjà utilisé.");
            }
            User user = employee.getUser();
            user.setUsername(form.getEmail());
        }

        mapFormToEmployee(form, employee);

        // Supprimer les anciens diplômes et réenregistrer
        diplomeRepository.deleteByEmployeeId(id);
        employee.getDiplomes().clear();
        employeeRepository.save(employee);

        saveDiplomes(form, employee);

        return employee;
    }

    /**
     * Supprime un employé
     */
    public void deleteById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employé introuvable avec l'ID : " + id));

        Long userId = employee.getUser() != null ? employee.getUser().getId() : null;
        employeeRepository.deleteById(id);

        if (userId != null) {
            userService.deleteById(userId);
        }
    }

    private void mapFormToEmployee(EmployeeForm form, Employee employee) {
        employee.setNom(form.getNom());
        employee.setPrenom(form.getPrenom());
        employee.setAge(form.getAge());
        employee.setEmail(form.getEmail());
        employee.setPoste(form.getPoste());
        employee.setDepartement(form.getDepartement());
        employee.setTelephone(form.getTelephone());
        employee.setDateEmbauche(form.getDateEmbauche());
        employee.setAdresse(form.getAdresse());
    }

    private void saveDiplomes(EmployeeForm form, Employee employee) {
        List<String> intitules = form.getDiplomeIntitules();
        if (intitules == null)
            return;

        for (int i = 0; i < intitules.size(); i++) {
            String intitule = intitules.get(i);
            if (intitule == null || intitule.isBlank())
                continue;

            Diplome diplome = new Diplome();
            diplome.setIntitule(intitule);
            diplome.setEtablissement(
                    getOrNull(form.getDiplomeEtablissements(), i));
            diplome.setAnneeObtention(
                    getOrNull(form.getDiplomeAnnees(), i));
            diplome.setEmployee(employee);
            diplomeRepository.save(diplome);
        }
    }

    private <T> T getOrNull(List<T> list, int index) {
        if (list == null || index >= list.size())
            return null;
        return list.get(index);
    }

}

// UserService.java
package com.entreprise.gestion.service;

import java.util.Collections;
import java.util.Optional;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.entreprise.gestion.model.User;
import com.entreprise.gestion.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Utilisateur non trouvé : " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true, true, true,
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole())));
    }

    // Créé un nouvel utilisateur

    public User createUser(String username, String rawPassword, String role) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("L'email '" + username + "' est déjà utilisé.");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public void changePassword(String username, String ancienMdp, String nouveauMdp) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé."));

        if (!passwordEncoder.matches(ancienMdp, user.getPassword())) {
            throw new IllegalArgumentException("L'ancien mot de passe est incorrect.");
        }
        if (nouveauMdp.length() < 4) {
            throw new IllegalArgumentException("Le nouveau mot de passe doit avoir au moins 4 caractères.");
        }
        user.setPassword(passwordEncoder.encode(nouveauMdp));
        userRepository.save(user);
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

}
