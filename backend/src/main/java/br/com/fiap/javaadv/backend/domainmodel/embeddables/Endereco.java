package br.com.fiap.javaadv.backend.domainmodel.embeddables;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Endereco {
    private String endereco;
    private String cidade;
    private String estado;
    private String cep;
}