package br.com.fiap.javaadv.backend.resources.dtos;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "data")
public abstract class BaseResponseDTO extends RepresentationModel<BaseResponseDTO> {
}
