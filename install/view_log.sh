#!/bin/sh

ps -ef | grep EmoldinoGW | grep -v 'grep' | awk '{print "_PID="$2}' | xargs journalctl -f
exit 0
