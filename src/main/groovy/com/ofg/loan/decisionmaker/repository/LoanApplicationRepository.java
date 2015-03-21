package com.ofg.loan.decisionmaker.repository;

import com.ofg.loan.decisionmaker.LoanApplicationResult;
import org.springframework.data.repository.CrudRepository;

public interface LoanApplicationRepository extends CrudRepository<LoanApplicationResult, Long> {

}
