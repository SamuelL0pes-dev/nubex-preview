export type AnelParceiro = "interno" | "externo";
export type EscopoParceiro = "Nacional" | "Internacional";

export interface Parceiro {
  id: string;
  nome: string;
  sigla: string;
  escopo: EscopoParceiro;
  descricao: string;
  logo: string;
  anel: AnelParceiro;
}

export const PARCEIROS: Parceiro[] = [
  { id: "unifor",      sigla: "Unifor",  nome: "Universidade de Fortaleza",          escopo: "Nacional",      anel: "externo", logo: "/parceiros/unifor.png",      descricao: "Instituição onde o NUBEX está sediado." },
  { id: "usp",         sigla: "USP",     nome: "Universidade de São Paulo",          escopo: "Nacional",      anel: "externo", logo: "/parceiros/usp.png",         descricao: "Maior universidade pública do país." },
  { id: "ufc",         sigla: "UFC",     nome: "Universidade Federal do Ceará",      escopo: "Nacional",      anel: "externo", logo: "/parceiros/ufc.png",         descricao: "Universidade federal sediada em Fortaleza." },
  { id: "uece",        sigla: "UECE",    nome: "Universidade Estadual do Ceará",     escopo: "Nacional",      anel: "externo", logo: "/parceiros/uece.png",        descricao: "Universidade estadual cearense." },
  { id: "ufpi",        sigla: "UFPI",    nome: "Universidade Federal do Piauí",      escopo: "Nacional",      anel: "externo", logo: "/parceiros/ufpi.png",        descricao: "Universidade federal do Piauí." },
  { id: "ufpe",        sigla: "UFPE",    nome: "Universidade Federal de Pernambuco", escopo: "Nacional",      anel: "externo", logo: "/parceiros/ufpe.png",        descricao: "Universidade federal pernambucana." },
  { id: "ufma",        sigla: "UFMA",    nome: "Universidade Federal do Maranhão",   escopo: "Nacional",      anel: "externo", logo: "/parceiros/ufma.png",        descricao: "Universidade federal maranhense." },
  { id: "embrapa",     sigla: "Embrapa", nome: "Embrapa",                            escopo: "Nacional",      anel: "externo", logo: "/parceiros/embrapa.png",     descricao: "Empresa Brasileira de Pesquisa Agropecuária." },
  { id: "fiocruz",     sigla: "Fiocruz", nome: "Fundação Oswaldo Cruz",              escopo: "Nacional",      anel: "externo", logo: "/parceiros/fiocruz.png",     descricao: "Pesquisa biomédica e saúde pública." },

  { id: "toronto",     sigla: "Toronto", nome: "Universidade de Toronto",            escopo: "Internacional", anel: "interno", logo: "/parceiros/toronto.png",     descricao: "University of Toronto, Canadá." },
  { id: "ucl",         sigla: "UCL",     nome: "University College London",          escopo: "Internacional", anel: "interno", logo: "/parceiros/ucl.png",         descricao: "Reino Unido." },
  { id: "utah",        sigla: "Utah",    nome: "Universidade de Utah",               escopo: "Internacional", anel: "interno", logo: "/parceiros/utah.png",        descricao: "University of Utah, Estados Unidos." },
  { id: "concepcion",  sigla: "UdeC",    nome: "Universidad de Concepción",          escopo: "Internacional", anel: "interno", logo: "/parceiros/concepcion.png",  descricao: "Chile." },
  { id: "pensilvania", sigla: "Penn",    nome: "Universidade da Pensilvânia",        escopo: "Internacional", anel: "interno", logo: "/parceiros/pensilvania.png", descricao: "University of Pennsylvania, Estados Unidos." },
  { id: "roslin",      sigla: "Roslin",  nome: "Instituto Roslin",                   escopo: "Internacional", anel: "interno", logo: "/parceiros/roslin.png",      descricao: "Universidade de Edimburgo, Escócia." },
  { id: "coimbra",     sigla: "Coimbra", nome: "Universidade de Coimbra",            escopo: "Internacional", anel: "interno", logo: "/parceiros/coimbra.png",     descricao: "Portugal." },
];