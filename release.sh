#!/bin/bash

clj -A:fig:min
cp -r resources/public/* docs/
mkdir -p docs/cljs-out
cp target/public/cljs-out/dev-main.js docs/cljs-out/  


