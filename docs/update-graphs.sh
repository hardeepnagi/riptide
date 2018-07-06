#!/bin/sh -ex

# colors: https://yuml.me/69f3a9ba.svg
http --follow --check-status "http://yuml.me/diagram/scruffy/class/$(paste -s -d',' docs/graph.yuml.txt)" > $(dirname $0)/graph.png
