#!/usr/bin/env bash

sbt clean compile scalafmt scalastyle coverage Test/test it/test coverageOff coverageReport