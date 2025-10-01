# pensions-lifetime-allowance-frontend

[![Apache-2.0 license](http://img.shields.io/badge/license-Apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

### Pensions Lifetime Allowance Frontend

This is the repository for the pensions lifetime allowance frontend. This service provides clients with a way to view and manage their pension protections.

#### Included scripts

##### `run.sh`

* Starts the Play! server on [localhost:9010](http://localhost:9010) with test routes enabled.


### Start dependencies via Service Manager

To start all dependencies and services for pensions lifetime allowance, use the following command:
```
sm2 --start PLA_ALL
```
If you are running performance tests and also need a specific branch backend running, stop service from sm2 and start with
```
    sbt run -Dapplication.router=testOnlyDoNotUseInAppConf.Routes
```
### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
