const SERVICOS = {
  usuarios: import.meta.env.VITE_API_USUARIOS,
  agendamentos: import.meta.env.VITE_API_AGENDAMENTOS,
  catalogo: import.meta.env.VITE_API_CATALOGO,
  projetos: import.meta.env.VITE_API_PROJETOS,
  conteudo: import.meta.env.VITE_API_CONTEUDO,
};

type Servico = keyof typeof SERVICOS;

const ONDE_MORA: Record<string, Servico> = {
  avisos: "conteudo",
  noticias: "conteudo",
  parceiros: "conteudo",
  editais: "conteudo",
  faq: "conteudo",
  pesquisadores: "conteudo",
  contato: "conteudo",

  unidades: "catalogo",
  laboratorios: "catalogo",
  equipamentos: "catalogo",
  servicos: "catalogo",

  usuarios: "usuarios",
  auth: "usuarios",
  cadastro: "usuarios",

  agendamentos: "agendamentos",
  feriados: "agendamentos",

  projetos: "projetos",
  compras: "projetos",
};

type ErroDoBack = {
  mensagem?: string;
  codigo?: string;
  campos?: { campo: string; mensagem: string }[];
};

export class ErroDaApi extends Error {
  status: number;
  codigo?: string;
  campos?: { campo: string; mensagem: string }[];

  constructor(
    status: number,
    mensagem: string,
    codigo?: string,
    campos?: { campo: string; mensagem: string }[],
  ) {
    super(mensagem);
    this.name = "ErroDaApi";
    this.status = status;
    this.codigo = codigo;
    this.campos = campos;
  }
}

function obterTokenArmazenado(): string | null {
  try {
    return (
      sessionStorage.getItem("nubex_token") ??
      localStorage.getItem("nubex_token")
    );
  } catch {
    return null;
  }
}

// "/avisos" -> "http://localhost:8080/api/avisos"
export function enderecoDe(caminho: string): string {
  const recurso = caminho.replace(/^\//, "").split(/[/?]/)[0];
  const servico = ONDE_MORA[recurso];

  if (!servico) {
    throw new Error(`Recurso "${recurso}" nao esta mapeado em ONDE_MORA.`);
  }

  const base = SERVICOS[servico];

  if (!base) {
    throw new Error(`Falta VITE_API_${servico.toUpperCase()} no .env.`);
  }

  return `${base}/api${caminho}`;
}

async function pedir<T>(caminho: string, opcoes?: RequestInit): Promise<T> {
  const cabecalhos = new Headers(opcoes?.headers);

  if (opcoes?.body && !cabecalhos.has("Content-Type")) {
    cabecalhos.set("Content-Type", "application/json");
  }

  if (!cabecalhos.has("Authorization")) {
    const token = obterTokenArmazenado();
    if (token) {
      cabecalhos.set("Authorization", `Bearer ${token}`);
    }
  }

  const resposta = await fetch(enderecoDe(caminho), {
    ...opcoes,
    headers: cabecalhos,
  });

  if (!resposta.ok) {
    const corpo: ErroDoBack = await resposta.json().catch(() => ({}));

    throw new ErroDaApi(
      resposta.status,
      corpo.mensagem ?? `O servidor respondeu ${resposta.status}.`,
      corpo.codigo,
      corpo.campos,
    );
  }

  if (resposta.status === 204) {
    return undefined as unknown as T;
  }

  return (await resposta.json()) as T;
}

export const api = {
  get: <T>(caminho: string) => pedir<T>(caminho),
  post: <T>(caminho: string, corpo: unknown) =>
    pedir<T>(caminho, { method: "POST", body: JSON.stringify(corpo) }),
  put: <T>(caminho: string, corpo: unknown) =>
    pedir<T>(caminho, { method: "PUT", body: JSON.stringify(corpo) }),
  remove: <T>(caminho: string) => pedir<T>(caminho, { method: "DELETE" }),
};
