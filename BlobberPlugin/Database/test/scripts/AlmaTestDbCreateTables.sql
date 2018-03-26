drop table almatest.xml_metainfo PURGE;
drop table almatest.xml_namespaces PURGE;
drop table almatest.xml_roles PURGE;
drop table almatest.xml_schemanamespaces PURGE;
drop table almatest.xml_schema_entities PURGE;
drop table almatest.xml_users PURGE;
drop table almatest.xml_userroles PURGE;
DROP TABLE ALMATEST.xml_logEntries PURGE;
drop type almatest.string_varray; 
drop table almatest.alma_alarms PURGE; 
drop sequence ALMATEST.uid_seq;

CREATE SEQUENCE ALMATEST.uid_seq START WITH 20 NOMAXVALUE NOCYCLE;

CREATE TYPE almatest.string_varray AS VARRAY(50) OF VARCHAR2(1024);
/
CREATE TABLE ALMATEST.xml_logEntries (  Log_Level  NUMBER(2) NOT NULL,  LogTimeStamp TIMESTAMP(6) NOT NULL,  Message  VARCHAR2(2048), Filename  VARCHAR2(64),  Line  NUMBER(10),   Routine  VARCHAR2(256),  SourceObject VARCHAR2(64),  Host  VARCHAR2(64),  Process  VARCHAR2(64),  Context  VARCHAR2(64),  Thread  VARCHAR2(64),  LogId  VARCHAR2(64),  Priority VARCHAR2(64),  Uri  VARCHAR2(64),  Audience VARCHAR2(64),  Alma_Array  VARCHAR2(64),  Antenna  VARCHAR2(64), dataStrings almatest.string_varray, xml VARCHAR2(4000), steEnv VARCHAR2(30));
 
CREATE INDEX ALMATEST.log_ts_ind ON ALMATEST.xml_logEntries(LogTimeStamp);
create index log_ste_ind on xml_logentries(steEnv);

CREATE TABLE ALMATEST.XML_METAINFO ( 
  NAME 		VARCHAR2(32), 
  VALUE 	VARCHAR2(128), CONSTRAINT metainfo_pk PRIMARY KEY (name)); 

-- make sure there is an archive ID. It has a habit of disappearing in the unit tests.
insert into almatest.xml_metainfo (name, value) values ('archiveID', 'UT01');

CREATE TABLE ALMATEST.XML_NAMESPACES ( 
  PREFIX 		VARCHAR2(16), 
  NAMESPACE 		VARCHAR2(128), 
CONSTRAINT namespace_pk PRIMARY KEY (PREFIX) ); 


CREATE TABLE ALMATEST.XML_ROLES ( 
  ROLENAME 		VARCHAR2(64)	NOT NULL, CONSTRAINT roles_pk PRIMARY KEY (rolename) ); 


CREATE TABLE ALMATEST.XML_SCHEMANAMESPACES ( 
  SCHEMAUID 		VARCHAR2(33), 
  PREFIX 		VARCHAR2(16), CONSTRAINT schemanamespaces_pk PRIMARY KEY (schemauid, prefix)); 


CREATE TABLE ALMATEST.XML_SCHEMA_ENTITIES ( 
  ARCHIVE_UID 		VARCHAR2(33)	NOT NULL, 
  SCHEMANAME 		VARCHAR2(32)	NOT NULL, 
  VERSION 		NUMBER(16)	NOT NULL, 
  TIMESTAMP 		TIMESTAMP(6)	NOT NULL, 
  XML 			CLOB, 
  SCHEMAUID 		VARCHAR2(33), 
  OWNER 		VARCHAR2(128), 
  DELETED 		NUMBER(1), 
  READPERMISSIONS 	VARCHAR2(128), 

  WRITEPERMISSIONS 	VARCHAR2(128), 
  HIDDEN 		NUMBER(1), 
  DIRTY 		NUMBER(1), 
  VIRTUAL 		NUMBER(1), CONSTRAINT schema_uid_pk PRIMARY KEY (archive_uid) );

CREATE TABLE ALMA_ALARMS (descriptor NUMBER(10), user_timestamp_secs NUMBER(20), user_timestamp_ms NUMBER(20), activated_by_backup NUMBER(1),terminated_by_backup NUMBER(1), family VARCHAR2(100), member VARCHAR2(100), code NUMBER(10), user_properties VARCHAR2(1000), source_name VARCHAR2(100), source_hostname VARCHAR2(100), source_timestamp_sec NUMBER(20), source_timestamp_ms NUMBER(20), backup NUMBER(1), version VARCHAR2(100));

CREATE TABLE ALMATEST.XML_USERS ( 
  USERNAME 		VARCHAR2(64)	NOT NULL, CONSTRAINT users_pk PRIMARY KEY (username) ); 

CREATE TABLE ALMATEST.XML_USERROLES ( 
  USERNAME VARCHAR2(64)	NOT NULL, 
  ROLENAME VARCHAR2(64)	NOT NULL, CONSTRAINT userroles_pk PRIMARY KEY (username, rolename) );

CREATE OR REPLACE VIEW ALMATEST.UID_LOOKUP 
AS 
SELECT ARCHIVE_UID, 'schema' AS SCHEMANAME 
FROM ALMATEST.XML_SCHEMA_ENTITIES;

exit
