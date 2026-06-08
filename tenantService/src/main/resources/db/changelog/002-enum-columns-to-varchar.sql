--liquibase formatted sql

--changeset soujanya09:002-enum-columns-to-varchar
-- Migrate tenant_status and tenant_type from integer ordinals to varchar string values
-- Required after adding @Enumerated(EnumType.STRING) to the Tenant entity

ALTER TABLE tenants DROP CONSTRAINT tenants_tenant_status_check;
ALTER TABLE tenants DROP CONSTRAINT tenants_tenant_type_check;

ALTER TABLE tenants ADD COLUMN tenant_status_new VARCHAR(255);
ALTER TABLE tenants ADD COLUMN tenant_type_new VARCHAR(255);

UPDATE tenants SET tenant_status_new = CASE tenant_status
    WHEN 0 THEN 'VACATED'
    WHEN 1 THEN 'TO_BE_VACATED'
    WHEN 2 THEN 'RESERVED'
    WHEN 3 THEN 'TO_BE_RESERVED'
    WHEN 4 THEN 'PRESENT'
    ELSE NULL
END;

UPDATE tenants SET tenant_type_new = CASE tenant_type
    WHEN 0 THEN 'PERMANENT'
    WHEN 1 THEN 'TEMPORARY'
    ELSE NULL
END;

ALTER TABLE tenants DROP COLUMN tenant_status;
ALTER TABLE tenants DROP COLUMN tenant_type;

ALTER TABLE tenants RENAME COLUMN tenant_status_new TO tenant_status;
ALTER TABLE tenants RENAME COLUMN tenant_type_new TO tenant_type;

ALTER TABLE tenants ALTER COLUMN tenant_status SET NOT NULL;
ALTER TABLE tenants ALTER COLUMN tenant_type SET NOT NULL;

--rollback ALTER TABLE tenants ADD COLUMN tenant_status_old SMALLINT;
--rollback ALTER TABLE tenants ADD COLUMN tenant_type_old SMALLINT;
--rollback UPDATE tenants SET tenant_status_old = CASE tenant_status WHEN 'VACATED' THEN 0 WHEN 'TO_BE_VACATED' THEN 1 WHEN 'RESERVED' THEN 2 WHEN 'TO_BE_RESERVED' THEN 3 WHEN 'PRESENT' THEN 4 ELSE NULL END;
--rollback UPDATE tenants SET tenant_type_old = CASE tenant_type WHEN 'PERMANENT' THEN 0 WHEN 'TEMPORARY' THEN 1 ELSE NULL END;
--rollback ALTER TABLE tenants DROP COLUMN tenant_status;
--rollback ALTER TABLE tenants DROP COLUMN tenant_type;
--rollback ALTER TABLE tenants RENAME COLUMN tenant_status_old TO tenant_status;
--rollback ALTER TABLE tenants RENAME COLUMN tenant_type_old TO tenant_type;
--rollback ALTER TABLE tenants ADD CONSTRAINT tenants_tenant_status_check CHECK (tenant_status >= 0 AND tenant_status <= 4);
--rollback ALTER TABLE tenants ADD CONSTRAINT tenants_tenant_type_check CHECK (tenant_type >= 0 AND tenant_type <= 1);
