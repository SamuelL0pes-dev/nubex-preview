import type { ReactNode } from "react";
import type { TipoErroFetch } from "../../../hooks/useFetch";

export interface EstadoSecaoProps<T = unknown> {
  carregando: boolean;
  erro?: Error | null | unknown;
  tipoErro?: TipoErroFetch | null;
  dado?: T | null;
  nomeSecao?: string;
  onTentarNovamente?: () => void;
  esqueleto?: ReactNode;
  mensagemErro?: string;
  mensagemOffline?: string;
  compacto?: boolean;
  className?: string;
  children: ReactNode | ((dado: T) => ReactNode);
}
