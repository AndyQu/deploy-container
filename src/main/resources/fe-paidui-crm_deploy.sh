#!/bin/bash
source ~/.nvm/nvm.sh
export NODE_PATH="/src/node_modules/"
nvm use iojs-v2.3.0
echo 'export PATH=$PATH:/usr/local/bin' >> ~/.bashrc
npm config set registry http://r.npm.sankuai.com

npm install
npm run start
