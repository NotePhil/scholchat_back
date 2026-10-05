package cmr.notep.business.utils;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;

/**
 * Règles de mot de passe (identiques à /auth/change-password) : au moins 8 caractères,
 * une majuscule, une minuscule, un chiffre et un caractère spécial.
 */
public final class PasswordPolicy {

    private PasswordPolicy() {
    }

    public static void valider(String password) {
        if (password == null || password.isBlank()) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "Le mot de passe est requis");
        }
        if (password.length() < 8) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT,
                    "Le mot de passe doit contenir au moins 8 caractères");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT,
                    "Le mot de passe doit contenir au moins une lettre majuscule");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT,
                    "Le mot de passe doit contenir au moins une lettre minuscule");
        }
        if (!password.matches(".*\\d.*")) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT,
                    "Le mot de passe doit contenir au moins un chiffre");
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT,
                    "Le mot de passe doit contenir au moins un caractère spécial (!@#$%^&*...)");
        }
    }
}
