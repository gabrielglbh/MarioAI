call java_path

call compilar.bat

FOR %%A IN (0 75 100) DO (
    echo "------------ Seed %%A -----------"
    call ejecutar.bat P3HumanAgent -ls %%A -ld 0
    python recortar.py
)
exit 0