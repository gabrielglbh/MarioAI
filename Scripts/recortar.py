#!/usr/bin/python

import os, sys

readFile = open("ejemplos.arff")

lines = readFile.readlines()

readFile.close()
w = open("ejemplos.arff",'w')

w.writelines([item for item in lines[:-1]])

print("Succesfully cleaned the arff")

w.close()
