package br.com.oficinatrack.estoque.application;

import br.com.oficinatrack.estoque.application.dto.MovimentacaoEstoqueRequest;
import br.com.oficinatrack.estoque.application.dto.PecaRequest;
import br.com.oficinatrack.estoque.application.dto.PecaResponse;
import br.com.oficinatrack.estoque.application.dto.PecaUpdateRequest;
import br.com.oficinatrack.estoque.domain.MovimentacaoEstoque;
import br.com.oficinatrack.estoque.domain.Peca;
import br.com.oficinatrack.estoque.domain.TipoMovimentacao;
import br.com.oficinatrack.estoque.exceptions.PecaNaoEncontradaException;
import br.com.oficinatrack.estoque.infrastructure.MovimentacaoEstoqueRepository;
import br.com.oficinatrack.estoque.infrastructure.PecaRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;



@Service
public class PecaService {
    // lógica de negócio

    private PecaRepository pecaRepository;
    private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;


    public PecaService(PecaRepository pecaRepository, MovimentacaoEstoqueRepository movimentacaoEstoqueRepository) {
        this.pecaRepository = pecaRepository;
        this.movimentacaoEstoqueRepository = movimentacaoEstoqueRepository;
    }

    // metodos de get
    public Page<PecaResponse> listar(String nome, Boolean estoqueBaixo, Pageable pageable){
        return pecaRepository.buscarComFiltros(nome, estoqueBaixo, pageable)
                .map(PecaResponse::from);
    }

    public PecaResponse buscarPorId(Long id){
        Peca peca = pecaRepository.findById(id)
                .orElseThrow(() -> new PecaNaoEncontradaException(
                                "Peça não encontrada com o ID: " + id
                        )
                );
        return PecaResponse.from(peca);
    }
    //metodos post

    public PecaResponse cadastrar(PecaRequest request){

        Peca peca = Peca.cadastrar(
                request.getNome(),
                request.getValorUnitario(),
                request.getQuantidadeEstoque(),
                request.getEstoqueMinimo()
        );

        Peca pecaSalva = pecaRepository.save(peca);
        return PecaResponse.from(pecaSalva);
    }

    @Transactional
    public PecaResponse atualizar(Long id, PecaUpdateRequest request){
        Peca peca = pecaRepository.findById(id)
                .orElseThrow(() -> new PecaNaoEncontradaException(
                                "Peça não encontrada com o ID: " + id
                        )
                );

        peca.atualizarDadosCadastrais(
                request.getNome(),
                request.getValorUnitario(),
                request.getEstoqueMinimo()
        );

        Peca pecaAtualizada = pecaRepository.save(peca);

        return PecaResponse.from(pecaAtualizada);
    }

    @Transactional
    public PecaResponse movimentarEstoque(Long id, MovimentacaoEstoqueRequest request){
        Peca peca = pecaRepository.findById(id)
                .orElseThrow(() -> new PecaNaoEncontradaException(
                                "Peça não encontrada com o ID: " + id
                        )
                );

        switch (request.getTipo()) {
            case ENTRADA -> peca.incrementarEstoque(request.getQuantidade());
            case SAIDA -> peca.debitarEstoque(request.getQuantidade());
            default -> throw new IllegalArgumentException("Tipo de movimentação inválido: " + request.getTipo());
        }

        Peca pecaAtualizada = pecaRepository.save(peca);

        MovimentacaoEstoque movimentacao = MovimentacaoEstoque.registrarManual(
                pecaAtualizada,
                request.getTipo(),
                request.getQuantidade(),
                1L
                // usuarioAtual() // quando o user for implementado eu coloco.
        );

        movimentacaoEstoqueRepository.save(movimentacao);

        return PecaResponse.from(pecaAtualizada);
    }
}
