package br.edu.unifor.nubex.catalogo.mapper;

import br.edu.unifor.nubex.catalogo.dto.EquipamentoResponse;
import br.edu.unifor.nubex.catalogo.dto.LaboratorioDetalheResponse;
import br.edu.unifor.nubex.catalogo.dto.UnidadeDetalheResponse;
import br.edu.unifor.nubex.catalogo.dto.UnidadeResumoResponse;
import br.edu.unifor.nubex.catalogo.model.Equipamento;
import br.edu.unifor.nubex.catalogo.model.Laboratorio;
import br.edu.unifor.nubex.catalogo.model.UnidadeEstrategica;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class UnidadeMapper {
    private final LaboratorioMapper laboratorioMapper;

    public UnidadeMapper(LaboratorioMapper laboratorioMapper) {
        this.laboratorioMapper = laboratorioMapper;
    }

    public UnidadeResumoResponse toResumo(UnidadeEstrategica unidade){
        return new UnidadeResumoResponse(
                unidade.getId(),
                unidade.getNome(),
                unidade.getSlug(),
                unidade.getDescricao(),
                unidade.getImagemUrl()
        );
    }

    public EquipamentoResponse toEquipamento(Equipamento equipamento){
        return new EquipamentoResponse(
                equipamento.getId(),
                equipamento.getLaboratorio().getId(),
                equipamento.getNome(),
                equipamento.getDescricao(),
                equipamento.getStatus()
        );
    }

    public UnidadeDetalheResponse toDetalhe(UnidadeEstrategica unidade, List<Laboratorio> laboratorios, Map<Long, List<Equipamento>> equipamentosPorLaboratorio){
        List<LaboratorioDetalheResponse> labs = laboratorios.stream().map(lab -> laboratorioMapper.toDetalhe(
                lab,
                equipamentosPorLaboratorio.getOrDefault(lab.getId(), List.of())
        )).toList();
        return new UnidadeDetalheResponse(
                unidade.getId(),
                unidade.getNome(),
                unidade.getSlug(),
                unidade.getDescricao(),
                unidade.getImagemUrl(),
                labs
        );
    }
}
