FROM node:12.2.0

# install chrome for protractor tests
RUN wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add -
RUN sh -c 'echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google.list'
RUN apt-get update && apt-get install -yq google-chrome-stable

WORKDIR /app

#RUN npm install -g protractor

COPY package.json .
COPY package-lock.json .
RUN npm install

RUN node node_modules/protractor/bin/webdriver-manager update

ENV PATH /app/node_modules/.bin:$PATH

COPY . /app
RUN npm run tsc

CMD sh test.sh
