#!/bin/bash

ps auxf | grep  publisher_server | grep -v grep | awk '{print $2}' | xargs -i -t kill -9 {}
ps auxf | grep  formatter_server | grep -v grep | awk '{print $2}' | xargs -i -t kill -9 {}

nohup java -jar bin/formatter_server.jar server 2>&1 > f.log &
nohup java -jar bin/publisher_server.jar server 2>&1 > p.log &




