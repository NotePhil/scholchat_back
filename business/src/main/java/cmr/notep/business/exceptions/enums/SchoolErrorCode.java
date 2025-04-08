package cmr.notep.business.exceptions.enums;

public enum SchoolErrorCode {
    NOT_FOUND("Resource not found"),
    OPERATION_INTERDITE("Operation is not allowed"),
    INTERFACE_NON_RESPECTEE("Interface contract not respected"),
    INTERNAL_ERROR("Internal server error"),
    INVALID_INPUT("Invalid input data"),
    INVALID_TOKEN("Invalid activation token"),
    INVALID_OPERATION("Invalid operation"),
    EMAIL_NOT_SENT("The activation email could not be sent."),
    INVALID_STATE("The user is not in a valid state for this operation."),

    MAPPING_FAILED("User entity mapping failed"),
    TOKEN_EXPIRED("Token Expired"),
    INACTIVE_USER("Cannot create refresh token for inactive user"),

    EMAIL_ERROR("Email processing error");

    private final String message;

    SchoolErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}