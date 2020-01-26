package com.creactiviti.piper.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SafeTextValidator implements ConstraintValidator<SafeText, String> {

    private Pattern safeTextPattern;

    @Override
    public void initialize(SafeText safeText) {
        safeTextPattern = Pattern.compile(safeText.regex());
    }

    @Override
    public boolean isValid(String input, ConstraintValidatorContext constraintValidatorContext) {
        if (input != null) {
            Matcher matcher = safeTextPattern.matcher(input);
            return matcher.matches();
        }
        return true;
    }
}
