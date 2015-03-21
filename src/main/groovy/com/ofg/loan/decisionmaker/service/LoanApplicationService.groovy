package com.ofg.loan.decisionmaker.service

import com.netflix.hystrix.HystrixCommand
import com.ofg.infrastructure.web.resttemplate.fluent.ServiceRestClient
import com.ofg.loan.decisionmaker.LoanApplication
import com.ofg.loan.decisionmaker.LoanApplicationResult
import com.ofg.loan.decisionmaker.repository.LoanApplicationRepository
import groovy.text.SimpleTemplateEngine
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

import javax.validation.constraints.NotNull

@Slf4j
@RestController
@RequestMapping("/api/loanApplication")
class LoanApplicationService {

    @Autowired
    ServiceRestClient serviceRestClient;

    @Autowired
    LoanApplicationRepository loanApplicationRepository;

    @RequestMapping(value = "/{loanApplicationId}", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public void importApplication(
            @PathVariable("loanApplicationId") Long loanApplicationId,
            @RequestBody @NotNull LoanApplication loanApplication) {
        log.debug("Received loanApplication {}", loanApplication)

        String result;
        if ("fraud".equalsIgnoreCase(loanApplication.getFraudStatus())) {
            result = "FAILURE"
        } else if ("fishy".equalsIgnoreCase(loanApplication.getFraudStatus())) {
            result = "MANUAL"
        } else if ("good".equalsIgnoreCase(loanApplication.getFraudStatus())) {
            result = "SUCCESS"
        }

        sendStatusToReportingService(loanApplicationId, loanApplication, result);
        sendStatusToMarketingService(loanApplicationId, loanApplication, result);

        loanApplicationRepository.save(new LoanApplicationResult(loanApplicationId, result))
    }

    @RequestMapping(value = "/{loanApplicationId}", method = RequestMethod.GET, consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public LoanApplicationResult loanApplicationDecision(@PathVariable @NotNull long loanApplicationId) {
        log.debug("Received request for status of loan application with id {}", loanApplicationId)
        LoanApplicationResult loanApplicationResult = loanApplicationRepository.findOne(loanApplicationId)
        log.debug("Found in database: {}", loanApplicationResult)
        return loanApplicationResult;
    }

    private void sendStatusToReportingService(Long loanApplicationId, LoanApplication loanApplication, String result) {
        serviceRestClient.forService("reportingService").post()
                .withCircuitBreaker(HystrixCommand.Setter.withGroupKey({'reporting-service-lodz'}), {
            '{}'
        })
                .onUrl("/api/loans/decisions")
                .body(buildLoanReportingJson(loanApplicationId, loanApplication, result))
                .withHeaders()
                .contentTypeJson()
                .andExecuteFor()
                .anObject()
                .ofType(String)
    }

    private void sendStatusToMarketingService(Long loanApplicationId, LoanApplication loanApplication, String result) {
        serviceRestClient.forService("marketingService").put().
                withCircuitBreaker(HystrixCommand.Setter.withGroupKey({'marketing-offer-generator-lodz'}), {
                    '{}'
                })
                .onUrl("/api/marketing/" + loanApplicationId)
                .body(buildLoanMarketingJson(loanApplication, result))
                .withHeaders()
                .contentTypeJson()
                .andExecuteFor()
                .anObject()
                .ofType(String)
    }

    String buildLoanReportingJson(long applicationId, LoanApplication loanApplication, String result) {
        return new SimpleTemplateEngine().createTemplate(JSON_RESPONSE_TEMPLATE_REPORTING)
                .make([loanId     : applicationId,
                       job        : loanApplication.getJob(),
                       amount     : loanApplication.getAmount(),
                       fraudStatus: loanApplication.getFraudStatus(),
                       decision   : result])
                .toString();
    }

    String buildLoanMarketingJson(LoanApplication loanApplication, String result) {
        return new SimpleTemplateEngine().createTemplate(JSON_RESPONSE_TEMPLATE_MARKETING)
                .make([firstName: loanApplication.getFirstName(),
                       lastName : loanApplication.getLastName(),
                       decision : result])
                .toString();
    }


    private static final String JSON_RESPONSE_TEMPLATE_REPORTING = '''
                {
                    "loanId" : "$loanId",
                    "job" : "$job",
                    "amount" : $amount,
                    "fraudStatus" : "$fraudStatus",
                    "decision" : "$decision"
                }
                '''

    private static final String JSON_RESPONSE_TEMPLATE_MARKETING = '''
                {
                    "person" : {
                        "firstName": "$firstName",
                        "lastName": "$lastName',
                    },
                    "decision" : "$decision"
                }
                '''

}
