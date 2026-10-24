package br.edu.unifor.nubex.catalogo.mapper;

import br.edu.unifor.nubex.catalogo.dto.EquipamentoResponse;
import br.edu.unifor.nubex.catalogo.dto.EquipamentoStatusResponse;
import br.edu.unifor.nubex.catalogo.model.Equipamento;
import org.springframework.stereotype.Component;

@Component
public class EquipamentoMapper {

    public EquipamentoResponse toResponse(Equipamento equipamento){
        return new EquipamentoResponse(
                equipamento.getId(),
                equipamento.getLaboratorio().getId(),
                equipamento.getNome(),
                equipamento.getDescricao(),
                equipamento.getStatus()
        );
    }

    public EquipamentoStatusResponse toStatus(Equipamento equipamento){
        return new EquipamentoStatusResponse(equipamento.getId(), equipamento.getStatus());
    }
}
