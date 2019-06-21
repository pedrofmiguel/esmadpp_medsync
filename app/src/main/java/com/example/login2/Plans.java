package com.example.login2;

public class Plans {

    public String userId;
    public String planId;
    public String planTitle;
    public String disease;
    public String personName;
    public String drugName;
    public int drugNum;
    public String drugDate;
    public String drugDays;

    public Plans() {
        // Default constructor required for calls to DataSnapshot.getValue(Drugs.class)
    }

    public Plans(String userId, String planId, String planTitle, String disease, String personName, String drugName, int drugNum, String drugDate, String drugDays) {
        this.userId = userId;
        this.planId = planId;
        this.planTitle = planTitle;
        this.disease = disease;
        this.personName = personName;
        this.drugName = drugName;
        this.drugNum = drugNum;
        this.drugDate = drugDate;
        this.drugDays = drugDays;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getPlanTitle() {
        return planTitle;
    }

    public void setPlanTitle(String planTitle) {
        this.planTitle = planTitle;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getDrugName() {
        return drugName;
    }

    public void setDrugName(String drugName) {
        this.drugName = drugName;
    }

    public String getDisease() {
        return disease;
    }

    public void setDisease(String disease) {
        this.disease = disease;
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

    public String getDrugDays() {
        return drugDays;
    }

    public void setDrugDays(String drugDays) {
        this.drugDays = drugDays;
    }
}
