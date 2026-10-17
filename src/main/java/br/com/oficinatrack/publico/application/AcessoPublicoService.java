package br.com.oficinatrack.publico.application;

import br.com.oficinatrack.ordemservico.domain.OrdemServico;
import br.com.oficinatrack.ordemservico.infra.OrdemServicoRepository;
import br.com.oficinatrack.publico.api.dto.AcessoPublicoResponse;
import br.com.oficinatrack.publico.domain.AcessoPublicoOrdemServico;
import br.com.oficinatrack.publico.infra.AcessoPublicoRepository;
import br.com.oficinatrack.shared.exception.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class AcessoPublicoService {
    private final AcessoPublicoRepository acessoRepository;
    private final OrdemServicoRepository ordemRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${api.public-access.expiration-days:7}")
    private long expirationDays;

    public AcessoPublicoService(AcessoPublicoRepository acessoRepository, OrdemServicoRepository ordemRepository) {
        this.acessoRepository = acessoRepository;
        this.ordemRepository = ordemRepository;
    }

    @Transactional
    public AcessoPublicoResponse emitir(Long ordemId) {
        OrdemServico os = ordemRepository.findById(ordemId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de serviço não encontrada."));
        acessoRepository.findAllByOrdemServicoIdAndRevogadoEmIsNull(ordemId)
                .forEach(AcessoPublicoOrdemServico::revogar);
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        LocalDateTime expiraEm = LocalDateTime.now().plusDays(expirationDays);
        AcessoPublicoOrdemServico acesso = acessoRepository.save(
                new AcessoPublicoOrdemServico(os, hash(token), expiraEm));
        return new AcessoPublicoResponse(acesso.getId(), token, os.getNumeroOs(), acesso.getExpiraEm());
    }

    @Transactional
    public void revogar(Long ordemId, Long acessoId) {
        AcessoPublicoOrdemServico acesso = acessoRepository.findByIdAndOrdemServicoId(acessoId, ordemId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Acesso público não encontrado."));
        acesso.revogar();
    }

    @Transactional(readOnly = true)
    public AcessoPublicoOrdemServico validar(String authorization, String numeroOs) {
        String token = extrairToken(authorization);
        AcessoPublicoOrdemServico acesso = acessoRepository.findByTokenHash(hash(token))
                .filter(AcessoPublicoOrdemServico::estaValido)
                .orElseThrow(CredencialPublicaException::new);
        if (!acesso.getOrdemServico().getNumeroOs().equals(numeroOs))
            throw new RecursoNaoEncontradoException("Atendimento não encontrado.");
        return acesso;
    }

    private String extrairToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ") || authorization.length() <= 7)
            throw new CredencialPublicaException();
        return authorization.substring(7).trim();
    }

    private String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 indisponível", ex);
        }
    }
}
