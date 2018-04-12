#!/usr/bin/python

import os, sys

readFile = open("ejemplos.csv")

lines = readFile.readlines()

readFile.close()
w = open("ejemplos.csv",'w')

w.writelines([item for item in lines[:-1]])

print("Succesfully cleaned the file")

w.close()
