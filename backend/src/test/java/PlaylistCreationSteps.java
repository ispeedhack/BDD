import org.jbehave.core.annotations.*;
import static org.junit.Assert.assertTrue;

public class PlaylistCreationSteps {

    private User user;
    private String playlistName;

    @Given("a logged-in user with username $username")
        public void givenALoggedInUser(String username) {
        user = new User(username);
        // Виконати вхід в обліковий запис користувача
    }

    @When("the user creates a playlist with name $name")
        public void whenTheUserCreatesPlaylist(String name) {
        playlistName = name;
        // Виконати створення плейлиста з вказаною назвою
    }

    @Then("the playlist is created successfully")
        public void thenThePlaylistIsCreatedSuccessfully() {
        assertTrue(/* Перевірка, чи плейлист створено успішно */);
    }
}
