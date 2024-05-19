import org.jbehave.core.annotations.*;
import static org.junit.Assert.assertTrue;

public class RegistrationSteps {

    private User user;

    @Given("a new user with username $username")
        public void givenANewUser(String username) {
        user = new User(username);
    }

    @When("the user registers with email $email and password $password")
        public void whenTheUserRegisters(String email, String password) { 
    }

    @Then("the registration is successful")
        public void thenTheRegistrationIsSuccessful() {
        assertTrue();
    }
}
