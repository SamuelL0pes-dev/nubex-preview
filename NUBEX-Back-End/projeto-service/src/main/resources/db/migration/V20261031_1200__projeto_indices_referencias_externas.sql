-- =====================================================================
-- Domínio projeto: índices e documentação das colunas que apontam para
-- outros serviços (usuario-service e catalogo-service).
-- Contexto: cartão "Integridade sem chave estrangeira entre serviços".
-- Desde a separação dos bancos (07/10), essas referências não têm FK:
-- a existência do registro é verificada pela aplicação antes de gravar.
-- =====================================================================

CREATE INDEX idx_compra_unidade ON solicitacao_compra (unidade_id);
CREATE INDEX idx_compra_equipamento ON solicitacao_compra(equipamento_id);

CREATE INDEX idx_projeto_integrante_usuario ON projeto_integrante (usuario_id);

COMMENT ON COLUMN projeto.solicitante_id IS 'Id do usuário no usuario-service. Sem FK: existência verificada pela aplicação.';
COMMENT ON COLUMN solicitacao_compra.avaliado_por IS 'Id do usuário no usuario-service. Sem FK: existência verificada pela aplicação.';
COMMENT ON COLUMN solicitacao_compra.solicitante_id IS 'Id do usuário no usuario-service. Sem FK: existência verificada pela aplicação.';
COMMENT ON COLUMN solicitacao_compra.unidade_id IS 'Id da unidade no catalogo-service. Sem FK: existência verificada pela aplicação.';
COMMENT ON COLUMN solicitacao_compra.equipamento_id IS 'Id do equipamento no catalogo-service. Sem FK: existência verificada pela aplicação.';
COMMENT ON COLUMN projeto_integrante.usuario_id IS 'Id do usuário no usuario-service. Sem FK: existência verificada pela aplicação.';
