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
