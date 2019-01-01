package judgels.uriel.api.contest.problem;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.Optional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import judgels.service.api.actor.AuthHeader;

@Path("/api/v2/contests/{contestJid}/problems")
public interface ContestProblemService {
    @POST
    @Path("/")
    @Consumes(APPLICATION_JSON)
    void upsertProblem(
            @HeaderParam(AUTHORIZATION) AuthHeader authHeader,
            @PathParam("contestJid") String contestJid,
            ContestProblemData data);

    @GET
    @Path("/")
    @Produces(APPLICATION_JSON)
    ContestProblemsResponse getProblems(
            @HeaderParam(AUTHORIZATION) Optional<AuthHeader> authHeader,
            @PathParam("contestJid") String contestJid);

    @GET
    @Path("/{problemAlias}/worksheet")
    @Produces(APPLICATION_JSON)
    ContestProblemWorksheet getProblemWorksheet(
            @HeaderParam(AUTHORIZATION) Optional<AuthHeader> authHeader,
            @PathParam("contestJid") String contestJid,
            @PathParam("problemAlias") String problemAlias,
            @QueryParam("language") Optional<String> language);
}
