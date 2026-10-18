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
