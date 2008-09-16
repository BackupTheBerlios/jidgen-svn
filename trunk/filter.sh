#!/bin/bash

# This is an example filter shell script which you
# may freely modify to match your needs.
# All output to stdout is passed on to the debug logger.
# All output to stderr is passed on to the error logger and
# is therefore visible with the standard logging level.
# For more help on this see the jidgen documentation at
# http://jidgen.berlios.de

# get the ID to check from the command line
ID=$1


# Example: Output some debug info to stdout (logged to debug level)
echo "Got ID to check for collisions: \"$ID\"" >&1

# Example: Output error messages to stderr (logged to error level)
#echo "Oh oh!" >&2


# do your collision tests here
# ...





# exit with code 0 to filter the given ID
# exit 0

# exit with code 1 to keep the given ID
exit 1
