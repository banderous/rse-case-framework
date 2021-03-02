
const { execSync } = require("child_process").execSync;
const { SpecReporter, StacktraceOption } = require('jasmine-spec-reporter');
var HtmlScreenshotReporter = require('protractor-jasmine2-screenshot-reporter');
var reporter = new HtmlScreenshotReporter({
  dest: 'build/functional/screenshots',
  filename: 'failure-report.html'
});

exports.config = {
  directConnect: true,
  framework: 'jasmine',
  capabilities: {
    browserName: 'chrome',
    chromeOptions: {
      args: [ "--headless", "--disable-gpu", "--disable-dev-shm-usage", "--no-sandbox", "--remote-debugging-port=9222", "--remote-debugging-address=0.0.0.0" ]
    }
  },
  specs: [
    'dist/*spec.js'
  ],
  // Setup the report before any tests start
  beforeLaunch: function() {
    return new Promise(function(resolve){
      reporter.beforeLaunch(resolve);
    });
  },
  // Close the report after all tests finish
  afterLaunch: function(exitCode) {
    return new Promise(function(resolve){
      reporter.afterLaunch(resolve.bind(this, exitCode));
    });
  },
  async onPrepare() {
    const fs = require('fs');
    fs.mkdirSync('build/functional', { recursive: true })
    require('jasmine-expect');
    require('ts-node').register({
      project: require('path').join(__dirname, './tsconfig.json')
    });
    jasmine.getEnv().addReporter(reporter);
    jasmine.getEnv().addReporter(new SpecReporter({
      spec: {
        displayStacktrace: StacktraceOption.PRETTY
      }
    }));

    require('child_process').execSync("sh create-user.sh")

    await browser.waitForAngularEnabled(false);
    while (true) {
      try {
        console.log("Waiting for login screen...")
        await browser.get('http://xui-manage-cases:3000');
        await browser.sleep(2000)

        try {
          console.log("Checking for login")
          await browser.driver.findElement(by.id('wb-jurisdiction'));
          console.log("Already logged in")
          break;
        } catch (error) {
          console.log(error)
          console.log("Not logged in")
        }

        console.log("Looking for login page")
        await browser.driver.findElement(by.id('username'));
        console.log("login page loaded")
        await browser.element(by.id('username')).sendKeys('super@gmail.com')
        console.log("entered username")
        await browser.element(by.id('password')).sendKeys('p')
        console.log("entered password")
        await browser.element(by.css('body > form > div > button')).click()
        console.log("clicked login")

        await browser.sleep(10000)

        console.log("Looking for jurisdictions")
        await browser.driver.findElement(by.id('wb-jurisdiction'));
        console.log("xui loaded")
        break;
      } catch (error) {
        console.log(error)
        console.log("Login page not ready")
        await browser.sleep(1000)
      }
    }
  }
};
