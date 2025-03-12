## datasahi flow

[Home Page](https://datasahi.com)

`datasahi flow` is a tool to transfer data between different data sources.

---
### Features
- [x] transfer data from redis to a database
- [x] easy to use, a single binary to start
- [x] flexible, filter data what to transfer and not

---
### Usage
- download the latest zip file from the releases. The zip file contains the jar file, a starter script and a sample config file 
- needs jre 17 to execute
- create a env file or set these variables in environment
```shell
export DATASAHI_PORT=8082
export DATASAHI_WORK_DIR=/custom/work/dir
export DATASAHI_CONFIG_PATHS=/custom/path/flow.json
```
Sample env file - `datasahi.env`
```shell
DATASAHI_PORT=8082
DATASAHI_WORK_DIR=/custom/work/dir
DATASAHI_CONFIG_PATHS=/custom/path/flow.json
```
- use this command to start the server
 ```shell
  chmod +x start-datasahi-flow.sh
  ./start-datasahi-flow.sh
```

Sample config json - `flow.json`
```json
{
  "dataservers": [
    {
      "id": "redis-local",
      "type": "REDIS",
      "url": "redis://localhost:6379",
      "host": "localhost",
      "port": 6379
    },
    {
      "id": "mysql-local",
      "type": "JDBC",
      "url": "jdbc:mysql://localhost:3306/crm",
      "user": "xxx",
      "password": "xxx",
      "driverClass" : "com.mysql.jdbc.Driver"
    },
    {
      "id": "sqlserver-dev",
      "type": "JDBC",
      "url": "jdbc:sqlserver://host:port;trustServerCertificate=true;databaseName=crm;",
      "user": "xxx",
      "password": "xxx",
      "driverClass" : "com.microsoft.sqlserver.jdbc.SQLServerDriver"
    }
  ],
  "flows": [
    {
      "id": "redis-to-sqlserver",
      "source": {
        "server": "redis-local",
        "type": "hash",
        "dataset": "D2C_LEAD_TRACKER",
        "idField": "id",
        "tsField": "updatedTs",
        "dataFilter": "status == 'SUBMITTED'",
        "queueSize": 10000,
        "tsCheck": true,
        "tsSkipSeconds": 60
      },
      "sink": {
        "server": "sqlserver-dev",
        "type": "table",
        "dataset": "crm.leads",
        "crud": {
          "create": "insert into crm..lead_tracker(id,name,age,status,created_at,updated_at) values (:id,:name,:age,:status,:createdTs,:updatedTs)",
          "read": "select id from crm..lead_tracker where id = :id",
          "update": "update crm..lead_tracker set name = :name, age = :age, status = :status, updated_at = :updatedTs where id = :id"
        }
      },
      "batch": {
        "maxCount": 500,
        "maxMillis": 1000
      }
    }
  ]
}

```
