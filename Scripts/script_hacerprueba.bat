call compilar.bat

call ejecutar.bat P1BotAgent -ls 0 -vis off
python recortar.py
call ejecutar.bat P1BotAgent -ls 2 -vis off
python recortar.py
call ejecutar.bat P1BotAgent -ls 36 -vis off
python recortar.py
call ejecutar.bat P1BotAgent -ls 18 -vis off
python recortar.py
call ejecutar.bat P1BotAgent -ls 45 -vis off
python recortar.py
call ejecutar.bat P1BotAgent -ls 100 -vis off
python recortar.py
call ejecutar.bat P1BotAgent -ls 25 -vis off
python recortar.py

mv ejemplos.arff bot_inst.arff

call ejecutar.bat P1HumanAgent -ls 0
python recortar.py
call ejecutar.bat P1HumanAgent -ls 2
python recortar.py
call ejecutar.bat P1HumanAgent -ls 36
python recortar.py
call ejecutar.bat P1HumanAgent -ls 18
python recortar.py
call ejecutar.bat P1HumanAgent -ls 45
python recortar.py
call ejecutar.bat P1HumanAgent -ls 100
python recortar.py
call ejecutar.bat P1HumanAgent -ls 25
python recortar.py

move ejemplos.arff human_inst.arff
echo "END OF SCRIPT AND EXECUTION"
