package com.ofg.loan.decisionmaker;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class LoanApplicationResult {
    public LoanApplicationResult(Long applicationId, String result) {
        this.setApplicationId(applicationId);
        this.setResult(result);
    }

    public LoanApplicationResult() {
    }

    @Id
    private Long applicationId;

    @Column(name = "result")
    private String result;

    public String toString() {
        return "applicationId " + this.getApplicationId() + " " + " result " + this.getResult();
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
