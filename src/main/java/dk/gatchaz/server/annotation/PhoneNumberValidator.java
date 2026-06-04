package your.newproject.rename.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {

    private static final String INTERNATIONAL_PHONE_PATTERN = "^[0-9]{7,11}$"; // 숫자만, 7~11자리

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && value.matches(INTERNATIONAL_PHONE_PATTERN);
    }
}
