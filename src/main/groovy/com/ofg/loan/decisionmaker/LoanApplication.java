package com.ofg.loan.decisionmaker;

import java.math.BigDecimal;

public class LoanApplication {
    private String firstName;
    private String lastName;
    private String job;
    private BigDecimal amount;
    private String fraudStatus;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getFraudStatus() {
        return fraudStatus;
    }

    public void setFraudStatus(String fraudStatus) {
        this.fraudStatus = fraudStatus;
    }

    public String toString() {
        return this.firstName + " " + this.lastName + " " + this.getJob() + " " + this.getFraudStatus() + "" + this.getAmount();
    }
}
