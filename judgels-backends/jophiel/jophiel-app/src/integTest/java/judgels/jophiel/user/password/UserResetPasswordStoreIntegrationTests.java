package judgels.jophiel.user.password;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.util.concurrent.Uninterruptibles;
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import judgels.jophiel.hibernate.UserResetPasswordHibernateDao;
import judgels.jophiel.persistence.UserResetPasswordDao;
import judgels.jophiel.persistence.UserResetPasswordModel;
import judgels.persistence.FixedActorProvider;
import judgels.persistence.hibernate.WithHibernateSession;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@WithHibernateSession(models = {UserResetPasswordModel.class})
class UserResetPasswordStoreIntegrationTests {
    private static final String USER_JID = "userJid";

    private Session currentSession;
    private UserResetPasswordStore store;

    @BeforeEach
    void before(SessionFactory sessionFactory) {
        currentSession = sessionFactory.getCurrentSession();

        UserResetPasswordDao userResetPasswordDao =
                new UserResetPasswordHibernateDao(sessionFactory, Clock.systemUTC(), new FixedActorProvider());
        store = new UserResetPasswordStore(userResetPasswordDao);
    }

    @Test
    void can_generate_find_consume_code() {
        assertThat(store.consumeEmailCode("code", Duration.ofHours(1))).isEmpty();

        store.generateEmailCode("userJid2", Duration.ofHours(1));

        String code = store.generateEmailCode(USER_JID, Duration.ofHours(1));
        assertThat(store.consumeEmailCode(code, Duration.ofHours(1))).contains(USER_JID);

        currentSession.flush();

        assertThat(store.consumeEmailCode(code, Duration.ofHours(1))).isEmpty();
        String newCode = store.generateEmailCode(USER_JID, Duration.ofHours(1));
        assertThat(newCode).isNotEqualTo(code);
    }

    @Test
    void same_code_is_returned_if_not_expired() {
        String code1 = store.generateEmailCode(USER_JID, Duration.ofHours(1));
        String code2 = store.generateEmailCode(USER_JID, Duration.ofHours(1));
        assertThat(code1).isEqualTo(code2);
    }

    @Test
    void different_code_is_returned_if_expired() {
        String code1 = store.generateEmailCode(USER_JID, Duration.ofHours(1));
        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
        String code2 = store.generateEmailCode(USER_JID, Duration.ofMillis(700));
        assertThat(code1).isNotEqualTo(code2);
    }
}