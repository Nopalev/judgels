package judgels.uriel.api.contest.contestant;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Map;
import judgels.jophiel.api.profile.Profile;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableContestContestantUpsertResponse.class)
public interface ContestContestantUpsertResponse {
    Map<String, Profile> getInsertedContestantProfilesMap();
    Map<String, Profile> getAlreadyContestantProfilesMap();

    class Builder extends ImmutableContestContestantUpsertResponse.Builder {}
}
