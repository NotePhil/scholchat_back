package cmr.notep.business.exceptions.enums;

public enum SchoolErrorCode {
    NOT_FOUND("Resource not found"),
    OPERATION_INTERDITE("Operation is not allowed"),
    INTERFACE_NON_RESPECTEE("Interface contract not respected"),
    INTERNAL_ERROR("Internal server error"),
    INVALID_INPUT("Invalid input data"),
    INVALID_TOKEN("Invalid activation token"),
    INVALID_OPERATION("Invalid operation");


    private final String message;

    SchoolErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}