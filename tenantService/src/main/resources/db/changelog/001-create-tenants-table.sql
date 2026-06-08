--liquibase formatted sql

--changeset soujanya09:001-create-tenants-table
CREATE TABLE IF NOT EXISTS tenants (
    tenant_id           VARCHAR(255)    NOT NULL,
    tenant_name         VARCHAR(255)    NOT NULL,
    tenant_email        VARCHAR(255)    NOT NULL,
    tenant_phone_number VARCHAR(255),
    tenant_address      VARCHAR(255),
    tenant_status       SMALLINT        NOT NULL,
    tenant_type         SMALLINT        NOT NULL,
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP,
    vacate_date         TIMESTAMP,
    CONSTRAINT tenants_pkey PRIMARY KEY (tenant_id),
    CONSTRAINT tenants_tenant_email_key UNIQUE (tenant_email),
    CONSTRAINT tenants_tenant_status_check CHECK (tenant_status >= 0 AND tenant_status <= 4),
    CONSTRAINT tenants_tenant_type_check CHECK (tenant_type >= 0 AND tenant_type <= 1)
);

--rollback DROP TABLE IF EXISTS tenants;
