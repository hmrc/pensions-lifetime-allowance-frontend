#!/bin/bash

sbt "run 9010 -Dplay.http.router=testOnlyDoNotUseInAppConf.Routes"