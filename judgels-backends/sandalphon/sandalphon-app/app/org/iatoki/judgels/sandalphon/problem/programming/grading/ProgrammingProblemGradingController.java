package org.iatoki.judgels.sandalphon.problem.programming.grading;

import static judgels.service.ServiceUtils.checkAllowed;
import static judgels.service.ServiceUtils.checkFound;

import java.io.File;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import judgels.fs.FileInfo;
import judgels.gabriel.api.GradingConfig;
import judgels.gabriel.api.LanguageRestriction;
import judgels.gabriel.engines.GradingEngineRegistry;
import judgels.sandalphon.api.problem.Problem;
import org.iatoki.judgels.play.template.HtmlTemplate;
import org.iatoki.judgels.sandalphon.problem.base.AbstractProblemController;
import org.iatoki.judgels.sandalphon.problem.base.ProblemRoleChecker;
import org.iatoki.judgels.sandalphon.problem.base.ProblemStore;
import org.iatoki.judgels.sandalphon.problem.programming.ProgrammingProblemStore;
import org.iatoki.judgels.sandalphon.problem.programming.grading.html.autoPopulationLayout;
import org.iatoki.judgels.sandalphon.problem.programming.grading.html.editGradingEngineView;
import org.iatoki.judgels.sandalphon.problem.programming.grading.html.editLanguageRestrictionView;
import org.iatoki.judgels.sandalphon.problem.programming.grading.html.listGradingHelperFilesView;
import org.iatoki.judgels.sandalphon.problem.programming.grading.html.listGradingTestDataFilesView;
import org.iatoki.judgels.sandalphon.problem.programming.grading.html.tokilibLayout;
import org.iatoki.judgels.sandalphon.resource.UploadFileForm;
import play.api.mvc.Call;
import play.data.Form;
import play.db.jpa.Transactional;
import play.filters.csrf.AddCSRFToken;
import play.filters.csrf.RequireCSRFCheck;
import play.mvc.Http;
import play.mvc.Result;

@Singleton
public final class ProgrammingProblemGradingController extends AbstractProblemController {
    private final ProblemStore problemStore;
    private final ProblemRoleChecker problemRoleChecker;
    private final ProgrammingProblemStore programmingProblemStore;

    @Inject
    public ProgrammingProblemGradingController(
            ProblemStore problemStore,
            ProblemRoleChecker problemRoleChecker,
            ProgrammingProblemStore programmingProblemStore) {

        super(problemStore, problemRoleChecker);
        this.problemStore = problemStore;
        this.problemRoleChecker = problemRoleChecker;
        this.programmingProblemStore = programmingProblemStore;
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result editGradingEngine(Http.Request req, long problemId) {
        String actorJid = getUserJid(req);
        Problem problem = checkFound(problemStore.findProblemById(problemId));
        checkAllowed(problemRoleChecker.isAllowedToManageGrading(req, problem));

        GradingEngineEditForm gradingEngineEditData = new GradingEngineEditForm();
        gradingEngineEditData.gradingEngineName = programmingProblemStore.getGradingEngine(actorJid, problem.getJid());

        Form<GradingEngineEditForm> gradingEngineEditForm = formFactory.form(GradingEngineEditForm.class).fill(gradingEngineEditData);

        return showEditGradingEngine(req, gradingEngineEditForm, problem);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postEditGradingEngine(Http.Request req, long problemId) {
        String actorJid = getUserJid(req);
        Problem problem = checkFound(problemStore.findProblemById(problemId));
        checkAllowed(problemRoleChecker.isAllowedToManageGrading(req, problem));

        Form<GradingEngineEditForm> gradingEngineEditForm = formFactory.form(GradingEngineEditForm.class).bindFromRequest(req);

        if (formHasErrors(gradingEngineEditForm)) {
            return showEditGradingEngine(req, gradingEngineEditForm, problem);
        }

        problemStore.createUserCloneIfNotExists(actorJid, problem.getJid());

        String gradingEngine = gradingEngineEditForm.get().gradingEngineName;
        String originalGradingEngine = programmingProblemStore.getGradingEngine(actorJid, problem.getJid());

        if (!gradingEngine.equals(originalGradingEngine)) {
            GradingConfig config = GradingEngineRegistry.getInstance().get(gradingEngine).createDefaultConfig();
            programmingProblemStore.updateGradingConfig(actorJid, problem.getJid(), config);
        }

        programmingProblemStore.updateGradingEngine(actorJid, problem.getJid(), gradingEngine);

        return redirect(routes.ProgrammingProblemGradingController.editGradingConfig(problem.getId()));
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result editGradingConfig(Http.Request req, long problemId) {
        String actorJid = getUserJid(req);
        Problem problem = checkFound(problemStore.findProblemById(problemId));
        checkAllowed(problemRoleChecker.isAllowedToManageGrading(req, problem));

        String engine = programmingProblemStore.getGradingEngine(actorJid, problem.getJid());
        GradingConfig config  = programmingProblemStore.getGradingConfig(actorJid, problem.getJid());
        List<FileInfo> testDataFiles = programmingProblemStore.getGradingTestDataFiles(actorJid, problem.getJid());
        List<FileInfo> helperFiles = programmingProblemStore.getGradingHelperFiles(actorJid, problem.getJid());

        Form<?> gradingEngineConfForm = GradingEngineAdapterRegistry.getInstance().getByGradingEngineName(engine).createFormFromConfig(formFactory, config);

        return showEditGradingConfig(req, gradingEngineConfForm, problem, engine, testDataFiles, helperFiles);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postEditGradingConfig(Http.Request req, long problemId) {
        String actorJid = getUserJid(req);
        Problem problem = checkFound(problemStore.findProblemById(problemId));
        checkAllowed(problemRoleChecker.isAllowedToManageGrading(req, problem));

        String engine = programmingProblemStore.getGradingEngine(actorJid, problem.getJid());
        Form<?> gradingEngineConfForm = GradingEngineAdapterRegistry.getInstance().getByGradingEngineName(engine).createEmptyForm(formFactory).bindFromRequest(req);

        if (formHasErrors(gradingEngineConfForm)) {
            List<FileInfo> testDataFiles = programmingProblemStore.getGradingTestDataFiles(actorJid, problem.getJid());
            List<FileInfo> helperFiles = programmingProblemStore.getGradingHelperFiles(actorJid, problem.getJid());

            return showEditGradingConfig(req, gradingEngineConfForm, problem, engine, testDataFiles, helperFiles);
        }

        problemStore.createUserCloneIfNotExists(actorJid, problem.getJid());

        GradingConfig config = GradingEngineAdapterRegistry.getInstance().getByGradingEngineName(engine).createConfigFromForm(gradingEngineConfForm);
        programmingProblemStore.updateGradingConfig(actorJid, problem.getJid(), config);

        return redirect(routes.ProgrammingProblemGradingController.editGradingConfig(problem.getId()));
    }

    @Transactional
    public Result editGradingConfigByTokilibFormat(Http.Request req, long problemId) {
        String actorJid = getUserJid(req);
        Problem problem = checkFound(problemStore.findProblemById(problemId));
        checkAllowed(problemRoleChecker.isAllowedToManageGrading(req, problem));

        problemStore.createUserCloneIfNotExists(actorJid, problem.getJid());

        String engine = programmingProblemStore.getGradingEngine(actorJid, problem.getJid());

        GradingEngineAdapter adapter = GradingEngineAdapterRegistry.getInstance().getByGradingEngineName(engine);

        if (!(adapter instanceof ConfigurableWithTokilibFormat)) {
            return forbidden();
        }

        List<FileInfo> testDataFiles = programmingProblemStore.getGradingTestDataFiles(actorJid, problem.getJid());
        GradingConfig config = programmingProblemStore.getGradingConfig(actorJid, problem.getJid());

        GradingConfig newConfig = ((ConfigurableWithTokilibFormat) adapter).updateConfigWithTokilibFormat(config, testDataFiles);
        programmingProblemStore.updateGradingConfig(actorJid, problem.getJid(), newConfig);

        return redirect(routes.ProgrammingProblemGradingController.editGradingConfig(problem.getId()));
    }

    @Transactional
    public Result editGradingConfigByAutoPopulation(Http.Request req, long problemId) {
        String actorJid = getUserJid(req);
        Problem problem = checkFound(problemStore.findProblemById(problemId));
        checkAllowed(problemRoleChecker.isAllowedToManageGrading(req, problem));

        problemStore.createUserCloneIfNotExists(actorJid, problem.getJid());

        String engine = programmingProblemStore.getGradingEngine(actorJid, problem.getJid());
        List<FileInfo> testDataFiles = programmingProblemStore.getGradingTestDataFiles(actorJid, problem.getJid());
        GradingEngineAdapter adapter = GradingEngineAdapterRegistry.getInstance().getByGradingEngineName(engine);

        if (!(adapter instanceof ConfigurableWithAutoPopulation)) {
            return forbidden();
        }

        GradingConfig config = programmingProblemStore.getGradingConfig(actorJid, problem.getJid());
        GradingConfig newConfig = ((ConfigurableWithAutoPopulation) adapter).updateConfigWithAutoPopulation(config, testDataFiles);

        programmingProblemStore.updateGradingConfig(actorJid, problem.getJid(), newConfig);

        return redirect(routes.ProgrammingProblemGradingController.editGradingConfig(problem.getId()));
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result listGradingTestDataFiles(Http.Request req, long problemId) {
        String actorJid = getUserJid(req);
        Problem problem = checkFound(problemStore.findProblemById(problemId));
        checkAllowed(problemRoleChecker.isAllowedToManageGrading(req, problem));

        Form<UploadFileForm> uploadFileForm = formFactory.form(UploadFileForm.class);
        List<FileInfo> testDataFiles = programmingProblemStore.getGradingTestDataFiles(actorJid, problem.getJid());

        return showListGradingTestDataFiles(req, uploadFileForm, problem, testDataFiles);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUploadGradingTestDataFiles(Http.Request req, long problemId) {
        String actorJid = getUserJid(req);
        Problem problem = checkFound(problemStore.findProblemById(problemId));
        checkAllowed(problemRoleChecker.isAllowedToManageGrading(req, problem));

        Http.MultipartFormData<File> body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart<File> file;

        file = body.getFile("file");
        if (file != null) {
            problemStore.createUserCloneIfNotExists(actorJid, problem.getJid());

            File testDataFile = file.getFile();
            programmingProblemStore.uploadGradingTestDataFile(actorJid, problem.getJid(), testDataFile, file.getFilename());

            return redirect(routes.ProgrammingProblemGradingController.listGradingTestDataFiles(problem.getId()));
        }

        file = body.getFile("fileZipped");
        if (file != null) {
            problemStore.createUserCloneIfNotExists(actorJid, problem.getJid());

            File testDataFile = file.getFile();
            programmingProblemStore.uploadGradingTestDataFileZipped(actorJid, problem.getJid(), testDataFile);

            return redirect(routes.ProgrammingProblemGradingController.listGradingTestDataFiles(problem.getId()));
        }

        return redirect(routes.ProgrammingProblemGradingController.listGradingTestDataFiles(problem.getId()));
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result listGradingHelperFiles(Http.Request req, long problemId) {
        String actorJid = getUserJid(req);
        Problem problem = checkFound(problemStore.findProblemById(problemId));
        checkAllowed(problemRoleChecker.isAllowedToManageGrading(req, problem));

        Form<UploadFileForm> uploadFileForm = formFactory.form(UploadFileForm.class);
        List<FileInfo> helperFiles = programmingProblemStore.getGradingHelperFiles(actorJid, problem.getJid());

        return showListGradingHelperFiles(req, uploadFileForm, problem, helperFiles);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postUploadGradingHelperFiles(Http.Request req, long problemId) {
        String actorJid = getUserJid(req);
        Problem problem = checkFound(problemStore.findProblemById(problemId));
        checkAllowed(problemRoleChecker.isAllowedToManageGrading(req, problem));

        Http.MultipartFormData<File> body = req.body().asMultipartFormData();
        Http.MultipartFormData.FilePart<File> file;

        file = body.getFile("file");
        if (file != null) {
            problemStore.createUserCloneIfNotExists(actorJid, problem.getJid());

            File helperFile = file.getFile();
            programmingProblemStore.uploadGradingHelperFile(actorJid, problem.getJid(), helperFile, file.getFilename());

            return redirect(routes.ProgrammingProblemGradingController.listGradingHelperFiles(problem.getId()));
        }

        file = body.getFile("fileZipped");
        if (file != null) {
            problemStore.createUserCloneIfNotExists(actorJid, problem.getJid());

            File helperFile = file.getFile();
            programmingProblemStore.uploadGradingHelperFileZipped(actorJid, problem.getJid(), helperFile);

            return redirect(routes.ProgrammingProblemGradingController.listGradingHelperFiles(problem.getId()));
        }

        return redirect(routes.ProgrammingProblemGradingController.listGradingHelperFiles(problem.getId()));
    }

    @Transactional(readOnly = true)
    @AddCSRFToken
    public Result editLanguageRestriction(Http.Request req, long problemId) {
        String actorJid = getUserJid(req);
        Problem problem = checkFound(problemStore.findProblemById(problemId));
        checkAllowed(problemRoleChecker.isAllowedToManageGrading(req, problem));

        LanguageRestriction languageRestriction = programmingProblemStore.getLanguageRestriction(actorJid, problem.getJid());

        LanguageRestrictionEditForm languageRestrictionEditData = new LanguageRestrictionEditForm();
        languageRestrictionEditData.allowedLanguageNames = LanguageRestrictionAdapter.getFormAllowedLanguageNamesFromLanguageRestriction(languageRestriction);
        languageRestrictionEditData.isAllowedAll = LanguageRestrictionAdapter.getFormIsAllowedAllFromLanguageRestriction(languageRestriction);

        Form<LanguageRestrictionEditForm> languageRestrictionEditForm = formFactory.form(LanguageRestrictionEditForm.class).fill(languageRestrictionEditData);

        return showEditLanguageRestriction(req, languageRestrictionEditForm, problem);
    }

    @Transactional
    @RequireCSRFCheck
    public Result postEditLanguageRestriction(Http.Request req, long problemId) {
        String actorJid = getUserJid(req);
        Problem problem = checkFound(problemStore.findProblemById(problemId));
        checkAllowed(problemRoleChecker.isAllowedToManageGrading(req, problem));

        Form<LanguageRestrictionEditForm> languageRestrictionEditForm = formFactory.form(LanguageRestrictionEditForm.class).bindFromRequest(req);

        if (formHasErrors(languageRestrictionEditForm)) {
            return showEditLanguageRestriction(req, languageRestrictionEditForm, problem);
        }

        problemStore.createUserCloneIfNotExists(actorJid, problem.getJid());

        LanguageRestrictionEditForm data = languageRestrictionEditForm.get();
        LanguageRestriction languageRestriction = LanguageRestrictionAdapter.createLanguageRestrictionFromForm(data.allowedLanguageNames, data.isAllowedAll);

        programmingProblemStore.updateLanguageRestriction(actorJid, problem.getJid(), languageRestriction);

        return redirect(routes.ProgrammingProblemGradingController.editLanguageRestriction(problem.getId()));
    }

    private Result showEditGradingEngine(Http.Request req, Form<GradingEngineEditForm> gradingEngineEditForm, Problem problem) {
        HtmlTemplate template = getBaseHtmlTemplate(req);
        template.setContent(editGradingEngineView.render(gradingEngineEditForm, problem));
        template.markBreadcrumbLocation("Update engine", routes.ProgrammingProblemGradingController.editGradingEngine(problem.getId()));
        template.setPageTitle("Problem - Update grading engine");

        return renderTemplate(template, problem);
    }

    private Result showEditGradingConfig(Http.Request req, Form<?> gradingConfForm, Problem problem, String gradingEngine, List<FileInfo> testDataFiles, List<FileInfo> helperFiles) {
        GradingEngineAdapter adapter = GradingEngineAdapterRegistry.getInstance().getByGradingEngineName(gradingEngine);
        Call postUpdateGradingConfigCall = routes.ProgrammingProblemGradingController.postEditGradingConfig(problem.getId());
        HtmlTemplate template = getBaseHtmlTemplate(req);
        template.setContent(adapter.renderUpdateGradingConfig(gradingConfForm, postUpdateGradingConfigCall, testDataFiles, helperFiles));

        if (adapter instanceof ConfigurableWithTokilibFormat) {
            Call updateGradingConfigCall = routes.ProgrammingProblemGradingController.editGradingConfigByTokilibFormat(problem.getId());
            template.transformContent(c -> tokilibLayout.render(updateGradingConfigCall, c));
        } else if (adapter instanceof ConfigurableWithAutoPopulation) {
            Call updateGradingConfigCall = routes.ProgrammingProblemGradingController.editGradingConfigByAutoPopulation(problem.getId());
            template.transformContent(c -> autoPopulationLayout.render(updateGradingConfigCall, c));
        }

        template.markBreadcrumbLocation("Update config", routes.ProgrammingProblemGradingController.editGradingConfig(problem.getId()));
        template.setPageTitle("Problem - Update grading config");

        return renderTemplate(template, problem);
    }

    private Result showListGradingTestDataFiles(Http.Request req, Form<UploadFileForm> uploadFileForm, Problem problem, List<FileInfo> testDataFiles) {
        HtmlTemplate template = getBaseHtmlTemplate(req);
        template.setContent(listGradingTestDataFilesView.render(uploadFileForm, problem.getId(), testDataFiles));
        template.markBreadcrumbLocation("Test data files", routes.ProgrammingProblemGradingController.listGradingTestDataFiles(problem.getId()));
        template.setPageTitle("Problem - Grading test data files");

        return renderTemplate(template, problem);
    }

    private Result showListGradingHelperFiles(Http.Request req, Form<UploadFileForm> uploadFileForm, Problem problem, List<FileInfo> helperFiles) {
        HtmlTemplate template = getBaseHtmlTemplate(req);
        template.setContent(listGradingHelperFilesView.render(uploadFileForm, problem.getId(), helperFiles));
        template.markBreadcrumbLocation("Helper files", routes.ProgrammingProblemGradingController.listGradingHelperFiles(problem.getId()));
        template.setPageTitle("Problem - Grading Helper Files");

        return renderTemplate(template, problem);
    }

    private Result showEditLanguageRestriction(Http.Request req, Form<LanguageRestrictionEditForm> languageRestrictionEditForm, Problem problem) {
        HtmlTemplate template = getBaseHtmlTemplate(req);
        template.setContent(editLanguageRestrictionView.render(languageRestrictionEditForm, problem));
        template.markBreadcrumbLocation("Update language restriction", routes.ProgrammingProblemGradingController.editLanguageRestriction(problem.getId()));
        template.setPageTitle("Problem - Update Language Restriction");

        return renderTemplate(template, problem);
    }

    protected Result renderTemplate(HtmlTemplate template, Problem problem) {
        template.addSecondaryTab("Engine", routes.ProgrammingProblemGradingController.editGradingEngine(problem.getId()));
        template.addSecondaryTab("Config", routes.ProgrammingProblemGradingController.editGradingConfig(problem.getId()));
        template.addSecondaryTab("Test data", routes.ProgrammingProblemGradingController.listGradingTestDataFiles(problem.getId()));
        template.addSecondaryTab("Helpers", routes.ProgrammingProblemGradingController.listGradingHelperFiles(problem.getId()));
        template.addSecondaryTab("Language restriction", routes.ProgrammingProblemGradingController.editLanguageRestriction(problem.getId()));

        template.markBreadcrumbLocation("Grading", org.iatoki.judgels.sandalphon.problem.programming.routes.ProgrammingProblemController.jumpToGrading(problem.getId()));

        return super.renderTemplate(template, problem);
    }
}