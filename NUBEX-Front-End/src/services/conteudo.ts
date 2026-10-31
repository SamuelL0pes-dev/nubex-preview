import { api } from "./api";
import type { Edital, Noticia } from "../types/conteudo";

/** Noticias ativas, da mais recente para a mais antiga. */
export function listarNoticias(limite = 3): Promise<Noticia[]> {
  return api.get<Noticia[]>(`/noticias?limite=${limite}`);
}

/** O edital aberto do mural. Vem undefined quando o back responde 204. */
export function buscarEditalAberto(): Promise<Edital | undefined> {
  return api.get<Edital | undefined>("/editais/aberto");
}
