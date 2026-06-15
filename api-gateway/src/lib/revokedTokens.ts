/**
 * Blocklist em memória de tokens revogados no logout.
 *
 * O JWT é stateless, então o logout (R2) só tem efeito se o gateway passar a
 * rejeitar o token usado. Mantemos o conjunto em memória do processo do gateway
 * — suficiente para o contexto (instância única). Cada login emite um token novo,
 * que não está revogado.
 */
const revoked = new Set<string>();

export function revokeToken(token: string): void {
  if (token) revoked.add(token);
}

/**
 * Reativa um token. Necessário porque o JWT do ms-auth é determinístico (iat em
 * segundos, sem jti): um login logo após o logout do mesmo usuário pode reemitir
 * um token idêntico ao revogado. Um login bem-sucedido = sessão válida → o token
 * volta a valer.
 */
export function unrevokeToken(token: string): void {
  if (token) revoked.delete(token);
}

export function isRevoked(token: string): boolean {
  return revoked.has(token);
}
