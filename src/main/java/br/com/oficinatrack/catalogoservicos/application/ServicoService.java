package br.com.oficinatrack.catalogoservicos.application;

import br.com.oficinatrack.catalogoservicos.api.dto.in.ServicoRequestIn;
import br.com.oficinatrack.catalogoservicos.api.dto.out.ServicoRequestOut;
import br.com.oficinatrack.catalogoservicos.domain.Servico;
import br.com.oficinatrack.catalogoservicos.infra.ServicoRepository;
import br.com.oficinatrack.shared.exception.RecursoNaoEncontradoException;
import br.com.oficinatrack.shared.exception.RegraDeNegocioException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class ServicoService {

    private ServicoRepository servicoRepository;

    public ServicoService(ServicoRepository servicoRepository) {
        this.servicoRepository = servicoRepository;
    }


    @Transactional
    public ResponseEntity<?> cadastrar(ServicoRequestIn request) throws Exception {

        validarServico(request);

        Servico servico = new Servico(
                request.nome(),
                request.valor(),
                request.tempoMedioEstimado()
        );


        Servico servicoSalvo = servicoRepository.save(servico);

        return ResponseEntity.ok(paraResponse(servicoSalvo));

    }

    @Transactional(readOnly = true)
    public ResponseEntity listar(String nome, Pageable pageable) {
        Page<Servico> servicos;

        if (nome != null && !nome.isBlank()) {
            servicos = servicoRepository.findByNomeContainingIgnoreCase(nome, pageable);
        } else {
            servicos = servicoRepository.findAll(pageable);
        }

        Page<ServicoRequestOut> ret = servicos.map(this::paraResponse);

        Map<String, Object> response = new HashMap<>();
        response.put("ordens", ret);
        response.put("currentPage", servicos.getNumber());
        response.put("totalItens", servicos.getTotalElements());
        response.put("totalPages", servicos.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @Transactional(readOnly = true)
    public ServicoRequestOut buscarPorId(Long id) {
        Servico servico = servicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço não encontrado com o ID: " + id));

        return paraResponse(servico);
    }

    private ServicoRequestOut paraResponse(Servico servico) {
        return new ServicoRequestOut(
                servico.getId(),
                servico.getNome(),
                servico.getValor(),
                servico.getTempoMedioEstimadoMinutos(),
                servico.getAtivo()
        );
    }

    @Transactional
    public ResponseEntity inativar(Long id) {
        Servico servico = servicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço não encontrado com o ID: " + id));

        servico.inativar();
        servicoRepository.save(servico);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity atualizar(Long id, ServicoRequestIn req){
        Servico servico = servicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço não encontrado com o ID: " + id));

        servico.atualizarDados(req.nome(), req.valor(), req.tempoMedioEstimado());
        servicoRepository.save(servico);
        return ResponseEntity.ok().build();
    }

    private void validarServico(ServicoRequestIn request) {
        if (request == null) {
            throw new RegraDeNegocioException("Os dados do serviço são obrigatórios.");
        }

        if (request.nome() == null || request.nome().trim().isEmpty()) {
            throw new RegraDeNegocioException("O nome do serviço é obrigatório.");
        }

        if (request.valor() == null || request.valor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("O valor do serviço deve ser maior que zero.");
        }
    }
}
