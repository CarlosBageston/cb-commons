package br.com.cb.test;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
public class CompanyDto {
    private UUID id;

    @NotBlank(message = "name não pode ser vazio")
    private String name;

    @Email(message = "email inválido")
    private String email;

    @Min(value = 1, message = "employees deve ser maior que 0")
    private int employees;

    @DecimalMin(value = "0.0", inclusive = true, message = "revenue não pode ser negativo")
    private double revenue;

    private boolean active;

    @NotNull(message = "createdAt é obrigatório")
    private LocalDate createdAt;

    @NotNull(message = "updatedAt é obrigatório")
    private LocalDateTime updatedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getEmployees() {
        return employees;
    }

    public void setEmployees(int employees) {
        this.employees = employees;
    }

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
