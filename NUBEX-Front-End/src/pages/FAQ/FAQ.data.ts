export interface PerguntaFAQ {
  id: string;
  pergunta: string;
  resposta: string;
}

export interface CategoriaFAQ {
  id: string;
  titulo: string;
  perguntas: PerguntaFAQ[];
}

export const CATEGORIAS_FAQ: CategoriaFAQ[] = [
  {
    id: "sobre",
    titulo: "Sobre o NUBEX",
    perguntas: [
      {
        id: "o-que-e",
        pergunta: "O que é o NUBEX?",
        resposta:
          "O Núcleo de Biologia Experimental (NUBEX) da Universidade de Fortaleza (Unifor) é uma plataforma científica multiusuária que reúne biotério SPF, laboratórios de biossegurança NB3 e plataformas analíticas de espectrometria e genômica, voltada à pesquisa, ao desenvolvimento e à inovação em saúde.",
      },
      {
        id: "quem-pode-usar",
        pergunta: "Quem pode utilizar a infraestrutura do NUBEX?",
        resposta:
          "Pesquisadores e estudantes da Unifor, pesquisadores de outras instituições de ensino e pesquisa e empresas do setor produtivo. Cada perfil segue regras próprias de cadastro e de uso.",
      },
      {
        id: "onde-fica",
        pergunta: "Onde o NUBEX está localizado?",
        resposta:
          "No campus da Unifor, na Av. Washington Soares, 1321, Edson Queiroz, Fortaleza - CE.",
      },
    ],
  },
  {
    id: "servicos",
    titulo: "Serviços e Agendamento",
    perguntas: [
      {
        id: "como-solicitar",
        pergunta: "Como solicito um serviço ou o uso de um equipamento?",
        resposta:
          "Pela página de Agendamento: escolha a unidade, o serviço ou equipamento e a data desejada. A equipe técnica analisa o pedido e confirma a reserva.",
      },
      {
        id: "cadastro",
        pergunta: "Preciso ter cadastro para agendar?",
        resposta:
          "Sim. O acesso ao portal é necessário para acompanhar solicitações, receber resultados e manter o histórico dos seus projetos.",
      },
      {
        id: "prazo",
        pergunta: "Qual é o prazo para receber os resultados?",
        resposta:
          "Depende do tipo de análise e da fila de cada laboratório. O prazo estimado é informado na confirmação do agendamento.",
      },
      {
        id: "valores",
        pergunta: "Os serviços são pagos?",
        resposta:
          "Os valores variam conforme o serviço e o perfil do solicitante (interno, acadêmico externo ou empresa). Entre em contato para receber um orçamento.",
      },
    ],
  },
  {
    id: "biosseguranca",
    titulo: "Biossegurança e Ética",
    perguntas: [
      {
        id: "ceua",
        pergunta: "Projetos com animais precisam de aprovação ética?",
        resposta:
          "Sim. Todo projeto que envolva animais deve ter aprovação prévia da Comissão de Ética no Uso de Animais (CEUA), conforme a Lei nº 11.794/2008.",
      },
      {
        id: "spf",
        pergunta: "O que significa biotério SPF?",
        resposta:
          "SPF (Specific Pathogen Free) indica animais livres de patógenos específicos, mantidos sob barreiras sanitárias rigorosas, o que garante maior confiabilidade aos resultados experimentais.",
      },
      {
        id: "nb3",
        pergunta: "Quem pode trabalhar no laboratório NB3?",
        resposta:
          "Apenas profissionais com treinamento específico em biossegurança nível 3 e autorização da coordenação do laboratório.",
      },
    ],
  },
  {
    id: "parcerias",
    titulo: "Parcerias e Projetos",
    perguntas: [
      {
        id: "parceria",
        pergunta: "Como minha instituição ou empresa pode firmar uma parceria?",
        resposta:
          "Envie uma proposta pela página de Contato descrevendo o projeto e o tipo de colaboração. A coordenação avalia e retorna com os próximos passos.",
      },
      {
        id: "pos-graduacao",
        pergunta:
          "Alunos de pós-graduação podem desenvolver projetos no NUBEX?",
        resposta:
          "Sim, desde que vinculados a um orientador responsável e com o projeto cadastrado e aprovado pelas instâncias necessárias.",
      },
    ],
  },
];