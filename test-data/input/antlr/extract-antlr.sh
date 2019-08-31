#!/bin/bash

for version in ./*
do
	cp $version/bin/antlr.jar $version
done

