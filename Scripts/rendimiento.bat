call compilar.bat
call ejecutar.bat P1BotAgent -ls 0
python recortar.py

move ejemplos.arff P1bot_inst.arff

call ejecutar.bat ClassificationAgent -ls 0
python recortar.py

move ejemplos.arff ClassificationAgent_inst.arff
echo "END OF SCRIPT AND EXECUTION"
