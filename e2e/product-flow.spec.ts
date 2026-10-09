import { expect, test, type Browser, type Page } from '@playwright/test';
import { mkdir } from 'node:fs/promises';

const evidence = 'artifacts/screenshots';

async function capture(page: Page, name: string) {
  await mkdir(evidence, { recursive: true });
  await page.screenshot({
    path: `${evidence}/${name}.jpg`,
    type: 'jpeg',
    quality: 88,
    fullPage: true,
  });
}

async function register(page: Page) {
  await page.goto('/');
  await page.getByLabel('Display name').fill('Abi Eka');
  await page.getByLabel('Email').fill(`abi.${Date.now()}@example.test`);
  await page.getByLabel('Password').fill('portfolio-txu-password');
  await page.getByRole('button', { name: 'Begin with TXU' }).click();
  await expect(
    page.getByRole('heading', { name: 'Appreciation worth keeping.' }),
  ).toBeVisible();
  await capture(page, '01-member-dashboard-empty');
}

async function issueReceipt(page: Page) {
  await page.getByRole('button', { name: '+ New receipt' }).click();
  await page.getByLabel('Recipient name').fill('Nadia Prameswari');
  await page
    .getByLabel('Receipt title')
    .fill('Made the difficult launch feel possible');
  await page.getByLabel('Category').selectOption('TEAMWORK');
  await page
    .getByLabel('What did they contribute?')
    .fill(
      'Your calm decisions and precise reviews kept everyone moving when the release changed at the last minute.',
    );
  await capture(page, '02-receipt-composer');
  await page.getByRole('button', { name: 'Preview receipt' }).click();
  await expect(
    page.getByRole('heading', { name: 'These words become permanent.' }),
  ).toBeVisible();
  await capture(page, '03-immutable-preview');
  await page.getByRole('button', { name: 'Confirm and issue' }).click();
  await expect(
    page.getByRole('heading', { name: /verifiable history/i }),
  ).toBeVisible();
  await capture(page, '04-issued-links');
  const links = await page.locator('.link-box code').allTextContents();
  return { publicUrl: links[0], recipientUrl: links[1] };
}

async function acknowledge(browser: Browser, recipientUrl: string) {
  const context = await browser.newContext();
  const page = await context.newPage();
  await page.goto(recipientUrl);
  await expect(
    page.getByRole('heading', { name: /Acknowledge the contribution/i }),
  ).toBeVisible();
  await capture(page, '06-recipient-acknowledgement');
  await page.getByRole('button', { name: 'Acknowledge receipt' }).click();
  await expect(page.getByText(/Acknowledgement recorded/i)).toBeVisible();
  await capture(page, '07-acknowledged-result');
  await context.close();
}

async function reportReceipt(browser: Browser, publicUrl: string) {
  const context = await browser.newContext();
  const page = await context.newPage();
  await page.goto(publicUrl);
  await expect(page.getByText('VERIFIED ACKNOWLEDGED')).toBeVisible();
  await capture(page, '08-public-verified-receipt');
  await page.getByRole('button', { name: 'Report this receipt' }).click();
  await page.getByLabel('Reason').selectOption('OTHER');
  await page
    .getByLabel('Context')
    .fill('End-to-end portfolio moderation check.');
  await page.getByRole('button', { name: 'Submit report' }).click();
  await expect(page.getByText(/Report received/i)).toBeVisible();
  await context.close();
}

async function moderate(browser: Browser, publicUrl: string) {
  const context = await browser.newContext();
  const page = await context.newPage();
  await page.goto('/');
  await page.getByRole('button', { name: 'Sign in' }).click();
  await page.getByLabel('Email').fill('moderator@txu.local');
  await page.getByLabel('Password').fill('local-txu-moderator');
  await page.getByRole('button', { name: 'Open my receipts' }).click();
  await page.getByRole('button', { name: 'Moderation' }).click();
  await expect(
    page.getByRole('heading', { name: 'Moderation queue' }),
  ).toBeVisible();
  await capture(page, '09-moderation-queue');
  await page.locator('.report-row').first().click();
  await page.getByRole('button', { name: 'Hide receipt' }).click();
  await page.goto(publicUrl);
  await expect(page.getByText('HIDDEN')).toBeVisible();
  await capture(page, '10-hidden-public-state');
  await page.goto('/moderation');
  await page.locator('.report-row').first().click();
  await page.getByRole('button', { name: 'Restore receipt' }).click();
  await context.close();
}

test('complete TXU product journey', async ({ page, browser }) => {
  await register(page);
  const links = await issueReceipt(page);
  await page.goto(links.publicUrl);
  await expect(
    page.getByText('Cryptographic signature verified'),
  ).toBeVisible();
  await capture(page, '05-public-verification');
  await acknowledge(browser, links.recipientUrl);
  await reportReceipt(browser, links.publicUrl);
  await moderate(browser, links.publicUrl);
  await page.goto(links.publicUrl);
  await expect(page.getByText('VERIFIED ACKNOWLEDGED')).toBeVisible();
  await capture(page, '11-restored-verification');
  await page.getByRole('link', { name: 'My receipts' }).click();
  await page
    .getByRole('button', { name: /Open Made the difficult launch/i })
    .click();
  await page.getByRole('button', { name: 'Revoke receipt' }).click();
  await page
    .getByLabel('Reason')
    .fill('The sender intentionally closed this demonstration receipt.');
  await page.getByRole('button', { name: 'Confirm revocation' }).click();
  await page.goto(links.publicUrl);
  await expect(page.getByText('REVOKED', { exact: true })).toBeVisible();
  await capture(page, '12-revoked-public-state');
});

test('mobile landing remains usable', async ({ browser }) => {
  const context = await browser.newContext({
    viewport: { width: 390, height: 844 },
  });
  const page = await context.newPage();
  await page.goto('/');
  await expect(
    page.getByRole('heading', { name: /Make your thank-you last/i }),
  ).toBeVisible();
  await capture(page, '13-mobile-landing');
  await context.close();
});
