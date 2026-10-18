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
