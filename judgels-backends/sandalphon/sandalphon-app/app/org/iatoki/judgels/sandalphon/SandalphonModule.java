package org.iatoki.judgels.sandalphon;

import akka.actor.ActorSystem;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.nio.file.Paths;
import java.time.Clock;
import javax.inject.Named;
import javax.inject.Singleton;
import judgels.fs.FileSystem;
import judgels.fs.local.LocalFileSystem;
import judgels.messaging.MessageClient;
import judgels.persistence.ActorProvider;
import judgels.sandalphon.SandalphonConfiguration;
import judgels.sandalphon.submission.programming.BaseSubmissionStore;
import judgels.sandalphon.submission.programming.SubmissionClient;
import judgels.sandalphon.submission.programming.SubmissionRegradeProcessor;
import judgels.sandalphon.submission.programming.SubmissionSourceBuilder;
import judgels.sandalphon.submission.programming.SubmissionStore;
import judgels.service.actor.JudgelsActorProvider;
import judgels.service.client.ClientChecker;
import org.hibernate.SessionFactory;
import org.iatoki.judgels.Git;
import org.iatoki.judgels.LocalGit;
import org.iatoki.judgels.play.general.GeneralConfig;
import org.iatoki.judgels.play.model.PlaySessionFactory;
import org.iatoki.judgels.sandalphon.lesson.LessonFs;
import org.iatoki.judgels.sandalphon.lesson.LessonGit;
import org.iatoki.judgels.sandalphon.problem.base.ProblemFs;
import org.iatoki.judgels.sandalphon.problem.base.ProblemGit;
import org.iatoki.judgels.sandalphon.problem.base.submission.SubmissionFs;
import org.iatoki.judgels.sandalphon.problem.programming.grading.GradingResponsePoller;
import org.iatoki.judgels.sandalphon.problem.programming.grading.GradingResponseProcessor;
import org.iatoki.judgels.sandalphon.problem.programming.grading.ProgrammingGradingDao;
import org.iatoki.judgels.sandalphon.problem.programming.submission.ProgrammingSubmissionDao;
import org.iatoki.judgels.sandalphon.problem.programming.submission.SubmissionRegrader;
import play.db.jpa.JPAApi;

public final class SandalphonModule extends AbstractModule {

    @Override
    public void configure() {
        Config config = ConfigFactory.load();

        GeneralConfig generalConfig = new GeneralConfig(config, "Sandalphon", "0.0");
        bind(GeneralConfig.class).toInstance(generalConfig);

        bind(SandalphonConfiguration.class).toInstance(SandalphonProperties.build(config));

        bind(SandalphonThreadsScheduler.class).asEagerSingleton();

        bind(ObjectMapper.class).toProvider(PlayObjectMapper.class).asEagerSingleton();

        bind(SessionFactory.class).to(PlaySessionFactory.class);
        bind(ActorProvider.class).to(JudgelsActorProvider.class);
        bind(Clock.class).toInstance(Clock.systemUTC());
    }

    @Provides
    @Singleton
    SubmissionStore submissionStore(
            ObjectMapper objectMapper,
            ProgrammingSubmissionDao submissionDao,
            ProgrammingGradingDao gradingDao) {
        return new BaseSubmissionStore<>(submissionDao, gradingDao, objectMapper);
    }

    @Provides
    @Singleton
    SubmissionClient submissionClient(
            SubmissionStore submissionStore,
            @Named("gradingRequestQueueName") String gradingRequestQueueName,
            @Named("gradingResponseQueueName") String gradingResponseQueueName,
            MessageClient messageClient,
            ObjectMapper mapper) {

        return new SubmissionClient(
                submissionStore,
                gradingRequestQueueName,
                gradingResponseQueueName,
                messageClient,
                mapper);
    }

    @Provides
    @Singleton
    SubmissionRegrader submissionRegrader(
            ActorSystem actorSystem,
            SubmissionStore submissionStore,
            SubmissionRegradeProcessor processor) {

        return new SubmissionRegrader(
                submissionStore,
                actorSystem.scheduler(),
                actorSystem.dispatcher(),
                processor);
    }

    @Provides
    @Singleton
    SubmissionSourceBuilder submissionSourceBuilder(@SubmissionFs FileSystem submissionFs) {
        return new SubmissionSourceBuilder(submissionFs);
    }

    @Provides
    @Singleton
    GradingResponsePoller gradingResponsePoller(
            ActorSystem actorSystem,
            @Named("gradingResponseQueueName") String queueName,
            MessageClient messageClient,
            GradingResponseProcessor processor) {

        return new GradingResponsePoller(
                queueName,
                messageClient,
                actorSystem.scheduler(),
                actorSystem.dispatcher(),
                processor);
    }

    @Provides
    @Singleton
    GradingResponseProcessor gradingResponseProcessor(
            JPAApi jpaApi,
            ObjectMapper mapper,
            MessageClient messageClient,
            SubmissionStore submissionStore) {

        return new GradingResponseProcessor(
                jpaApi,
                mapper,
                submissionStore,
                messageClient);
    }

    @Provides
    @Singleton
    ClientChecker clientChecker(SandalphonConfiguration config) {
        return new ClientChecker(config.getClients());
    }

    @Provides
    @Singleton
    @ProblemFs
    FileSystem problemFs(SandalphonConfiguration config) {
        return new LocalFileSystem(Paths.get(config.getBaseDataDir()));
    }

    @Provides
    @Singleton
    @SubmissionFs
    FileSystem submissionFs(SandalphonConfiguration config) {
        return new LocalFileSystem(Paths.get(config.getBaseDataDir(), "submissions"));
    }

    @Provides
    @Singleton
    @LessonFs
    FileSystem lessonFs(SandalphonConfiguration config) {
        return new LocalFileSystem(Paths.get(config.getBaseDataDir()));
    }

    @Provides
    @Singleton
    @ProblemGit
    Git problemGit(@ProblemFs FileSystem fs) {
        return new LocalGit((LocalFileSystem) fs);
    }

    @Provides
    @Singleton
    @LessonGit
    Git lessonGit(@LessonFs FileSystem fs) {
        return new LocalGit((LocalFileSystem) fs);
    }
}
