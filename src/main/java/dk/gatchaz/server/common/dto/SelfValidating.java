package dk.gatchaz.server.common.dto;

import jakarta.validation.*;

import java.util.Set;

/**
 * 생성자에서 바로 자기 자신(필드에 붙은 @NotBlank 등)을 검증하고 싶은 DTO 의 베이스 클래스.
 * (참고 코드에 있던 디버그용 System.out.println("Valid Check!") 은 실서비스 로그에 의미 없이
 * 매번 찍히는 잔재라 제거했다.)
 */
public abstract class SelfValidating<T> {

    private final Validator validator;

    protected SelfValidating() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    protected void validateSelf() {
        Set<ConstraintViolation<T>> violations = validator.validate((T) this);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
