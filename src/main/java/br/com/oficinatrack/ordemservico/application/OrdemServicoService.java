package br.com.oficinatrack.ordemservico.application;

import br.com.oficinatrack.cliente.api.dto.ClienteResponse;
import br.com.oficinatrack.cliente.domain.Cliente;
import br.com.oficinatrack.ordemservico.api.dto.in.CadastrarOrdemServicoIn;
import br.com.oficinatrack.ordemservico.api.dto.out.CadastrarOrdemServicoOut;
import br.com.oficinatrack.ordemservico.domain.OrdemServico;
import br.com.oficinatrack.ordemservico.infra.OrderServicoRepository;
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
public class OrdemServicoService {

    private OrderServicoRepository orderServicoRepository;

    public OrdemServicoService(OrderServicoRepository orderServicoRepository) {
        this.orderServicoRepository = orderServicoRepository;
    }


    @Transactional
    public ResponseEntity<?> cadastrar(CadastrarOrdemServicoIn request) throws Exception {

        validarServico(request);

        OrdemServico servico = new OrdemServico(
                request.nome(),
                request.valor(),
                request.tempoMedioEstimado()
        );


        OrdemServico servicoSalvo = orderServicoRepository.save(servico);

        return ResponseEntity.ok(new CadastrarOrdemServicoOut(
                        servicoSalvo.getId(),
                        servicoSalvo.getNome(),
                        servicoSalvo.getValor(),
                        servicoSalvo.getTempoMedioEstimadoMinutos()
                )
        );

    }

    @Transactional(readOnly = true)
    public ResponseEntity listar(String nome, Pageable pageable) {
        Page<OrdemServico> servicos;

        if (nome != null && !nome.isBlank()) {
            servicos = orderServicoRepository.findByNomeContainingIgnoreCase(nome, pageable);
        } else {
            servicos = orderServicoRepository.findAll(pageable);
        }

        Page<CadastrarOrdemServicoOut> ret = servicos.map(s -> new CadastrarOrdemServicoOut(
                s.getId(),
                s.getNome(),
                s.getValor(),
                s.getTempoMedioEstimadoMinutos()
        ));

        Map<String, Object> response = new HashMap<>();
        response.put("ordens", ret);
        response.put("currentPage", servicos.getNumber());
        response.put("totalItens", servicos.getTotalElements());
        response.put("totalPages", servicos.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @Transactional(readOnly = true)
    public ResponseEntity buscarPorId(Long id) {
        OrdemServico servico = orderServicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço não encontrado com o ID: " + id));

        return ResponseEntity.ok(new CadastrarOrdemServicoOut(
                        servico.getId(),
                        servico.getNome(),
                        servico.getValor(),
                        servico.getTempoMedioEstimadoMinutos()
                )
        );
    }

    @Transactional
    public ResponseEntity inativar(Long id) {
        OrdemServico servico = orderServicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço não encontrado com o ID: " + id));

        servico.inativar();
        orderServicoRepository.save(servico);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity atualizar(Long id, CadastrarOrdemServicoIn req){
        OrdemServico servico = orderServicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço não encontrado com o ID: " + id));

        servico.atualizarDados(req.nome(), req.valor(), req.tempoMedioEstimado());
        orderServicoRepository.save(servico);
        return ResponseEntity.ok().build();
    }

    private void validarServico(CadastrarOrdemServicoIn request) {
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
