FROM node:12.2.0

# install chrome for protractor tests
RUN wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add -
RUN sh -c 'echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google.list'
RUN apt-get update && apt-get install -yq google-chrome-stable

# set working directory
WORKDIR /app

# add `/app/node_modules/.bin` to $PATH
ENV PATH /app/node_modules/.bin:$PATH

EXPOSE 9222

RUN npm install -g @angular/cli@11.0.2

# install and cache app dependencies
COPY package.json /app/package.json
COPY package-lock.json /app/package-lock.json
RUN npm ci
# Run and cache the compatibility compiler
RUN /app/node_modules/.bin/ngcc --properties es2015 --tsconfig "./tsconfig.app.json"
COPY . /app

CMD ng serve --host 0.0.0.0 --configuration local --proxy-config proxy.conf.docker.json
