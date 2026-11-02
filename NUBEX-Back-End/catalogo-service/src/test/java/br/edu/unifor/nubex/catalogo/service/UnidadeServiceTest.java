package br.edu.unifor.nubex.catalogo.service;

import br.edu.unifor.nubex.catalogo.mapper.EquipamentoMapper;
import br.edu.unifor.nubex.catalogo.mapper.LaboratorioMapper;
import br.edu.unifor.nubex.catalogo.mapper.UnidadeMapper;
import br.edu.unifor.nubex.catalogo.repository.EquipamentoRepository;
import br.edu.unifor.nubex.catalogo.repository.LaboratorioRepository;
import br.edu.unifor.nubex.catalogo.repository.UnidadeEstrategicaRepository;
import br.edu.unifor.nubex.common.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UnidadeServiceTest {
    private final UnidadeEstrategicaRepository repository = mock(UnidadeEstrategicaRepository.class);
    private final LaboratorioRepository laboratorioRepository = mock(LaboratorioRepository.class);
    private final EquipamentoRepository equipamentoRepository = mock(EquipamentoRepository.class);
    private final UnidadeService service = new UnidadeService(repository, laboratorioRepository, equipamentoRepository, new UnidadeMapper(new LaboratorioMapper(new EquipamentoMapper())));

    @Test
    void buscarPorIdInexistenteInformaOId() {
        when(repository.findByIdAndAtivoTrue(999L)).thenReturn(Optional.empty());
        RecursoNaoEncontradoException erro = assertThrows(RecursoNaoEncontradoException.class, () -> service.buscarPorId(999L));
        assertEquals("Unidade não encontrada: 999", erro.getMessage());
    }

    @Test
    void buscarPorSlugInexistenteInformaOSlug(){
        when(repository.findBySlugAndAtivoTrue("nao-existe")).thenReturn(Optional.empty());
        RecursoNaoEncontradoException erro = assertThrows(RecursoNaoEncontradoException.class, () -> service.buscarPorSlug("nao-existe"));
        assertEquals("Unidade não encontrada: nao-existe", erro.getMessage());
    }
}
