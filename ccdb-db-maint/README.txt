This the CCDB DB Maintenance folder.

The code in here is used to initialize the DB for the application or modify the 
data-model (and migrate the data) as the application evolves.

The database maintenance is performed using Flyway (https://flywaydb.org/) plugin for Maven.

!!! 
!!! The database initialization and migration SQL scripts are written for PostgreSQL RDBMS.
!!!

The instructions to initialize the database can be found in conf-core/documentation/CCDB-devenv-instructions.docx
