package judgels.jophiel.user.registration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import judgels.jophiel.DaggerJophielIntegrationTestComponent;
import judgels.jophiel.JophielIntegrationTestComponent;
import judgels.jophiel.JophielIntegrationTestHibernateModule;
import judgels.jophiel.persistence.UserRegistrationEmailModel;
import judgels.persistence.hibernate.WithHibernateSession;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@WithHibernateSession(models = {UserRegistrationEmailModel.class})
class UserRegistrationEmailStoreIntegrationTests {
    private static final String USER_JID = "userJid";

    private UserRegistrationEmailStore store;

    @BeforeEach
    void before(SessionFactory sessionFactory) {
        JophielIntegrationTestComponent component = DaggerJophielIntegrationTestComponent.builder()
                .jophielIntegrationTestHibernateModule(new JophielIntegrationTestHibernateModule(sessionFactory))
                .build();
        store = component.userRegistrationEmailStore();
    }

    @Test
    void user_with_missing_entry_is_considered_verified() {
        assertThat(store.isUserActivated(USER_JID)).isTrue();
    }

    @Test
    void cannot_verify_nonexistent_email_code() {
        assertThat(store.verifyEmailCode("nonexistent")).isFalse();
    }

    @Test
    void can_generate_email_code_and_verify() {
        String emailCode = store.generateEmailCode(USER_JID);
        assertThat(store.isUserActivated(USER_JID)).isFalse();

        assertThat(store.verifyEmailCode(emailCode)).isTrue();
        assertThat(store.isUserActivated(USER_JID)).isTrue();

        assertThat(store.verifyEmailCode(emailCode)).isFalse();
    }
}