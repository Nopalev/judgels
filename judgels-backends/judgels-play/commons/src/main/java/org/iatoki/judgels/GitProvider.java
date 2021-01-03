package org.iatoki.judgels;

import java.nio.file.Path;
import java.util.List;

public interface GitProvider {

    void init(Path rootDirPath);

    void clone(Path originDirPath, Path rootDirPath);

    boolean fetch(Path rootDirPath);

    void addAll(Path rootDirPath);

    void commit(Path rootDirPath, String committerName, String committerEmail, String title, String description);

    boolean rebase(Path rootDirPath);

    boolean push(Path rootDirPath);

    void resetToParent(Path rootDirPath);

    void resetHard(Path rootDirPath);

    List<GitCommit> getLog(Path rootDirPath);

    void restore(Path rootDirPath, String hash);
}
