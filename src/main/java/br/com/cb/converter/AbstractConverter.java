package br.com.cb.converter;

import java.util.List;

public interface AbstractConverter<ORM, DTO> {
    ORM dtoToOrm(DTO dto);
    ORM dtoToOrm(DTO dto, ORM orm);

    DTO ormToDto(ORM orm);
    DTO ormToDto(ORM orm, DTO dto);

    default List<ORM> dtoListToOrmList(List<DTO> dtoList) {
        if (dtoList == null) return null;
        return dtoList.stream().map(this::dtoToOrm).toList();
    }

    default List<DTO> ormListToDtoList(List<ORM> ormList) {
        if (ormList == null) return null;
        return ormList.stream().map(this::ormToDto).toList();
    }
}
