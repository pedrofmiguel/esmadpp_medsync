package com.example.login2;

import java.util.Date;

public class Drugs {

    public String drugId;
    public String userId;
    public String drugName;
    public int drugNum;
    public String drugDate;
    public int personsUsing;

    public Drugs() {
        // Default constructor required for calls to DataSnapshot.getValue(Drugs.class)
    }

    public Drugs(String drugId, String userId, String drugName, int drugNum, String drugDate, int personsUsing) {
        this.drugId = drugId;
        this.userId = userId;
        this.drugName = drugName;
        this.drugNum = drugNum;
        this.drugDate = drugDate;
        this.personsUsing = personsUsing;
    }

    public String getDrugId() {
        return drugId;
    }

    public void setDrugId(String drugId) {
        this.drugId = drugId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDrugName() {
        return drugName;
    }

    public void setDrugName(String drugName) {
        this.drugName = drugName;
    }

    public int getDrugNum() {
        return drugNum;
    }

    public void setDrugNum(int drugNum) {
        this.drugNum = drugNum;
    }

    public String getDrugDate() {
        return drugDate;
    }

    public void setDrugDate(String drugDate) {
        this.drugDate = drugDate;
    }

    public int getPersonsUsing() {
        return personsUsing;
    }

    public void setPersonsUsing(int personsUsing) {
        this.personsUsing = personsUsing;
    }
}
