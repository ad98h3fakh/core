package com.dotcms.rest.api.v1.pushpublish;

import com.dotcms.publishing.FilterDescriptor;
import com.dotcms.publishing.PushPublishFiltersInitializer;
import com.dotcms.rest.ErrorEntity;
import com.dotcms.rest.InitDataObject;
import com.dotcms.rest.ResponseEntityView;
import com.dotcms.rest.WebResource;
import com.dotcms.rest.annotation.NoCache;
import com.dotcms.rest.api.MultiPartUtils;
import com.dotcms.util.YamlUtil;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.Role;
import com.dotmarketing.exception.DoesNotExistException;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;
import com.google.common.annotations.VisibleForTesting;
import com.liferay.portal.model.User;
import org.apache.commons.io.FileUtils;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.server.JSONP;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This REST Endpoint provides developers useful methods to interact with Push Publishing Filters in dotCMS.
 * <p>You may create Push Publishing filters to control which content is pushed from your sending server to your
 * receiving server. The filters allow you to have fine-grained control over what content does and does not get pushed,
 * whether intentionally (when specifically selected) or by dependency.</p>
 * <p>You may create as many filters as you wish. You can specify permissions for the filters, allowing you to control
 * what content and objects different users and Roles may push. For example, you can allow users with a specific Role to
 * only push content of a specific Content Type, or only push content in a specific location.</p>
 *
 * @author Erick Gonzalez
 * @since Mar 6th, 2020
 */
@Path("/v1/pushpublish/filters")
public class PushPublishFilterResource {

    private final WebResource webResource;

    private final MultiPartUtils multiPartUtils = new MultiPartUtils();

    public PushPublishFilterResource(){
        this(new WebResource());
    }

    @VisibleForTesting
    public PushPublishFilterResource(final WebResource webResource) {
        this.webResource = webResource;
    }

    /**
     * Lists all Push Publishing filter descriptors that the User calling this method has access to, sorted
     * alphabetically.
     * <p>Example:</p>
     * <pre>
     * GET: {{serverURL}}/api/v1/pushpublish/filters
     * </pre>
     *
     * @param request  The current instance of the {@link HttpServletRequest} object.
     * @param response The current instance of the {@link HttpServletResponse} object.
     *
     * @return The list of Push Publishing Filters.
     *
     * @throws DotDataException An error occurred when interacting with the data source.
     */
    @GET
    @JSONP
    @NoCache
    @Produces({MediaType.APPLICATION_JSON, "application/javascript"})
    public final Response getFilters(@Context final HttpServletRequest request,
            @Context final HttpServletResponse response) throws DotDataException {
        final InitDataObject initData =
                new WebResource.InitBuilder(webResource)
                        .requiredBackendUser(true)
                        .requiredFrontendUser(false)
                        .requestAndResponse(request, response)
                        .rejectWhenNoUser(true)
                        .init();
        final User user = initData.getUser();
        final List<FilterDescriptor> list = APILocator.getPublisherAPI().getFiltersDescriptorsByRole(user);
        if (UtilMethods.isSet(list)) {
            Collections.sort(list);
        }
        return Response.ok(new ResponseEntityView(list)).build();
    }

    /**
     * Returns a specific Push Publishing filter descriptor by its key (if the User has access to it).
     * <p>Example:</p>
     * <pre>
     * GET: {{serverURL}}/api/v1/pushpublish/filters/ForcePush.yml
     * </pre>
     *
     * @param request   The current instance of the {@link HttpServletRequest} object.
     * @param response  The current instance of the {@link HttpServletResponse} object.
     * @param filterKey The Push Publishing Filter key.
     *
     * @return The Push Publishing Filter.
     *
     * @throws DotDataException     An error occurred when interacting with the data source.
     * @throws DotSecurityException The {@link User} calling this method does not have the required permission to
     *                              perform this action.
     */
    @GET
    @Path("/{filterKey}")
    @JSONP
    @NoCache
    @Produces({MediaType.APPLICATION_JSON, "application/javascript"})
    public final Response getFilter(@Context final HttpServletRequest request,
                                     @Context final HttpServletResponse response,
                                     @PathParam("filterKey") final String filterKey) throws DotDataException, DotSecurityException {

        final InitDataObject initData =
                new WebResource.InitBuilder(webResource)
                        .requiredBackendUser(true)
                        .requiredFrontendUser(false)
                        .requestAndResponse(request, response)
                        .rejectWhenNoUser(true)
                        .init();
        final User user = initData.getUser();

        if (APILocator.getPublisherAPI().existsFilterDescriptor(filterKey)) {

            final FilterDescriptor filterDescriptor = APILocator.getPublisherAPI().getFilterDescriptorByKey(filterKey);
            if (!user.isAdmin()) {
                final List<Role> roles = APILocator.getRoleAPI().loadRolesForUser(user.getUserId(), true);
                Logger.debug(this, ()->"User Roles: " + roles.toString());
                final String filterRoles = filterDescriptor.getRoles();
                Logger.debug(this, ()-> "File: " + filterDescriptor.getKey() + " Roles: " + filterRoles);
                final boolean allowed = roles.stream().anyMatch(role -> UtilMethods.isSet(role.getRoleKey()) && filterRoles.contains(role.getRoleKey()));
                if (!allowed) {
                    throw new DotSecurityException(
                            String.format("User '%s' does not have access to filter '%s'", user.getUserId(),
                                    filterKey));
                }
            }
            return Response.ok(new ResponseEntityView(filterDescriptor)).build();
        }
        final String errorMsg = String.format("Filter '%s' does not exist", filterKey);
        Logger.debug(this, ()-> errorMsg);
        throw new DoesNotExistException(errorMsg);
    }

    /**
     * Creates a new Push Publishing Filter based on a bean form.
     * <p>Example:</p>
     * <pre>
     * POST: {{serverURL}}/api/v1/pushpublish/filters
     * </pre>
     * Body:
     * <pre>
     * {
     *     "key":"NoWorkflow.yml",
     *     "title":"Push without Wofklows",
     *     "defaultFilter":"false",
     *     "roles":"DOTCMS_BACK_END_USER",
     *     "filters": {
     *         "excludeQuery": "",
     *         "excludeClasses": ["Host", "Workflow", "OSGI"],
     *         "dependencies": true,
     *         "excludeDependencyQuery": "",
     *         "excludeDependencyClasses": ["Host", "Workflow"],
     *         "forcePush": false,
     *         "relationships": false
     *     }
     * }
     * </pre>
     *
     * @param request              The current instance of the {@link HttpServletRequest} object.
     * @param response             The current instance of the {@link HttpServletResponse} object.
     * @param filterDescriptorForm An instance of the {@link FilterDescriptorForm} containing all the required
     *                             information to create a Push Publishing Filter.
     *
     * @return The complete list of Push Publishing Filters in the system.
     *
     * @throws DotDataException An error occurred when interacting with the data source.
     */
    @POST
    @JSONP
    @NoCache
    @Produces({MediaType.APPLICATION_JSON, "application/javascript"})
    public final Response saveFromForm(@Context final HttpServletRequest request,
                                       @Context final HttpServletResponse response,
                                       final FilterDescriptorForm filterDescriptorForm) throws DotDataException {

        final InitDataObject initData =
                new WebResource.InitBuilder(webResource)
                        .requiredBackendUser(true)
                        .requiredFrontendUser(false)
                        .requestAndResponse(request, response)
                        .rejectWhenNoUser(true)
                        .requiredRoles(Role.CMS_ADMINISTRATOR_ROLE)
                        .init();
        final User user = initData.getUser();

        Logger.debug(this, ()-> "Adding PP filter: " + filterDescriptorForm);

        if (APILocator.getPublisherAPI().existsFilterDescriptor(filterDescriptorForm.getKey())) {
            final String errorMsg = String.format("Filter '%s' cannot be added because it already exists",
                    filterDescriptorForm.getKey());
            Logger.debug(this, () -> errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        final FilterDescriptor filterDescriptor = new FilterDescriptor(filterDescriptorForm.getKey(), filterDescriptorForm.getTitle(),
                filterDescriptorForm.getFilters(), filterDescriptorForm.isDefaultFilter(), filterDescriptorForm.getRoles());
        final File filterPathFile = new File(new File(APILocator.getFileAssetAPI().getRealAssetsRootPath() + File.separator + "server"
                + File.separator + "publishing-filters" + File.separator), filterDescriptor.getKey());
        YamlUtil.write(filterPathFile, filterDescriptor);
        new PushPublishFiltersInitializer().init();
        final List<String> filterNames = APILocator.getPublisherAPI()
                .getFiltersDescriptorsByRole(user).stream().map(filter -> filter.getKey()).collect(Collectors.toList());
        return Response.ok(new ResponseEntityView(filterNames)).build();
    }

    /**
     * Creates a new Push Publishing Filter based on a YML file.
     * <p>Example:</p>
     * <pre>
     * POST: {{serverURL}}/api/v1/pushpublish/filters
     * </pre>
     * Body:
     * <pre>
     * --form 'file=@"resources/TestPPFilter.yml"'
     * </pre>
     *
     * @param request   The current instance of the {@link HttpServletRequest} object.
     * @param response  The current instance of the {@link HttpServletResponse} object.
     * @param multipart An instance of the {@link FilterDescriptorForm} containing all the required information to
     *                  create a Push Publishing Filter.
     *
     * @return The complete list of Push Publishing Filters in the system.
     *
     * @throws DotDataException An error occurred when interacting with the data source.
     * @throws IOException      An error occurred when reading the incoming binary file.
     */
    @POST
    @JSONP
    @NoCache
    @Produces({MediaType.APPLICATION_JSON, "application/javascript"})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public final Response saveFromFile(@Context final HttpServletRequest request,
                                       @Context final HttpServletResponse response,
                                       final FormDataMultiPart multipart) throws DotDataException, IOException {

        final InitDataObject initData =
                new WebResource.InitBuilder(webResource)
                        .requiredBackendUser(true)
                        .requiredFrontendUser(false)
                        .requestAndResponse(request, response)
                        .rejectWhenNoUser(true)
                        .requiredRoles(Role.CMS_ADMINISTRATOR_ROLE)
                        .init();
        final User user = initData.getUser();

        Logger.debug(this, ()-> "Adding PP filter by file ");

        final List<File> filterFiles = this.multiPartUtils.getBinariesFromMultipart(multipart);

        for (final File file : filterFiles) {
            if (APILocator.getPublisherAPI().existsFilterDescriptor(file.getName())) {
                final String errorMsg =
                        String.format("Filter '%s' cannot be added because it already exists", file.getName());
                Logger.debug(this, () -> errorMsg);
                throw new IllegalArgumentException(errorMsg);
            }
        }

        for (final File file : filterFiles) {
            final File filterPathFile = new File(new File(APILocator.getFileAssetAPI().getRealAssetsRootPath() + File.separator + "server"
                    + File.separator + "publishing-filters" + File.separator), file.getName());
            FileUtils.copyFile(file, filterPathFile);
        }
        new PushPublishFiltersInitializer().init();
        final List<String> filterNames = APILocator.getPublisherAPI()
                .getFiltersDescriptorsByRole(user).stream().map(filter -> filter.getKey()).collect(Collectors.toList());

        return Response.ok(new ResponseEntityView(filterNames)).build();
    }

    /**
     * Updates a Push Publishing Filter based on a bean form.
     * <p>Example:</p>
     * <pre>
     * PUT: {{serverURL}}/api/v1/pushpublish/filters
     * </pre>
     * Body:
     * <pre>
     * {
     *     "key":"NoWorkflow.yml",
     *     "title":"Push without Wofklows",
     *     "defaultFilter":"false",
     *     "roles":"DOTCMS_BACK_END_USER",
     *     "filters": {
     *         "excludeQuery": "",
     *         "excludeClasses": ["Host", "Workflow", "OSGI"],
     *         "dependencies": true,
     *         "excludeDependencyQuery": "",
     *         "excludeDependencyClasses": ["Host", "Workflow"],
     *         "forcePush": false,
     *         "relationships": false
     *     }
     * }
     * </pre>
     *
     * @param request              The current instance of the {@link HttpServletRequest} object.
     * @param response             The current instance of the {@link HttpServletResponse} object.
     * @param filterDescriptorForm An instance of the {@link FilterDescriptorForm} containing all the required
     *                             information to create a Push Publishing Filter.
     *
     * @return The complete list of Push Publishing Filters in the system.
     *
     * @throws DotDataException An error occurred when interacting with the data source.
     */
    @PUT
    @JSONP
    @NoCache
    @Produces({MediaType.APPLICATION_JSON, "application/javascript"})
    public final Response updateFromForm(@Context final HttpServletRequest request,
                                       @Context final HttpServletResponse response,
                                       final FilterDescriptorForm filterDescriptorForm) throws DotDataException {

        final InitDataObject initData =
                new WebResource.InitBuilder(webResource)
                        .requiredBackendUser(true)
                        .requiredFrontendUser(false)
                        .requestAndResponse(request, response)
                        .rejectWhenNoUser(true)
                        .requiredRoles(Role.CMS_ADMINISTRATOR_ROLE)
                        .init();
        final User user = initData.getUser();

        Logger.debug(this, ()-> "Updating PP filter: " + filterDescriptorForm);

        if (!APILocator.getPublisherAPI().existsFilterDescriptor(filterDescriptorForm.getKey())) {
            final String errorMsg = String.format("Filter '%s' does not exist", filterDescriptorForm.getKey());
            Logger.debug(this, ()-> errorMsg);
            throw new DoesNotExistException(errorMsg);
        }

        final FilterDescriptor filterDescriptor = new FilterDescriptor(filterDescriptorForm.getKey(), filterDescriptorForm.getTitle(),
                filterDescriptorForm.getFilters(), filterDescriptorForm.isDefaultFilter(), filterDescriptorForm.getRoles());
        final File filterPathFile = new File(new File(APILocator.getFileAssetAPI().getRealAssetsRootPath() + File.separator + "server"
                + File.separator + "publishing-filters" + File.separator), filterDescriptor.getKey());
        YamlUtil.write(filterPathFile, filterDescriptor);
        new PushPublishFiltersInitializer().init();
        final List<String> filterNames = APILocator.getPublisherAPI()
                .getFiltersDescriptorsByRole(user).stream().map(filter -> filter.getKey()).collect(Collectors.toList());
        return Response.ok(new ResponseEntityView(filterNames)).build();
    }

    /**
     * Updates a Push Publishing Filter based on a YML file.
     * <p>Example:</p>
     * <pre>
     * POST: {{serverURL}}/api/v1/pushpublish/filters
     * </pre>
     * Body:
     * <pre>
     * --form 'file=@"resources/TestPPFilter.yml"'
     * </pre>
     *
     * @param request   The current instance of the {@link HttpServletRequest} object.
     * @param response  The current instance of the {@link HttpServletResponse} object.
     * @param multipart An instance of the {@link FilterDescriptorForm} containing all the required information to
     *                  create a Push Publishing Filter.
     *
     * @return The complete list of Push Publishing Filters in the system.
     *
     * @throws DotDataException An error occurred when interacting with the data source.
     * @throws IOException      An error occurred when reading the incoming binary file.
     */
    @PUT
    @JSONP
    @NoCache
    @Produces({MediaType.APPLICATION_JSON, "application/javascript"})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public final Response updateFromFile(@Context final HttpServletRequest request,
                                       @Context final HttpServletResponse response,
                                       final FormDataMultiPart multipart) throws DotDataException, IOException {

        final InitDataObject initData =
                new WebResource.InitBuilder(webResource)
                        .requiredBackendUser(true)
                        .requiredFrontendUser(false)
                        .requestAndResponse(request, response)
                        .rejectWhenNoUser(true)
                        .requiredRoles(Role.CMS_ADMINISTRATOR_ROLE)
                        .init();
        final User user = initData.getUser();

        Logger.debug(this, ()-> "Updating PP filter by file ");

        final List<File> filterFiles = this.multiPartUtils.getBinariesFromMultipart(multipart);

        for (final File file : filterFiles) {
            if (!APILocator.getPublisherAPI().existsFilterDescriptor(file.getName())) {
                final String errorMsg = String.format("Filter '%s' does not exist", file.getName());
                Logger.debug(this, ()-> errorMsg);
                throw new IllegalArgumentException(errorMsg);
            }
        }

        for (final File file : filterFiles) {
            final File filterPathFile = new File(new File(APILocator.getFileAssetAPI().getRealAssetsRootPath() + File.separator + "server"
                    + File.separator + "publishing-filters" + File.separator), file.getName());
            FileUtils.copyFile(file, filterPathFile);
        }
        new PushPublishFiltersInitializer().init();
        final List<String> filterNames = APILocator.getPublisherAPI()
                .getFiltersDescriptorsByRole(user).stream().map(filter -> filter.getKey()).collect(Collectors.toList());

        return Response.ok(new ResponseEntityView(filterNames)).build();
    }

    /**
     * Deletes a Push Publishing Filter by its filter key.
     * <p>Example:</p>
     * <pre>
     * DELETE: {{serverURL}}/api/v1/pushpublish/filters/NoWorkflow.yml
     * </pre>
     *
     * @param request   The current instance of the {@link HttpServletRequest} object.
     * @param response  The current instance of the {@link HttpServletResponse} object.
     * @param filterKey The unique filter key.
     *
     * @return The complete list of Push Publishing Filters in the system.
     *
     * @throws DotDataException     An error occurred when interacting with the data source.
     * @throws DotSecurityException The {@link User} calling this method does not have the required permission to
     *                              perform this action.
     */
    @DELETE
    @Path("/{filterKey}")
    @JSONP
    @NoCache
    @Produces({MediaType.APPLICATION_JSON, "application/javascript"})
    public final Response deleteFilter(@Context final HttpServletRequest request,
                                     @Context final HttpServletResponse response,
                                     @PathParam("filterKey") final String filterKey) throws DotDataException, DotSecurityException {

        final InitDataObject initData =
                new WebResource.InitBuilder(webResource)
                        .requiredBackendUser(true)
                        .requiredFrontendUser(false)
                        .requestAndResponse(request, response)
                        .rejectWhenNoUser(true)
                        .requiredRoles(Role.CMS_ADMINISTRATOR_ROLE)
                        .init();
        final User user = initData.getUser();

        if (!APILocator.getPublisherAPI().existsFilterDescriptor(filterKey)) {
            final String errorMsg = String.format("Filter '%s' does not exist", filterKey);
            Logger.debug(this, ()-> errorMsg);
            throw new DoesNotExistException(errorMsg);
        }

        final File filterPathFile = new File(new File(APILocator.getFileAssetAPI().getRealAssetsRootPath() + File.separator + "server"
                + File.separator + "publishing-filters" + File.separator), filterKey);
        if (FileUtils.deleteQuietly(filterPathFile)) {
            new PushPublishFiltersInitializer().init();
            final List<String> filterNames = APILocator.getPublisherAPI()
                    .getFiltersDescriptorsByRole(user).stream().map(filter -> filter.getKey()).collect(Collectors.toList());

            return Response.ok(new ResponseEntityView(filterNames)).build();
        }

        return Response.status(Response.Status.EXPECTATION_FAILED).
                entity(new ResponseEntityView(Arrays.asList(
                        new ErrorEntity(
                                new Integer(Response.Status.EXPECTATION_FAILED.getStatusCode()).toString(),
                                String.format("Filter '%s' cannot be deleted", filterKey))
                        )
                    )
                ).build();
    }

}
