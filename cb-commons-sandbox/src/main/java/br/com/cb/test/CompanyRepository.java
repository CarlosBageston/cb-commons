package br.com.cb.test;


import br.com.cb.search.SearchRepository;
import jakarta.enterprise.context.RequestScoped;


@RequestScoped
public class CompanyRepository extends SearchRepository<Company, CompanyDto> {
    public CompanyRepository() {
        super(Company.class);
    }
}
