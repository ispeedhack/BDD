package com.eritten.backend.auth;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;

public class AccountVerificationRequestSteps {

    private AccountVerificationRequest request;
    private String email;
    private String code;

    @Given("a verification request with email {string} and code {string}")
    public void a_verification_request_with_email_and_code(String email, String code) {
        this.email = email;
        this.code = code;
    }

    @When("I create a verification request")
    public void i_create_a_verification_request() {
        request = new AccountVerificationRequest(email, code);
    }

    @Then("the email in the request should be {string}")
    public void the_email_in_the_request_should_be(String expectedEmail) {
        Assert.assertEquals(expectedEmail, request.getEmail());
    }

    @Then("the code in the request should be {string}")
    public void the_code_in_the_request_should_be(String expectedCode) {
        Assert.assertEquals(expectedCode, request.getCode());
    }
}
