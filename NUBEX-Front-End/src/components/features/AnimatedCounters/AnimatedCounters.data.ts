export interface CounterItem {
  id: string;
  valorFinal: number;
  prefixo?: string;
  sufixo?: string;
  frase: string;
}

export const COUNTERS_DATA: CounterItem[] = [
  {
    id: "area",
    valorFinal: 4000,
    prefixo: "+",

    frase: "m² de área construída dedicada à pesquisa e inovação",
  },
  {
    id: "unidades",
    valorFinal: 6,

    frase: "unidades estratégicas de biologia experimental",
  },
  {
    id: "ppgs",
    valorFinal: 5,

    frase: "programas de pós-graduação integrados",
  },
  {
    id: "laboratorios",
    valorFinal: 30,
    prefixo: "+",
    frase: "laboratórios multiusuários de alta tecnologia",
  },
];
