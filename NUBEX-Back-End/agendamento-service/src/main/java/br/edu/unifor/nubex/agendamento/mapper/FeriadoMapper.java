package br.edu.unifor.nubex.agendamento.mapper;

import br.edu.unifor.nubex.agendamento.dto.FeriadoResponse;
import br.edu.unifor.nubex.agendamento.model.Feriado;
import org.springframework.stereotype.Component;

@Component
public class FeriadoMapper {

    public FeriadoResponse toResponse(Feriado feriado){
        return new FeriadoResponse(
                feriado.getId(),
                feriado.getData(),
                feriado.getDescricao()
        );
    }
}
