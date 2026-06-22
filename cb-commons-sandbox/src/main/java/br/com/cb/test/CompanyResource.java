package br.com.cb.test;

import br.com.cb.resource.BaseResource;
import br.com.cb.validation.AutoValidate;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/companies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@AutoValidate
public class CompanyResource extends BaseResource {

    @Inject
    CompanyRepository companyRepository;

    @Inject
    CompanyController companyController;

    @Inject
    CompanyConverter companyConverter;

    @POST()
    @Path("/validator")
    public String testValidator(CompanyDto companyDto){
        return companyController.testValidator(companyDto);
    }

    @GET
    @Path("exception")
    public String testException(CompanyDto companyDto){
        return companyController.testException(companyDto);
    }
    @GET
    public Response search() {
        return search(companyRepository, companyConverter::ormToDto);
    }
}