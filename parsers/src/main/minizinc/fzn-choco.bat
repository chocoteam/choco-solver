@echo off
:: Call the python script with the arguments passed to this script
set "PY=%~dp0fzn-choco.py"
python3 %PY% %*