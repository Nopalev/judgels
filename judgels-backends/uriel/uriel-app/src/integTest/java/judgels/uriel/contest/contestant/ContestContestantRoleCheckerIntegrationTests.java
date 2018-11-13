package judgels.uriel.contest.contestant;

import static java.time.temporal.ChronoUnit.HOURS;
import static judgels.persistence.TestClock.NOW;
import static org.assertj.core.api.Assertions.assertThat;

import judgels.uriel.api.contest.Contest;
import judgels.uriel.api.contest.ContestCreateData;
import judgels.uriel.api.contest.ContestUpdateData;
import judgels.uriel.api.contest.supervisor.SupervisorPermissionType;
import judgels.uriel.contest.role.AbstractRoleCheckerIntegrationTests;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ContestContestantRoleCheckerIntegrationTests extends AbstractRoleCheckerIntegrationTests {
    private ContestContestantRoleChecker checker;

    @BeforeEach
    void setUpSession(SessionFactory sessionFactory) {
        prepare(sessionFactory);
        checker = component.contestContestantRoleChecker();
    }

    @Test
    void register() {
        Contest contestAFinished = contestStore.createContest(
                new ContestCreateData.Builder().slug("contest-a-finished").build());
        contestAFinished = contestStore.updateContest(
                contestAFinished.getJid(),
                new ContestUpdateData.Builder().beginTime(NOW.minus(10, HOURS)).build()).get();

        moduleStore.upsertRegistrationModule(contestAFinished.getJid());

        assertThat(checker.canRegister(ADMIN, contestA)).isTrue();
        assertThat(checker.canRegister(ADMIN, contestAStarted)).isTrue();
        assertThat(checker.canRegister(ADMIN, contestAFinished)).isFalse();
        assertThat(checker.canRegister(ADMIN, contestB)).isFalse();
        assertThat(checker.canRegister(ADMIN, contestBStarted)).isFalse();
        assertThat(checker.canRegister(ADMIN, contestC)).isFalse();

        assertThat(checker.canRegister(USER, contestA)).isTrue();
        assertThat(checker.canRegister(USER, contestAStarted)).isTrue();
        assertThat(checker.canRegister(USER, contestAFinished)).isFalse();
        assertThat(checker.canRegister(USER, contestB)).isFalse();
        assertThat(checker.canRegister(USER, contestBStarted)).isFalse();
        assertThat(checker.canRegister(USER, contestC)).isFalse();

        assertThat(checker.canRegister(CONTESTANT, contestA)).isTrue();
        assertThat(checker.canRegister(CONTESTANT, contestAStarted)).isTrue();
        assertThat(checker.canRegister(CONTESTANT, contestAFinished)).isFalse();
        assertThat(checker.canRegister(CONTESTANT, contestB)).isFalse();
        assertThat(checker.canRegister(CONTESTANT, contestBStarted)).isFalse();
        assertThat(checker.canRegister(CONTESTANT, contestC)).isFalse();

        assertThat(checker.canRegister(SUPERVISOR, contestA)).isTrue();
        assertThat(checker.canRegister(SUPERVISOR, contestAStarted)).isTrue();
        assertThat(checker.canRegister(SUPERVISOR, contestAFinished)).isFalse();
        assertThat(checker.canRegister(SUPERVISOR, contestB)).isFalse();
        assertThat(checker.canRegister(SUPERVISOR, contestBStarted)).isFalse();
        assertThat(checker.canRegister(SUPERVISOR, contestC)).isFalse();

        assertThat(checker.canRegister(MANAGER, contestA)).isTrue();
        assertThat(checker.canRegister(MANAGER, contestAStarted)).isTrue();
        assertThat(checker.canRegister(MANAGER, contestAFinished)).isFalse();
        assertThat(checker.canRegister(MANAGER, contestB)).isFalse();
        assertThat(checker.canRegister(MANAGER, contestBStarted)).isFalse();
        assertThat(checker.canRegister(MANAGER, contestC)).isFalse();
    }

    @Test
    void unregister() {
        assertThat(checker.canUnregister(ADMIN, contestA)).isFalse();
        assertThat(checker.canUnregister(ADMIN, contestAStarted)).isFalse();
        assertThat(checker.canUnregister(ADMIN, contestB)).isFalse();
        assertThat(checker.canUnregister(ADMIN, contestBStarted)).isFalse();
        assertThat(checker.canUnregister(ADMIN, contestC)).isFalse();

        assertThat(checker.canUnregister(USER, contestA)).isFalse();
        assertThat(checker.canUnregister(USER, contestAStarted)).isFalse();
        assertThat(checker.canUnregister(USER, contestB)).isFalse();
        assertThat(checker.canUnregister(USER, contestBStarted)).isFalse();
        assertThat(checker.canUnregister(USER, contestC)).isFalse();

        assertThat(checker.canUnregister(CONTESTANT, contestA)).isFalse();
        assertThat(checker.canUnregister(CONTESTANT, contestAStarted)).isFalse();
        assertThat(checker.canUnregister(CONTESTANT, contestB)).isFalse();
        assertThat(checker.canUnregister(CONTESTANT, contestBStarted)).isFalse();
        moduleStore.upsertRegistrationModule(contestB.getJid());
        assertThat(checker.canUnregister(CONTESTANT, contestB)).isTrue();
        assertThat(checker.canUnregister(CONTESTANT, contestBStarted)).isFalse();
        assertThat(checker.canUnregister(CONTESTANT, contestC)).isFalse();

        assertThat(checker.canUnregister(SUPERVISOR, contestA)).isFalse();
        assertThat(checker.canUnregister(SUPERVISOR, contestAStarted)).isFalse();
        assertThat(checker.canUnregister(SUPERVISOR, contestB)).isFalse();
        assertThat(checker.canUnregister(SUPERVISOR, contestBStarted)).isFalse();
        assertThat(checker.canUnregister(SUPERVISOR, contestC)).isFalse();

        assertThat(checker.canUnregister(MANAGER, contestA)).isFalse();
        assertThat(checker.canUnregister(MANAGER, contestAStarted)).isFalse();
        assertThat(checker.canUnregister(MANAGER, contestB)).isFalse();
        assertThat(checker.canUnregister(MANAGER, contestBStarted)).isFalse();
        assertThat(checker.canUnregister(MANAGER, contestC)).isFalse();
    }

    @Test
    void view_list() {
        assertThat(checker.canViewList(ADMIN, contestA)).isTrue();
        assertThat(checker.canViewList(ADMIN, contestB)).isTrue();
        assertThat(checker.canViewList(ADMIN, contestC)).isTrue();

        assertThat(checker.canViewList(USER, contestA)).isTrue();
        assertThat(checker.canViewList(USER, contestB)).isFalse();
        assertThat(checker.canViewList(USER, contestC)).isFalse();

        assertThat(checker.canViewList(CONTESTANT, contestA)).isTrue();
        assertThat(checker.canViewList(CONTESTANT, contestB)).isTrue();
        assertThat(checker.canViewList(CONTESTANT, contestC)).isFalse();

        assertThat(checker.canViewList(SUPERVISOR, contestA)).isTrue();
        assertThat(checker.canViewList(SUPERVISOR, contestB)).isTrue();
        assertThat(checker.canViewList(SUPERVISOR, contestC)).isFalse();

        assertThat(checker.canViewList(MANAGER, contestA)).isTrue();
        assertThat(checker.canViewList(MANAGER, contestB)).isTrue();
        assertThat(checker.canViewList(MANAGER, contestC)).isFalse();
    }

    @Test
    void supervise() {
        assertThat(checker.canSupervise(ADMIN, contestA)).isTrue();
        assertThat(checker.canSupervise(ADMIN, contestB)).isTrue();
        assertThat(checker.canSupervise(ADMIN, contestC)).isTrue();

        assertThat(checker.canSupervise(USER, contestA)).isFalse();
        assertThat(checker.canSupervise(USER, contestB)).isFalse();
        assertThat(checker.canSupervise(USER, contestC)).isFalse();

        assertThat(checker.canSupervise(CONTESTANT, contestA)).isFalse();
        assertThat(checker.canSupervise(CONTESTANT, contestB)).isFalse();
        assertThat(checker.canSupervise(CONTESTANT, contestC)).isFalse();

        assertThat(checker.canSupervise(SUPERVISOR, contestA)).isFalse();
        assertThat(checker.canSupervise(SUPERVISOR, contestB)).isFalse();
        addSupervisorToContestBWithPermission(SupervisorPermissionType.CONTESTANT);
        assertThat(checker.canSupervise(SUPERVISOR, contestB)).isTrue();
        assertThat(checker.canSupervise(SUPERVISOR, contestC)).isFalse();

        assertThat(checker.canSupervise(MANAGER, contestA)).isFalse();
        assertThat(checker.canSupervise(MANAGER, contestB)).isTrue();
        assertThat(checker.canSupervise(MANAGER, contestC)).isFalse();
    }
}
