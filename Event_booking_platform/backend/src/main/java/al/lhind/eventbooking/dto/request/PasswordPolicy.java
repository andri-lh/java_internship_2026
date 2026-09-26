package al.lhind.eventbooking.dto.request;

/** Server-side mirror of the password rules shown in the frontend checklist. */
public final class PasswordPolicy {

    public static final String REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,72}$";
    public static final String MESSAGE =
            "must be 8-72 characters with an uppercase letter, a lowercase letter, a number, and a special character";

    private PasswordPolicy() {
    }
}
