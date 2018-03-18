call compilar.bat

FOR %%A in (0 1 2 25 45) DO (
    FOR %%B in (0 25 50 75 100 3000 10000) DO (
	    echo "------------ Dificultad %%A -----------"
	    call ejecutar.bat P1BotAgent -ls %%B -ld %%A -vis off
	    python recortar.py
    )
)
exit 0
