/**
 * Domínio "usuario". Tudo deste domínio fica neste pacote: controller, service,
 * repository e entidades (ex.: UsuarioController, UsuarioService, UsuarioRepository).
 *
 * REGRA DE OURO: este domínio nunca usa o repository de outro domínio. Quando
 * precisar de um dado que é de outro serviço, pede ao service dele.
 *
 * CORS, Swagger, tratamento de erros e segurança base vêm do nubex-common.
 */
package br.edu.unifor.nubex.usuario;
