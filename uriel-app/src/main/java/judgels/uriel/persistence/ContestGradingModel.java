package judgels.uriel.persistence;

import javax.persistence.Entity;
import judgels.sandalphon.persistence.AbstractGradingModel;

@SuppressWarnings("checkstyle:visibilitymodifier")
@Entity(name = "uriel_contest_programming_grading")
public class ContestGradingModel extends AbstractGradingModel {
    public ContestGradingModel() {}

    public ContestGradingModel(
            long id,
            String jid,
            String submissionJid,
            String verdictCode,
            String verdictName,
            int score) {

        this.id = id;
        this.jid = jid;
        this.submissionJid = submissionJid;
        this.verdictCode = verdictCode;
        this.verdictName = verdictName;
        this.score = score;
    }
}
