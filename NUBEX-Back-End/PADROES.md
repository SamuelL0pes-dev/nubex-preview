## Permissões (Spring Security)

- O token gera uma permissão por perfil, no formato `ROLE_<PERFIL>`: `ROLE_ADMINISTRADOR`, `ROLE_SUPERVISAO_ADMINISTRATIVA`, `ROLE_ADMINISTRADOR_CONSULTA`, `ROLE_SOLICITANTE` e `ROLE_PARTICIPANTE`.
- Nos controllers, use `@PreAuthorize("hasRole('ADMINISTRADOR')")` ou `hasAnyRole(...)`, sem o prefixo: o `hasRole` acrescenta o `ROLE_` sozinho.
- Nos testes, monte a permissão com o texto exato do token: `.with(user("admin").authorities(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR")))`.
