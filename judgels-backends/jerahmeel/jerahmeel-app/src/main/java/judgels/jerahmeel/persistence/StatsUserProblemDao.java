package judgels.jerahmeel.persistence;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import judgels.persistence.Dao;
import judgels.persistence.api.SelectionOptions;

public interface StatsUserProblemDao extends Dao<StatsUserProblemModel> {
    Optional<StatsUserProblemModel> selectByUserJidAndProblemJid(String userJid, String problemJid);
    List<StatsUserProblemModel> selectAllByUserJidAndProblemJids(String userJid, Set<String> problemJids);
    List<StatsUserProblemModel> selectAllByProblemJid(String problemJid, SelectionOptions options);
    Map<String, Long> selectCountsAcceptedByProblemJids(Set<String> problemJids);
    Map<String, Long> selectCountsTriedByProblemJids(Set<String> problemJids);
}