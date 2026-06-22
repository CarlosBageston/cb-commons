package br.com.cb.test;

import br.com.cb.exceptions.ApiException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.core.Response;

@RequestScoped
public class CompanyController {


    public String testValidator(CompanyDto companyDto) {
        return "validou";
    }

    public String testException(CompanyDto companyDto) {
        // Não encontrou no banco
//        throw ApiException.notFound("Empresa não encontrada");

//        throw ApiException.conflict("CNPJ já cadastrado");
//
//        throw ApiException.badRequest("Empresa inativa não pode ser alterada");
//
        throw ApiException.of("Contrato já encerrado");
    }
}
