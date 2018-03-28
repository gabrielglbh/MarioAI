call java_path

call compilar.bat

FOR %%A IN (0 25 50 75 100 3000 10000) DO (
    echo "------------ Seed %%A -----------"
    call ejecutar.bat P2HumanAgent -ls %%A -ld 0
    python recortar.py
    call ejecutar.bat P2HumanAgent -ls %%A -ld 0
    python recortar.py
)
exit 0