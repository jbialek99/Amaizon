package org.example.amaizon.model.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.example.amaizon.model.validators.validation.EmailNotTaken;
import org.example.amaizon.model.validators.validation.FieldMatch;
import org.example.amaizon.model.validators.validation.StrongPassword;


@FieldMatch(
        first = "password",
        second = "confirmPassword",
        message = "{validation.user.password.confirm_mismatch}"
)
public class RegisterRequest {

    @NotBlank(message = "{validation.user_country_profiles.firstName.is_empty}")
    @Size(min = 2, max = 50, message = "{validation.user_country_profiles.firstName.invalid_length}")
    private String firstName;

    @NotBlank(message = "{validation.user_country_profiles.lastName.is_empty}")
    @Size(min = 2, max = 50, message = "{validation.user_country_profiles.lastName.invalid_length}")
    private String lastName;

    @EmailNotTaken
    @NotBlank(message = "{validation.user.email.is_empty}")
    @Size(min = 6, max = 100, message = "{validation.user.email.invalid_length}")
    @Pattern(
            regexp = "^(?!\\.)[A-Za-z0-9._%+-]+(?<!\\.)@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$",
            message = "{validation.user.email.invalid}"
    )
    private String email;

    @NotBlank(message = "{validation.user.password.is_empty}")
    @StrongPassword(min = 8, max = 100, minDigits = 2)
    private String password;

    @NotBlank(message = "{validation.user.password.confirm.is_empty}")
    private String confirmPassword;


    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getConfirmPassword() { return confirmPassword; }

}
