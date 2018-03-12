#!/bin/bash

bash compilar.sh

for i in 0 25 50 75 100 3000 10000
  do
    echo "------------ Seed $i -----------"
    bash ejecutar.sh T3BotAgent -ls $i -vis off
    python recortar.py
    bash ejecutar.sh T3HumanAgent -ls $i
    python recortar.py
  done
exit 0
