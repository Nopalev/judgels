package judgels.jophiel.api.user.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.awaitility.Awaitility.await;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import judgels.jophiel.api.AbstractServiceIntegrationTests;
import judgels.jophiel.api.session.Credentials;
import judgels.jophiel.api.session.SessionErrors;
import judgels.jophiel.api.session.SessionService;
import judgels.jophiel.api.user.UserData;
import judgels.jophiel.api.user.UserService;
import org.junit.jupiter.api.Test;
import org.subethamail.wiser.Wiser;

class UserAccountServiceIntegrationTests extends AbstractServiceIntegrationTests {
    private final UserService userService = createService(UserService.class);
    private final UserAccountService accountService = createService(UserAccountService.class);
    private final SessionService sessionService = createService(SessionService.class);

    @Test
    void register_activate_user() {
        Wiser wiser = new Wiser();
        wiser.setPort(2500);
        wiser.start();

        accountService.registerUser(new UserRegistrationData.Builder()
                .username("beta")
                .name("Beta")
                .password("pass")
                .email("beta@domain.com")
                .build());
        Credentials credentials = Credentials.of("beta", "pass");

        String email = readEmail(wiser, 0);

        assertForbidden(() -> sessionService.logIn(credentials))
                .hasMessageContaining(SessionErrors.USER_NOT_ACTIVATED);

        String emailCode = extractEmailCode(email);

        accountService.activateUser(emailCode);
        assertPermitted(() -> sessionService.logIn(credentials));

        wiser.stop();
    }

    @Test
    void resend_activation_email() {
        Wiser wiser = new Wiser();
        wiser.setPort(2500);
        wiser.start();

        assertNotFound(() -> accountService.resendActivationEmail("nonexistent"));

        accountService.registerUser(new UserRegistrationData.Builder()
                .username("alfa")
                .name("Alfa")
                .password("pass")
                .email("alfa@domain.com")
                .build());

        String email = readEmail(wiser, 0);
        String emailCode1 = extractEmailCode(email);

        assertThatCode(() -> accountService.resendActivationEmail("alfa@domain.com"))
                .doesNotThrowAnyException();

        email = readEmail(wiser, 1);
        String emailCode2 = extractEmailCode(email);
        assertThat(emailCode2).isEqualTo(emailCode1);

        assertPermitted(() -> accountService.activateUser(emailCode2));
        assertNotFound(() -> accountService.resendActivationEmail("alfa@domain.com"));

        wiser.stop();
    }

    @Test
    void reset_password() {
        Wiser wiser = new Wiser();
        wiser.setPort(2500);
        wiser.start();

        assertThatCode(() -> accountService.requestToResetPassword("delta@domain.com"))
                .doesNotThrowAnyException();

        userService.createUser(adminHeader, new UserData.Builder()
                .username("delta")
                .password("pass")
                .email("delta@domain.com")
                .build());

        accountService.requestToResetPassword("delta@domain.com");

        assertThatCode(() -> sessionService.logIn(Credentials.of("delta", "pass")))
                .doesNotThrowAnyException();

        String email = readEmail(wiser, 0);
        String emailCode = extractEmailCode(email);

        accountService.requestToResetPassword("delta@domain.com");
        String email2 = readEmail(wiser, 1);
        String emailCode2 = extractEmailCode(email2);
        assertThat(emailCode2).isEqualTo(emailCode);

        accountService.resetPassword(PasswordResetData.of(emailCode, "newPass"));

        readEmail(wiser, 2);

        accountService.requestToResetPassword("delta@domain.com");
        String email3 = readEmail(wiser, 3);
        String emailCode3 = extractEmailCode(email3);
        assertThat(emailCode3).isNotEqualTo(emailCode2);

        wiser.stop();
    }

    private static String readEmail(Wiser wiser, int index) {
        await()
                .atMost(30, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .until(() -> wiser.getMessages().size() > index);

        return new String(wiser.getMessages().get(index).getData());
    }

    private static String extractEmailCode(String email) {
        Pattern pattern = Pattern.compile("^.*#(\\w*)#.*$", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(email);
        matcher.matches();
        return matcher.group(1);
    }
}
