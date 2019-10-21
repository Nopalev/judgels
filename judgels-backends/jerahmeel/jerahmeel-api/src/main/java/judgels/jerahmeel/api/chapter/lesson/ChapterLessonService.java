package judgels.jerahmeel.api.chapter.lesson;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.Optional;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import judgels.service.api.actor.AuthHeader;

@Path("/api/v2/chapters/{chapterJid}/lessons")
public interface ChapterLessonService {
    @GET
    @Path("/")
    @Produces(APPLICATION_JSON)
    ChapterLessonsResponse getLessons(
            @HeaderParam(AUTHORIZATION) Optional<AuthHeader> authHeader,
            @PathParam("chapterJid") String chapterJid);
}
