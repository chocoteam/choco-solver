#!/bin/sh
# Get arguments
# 1. the jar file
JAR=$1
# 2. command arguments
ARGS=$2
# 3. instance to solve
FILE=$3

# Configuration of the command
JARGS="-Xss64m -Xms64m -Xmx4096m -server"
PARSER="org.chocosolver.parser.xcsp.ChocoXCSP"

# Build the command
CMD="java ${JARGS} -cp .:${JAR} ${PARSER} ${ARGS} \"${FILE}\""

# Run the command
eval ${CMD}
