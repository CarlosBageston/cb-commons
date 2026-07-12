package br.com.cb.uild;


import com.github.f4b6a3.ulid.Ulid;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;
import java.util.UUID;

public class ULIDGenerator implements IdentifierGenerator {

    @Override
    public Serializable generate(
            SharedSessionContractImplementor session,
            Object object
    ) throws HibernateException {

        return Ulid.fast().toUuid();
    }

    public static UUID generate() {
        return Ulid.fast().toUuid();
    }
}