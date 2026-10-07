package cmr.notep.business.services;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.modeles.Professeurs;
import cmr.notep.interfaces.modeles.Utilisateurs;
import cmr.notep.ressourcesjpa.repository.UtilisateursRepository;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Service;

@Service
public class UserValidationService {
    
    private final UtilisateursRepository utilisateursRepository;
    
    public UserValidationService(UtilisateursRepository utilisateursRepository) {
        this.utilisateursRepository = utilisateursRepository;
    }

    public void validateUserData(Utilisateurs utilisateur) {
        validateRequiredFields(utilisateur);
        validateEmailFormat(utilisateur.getEmail());
 

        if (utilisateur.getTelephone() != null && !utilisateur.getTelephone().isEmpty()) {
            validatePhoneNumber(utilisateur.getTelephone());
        }

        // validateUserTypeRequirements(utilisateur);
    }


    /**
     * Validates that all required fields are present
     */
    private void validateRequiredFields(Utilisateurs utilisateur) {
        if (utilisateur.getEmail() == null || utilisateur.getEmail().trim().isEmpty()) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "Email is required");
        }

        if (utilisateur.getNom() == null || utilisateur.getNom().trim().isEmpty()) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "Last name is required");
        }

        if (utilisateur.getPrenom() == null || utilisateur.getPrenom().trim().isEmpty()) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "First name is required");
        }

    }

    /**
     * Validates email format using regex pattern
     */
    private void validateEmailFormat(String email) {
        if (!EmailValidator.getInstance().isValid(email)) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT,
                    "Invalid email format");
        }
    }
    /**
     * Validates password strength requirements
     */
    public void validatePasswordStrength(String password) {
        // Password must be at least 12 characters
        if (password.length() < 12) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT,
                    "Password must be at least 8 characters long");
        }

        // Check for at least one uppercase letter
        if (!password.matches(".*[A-Z].*")) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT,
                    "Password must contain at least one uppercase letter");
        }

        // Check for at least one lowercase letter
        if (!password.matches(".*[a-z].*")) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT,
                    "Password must contain at least one lowercase letter");
        }

        // Check for at least one digit
        if (!password.matches(".*\\d.*")) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT,
                    "Password must contain at least one digit");
        }

        // Check for at least one special character
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) { throw new SchoolException(SchoolErrorCode.INVALID_INPUT,
                "Password must contain at least one special character");
        }
    }

    /**
     * Validates the phone number (international format, default region Cameroon).
     * Cameroon numbers are accepted as soon as they have the national shape (9 digits starting with
     * 2 or 6): operators open new ranges (e.g. 64x) before libphonenumber's metadata lists them, and
     * the clients apply the same rule. Other countries must be valid per libphonenumber.
     */
    private void validatePhoneNumber(String phoneNumber) {
        try {
            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            Phonenumber.PhoneNumber numberProto = phoneUtil.parse(phoneNumber, "CM");
            if (phoneUtil.isValidNumber(numberProto)) {
                return;
            }
            String national = String.valueOf(numberProto.getNationalNumber());
            if (numberProto.getCountryCode() == 237 && national.matches("[26]\\d{8}")) {
                return;
            }
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "Invalid phone number format");
        } catch (NumberParseException e) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT,
                    "Invalid phone number format");
        }
    }
    /**
     * Validates user type-specific requirements
     */
    private void validateUserTypeRequirements(Utilisateurs utilisateur) {
        // Implement any additional validation logic based on user type
        if (utilisateur instanceof Professeurs) {
            // Add specific validations for professors if needed
        }
        // Add other user type checks as necessary
    }
}