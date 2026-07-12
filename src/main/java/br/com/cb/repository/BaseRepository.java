package br.com.cb.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import java.util.UUID;

public abstract class BaseRepository<T> implements PanacheRepositoryBase<T, UUID> {
}
