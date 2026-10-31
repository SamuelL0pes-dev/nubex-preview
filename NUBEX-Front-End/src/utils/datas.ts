const MESES = [
  "jan", "fev", "mar", "abr", "mai", "jun",
  "jul", "ago", "set", "out", "nov", "dez",
];

export function dataCurta(iso: string): string {
  const [ano, mes, dia] = iso.split("-");
  return `${dia} ${MESES[Number(mes) - 1]} ${ano}`;
}

export function diaEMes(iso: string): string {
  const [, mes, dia] = iso.split("-");
  return `${dia}/${mes}`;
}

export function diasAte(iso: string): number {
  const [ano, mes, dia] = iso.split("-").map(Number);
  const alvo = Date.UTC(ano, mes - 1, dia);
  const agora = new Date();
  const hoje = Date.UTC(agora.getFullYear(), agora.getMonth(), agora.getDate());

  return Math.round((alvo - hoje) / 86400000);
}