package com.matvey.innowiseuserservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {

    @NotNull
    private UUID userId;

    @NotBlank
    private String name;

    @NotBlank
    private String surname;

    @NotNull
    @Past
    private LocalDate birthDate;

    @NotBlank
    @Email
    private String email;
}
