-- Recupera o Data URL mantendo os valores e os Large Objects originais.
BEGIN;
LOCK TABLE foto IN SHARE ROW EXCLUSIVE MODE;
CREATE TABLE IF NOT EXISTS foto_imagem_lob_backup (
    foto_id bigint PRIMARY KEY,
    referencia_original text NOT NULL,
    salvo_em timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO foto_imagem_lob_backup (foto_id, referencia_original)
SELECT id, imagem_base64 FROM foto WHERE imagem_base64 ~ '^[0-9]+$'
ON CONFLICT (foto_id) DO NOTHING;
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM foto WHERE imagem_base64 ~ '^[0-9]+$'
        AND convert_from(lo_get(imagem_base64::oid), 'UTF8')
            !~ '^data:image/(png|jpeg|gif|webp);base64,'
    ) THEN
        RAISE EXCEPTION 'Conteudo legado invalido; conversao cancelada.';
    END IF;
END $$;
UPDATE foto SET imagem_base64 = convert_from(lo_get(imagem_base64::oid), 'UTF8')
WHERE imagem_base64 ~ '^[0-9]+$';
COMMIT;
