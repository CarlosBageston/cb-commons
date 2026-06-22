package br.com.cb.search;

/**
 * Operadores usados para construir filtros dinâmicos na query.
 */
public enum FilterOperator {

    /**
     * EQ (Equal)
     * Igualdade exata.
     * Ex: name = 'Apple'
     */
    EQ,

    /**
     * LIKE
     * Busca parcial (texto contém algo).
     * Ex: name LIKE '%App%'
     * Usado para pesquisa tipo "autocomplete" ou busca livre.
     */
    LIKE,

    /**
     * GT (Greater Than)
     * Maior que.
     * Ex: age > 18
     */
    GT,

    /**
     * GTE (Greater Than or Equal)
     * Maior ou igual.
     * Ex: age >= 18
     */
    GTE,

    /**
     * LT (Less Than)
     * Menor que.
     * Ex: price < 100
     */
    LT,

    /**
     * LTE (Less Than or Equal)
     * Menor ou igual.
     * Ex: price <= 100
     */
    LTE,

    /**
     * IN
     * Verifica se o valor está dentro de uma lista.
     * Ex: status IN ('ACTIVE', 'PENDING')
     */
    IN
}