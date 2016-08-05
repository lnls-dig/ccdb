--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: alignment_artifact; Type: TABLE; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE TABLE alignment_artifact (
    id bigint NOT NULL,
    modified_at timestamp without time zone NOT NULL,
    modified_by character varying(64) NOT NULL,
    version bigint,
    description character varying(255) NOT NULL,
    is_internal boolean NOT NULL,
    name character varying(128) NOT NULL,
    uri text NOT NULL,
    alignment_record bigint NOT NULL
);


ALTER TABLE public.alignment_artifact OWNER TO ccdb;

--
-- Name: alignment_property_value; Type: TABLE; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE TABLE alignment_property_value (
    id bigint NOT NULL,
    modified_at timestamp without time zone NOT NULL,
    modified_by character varying(64) NOT NULL,
    version bigint,
    in_repository boolean NOT NULL,
    prop_value text,
    property bigint NOT NULL,
    unit bigint,
    alignment_record bigint NOT NULL
);


ALTER TABLE public.alignment_property_value OWNER TO ccdb;

--
-- Name: alignment_record; Type: TABLE; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE TABLE alignment_record (
    id bigint NOT NULL,
    modified_at timestamp without time zone NOT NULL,
    modified_by character varying(64) NOT NULL,
    version bigint,
    alignment_date timestamp without time zone NOT NULL,
    global_pitch double precision,
    global_roll double precision,
    global_x double precision,
    global_y double precision,
    global_yaw double precision,
    global_z double precision,
    record_number character varying(64) NOT NULL,
    device bigint NOT NULL,
    slot bigint NOT NULL
);


ALTER TABLE public.alignment_record OWNER TO ccdb;

--
-- Name: alignment_tag; Type: TABLE; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE TABLE alignment_tag (
    alignment_id bigint NOT NULL,
    tag_id character varying(255) NOT NULL
);


ALTER TABLE public.alignment_tag OWNER TO ccdb;

--
-- Name: audit_record; Type: TABLE; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE TABLE audit_record (
    id bigint NOT NULL,
    entity_id bigint,
    entity_key character varying(255),
    entity_type character varying(255),
    entry text NOT NULL,
    log_time timestamp without time zone NOT NULL,
    oper character varying(255) NOT NULL,
    ccdb_user character varying(64) NOT NULL,
    version bigint
);


ALTER TABLE public.audit_record OWNER TO ccdb;

--
-- Name: ccdb_user; Type: TABLE; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE TABLE ccdb_user (
    user_id character varying(64) NOT NULL,
    comment character varying(255),
    email character varying(64),
    name character varying(128) NOT NULL,
    version bigint
);


ALTER TABLE public.ccdb_user OWNER TO ccdb;

--
-- Name: component_type; Type: TABLE; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE TABLE component_type (
    id bigint NOT NULL,
    modified_at timestamp without time zone NOT NULL,
    modified_by character varying(64) NOT NULL,
    version bigint,
    description character varying(255),
    name character varying(32) NOT NULL,
    super_component_type bigint
);


ALTER TABLE public.component_type OWNER TO ccdb;

--
-- Name: comptype_artifact; Type: TABLE; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE TABLE comptype_artifact (
    id bigint NOT NULL,
    modified_at timestamp without time zone NOT NULL,
    modified_by character varying(64) NOT NULL,
    version bigint,
    description character varying(255) NOT NULL,
    is_internal boolean NOT NULL,
    name character varying(128) NOT NULL,
    uri text NOT NULL,
    component_type bigint NOT NULL
);


ALTER TABLE public.comptype_artifact OWNER TO ccdb;

--
-- Name: comptype_asm; Type: TABLE; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE TABLE comptype_asm (
    id bigint NOT NULL,
    modified_at timestamp without time zone NOT NULL,
    modified_by character varying(64) NOT NULL,
    version bigint,
    child_position character varying(16) NOT NULL,
    description character varying(255),
    child_type bigint NOT NULL,
    parent_type bigint NOT NULL
);


ALTER TABLE public.comptype_asm OWNER TO ccdb;

--
-- Name: comptype_authorization; Type: TABLE; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE TABLE comptype_authorization (
    comptype_id bigint NOT NULL,
    auth_data_id bigint NOT NULL
);


ALTER TABLE public.comptype_authorization OWNER TO ccdb;

--
-- Name: comptype_property_value; Type: TABLE; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE TABLE comptype_property_value (
    id bigint NOT NULL,
    modified_at timestamp without time zone NOT NULL,
    modified_by character varying(64) NOT NULL,
    version bigint,
    in_repository boolean NOT NULL,
    prop_value text,
    property bigint NOT NULL,
    unit bigint,
    is_def_target_device boolean NOT NULL,
    is_def_target_slot boolean NOT NULL,
    is_property_definition boolean NOT NULL,
    component_type bigint NOT NULL
);


ALTER TABLE public.comptype_property_value OWNER TO ccdb;

--
-- Name: comptype_tag; Type: TABLE; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE TABLE comptype_tag (
    comptype_id bigint NOT NULL,
    tag_id character varying(255) NOT NULL
);


ALTER TABLE public.comptype_tag OWNER TO ccdb;

--
-- Name: comptypepropval_authorization; Type: TABLE; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE TABLE comptypepropval_authorization (
    propval_id bigint NOT NULL,
    auth_data_id bigint NOT NULL
);


ALTER TABLE public.comptypepropval_authorization OWNER TO ccdb;

--
-- Name: config; Type: TABLE; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE TABLE config (
    config_id integer NOT NULL,
    name character varying(64) NOT NULL,
    prop_value character varying(128),
    version bigint
);


ALTER TABLE public.config OWNER TO ccdb;

--
-- Name: data_type; Type: TABLE; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE TABLE data_type (
    id bigint NOT NULL,
    modified_at timestamp without time zone NOT NULL,
    modified_by character varying(64) NOT NULL,
    version bigint,
    definition text,
    description character varying(255) NOT NULL,
    name character varying(64) NOT NULL,
    scalar boolean NOT NULL
);


ALTER TABLE public.data_type OWNER TO ccdb;

--
-- Name: device; Type: TABLE; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE TABLE device (
    id bigint NOT NULL,
    modified_at timestamp without time zone NOT NULL,
    modified_by character varying(64) NOT NULL,
    version bigint,
    asm_description character varying(255),
    asm_position character varying(16),
    serial_number character varying(64) NOT NULL,
    asm_parent bigint,
    component_type bigint NOT NULL
);


ALTER TABLE public.device OWNER TO ccdb;

--
-- Name: device_artifact; Type: TABLE; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE TABLE device_artifact (
    id bigint NOT NULL,
    modified_at timestamp without time zone NOT NULL,
    modified_by character varying(64) NOT NULL,
    version bigint,
    description character varying(255) NOT NULL,
    is_internal boolean NOT NULL,
    name character varying(128) NOT NULL,
    uri text NOT NULL,
    device bigint NOT NULL
);


ALTER TABLE public.device_artifact OWNER TO ccdb;

--
-- Name: device_authorization; Type: TABLE; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE TABLE device_authorization (
    device_id bigint NOT NULL,
    auth_data_id bigint NOT NULL
);


ALTER TABLE public.device_authorization OWNER TO ccdb;

--
-- Name: device_property_value; Type: TABLE; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE TABLE device_property_value (
    id bigint NOT NULL,
    modified_at timestamp without time zone NOT NULL,
    modified_by character varying(64) NOT NULL,
    version bigint,
    in_repository boolean NOT NULL,
    prop_value text,
    property bigint NOT NULL,
    unit bigint,
    device bigint NOT NULL
);


ALTER TABLE public.device_property_value OWNER TO ccdb;

--
-- Name: device_tag; Type: TABLE; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE TABLE device_tag (
    device_id bigint NOT NULL,
    tag_id character varying(255) NOT NULL
);


ALTER TABLE public.device_tag OWNER TO ccdb;

--
-- Name: entity_auth; Type: TABLE; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE TABLE entity_auth (
    id bigint NOT NULL,
    modified_at timestamp without time zone NOT NULL,
    modified_by character varying(64) NOT NULL,
    version bigint,
    is_permission boolean NOT NULL,
    user_permission character varying(64) NOT NULL
);


ALTER TABLE public.entity_auth OWNER TO ccdb;

--
-- Name: filter_by_type; Type: TABLE; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE TABLE filter_by_type (
    report_id bigint NOT NULL,
    type_id bigint NOT NULL
);


ALTER TABLE public.filter_by_type OWNER TO ccdb;

--
-- Name: hibernate_sequence; Type: SEQUENCE; Schema: public; Owner: ccdb
--

CREATE SEQUENCE hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.hibernate_sequence OWNER TO ccdb;

--
-- Name: installation_artifact; Type: TABLE; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE TABLE installation_artifact (
    id bigint NOT NULL,
    modified_at timestamp without time zone NOT NULL,
    modified_by character varying(64) NOT NULL,
    version bigint,
    description character varying(255) NOT NULL,
    is_internal boolean NOT NULL,
    name character varying(128) NOT NULL,
    uri text NOT NULL,
    installation_record bigint NOT NULL
);


ALTER TABLE public.installation_artifact OWNER TO ccdb;

--
-- Name: installation_record; Type: TABLE; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE TABLE installation_record (
    id bigint NOT NULL,
    modified_at timestamp without time zone NOT NULL,
    modified_by character varying(64) NOT NULL,
    version bigint,
    install_date timestamp without time zone NOT NULL,
    notes text,
    record_number character varying(64) NOT NULL,
    uninstall_date timestamp without time zone,
    device bigint NOT NULL,
    slot bigint NOT NULL
);


ALTER TABLE public.installation_record OWNER TO ccdb;

--
-- Name: privilege; Type: TABLE; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE TABLE privilege (
    privilege_id integer NOT NULL,
    oper character varying(255) NOT NULL,
    resource character varying(255) NOT NULL,
    role character varying(64) NOT NULL
);


ALTER TABLE public.privilege OWNER TO ccdb;

--
-- Name: property; Type: TABLE; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE TABLE property (
    id bigint NOT NULL,
    modified_at timestamp without time zone NOT NULL,
    modified_by character varying(64) NOT NULL,
    version bigint,
    description character varying(255) NOT NULL,
    name character varying(64) NOT NULL,
    value_unique character varying(255) NOT NULL,
    data_type bigint NOT NULL,
    unit bigint
);


ALTER TABLE public.property OWNER TO ccdb;

--
-- Name: report; Type: TABLE; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE TABLE report (
    id bigint NOT NULL,
    modified_at timestamp without time zone NOT NULL,
    modified_by character varying(64) NOT NULL,
    version bigint,
    name character varying(255) NOT NULL
);


ALTER TABLE public.report OWNER TO ccdb;

--
-- Name: report_action; Type: TABLE; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE TABLE report_action (
    id bigint NOT NULL,
    modified_at timestamp without time zone NOT NULL,
    modified_by character varying(64) NOT NULL,
    version bigint,
    field character varying(255),
    operation character varying(255) NOT NULL,
    value character varying(255),
    report_id bigint,
    property_id bigint,
    tag_id character varying(255)
);


ALTER TABLE public.report_action OWNER TO ccdb;

--
-- Name: role; Type: TABLE; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE TABLE role (
    role_id character varying(64) NOT NULL,
    description character varying(255) NOT NULL,
    version bigint
);


ALTER TABLE public.role OWNER TO ccdb;

--
-- Name: slot; Type: TABLE; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE TABLE slot (
    id bigint NOT NULL,
    modified_at timestamp without time zone NOT NULL,
    modified_by character varying(64) NOT NULL,
    version bigint,
    asm_comment character varying(255),
    asm_position character varying(16),
    comment character varying(255),
    description character varying(255),
    is_hosting_slot boolean NOT NULL,
    name character varying(128) NOT NULL,
    asm_slot bigint,
    component_type bigint NOT NULL
);


ALTER TABLE public.slot OWNER TO ccdb;

--
-- Name: slot_artifact; Type: TABLE; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE TABLE slot_artifact (
    id bigint NOT NULL,
    modified_at timestamp without time zone NOT NULL,
    modified_by character varying(64) NOT NULL,
    version bigint,
    description character varying(255) NOT NULL,
    is_internal boolean NOT NULL,
    name character varying(128) NOT NULL,
    uri text NOT NULL,
    slot bigint NOT NULL
);


ALTER TABLE public.slot_artifact OWNER TO ccdb;

--
-- Name: slot_authorization; Type: TABLE; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE TABLE slot_authorization (
    slot_id bigint NOT NULL,
    auth_data_id bigint NOT NULL
);


ALTER TABLE public.slot_authorization OWNER TO ccdb;

--
-- Name: slot_pair; Type: TABLE; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE TABLE slot_pair (
    id bigint NOT NULL,
    slot_order integer NOT NULL,
    version bigint,
    child_slot bigint NOT NULL,
    parent_slot bigint NOT NULL,
    slot_relation bigint NOT NULL
);


ALTER TABLE public.slot_pair OWNER TO ccdb;

--
-- Name: slot_property_value; Type: TABLE; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE TABLE slot_property_value (
    id bigint NOT NULL,
    modified_at timestamp without time zone NOT NULL,
    modified_by character varying(64) NOT NULL,
    version bigint,
    in_repository boolean NOT NULL,
    prop_value text,
    property bigint NOT NULL,
    unit bigint,
    slot bigint NOT NULL
);


ALTER TABLE public.slot_property_value OWNER TO ccdb;

--
-- Name: slot_relation; Type: TABLE; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE TABLE slot_relation (
    id bigint NOT NULL,
    modified_at timestamp without time zone NOT NULL,
    modified_by character varying(64) NOT NULL,
    version bigint,
    description character varying(255),
    iname character varying(255) NOT NULL,
    name character varying(255) NOT NULL
);


ALTER TABLE public.slot_relation OWNER TO ccdb;

--
-- Name: slot_tag; Type: TABLE; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE TABLE slot_tag (
    slot_id bigint NOT NULL,
    tag_id character varying(255) NOT NULL
);


ALTER TABLE public.slot_tag OWNER TO ccdb;

--
-- Name: tag; Type: TABLE; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE TABLE tag (
    name character varying(255) NOT NULL,
    version bigint
);


ALTER TABLE public.tag OWNER TO ccdb;

--
-- Name: unit; Type: TABLE; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE TABLE unit (
    id bigint NOT NULL,
    modified_at timestamp without time zone NOT NULL,
    modified_by character varying(64) NOT NULL,
    version bigint,
    description character varying(255) NOT NULL,
    unit_name character varying(32) NOT NULL,
    symbol character varying(128) NOT NULL
);


ALTER TABLE public.unit OWNER TO ccdb;

--
-- Name: user_role; Type: TABLE; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE TABLE user_role (
    user_role_id integer NOT NULL,
    candelegate boolean NOT NULL,
    comment character varying(255),
    endtime timestamp without time zone NOT NULL,
    isrolemanager boolean NOT NULL,
    starttime timestamp without time zone NOT NULL,
    version bigint,
    role character varying(64) NOT NULL,
    ccdb_user character varying(64) NOT NULL
);


ALTER TABLE public.user_role OWNER TO ccdb;

--
-- Data for Name: alignment_artifact; Type: TABLE DATA; Schema: public; Owner: ccdb
--

COPY alignment_artifact (id, modified_at, modified_by, version, description, is_internal, name, uri, alignment_record) FROM stdin;
\.


--
-- Data for Name: alignment_property_value; Type: TABLE DATA; Schema: public; Owner: ccdb
--

COPY alignment_property_value (id, modified_at, modified_by, version, in_repository, prop_value, property, unit, alignment_record) FROM stdin;
\.


--
-- Data for Name: alignment_record; Type: TABLE DATA; Schema: public; Owner: ccdb
--

COPY alignment_record (id, modified_at, modified_by, version, alignment_date, global_pitch, global_roll, global_x, global_y, global_yaw, global_z, record_number, device, slot) FROM stdin;
\.


--
-- Data for Name: alignment_tag; Type: TABLE DATA; Schema: public; Owner: ccdb
--

COPY alignment_tag (alignment_id, tag_id) FROM stdin;
\.


--
-- Data for Name: audit_record; Type: TABLE DATA; Schema: public; Owner: ccdb
--

COPY audit_record (id, entity_id, entity_key, entity_type, entry, log_time, oper, ccdb_user, version) FROM stdin;
\.


--
-- Data for Name: ccdb_user; Type: TABLE DATA; Schema: public; Owner: ccdb
--

COPY ccdb_user (user_id, comment, email, name, version) FROM stdin;
admin	\N	\N	admin	0
\.


--
-- Data for Name: component_type; Type: TABLE DATA; Schema: public; Owner: ccdb
--

COPY component_type (id, modified_at, modified_by, version, description, name, super_component_type) FROM stdin;
45	2016-07-13 15:03:58.929	system	0	_ROOT	_ROOT	\N
46	2016-07-13 15:03:58.929	system	0	_GRP	_GRP	\N
\.


--
-- Data for Name: comptype_artifact; Type: TABLE DATA; Schema: public; Owner: ccdb
--

COPY comptype_artifact (id, modified_at, modified_by, version, description, is_internal, name, uri, component_type) FROM stdin;
\.


--
-- Data for Name: comptype_asm; Type: TABLE DATA; Schema: public; Owner: ccdb
--

COPY comptype_asm (id, modified_at, modified_by, version, child_position, description, child_type, parent_type) FROM stdin;
\.


--
-- Data for Name: comptype_authorization; Type: TABLE DATA; Schema: public; Owner: ccdb
--

COPY comptype_authorization (comptype_id, auth_data_id) FROM stdin;
\.


--
-- Data for Name: comptype_property_value; Type: TABLE DATA; Schema: public; Owner: ccdb
--

COPY comptype_property_value (id, modified_at, modified_by, version, in_repository, prop_value, property, unit, is_def_target_device, is_def_target_slot, is_property_definition, component_type) FROM stdin;
\.


--
-- Data for Name: comptype_tag; Type: TABLE DATA; Schema: public; Owner: ccdb
--

COPY comptype_tag (comptype_id, tag_id) FROM stdin;
\.


--
-- Data for Name: comptypepropval_authorization; Type: TABLE DATA; Schema: public; Owner: ccdb
--

COPY comptypepropval_authorization (propval_id, auth_data_id) FROM stdin;
\.


--
-- Data for Name: data_type; Type: TABLE DATA; Schema: public; Owner: ccdb
--

COPY data_type (id, modified_at, modified_by, version, definition, description, name, scalar) FROM stdin;
34	2016-07-13 15:03:58.911	system	0	\N	Integer number	Integer	t
35	2016-07-13 15:03:58.912	system	0	\N	Double precision floating point	Double	t
36	2016-07-13 15:03:58.913	system	0	\N	String of characters (text)	String	t
37	2016-07-13 15:03:58.913	system	0	\N	Date and time	Timestamp	t
38	2016-07-13 15:03:58.914	system	0	\N	Vector of integer numbers (1D array)	Integers Vector	f
39	2016-07-13 15:03:58.914	system	0	\N	Vector of double precision numbers (1D array)	Doubles Vector	f
40	2016-07-13 15:03:58.916	system	0	\N	List of strings (1D array)	Strings List	f
41	2016-07-13 15:03:58.917	system	0	\N	Table of double precision numbers (2D array)	Doubles Table	f
42	2016-07-13 15:03:58.918	system	0	{"meta":{"type":"SedsEnum","protocol":"SEDSv1","version":"1.0.0"},"data":{"selected":"FALSE"},"type":{"elements":["FALSE","TRUE"]}}	Boolean data type	Boolean	t
\.


--
-- Data for Name: device; Type: TABLE DATA; Schema: public; Owner: ccdb
--

COPY device (id, modified_at, modified_by, version, asm_description, asm_position, serial_number, asm_parent, component_type) FROM stdin;
\.


--
-- Data for Name: device_artifact; Type: TABLE DATA; Schema: public; Owner: ccdb
--

COPY device_artifact (id, modified_at, modified_by, version, description, is_internal, name, uri, device) FROM stdin;
\.


--
-- Data for Name: device_authorization; Type: TABLE DATA; Schema: public; Owner: ccdb
--

COPY device_authorization (device_id, auth_data_id) FROM stdin;
\.


--
-- Data for Name: device_property_value; Type: TABLE DATA; Schema: public; Owner: ccdb
--

COPY device_property_value (id, modified_at, modified_by, version, in_repository, prop_value, property, unit, device) FROM stdin;
\.


--
-- Data for Name: device_tag; Type: TABLE DATA; Schema: public; Owner: ccdb
--

COPY device_tag (device_id, tag_id) FROM stdin;
\.


--
-- Data for Name: entity_auth; Type: TABLE DATA; Schema: public; Owner: ccdb
--

COPY entity_auth (id, modified_at, modified_by, version, is_permission, user_permission) FROM stdin;
\.


--
-- Data for Name: filter_by_type; Type: TABLE DATA; Schema: public; Owner: ccdb
--

COPY filter_by_type (report_id, type_id) FROM stdin;
\.


--
-- Name: hibernate_sequence; Type: SEQUENCE SET; Schema: public; Owner: ccdb
--

SELECT pg_catalog.setval('hibernate_sequence', 48, true);


--
-- Data for Name: installation_artifact; Type: TABLE DATA; Schema: public; Owner: ccdb
--

COPY installation_artifact (id, modified_at, modified_by, version, description, is_internal, name, uri, installation_record) FROM stdin;
\.


--
-- Data for Name: installation_record; Type: TABLE DATA; Schema: public; Owner: ccdb
--

COPY installation_record (id, modified_at, modified_by, version, install_date, notes, record_number, uninstall_date, device, slot) FROM stdin;
\.


--
-- Data for Name: privilege; Type: TABLE DATA; Schema: public; Owner: ccdb
--

COPY privilege (privilege_id, oper, resource, role) FROM stdin;
2	CREATE	COMPONENT_TYPE	admin
3	UPDATE	COMPONENT_TYPE	admin
4	RENAME	COMPONENT_TYPE	admin
5	DELETE	COMPONENT_TYPE	admin
6	CREATE	UNIT	admin
7	UPDATE	UNIT	admin
8	RENAME	UNIT	admin
9	DELETE	UNIT	admin
10	CREATE	PROPERTY	admin
11	UPDATE	PROPERTY	admin
12	RENAME	PROPERTY	admin
13	DELETE	PROPERTY	admin
14	CREATE	SLOT	admin
15	UPDATE	SLOT	admin
16	RENAME	SLOT	admin
17	DELETE	SLOT	admin
18	CREATE	DEVICE	admin
19	UPDATE	DEVICE	admin
20	RENAME	DEVICE	admin
21	DELETE	DEVICE	admin
22	CREATE	ALIGNMENT_RECORD	admin
23	UPDATE	ALIGNMENT_RECORD	admin
24	RENAME	ALIGNMENT_RECORD	admin
25	DELETE	ALIGNMENT_RECORD	admin
26	CREATE	DATA_TYPE	admin
27	UPDATE	DATA_TYPE	admin
28	RENAME	DATA_TYPE	admin
29	DELETE	DATA_TYPE	admin
30	CREATE	INSTALLATION_RECORD	admin
31	UPDATE	INSTALLATION_RECORD	admin
32	RENAME	INSTALLATION_RECORD	admin
33	DELETE	INSTALLATION_RECORD	admin
\.


--
-- Data for Name: property; Type: TABLE DATA; Schema: public; Owner: ccdb
--

COPY property (id, modified_at, modified_by, version, description, name, value_unique, data_type, unit) FROM stdin;
\.


--
-- Data for Name: report; Type: TABLE DATA; Schema: public; Owner: ccdb
--

COPY report (id, modified_at, modified_by, version, name) FROM stdin;
\.


--
-- Data for Name: report_action; Type: TABLE DATA; Schema: public; Owner: ccdb
--

COPY report_action (id, modified_at, modified_by, version, field, operation, value, report_id, property_id, tag_id) FROM stdin;
\.


--
-- Data for Name: role; Type: TABLE DATA; Schema: public; Owner: ccdb
--

COPY role (role_id, description, version) FROM stdin;
admin	test role	0
\.


--
-- Data for Name: slot; Type: TABLE DATA; Schema: public; Owner: ccdb
--

COPY slot (id, modified_at, modified_by, version, asm_comment, asm_position, comment, description, is_hosting_slot, name, asm_slot, component_type) FROM stdin;
47	2016-07-13 15:03:58.93	system	0	\N	\N	\N	Implicit CCDB type.	f	_ROOT	\N	45
\.


--
-- Data for Name: slot_artifact; Type: TABLE DATA; Schema: public; Owner: ccdb
--

COPY slot_artifact (id, modified_at, modified_by, version, description, is_internal, name, uri, slot) FROM stdin;
\.


--
-- Data for Name: slot_authorization; Type: TABLE DATA; Schema: public; Owner: ccdb
--

COPY slot_authorization (slot_id, auth_data_id) FROM stdin;
\.


--
-- Data for Name: slot_pair; Type: TABLE DATA; Schema: public; Owner: ccdb
--

COPY slot_pair (id, slot_order, version, child_slot, parent_slot, slot_relation) FROM stdin;
\.


--
-- Data for Name: slot_property_value; Type: TABLE DATA; Schema: public; Owner: ccdb
--

COPY slot_property_value (id, modified_at, modified_by, version, in_repository, prop_value, property, unit, slot) FROM stdin;
\.


--
-- Data for Name: slot_relation; Type: TABLE DATA; Schema: public; Owner: ccdb
--

COPY slot_relation (id, modified_at, modified_by, version, description, iname, name) FROM stdin;
42	2016-07-13 15:03:58.919	system	0	\N	Contained in	CONTAINS
43	2016-07-13 15:03:58.921	system	0	\N	Powered by	POWERS
44	2016-07-13 15:03:58.928	system	0	\N	Controlled by	CONTROLS
\.


--
-- Data for Name: slot_tag; Type: TABLE DATA; Schema: public; Owner: ccdb
--

COPY slot_tag (slot_id, tag_id) FROM stdin;
\.


--
-- Data for Name: tag; Type: TABLE DATA; Schema: public; Owner: ccdb
--

COPY tag (name, version) FROM stdin;
\.


--
-- Data for Name: unit; Type: TABLE DATA; Schema: public; Owner: ccdb
--

COPY unit (id, modified_at, modified_by, version, description, unit_name, symbol) FROM stdin;
\.


--
-- Data for Name: user_role; Type: TABLE DATA; Schema: public; Owner: ccdb
--

COPY user_role (user_role_id, candelegate, comment, endtime, isrolemanager, starttime, version, role, ccdb_user) FROM stdin;
1	t	\N	2016-07-13 15:03:58.829	t	2016-07-13 15:03:58.829	0	admin	admin
\.


--
-- Name: alignment_artifact_pkey; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY alignment_artifact
    ADD CONSTRAINT alignment_artifact_pkey PRIMARY KEY (id);


--
-- Name: alignment_property_value_pkey; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY alignment_property_value
    ADD CONSTRAINT alignment_property_value_pkey PRIMARY KEY (id);


--
-- Name: alignment_record_pkey; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY alignment_record
    ADD CONSTRAINT alignment_record_pkey PRIMARY KEY (id);


--
-- Name: alignment_tag_pkey; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY alignment_tag
    ADD CONSTRAINT alignment_tag_pkey PRIMARY KEY (alignment_id, tag_id);


--
-- Name: audit_record_pkey; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY audit_record
    ADD CONSTRAINT audit_record_pkey PRIMARY KEY (id);


--
-- Name: ccdb_user_pkey; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY ccdb_user
    ADD CONSTRAINT ccdb_user_pkey PRIMARY KEY (user_id);


--
-- Name: component_type_pkey; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY component_type
    ADD CONSTRAINT component_type_pkey PRIMARY KEY (id);


--
-- Name: comptype_artifact_pkey; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY comptype_artifact
    ADD CONSTRAINT comptype_artifact_pkey PRIMARY KEY (id);


--
-- Name: comptype_asm_pkey; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY comptype_asm
    ADD CONSTRAINT comptype_asm_pkey PRIMARY KEY (id);


--
-- Name: comptype_property_value_pkey; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY comptype_property_value
    ADD CONSTRAINT comptype_property_value_pkey PRIMARY KEY (id);


--
-- Name: comptype_tag_pkey; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY comptype_tag
    ADD CONSTRAINT comptype_tag_pkey PRIMARY KEY (comptype_id, tag_id);


--
-- Name: config_pkey; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY config
    ADD CONSTRAINT config_pkey PRIMARY KEY (config_id);


--
-- Name: data_type_pkey; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY data_type
    ADD CONSTRAINT data_type_pkey PRIMARY KEY (id);


--
-- Name: device_artifact_pkey; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY device_artifact
    ADD CONSTRAINT device_artifact_pkey PRIMARY KEY (id);


--
-- Name: device_pkey; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY device
    ADD CONSTRAINT device_pkey PRIMARY KEY (id);


--
-- Name: device_property_value_pkey; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY device_property_value
    ADD CONSTRAINT device_property_value_pkey PRIMARY KEY (id);


--
-- Name: device_tag_pkey; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY device_tag
    ADD CONSTRAINT device_tag_pkey PRIMARY KEY (device_id, tag_id);


--
-- Name: entity_auth_pkey; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY entity_auth
    ADD CONSTRAINT entity_auth_pkey PRIMARY KEY (id);


--
-- Name: installation_artifact_pkey; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY installation_artifact
    ADD CONSTRAINT installation_artifact_pkey PRIMARY KEY (id);


--
-- Name: installation_record_pkey; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY installation_record
    ADD CONSTRAINT installation_record_pkey PRIMARY KEY (id);


--
-- Name: privilege_pkey; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY privilege
    ADD CONSTRAINT privilege_pkey PRIMARY KEY (privilege_id);


--
-- Name: property_pkey; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY property
    ADD CONSTRAINT property_pkey PRIMARY KEY (id);


--
-- Name: report_action_pkey; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY report_action
    ADD CONSTRAINT report_action_pkey PRIMARY KEY (id);


--
-- Name: report_pkey; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY report
    ADD CONSTRAINT report_pkey PRIMARY KEY (id);


--
-- Name: role_pkey; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY role
    ADD CONSTRAINT role_pkey PRIMARY KEY (role_id);


--
-- Name: slot_artifact_pkey; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY slot_artifact
    ADD CONSTRAINT slot_artifact_pkey PRIMARY KEY (id);


--
-- Name: slot_pair_pkey; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY slot_pair
    ADD CONSTRAINT slot_pair_pkey PRIMARY KEY (id);


--
-- Name: slot_pkey; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY slot
    ADD CONSTRAINT slot_pkey PRIMARY KEY (id);


--
-- Name: slot_property_value_pkey; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY slot_property_value
    ADD CONSTRAINT slot_property_value_pkey PRIMARY KEY (id);


--
-- Name: slot_relation_pkey; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY slot_relation
    ADD CONSTRAINT slot_relation_pkey PRIMARY KEY (id);


--
-- Name: slot_tag_pkey; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY slot_tag
    ADD CONSTRAINT slot_tag_pkey PRIMARY KEY (slot_id, tag_id);


--
-- Name: tag_pkey; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY tag
    ADD CONSTRAINT tag_pkey PRIMARY KEY (name);


--
-- Name: uk_1fija2llyxxpw7tse5qhkdyc0; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY entity_auth
    ADD CONSTRAINT uk_1fija2llyxxpw7tse5qhkdyc0 UNIQUE (user_permission);


--
-- Name: uk_4776vaiywo1kdis4lp8jkm0av; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY device
    ADD CONSTRAINT uk_4776vaiywo1kdis4lp8jkm0av UNIQUE (serial_number);


--
-- Name: uk_95873m4oi7rlt8v20mrtn7t4n; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY component_type
    ADD CONSTRAINT uk_95873m4oi7rlt8v20mrtn7t4n UNIQUE (name);


--
-- Name: uk_awsyx46evbfvothecqq9fovn0; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY slot_pair
    ADD CONSTRAINT uk_awsyx46evbfvothecqq9fovn0 UNIQUE (parent_slot, child_slot, slot_relation);


--
-- Name: uk_bkdt0hph1p112dq2qtomohf1c; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY slot_pair
    ADD CONSTRAINT uk_bkdt0hph1p112dq2qtomohf1c UNIQUE (parent_slot, slot_relation, slot_order);


--
-- Name: uk_da8eog93rhwgf6726q1nftsr6; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY unit
    ADD CONSTRAINT uk_da8eog93rhwgf6726q1nftsr6 UNIQUE (unit_name);


--
-- Name: uk_e91n7aknn29sf1duor91i0vd6; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY report
    ADD CONSTRAINT uk_e91n7aknn29sf1duor91i0vd6 UNIQUE (name);


--
-- Name: uk_eq4a9bnxmofe9qo9im31up0mn; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY data_type
    ADD CONSTRAINT uk_eq4a9bnxmofe9qo9im31up0mn UNIQUE (name);


--
-- Name: uk_hgo2avysvdf8312u6ivgyc1lp; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY property
    ADD CONSTRAINT uk_hgo2avysvdf8312u6ivgyc1lp UNIQUE (name);


--
-- Name: unit_pkey; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY unit
    ADD CONSTRAINT unit_pkey PRIMARY KEY (id);


--
-- Name: user_role_pkey; Type: CONSTRAINT; Schema: public; Owner: ccdb; Tablespace: 
--

ALTER TABLE ONLY user_role
    ADD CONSTRAINT user_role_pkey PRIMARY KEY (user_role_id);


--
-- Name: uk_3vvk53wjgpo717h7t7dgrmg7d; Type: INDEX; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE INDEX uk_3vvk53wjgpo717h7t7dgrmg7d ON entity_auth USING btree (is_permission);


--
-- Name: uk_4k4glv89c6wio09ng0t6nmmx2; Type: INDEX; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE INDEX uk_4k4glv89c6wio09ng0t6nmmx2 ON installation_record USING btree (slot);


--
-- Name: uk_5ok83skpwgyumh1d7xxjsyrsa; Type: INDEX; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE INDEX uk_5ok83skpwgyumh1d7xxjsyrsa ON component_type USING btree (super_component_type);


--
-- Name: uk_5qpcgclfenvdrmtdhuwrae4g7; Type: INDEX; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE INDEX uk_5qpcgclfenvdrmtdhuwrae4g7 ON audit_record USING btree (entity_id, entity_type);


--
-- Name: uk_6dipp9esg978y3gundtie7vuc; Type: INDEX; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE INDEX uk_6dipp9esg978y3gundtie7vuc ON alignment_record USING btree (slot);


--
-- Name: uk_6tyaontvjtqnjj9sntjfpl7jj; Type: INDEX; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE INDEX uk_6tyaontvjtqnjj9sntjfpl7jj ON slot_pair USING btree (parent_slot, slot_relation);


--
-- Name: uk_hy4o3880lg18aaxbugfawx2id; Type: INDEX; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE INDEX uk_hy4o3880lg18aaxbugfawx2id ON slot_relation USING btree (name);


--
-- Name: uk_k865m9x68cml2a2p66iakhpyw; Type: INDEX; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE INDEX uk_k865m9x68cml2a2p66iakhpyw ON device USING btree (component_type);


--
-- Name: uk_k9qpr93vr5mqx4mhmiud0ecvu; Type: INDEX; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE INDEX uk_k9qpr93vr5mqx4mhmiud0ecvu ON comptype_property_value USING btree (component_type, prop_value);


--
-- Name: uk_m0t3rax6nr55hkqmu42i41yq6; Type: INDEX; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE INDEX uk_m0t3rax6nr55hkqmu42i41yq6 ON alignment_record USING btree (device);


--
-- Name: uk_nkjnkn8s5uwtf09hxgkcdvyhi; Type: INDEX; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE INDEX uk_nkjnkn8s5uwtf09hxgkcdvyhi ON slot_pair USING btree (child_slot);


--
-- Name: uk_pe32328mbm50t13xbc2tuvr02; Type: INDEX; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE INDEX uk_pe32328mbm50t13xbc2tuvr02 ON installation_record USING btree (device, uninstall_date);


--
-- Name: uk_pqi2b7y0ksra6pk1tldbaarkl; Type: INDEX; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE INDEX uk_pqi2b7y0ksra6pk1tldbaarkl ON alignment_property_value USING btree (alignment_record);


--
-- Name: uk_qjr8s5le6hlofn7tv0j6h0aru; Type: INDEX; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE INDEX uk_qjr8s5le6hlofn7tv0j6h0aru ON slot USING btree (name);


--
-- Name: uk_qn4ml3i831lsg4qre6v5ib4os; Type: INDEX; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE INDEX uk_qn4ml3i831lsg4qre6v5ib4os ON slot USING btree (component_type);


--
-- Name: uk_r36nim7lddtnor46ba0680sma; Type: INDEX; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE INDEX uk_r36nim7lddtnor46ba0680sma ON alignment_artifact USING btree (alignment_record);


--
-- Name: uk_rwtq9vhs5cv8rp92ggmonukel; Type: INDEX; Schema: public; Owner: ccdb; Tablespace: 
--

CREATE INDEX uk_rwtq9vhs5cv8rp92ggmonukel ON slot USING btree (is_hosting_slot);


--
-- Name: fk_1gs21h4h9nllscumeydyeefv6; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY comptype_authorization
    ADD CONSTRAINT fk_1gs21h4h9nllscumeydyeefv6 FOREIGN KEY (comptype_id) REFERENCES component_type(id);


--
-- Name: fk_2l2o1tu19pxoxe9a1p3b7rypw; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY comptypepropval_authorization
    ADD CONSTRAINT fk_2l2o1tu19pxoxe9a1p3b7rypw FOREIGN KEY (propval_id) REFERENCES comptype_property_value(id);


--
-- Name: fk_4k4glv89c6wio09ng0t6nmmx2; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY installation_record
    ADD CONSTRAINT fk_4k4glv89c6wio09ng0t6nmmx2 FOREIGN KEY (slot) REFERENCES slot(id);


--
-- Name: fk_5ok83skpwgyumh1d7xxjsyrsa; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY component_type
    ADD CONSTRAINT fk_5ok83skpwgyumh1d7xxjsyrsa FOREIGN KEY (super_component_type) REFERENCES component_type(id);


--
-- Name: fk_6dipp9esg978y3gundtie7vuc; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY alignment_record
    ADD CONSTRAINT fk_6dipp9esg978y3gundtie7vuc FOREIGN KEY (slot) REFERENCES slot(id);


--
-- Name: fk_6j576ls9dy96dpwb4yahj5hxl; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY slot_tag
    ADD CONSTRAINT fk_6j576ls9dy96dpwb4yahj5hxl FOREIGN KEY (slot_id) REFERENCES slot(id);


--
-- Name: fk_7gjah9jd6tec2f3p4dqlb1opx; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY comptype_asm
    ADD CONSTRAINT fk_7gjah9jd6tec2f3p4dqlb1opx FOREIGN KEY (child_type) REFERENCES component_type(id);


--
-- Name: fk_7rfuon63lhbh98cgfitbkf7cg; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY filter_by_type
    ADD CONSTRAINT fk_7rfuon63lhbh98cgfitbkf7cg FOREIGN KEY (type_id) REFERENCES component_type(id);


--
-- Name: fk_8bp9u0xbrfh6awopst6tjset5; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY device_property_value
    ADD CONSTRAINT fk_8bp9u0xbrfh6awopst6tjset5 FOREIGN KEY (property) REFERENCES property(id);


--
-- Name: fk_8cll4r4m580myd98bc5uykhso; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY privilege
    ADD CONSTRAINT fk_8cll4r4m580myd98bc5uykhso FOREIGN KEY (role) REFERENCES role(role_id);


--
-- Name: fk_8hme5d1n4rkwvpqgkkla4c3ki; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY user_role
    ADD CONSTRAINT fk_8hme5d1n4rkwvpqgkkla4c3ki FOREIGN KEY (ccdb_user) REFERENCES ccdb_user(user_id);


--
-- Name: fk_8ohf00e7wiuyt4n9npa4t8eug; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY slot_tag
    ADD CONSTRAINT fk_8ohf00e7wiuyt4n9npa4t8eug FOREIGN KEY (tag_id) REFERENCES tag(name);


--
-- Name: fk_9mvcf4xugfmt0iy41lq8n7jws; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY comptype_asm
    ADD CONSTRAINT fk_9mvcf4xugfmt0iy41lq8n7jws FOREIGN KEY (parent_type) REFERENCES component_type(id);


--
-- Name: fk_a7l1yexk2ta452rim87xn8tjl; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY comptype_authorization
    ADD CONSTRAINT fk_a7l1yexk2ta452rim87xn8tjl FOREIGN KEY (auth_data_id) REFERENCES entity_auth(id);


--
-- Name: fk_aeufnum41x66u2ykx6fl4aav9; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY report_action
    ADD CONSTRAINT fk_aeufnum41x66u2ykx6fl4aav9 FOREIGN KEY (property_id) REFERENCES property(id);


--
-- Name: fk_bqet9wwypk8heehiu5erpeydd; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY installation_record
    ADD CONSTRAINT fk_bqet9wwypk8heehiu5erpeydd FOREIGN KEY (device) REFERENCES device(id);


--
-- Name: fk_bu1wsfikk9naddf2924sjhl6j; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY alignment_property_value
    ADD CONSTRAINT fk_bu1wsfikk9naddf2924sjhl6j FOREIGN KEY (property) REFERENCES property(id);


--
-- Name: fk_bxu0bmqt9dw88qdrl56vftwd6; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY device_artifact
    ADD CONSTRAINT fk_bxu0bmqt9dw88qdrl56vftwd6 FOREIGN KEY (device) REFERENCES device(id);


--
-- Name: fk_chk700yd4826xohsj0xjfyvgu; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY device_tag
    ADD CONSTRAINT fk_chk700yd4826xohsj0xjfyvgu FOREIGN KEY (tag_id) REFERENCES tag(name);


--
-- Name: fk_d6p256anv0co5w2qmuesg9pa9; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY property
    ADD CONSTRAINT fk_d6p256anv0co5w2qmuesg9pa9 FOREIGN KEY (unit) REFERENCES unit(id);


--
-- Name: fk_e5hc82in2ma9qj4a7h9ch5cuk; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY comptype_property_value
    ADD CONSTRAINT fk_e5hc82in2ma9qj4a7h9ch5cuk FOREIGN KEY (property) REFERENCES property(id);


--
-- Name: fk_exfaihy52wjrt34y4gefdlam2; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY device_property_value
    ADD CONSTRAINT fk_exfaihy52wjrt34y4gefdlam2 FOREIGN KEY (device) REFERENCES device(id);


--
-- Name: fk_fdh8momxf0nef53btyfahfl2p; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY slot_property_value
    ADD CONSTRAINT fk_fdh8momxf0nef53btyfahfl2p FOREIGN KEY (slot) REFERENCES slot(id);


--
-- Name: fk_fg2eclv374ruky39c7sl8vvp6; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY report_action
    ADD CONSTRAINT fk_fg2eclv374ruky39c7sl8vvp6 FOREIGN KEY (tag_id) REFERENCES tag(name);


--
-- Name: fk_fy6uvt4k9u9jmu5f0f8nmtsv1; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY comptype_artifact
    ADD CONSTRAINT fk_fy6uvt4k9u9jmu5f0f8nmtsv1 FOREIGN KEY (component_type) REFERENCES component_type(id);


--
-- Name: fk_g12bcxa5miyttl8x4y4bxv0xw; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY device_property_value
    ADD CONSTRAINT fk_g12bcxa5miyttl8x4y4bxv0xw FOREIGN KEY (unit) REFERENCES unit(id);


--
-- Name: fk_hbm0be5vdbkjnj03qj0vrigxo; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY comptype_tag
    ADD CONSTRAINT fk_hbm0be5vdbkjnj03qj0vrigxo FOREIGN KEY (tag_id) REFERENCES tag(name);


--
-- Name: fk_hfmkll1tkqqwx8kgqwifk0c28; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY slot_property_value
    ADD CONSTRAINT fk_hfmkll1tkqqwx8kgqwifk0c28 FOREIGN KEY (unit) REFERENCES unit(id);


--
-- Name: fk_hlbkvruhy9vmnd5uj4ss7rkgo; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY alignment_tag
    ADD CONSTRAINT fk_hlbkvruhy9vmnd5uj4ss7rkgo FOREIGN KEY (alignment_id) REFERENCES alignment_record(id);


--
-- Name: fk_ir3ybiaxqj5rbiwrb36el1q6t; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY report_action
    ADD CONSTRAINT fk_ir3ybiaxqj5rbiwrb36el1q6t FOREIGN KEY (report_id) REFERENCES report(id);


--
-- Name: fk_iu0kjdt88pdpd5hy6r8v8v5p2; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY device_authorization
    ADD CONSTRAINT fk_iu0kjdt88pdpd5hy6r8v8v5p2 FOREIGN KEY (auth_data_id) REFERENCES entity_auth(id);


--
-- Name: fk_jahs9k4q547rsylqgd9aesm97; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY slot_pair
    ADD CONSTRAINT fk_jahs9k4q547rsylqgd9aesm97 FOREIGN KEY (slot_relation) REFERENCES slot_relation(id);


--
-- Name: fk_jo6duyf3ww5inpht0176ohgbk; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY device
    ADD CONSTRAINT fk_jo6duyf3ww5inpht0176ohgbk FOREIGN KEY (asm_parent) REFERENCES device(id);


--
-- Name: fk_k84dpm8pdw85rd10181o0nidv; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY filter_by_type
    ADD CONSTRAINT fk_k84dpm8pdw85rd10181o0nidv FOREIGN KEY (report_id) REFERENCES report(id);


--
-- Name: fk_k865m9x68cml2a2p66iakhpyw; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY device
    ADD CONSTRAINT fk_k865m9x68cml2a2p66iakhpyw FOREIGN KEY (component_type) REFERENCES component_type(id);


--
-- Name: fk_ls4e0c8uinpboph4i1tg0tooq; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY slot
    ADD CONSTRAINT fk_ls4e0c8uinpboph4i1tg0tooq FOREIGN KEY (asm_slot) REFERENCES slot(id);


--
-- Name: fk_lxw2d51w9vodbewn6cgi2t1g5; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY comptype_tag
    ADD CONSTRAINT fk_lxw2d51w9vodbewn6cgi2t1g5 FOREIGN KEY (comptype_id) REFERENCES component_type(id);


--
-- Name: fk_m0t3rax6nr55hkqmu42i41yq6; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY alignment_record
    ADD CONSTRAINT fk_m0t3rax6nr55hkqmu42i41yq6 FOREIGN KEY (device) REFERENCES device(id);


--
-- Name: fk_mn8l71ogg3oov9b98gb8r1mo; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY slot_authorization
    ADD CONSTRAINT fk_mn8l71ogg3oov9b98gb8r1mo FOREIGN KEY (auth_data_id) REFERENCES entity_auth(id);


--
-- Name: fk_n320rerx0fbxbv6phpijlc26e; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY property
    ADD CONSTRAINT fk_n320rerx0fbxbv6phpijlc26e FOREIGN KEY (data_type) REFERENCES data_type(id);


--
-- Name: fk_nkjnkn8s5uwtf09hxgkcdvyhi; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY slot_pair
    ADD CONSTRAINT fk_nkjnkn8s5uwtf09hxgkcdvyhi FOREIGN KEY (child_slot) REFERENCES slot(id);


--
-- Name: fk_ox2n1wxy2sd2iq1v0x8cvuxxk; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY installation_artifact
    ADD CONSTRAINT fk_ox2n1wxy2sd2iq1v0x8cvuxxk FOREIGN KEY (installation_record) REFERENCES installation_record(id);


--
-- Name: fk_pqi2b7y0ksra6pk1tldbaarkl; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY alignment_property_value
    ADD CONSTRAINT fk_pqi2b7y0ksra6pk1tldbaarkl FOREIGN KEY (alignment_record) REFERENCES alignment_record(id);


--
-- Name: fk_psm9a7rpuv7oforsx18btrg4l; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY device_authorization
    ADD CONSTRAINT fk_psm9a7rpuv7oforsx18btrg4l FOREIGN KEY (device_id) REFERENCES device(id);


--
-- Name: fk_pu2p15w2ycrsptf48pd08lsvq; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY slot_property_value
    ADD CONSTRAINT fk_pu2p15w2ycrsptf48pd08lsvq FOREIGN KEY (property) REFERENCES property(id);


--
-- Name: fk_q0o8eoj8x4pb39x0a5n65a1x3; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY slot_authorization
    ADD CONSTRAINT fk_q0o8eoj8x4pb39x0a5n65a1x3 FOREIGN KEY (slot_id) REFERENCES slot(id);


--
-- Name: fk_qj054mhce64r40vt7vr75vfes; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY comptypepropval_authorization
    ADD CONSTRAINT fk_qj054mhce64r40vt7vr75vfes FOREIGN KEY (auth_data_id) REFERENCES entity_auth(id);


--
-- Name: fk_qn4ml3i831lsg4qre6v5ib4os; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY slot
    ADD CONSTRAINT fk_qn4ml3i831lsg4qre6v5ib4os FOREIGN KEY (component_type) REFERENCES component_type(id);


--
-- Name: fk_r0ev4rx6exbmtvjjjg28y1uu9; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY slot_artifact
    ADD CONSTRAINT fk_r0ev4rx6exbmtvjjjg28y1uu9 FOREIGN KEY (slot) REFERENCES slot(id);


--
-- Name: fk_r1flu04491leu8vxgldbx4ehm; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY comptype_property_value
    ADD CONSTRAINT fk_r1flu04491leu8vxgldbx4ehm FOREIGN KEY (component_type) REFERENCES component_type(id);


--
-- Name: fk_r36nim7lddtnor46ba0680sma; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY alignment_artifact
    ADD CONSTRAINT fk_r36nim7lddtnor46ba0680sma FOREIGN KEY (alignment_record) REFERENCES alignment_record(id);


--
-- Name: fk_rjio4rto1cnb945y9pm3cgj7t; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY alignment_tag
    ADD CONSTRAINT fk_rjio4rto1cnb945y9pm3cgj7t FOREIGN KEY (tag_id) REFERENCES tag(name);


--
-- Name: fk_rncjdouewwgbl9kkhngfs1onp; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY alignment_property_value
    ADD CONSTRAINT fk_rncjdouewwgbl9kkhngfs1onp FOREIGN KEY (unit) REFERENCES unit(id);


--
-- Name: fk_s21d8k5lywjjc7inw14brj6ro; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY user_role
    ADD CONSTRAINT fk_s21d8k5lywjjc7inw14brj6ro FOREIGN KEY (role) REFERENCES role(role_id);


--
-- Name: fk_s64fyojngxafr2tq38et50xbj; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY comptype_property_value
    ADD CONSTRAINT fk_s64fyojngxafr2tq38et50xbj FOREIGN KEY (unit) REFERENCES unit(id);


--
-- Name: fk_sjfj75hk02ws09s0ikmajl35g; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY device_tag
    ADD CONSTRAINT fk_sjfj75hk02ws09s0ikmajl35g FOREIGN KEY (device_id) REFERENCES device(id);


--
-- Name: fk_sn7fx5c9fmbms19adq4iyjrk5; Type: FK CONSTRAINT; Schema: public; Owner: ccdb
--

ALTER TABLE ONLY slot_pair
    ADD CONSTRAINT fk_sn7fx5c9fmbms19adq4iyjrk5 FOREIGN KEY (parent_slot) REFERENCES slot(id);

--
-- PostgreSQL database dump complete
--

