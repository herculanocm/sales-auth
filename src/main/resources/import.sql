-- This file allow to write SQL commands that will be emitted in test and dev.
-- The commands are commented as their support depends of the database
-- insert into myentity (id, field) values(1, 'field-1');
-- insert into myentity (id, field) values(2, 'field-2');
-- insert into myentity (id, field) values(3, 'field-3');
-- alter sequence myentity_seq restart with 4;



CREATE TABLE master.company_rule (
	cpr_pk_uuid uuid NOT NULL,
	cpr_tx_cgc varchar(20) NULL UNIQUE,
	cpr_dt_include timestamp(6) NULL,
	cpr_dt_update timestamp(6) NULL,
	cpr_lg_enabled bool NULL,
	cpr_tx_name varchar(255) NULL,
	cpr_tx_user_include varchar(255) NULL,
	cpr_tx_user_update varchar(255) NULL,
	CONSTRAINT company_rule_pkey PRIMARY KEY (cpr_pk_uuid)
);



CREATE TABLE master.group_user (
	gru_pk_uuid uuid NOT NULL,
	gru_tx_description varchar(3000) NULL,
	gru_dt_include timestamp(6) NULL,
	gru_dt_update timestamp(6) NULL,
	gru_lg_enabled bool NULL,
	gru_tx_name varchar(255) NULL,
	gru_tx_user_include varchar(255) NULL,
	gru_tx_user_update varchar(255) NULL,
	gru_fk_company_rule_uuid uuid NULL,
	CONSTRAINT group_user_pkey PRIMARY KEY (gru_pk_uuid)
);



ALTER TABLE master.group_user ADD CONSTRAINT group_user_company_fk FOREIGN KEY (gru_fk_company_rule_uuid) REFERENCES master.company_rule(cpr_pk_uuid);


 create table master.sales_user (
        usr_pk_uuid uuid not null,
        usr_lg_activated boolean,
        usr_tx_activation_key varchar(255),
        usr_dt_include timestamp(6) not null,
        usr_dt_update timestamp(6) not null,
        usr_tx_email varchar(255) not null,
        usr_lg_enabled boolean,
        usr_tx_first_name varchar(100) not null,
        usr_tx_image_url varchar(255),
        usr_tx_last_name varchar(200),
        usr_tx_password_hash varchar(255) not null,
        usr_tx_reset_key varchar(255),
        usr_tx_user_include varchar(255) not null,
        usr_tx_user_update varchar(255) not null,
        usr_fk_company_rule_uuid uuid,
        primary key (usr_pk_uuid)
    );

    create table master.sales_user_permission (
        urp_pk_permission varchar(255) not null,
        urp_pk_user_uuid uuid not null,
        primary key (urp_pk_permission, urp_pk_user_uuid)
    );

    create table master.sales_user_role (
        urr_pk_role varchar(255) not null,
        urr_pk_user_uuid uuid not null,
        primary key (urr_pk_role, urr_pk_user_uuid)
    );

    alter table if exists master.sales_user 
       add constraint sales_user_company_rule_fk 
       foreign key (usr_fk_company_rule_uuid) 
       references master.company_rule;

    alter table if exists master.sales_user_permission 
       add constraint sales_user_permission_user_fk 
       foreign key (urp_pk_user_uuid) 
	    references master.sales_user;

	alter table if exists master.sales_user_role
       add constraint sales_user_role_user_fk 
       foreign key (urr_pk_user_uuid) 
	    references master.sales_user;