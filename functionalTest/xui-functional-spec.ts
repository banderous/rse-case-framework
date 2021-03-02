import {AppPage} from './app.po';
import {$, browser, by, element, logging, protractor} from 'protractor';

describe('xui end to end tests', function() {
  let page: AppPage;

  beforeEach(() => {
    page = new AppPage();
  });

  it('should display case list', () => {
    browser.get('http://xui-manage-cases:3000');
    browser.wait(protractor.ExpectedConditions.presenceOf(
        $('#wb-jurisdiction')), 10000);
    element(by.xpath('//button[contains(text(), "Apply")]')).click();
    browser.wait(protractor.ExpectedConditions.presenceOf(
        element(by.xpath('//a[@href="/cases/case-details/2542345663454321"]'))), 10000);
  });

  it('loads case details', () => {
    element(by.xpath('//span[contains(text(), "2542-3456-6345-4321")]')).click();
    browser.wait(protractor.ExpectedConditions.presenceOf(
        element(by.xpath('//div[contains(text(), "Parties")]'))), 10000);
  });

  afterEach(async () => {
    // Assert that there are no errors emitted from the browser
    const logs = await browser.manage().logs().get(logging.Type.BROWSER);
    expect(logs).not.toContain(jasmine.objectContaining({
      level: logging.Level.SEVERE,
    } as logging.Entry));
  });
});
