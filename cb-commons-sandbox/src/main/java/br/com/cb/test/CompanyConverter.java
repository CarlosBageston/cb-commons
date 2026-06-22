package br.com.cb.test;

import br.com.cb.converter.AbstractConverter;
import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class CompanyConverter implements AbstractConverter<Company, CompanyDto> {

    @Override
    public Company dtoToOrm(CompanyDto companyDto) {
        return dtoToOrm(companyDto, new Company());
    }

    @Override
    public Company dtoToOrm(CompanyDto dto, Company orm) {
        orm.setId(dto.getId());
        orm.setName(dto.getName());
        orm.setEmail(dto.getEmail());

        orm.setEmployees(dto.getEmployees());
        orm.setRevenue(dto.getRevenue());
        orm.setActive(dto.isActive());
        orm.setCreatedAt(dto.getCreatedAt());
        orm.setUpdatedAt(dto.getUpdatedAt());
        return orm;
    }

    @Override
    public CompanyDto ormToDto(Company company) {
        return ormToDto(company, new CompanyDto());
    }

    @Override
    public CompanyDto ormToDto(Company orm, CompanyDto dto) {

        dto.setId(orm.getId());
        dto.setName(orm.getName());
        dto.setEmail(orm.getEmail());

        dto.setEmployees(orm.getEmployees());
        dto.setRevenue(orm.getRevenue());
        dto.setActive(orm.isActive());
        dto.setCreatedAt(orm.getCreatedAt());
        dto.setUpdatedAt(orm.getUpdatedAt());
        return dto;
    }
}