package br.edu.unifor.nubex.conteudo.service;

import br.edu.unifor.nubex.common.util.FusoNubex;
import br.edu.unifor.nubex.conteudo.mapper.NoticiaMapper;
import br.edu.unifor.nubex.conteudo.repository.NoticiaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoticiaServiceTest {

    @Mock
    private NoticiaRepository repository;
    @Mock
    private NoticiaMapper mapper;
    @InjectMocks
    private NoticiaService service;

    @Captor
    private ArgumentCaptor<LocalDate> data;
    @Captor
    private ArgumentCaptor<Pageable> paginacao;

    @Test
    void limiteAcimaDoTetoEhCortadoEmVinte() {
        quandoConsultado();

        service.listar(100000);

        assertThat(paginacaoUsada().getPageSize()).isEqualTo(20);
    }

    @Test
    void limiteZeroOuNegativoViraUm() {
        quandoConsultado();

        service.listar(-1);

        assertThat(paginacaoUsada().getPageSize()).isEqualTo(1);
    }

    /** O filtro de data usa o fuso de Fortaleza, nao o da maquina. */
    @Test
    void pedeApenasNoticiasPublicadasAteHoje() {
        quandoConsultado();

        service.listar(3);

        verify(repository).findByAtivoTrueAndDataPublicacaoLessThanEqualOrderByDataPublicacaoDesc(
                data.capture(), paginacao.capture());
        assertThat(data.getValue()).isEqualTo(FusoNubex.hoje());
    }

    private void quandoConsultado() {
        when(repository.findByAtivoTrueAndDataPublicacaoLessThanEqualOrderByDataPublicacaoDesc(
                any(), any())).thenReturn(List.of());
    }

    private Pageable paginacaoUsada() {
        verify(repository).findByAtivoTrueAndDataPublicacaoLessThanEqualOrderByDataPublicacaoDesc(
                data.capture(), paginacao.capture());
        return paginacao.getValue();
    }
}
