package org.pillarone.riskanalytics.graph.formeditor.ui.model.checkers;

import com.ulcjava.applicationframework.application.form.model.PropertyValidator;

@SuppressWarnings("serial")
public class PropertySpellChecker extends PropertyValidator<String> {

    public PropertySpellChecker(String... propertyNames) {
        super(propertyNames);
    }

    public String validateValue(String value) {
        if (value == null || value.trim().length() == 0) {
            return "Null or a zero length string is not a valid name!";
        }
        char firstCharacter = value.charAt(0);
        if (Character.isDigit(firstCharacter)) {
            return "Name field must not start with digit";
        }
        return null;
    }
}
