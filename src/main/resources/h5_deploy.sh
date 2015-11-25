#!/bin/bash
source ~/.nvm/nvm.sh
export NODE_PATH="/src/node_modules/"
nvm use iojs-v2.3.0
echo 'export PATH=$PATH:/usr/local/bin' >> ~/.bashrc
npm config set registry http://r.npm.sankuai.com

npm install
#mv config/dev.js.sample config/dev.js
sed 's/sr.test.meituan.com/localhost:8088/g' -i config/test.js
mkdir -p /usr/local/tmp/
sed 's/\/var\/sankuai\/logs\/paidui/\/usr\/local\/tmp/g' -i pm2-testy.json
npm run-script start-testy
