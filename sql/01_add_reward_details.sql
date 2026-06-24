-- Migration for databases created before the FXML reward catalog was activated.

ALTER TABLE rewards_catalog
    ADD COLUMN IF NOT EXISTS description VARCHAR(500) NOT NULL DEFAULT '';

ALTER TABLE rewards_catalog
    ADD COLUMN IF NOT EXISTS available BOOLEAN NOT NULL DEFAULT TRUE;
