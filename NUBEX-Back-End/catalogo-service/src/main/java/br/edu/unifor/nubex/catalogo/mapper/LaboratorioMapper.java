package br.edu.unifor.nubex.catalogo.mapper;

import br.edu.unifor.nubex.catalogo.dto.LaboratorioDetalheResponse;
import br.edu.unifor.nubex.catalogo.dto.LaboratorioResponse;
import br.edu.unifor.nubex.catalogo.model.Equipamento;
import br.edu.unifor.nubex.catalogo.model.Laboratorio;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LaboratorioMapper {
    private final EquipamentoMapper equipamentoMapper;

    public LaboratorioMapper(EquipamentoMapper equipamentoMapper){
        this.equipamentoMapper = equipamentoMapper;
    }

    public LaboratorioResponse toResponse(Laboratorio laboratorio){
        return new LaboratorioResponse(
                laboratorio.getId(),
                laboratorio.getUnidade().getId(),
                laboratorio.getNome(),
                laboratorio.getDescricao()
        );
    }

    public LaboratorioDetalheResponse toDetalhe(Laboratorio laboratorio, List<Equipamento> equipamentos) {
        return new LaboratorioDetalheResponse(
                laboratorio.getId(),
                laboratorio.getNome(),
                laboratorio.getDescricao(),
                equipamentos.stream().map(equipamentoMapper::toResponse).toList()
        );
    }
}
