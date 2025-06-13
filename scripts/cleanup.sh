#!/bin/bash

# Skripts servera procesa apturēšanai uz porta 8082
# Izmanto lsof komandu, lai atrastu procesu, kas izmanto portu 8082

# Atrod un aptur procesu, kas izmanto portu 8082
pid=$(lsof -ti:8082)
if [ ! -z "$pid" ]; then
    echo "Aptur procesu uz porta 8082 (PID: $pid)"
    kill -9 $pid
else
    echo "Nav atrasts neviens process uz porta 8082"
fi

# Gaida, līdz ports tiek atbrīvots
echo "Gaida 2 sekundes, lai ports tiktu atbrīvots..."
sleep 2

echo "Ports 8082 ir gatavs izmantošanai"