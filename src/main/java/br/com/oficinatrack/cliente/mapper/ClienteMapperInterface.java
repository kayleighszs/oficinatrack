package br.com.oficinatrack.cliente.mapper;

import br.com.oficinatrack.cliente.api.dto.AtualizarClienteRequest;
import br.com.oficinatrack.cliente.api.dto.CadastrarClienteRequest;
import br.com.oficinatrack.cliente.domain.Cliente;
import br.com.oficinatrack.cliente.api.dto.ClienteResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ClienteMapperInterface {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tipoPessoa", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    @Mapping(target = "criadoEm", ignore = true)
    @Mapping(target = "atualizadoEm", ignore = true)
    Cliente toEntity(CadastrarClienteRequest request);

    ClienteResponse toResponse(Cliente cliente);

    List<ClienteResponse> toResponseList(List<Cliente> clientes);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cpfCnpj", ignore = true)
    @Mapping(target = "tipoPessoa", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    @Mapping(target = "criadoEm", ignore = true)
    @Mapping(target = "atualizadoEm", ignore = true)
    void atualizarCliente(@MappingTarget Cliente cliente, AtualizarClienteRequest request);
}
