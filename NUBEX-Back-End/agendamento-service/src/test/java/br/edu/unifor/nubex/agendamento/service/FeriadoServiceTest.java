package br.edu.unifor.nubex.agendamento.service;

import br.edu.unifor.nubex.agendamento.dto.FeriadoRequest;
import br.edu.unifor.nubex.agendamento.dto.FeriadoResponse;
import br.edu.unifor.nubex.agendamento.mapper.FeriadoMapper;
import br.edu.unifor.nubex.agendamento.model.Feriado;
import br.edu.unifor.nubex.agendamento.repository.FeriadoRepository;
import br.edu.unifor.nubex.common.exception.ConflitoException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FeriadoServiceTest {
    private final FeriadoRepository repository = mock(FeriadoRepository.class);
    private final FeriadoService service = new FeriadoService(repository, new FeriadoMapper());
    private static final LocalDate NATAL = LocalDate.of(2026, 12, 25);

    @Test
    void alterarSoADescricaoNaoConflitaComOProprioFeriado(){
        Feriado natal = new Feriado();
        ReflectionTestUtils.setField(natal, "id", 7L);
        natal.setData(NATAL);
        natal.setDescricao("Natal");

        when(repository.findById(7L)).thenReturn(Optional.of(natal));
        when(repository.existsByData(NATAL)).thenReturn(true);
        when(repository.existsByDataAndIdNot(NATAL, 7L)).thenReturn(false);

        FeriadoResponse resposta = service.atualizar(7L, new FeriadoRequest(NATAL, "Natal de Jesus"));

        assertEquals("Natal de Jesus", resposta.descricao());
    }

    @Test
    void alterarParaADataDeOutroFeriadoLancaConflito(){
        Feriado feriado = new Feriado();
        ReflectionTestUtils.setField(feriado, "id", 7L);
        feriado.setData(LocalDate.of(2026, 11, 2));
        feriado.setDescricao("Finados");

        when(repository.findById(7L)).thenReturn(Optional.of(feriado));
        when(repository.existsByDataAndIdNot(NATAL, 7L)).thenReturn(true);

        assertThrows(ConflitoException.class, () -> service.atualizar(7L, new FeriadoRequest(NATAL, "Natal")));
    }
}
