/** O que o back-end devolve em /api/noticias e /api/editais. */
type ItemDoMural = {
  id: number;
  titulo: string;
  resumo: string | null;
  unidade: string | null;
  imagemUrl: string | null;
  link: string | null;
  dataPublicacao: string;
  destaque: boolean;
};

export type Noticia = ItemDoMural;

export type Edital = ItemDoMural & {
  dataEncerramento: string;
};

export type CartaoDoMural =
  | (Noticia & { categoria: "noticia" })
  | (Edital & { categoria: "edital" });