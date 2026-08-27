#!/bin/bash

sbt clean scalafmt Test/scalafmt IntegrationTest/scalafmt scalafmtSbt coverage test it/test scalafmtCheckAll coverageReport