package com.entreprise.gestion.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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
