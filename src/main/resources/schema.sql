-- 01/12/2018 : Mantis 0320 : Ajout information Frequence d'utilisation de la lice
ALTER TABLE IF EXISTS ls_stats_eleveur ADD COLUMN IF NOT EXISTS new_lice varchar(1);

-- 15/01/2019 : Ajout type d'inscription s/ le chien confirmé
ALTER TABLE IF EXISTS ls_stats_confirmation ADD COLUMN IF NOT EXISTS typ_inscription numeric(28,0);

-- 22/01/2019 : Ajout tri s/ les variétés
-- Table LS_RACE
-- id = primary key ls_race
ALTER TABLE IF EXISTS ls_race ADD COLUMN IF NOT EXISTS tri numeric(28,0);
-- Table LS_STATS_ELEVEUR
ALTER TABLE IF EXISTS ls_stats_eleveur ADD COLUMN IF NOT EXISTS tri numeric(28,0);
-- Table LS_STATS_ADN
ALTER TABLE IF EXISTS ls_stats_adn ADD COLUMN IF NOT EXISTS tri numeric(28,0);
-- Table LS_STATS_CONFIRMATION
ALTER TABLE IF EXISTS ls_stats_confirmation ADD COLUMN IF NOT EXISTS tri numeric(28,0);
-- Table LS_STATS_SANTE
ALTER TABLE IF EXISTS ls_stats_sante ADD COLUMN IF NOT EXISTS tri numeric(28,0);
