package com.example.Bean.model;

import java.util.List;

public class MonthData {
    private List<DayData> days;

    public MonthData(List<DayData> days) {
        this.days = days;
    }

    public List<DayData> getDays() {
        return days;
    }

    public void setDays(List<DayData> days) {
        this.days = days;
    }
}



