package org.example.internshipassignmentkafka.utility;

public class ValidationMessages {
    private ValidationMessages() {}

    public static final String TITLE_NOT_BLANK = "Title cannot be blank";
    public static final String DESCRIPTION_NOT_BLANK = "Description cannot be blank";
    public static final String PRIORITY_NOT_NULL = "Priority cannot be null";
    public static final String DUE_DATE_NOT_NULL = "Due date cannot be null";
    public static final String TITLE_SIZE = "Title must be between 3 and 35 characters";
    public static final String DESCRIPTION_SIZE = "Description must be between 3 and 100 characters";

    public static final String DUE_DATE_FUTURE_OR_PRESENT = "Due date must be today or in the future";
}
