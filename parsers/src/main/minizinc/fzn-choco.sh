#!/bin/bash

# Call the python script with the arguments passed to this script
python3 "$(dirname "$0")/fzn-choco.py" "$@"