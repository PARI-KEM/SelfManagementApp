package com.example.Bean.model;

public class DayData {
    private int dayNumber;
    private String note;

    public DayData(int dayNumber, String note) {
        this.dayNumber = dayNumber;
        this.note = note;
    }

    public int getDayNumber() {
        return dayNumber;
    }

    public void setDayNumber(int dayNumber) {
        this.dayNumber = dayNumber;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
