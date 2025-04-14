#!/bin/bash

sbt clean scalafmt Test/scalafmt IntegrationTest/scalafmt coverage test it/test scalafmtCheckAll coverageReport