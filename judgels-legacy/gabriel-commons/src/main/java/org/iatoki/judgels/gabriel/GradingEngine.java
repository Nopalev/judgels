package org.iatoki.judgels.gabriel;

import judgels.gabriel.api.GradingConfig;
import judgels.gabriel.api.GradingLanguage;
import judgels.gabriel.api.ScoringException;
import org.iatoki.judgels.gabriel.sandboxes.SandboxFactory;

import java.io.File;
import java.io.IOException;

public interface GradingEngine {

    String getName();

    GradingConfig createDefaultGradingConfig();

    GradingConfig createGradingConfigFromJson(String json) throws IOException;

    GradingResult grade(File gradingDir, GradingConfig config, GradingLanguage language, GradingSource source, SandboxFactory sandboxFactory) throws GradingException, ScoringException;
}
