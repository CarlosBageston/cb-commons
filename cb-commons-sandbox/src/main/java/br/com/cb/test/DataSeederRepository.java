package br.com.cb.test;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DataSeederRepository implements PanacheRepository<Company> {
}
