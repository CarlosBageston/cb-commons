package br.com.cb.test;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
@ApplicationScoped
public class DataSeeder {

    @Inject
    DataSeederRepository repository;

    @Transactional
    void onStart(@Observes StartupEvent ev) {

        if (repository.count() > 0) return;

        repository.persist(

                company("Apple", "apple@tech.com", 150000, 3000.50, true, 2020),
                company("Apple Store", "store@apple.com", 5000, 120.10, true, 2021),
                company("Apple Services", "services@apple.com", 8000, 900.00, true, 2022),

                company("Amazon", "amazon@tech.com", 1600000, 5000.90, true, 2019),
                company("Amazon Prime", "prime@amazon.com", 300000, 2000.00, true, 2020),
                company("Amazon Logistics", "logistics@amazon.com", 120000, 800.00, true, 2021),

                company("Google", "google@tech.com", 190000, 4500.00, true, 2018),
                company("Google Cloud", "cloud@google.com", 70000, 3200.00, true, 2022),
                company("Google Ads", "ads@google.com", 50000, 1500.00, true, 2023),

                company("Microsoft", "microsoft@tech.com", 220000, 6000.00, true, 2017),
                company("Microsoft Azure", "azure@microsoft.com", 90000, 4100.00, true, 2021),
                company("Microsoft 365", "office@microsoft.com", 110000, 2100.00, true, 2022),

                company("Meta", "meta@tech.com", 180000, 3500.00, true, 2016),
                company("Instagram", "instagram@meta.com", 60000, 1800.00, true, 2018),
                company("WhatsApp", "whatsapp@meta.com", 70000, 2200.00, true, 2019),

                company("Netflix", "netflix@stream.com", 30000, 2800.00, true, 2015),
                company("Spotify", "spotify@music.com", 20000, 1500.00, true, 2014),

                company("Uber", "uber@transport.com", 100000, 900.00, false, 2013),
                company("Airbnb", "airbnb@travel.com", 50000, 1700.00, true, 2012),

                company("Casquim Tech", "contato@casquim.com", 50, 10.0, true, 2024),
                company("Casquim Cloud", "cloud@casquim.com", 20, 5.0, true, 2025),

                company("XP Investimentos", "xp@finance.com", 15000, 2200.00, true, 2010),
                company("Nubank", "nubank@finance.com", 20000, 2400.00, true, 2013),

                company("Empresa Teste", "teste@email.com", 1, 0.0, false, 2026)
        );
    }

    private Company company(
            String name,
            String email,
            int employees,
            double revenue,
            boolean active,
            int year
    ) {

        Company c = new Company();

        c.setId(UUID.randomUUID());
        c.setName(name);
        c.setEmail(email);

        c.setEmployees(employees);
        c.setRevenue(revenue);
        c.setActive(active);

        c.setCreatedAt(LocalDate.of(year, 1, 1));
        c.setUpdatedAt(LocalDateTime.now());

        return c;
    }
}