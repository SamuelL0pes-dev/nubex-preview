import type { ReactNode } from "react";
import { IconePesquisa, IconeLaboratorios, IconeServicos } from "./QuickAccessCards.icones"


export interface QuickAccessCardItem {
  id: string;
  tituloFrente: string;
  icone: ReactNode;
  resumoFrente: string;
  tituloVerso: string;
  textoVerso: string;
  linkTexto: string;
  linkUrl: string;
}

export const QUICK_ACCESS_CARDS: QuickAccessCardItem[] = [
  {
    id: "pesquisa",
    tituloFrente: "Pesquisa & Inovação",
    icone: <IconePesquisa />,
    resumoFrente: "Ciência de ponta e desenvolvimento de soluções biológicas.",
    tituloVerso: "Nossos Projetos",
    textoVerso:
      "Atuamos em biologia celular, genética aplicada e tecnologias para a saúde.",
    linkTexto: "Conhecer Linhas",
    linkUrl: "#",
  },
  {
    id: "laboratorios",
    tituloFrente: "Laboratórios Multiusuário",
     icone: <IconeLaboratorios/>,
    resumoFrente: "Infraestrutura avançada aberta à comunidade científica.",
    tituloVerso: "Instalações Abertas",
    textoVerso:
      "Equipamentos de última geração disponíveis para pesquisadores e empresas parceiras.",
    linkTexto: "Ver Instalações",
    linkUrl: "#",
  },
  {
    id: "servicos",
    tituloFrente: "Serviços Especializados",
     icone: <IconeServicos />,
    resumoFrente: "Ensaios biológicos, análises e consultoria técnica.",
    tituloVerso: "Atendimento Técnico",
    textoVerso:
      "Prestação de serviços com rigor analítico e controle de qualidade certificado.",
    linkTexto: "Solicitar Análise",
    linkUrl: "#",
  },
];
