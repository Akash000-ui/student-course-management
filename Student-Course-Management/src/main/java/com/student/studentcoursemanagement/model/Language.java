package com.student.studentcoursemanagement.model;

public enum Language {
    ENGLISH("English"),
    HINDI("Hindi"),
    TELUGU("Telugu");

    private final String displayName;

    Language(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}